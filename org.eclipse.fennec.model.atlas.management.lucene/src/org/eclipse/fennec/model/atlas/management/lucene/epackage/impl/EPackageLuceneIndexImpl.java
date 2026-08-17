/**
 * Copyright (c) 2012 - 2026 Data In Motion and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.management.lucene.epackage.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.model.atlas.management.lucene.epackage.EPackageLuceneIndex;
import org.eclipse.fennec.model.atlas.management.lucene.epackage.EPackageSearchQuery;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Standalone Lucene index for EPackage-specific fields.
 * Manages its own index directory and can be reused independently
 * of the ObjectMetadata registry index.
 *
 * <p>Thread-safe via {@link ReentrantReadWriteLock}. Uses NRT search
 * via {@link SearcherManager}.</p>
 *
 * @author ilenia
 * @since Apr 8, 2026
 * @see EPackageLuceneIndex
 */
@Component(name = "EPackageLuceneIndex", service = EPackageLuceneIndex.class, configurationPid = "EPackageLuceneIndex", configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = EPackageLuceneIndexImpl.Config.class)
public class EPackageLuceneIndexImpl implements EPackageLuceneIndex, AutoCloseable {

	private static final Logger LOGGER = Logger.getLogger(EPackageLuceneIndexImpl.class.getName());

	// Lucene field names
	static final String FIELD_OBJECT_ID = "objectId";
	static final String FIELD_SCOPE = "scope";
	static final String FIELD_REGISTRY = "registry";
	static final String FIELD_STAGE = "stage";
	static final String FIELD_NSURI = "epackage_nsUri";
	static final String FIELD_NSURI_ANALYZED = "epackage_nsUri_analyzed";
	static final String FIELD_NAME = "epackage_name";
	static final String FIELD_NAME_ANALYZED = "epackage_name_analyzed";
	static final String FIELD_NSPREFIX = "epackage_nsPrefix";
	static final String FIELD_NSPREFIX_ANALYZED = "epackage_nsPrefix_analyzed";
	static final String FIELD_CLASSIFIER_NAMES = "epackage_classifierNames";
	static final String FIELD_FEATURE_NAMES = "epackage_featureNames";
	static final String FIELD_FEATURE_TYPES = "epackage_featureTypes";
	static final String FIELD_FEATURE_NAME_TYPE_PAIRS = "epackage_featureNameTypePairs";
	/** Separator for name/type pairs. Underscore is not split by StandardAnalyzer (unlike colon). */
	static final String PAIR_SEPARATOR = "_";

	private Directory directory;
	private IndexWriter indexWriter;
	private SearcherManager searcherManager;
	private StandardAnalyzer analyzer;
	private final ReadWriteLock indexLock = new ReentrantReadWriteLock();



	@ObjectClassDefinition(name = "EPackageLuceneIndex Configuration", description = "Configuration for centralized EPackage Lucene Index")
    public @interface Config {
		 /**
         * Directory path for the shared Lucene index. This should be independent of any
         * storage service workspace.
         */
        @AttributeDefinition(name = "Index Folder", description = "Directory path for the epackage Lucene index", required = true)
        String index_folder() default "/tmp/epackage-index";
	}
	/**
	 * Creates and initializes the index at the given path.
	 *
	 * @param indexPath the directory where the Lucene index will be stored
	 * @throws IOException if the index cannot be created or opened
	 */
	@Activate
	public EPackageLuceneIndexImpl(Config config) throws IOException {
		Path indexPath = Path.of(config.index_folder());
		this.directory = FSDirectory.open(indexPath);
		this.analyzer = new StandardAnalyzer();

		IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
		try {
			this.indexWriter = new IndexWriter(directory, writerConfig);
		} catch (IOException | RuntimeException e) {
			LOGGER.warning("Index corruption detected, recreating index: " + e.getMessage());
			cleanCorruptedIndex(indexPath);

			this.directory = FSDirectory.open(indexPath);
			IndexWriterConfig recoveryConfig = new IndexWriterConfig(analyzer);
			recoveryConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
			this.indexWriter = new IndexWriter(directory, recoveryConfig);
		}

		this.searcherManager = new SearcherManager(indexWriter, null);
		LOGGER.info("Initialized EPackage Lucene index at: " + indexPath);
	}

