/*
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
package org.eclipse.fennec.model.atlas.governance.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.eclipse.fennec.model.atlas.governance.ApprovalStatus;
import org.eclipse.fennec.model.atlas.governance.workflow.impl.GovernanceDocumentationStateMachine;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the governance documentation state machine.
 * 
 * <p>These tests validate the state transition rules and business logic
 * independently of the workflow implementation.</p>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
public class GovernanceDocumentationStateMachineTest {

    @Test
    public void testInitialStateTransitions() {
        // Initial state (null) can only transition to DRAFT
        assertTrue(GovernanceDocumentationStateMachine.isValidTransition(null, ApprovalStatus.DRAFT),
                "Should allow NULL → DRAFT transition");
        
        assertFalse(GovernanceDocumentationStateMachine.isValidTransition(null, ApprovalStatus.IN_REVIEW),
                "Should not allow NULL → IN_REVIEW transition");
        
        assertFalse(GovernanceDocumentationStateMachine.isValidTransition(null, ApprovalStatus.APPROVED),
                "Should not allow NULL → APPROVED transition");
        
        assertFalse(GovernanceDocumentationStateMachine.isValidTransition(null, ApprovalStatus.REJECTED),
                "Should not allow NULL → REJECTED transition");
    }

    @Test
    public void testDraftStateTransitions() {
        // DRAFT can transition to IN_REVIEW
        assertTrue(GovernanceDocumentationStateMachine.isValidTransition(ApprovalStatus.DRAFT, ApprovalStatus.IN_REVIEW),
                "Should allow DRAFT → IN_REVIEW transition");
        
        // DRAFT cannot directly transition to APPROVED or REJECTED
        assertFalse(GovernanceDocumentationStateMachine.isValidTransition(ApprovalStatus.DRAFT, ApprovalStatus.APPROVED),
                "Should not allow DRAFT → APPROVED transition (must go through IN_REVIEW)");
        
        assertFalse(GovernanceDocumentationStateMachine.isValidTransition(ApprovalStatus.DRAFT, ApprovalStatus.REJECTED),
                "Should not allow DRAFT → REJECTED transition (must go through IN_REVIEW)");
        
        // DRAFT can transition to DRAFT (universal rule)
        assertTrue(GovernanceDocumentationStateMachine.isValidTransition(ApprovalStatus.DRAFT, ApprovalStatus.DRAFT),
                "Should allow DRAFT → DRAFT transition (reset)");
    }

    @Test
    public void testInReviewStateTransitions() {
        // IN_REVIEW can transition to APPROVED or REJECTED
        assertTrue(GovernanceDocumentationStateMachine.isValidTransition(ApprovalStatus.IN_REVIEW, ApprovalStatus.APPROVED),
                "Should allow IN_REVIEW → APPROVED transition");
        
        assertTrue(GovernanceDocumentationStateMachine.isValidTransition(ApprovalStatus.IN_REVIEW, ApprovalStatus.REJECTED),
                "Should allow IN_REVIEW → REJECTED transition");
        
        // IN_REVIEW can transition to DRAFT (universal rule)
        assertTrue(GovernanceDocumentationStateMachine.isValidTransition(ApprovalStatus.IN_REVIEW, ApprovalStatus.DRAFT),
                "Should allow IN_REVIEW → DRAFT transition (reset/rework)");
        
        // IN_REVIEW cannot transition to itself
        assertFalse(GovernanceDocumentationStateMachine.isValidTransition(ApprovalStatus.IN_REVIEW, ApprovalStatus.IN_REVIEW),
                "Should not allow IN_REVIEW → IN_REVIEW transition");
    }

    @Test
    public void testApprovedStateTransitions() {
        // APPROVED is a terminal state - no standard transitions
        assertFalse(GovernanceDocumentationStateMachine.isValidTransition(ApprovalStatus.APPROVED, ApprovalStatus.IN_REVIEW),
                "Should not allow APPROVED → IN_REVIEW transition");
        
        assertFalse(GovernanceDocumentationStateMachine.isValidTransition(ApprovalStatus.APPROVED, ApprovalStatus.REJECTED),
                "Should not allow APPROVED → REJECTED transition");
        
        assertFalse(GovernanceDocumentationStateMachine.isValidTransition(ApprovalStatus.APPROVED, ApprovalStatus.APPROVED),
                "Should not allow APPROVED → APPROVED transition");
        
        // APPROVED can transition to DRAFT (universal rule for rework)
        assertTrue(GovernanceDocumentationStateMachine.isValidTransition(ApprovalStatus.APPROVED, ApprovalStatus.DRAFT),
                "Should allow APPROVED → DRAFT transition (reset for rework)");
    }

    @Test
    public void testRejectedStateTransitions() {
        // REJECTED can transition to DRAFT (rework) or IN_REVIEW (resubmit)
        assertTrue(GovernanceDocumentationStateMachine.isValidTransition(ApprovalStatus.REJECTED, ApprovalStatus.DRAFT),
                "Should allow REJECTED → DRAFT transition (rework)");
        
        assertTrue(GovernanceDocumentationStateMachine.isValidTransition(ApprovalStatus.REJECTED, ApprovalStatus.IN_REVIEW),
                "Should allow REJECTED → IN_REVIEW transition (resubmit)");
        
        // REJECTED cannot directly transition to APPROVED
        assertFalse(GovernanceDocumentationStateMachine.isValidTransition(ApprovalStatus.REJECTED, ApprovalStatus.APPROVED),
                "Should not allow REJECTED → APPROVED transition (must go through IN_REVIEW)");
        
        // REJECTED cannot transition to itself
        assertFalse(GovernanceDocumentationStateMachine.isValidTransition(ApprovalStatus.REJECTED, ApprovalStatus.REJECTED),
                "Should not allow REJECTED → REJECTED transition");
    }

    @Test
    public void testUniversalDraftTransition() {
        // Any state can transition to DRAFT (universal reset/rework rule)
        for (ApprovalStatus fromState : ApprovalStatus.values()) {
            assertTrue(GovernanceDocumentationStateMachine.isValidTransition(fromState, ApprovalStatus.DRAFT),
                    "Should allow " + fromState + " → DRAFT transition (universal rule)");
        }
    }

    @Test
    public void testValidateTransitionSuccess() {
        // Valid transitions should not throw exceptions
        GovernanceDocumentationStateMachine.validateTransition(null, ApprovalStatus.DRAFT, "test-object");
        GovernanceDocumentationStateMachine.validateTransition(ApprovalStatus.DRAFT, ApprovalStatus.IN_REVIEW, "test-object");
        GovernanceDocumentationStateMachine.validateTransition(ApprovalStatus.IN_REVIEW, ApprovalStatus.APPROVED, "test-object");
        GovernanceDocumentationStateMachine.validateTransition(ApprovalStatus.IN_REVIEW, ApprovalStatus.REJECTED, "test-object");
        GovernanceDocumentationStateMachine.validateTransition(ApprovalStatus.REJECTED, ApprovalStatus.DRAFT, "test-object");
        GovernanceDocumentationStateMachine.validateTransition(ApprovalStatus.REJECTED, ApprovalStatus.IN_REVIEW, "test-object");
        GovernanceDocumentationStateMachine.validateTransition(ApprovalStatus.APPROVED, ApprovalStatus.DRAFT, "test-object");
    }

    @Test
    public void testValidateTransitionFailure() {
        // Invalid transitions should throw IllegalStateException
        IllegalStateException exception1 = assertThrows(IllegalStateException.class, () -> {
            GovernanceDocumentationStateMachine.validateTransition(ApprovalStatus.DRAFT, ApprovalStatus.APPROVED, "test-object");
        });
        assertTrue(exception1.getMessage().contains("Invalid governance documentation state transition"),
                "Should provide descriptive error message");
        assertTrue(exception1.getMessage().contains("test-object"),
                "Should include context in error message");

        IllegalStateException exception2 = assertThrows(IllegalStateException.class, () -> {
            GovernanceDocumentationStateMachine.validateTransition(ApprovalStatus.APPROVED, ApprovalStatus.IN_REVIEW, "another-object");
        });
        assertTrue(exception2.getMessage().contains("APPROVED to IN_REVIEW"),
                "Should specify the invalid transition");
    }

    @Test
    public void testNullTargetState() {
        // Null target state should throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            GovernanceDocumentationStateMachine.isValidTransition(ApprovalStatus.DRAFT, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            GovernanceDocumentationStateMachine.validateTransition(ApprovalStatus.DRAFT, null, "test-object");
        });
    }

    @Test
    public void testGetValidTargetStates() {
        // Test initial state
        Set<ApprovalStatus> initialTargets = GovernanceDocumentationStateMachine.getValidTargetStates(null);
        assertEquals(1, initialTargets.size());
        assertTrue(initialTargets.contains(ApprovalStatus.DRAFT));

        // Test DRAFT state
        Set<ApprovalStatus> draftTargets = GovernanceDocumentationStateMachine.getValidTargetStates(ApprovalStatus.DRAFT);
        assertEquals(2, draftTargets.size());
        assertTrue(draftTargets.contains(ApprovalStatus.DRAFT)); // Universal rule
        assertTrue(draftTargets.contains(ApprovalStatus.IN_REVIEW));

        // Test IN_REVIEW state
        Set<ApprovalStatus> reviewTargets = GovernanceDocumentationStateMachine.getValidTargetStates(ApprovalStatus.IN_REVIEW);
        assertEquals(3, reviewTargets.size());
        assertTrue(reviewTargets.contains(ApprovalStatus.DRAFT)); // Universal rule
        assertTrue(reviewTargets.contains(ApprovalStatus.APPROVED));
        assertTrue(reviewTargets.contains(ApprovalStatus.REJECTED));

        // Test APPROVED state (terminal)
        Set<ApprovalStatus> approvedTargets = GovernanceDocumentationStateMachine.getValidTargetStates(ApprovalStatus.APPROVED);
        assertEquals(1, approvedTargets.size());
        assertTrue(approvedTargets.contains(ApprovalStatus.DRAFT)); // Only universal rule

        // Test REJECTED state
        Set<ApprovalStatus> rejectedTargets = GovernanceDocumentationStateMachine.getValidTargetStates(ApprovalStatus.REJECTED);
        assertEquals(2, rejectedTargets.size());
        assertTrue(rejectedTargets.contains(ApprovalStatus.DRAFT)); // Universal rule
        assertTrue(rejectedTargets.contains(ApprovalStatus.IN_REVIEW));
    }

    @Test
    public void testTerminalState() {
        assertFalse(GovernanceDocumentationStateMachine.isTerminalState(null));
        assertFalse(GovernanceDocumentationStateMachine.isTerminalState(ApprovalStatus.DRAFT));
        assertFalse(GovernanceDocumentationStateMachine.isTerminalState(ApprovalStatus.IN_REVIEW));
        assertTrue(GovernanceDocumentationStateMachine.isTerminalState(ApprovalStatus.APPROVED));
        assertFalse(GovernanceDocumentationStateMachine.isTerminalState(ApprovalStatus.REJECTED));
    }

    @Test
    public void testObjectApprovalRules() {
        // Only APPROVED state enables object approval
        assertFalse(GovernanceDocumentationStateMachine.enablesObjectApproval(null));
        assertFalse(GovernanceDocumentationStateMachine.enablesObjectApproval(ApprovalStatus.DRAFT));
        assertFalse(GovernanceDocumentationStateMachine.enablesObjectApproval(ApprovalStatus.IN_REVIEW));
        assertTrue(GovernanceDocumentationStateMachine.enablesObjectApproval(ApprovalStatus.APPROVED));
        assertFalse(GovernanceDocumentationStateMachine.enablesObjectApproval(ApprovalStatus.REJECTED));

        // All states except APPROVED block object approval
        assertTrue(GovernanceDocumentationStateMachine.blocksObjectApproval(null));
        assertTrue(GovernanceDocumentationStateMachine.blocksObjectApproval(ApprovalStatus.DRAFT));
        assertTrue(GovernanceDocumentationStateMachine.blocksObjectApproval(ApprovalStatus.IN_REVIEW));
        assertFalse(GovernanceDocumentationStateMachine.blocksObjectApproval(ApprovalStatus.APPROVED));
        assertTrue(GovernanceDocumentationStateMachine.blocksObjectApproval(ApprovalStatus.REJECTED));
    }

    @Test
    public void testValidTransitionsText() {
        // Test descriptive text generation
        String initialText = GovernanceDocumentationStateMachine.getValidTransitionsText(null);
        assertTrue(initialText.contains("NULL → DRAFT"));

        String draftText = GovernanceDocumentationStateMachine.getValidTransitionsText(ApprovalStatus.DRAFT);
        assertTrue(draftText.contains("DRAFT → DRAFT"));
        assertTrue(draftText.contains("DRAFT → IN_REVIEW"));

        String approvedText = GovernanceDocumentationStateMachine.getValidTransitionsText(ApprovalStatus.APPROVED);
        assertTrue(approvedText.contains("APPROVED → DRAFT"));
    }
}