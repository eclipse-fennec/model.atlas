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
package org.eclipse.fennec.model.atlas.dcat.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.fennec.dcat.atlas.client.api.ConflictException;
import org.eclipse.fennec.dcat.atlas.client.api.DcatShaclException;
import org.eclipse.fennec.dcat.atlas.client.api.RetryableException;
import org.eclipse.fennec.dcat.atlas.client.api.TransportException;
import org.junit.jupiter.api.Test;

/**
 * Which failures come back, which are recorded and left alone, and what the health check gets told.
 */
public class RetryQueueTest {

    /** Fast enough for a unit test; the shipped defaults are 5s doubling to 300s. */
    private static RetryQueue queue(int maxAttempts) {
        return new RetryQueue("test", 1_000, 1_000, maxAttempts);
    }

    // ---- classification --------------------------------------------------

    @Test
    public void aTransientFailureIsWorthAnotherAttempt() {
        // A 503 on a write is the case not to give up on: the portal's own commit may well have
        // succeeded, and nothing is wrong with what we sent.
        assertThat(RetryQueue.retryable(new RetryableException("503"))).isTrue();
        assertThat(RetryQueue.retryable(new TransportException("connect timeout"))).isTrue();
        assertThat(RetryQueue.retryable(new PortalNotReadyException("not ready"))).isTrue();
    }

    @Test
    public void aRefusedPayloadIsNot() {
        // The portal will refuse the identical payload every time, so retrying turns one useful
        // report into a stream of noise.
        assertThat(RetryQueue.retryable(new DcatShaclException("shape violation", "", "text/turtle"))).isFalse();
        assertThat(RetryQueue.retryable(new ConflictException("still referenced"))).isFalse();
        assertThat(RetryQueue.retryable(new IllegalStateException("license.uri is required"))).isFalse();
    }

    @Test
    public void classificationLooksThroughTheCauseChain() {
        // A failure reaches the publisher through a Promise, which may have wrapped it. A wrapped
        // 503 is still a 503.
        assertThat(RetryQueue.retryable(new RuntimeException("wrapped", new RetryableException("503")))).isTrue();
        assertThat(RetryQueue.retryable(new RuntimeException("wrapped", new DcatShaclException("nope", "", "text/turtle")))).isFalse();
    }

    @Test
    public void aSelfReferencingCauseDoesNotLoop() {
        RuntimeException self = new RuntimeException("odd") {
            private static final long serialVersionUID = 1L;

            @Override
            public synchronized Throwable getCause() {
                return this;
            }
        };
        assertThat(RetryQueue.retryable(self)).isFalse();
    }

    // ---- retrying --------------------------------------------------------

    @Test
    public void aTransientFailureIsRetried() throws Exception {
        try (RetryQueue queue = queue(5)) {
            CountDownLatch retried = new CountDownLatch(1);
            queue.failed("k", "publishing X", new TransportException("down"), retried::countDown);

            assertThat(queue.pending()).isEqualTo(1);
            assertThat(retried.await(10, TimeUnit.SECONDS)).as("the same operation should be run again").isTrue();
        }
    }

    @Test
    public void aPermanentFailureIsRecordedAndNotRetried() throws Exception {
        try (RetryQueue queue = queue(5)) {
            AtomicInteger runs = new AtomicInteger();
            queue.failed("k", "publishing X", new DcatShaclException("shape violation", "", "text/turtle"), runs::incrementAndGet);

            Thread.sleep(2_500);
            assertThat(runs.get()).isZero();
            assertThat(queue.pending()).isZero();
            // Recorded rather than forgotten: that is the difference between a stale catalogue
            // somebody can see and one nobody knows about.
            assertThat(queue.report().abandoned()).isEqualTo(1);
            assertThat(queue.report().lines()).singleElement(org.assertj.core.api.InstanceOfAssertFactories.STRING)
                    .contains("publishing X", "shape violation");
        }
    }

    @Test
    public void attemptsRunOutAndTheGiveUpIsReported() throws Exception {
        try (RetryQueue queue = queue(2)) {
            queue.failed("k", "publishing X", new TransportException("down"), () -> {
            });
            assertThat(queue.report().retrying()).isEqualTo(1);

            // The second failure is the last attempt allowed.
            queue.failed("k", "publishing X", new TransportException("down"), () -> {
            });

            assertThat(queue.pending()).isZero();
            assertThat(queue.report().abandoned()).isEqualTo(1);
            assertThat(queue.report().lines()).singleElement(org.assertj.core.api.InstanceOfAssertFactories.STRING)
                    .contains("gave up");
        }
    }

    @Test
    public void successClearsTheRecord() throws Exception {
        try (RetryQueue queue = queue(5)) {
            queue.failed("k", "publishing X", new TransportException("down"), () -> {
            });
            assertThat(queue.report().lines()).hasSize(1);

            queue.succeeded("k");

            assertThat(queue.report().lines()).isEmpty();
            assertThat(queue.pending()).isZero();
        }
    }

    @Test
    public void aSecondFailureOfTheSameTargetReplacesItsPendingRetry() throws Exception {
        try (RetryQueue queue = queue(9)) {
            AtomicInteger runs = new AtomicInteger();
            queue.failed("k", "publishing X", new TransportException("down"), runs::incrementAndGet);
            queue.failed("k", "publishing X", new TransportException("down"), runs::incrementAndGet);

            // One pending retry per target, however many failures it reports.
            assertThat(queue.pending()).isEqualTo(1);
            Thread.sleep(3_000);
            assertThat(runs.get()).as("the superseded retry must not also run").isEqualTo(1);
        }
    }

    @Test
    public void closingCancelsWhatIsPending() throws Exception {
        AtomicInteger runs = new AtomicInteger();
        RetryQueue queue = queue(5);
        queue.failed("k", "publishing X", new TransportException("down"), runs::incrementAndGet);

        queue.close();

        Thread.sleep(2_500);
        assertThat(runs.get()).as("a retry must not outlive the configuration that scheduled it").isZero();
    }

    @Test
    public void permanentRecordsWithoutAThrowable() {
        try (RetryQueue queue = queue(5)) {
            queue.permanent("k", "publishing X", "license.uri is required");

            assertThat(queue.report().abandoned()).isEqualTo(1);
            assertThat(queue.report().lines()).singleElement(org.assertj.core.api.InstanceOfAssertFactories.STRING)
                    .contains("license.uri is required");
        }
    }
}
