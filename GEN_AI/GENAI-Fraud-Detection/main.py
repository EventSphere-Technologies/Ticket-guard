"""
Main FastAPI application entry point for GENAI-Fraud-Detection.

This module initializes the FastAPI service, configures endpoints for real-time
ticket fraud evaluation, and sets up exception handlers and logging.
"""

import logging
from typing import Any, Dict, Optional

from fastapi import FastAPI, HTTPException, status
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field

from model.agent_response import AgentResponse
from model.behaviour_data import BehaviourData
from model.booking_data import BookingData
from service.ai_fraud_detection_service import (
    AIFraudDetectionService,
    AIFraudDetectionServiceException,
)

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger("genai_fraud_detection")

# Initialize FastAPI application
app = FastAPI(
    title="GENAI Fraud Detection API",
    description="Real-Time AI-Powered Ticket Fraud Detection System utilizing Multi-Agent Orchestration.",
    version="1.0.0"
)

# Initialize Fraud Detection Service singleton
fraud_service = AIFraudDetectionService()


class FraudDetectionRequest(BaseModel):
    """
    Request payload schema for ticket fraud evaluation endpoint.
    """
    booking: BookingData = Field(..., description="Ticket booking details and payment telemetry.")
    behaviour: Optional[BehaviourData] = Field(None, description="Client session behavioral metrics.")

    class Config:
        json_schema_extra = {
            "example": {
                "booking": {
                    "booking_id": "BK-987654321",
                    "user_id": "USR-102938",
                    "event_id": "EVT-554433",
                    "ticket_quantity": 4,
                    "total_amount": 450.00,
                    "currency": "USD",
                    "payment_method": "CREDIT_CARD",
                    "timestamp": "2026-07-21T22:45:00Z",
                    "ip_address": "192.168.1.100",
                    "user_agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
                },
                "behaviour": {
                    "time_spent_seconds": 12.5,
                    "mouse_movement_entropy": 0.85,
                    "keystroke_velocity": 140.0,
                    "pages_visited": 3,
                    "failed_attempts": 0,
                    "is_headless_browser": False,
                    "device_fingerprint": "dfp_a1b2c3d4e5f67890"
                }
            }
        }


@app.get("/", summary="Health Check", tags=["Health"])
async def root() -> Dict[str, str]:
    """
    Health check endpoint verifying API service availability.

    Returns:
        Dictionary containing service status and title.
    """
    logger.info("Health check endpoint pinged.")
    return {
        "status": "running",
        "service": "GENAI Fraud Detection API"
    }


@app.post(
    "/detect-fraud",
    response_model=AgentResponse,
    status_code=status.HTTP_200_OK,
    summary="Evaluate Ticket Fraud Risk",
    tags=["Fraud Detection"]
)
async def detect_fraud(payload: FraudDetectionRequest) -> AgentResponse:
    """
    Evaluates incoming ticket booking request and behavioral telemetry for fraud indicators.

    Args:
        payload: FraudDetectionRequest containing booking and optional behaviour data.

    Returns:
        AgentResponse model containing risk scores, decision result, and execution metrics.

    Raises:
        HTTPException: If fraud detection evaluation pipeline fails.
    """
    logger.info(
        "Received fraud detection request for booking_id: %s",
        payload.booking.booking_id
    )

    try:
        response: AgentResponse = fraud_service.detect_fraud(
            raw_booking=payload.booking,
            raw_behaviour=payload.behaviour
        )
        logger.info(
            "Fraud evaluation successful for booking_id %s: Status=%s",
            payload.booking.booking_id,
            response.status
        )
        return response

    except AIFraudDetectionServiceException as e:
        logger.error("Fraud detection service error: %s", str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Fraud detection pipeline failed: {str(e)}"
        )
    except Exception as e:
        logger.error("Unexpected error during request processing: %s", str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"An unexpected server error occurred: {str(e)}"
        )


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
