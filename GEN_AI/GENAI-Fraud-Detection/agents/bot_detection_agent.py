"""
Bot Detection Agent module.

This module provides the BotDetectionAgent class responsible for analyzing booking telemetry,
browser fingerprints, automation flags, and user interaction signals to determine if a
booking request was initiated by an automated bot script.
"""

import logging
from typing import Any, Dict, List, Optional, Set

from model.behaviour_data import BehaviourData
from model.booking_data import BookingData
from model.risk_result import RiskResult

# Configure module-level logger
logger = logging.getLogger(__name__)


class BotDetectionAgentException(Exception):
    """Custom exception raised when bot detection processing fails."""
    pass


class BotDetectionAgent:
    """
    Agent dedicated to inspecting booking telemetry and behavioral dynamics to detect
    bot traffic, automated checkout tools, headless browsers, and scraper scripts.
    """

    # Known bot / automation signatures in User-Agent strings
    SUSPICIOUS_USER_AGENTS: Set[str] = {
        "headlesschrome",
        "puppeteer",
        "selenium",
        "phantomjs",
        "playwright",
        "scrapy",
        "python-requests",
        "python-urllib",
        "curl",
        "wget",
        "postmanruntime",
        "aiohttp",
        "httpx"
    }

    def __init__(self) -> None:
        """Initialize the BotDetectionAgent instance."""
        logger.info("BotDetectionAgent initialized.")

    def _check_user_agent(self, user_agent: str) -> Optional[str]:
        """
        Inspects the HTTP User-Agent string for automation library signatures.

        Args:
            user_agent: Raw HTTP User-Agent header value.

        Returns:
            Detected pattern flag string if suspicious, otherwise None.
        """
        if not user_agent or not user_agent.strip():
            return "MISSING_USER_AGENT"

        ua_lower = user_agent.lower()
        for bot_keyword in self.SUSPICIOUS_USER_AGENTS:
            if bot_keyword in ua_lower:
                return f"AUTOMATION_USER_AGENT ({bot_keyword})"

        return None

    def detect(
        self,
        booking_data: BookingData,
        behaviour_data: BehaviourData,
        historical_ip_count: int = 1,
        historical_device_count: int = 1
    ) -> Dict[str, Any]:
        """
        Evaluates booking details and session behaviour to calculate bot confidence score.

        Args:
            booking_data: Validated BookingData object.
            behaviour_data: Validated BehaviourData object.
            historical_ip_count: Count of recent bookings from the same IP address.
            historical_device_count: Count of recent bookings from the same device fingerprint.

        Returns:
            Structured dictionary containing bot detection results:
            - is_bot: bool
            - confidence_score: float (0.0 to 100.0)
            - detected_patterns: List[str]
            - explanation: str

        Raises:
            BotDetectionAgentException: If invalid inputs are provided or an internal error occurs.
        """
        if not isinstance(booking_data, BookingData):
            msg = f"Invalid booking_data type: expected BookingData, got {type(booking_data).__name__}"
            logger.error(msg)
            raise BotDetectionAgentException(msg)

        if not isinstance(behaviour_data, BehaviourData):
            msg = f"Invalid behaviour_data type: expected BehaviourData, got {type(behaviour_data).__name__}"
            logger.error(msg)
            raise BotDetectionAgentException(msg)

        logger.info(
            "Executing bot detection check for booking_id: %s, IP: %s",
            booking_data.booking_id,
            booking_data.ip_address
        )

        detected_patterns: List[str] = []
        confidence_points = 0.0

        try:
            # Rule 1: Headless Browser Flag
            if behaviour_data.is_headless_browser:
                detected_patterns.append("HEADLESS_BROWSER_DETECTED")
                confidence_points += 40.0

            # Rule 2: Suspicious User-Agent
            ua_flag = self._check_user_agent(booking_data.user_agent)
            if ua_flag:
                detected_patterns.append(ua_flag)
                confidence_points += 35.0

            # Rule 3: Extremely Fast Booking Completion (< 2 seconds)
            if behaviour_data.time_spent_seconds < 2.0:
                detected_patterns.append("EXTREMELY_FAST_COMPLETION (<2s)")
                confidence_points += 30.0
            elif behaviour_data.time_spent_seconds < 4.0:
                detected_patterns.append("SUSPICIOUSLY_FAST_COMPLETION (<4s)")
                confidence_points += 15.0

            # Rule 4: Zero Mouse Movement (Zero entropy with multiple interactions)
            if behaviour_data.mouse_movement_entropy <= 0.05:
                detected_patterns.append("NO_MOUSE_MOVEMENT (Robotic Entropy)")
                confidence_points += 25.0

            # Rule 5: High Click / Typing Frequency without mouse movement
            if behaviour_data.keystroke_velocity > 600.0:
                detected_patterns.append("SUPERHUMAN_KEYSTROKE_VELOCITY (>600 CPM)")
                confidence_points += 20.0

            # Rule 6: High Repeated Failed Attempt Count (Brute Force / Scraping)
            if behaviour_data.failed_attempts >= 3:
                detected_patterns.append(f"REPEATED_FAILED_ATTEMPTS ({behaviour_data.failed_attempts})")
                confidence_points += 20.0

            # Rule 7: High Booking Velocity from Same IP Address
            if historical_ip_count > 5:
                detected_patterns.append(f"HIGH_IP_BOOKING_VELOCITY ({historical_ip_count} bookings)")
                confidence_points += 25.0
            elif historical_ip_count > 2:
                detected_patterns.append(f"MULTIPLE_IP_BOOKINGS ({historical_ip_count} bookings)")
                confidence_points += 10.0

            # Rule 8: Repeated Bookings from Same Device Fingerprint
            if historical_device_count > 4:
                detected_patterns.append(f"HIGH_DEVICE_BOOKING_VELOCITY ({historical_device_count} bookings)")
                confidence_points += 25.0

            # Cap confidence score to 100.0 max
            final_confidence_score = min(100.0, confidence_points)
            is_bot = final_confidence_score >= 50.0

            # Construct human-readable explanation
            if is_bot:
                explanation = (
                    f"High probability bot detected (Confidence: {final_confidence_score:.1f}%). "
                    f"Triggered patterns: {', '.join(detected_patterns)}."
                )
            elif final_confidence_score > 20.0:
                explanation = (
                    f"Elevated bot suspicion (Confidence: {final_confidence_score:.1f}%). "
                    f"Minor indicators found: {', '.join(detected_patterns)}."
                )
            else:
                explanation = (
                    f"Human user behavior confirmed (Confidence: {final_confidence_score:.1f}%). "
                    "No critical automation signatures detected."
                )

            logger.info(
                "Bot detection completed for booking_id %s: is_bot=%s, confidence=%.1f%%",
                booking_data.booking_id,
                is_bot,
                final_confidence_score
            )

            return {
                "is_bot": is_bot,
                "confidence_score": round(final_confidence_score, 2),
                "detected_patterns": detected_patterns,
                "explanation": explanation
            }

        except Exception as e:
            msg = f"Unexpected error during bot detection processing: {str(e)}"
            logger.error(msg)
            raise BotDetectionAgentException(msg) from e
