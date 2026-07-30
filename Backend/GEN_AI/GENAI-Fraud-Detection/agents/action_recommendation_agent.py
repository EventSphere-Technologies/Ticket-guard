"""
Action Recommendation Agent module.

This module provides the ActionRecommendationAgent class responsible for translating
fraud decision outputs into structured operational action plans and mitigation steps.
"""

import logging
from typing import Any, Dict, List, Optional
from pydantic import BaseModel, Field

from model.decision_result import DecisionResult, DecisionType

# Configure module-level logger
logger = logging.getLogger(__name__)


class ActionRecommendationAgentException(Exception):
    """Custom exception raised when action recommendation evaluation fails."""
    pass


class ActionRecommendation(BaseModel):
    """
    Structured action recommendation payload returned after decision analysis.
    """
    decision: str = Field(..., description="Decision classification tag (APPROVE, MANUAL_REVIEW, BLOCK_AND_REVIEW, BLOCK_IMMEDIATELY).")
    recommended_actions: List[str] = Field(..., description="Targeted operational procedures to execute.")
    priority: str = Field(..., description="Priority level for executing recommended actions (LOW, MEDIUM, HIGH, CRITICAL).")
    reason: str = Field(..., description="Justification explaining why these actions were recommended.")
    next_step: str = Field(..., description="Immediate next workflow step for downstream services.")

    class Config:
        json_schema_extra = {
            "example": {
                "decision": "APPROVE",
                "recommended_actions": [
                    "Approve Booking",
                    "Allow Payment",
                    "Continue Workflow"
                ],
                "priority": "LOW",
                "reason": "Low risk confirmed. Booking telemetry consistent with legitimate human user behavior.",
                "next_step": "PROCEED_TO_CHECKOUT"
            }
        }


class ActionRecommendationAgent:
    """
    Agent responsible for generating structured mitigation recommendations and workflow
    next-steps based on the enforcement decisions made by DecisionAgent.
    """

    def __init__(self) -> None:
        """Initialize the ActionRecommendationAgent instance."""
        logger.info("ActionRecommendationAgent initialized.")

    def recommend(self, decision_result: DecisionResult) -> ActionRecommendation:
        """
        Generates structured action recommendations based on DecisionResult.

        Args:
            decision_result: DecisionResult model object from DecisionAgent.

        Returns:
            Populated ActionRecommendation object.

        Raises:
            ActionRecommendationAgentException: If input decision_result is invalid.
        """
        if not isinstance(decision_result, DecisionResult):
            msg = f"Invalid decision_result type: expected DecisionResult, got {type(decision_result).__name__}"
            logger.error(msg)
            raise ActionRecommendationAgentException(msg)

        logger.info("Generating action recommendations for decision: %s", decision_result.decision.value)

        try:
            # Extract decision parameters
            decision_val = decision_result.decision
            action_name = getattr(decision_result, "action_name", "").upper()
            reason_text = getattr(decision_result, "reason", decision_result.reasoning)
            risk_score = getattr(decision_result, "risk_score", 0.0)

            # Determine exact decision category rule
            if action_name == "BLOCK_IMMEDIATELY" or (decision_val == DecisionType.BLOCK and risk_score >= 85.0):
                category = "BLOCK_IMMEDIATELY"
                actions = [
                    "Cancel Booking",
                    "Blacklist IP",
                    "Blacklist Device",
                    "Notify Security Team",
                    "Store Fraud Log"
                ]
                priority = "CRITICAL"
                next_step = "ENFORCE_SECURITY_BLACKLIST_AND_CANCEL"

            elif action_name == "BLOCK_AND_REVIEW" or (decision_val == DecisionType.BLOCK and risk_score >= 60.0):
                category = "BLOCK_AND_REVIEW"
                actions = [
                    "Reject Booking",
                    "Block Payment",
                    "Notify Fraud Team",
                    "Log Fraud Event"
                ]
                priority = "HIGH"
                next_step = "CANCEL_PAYMENT_AND_ALERT_FRAUD_TEAM"

            elif action_name == "MANUAL_REVIEW" or decision_val == DecisionType.FLAG_FOR_REVIEW:
                category = "MANUAL_REVIEW"
                actions = [
                    "Hold Booking",
                    "Notify Admin",
                    "Create Investigation Ticket"
                ]
                priority = "MEDIUM"
                next_step = "ROUTE_TO_MANUAL_REVIEW_QUEUE"

            else:  # APPROVE / ALLOW
                category = "APPROVE"
                actions = [
                    "Approve Booking",
                    "Allow Payment",
                    "Continue Workflow"
                ]
                priority = "LOW"
                next_step = "PROCEED_TO_CHECKOUT"

            recommendation = ActionRecommendation(
                decision=category,
                recommended_actions=actions,
                priority=priority,
                reason=reason_text,
                next_step=next_step
            )

            logger.info(
                "Action recommendations generated successfully: Decision=%s, Priority=%s, ActionsCount=%d",
                category,
                priority,
                len(actions)
            )

            return recommendation

        except Exception as e:
            msg = f"Unexpected error while generating action recommendations: {str(e)}"
            logger.error(msg)
            raise ActionRecommendationAgentException(msg) from e
