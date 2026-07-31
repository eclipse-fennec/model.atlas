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
package org.eclipse.fennec.model.atlas.datagen.impl;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.ByteBuffersDirectory;

/**
 * In-memory Lucene index that maps searchable terms (category + method names)
 * to Datafaker expressions. Supports fuzzy matching so that feature names like
 * "jobTitle" on an EClass "CompanyPerson" can resolve to "#{Job.title}".
 */
public class ExpressionIndex {

	private static final String FIELD_CATEGORY = "category";
	private static final String FIELD_METHOD = "method";
	private static final String FIELD_EXPRESSION = "expression";
	private static final float MIN_SCORE_THRESHOLD = 0.1f;

	private final ByteBuffersDirectory directory;
	private final StandardAnalyzer analyzer;
	private final IndexSearcher searcher;

	/**
	 * Builds the index from a map of generatorKey -> Datafaker expression.
	 * Both the generatorKey parts and the expression parts are indexed for matching.
	 */
	public ExpressionIndex(Map<String, String> keyToExpression) {
		this.directory = new ByteBuffersDirectory();
		this.analyzer = new StandardAnalyzer();

		try {
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			try (IndexWriter writer = new IndexWriter(directory, config)) {
				for (Map.Entry<String, String> entry : keyToExpression.entrySet()) {
					indexEntry(writer, entry.getKey(), entry.getValue());
				}
			}
			DirectoryReader reader = DirectoryReader.open(directory);
			this.searcher = new IndexSearcher(reader);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to build expression index", e);
		}
	}

	private void indexEntry(IndexWriter writer, String generatorKey, String expression) throws IOException {
		// Parse generatorKey: "faker.person.firstName" -> category="person", method="firstName"
		String working = generatorKey;
		if (working.startsWith("faker.")) {
			working = working.substring("faker.".length());
		}
		int dot = working.indexOf('.');
		String category = dot >= 0 ? working.substring(0, dot) : working;
		String method = dot >= 0 ? working.substring(dot + 1) : "";

		// Also parse expression: "#{Name.first_name}" -> exprCategory="name", exprMethod="first name"
		String exprText = expression.replace("#{", "").replace("}", "");
		int exprDot = exprText.indexOf('.');
		String exprCategory = exprDot >= 0 ? exprText.substring(0, exprDot) : exprText;
		String exprMethod = exprDot >= 0 ? exprText.substring(exprDot + 1).replace("_", " ") : "";

		Document doc = new Document();
		// Index category terms (both generatorKey category and expression category)
		doc.add(new TextField(FIELD_CATEGORY, category + " " + exprCategory.toLowerCase(), Field.Store.NO));
		// Index method terms (both camelCase split and snake_case split)
		String methodTerms = splitCamelCase(method) + " " + exprMethod;
		doc.add(new TextField(FIELD_METHOD, methodTerms, Field.Store.NO));
		// Store the expression for retrieval
		doc.add(new StoredField(FIELD_EXPRESSION, expression));

		writer.addDocument(doc);
	}

	/**
	 * Searches for the best matching Datafaker expression given a feature name
	 * and an optional EClass name for context.
	 *
	 * @param featureName the EMF feature name (e.g. "jobTitle", "firstName", "name")
	 * @param eClassName the containing EClass name for disambiguation (e.g. "Person", "CompanyPerson"), may be null
	 * @return the best matching Datafaker expression, or null if no match above threshold
	 */
	public String findExpression(String featureName, String eClassName) {
		try {
			BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();

			// Fuzzy match on method/feature name (MUST — this is the primary match criterion)
			String methodTerms = splitCamelCase(featureName).toLowerCase();
			for (String term : methodTerms.split("\\s+")) {
				if (!term.isBlank()) {
					queryBuilder.add(new FuzzyQuery(new Term(FIELD_METHOD, term), 1), BooleanClause.Occur.MUST);
				}
			}

			// Boost with EClass name context (SHOULD — helps disambiguate)
			if (eClassName != null && !eClassName.isBlank()) {
				String classTerms = splitCamelCase(eClassName).toLowerCase();
				for (String term : classTerms.split("\\s+")) {
					if (!term.isBlank()) {
						queryBuilder.add(new FuzzyQuery(new Term(FIELD_CATEGORY, term), 1), BooleanClause.Occur.SHOULD);
					}
				}
			}

			BooleanQuery query = queryBuilder.build();
			TopDocs topDocs = searcher.search(query, 1);

			if (topDocs.scoreDocs.length > 0) {
				ScoreDoc scoreDoc = topDocs.scoreDocs[0];
				if (scoreDoc.score >= MIN_SCORE_THRESHOLD) {
					Document doc = searcher.storedFields().document(scoreDoc.doc);
					return doc.get(FIELD_EXPRESSION);
				}
			}
			return null;
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Splits a camelCase string into space-separated words.
	 * Handles consecutive uppercase letters (acronyms) correctly.
	 * Examples: "jobTitle" -> "job Title", "HREmployee" -> "HR Employee",
	 *           "firstName" -> "first Name", "XMLParser" -> "XML Parser"
	 */
	static String splitCamelCase(String camelCase) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < camelCase.length(); i++) {
			char c = camelCase.charAt(i);
			if (Character.isUpperCase(c) && i > 0) {
				char prev = camelCase.charAt(i - 1);
				if (Character.isLowerCase(prev)) {
					// lowerUpper boundary: "job|Title"
					sb.append(' ');
				} else if (Character.isUpperCase(prev) && i + 1 < camelCase.length()
						&& Character.isLowerCase(camelCase.charAt(i + 1))) {
					// end of acronym before lowercase: "HR|Employee", "XML|Parser"
					sb.append(' ');
				}
			}
			sb.append(c);
		}
		return sb.toString();
	}
}