	@Override
	public void index(ObjectMetadata metadata, EPackage ePackage) {
		if (metadata == null || ePackage == null) {
			throw new IllegalArgumentException("metadata and ePackage must not be null");
		}
		String objectId = metadata.getObjectId();
		if (objectId == null || objectId.isEmpty()) {
			throw new IllegalArgumentException("ObjectMetadata.objectId must not be null or empty");
		}

		indexLock.writeLock().lock();
		try {
			indexWriter.deleteDocuments(new Term(FIELD_OBJECT_ID, objectId));
			Document doc = createDocument(metadata, ePackage);
			indexWriter.addDocument(doc);
			indexWriter.commit();
			searcherManager.maybeRefresh();

			LOGGER.fine("Indexed EPackage for object: " + objectId);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Failed to index EPackage for object: " + objectId, e);
		} finally {
			indexLock.writeLock().unlock();
		}
	}

	@Override
	public void remove(String objectId) {
		if (objectId == null || objectId.isEmpty()) {
			return;
		}

		indexLock.writeLock().lock();
		try {
			indexWriter.deleteDocuments(new Term(FIELD_OBJECT_ID, objectId));
			indexWriter.commit();
			searcherManager.maybeRefresh();

			LOGGER.fine("Removed EPackage from index: " + objectId);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Failed to remove EPackage from index: " + objectId, e);
		} finally {
			indexLock.writeLock().unlock();
		}
	}

