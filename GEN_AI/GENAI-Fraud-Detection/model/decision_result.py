from enum import Enum
from typing import List
from pydantic import BaseModel, Field


class DecisionType(str, Enum):
    """
    Enumeration of automated enforcement decisions.
    """
    ALLOW = "ALLOW"
    FLAG_FOR_REVIEW = "FLAG_FOR_REVIEW"
    BLOCK = "BLOCK"


class DecisionResult(BaseModel):
    """
    Model representing AI-driven decision engine assessment and recommended actions.
    """
    decision: DecisionType = Field(..., description="Final operational decision for the transaction.")
    reasoning: str = Field(..., description="Human-readable explanation and summary justifying the decision.")
    recommended_actions: List[str] = Field(default_factory=list, description="Targeted remediation steps (e.g., Step-Up 3DS Auth, Manual Queue).")

    class Config:
        json_schema_extra = {
            "example": {
                "decision": "ALLOW",
                "reasoning": "Standard user behavior pattern, normal checkout duration, low velocity score.",
                "recommended_actions": ["PROCEED_TO_CHECKOUT"]
            }
        }
