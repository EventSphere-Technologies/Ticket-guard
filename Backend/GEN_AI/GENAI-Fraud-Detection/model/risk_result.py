from enum import Enum
from typing import List
from pydantic import BaseModel, Field


class RiskLevel(str, Enum):
    """
    Enumeration of calculated fraud risk levels.
    """
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"
    CRITICAL = "CRITICAL"


class RiskResult(BaseModel):
    """
    Model representing calculated fraud risk evaluation outputs.
    """
    risk_score: float = Field(..., ge=0.0, le=100.0, description="Calculated fraud risk index between 0.0 (safe) and 100.0 (fraud).")
    risk_level: RiskLevel = Field(..., description="Categorical risk classification level.")
    is_bot: bool = Field(default=False, description="Evaluated probability flag indicating automated script or bot activity.")
    risk_factors: List[str] = Field(default_factory=list, description="List of specific fraud risk triggers or anomalies identified.")

    class Config:
        json_schema_extra = {
            "example": {
                "risk_score": 15.0,
                "risk_level": "LOW",
                "is_bot": False,
                "risk_factors": ["Fast checkout duration"]
            }
        }
