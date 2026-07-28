from datetime import datetime
from typing import Optional
from pydantic import BaseModel, Field

from model.risk_result import RiskResult
from model.decision_result import DecisionResult


class AgentResponse(BaseModel):
    """
    Model representing the aggregated response produced after multi-agent execution pipeline finishes.
    """
    request_id: str = Field(..., description="Unique tracking identifier for the detection request execution.")
    booking_id: str = Field(..., description="Associated ticket booking request identifier.")
    status: str = Field(default="SUCCESS", description="Overall execution status of the detection agent pipeline.")
    risk_assessment: Optional[RiskResult] = Field(None, description="Detailed calculated risk metrics.")
    decision_result: Optional[DecisionResult] = Field(None, description="Final decision and recommended enforcement action.")
    evaluated_at: datetime = Field(..., description="UTC timestamp marking completion of agent evaluation pipeline.")

    class Config:
        json_schema_extra = {
            "example": {
                "request_id": "req-7c9b2e1a-4d3f",
                "booking_id": "BK-987654321",
                "status": "SUCCESS",
                "risk_assessment": {
                    "risk_score": 15.0,
                    "risk_level": "LOW",
                    "is_bot": False,
                    "risk_factors": ["Fast checkout duration"]
                },
                "decision_result": {
                    "decision": "ALLOW",
                    "reasoning": "Standard user behavior pattern, normal checkout duration, low velocity score.",
                    "recommended_actions": ["PROCEED_TO_CHECKOUT"]
                },
                "evaluated_at": "2026-07-21T22:45:02Z"
            }
        }
