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
 *      Mark Hoffmann - initial API and implementation
 */
package org.gecko.mac.management.lucene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.gecko.mac.management.lucene.registry.MetadataQueryBuilder;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for MetadataQueryBuilder.
 * 
 * <p>Tests the query building functionality including field searches,
 * time ranges, property queries, and special character escaping.</p>
 */
class MetadataQueryBuilderTest {

    @Test
    void testEmptyQuery() {
        String query = MetadataQueryBuilder.create().build();
        assertEquals("*:*", query, "Empty query should return match-all");
    }

    @Test
    void testSingleUserQuery() {
        String query = MetadataQueryBuilder.create()
            .uploadUser("john")
            .build();
        assertEquals("uploadUser:john", query);
    }

    @Test
    void testMultipleConditionsWithAnd() {
        String query = MetadataQueryBuilder.create()
            .uploadUser("alice")
            .sourceChannel("AI_GENERATOR")
            .objectType("EPackage")
            .build();
        assertEquals("uploadUser:alice AND sourceChannel:AI_GENERATOR AND objectType:EPackage", query);
    }

    @Test
    void testMultipleConditionsWithOr() {
        String query = MetadataQueryBuilder.create()
            .or()
            .uploadUser("alice")
            .uploadUser("bob")
            .build();
        assertEquals("uploadUser:alice OR uploadUser:bob", query);
    }

    @Test
    void testAnyUserQuery() {
        String query = MetadataQueryBuilder.create()
            .anyUser("john")
            .build();
        assertEquals("(uploadUser:john OR reviewUser:john OR lastChangeUser:john)", query);
    }

    @Test
    void testContentHashQuery() {
        String query = MetadataQueryBuilder.create()
            .contentHash("sha256-abc123")
            .build();
        assertEquals("contentHash:sha256-abc123", query);
    }

    @Test
    void testTimeRangeQuery() {
        Instant start = Instant.parse("2024-01-01T00:00:00Z");
        Instant end = Instant.parse("2024-12-31T23:59:59Z");
        
        String query = MetadataQueryBuilder.create()
            .uploadTimeRange(start, end)
            .build();
        
        // Should use epoch millis for efficient range queries
        long startMillis = start.toEpochMilli();
        long endMillis = end.toEpochMilli();
        assertEquals("uploadTime:[" + startMillis + " TO " + endMillis + "]", query);
    }

    @Test
    void testTimeAfterQuery() {
        Instant after = Instant.parse("2024-06-01T12:00:00Z");
        
        String query = MetadataQueryBuilder.create()
            .uploadTimeAfter(after)
            .build();
        
        long afterMillis = after.toEpochMilli();
        assertEquals("uploadTime:[" + afterMillis + " TO *]", query);
    }

    @Test
    void testTimeBeforeQuery() {
        Instant before = Instant.parse("2024-06-01T12:00:00Z");
        
        String query = MetadataQueryBuilder.create()
            .uploadTimeBefore(before)
            .build();
        
        long beforeMillis = before.toEpochMilli();
        assertEquals("uploadTime:[* TO " + beforeMillis + "]", query);
    }

    @Test
    void testReviewTimeRange() {
        Instant start = Instant.parse("2024-01-01T00:00:00Z");
        Instant end = Instant.parse("2024-01-31T23:59:59Z");
        
        String query = MetadataQueryBuilder.create()
            .reviewTimeRange(start, end)
            .build();
        
        long startMillis = start.toEpochMilli();
        long endMillis = end.toEpochMilli();
        assertEquals("reviewTime:[" + startMillis + " TO " + endMillis + "]", query);
    }

    @Test
    void testPropertyQuery() {
        String query = MetadataQueryBuilder.create()
            .property("version", "1.0")
            .build();
        assertEquals("properties:version\\:1.0", query);
    }

    @Test
    void testHasPropertyQuery() {
        String query = MetadataQueryBuilder.create()
            .hasProperty("namespace")
            .build();
        assertEquals("properties:namespace\\:\\*", query);
    }

    @Test
    void testAnyFieldQuery() {
        String query = MetadataQueryBuilder.create()
            .anyField("sensor")
            .build();
        String expected = "(uploadUser:sensor OR sourceChannel:sensor OR objectType:sensor OR objectName:sensor OR reviewUser:sensor OR reviewReason:sensor OR complianceStatus:sensor OR lastChangeUser:sensor OR properties:sensor)";
        assertEquals(expected, query);
    }

    @Test
    void testCustomQuery() {
        String query = MetadataQueryBuilder.create()
            .custom("(uploadUser:alice OR uploadUser:bob) AND objectType:EPackage")
            .build();
        assertEquals("(uploadUser:alice OR uploadUser:bob) AND objectType:EPackage", query);
    }

