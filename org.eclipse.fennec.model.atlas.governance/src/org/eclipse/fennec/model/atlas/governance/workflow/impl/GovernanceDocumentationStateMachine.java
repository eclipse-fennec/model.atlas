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
package org.eclipse.fennec.model.atlas.governance.workflow.impl;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.fennec.model.atlas.governance.ApprovalStatus;

/**
 * State machine helper for governance documentation state transitions.
 * 
 * <p>This class encapsulates the business rules for valid state transitions
 * in the governance documentation lifecycle:</p>
 * 
 * <pre>
 * State Transition Model:
 * Any State → DRAFT → IN_REVIEW → APPROVED (success path)
 *                       ↓
 *                   REJECTED → IN_REVIEW (resubmission)
 *                       ↓
 *                     DRAFT (rework)
 * </pre>
 * 
 * <p><strong>State Transition Rules:</strong></p>
 * <ul>
 * <li><strong>DRAFT</strong>: Can transition from any state (reset/rework capability)</li>
 * <li><strong>IN_REVIEW</strong>: Only from DRAFT or REJECTED states</li>
 * <li><strong>APPROVED</strong>: Only from IN_REVIEW state (terminal state for object approval)</li>
 * <li><strong>REJECTED</strong>: Only from IN_REVIEW state (blocks object approval until resolved)</li>
 * </ul>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
public class GovernanceDocumentationStateMachine {

    /**
     * Map of valid transitions from each state.
     * Key: Current state, Value: Set of valid target states
     */
    private static final Map<ApprovalStatus, Set<ApprovalStatus>> VALID_TRANSITIONS = Map.of(
        ApprovalStatus.DRAFT, EnumSet.of(ApprovalStatus.IN_REVIEW),
        ApprovalStatus.IN_REVIEW, EnumSet.of(ApprovalStatus.APPROVED, ApprovalStatus.REJECTED),
        ApprovalStatus.APPROVED, EnumSet.noneOf(ApprovalStatus.class), // Terminal state - no transitions
        ApprovalStatus.REJECTED, EnumSet.of(ApprovalStatus.DRAFT, ApprovalStatus.IN_REVIEW)
    );

    /**
     * Special rule: DRAFT can be reached from any state (reset/rework capability).
     * This is handled separately since it's a universal transition.
     */
    private static final ApprovalStatus UNIVERSAL_TARGET = ApprovalStatus.DRAFT;

    /**
     * Validates if a state transition is allowed according to the governance documentation state machine.
     * 
     * @param fromState the current state (can be null for initial state)
     * @param toState the target state (cannot be null)
     * @return true if the transition is valid, false otherwise
     * @throws IllegalArgumentException if toState is null
     */
    public static boolean isValidTransition(ApprovalStatus fromState, ApprovalStatus toState) {
        if (toState == null) {
            throw new IllegalArgumentException("Target state cannot be null");
        }

        // Initial state: can transition to DRAFT (first state)
        if (fromState == null) {
            return toState == ApprovalStatus.DRAFT;
        }

        // Universal transition: Any state can transition to DRAFT
        if (toState == UNIVERSAL_TARGET) {
            return true;
        }

        // Standard transitions based on state machine rules
        Set<ApprovalStatus> validTargets = VALID_TRANSITIONS.get(fromState);
        return validTargets != null && validTargets.contains(toState);
    }

    /**
     * Validates a state transition and throws an exception if invalid.
     * 
     * @param fromState the current state (can be null for initial state)
     * @param toState the target state (cannot be null)
     * @param context additional context for error messages (e.g., objectId)
     * @throws IllegalStateException if the transition is not valid
     * @throws IllegalArgumentException if toState is null
     */
    public static void validateTransition(ApprovalStatus fromState, ApprovalStatus toState, String context) {
        if (!isValidTransition(fromState, toState)) {
            String fromStateText = fromState != null ? fromState.toString() : "NULL";
            String contextText = context != null ? " for " + context : "";
            
            throw new IllegalStateException(
                String.format("Invalid governance documentation state transition from %s to %s%s. %s",
                    fromStateText, toState, contextText, getValidTransitionsText(fromState)));
        }
    }

    /**
     * Get all valid target states from a given current state.
     * 
     * @param fromState the current state (can be null for initial state)
     * @return set of valid target states
     */
    public static Set<ApprovalStatus> getValidTargetStates(ApprovalStatus fromState) {
        if (fromState == null) {
            // Initial state: can only go to DRAFT
            return EnumSet.of(ApprovalStatus.DRAFT);
        }

        Set<ApprovalStatus> validTargets = EnumSet.noneOf(ApprovalStatus.class);
        
        // Add universal target (DRAFT)
        validTargets.add(UNIVERSAL_TARGET);
        
        // Add state-specific valid targets
        Set<ApprovalStatus> stateSpecificTargets = VALID_TRANSITIONS.get(fromState);
        if (stateSpecificTargets != null) {
            validTargets.addAll(stateSpecificTargets);
        }
        
        return validTargets;
    }

    /**
     * Get a human-readable description of valid transitions from a state.
     * 
     * @param fromState the current state
     * @return descriptive text of valid transitions
     */
    public static String getValidTransitionsText(ApprovalStatus fromState) {
        if (fromState == null) {
            return "Valid initial transition: NULL → DRAFT";
        }

        Set<ApprovalStatus> validTargets = getValidTargetStates(fromState);
        if (validTargets.isEmpty()) {
            return "No valid transitions available from " + fromState + " (terminal state)";
        }

        StringBuilder sb = new StringBuilder("Valid transitions from ").append(fromState).append(": ");
        validTargets.forEach(target -> sb.append(fromState).append(" → ").append(target).append(", "));
        
        // Remove trailing comma and space
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }
        
        return sb.toString();
    }

    /**
     * Check if a state is a terminal state (no outbound transitions except to DRAFT).
     * 
     * @param state the state to check
     * @return true if the state is terminal
     */
    public static boolean isTerminalState(ApprovalStatus state) {
        if (state == null) {
            return false;
        }
        
        Set<ApprovalStatus> validTargets = VALID_TRANSITIONS.get(state);
        return validTargets == null || validTargets.isEmpty();
    }

    /**
     * Check if a state enables object approval in the governance-gated workflow.
     * 
     * @param state the governance documentation state
     * @return true if this state allows object approval
     */
    public static boolean enablesObjectApproval(ApprovalStatus state) {
        return ApprovalStatus.APPROVED.equals(state);
    }

    /**
     * Check if a state blocks object approval in the governance-gated workflow.
     * 
     * @param state the governance documentation state
     * @return true if this state blocks object approval
     */
    public static boolean blocksObjectApproval(ApprovalStatus state) {
        return !enablesObjectApproval(state);
    }
}