"""
Decision Agent module.

This module provides the DecisionAgent class responsible for evaluating RiskResult
outputs and Google Gemini AI insights to enforce operational fraud prevention decisions.
"""

import logging
from datetime import datetime
from typing import Any, Dict, Optional

from model.decision_result import DecisionResult, DecisionType
from model.risk_result import RiskLevel, RiskResult

# Configure module-level logger
logger = logging.getLogger(__name__)


class DecisionAgentException(Exception):
    """Custom exception raised when decision evaluation fails or receives invalid inputs."""
    pass


class DecisionAgent:
    """
    Agent responsible for evaluating calculated fraud risk metrics and Gemini LLM insights
    to determine the final operational enforcement decision (ALLOW, FLAG_FOR_REVIEW, BLOCK).
    """

    def __init__(self) -> None:
        """Initialize the DecisionAgent instance."""
        logger.info("DecisionAgent initialized.")

    def evaluate_decision(
        self,
        risk_result: RiskResult,
        gemini_analysis: Optional[Dict[str, Any]] = None
    ) -> DecisionResult:
        """
        Evaluates RiskResult object and optional Gemini LLM analysis to generate DecisionResult.

        Decision Rules:
        - If Gemini Analysis is present:
            - Gemini "ALLOW"  -> DecisionType.ALLOW (APPROVE)
            - Gemini "REVIEW" -> DecisionType.FLAG_FOR_REVIEW (MANUAL_REVIEW)
            - Gemini "BLOCK"  -> DecisionType.BLOCK (BLOCK_IMMEDIATELY or BLOCK_AND_REVIEW)
        - If Gemini is absent (Fallback Rule-Based):
            - LOW (0-29.9)      -> ALLOW / APPROVE
            - MEDIUM (30-59.9)  -> FLAG_FOR_REVIEW / MANUAL_REVIEW
            - HIGH (60-84.9)    -> BLOCK / BLOCK_AND_REVIEW
            - CRITICAL (85-100) -> BLOCK / BLOCK_IMMEDIATELY

        Args:
            risk_result: Validated RiskResult object from RiskScoreAgent.
            gemini_analysis: Optional dictionary response from Google Gemini API.

        Returns:
            Populated DecisionResult model instance.

        Raises:
            DecisionAgentException: If input risk_result is invalid.
        """
        if not isinstance(risk_result, RiskResult):
            msg = f"Invalid risk_result type: expected RiskResult, got {type(risk_result).__name__}"
            logger.error(msg)
            raise DecisionAgentException(msg)

        logger.info(
            "Evaluating operational decision for Risk Score: %.2f (Level: %s, IsBot: %s, GeminiProvided: %s)",
            risk_result.risk_score,
            risk_result.risk_level.value,
            risk_result.is_bot,
            bool(gemini_analysis)
        )

        try:
            timestamp_str = datetime.utcnow().isoformat() + "Z"
            risk_score = risk_result.risk_score
            risk_level = risk_result.risk_level

            # Check if valid Gemini AI Analysis is available
            if gemini_analysis and isinstance(gemini_analysis, dict) and "decision" in gemini_analysis:
                raw_gemini_dec = str(gemini_analysis["decision"]).upper().strip()
                gemini_reason = gemini_analysis.get("reasoning", "")
                gemini_confidence = float(gemini_analysis.get("confidence", 90.0))

                logger.info("Incorporating Gemini LLM decision: '%s'", raw_gemini_dec)

                if raw_gemini_dec in ("ALLOW", "APPROVE"):
                    decision_enum = DecisionType.ALLOW
                    action_name = "APPROVE"
                    recommended_action = "PROCEED_TO_CHECKOUT"
                    reason = f"[Gemini AI] {gemini_reason}" if gemini_reason else "Gemini AI approved transaction as low risk."
                    confidence = gemini_confidence

                elif raw_gemini_dec in ("REVIEW", "MANUAL_REVIEW", "FLAG_FOR_REVIEW"):
                    decision_enum = DecisionType.FLAG_FOR_REVIEW
                    action_name = "MANUAL_REVIEW"
                    recommended_action = "TRIGGER_STEP_UP_AUTHENTICATION_AND_REVIEW"
                    reason = f"[Gemini AI] {gemini_reason}" if gemini_reason else "Gemini AI recommended manual fraud review."
                    confidence = gemini_confidence

                else:  # BLOCK / BLOCK_IMMEDIATELY / BLOCK_AND_REVIEW
                    decision_enum = DecisionType.BLOCK
                    if risk_score >= 85.0 or risk_result.is_bot:
                        action_name = "BLOCK_IMMEDIATELY"
                        recommended_action = "BLOCK_TRANSACTION_AND_BLACKLIST_DEVICE"
                    else:
                        action_name = "BLOCK_AND_REVIEW"
                        recommended_action = "HOLD_TRANSACTION_FOR_FRAUD_INVESTIGATION"

                    reason = f"[Gemini AI] {gemini_reason}" if gemini_reason else "Gemini AI recommended blocking suspicious transaction."
                    confidence = gemini_confidence

            else:
                # Rule-Based Fallback Strategy
                logger.info("Using Rule-Based decision fallback engine.")
                if risk_level == RiskLevel.CRITICAL or risk_result.is_bot:
                    decision_enum = DecisionType.BLOCK
                    action_name = "BLOCK_IMMEDIATELY"
                    recommended_action = "BLOCK_TRANSACTION_AND_BLACKLIST_DEVICE"
                    reason = (
                        f"Critical risk detected (Score: {risk_score:.1f}, Level: {risk_level.value}). "
                        f"Triggers: {', '.join(risk_result.risk_factors or ['Bot activity'])}. Immediate blockage enforced."
                    )
                    confidence = round(min(99.0, max(85.0, 80.0 + (risk_score * 0.15))), 2)

                elif risk_level == RiskLevel.HIGH:
                    decision_enum = DecisionType.BLOCK
                    action_name = "BLOCK_AND_REVIEW"
                    recommended_action = "HOLD_TRANSACTION_FOR_FRAUD_INVESTIGATION"
                    reason = (
                        f"High risk detected (Score: {risk_score:.1f}, Level: {risk_level.value}). "
                        f"Triggers: {', '.join(risk_result.risk_factors)}. Transaction blocked and flagged for review."
                    )
                    confidence = round(min(95.0, max(75.0, 70.0 + (risk_score * 0.2))), 2)

                elif risk_level == RiskLevel.MEDIUM:
                    decision_enum = DecisionType.FLAG_FOR_REVIEW
                    action_name = "MANUAL_REVIEW"
                    recommended_action = "TRIGGER_STEP_UP_AUTHENTICATION_AND_REVIEW"
                    reason = (
                        f"Medium risk detected (Score: {risk_score:.1f}, Level: {risk_level.value}). "
                        f"Anomalies found: {', '.join(risk_result.risk_factors)}. Flagged for manual review."
                    )
                    confidence = round(min(90.0, max(70.0, 65.0 + (risk_score * 0.2))), 2)

                else:  # RiskLevel.LOW
                    decision_enum = DecisionType.ALLOW
                    action_name = "APPROVE"
                    recommended_action = "PROCEED_TO_CHECKOUT"
                    reason = (
                        f"Low risk confirmed (Score: {risk_score:.1f}, Level: {risk_level.value}). "
                        "Booking telemetry consistent with legitimate human user behavior."
                    )
                    confidence = round(min(99.0, max(80.0, 100.0 - risk_score)), 2)

            decision_result = DecisionResult(
                decision=decision_enum,
                reasoning=reason,
                recommended_actions=[recommended_action]
            )

            # Dynamically attach extended attributes for downstream consumers
            object.__setattr__(decision_result, "reason", reason)
            object.__setattr__(decision_result, "risk_level", risk_level.value)
            object.__setattr__(decision_result, "risk_score", risk_score)
            object.__setattr__(decision_result, "recommended_action", recommended_action)
            object.__setattr__(decision_result, "confidence", confidence)
            object.__setattr__(decision_result, "timestamp", timestamp_str)
            object.__setattr__(decision_result, "action_name", action_name)
            object.__setattr__(decision_result, "gemini_evaluated", bool(gemini_analysis))

            logger.info(
                "Decision evaluation complete: Decision=%s (%s), Confidence=%.1f%%, Gemini=%s",
                decision_enum.value,
                action_name,
                confidence,
                bool(gemini_analysis)
            )

            return decision_result

        except Exception as e:
            msg = f"Unexpected error during decision evaluation: {str(e)}"
            logger.error(msg)
            raise DecisionAgentException(msg) from e