    @Test
    void testSpecialCharacterEscaping() {
        String query = MetadataQueryBuilder.create()
            .uploadUser("user@domain.com")
            .build();
        assertEquals("uploadUser:user@domain.com", query);
    }

    @Test
    void testSpaceInValueQuoting() {
        String query = MetadataQueryBuilder.create()
            .uploadUser("John Doe")
            .build();
        assertEquals("uploadUser:John Doe", query);
    }

    @Test
    void testSpecialCharactersEscaping() {
        String query = MetadataQueryBuilder.create()
            .uploadUser("user+test")
            .sourceChannel("AI_GENERATOR-v2")
            .property("key:with:colons", "value(with)parens")
            .build();
        
        String expected = "uploadUser:user+test AND sourceChannel:AI_GENERATOR\\-v2 AND properties:key\\:with\\:colons\\:value\\(with\\)parens";
        assertEquals(expected, query);
    }

    @Test
    void testNullAndEmptyValues() {
        String query = MetadataQueryBuilder.create()
            .uploadUser(null)
            .uploadUser("")
            .sourceChannel("AI_GENERATOR")
            .objectType(null)
            .build();
        assertEquals("sourceChannel:AI_GENERATOR", query);
    }

    @Test
    void testComplexQuery() {
        Instant start = Instant.parse("2024-01-01T00:00:00Z");
        Instant end = Instant.parse("2024-12-31T23:59:59Z");
        
        String query = MetadataQueryBuilder.create()
            .sourceChannel("AI_GENERATOR")
            .objectType("EPackage")
            .uploadTimeRange(start, end)
            .property("version", "1.0")
            .anyUser("system")
            .build();
        
        long startMillis = start.toEpochMilli();
        long endMillis = end.toEpochMilli();
        String expected = "sourceChannel:AI_GENERATOR AND objectType:EPackage AND " +
                         "uploadTime:[" + startMillis + " TO " + endMillis + "] AND " +
                         "properties:version\\:1.0 AND (uploadUser:system OR reviewUser:system OR lastChangeUser:system)";
        assertEquals(expected, query);
    }

    @Test
    void testComplexOrQuery() {
        String query = MetadataQueryBuilder.create()
            .or()
            .objectType("EPackage")
            .objectType("Route")
            .objectType("SensorModel")
            .build();
        assertEquals("objectType:EPackage OR objectType:Route OR objectType:SensorModel", query);
    }

    @Test
    void testMixedAndOrQuery() {
        String query = MetadataQueryBuilder.create()
            .sourceChannel("AI_GENERATOR")
            .custom("(objectType:EPackage OR objectType:Route)")
            .anyUser("system")
            .build();
        
        String expected = "sourceChannel:AI_GENERATOR AND (objectType:EPackage OR objectType:Route) AND (uploadUser:system OR reviewUser:system OR lastChangeUser:system)";
        assertEquals(expected, query);
    }

    @Test
    void testReviewReasonQuery() {
        String query = MetadataQueryBuilder.create()
            .reviewReason("Quality check passed")
            .build();
        assertEquals("reviewReason:\"Quality check passed\"", query);
    }

    @Test
    void testMethodChaining() {
        // Test that all methods return the builder for chaining
        MetadataQueryBuilder builder = MetadataQueryBuilder.create();
        
        assertSame(builder, builder.or());
        assertSame(builder, builder.uploadUser("test"));
        assertSame(builder, builder.reviewUser("test"));
        assertSame(builder, builder.reviewReason("test"));
        assertSame(builder, builder.anyUser("test"));
        assertSame(builder, builder.sourceChannel("test"));
        assertSame(builder, builder.objectType("test"));
        assertSame(builder, builder.contentHash("test"));
        assertSame(builder, builder.property("key", "value"));
        assertSame(builder, builder.hasProperty("key"));
        assertSame(builder, builder.anyField("test"));
        assertSame(builder, builder.custom("test"));
        
        Instant now = Instant.now();
        assertSame(builder, builder.uploadTimeRange(now, now));
        assertSame(builder, builder.uploadTimeAfter(now));
        assertSame(builder, builder.uploadTimeBefore(now));
        assertSame(builder, builder.reviewTimeRange(now, now));
        assertSame(builder, builder.complianceCheckTimeRange(now, now));
        assertSame(builder, builder.lastChangeTimeRange(now, now));
    }

    @Test
    void testLuceneReservedCharactersEscaping() {
        // Test that user fields are exact match and don't need escaping
        String input = "test\\\"()[]{}~*?:^-+";
        String query = MetadataQueryBuilder.create()
            .uploadUser(input)
            .build();
        
        String expected = "uploadUser:test\\\"()[]{}~*?:^-+";
        assertEquals(expected, query);
    }

    @Test
    void testWildcardSearch() {
        String query = MetadataQueryBuilder.create()
            .custom("uploadUser:john*")
            .build();
        assertEquals("uploadUser:john*", query);
    }

