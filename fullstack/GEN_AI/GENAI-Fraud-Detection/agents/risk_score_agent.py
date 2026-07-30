"""
Risk Score Agent module.

This module provides the RiskScoreAgent class responsible for aggregating evaluation
outputs from DataCollectorAgent, BehaviourAnalyzerAgent, and BotDetectionAgent to compute
a unified weighted fraud risk score and classify the final risk level.
"""

import logging
from typing import Any, Dict, List

from model.agent_response import AgentResponse
from model.behaviour_data import BehaviourData
from model.booking_data import BookingData
from model.risk_result import RiskLevel, RiskResult

# Configure module-level logger
logger = logging.getLogger(__name__)


class RiskScoreAgentException(Exception):
    """Custom exception raised when risk score calculation encounters invalid inputs or errors."""
    pass


class RiskScoreAgent:
    """
    Agent responsible for combining multi-agent telemetry and risk signals into a
    calibrated overall fraud risk index (0.0 to 100.0) and categorical RiskLevel.
    """

    # Scoring weights for composite risk calculation
    BOT_WEIGHT: float = 0.35
    BEHAVIOR_WEIGHT: float = 0.30
    VELOCITY_WEIGHT: float = 0.15
    DEVICE_IP_WEIGHT: float = 0.10
    TIME_WEIGHT: float = 0.10

    def __init__(self) -> None:
        """Initialize the RiskScoreAgent instance."""
        logger.info("RiskScoreAgent initialized.")

    def _determine_risk_level(self, score: float) -> RiskLevel:
        """
        Maps numerical risk score to categorical RiskLevel enum.

        Args:
            score: Risk index between 0.0 and 100.0.

        Returns:
            RiskLevel enum value (LOW, MEDIUM, HIGH, CRITICAL).
        """
        if score >= 85.0:
            return RiskLevel.CRITICAL
        elif score >= 60.0:
            return RiskLevel.HIGH
        elif score >= 30.0:
            return RiskLevel.MEDIUM
        else:
            return RiskLevel.LOW

    def calculate_risk(
        self,
        booking_data: BookingData,
        behaviour_data: BehaviourData,
        behaviour_analysis: Dict[str, Any],
        bot_detection: Dict[str, Any]
    ) -> RiskResult:
        """
        Aggregates output from prior agents to compute a weighted risk score and RiskResult.

        Args:
            booking_data: Validated BookingData object.
            behaviour_data: Validated BehaviourData object.
            behaviour_analysis: Dictionary output from BehaviourAnalyzerAgent.
            bot_detection: Dictionary output from BotDetectionAgent.

        Returns:
            Populated RiskResult model instance.

        Raises:
            RiskScoreAgentException: If invalid inputs are passed or scoring fails.
        """
        if not isinstance(booking_data, BookingData):
            msg = f"Invalid booking_data type: expected BookingData, got {type(booking_data).__name__}"
            logger.error(msg)
            raise RiskScoreAgentException(msg)

        if not isinstance(behaviour_data, BehaviourData):
            msg = f"Invalid behaviour_data type: expected BehaviourData, got {type(behaviour_data).__name__}"
            logger.error(msg)
            raise RiskScoreAgentException(msg)

        if not isinstance(behaviour_analysis, dict):
            msg = f"Invalid behaviour_analysis type: expected dict, got {type(behaviour_analysis).__name__}"
            logger.error(msg)
            raise RiskScoreAgentException(msg)

        if not isinstance(bot_detection, dict):
            msg = f"Invalid bot_detection type: expected dict, got {type(bot_detection).__name__}"
            logger.error(msg)
            raise RiskScoreAgentException(msg)

        logger.info(
            "Calculating comprehensive risk score for booking_id: %s",
            booking_data.booking_id
        )

        try:
            # 1. Extract component scores
            bot_confidence = float(bot_detection.get("confidence_score", 0.0))
            behavioral_score = float(behaviour_analysis.get("behavioral_risk_score", 0.0))

            # 2. Velocity and financial magnitude check
            velocity_score = 0.0
            contributing_factors: List[str] = []

            if booking_data.ticket_quantity >= 8:
                velocity_score += 40.0
                contributing_factors.append(f"BULK_TICKET_QUANTITY ({booking_data.ticket_quantity} tickets)")
            elif booking_data.ticket_quantity >= 5:
                velocity_score += 20.0
                contributing_factors.append(f"HIGH_TICKET_QUANTITY ({booking_data.ticket_quantity} tickets)")

            if booking_data.total_amount > 1000.0:
                velocity_score += 30.0
                contributing_factors.append(f"HIGH_TRANSACTION_VALUE (${booking_data.total_amount:.2f})")

            if behaviour_data.failed_attempts >= 3:
                velocity_score += 30.0
                contributing_factors.append(f"FAILED_ATTEMPTS_SPIKE ({behaviour_data.failed_attempts})")

            velocity_score = min(100.0, velocity_score)

            # 3. Device & IP Trust evaluation
            device_ip_score = 0.0
            if not behaviour_data.device_fingerprint:
                device_ip_score += 50.0
                contributing_factors.append("UNTRUSTED_DEVICE_FINGERPRINT_MISSING")

            if not booking_data.ip_address:
                device_ip_score += 50.0
                contributing_factors.append("IP_ADDRESS_MISSING")

            device_ip_score = min(100.0, device_ip_score)

            # 4. Time-based anomaly evaluation
            time_score = 0.0
            hour = booking_data.timestamp.hour
            if 1 <= hour <= 5:
                time_score = 60.0
                contributing_factors.append(f"OFF_PEAK_BOOKING_HOUR ({hour:02d}:00 UTC)")

            # 5. Weighted composite risk score computation
            composite_score = (
                (bot_confidence * self.BOT_WEIGHT)
                + (behavioral_score * self.BEHAVIOR_WEIGHT)
                + (velocity_score * self.VELOCITY_WEIGHT)
                + (device_ip_score * self.DEVICE_IP_WEIGHT)
                + (time_score * self.TIME_WEIGHT)
            )

            final_risk_score = round(min(100.0, max(0.0, composite_score)), 2)

            # Merge external factors from upstream agents
            bot_patterns = bot_detection.get("detected_patterns", [])
            for pattern in bot_patterns:
                if pattern not in contributing_factors:
                    contributing_factors.append(f"BOT_{pattern}")

            behavior_anomalies = behaviour_analysis.get("flagged_anomalies", [])
            for anomaly in behavior_anomalies:
                if anomaly not in contributing_factors:
                    contributing_factors.append(f"BEHAVIOR_{anomaly}")

            is_bot_flag = bool(bot_detection.get("is_bot", False)) or bot_confidence >= 50.0
            risk_level = self._determine_risk_level(final_risk_score)

            logger.info(
                "Risk evaluation complete for booking %s: Score=%.2f, Level=%s, IsBot=%s",
                booking_data.booking_id,
                final_risk_score,
                risk_level.value,
                is_bot_flag
            )

            return RiskResult(
                risk_score=final_risk_score,
                risk_level=risk_level,
                is_bot=is_bot_flag,
                risk_factors=contributing_factors
            )

        except Exception as e:
            msg = f"Unexpected error during risk calculation: {str(e)}"
            logger.error(msg)
            raise RiskScoreAgentException(msg) from e