	@Override
	public SearchResult search(EPackageSearchQuery query) {
		if (query == null) {
			return new SearchResult(List.of(), 0);
		}

		indexLock.readLock().lock();
		try {
			IndexSearcher searcher = searcherManager.acquire();
			try {
				Query luceneQuery = buildQuery(query);
				int maxHits = query.getOffset() + query.getLimit();

				TopDocs topDocs = searcher.search(luceneQuery, maxHits);
				long totalHits = topDocs.totalHits.value;

				List<SearchHit> hits = new ArrayList<>();
				ScoreDoc[] scoreDocs = topDocs.scoreDocs;
				for (int i = query.getOffset(); i < scoreDocs.length; i++) {
					Document doc = searcher.storedFields().document(scoreDocs[i].doc);
					hits.add(new SearchHit(
							doc.get(FIELD_OBJECT_ID),
							doc.get(FIELD_SCOPE),
							doc.get(FIELD_REGISTRY),
							doc.get(FIELD_STAGE)));
				}

				return new SearchResult(hits, totalHits);
			} finally {
				searcherManager.release(searcher);
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Failed to search EPackage index", e);
			return new SearchResult(List.of(), 0);
		} finally {
			indexLock.readLock().unlock();
		}
	}

	@Override
	@Deactivate
	public void close() throws Exception {
		if (searcherManager != null) {
			try {
				searcherManager.close();
			} catch (NoSuchFileException | FileNotFoundException e) {
				LOGGER.fine("SearcherManager close skipped - index directory already removed");
			}
		}
		if (indexWriter != null) {
			try {
				if (indexWriter.isOpen()) {
					indexWriter.rollback();
				}
			} catch (NoSuchFileException | FileNotFoundException e) {
				LOGGER.fine("IndexWriter rollback skipped - index directory already removed");
			}
		}
		if (directory != null) {
			try {
				directory.close();
			} catch (NoSuchFileException | FileNotFoundException e) {
				LOGGER.fine("Directory close skipped - index directory already removed");
			}
		}
		if (analyzer != null) {
			analyzer.close();
		}

		LOGGER.info("Closed EPackage Lucene index");
	}

	// -- Document creation --

	private Document createDocument(ObjectMetadata metadata, EPackage ePackage) {
		Document doc = new Document();

		// Correlation fields from ObjectMetadata
		doc.add(new StringField(FIELD_OBJECT_ID, metadata.getObjectId(), Field.Store.YES));
		addStringFieldIfNotNull(doc, FIELD_SCOPE, metadata.getScope());
		addStringFieldIfNotNull(doc, FIELD_REGISTRY, metadata.getRegistry());
		addStringFieldIfNotNull(doc, FIELD_STAGE, metadata.getStage());

		// EPackage identity fields (dual indexed: exact + analyzed)
		addDualField(doc, FIELD_NSURI, FIELD_NSURI_ANALYZED, ePackage.getNsURI());
		addDualField(doc, FIELD_NAME, FIELD_NAME_ANALYZED, ePackage.getName());
		addDualField(doc, FIELD_NSPREFIX, FIELD_NSPREFIX_ANALYZED, ePackage.getNsPrefix());

		// Classifier names
		StringBuilder classifierNames = new StringBuilder();
		for (EClassifier classifier : ePackage.getEClassifiers()) {
			if (classifier.getName() != null) {
				if (classifierNames.length() > 0) {
					classifierNames.append(' ');
				}
				classifierNames.append(classifier.getName());
			}
		}
		if (classifierNames.length() > 0) {
			doc.add(new TextField(FIELD_CLASSIFIER_NAMES, classifierNames.toString(), Field.Store.YES));
		}

		// Structural feature names, types, and name:type pairs
		Set<String> featureNames = new LinkedHashSet<>();
		Set<String> featureTypes = new LinkedHashSet<>();
		Set<String> featureNameTypePairs = new LinkedHashSet<>();

		for (EClassifier classifier : ePackage.getEClassifiers()) {
			if (classifier instanceof EClass eClass) {
				for (EStructuralFeature feature : eClass.getEStructuralFeatures()) {
					String fName = feature.getName();
					String fType = feature.getEType() != null ? feature.getEType().getName() : null;

					if (fName != null) {
						featureNames.add(fName);
					}
					if (fType != null) {
						featureTypes.add(fType);
					}
					if (fName != null && fType != null) {
						featureNameTypePairs.add(fName + PAIR_SEPARATOR + fType);
					}
				}
			}
		}

		if (!featureNames.isEmpty()) {
			doc.add(new TextField(FIELD_FEATURE_NAMES, String.join(" ", featureNames), Field.Store.YES));
		}
		if (!featureTypes.isEmpty()) {
			doc.add(new TextField(FIELD_FEATURE_TYPES, String.join(" ", featureTypes), Field.Store.YES));
		}
		if (!featureNameTypePairs.isEmpty()) {
			doc.add(new TextField(FIELD_FEATURE_NAME_TYPE_PAIRS, String.join(" ", featureNameTypePairs), Field.Store.YES));
		}

		return doc;
	}

	private void addStringFieldIfNotNull(Document doc, String fieldName, String value) {
		if (value != null && !value.isEmpty()) {
			doc.add(new StringField(fieldName, value, Field.Store.YES));
		}
	}

	private void addDualField(Document doc, String exactField, String analyzedField, String value) {
		if (value != null && !value.isEmpty()) {
			doc.add(new StringField(exactField, value, Field.Store.YES));
			doc.add(new TextField(analyzedField, value, Field.Store.NO));
		}
	}

	// -- Query building --

	private Query buildQuery(EPackageSearchQuery query) {
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		boolean hasClause = false;

		// Scope chain filter (OR'd)
		if (query.getScopes() != null && !query.getScopes().isEmpty()) {
			BooleanQuery.Builder scopeBuilder = new BooleanQuery.Builder();
			for (String scope : query.getScopes()) {
				scopeBuilder.add(new TermQuery(new Term(FIELD_SCOPE, scope)), BooleanClause.Occur.SHOULD);
			}
			builder.add(scopeBuilder.build(), BooleanClause.Occur.MUST);
			hasClause = true;
		}

		// Stage filter (exact)
		if (query.getStage() != null && !query.getStage().isEmpty()) {
			builder.add(new TermQuery(new Term(FIELD_STAGE, query.getStage())), BooleanClause.Occur.MUST);
			hasClause = true;
		}

		// nsUri exact match
		if (query.getNsUriExact() != null && !query.getNsUriExact().isEmpty()) {
			builder.add(new TermQuery(new Term(FIELD_NSURI, query.getNsUriExact())), BooleanClause.Occur.MUST);
			hasClause = true;
		}

		// nsUri partial match (analyzed)
		hasClause |= addAnalyzedClause(builder, FIELD_NSURI_ANALYZED, query.getNsUri());

		// Package name partial match (analyzed)
		hasClause |= addAnalyzedClause(builder, FIELD_NAME_ANALYZED, query.getName());

		// nsPrefix partial match (analyzed)
		hasClause |= addAnalyzedClause(builder, FIELD_NSPREFIX_ANALYZED, query.getNsPrefix());

		// Classifier name (full-text)
		hasClause |= addAnalyzedClause(builder, FIELD_CLASSIFIER_NAMES, query.getClassifier());

		// Feature name (full-text)
		hasClause |= addAnalyzedClause(builder, FIELD_FEATURE_NAMES, query.getFeatureName());

		// Feature type (full-text)
		hasClause |= addAnalyzedClause(builder, FIELD_FEATURE_TYPES, query.getFeatureType());

		// Feature name:type pair (full-text) — convert colon to internal separator
		String pairValue = query.getFeatureNameTypePair();
		if (pairValue != null && pairValue.contains(":")) {
			pairValue = pairValue.replace(":", PAIR_SEPARATOR);
		}
		hasClause |= addAnalyzedClause(builder, FIELD_FEATURE_NAME_TYPE_PAIRS, pairValue);

		if (!hasClause) {
			return new org.apache.lucene.search.MatchAllDocsQuery();
		}

		return builder.build();
	}

	/**
	 * Adds an analyzed (TextField) clause to the boolean query builder.
	 *
	 * <p>The value is a search criterion, not query syntax: it goes through the same
	 * StandardAnalyzer the field was indexed with, so punctuation in it is analyzed away
	 * rather than parsed. Parsing it as a query string instead used to drop the clause
	 * whenever the value did not happen to be valid Lucene syntax — silently widening the
	 * result set, up to matching every package when it was the only criterion.</p>
	 *
	 * @return true if a clause was added
	 */
	private boolean addAnalyzedClause(BooleanQuery.Builder builder, String field, String value) {
		if (value == null || value.isEmpty()) {
			return false;
		}
		Query analyzed = new QueryBuilder(analyzer).createBooleanQuery(field, value, BooleanClause.Occur.SHOULD);
		if (analyzed == null) {
			// The value analyzed away to nothing (all punctuation): it can match nothing,
			// and saying so is better than dropping the criterion.
			analyzed = new TermQuery(new Term(field, value));
		}
		builder.add(analyzed, BooleanClause.Occur.MUST);
		return true;
	}

	// -- Index recovery --

	private void cleanCorruptedIndex(Path indexPath) {
		if (directory != null) {
			try {
				directory.close();
			} catch (Exception ex) {
				LOGGER.fine("Error closing corrupted directory: " + ex.getMessage());
			}
		}
		try {
			if (Files.exists(indexPath)) {
				Files.walk(indexPath)
						.filter(Files::isRegularFile)
						.forEach(file -> {
							try {
								Files.delete(file);
							} catch (IOException ioe) {
								LOGGER.fine("Could not delete corrupted file: " + file + " - " + ioe.getMessage());
							}
						});
			}
		} catch (IOException ioe) {
			LOGGER.warning("Error cleaning corrupted index directory: " + ioe.getMessage());
		}
	}
}