    @Test
    void testFuzzySearch() {
        String query = MetadataQueryBuilder.create()
            .custom("uploadUser:john~")
            .build();
        assertEquals("uploadUser:john~", query);
    }

    @Test
    void testBooleanOperatorPrecedence() {
        String query = MetadataQueryBuilder.create()
            .custom("(uploadUser:alice OR uploadUser:bob) AND (objectType:EPackage OR objectType:Route)")
            .build();
        assertEquals("(uploadUser:alice OR uploadUser:bob) AND (objectType:EPackage OR objectType:Route)", query);
    }

    @Test
    void testGenerationTriggerFingerprintQuery() {
        String query = MetadataQueryBuilder.create()
            .generationTriggerFingerprint("fp-abc123def456")
            .build();
        assertEquals("generationTriggerFingerprint:\"fp-abc123def456\"", query);
    }

    @Test
    void testComplianceStatusQuery() {
        String query = MetadataQueryBuilder.create()
            .complianceStatus("COMPLIANT")
            .build();
        assertEquals("complianceStatus:COMPLIANT", query);
    }

    @Test
    void testGovernanceDocumentationIdQuery() {
        String query = MetadataQueryBuilder.create()
            .governanceDocumentationId("gov-doc-789")
            .build();
        assertEquals("governanceDocumentationId:\"gov-doc-789\"", query);
    }

    @Test
    void testLastChangeUserQuery() {
        String query = MetadataQueryBuilder.create()
            .lastChangeUser("admin")
            .build();
        assertEquals("lastChangeUser:admin", query);
    }

    @Test
    void testComplianceCheckTimeRangeQuery() {
        Instant start = Instant.parse("2024-01-01T00:00:00Z");
        Instant end = Instant.parse("2024-01-31T23:59:59Z");
        
        String query = MetadataQueryBuilder.create()
            .complianceCheckTimeRange(start, end)
            .build();
        
        long startMillis = start.toEpochMilli();
        long endMillis = end.toEpochMilli();
        assertEquals("complianceCheckTime:[" + startMillis + " TO " + endMillis + "]", query);
    }

    @Test
    void testLastChangeTimeRangeQuery() {
        Instant start = Instant.parse("2024-06-01T00:00:00Z");
        Instant end = Instant.parse("2024-06-30T23:59:59Z");
        
        String query = MetadataQueryBuilder.create()
            .lastChangeTimeRange(start, end)
            .build();
        
        long startMillis = start.toEpochMilli();
        long endMillis = end.toEpochMilli();
        assertEquals("lastChangeTime:[" + startMillis + " TO " + endMillis + "]", query);
    }

    @Test
    void testCombinedNewFieldsQuery() {
        Instant start = Instant.parse("2024-01-01T00:00:00Z");
        Instant end = Instant.parse("2024-12-31T23:59:59Z");
        
        String query = MetadataQueryBuilder.create()
            .complianceStatus("COMPLIANT")
            .lastChangeUser("admin")
            .complianceCheckTimeRange(start, end)
            .generationTriggerFingerprint("fp-sensors-123")
            .build();
        
        long startMillis = start.toEpochMilli();
        long endMillis = end.toEpochMilli();
        String expected = "complianceStatus:COMPLIANT AND lastChangeUser:admin AND " +
                         "complianceCheckTime:[" + startMillis + " TO " + endMillis + "] AND " +
                         "generationTriggerFingerprint:\"fp-sensors-123\"";
        assertEquals(expected, query);
    }

    @Test
    void testNewFieldsMethodChaining() {
        // Test that new methods return the builder for chaining
        MetadataQueryBuilder builder = MetadataQueryBuilder.create();
        Instant now = Instant.now();
        
        assertSame(builder, builder.reviewReason("test"));
        assertSame(builder, builder.generationTriggerFingerprint("test"));
        assertSame(builder, builder.complianceStatus("test"));
        assertSame(builder, builder.governanceDocumentationId("test"));
        assertSame(builder, builder.lastChangeUser("test"));
        assertSame(builder, builder.complianceCheckTimeRange(now, now));
        assertSame(builder, builder.lastChangeTimeRange(now, now));
    }

    @Test
    void testNewFieldsWithNullValues() {
        String query = MetadataQueryBuilder.create()
            .generationTriggerFingerprint(null)
            .complianceStatus("")
            .governanceDocumentationId("gov-123")
            .lastChangeUser(null)
            .build();
        assertEquals("governanceDocumentationId:\"gov-123\"", query);
    }

    @Test
    void testNewFieldsWithSpecialCharacters() {
        String query = MetadataQueryBuilder.create()
            .complianceStatus("NON_COMPLIANT")
            .lastChangeUser("user@domain.com")
            .generationTriggerFingerprint("fp-abc:def+ghi")
            .build();
        
        String expected = "complianceStatus:NON_COMPLIANT AND lastChangeUser:user@domain.com AND generationTriggerFingerprint:\"fp-abc:def+ghi\"";
        assertEquals(expected, query);
    }

