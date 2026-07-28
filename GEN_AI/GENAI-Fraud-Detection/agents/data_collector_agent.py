"""
Data Collector Agent module.

This module provides the DataCollectorAgent class responsible for ingesting,
validating, and sanitizing incoming booking requests and behavioral telemetry
prior to downstream multi-agent fraud analysis.
"""

import logging
from typing import Any, Dict, Tuple, Union

from pydantic import ValidationError

from model.behaviour_data import BehaviourData
from model.booking_data import BookingData

# Configure module-level logger
logger = logging.getLogger(__name__)


class DataCollectorAgentException(Exception):
    """Custom exception raised when data collection or validation fails."""
    pass


class DataCollectorAgent:
    """
    Agent responsible for collecting, validating, and normalizing incoming
    booking data and user behavioral telemetry for fraud evaluation.
    """

    def __init__(self) -> None:
        """Initialize the DataCollectorAgent instance."""
        logger.info("DataCollectorAgent initialized.")

    def validate_booking_data(
        self, raw_booking: Union[Dict[str, Any], BookingData]
    ) -> BookingData:
        """
        Validates raw booking payload against the BookingData Pydantic model.

        Args:
            raw_booking: Raw dictionary payload or existing BookingData instance.

        Returns:
            Validated BookingData model instance.

        Raises:
            DataCollectorAgentException: If mandatory fields are missing or invalid.
        """
        if isinstance(raw_booking, BookingData):
            logger.debug("Received pre-instantiated BookingData object.")
            return raw_booking

        if not isinstance(raw_booking, dict):
            msg = f"Invalid booking payload type: expected dict or BookingData, got {type(raw_booking).__name__}"
            logger.error(msg)
            raise DataCollectorAgentException(msg)

        logger.info("Validating incoming booking data payload.")
        try:
            booking = BookingData(**raw_booking)
            logger.info("Booking data successfully validated for booking_id: %s", booking.booking_id)
            return booking
        except ValidationError as e:
            msg = f"Booking data validation failed: {e.errors()}"
            logger.error(msg)
            raise DataCollectorAgentException(msg) from e
        except Exception as e:
            msg = f"Unexpected error during booking validation: {str(e)}"
            logger.error(msg)
            raise DataCollectorAgentException(msg) from e

    def validate_and_extract_behaviour(
        self, raw_behaviour: Union[Dict[str, Any], BehaviourData, None] = None
    ) -> BehaviourData:
        """
        Validates, normalizes, and extracts a clean BehaviourData object from telemetry.
        Fills sensible safe defaults for missing optional behavioral attributes.

        Args:
            raw_behaviour: Raw dictionary telemetry, existing BehaviourData instance, or None.

        Returns:
            Sanitized BehaviourData object ready for downstream agent analysis.

        Raises:
            DataCollectorAgentException: If critical schema type violations occur.
        """
        if isinstance(raw_behaviour, BehaviourData):
            logger.debug("Received pre-instantiated BehaviourData object.")
            return raw_behaviour

        payload: Dict[str, Any] = raw_behaviour if isinstance(raw_behaviour, dict) else {}

        # Normalize and sanitize default parameters gracefully
        sanitized_data = {
            "time_spent_seconds": max(0.0, float(payload.get("time_spent_seconds", 0.0))),
            "mouse_movement_entropy": min(1.0, max(0.0, float(payload.get("mouse_movement_entropy", 0.5)))),
            "keystroke_velocity": max(0.0, float(payload.get("keystroke_velocity", 0.0))),
            "pages_visited": max(1, int(payload.get("pages_visited", 1))),
            "failed_attempts": max(0, int(payload.get("failed_attempts", 0))),
            "is_headless_browser": bool(payload.get("is_headless_browser", False)),
            "device_fingerprint": payload.get("device_fingerprint")
        }

        logger.info("Normalizing behavioral telemetry data.")
        try:
            behaviour = BehaviourData(**sanitized_data)
            logger.info("Behavioral data sanitized successfully.")
            return behaviour
        except ValidationError as e:
            msg = f"Behaviour data normalization failed: {e.errors()}"
            logger.error(msg)
            raise DataCollectorAgentException(msg) from e

    def process_request(
        self,
        raw_booking: Union[Dict[str, Any], BookingData],
        raw_behaviour: Union[Dict[str, Any], BehaviourData, None] = None
    ) -> Tuple[BookingData, BehaviourData]:
        """
        Main entry point for DataCollectorAgent.
        Processes and validates both booking data and behavioral telemetry.

        Args:
            raw_booking: Raw booking dictionary or BookingData object.
            raw_behaviour: Optional behavioral metrics payload.

        Returns:
            Tuple containing validated (BookingData, BehaviourData).
        """
        logger.info("Starting data collection and validation process.")
        booking = self.validate_booking_data(raw_booking)
        behaviour = self.validate_and_extract_behaviour(raw_behaviour)
        return booking, behaviour
