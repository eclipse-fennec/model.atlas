/**
 * Copyright (c) 2012 - 2025 Data In Motion and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.management.lucene.registry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.eclipse.fennec.model.atlas.mgmt.registry.AbstractRegistryHelper;

/**
 * Lucene-based registry helper for fast metadata indexing and searching.
 * 
 * <p>
 * This implementation provides high-performance metadata indexing using Apache
 * Lucene. It handles only registry operations and does not mix storage
 * concerns.
 * </p>
 * 
 * <h3>Index Structure</h3>
 * <p>
 * Each document in the Lucene index contains:
 * </p>
 * <ul>
 * <li><strong>objectId</strong> - Object identifier (StringField, stored)</li>
 * <li><strong>uploadUser</strong> - Upload user (TextField, analyzed)</li>
 * <li><strong>uploadTime</strong> - Upload timestamp (LongPoint, stored)</li>
 * <li><strong>sourceChannel</strong> - Source channel (TextField,
 * analyzed)</li>
 * <li><strong>contentHash</strong> - Content hash (StringField, exact
 * match)</li>
 * <li><strong>objectType</strong> - Object type (TextField, analyzed)</li>
 * <li><strong>reviewUser</strong> - Review user (TextField, analyzed)</li>
 * <li><strong>reviewTime</strong> - Review timestamp (LongPoint, stored)</li>
 * <li><strong>reviewReason</strong> - Review reason (TextField, analyzed)</li>
 * <li><strong>status</strong> - Object status (StringField, exact match)</li>
 * <li><strong>objectName</strong> - Human-readable object name (TextField,
 * analyzed)</li>
 * <li><strong>role</strong> - Storage role (StringField, exact match)</li>
 * <li><strong>properties</strong> - Custom properties as key:value pairs
 * (TextField, analyzed)</li>
 * </ul>
 * 
 * <h3>Performance Features</h3>
 * <ul>
 * <li><strong>Near Real Time (NRT) Search</strong> - SearcherManager with
 * sub-millisecond latency</li>
 * <li><strong>Thread-Safe Operations</strong> - ReadWriteLock protection for
 * concurrent access</li>
 * <li><strong>Efficient Indexing</strong> - Single IndexWriter with optimized
 * commits</li>
 * <li><strong>Complex Queries</strong> - Boolean logic, time ranges, full-text
 * search</li>
 * </ul>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
public class LuceneRegistryHelper extends AbstractRegistryHelper {

    private static final Logger LOGGER = Logger.getLogger(LuceneRegistryHelper.class.getName());

    // Lucene field names
    public static final String FIELD_OBJECT_ID = "objectId";
    public static final String FIELD_UPLOAD_USER = "uploadUser";
    public static final String FIELD_UPLOAD_TIME = "uploadTime";
    public static final String FIELD_SOURCE_CHANNEL = "sourceChannel";
    public static final String FIELD_CONTENT_HASH = "contentHash";
    public static final String FIELD_OBJECT_TYPE = "objectType";
    public static final String FIELD_REVIEW_USER = "reviewUser";
    public static final String FIELD_REVIEW_TIME = "reviewTime";
    public static final String FIELD_REVIEW_REASON = "reviewReason";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_OBJECT_NAME = "objectName";
    public static final String FIELD_PROPERTIES = "properties";
    public static final String FIELD_SCOPE = "scope";
    public static final String FIELD_REGISTRY = "registry";
    public static final String FIELD_STAGE = "stage";
    public static final String FIELD_FINGERPRINT = "fingerprint";

    // Additional fields for advanced querying
    public static final String FIELD_LAST_CHANGE_USER = "lastChangeUser";
    public static final String FIELD_LAST_CHANGE_TIME = "lastChangeTime";
    public static final String FIELD_COMPLIANCE_CHECK_TIME = "complianceCheckTime";
    public static final String FIELD_COMPLIANCE_STATUS = "complianceStatus";
    public static final String FIELD_GOVERNANCE_DOCUMENTATION_ID = "governanceDocumentationId";
    public static final String FIELD_GENERATION_TRIGGER_FINGERPRINT = "generationTriggerFingerprint";
    public static final String FIELD_VERSION = "version";
    public static final String FIELD_OBJECT_REF = "objectRef";
    public static final String FIELD_OBJECT_METADATA_ID = "objectMetadataId";

    // Text fields for analyzed content (separate from exact match fields)
    public static final String FIELD_UPLOAD_USER_TEXT = "uploadUser_text";
    public static final String FIELD_REVIEW_USER_TEXT = "reviewUser_text";
    public static final String FIELD_LAST_CHANGE_USER_TEXT = "lastChangeUser_text";

    /** Custom property under which storage services record their backend. */
    public static final String PROPERTY_STORAGE_BACKEND = "storage.backend";

    /** Fields indexed as {@link StringField}: they are matched as one exact term. */
    private static final Set<String> EXACT_MATCH_FIELDS = Set.of(FIELD_UPLOAD_USER, FIELD_REVIEW_USER,
            FIELD_LAST_CHANGE_USER, FIELD_CONTENT_HASH, FIELD_GENERATION_TRIGGER_FINGERPRINT,
            FIELD_GOVERNANCE_DOCUMENTATION_ID, FIELD_STATUS, FIELD_OBJECT_REF, FIELD_OBJECT_METADATA_ID, FIELD_STAGE,
            FIELD_SCOPE, FIELD_REGISTRY, FIELD_FINGERPRINT);

    /** Analyzed twins of the user fields, for the wildcard/fuzzy searches tests use. */
    private static final Set<String> ANALYZED_USER_FIELDS = Set.of(FIELD_UPLOAD_USER_TEXT, FIELD_REVIEW_USER_TEXT,
            FIELD_LAST_CHANGE_USER_TEXT);

    private final Path workspacePath;
    private final String indexSubdirectory;
    private Directory directory;
    private IndexWriter indexWriter;
    private SearcherManager searcherManager;
    private StandardAnalyzer analyzer;
    private final ReadWriteLock indexLock = new ReentrantReadWriteLock();

    public LuceneRegistryHelper(Path workspacePath) {
        this(workspacePath, ".lucene-index");
    }

    public LuceneRegistryHelper(Path workspacePath, String indexSubdirectory) {
        this.workspacePath = workspacePath;
        this.indexSubdirectory = indexSubdirectory;
    }

    @Override
    public void initialize() throws IOException {
        // Create Lucene index directory
        Path indexPath = workspacePath.resolve(indexSubdirectory);
        this.directory = FSDirectory.open(indexPath);
        this.analyzer = new StandardAnalyzer();

        // Create singleton IndexWriter with corruption recovery
        IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
        try {
            this.indexWriter = new IndexWriter(directory, writerConfig);
        } catch (IOException | RuntimeException e) {
            // If corrupted, clean up directory and recreate with CREATE mode
            LOGGER.warning("Index corruption detected, recreating index: " + e.getMessage());

            // Close and cleanup corrupted directory
            if (directory != null) {
                try {
                    directory.close();
                } catch (Exception ex) {
                    LOGGER.fine("Error closing corrupted directory: " + ex.getMessage());
                }
            }

            // Delete all files in the index directory to clean corruption
            try {
                if (Files.exists(indexPath)) {
                    Files.walk(indexPath).filter(Files::isRegularFile).forEach(file -> {
                        try {
                            Files.delete(file);
                            LOGGER.fine("Deleted corrupted index file: " + file);
                        } catch (IOException ioe) {
                            LOGGER.fine("Could not delete corrupted file: " + file + " - " + ioe.getMessage());
                        }
                    });
                }
            } catch (IOException ioe) {
                LOGGER.warning("Error cleaning corrupted index directory: " + ioe.getMessage());
            }

            // Recreate directory and IndexWriter
            this.directory = FSDirectory.open(indexPath);
            IndexWriterConfig recoveryConfig = new IndexWriterConfig(analyzer);
            recoveryConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            this.indexWriter = new IndexWriter(directory, recoveryConfig);
        }

        // Create SearcherManager for NRT searching
        this.searcherManager = new SearcherManager(indexWriter, null);

        // Initialize index if empty
        initializeIndex();

        LOGGER.info("Initialized Lucene registry at: " + indexPath);
    }

    /**
     * Initializes the Lucene index if it doesn't exist or is empty.
     */
    private void initializeIndex() throws IOException {
        indexLock.writeLock().lock();
        try {
            // Check if index needs rebuilding
            if (indexWriter.getDocStats().numDocs == 0) {
                LOGGER.info("Index is empty, ready for use");
            }
        } finally {
            indexLock.writeLock().unlock();
        }
    }

    @Override
    public void updateIndex(String objectId, ObjectMetadata metadata) throws IOException {
        indexLock.writeLock().lock();
        try {
            // Remove existing document if it exists
            indexWriter.deleteDocuments(new Term(FIELD_OBJECT_ID, objectId));

            // Create new document
            Document doc = createDocument(objectId, metadata);
            indexWriter.addDocument(doc);

            // Commit changes
            indexWriter.commit();

            // Refresh searcher manager for Near Real Time searching
            searcherManager.maybeRefresh();

            LOGGER.fine("Updated Lucene index for object: " + objectId);

        } finally {
            indexLock.writeLock().unlock();
        }
    }

    @Override
    public void removeFromIndex(String objectId) throws IOException {
        indexLock.writeLock().lock();
        try {
            indexWriter.deleteDocuments(new Term(FIELD_OBJECT_ID, objectId));
            indexWriter.commit();

            // Refresh searcher manager for Near Real Time searching
            searcherManager.maybeRefresh();

            LOGGER.fine("Removed from Lucene index: " + objectId);

        } finally {
            indexLock.writeLock().unlock();
        }
    }

    /**
     * Searches the index with a Lucene query string.
     *
     * <p>
     * A query this index cannot parse is rejected with an
     * {@link IllegalArgumentException} rather than answered: a search that cannot honour
     * its filter must not fall back to matching every document, because the caller
     * cannot tell that apart from a genuine match-all — a scoped search would silently
     * return the objects of every other scope.
     * </p>
     *
     * <p>
     * Callers that filter on values rather than on a query written by hand should use
     * the {@code findBy...} methods below: those build the query from the values, so a
     * name or a scope containing Lucene syntax stays data.
     * </p>
     *
     * @param query      the Lucene query string
     * @param maxResults maximum number of results to return
     * @return the ids of the matching objects
     * @throws IllegalArgumentException if the query cannot be parsed
     */
    @Override
    public List<String> searchObjectIds(String query, int maxResults) throws IOException {
        return search(parseQuery(query), maxResults, query);
    }

    @Override
    public List<String> findByStatus(ObjectStatus status) throws IOException {
        return search(exactly(FIELD_STATUS, status.getLiteral()), Integer.MAX_VALUE);
    }

    @Override
    public List<String> findByObjectName(String objectName) throws IOException {
        return search(objectNameQuery(objectName), Integer.MAX_VALUE);
    }

    @Override
    public List<String> findByStage(String stage) throws IOException {
        return search(exactly(FIELD_STAGE, stage), Integer.MAX_VALUE);
    }

    @Override
    public Optional<String> findByObjectNameAndStage(String objectName, String role) throws IOException {
        List<String> results = search(allOf(objectNameQuery(objectName), exactly(FIELD_STAGE, role)), 1);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Finds the objects of one scope in one stage.
     *
     * @param scope the scope
     * @param stage the stage
     * @return the ids of the matching objects
     * @throws IOException if the search fails
     */
    public List<String> findByScopeAndStage(String scope, String stage) throws IOException {
        return search(allOf(exactly(FIELD_SCOPE, scope), exactly(FIELD_STAGE, stage)), Integer.MAX_VALUE);
    }

    /**
     * Finds the objects of one registry of one scope in one stage.
     *
     * @param scope    the scope
     * @param registry the registry within the scope
     * @param stage    the stage
     * @return the ids of the matching objects
     * @throws IOException if the search fails
     */
    public List<String> findByScopeRegistryAndStage(String scope, String registry, String stage) throws IOException {
        return search(allOf(exactly(FIELD_SCOPE, scope), exactly(FIELD_REGISTRY, registry),
                exactly(FIELD_STAGE, stage)), Integer.MAX_VALUE);
    }

    /**
     * Finds the objects of one scope in one stage whose name matches the given pattern.
     *
     * @param scope       the scope
     * @param stage       the stage
     * @param namePattern the object name, or a pattern containing {@code *} or {@code ?}
     * @return the ids of the matching objects
     * @throws IOException if the search fails
     */
    public List<String> findByScopeStageAndName(String scope, String stage, String namePattern) throws IOException {
        return search(allOf(exactly(FIELD_SCOPE, scope), exactly(FIELD_STAGE, stage), objectNameQuery(namePattern)),
                Integer.MAX_VALUE);
    }

    /**
     * Finds the objects of one registry of one scope in one stage whose name matches the
     * given pattern.
     *
     * @param scope       the scope
     * @param registry    the registry within the scope
     * @param stage       the stage
     * @param namePattern the object name, or a pattern containing {@code *} or {@code ?}
     * @return the ids of the matching objects
     * @throws IOException if the search fails
     */
    public List<String> findByScopeRegistryStageAndName(String scope, String registry, String stage,
            String namePattern) throws IOException {
        return search(allOf(exactly(FIELD_SCOPE, scope), exactly(FIELD_REGISTRY, registry),
                exactly(FIELD_STAGE, stage), objectNameQuery(namePattern)), Integer.MAX_VALUE);
    }

    /**
     * Finds the objects recorded as living in the given storage backend, which storage
     * services track in the {@code storage.backend} custom property.
     *
     * @param backend the storage backend, e.g. {@code file}
     * @return the ids of the matching objects
     * @throws IOException if the search fails
     */
    public List<String> findByStorageBackend(String backend) throws IOException {
        return search(propertyQuery(PROPERTY_STORAGE_BACKEND, backend), Integer.MAX_VALUE);
    }

    /**
     * Finds the objects of one stage recorded as living in the given storage backend.
     *
     * @param backend the storage backend, e.g. {@code file}
     * @param stage   the stage
     * @return the ids of the matching objects
     * @throws IOException if the search fails
     */
    public List<String> findByStorageBackendAndStage(String backend, String stage) throws IOException {
        return search(allOf(propertyQuery(PROPERTY_STORAGE_BACKEND, backend), exactly(FIELD_STAGE, stage)),
                Integer.MAX_VALUE);
    }

    @Override
    public List<String> getAllObjectIds() throws IOException {
        indexLock.readLock().lock();
        try {
            IndexSearcher searcher = searcherManager.acquire();
            try {
                Query query = new MatchAllDocsQuery();
                TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);

                List<String> results = new ArrayList<>();
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    Document doc = searcher.storedFields().document(scoreDoc.doc);
                    results.add(doc.get(FIELD_OBJECT_ID));
                }

                return results;

            } finally {
                searcherManager.release(searcher);
            }
        } finally {
            indexLock.readLock().unlock();
        }
    }

    @Override
    public long getObjectCount() throws IOException {
        indexLock.readLock().lock();
        try {
            IndexSearcher searcher = searcherManager.acquire();
            try {
                Query query = new MatchAllDocsQuery();
                TopDocs topDocs = searcher.search(query, 1);
                return topDocs.totalHits.value;

            } finally {
                searcherManager.release(searcher);
            }
        } finally {
            indexLock.readLock().unlock();
        }
    }

    @Override
    public boolean exists(String objectId) throws IOException {
        indexLock.readLock().lock();
        try {
            IndexSearcher searcher = searcherManager.acquire();
            try {
                Query query = new TermQuery(new Term(FIELD_OBJECT_ID, objectId));
                TopDocs topDocs = searcher.search(query, 1);
                return topDocs.totalHits.value > 0;

            } finally {
                searcherManager.release(searcher);
            }
        } finally {
            indexLock.readLock().unlock();
        }
    }

    @Override
    public void rebuildIndex() throws IOException {
        indexLock.writeLock().lock();
        try {
            // Clear all documents
            indexWriter.deleteAll();
            indexWriter.commit();
            searcherManager.maybeRefresh();

            LOGGER.info("Rebuilt Lucene index (cleared all documents)");

        } finally {
            indexLock.writeLock().unlock();
        }
    }

    @Override
    public Object getRegistryStatistics() throws IOException {
        indexLock.readLock().lock();
        try {
            IndexSearcher searcher = searcherManager.acquire();
            try {
                long totalDocs = getObjectCount();

                java.util.Map<String, Object> stats = new java.util.HashMap<>();
                stats.put("registryType", getRegistryType());
                stats.put("totalDocuments", totalDocs);
                stats.put("indexPath", workspacePath.resolve(indexSubdirectory).toString());
                stats.put("maxDoc", searcher.getIndexReader().maxDoc());
                stats.put("numDocs", searcher.getIndexReader().numDocs());

                return stats;

            } finally {
                searcherManager.release(searcher);
            }
        } finally {
            indexLock.readLock().unlock();
        }
    }

    @Override
    public String getRegistryType() {
        return "lucene";
    }

    /**
     * Search for objects by upload time range.
     * 
     * @param startTime  start of time range (inclusive), or null for no lower bound
     * @param endTime    end of time range (inclusive), or null for no upper bound
     * @param maxResults maximum number of results to return
     * @return list of object IDs matching the time range
     * @throws IOException if search fails
     */
    public List<String> searchByUploadTimeRange(Instant startTime, Instant endTime, int maxResults) throws IOException {
        return searchByTimeRange(FIELD_UPLOAD_TIME, startTime, endTime, maxResults);
    }

    /**
     * Search for objects by compliance check time range.
     * 
     * @param startTime  start of time range (inclusive), or null for no lower bound
     * @param endTime    end of time range (inclusive), or null for no upper bound
     * @param maxResults maximum number of results to return
     * @return list of object IDs matching the time range
     * @throws IOException if search fails
     */
    public List<String> searchByComplianceCheckTimeRange(Instant startTime, Instant endTime, int maxResults)
            throws IOException {
        return searchByTimeRange(FIELD_COMPLIANCE_CHECK_TIME, startTime, endTime, maxResults);
    }

    /**
     * Search for objects by last change time range.
     * 
     * @param startTime  start of time range (inclusive), or null for no lower bound
     * @param endTime    end of time range (inclusive), or null for no upper bound
     * @param maxResults maximum number of results to return
     * @return list of object IDs matching the time range
     * @throws IOException if search fails
     */
    public List<String> searchByLastChangeTimeRange(Instant startTime, Instant endTime, int maxResults)
            throws IOException {
        return searchByTimeRange(FIELD_LAST_CHANGE_TIME, startTime, endTime, maxResults);
    }

    /**
     * Generic time range search helper.
     */
    private List<String> searchByTimeRange(String timeField, Instant startTime, Instant endTime, int maxResults)
            throws IOException {
        long startMillis = startTime != null ? startTime.toEpochMilli() : Long.MIN_VALUE;
        long endMillis = endTime != null ? endTime.toEpochMilli() : Long.MAX_VALUE;

        indexLock.readLock().lock();
        try {
            IndexSearcher searcher = searcherManager.acquire();
            try {
                Query query = LongPoint.newRangeQuery(timeField, startMillis, endMillis);
                TopDocs topDocs = searcher.search(query, maxResults);

                List<String> results = new ArrayList<>();
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    Document doc = searcher.storedFields().document(scoreDoc.doc);
                    results.add(doc.get(FIELD_OBJECT_ID));
                }

                return results;

            } finally {
                searcherManager.release(searcher);
            }
        } finally {
            indexLock.readLock().unlock();
        }
    }

    @Override
    public void close() throws Exception {
        super.close();

        // Close Lucene-specific resources in proper order.
        // Each resource is closed independently to ensure best-effort cleanup,
        // even if the underlying directory was already deleted (e.g. JUnit @TempDir
        // cleanup running before OSGi service deactivation).
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

        LOGGER.info("Closed Lucene registry helper");
    }

    // Private helper methods

    private Document createDocument(String objectId, ObjectMetadata metadata) {
        Document doc = new Document();

        // Object ID (exact match, stored)
        doc.add(new StringField(FIELD_OBJECT_ID, objectId, Field.Store.YES));

        // Upload information - using dual indexing for user fields
        addUserField(doc, FIELD_UPLOAD_USER, FIELD_UPLOAD_USER_TEXT, metadata.getUploadUser());
        addTimeField(doc, FIELD_UPLOAD_TIME, metadata.getUploadTime());
        addFieldIfNotNull(doc, FIELD_SOURCE_CHANNEL, metadata.getSourceChannel(), true);

        // Content information
        addFieldIfNotNull(doc, FIELD_CONTENT_HASH, metadata.getContentHash(), false);
        addFieldIfNotNull(doc, FIELD_OBJECT_TYPE, metadata.getObjectType(), true);

        // Review information - using dual indexing for user fields
        addUserField(doc, FIELD_REVIEW_USER, FIELD_REVIEW_USER_TEXT, metadata.getReviewUser());
        addTimeField(doc, FIELD_REVIEW_TIME, metadata.getReviewTime());
        addFieldIfNotNull(doc, FIELD_REVIEW_REASON, metadata.getReviewReason(), true);

        // Status (exact match)
        if (metadata.getStatus() != null) {
            doc.add(new StringField(FIELD_STATUS, metadata.getStatus().getLiteral(), Field.Store.YES));
        }

        // Object name, stage, scope, registry
        addFieldIfNotNull(doc, FIELD_OBJECT_NAME, metadata.getObjectName(), true);
        addFieldIfNotNull(doc, FIELD_STAGE, metadata.getStage(), false);
        addFieldIfNotNull(doc, FIELD_SCOPE, metadata.getScope(), false);
        addFieldIfNotNull(doc, FIELD_REGISTRY, metadata.getRegistry(), false);

        // Additional metadata fields - following LuceneFileStorageHelper pattern
        addFieldIfNotNull(doc, FIELD_GENERATION_TRIGGER_FINGERPRINT, metadata.getGenerationTriggerFingerprint(), false);
        addFieldIfNotNull(doc, FIELD_COMPLIANCE_STATUS, metadata.getComplianceStatus(), true);
        addFieldIfNotNull(doc, FIELD_GOVERNANCE_DOCUMENTATION_ID, metadata.getGovernanceDocumentationId(), false);
        addUserField(doc, FIELD_LAST_CHANGE_USER, FIELD_LAST_CHANGE_USER_TEXT, metadata.getLastChangeUser());
        addTimeField(doc, FIELD_COMPLIANCE_CHECK_TIME, metadata.getComplianceCheckTime());
        addTimeField(doc, FIELD_LAST_CHANGE_TIME, metadata.getLastChangeTime());
        addFieldIfNotNull(doc, FIELD_VERSION, metadata.getVersion(), true);
        addFieldIfNotNull(doc, FIELD_FINGERPRINT, metadata.getFingerprint(), false);

        // Handle ObjectRef - it's an EObject reference, only store if not null
        if (metadata.getObjectRef() != null) {
            String objectRefString = extractObjectRefUri(metadata.getObjectRef());
            if (objectRefString != null) {
                doc.add(new StringField(FIELD_OBJECT_REF, objectRefString, Field.Store.YES));
            }
        }

        // Store the metadata's own object ID (for cross-references)
        addFieldIfNotNull(doc, FIELD_OBJECT_METADATA_ID, metadata.getObjectId(), false);

        // Custom properties
        if (metadata.getProperties() != null && !metadata.getProperties().isEmpty()) {
            StringBuilder properties = new StringBuilder();
            metadata.getProperties().forEach((e) -> {
                properties.append(e.getKey()).append(":").append(e.getValue()).append(" ");
            });
            doc.add(new TextField(FIELD_PROPERTIES, properties.toString(), Field.Store.NO));
        }

        return doc;
    }

    /**
     * Adds a field to the document if the value is not null.
     */
    private void addFieldIfNotNull(Document doc, String fieldName, String value, boolean analyzed) {
        if (value != null && !value.isEmpty()) {
            if (analyzed) {
                doc.add(new TextField(fieldName, value, Field.Store.YES));
            } else {
                doc.add(new StringField(fieldName, value, Field.Store.YES));
            }
        }
    }

    /**
     * Adds a user field with dual indexing: exact-match for MetadataQueryBuilder
     * and analyzed version for wildcard/fuzzy searches in tests.
     */
    private void addUserField(Document doc, String exactFieldName, String analyzedFieldName, String value) {
        if (value != null && !value.isEmpty()) {
            // Exact-match version for MetadataQueryBuilder
            doc.add(new StringField(exactFieldName, value, Field.Store.YES));
            // Analyzed version for wildcard/fuzzy searches in tests
            doc.add(new TextField(analyzedFieldName, value, Field.Store.NO));
        }
    }

    private void addTimeField(Document doc, String fieldName, Instant instant) {
        if (instant != null) {
            long epochMilli = instant.toEpochMilli();
            doc.add(new LongPoint(fieldName, epochMilli));
            doc.add(new StoredField(fieldName, epochMilli));

            // Also add human-readable version for debugging
            String isoString = instant.toString();
            doc.add(new StringField(fieldName + "_iso", isoString, Field.Store.YES));
        }
    }

    /**
     * Runs a query that was built from values, so it cannot fail to parse.
     */
    private List<String> search(Query luceneQuery, int maxResults) throws IOException {
        return search(luceneQuery, maxResults, luceneQuery.toString());
    }

    private List<String> search(Query luceneQuery, int maxResults, String description) throws IOException {
        indexLock.readLock().lock();
        try {
            IndexSearcher searcher = searcherManager.acquire();
            try {
                TopDocs topDocs = searcher.search(luceneQuery, maxResults);

                List<String> results = new ArrayList<>();
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    Document doc = searcher.storedFields().document(scoreDoc.doc);
                    results.add(doc.get(FIELD_OBJECT_ID));
                }

                LOGGER.fine("Search query '" + description + "' returned " + results.size() + " results");

                return results;

            } finally {
                searcherManager.release(searcher);
            }
        } finally {
            indexLock.readLock().unlock();
        }
    }

    /**
     * Matches one exact term, for the fields indexed as {@link StringField}.
     */
    private static Query exactly(String field, String value) {
        return new TermQuery(new Term(field, value));
    }

    /**
     * Matches an object name, which is indexed analyzed: a pattern containing
     * {@code *} or {@code ?} becomes a wildcard query over the normalized term, anything
     * else a phrase over the analyzed value. Either way the name is only ever a value —
     * its own punctuation is never read as query syntax.
     */
    private Query objectNameQuery(String namePattern) {
        if (namePattern.indexOf('*') >= 0 || namePattern.indexOf('?') >= 0) {
            return new WildcardQuery(new Term(FIELD_OBJECT_NAME, analyzer.normalize(FIELD_OBJECT_NAME, namePattern)));
        }
        return analyzed(FIELD_OBJECT_NAME, namePattern);
    }

    /**
     * Matches a {@code key:value} pair in the analyzed {@link #FIELD_PROPERTIES} field.
     */
    private Query propertyQuery(String key, String value) {
        return analyzed(FIELD_PROPERTIES, key + ":" + value);
    }

    /**
     * Matches the analyzed value as a phrase, so that multi-token values stay one value.
     */
    private Query analyzed(String field, String value) {
        Query query = new QueryBuilder(analyzer).createPhraseQuery(field, value);
        // The analyzer can leave nothing behind (a value that is all punctuation); a term
        // query on the raw value then matches nothing, which is the honest answer.
        return query != null ? query : exactly(field, value);
    }

    private static Query allOf(Query... clauses) {
        if (clauses.length == 1) {
            return clauses[0];
        }
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        for (Query clause : clauses) {
            builder.add(clause, BooleanClause.Occur.MUST);
        }
        return builder.build();
    }

    /**
     * Parses a query string with field-aware handling for StringField vs TextField.
     * Supports both exact-match user fields (for MetadataQueryBuilder) and analyzed
     * user fields (for wildcard/fuzzy searches in tests).
     *
     * @throws IllegalArgumentException if the query cannot be parsed
     */
    private Query parseQuery(String queryString) {
        try {
            // Check if query uses analyzed user fields (test queries)
            boolean hasAnalyzedUserField = false;
            for (String fieldName : ANALYZED_USER_FIELDS) {
                if (queryString.contains(fieldName + ":")) {
                    hasAnalyzedUserField = true;
                    break;
                }
            }

            // If using analyzed user fields, use standard QueryParser
            if (hasAnalyzedUserField) {
                QueryParser parser = new QueryParser(FIELD_UPLOAD_USER_TEXT, analyzer);
                return parser.parse(queryString);
            }

            // Handle exact match fields by building TermQuery manually
            Query exactMatchQuery = buildExactMatchQuery(queryString, EXACT_MATCH_FIELDS);
            if (exactMatchQuery != null) {
                return exactMatchQuery;
            }

            // For all other queries, use standard QueryParser with default field
            QueryParser parser = new QueryParser(FIELD_SOURCE_CHANNEL, analyzer);
            return parser.parse(queryString);
        } catch (ParseException e) {
            // Never fall back to matching every document: the caller asked for a subset,
            // and cannot tell a match-all answer apart from a genuine one.
            throw new IllegalArgumentException("Cannot parse search query '" + queryString + "': " + e.getMessage(), e);
        }
    }

    /**
     * Builds a query for exact match fields using TermQuery instead of QueryParser.
     * This avoids PhraseQuery issues with StringField that don't store position
     * data.
     */
    private Query buildExactMatchQuery(String queryString, Set<String> exactMatchFields) {
        // Check if query contains any exact match fields
        boolean hasExactMatchField = false;
        for (String fieldName : exactMatchFields) {
            if (queryString.contains(fieldName + ":")) {
                hasExactMatchField = true;
                break;
            }
        }

        if (!hasExactMatchField) {
            return null; // No exact match fields, use standard parsing
        }

        // Parse the query manually for exact match fields
        List<Query> clauses = new ArrayList<>();
        boolean isOrQuery = queryString.contains(" OR ");

        // Split by AND or OR
        String[] parts = isOrQuery ? queryString.split(" OR ") : queryString.split(" AND ");

        for (String part : parts) {
            part = part.trim().replaceAll("^\\(+|\\)+$", "");
            if (part.contains(":")) {
                String[] fieldValuePair = part.split(":", 2);
                if (fieldValuePair.length == 2) {
                    String fieldName = fieldValuePair[0].trim();
                    String fieldValue = fieldValuePair[1].trim();

                    // Remove quotes if present
                    if (fieldValue.startsWith("\"") && fieldValue.endsWith("\"")) {
                        fieldValue = fieldValue.substring(1, fieldValue.length() - 1);
                    }

                    if (exactMatchFields.contains(fieldName)) {
                        // Use exact term query for StringField
                        clauses.add(new TermQuery(new Term(fieldName, fieldValue)));
                    } else {
                        // For other fields, we'll need to use QueryParser on just this part
                        try {
                            QueryParser parser = new QueryParser(fieldName, analyzer);
                            clauses.add(parser.parse(fieldName + ":" + fieldValue));
                        } catch (ParseException e) {
                            LOGGER.warning("Failed to parse field query: " + part + " - " + e.getMessage());
                            return null; // Fall back to standard parsing
                        }
                    }
                }
            }
        }

        if (clauses.isEmpty()) {
            return null;
        }

        if (clauses.size() == 1) {
            return clauses.get(0);
        }

        // Build boolean query
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        BooleanClause.Occur occur = isOrQuery ? BooleanClause.Occur.SHOULD : BooleanClause.Occur.MUST;

        for (Query clause : clauses) {
            builder.add(clause, occur);
        }

        return builder.build();
    }

    /**
     * Extracts URI string from EObject reference following EMF proxy handling.
     * Based on the original LuceneFileStorageHelper addObjectRefField method.
     */
    private String extractObjectRefUri(EObject eObject) {
        if (eObject == null) {
            return null;
        }

        // Check if this is a proxy object
        if (eObject.eIsProxy()) {
            URI proxyURI = ((InternalEObject) eObject).eProxyURI();
            if (proxyURI != null) {
                return proxyURI.toString();
            }
        }

        // For regular EMF objects, get the URI from the resource
        Resource resource = eObject.eResource();
        if (resource != null && resource.getURI() != null) {
            URI resourceURI = resource.getURI();
            String fragment = resource.getURIFragment(eObject);
            if (fragment != null && !fragment.isEmpty()) {
                return resourceURI.toString() + "#" + fragment;
            } else {
                return resourceURI.toString();
            }
        }

        // Fallback to toString() if no resource or URI available
        return eObject.toString();
    }
}