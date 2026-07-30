"""
Behaviour Analyzer Agent module.

This module provides the BehaviourAnalyzerAgent class responsible for inspecting
user interaction telemetry, session velocity, device characteristics, and booking
time patterns to detect suspicious behavior.
"""

import logging
from datetime import datetime
from typing import Any, Dict, List, Optional

from model.behaviour_data import BehaviourData
from model.booking_data import BookingData

# Configure module-level logger
logger = logging.getLogger(__name__)


class BehaviourAnalyzerException(Exception):
    """Custom exception raised when behavioral analysis fails or invalid inputs are provided."""
    pass


class BehaviourAnalyzerAgent:
    """
    Agent responsible for analyzing user interaction patterns, session metrics,
    and device telemetry to identify behavioral fraud indicators.
    """

    def __init__(self) -> None:
        """Initialize the BehaviourAnalyzerAgent instance."""
        logger.info("BehaviourAnalyzerAgent initialized.")

    def _analyze_session_duration(self, time_spent_seconds: float) -> Dict[str, Any]:
        """
        Analyzes time spent on the checkout session.

        Args:
            time_spent_seconds: Active session duration in seconds.

        Returns:
            Dictionary containing risk assessment for session duration.
        """
        if time_spent_seconds < 2.0:
            status = "CRITICAL_FAST"
            risk_score = 40.0
            description = "Abnormally fast checkout completion (< 2s), likely automated bot script."
        elif time_spent_seconds < 5.0:
            status = "SUSPICIOUS_FAST"
            risk_score = 20.0
            description = "Extremely rapid checkout completion (< 5s)."
        elif time_spent_seconds > 1800.0:  # > 30 minutes
            status = "EXTENDED_SESSION"
            risk_score = 10.0
            description = "Session idle or long duration (> 30 mins)."
        else:
            status = "NORMAL"
            risk_score = 0.0
            description = "Session duration within expected human parameters."

        return {
            "metric": "session_duration",
            "value_seconds": time_spent_seconds,
            "status": status,
            "risk_score_impact": risk_score,
            "description": description
        }

    def _analyze_mouse_movement(self, entropy: float) -> Dict[str, Any]:
        """
        Evaluates mouse movement entropy to detect robotic or linear cursor motion.

        Args:
            entropy: Mouse trajectory entropy between 0.0 and 1.0.

        Returns:
            Dictionary assessing cursor movement naturalness.
        """
        if entropy < 0.15:
            status = "ROBOTIC_LINEAR"
            risk_score = 35.0
            description = "Near-zero mouse entropy indicating synthetic or scripted cursor path."
        elif entropy < 0.35:
            status = "LOW_ENTROPY"
            risk_score = 15.0
            description = "Low mouse movement variation."
        else:
            status = "NATURAL_HUMAN"
            risk_score = 0.0
            description = "Natural mouse trajectory variation observed."

        return {
            "metric": "mouse_entropy",
            "value": entropy,
            "status": status,
            "risk_score_impact": risk_score,
            "description": description
        }

    def _analyze_keystroke_velocity(self, velocity: float) -> Dict[str, Any]:
        """
        Evaluates keystroke typing speed in characters per minute (CPM).

        Args:
            velocity: Typing speed CPM.

        Returns:
            Dictionary assessing keystroke dynamics.
        """
        if velocity > 800.0:
            status = "IMPOSSIBLE_SPEED"
            risk_score = 35.0
            description = "Keystroke velocity (> 800 CPM) exceeds human limits (clipboard injection or script)."
        elif velocity > 450.0:
            status = "HIGH_SPEED"
            risk_score = 15.0
            description = "Unusually fast typing speed observed."
        else:
            status = "NORMAL_SPEED"
            risk_score = 0.0
            description = "Keystroke speed within normal human range."

        return {
            "metric": "keystroke_velocity",
            "value_cpm": velocity,
            "status": status,
            "risk_score_impact": risk_score,
            "description": description
        }

    def _analyze_failure_frequency(self, failed_attempts: int) -> Dict[str, Any]:
        """
        Inspects failed payment or login attempt counts.

        Args:
            failed_attempts: Count of prior failed attempts in session.

        Returns:
            Dictionary assessing attempt failure risk.
        """
        if failed_attempts >= 5:
            status = "CRITICAL_FAILURES"
            risk_score = 40.0
            description = f"High failure frequency ({failed_attempts} failed attempts); possible card testing."
        elif failed_attempts >= 2:
            status = "ELEVATED_FAILURES"
            risk_score = 20.0
            description = f"Multiple failed attempts ({failed_attempts}) detected."
        else:
            status = "LOW_FAILURES"
            risk_score = 0.0
            description = "No significant prior failure count."

        return {
            "metric": "failed_attempts",
            "count": failed_attempts,
            "status": status,
            "risk_score_impact": risk_score,
            "description": description
        }

    def _analyze_booking_time(self, booking_timestamp: datetime) -> Dict[str, Any]:
        """
        Evaluates time of booking for off-peak or suspicious purchasing hours.

        Args:
            booking_timestamp: UTC timestamp of the booking attempt.

        Returns:
            Dictionary assessing time-based risk.
        """
        hour = booking_timestamp.hour
        # Off-peak hours between 01:00 AM and 05:00 AM UTC
        if 1 <= hour <= 5:
            status = "OFF_PEAK_HOURS"
            risk_score = 10.0
            description = f"Booking attempted during off-peak hours ({hour:02d}:00 UTC)."
        else:
            status = "STANDARD_HOURS"
            risk_score = 0.0
            description = "Booking attempted during standard operating hours."

        return {
            "metric": "booking_time",
            "timestamp_utc": booking_timestamp.isoformat(),
            "hour_of_day": hour,
            "status": status,
            "risk_score_impact": risk_score,
            "description": description
        }

    def _analyze_device_and_ip_consistency(
        self, ip_address: str, device_fingerprint: Optional[str], user_agent: str
    ) -> Dict[str, Any]:
        """
        Evaluates device fingerprint presence, IP format, and User-Agent parameters.

        Args:
            ip_address: Client IP address string.
            device_fingerprint: Optional hardware/browser fingerprint hash.
            user_agent: HTTP User-Agent header string.

        Returns:
            Dictionary evaluating device and network telemetry consistency.
        """
        anomalies: List[str] = []
        risk_impact = 0.0

        if not device_fingerprint or len(device_fingerprint.strip()) == 0:
            anomalies.append("MISSING_DEVICE_FINGERPRINT")
            risk_impact += 15.0

        if not ip_address or len(ip_address.strip()) == 0:
            anomalies.append("MISSING_IP_ADDRESS")
            risk_impact += 25.0

        if not user_agent or len(user_agent.strip()) == 0:
            anomalies.append("MISSING_USER_AGENT")
            risk_impact += 20.0

        status = "INCONSISTENT" if anomalies else "CONSISTENT"

        return {
            "metric": "device_ip_consistency",
            "ip_address": ip_address,
            "has_device_fingerprint": bool(device_fingerprint),
            "status": status,
            "anomalies": anomalies,
            "risk_score_impact": risk_impact,
            "description": "Device and network configuration consistency analysis."
        }

    def analyze(
        self, booking_data: BookingData, behaviour_data: BehaviourData
    ) -> Dict[str, Any]:
        """
        Executes comprehensive behavioral analysis on validated booking and interaction telemetry.

        Args:
            booking_data: Validated BookingData object.
            behaviour_data: Validated BehaviourData object.

        Returns:
            Structured dictionary containing detailed behavioral analysis metrics,
            anomalies, and cumulative behavioral risk score.

        Raises:
            BehaviourAnalyzerException: If required model objects are missing or invalid.
        """
        if not isinstance(booking_data, BookingData):
            msg = f"Invalid booking_data type: expected BookingData, got {type(booking_data).__name__}"
            logger.error(msg)
            raise BehaviourAnalyzerException(msg)

        if not isinstance(behaviour_data, BehaviourData):
            msg = f"Invalid behaviour_data type: expected BehaviourData, got {type(behaviour_data).__name__}"
            logger.error(msg)
            raise BehaviourAnalyzerException(msg)

        logger.info(
            "Executing behavioral analysis for booking_id: %s, user_id: %s",
            booking_data.booking_id,
            booking_data.user_id
        )

        try:
            session_eval = self._analyze_session_duration(behaviour_data.time_spent_seconds)
            mouse_eval = self._analyze_mouse_movement(behaviour_data.mouse_movement_entropy)
            keystroke_eval = self._analyze_keystroke_velocity(behaviour_data.keystroke_velocity)
            failure_eval = self._analyze_failure_frequency(behaviour_data.failed_attempts)
            time_eval = self._analyze_booking_time(booking_data.timestamp)
            device_eval = self._analyze_device_and_ip_consistency(
                booking_data.ip_address,
                behaviour_data.device_fingerprint,
                booking_data.user_agent
            )

            # Compute cumulative risk score from behavioral features (capped at 100.0)
            total_behavioral_risk = min(
                100.0,
                session_eval["risk_score_impact"]
                + mouse_eval["risk_score_impact"]
                + keystroke_eval["risk_score_impact"]
                + failure_eval["risk_score_impact"]
                + time_eval["risk_score_impact"]
                + device_eval["risk_score_impact"]
            )

            # Collect flagged anomalies
            flagged_anomalies: List[str] = []
            for eval_res in [session_eval, mouse_eval, keystroke_eval, failure_eval, time_eval]:
                if eval_res["risk_score_impact"] > 0.0:
                    flagged_anomalies.append(f"{eval_res['metric'].upper()}_{eval_res['status']}")

            flagged_anomalies.extend(device_eval.get("anomalies", []))

            is_suspicious_behavior = total_behavioral_risk >= 30.0

            analysis_result: Dict[str, Any] = {
                "booking_id": booking_data.booking_id,
                "user_id": booking_data.user_id,
                "behavioral_risk_score": total_behavioral_risk,
                "is_suspicious_behavior": is_suspicious_behavior,
                "flagged_anomalies": flagged_anomalies,
                "metrics_breakdown": {
                    "session_duration": session_eval,
                    "mouse_movement": mouse_eval,
                    "keystroke_velocity": keystroke_eval,
                    "failure_frequency": failure_eval,
                    "booking_time": time_eval,
                    "device_ip_consistency": device_eval
                },
                "analyzed_at": datetime.utcnow().isoformat() + "Z"
            }

            logger.info(
                "Behavioral analysis completed for booking_id %s: Risk Score = %.2f, Suspicious = %s",
                booking_data.booking_id,
                total_behavioral_risk,
                is_suspicious_behavior
            )

            return analysis_result

        except Exception as e:
            msg = f"Unexpected error during behavioral analysis: {str(e)}"
            logger.error(msg)
            raise BehaviourAnalyzerException(msg) from e
