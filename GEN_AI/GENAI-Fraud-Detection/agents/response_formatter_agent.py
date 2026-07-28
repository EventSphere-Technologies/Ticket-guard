"""
Response Formatter Agent module.

This module provides the ResponseFormatterAgent class responsible for aggregating
and formatting execution results from all prior fraud detection agents into a unified,
production-ready JSON response payload suitable for FastAPI endpoints and API clients.
"""

import logging
import time
from datetime import datetime
from typing import Any, Dict, List, Optional

from model.behaviour_data import BehaviourData
from model.booking_data import BookingData
from model.decision_result import DecisionResult
from model.risk_result import RiskResult

# Configure module-level logger
logger = logging.getLogger(__name__)


class ResponseFormatterAgentException(Exception):
    """Custom exception raised when response formatting encounters invalid inputs or errors."""
    pass


class ResponseFormatterAgent:
    """
    Agent responsible for compiling multi-agent evaluation outputs into a standardized
    final API response schema for consumption by external services and FastAPI endpoints.
    """

    def __init__(self) -> None:
        """Initialize the ResponseFormatterAgent instance."""
        logger.info("ResponseFormatterAgent initialized.")

    def format_response(
        self,
        booking_data: BookingData,
        behaviour_data: BehaviourData,
        behaviour_analysis: Dict[str, Any],
        bot_detection: Dict[str, Any],
        risk_result: RiskResult,
        decision_result: DecisionResult,
        action_recommendation: Any,
        start_time: Optional[float] = None
    ) -> Dict[str, Any]:
        """
        Aggregates outputs from all pipeline agents into a clean, unified response dictionary.

        Args:
            booking_data: Validated BookingData object from DataCollectorAgent.
            behaviour_data: Validated BehaviourData object from DataCollectorAgent.
            behaviour_analysis: Dictionary output from BehaviourAnalyzerAgent.
            bot_detection: Dictionary output from BotDetectionAgent.
            risk_result: RiskResult object from RiskScoreAgent.
            decision_result: DecisionResult object from DecisionAgent.
            action_recommendation: ActionRecommendation object from ActionRecommendationAgent.
            start_time: Optional float timestamp marking pipeline execution start time.

        Returns:
            Standardized API response dictionary ready for FastAPI serialization.

        Raises:
            ResponseFormatterAgentException: If required agent outputs are missing or invalid.
        """
        logger.info(
            "Formatting final API response payload for booking_id: %s",
            getattr(booking_data, "booking_id", "UNKNOWN")
        )

        try:
            # 1. Calculate processing duration
            if start_time is not None:
                processing_time_sec = round(max(0.0, time.time() - start_time), 4)
            else:
                processing_time_sec = 0.005

            # 2. Extract decision details
            decision_val = (
                getattr(action_recommendation, "decision", None)
                or getattr(decision_result, "action_name", None)
                or getattr(decision_result.decision, "value", str(decision_result.decision))
            )

            # 3. Extract recommended actions list
            rec_actions = (
                getattr(action_recommendation, "recommended_actions", None)
                or getattr(decision_result, "recommended_actions", [])
            )

            # 4. Extract reason & confidence
            reason_text = (
                getattr(action_recommendation, "reason", None)
                or getattr(decision_result, "reason", None)
                or getattr(decision_result, "reasoning", "")
            )

            confidence_val = (
                getattr(decision_result, "confidence", None)
                or float(bot_detection.get("confidence_score", 90.0))
            )

            # 5. Extract detected behaviors and fraud indicators
            detected_behaviours = list(behaviour_analysis.get("flagged_anomalies", []))
            fraud_indicators = list(getattr(risk_result, "risk_factors", []))

            # 6. Extract bot probability
            bot_probability = float(bot_detection.get("confidence_score", 0.0))

            # 7. Extract risk level value
            risk_level_str = (
                risk_result.risk_level.value
                if hasattr(risk_result.risk_level, "value")
                else str(risk_result.risk_level)
            )

            # 8. Build final response schema
            response_payload: Dict[str, Any] = {
                "status": "SUCCESS",
                "booking_id": booking_data.booking_id,
                "user_id": booking_data.user_id,
                "event_id": booking_data.event_id,
                "risk_score": risk_result.risk_score,
                "risk_level": risk_level_str,
                "bot_probability": bot_probability,
                "decision": decision_val,
                "recommended_action": rec_actions,
                "reason": reason_text,
                "detected_behaviours": detected_behaviours,
                "fraud_indicators": fraud_indicators,
                "confidence": confidence_val,
                "processing_time": processing_time_sec,
                "timestamp": datetime.utcnow().isoformat() + "Z"
            }

            logger.info(
                "Response payload formatted successfully for booking %s: Decision=%s, RiskScore=%.2f",
                booking_data.booking_id,
                decision_val,
                risk_result.risk_score
            )

            return response_payload

        except Exception as e:
            msg = f"Unexpected error formatting agent response payload: {str(e)}"
            logger.error(msg)
            raise ResponseFormatterAgentException(msg) from e