    @Test
    void testGovernanceWorkflowQuery() {
        // Test a realistic governance workflow query
        Instant oneWeekAgo = Instant.parse("2024-01-01T00:00:00Z");
        Instant now = Instant.parse("2024-01-08T00:00:00Z");
        
        String query = MetadataQueryBuilder.create()
            .sourceChannel("AI_GENERATOR")
            .complianceStatus("PENDING")
            .complianceCheckTimeRange(oneWeekAgo, now)
            .lastChangeUser("system")
            .build();
        
        long startMillis = oneWeekAgo.toEpochMilli();
        long endMillis = now.toEpochMilli();
        String expected = "sourceChannel:AI_GENERATOR AND complianceStatus:PENDING AND " +
                         "complianceCheckTime:[" + startMillis + " TO " + endMillis + "] AND " +
                         "lastChangeUser:system";
        assertEquals(expected, query);
    }

    @Test
    void testAIGenerationWorkflowQuery() {
        // Test a realistic AI generation workflow query
        String query = MetadataQueryBuilder.create()
            .or()
            .generationTriggerFingerprint("fp-sensors-123")
            .generationTriggerFingerprint("fp-routes-456")
            .build();
        
        String expected = "generationTriggerFingerprint:\"fp-sensors-123\" OR generationTriggerFingerprint:\"fp-routes-456\"";
        assertEquals(expected, query);
    }

    @Test
    void testUploadUserWildcard() {
        String query = MetadataQueryBuilder.create()
            .uploadUserWildcard("admin*")
            .build();
        
        String expected = "uploadUser_text:admin*";
        assertEquals(expected, query);
    }

    @Test
    void testReviewUserWildcard() {
        String query = MetadataQueryBuilder.create()
            .reviewUserWildcard("*reviewer")
            .build();
        
        String expected = "reviewUser_text:*reviewer";
        assertEquals(expected, query);
    }

    @Test
    void testLastChangeUserWildcard() {
        String query = MetadataQueryBuilder.create()
            .lastChangeUserWildcard("test*user")
            .build();
        
        String expected = "lastChangeUser_text:test*user";
        assertEquals(expected, query);
    }

    @Test
    void testUploadUserFuzzy() {
        String query = MetadataQueryBuilder.create()
            .uploadUserFuzzy("jhon", 1)
            .build();
        
        String expected = "uploadUser_text:jhon~1";
        assertEquals(expected, query);
    }

    @Test
    void testReviewUserFuzzy() {
        String query = MetadataQueryBuilder.create()
            .reviewUserFuzzy("admnistrator", 2)
            .build();
        
        String expected = "reviewUser_text:admnistrator~2";
        assertEquals(expected, query);
    }

    @Test
    void testLastChangeUserFuzzy() {
        String query = MetadataQueryBuilder.create()
            .lastChangeUserFuzzy("systm", 1)
            .build();
        
        String expected = "lastChangeUser_text:systm~1";
        assertEquals(expected, query);
    }

    @Test
    void testWildcardAndFuzzySearchMixed() {
        String query = MetadataQueryBuilder.create()
            .uploadUserWildcard("admin*")
            .reviewUserFuzzy("operatr", 1)
            .build();
        
        String expected = "uploadUser_text:admin* AND reviewUser_text:operatr~1";
        assertEquals(expected, query);
    }

    @Test
    void testWildcardWithNullAndEmpty() {
        String query = MetadataQueryBuilder.create()
            .uploadUserWildcard(null)
            .reviewUserWildcard("")
            .lastChangeUserWildcard("valid*")
            .build();
        
        String expected = "lastChangeUser_text:valid*";
        assertEquals(expected, query);
    }

    @Test
    void testFuzzyWithInvalidEditDistance() {
        String query = MetadataQueryBuilder.create()
            .uploadUserFuzzy("user", 0) // Invalid edit distance
            .reviewUserFuzzy("admin", 3) // Invalid edit distance 
            .lastChangeUserFuzzy("test", 1) // Valid
            .build();
        
        String expected = "lastChangeUser_text:test~1";
        assertEquals(expected, query);
    }

    @Test
    void testObjectNameQuery() {
        String query = MetadataQueryBuilder.create()
            .objectName("Sensor Model")
            .build();
        assertEquals("objectName:\"Sensor Model\"", query);
    }

    @Test
    void testObjectNameInAnyFieldQuery() {
        // Test that objectName is included in anyField searches
        String query = MetadataQueryBuilder.create()
            .anyField("Model")
            .build();
        assertTrue(query.contains("objectName:Model"));
    }
}