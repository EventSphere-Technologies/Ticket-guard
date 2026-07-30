from typing import Optional
from pydantic import BaseModel, Field


class BehaviourData(BaseModel):
    """
    Model representing user session interaction metrics, frontend telemetry, and bot heuristics.
    """
    time_spent_seconds: float = Field(..., ge=0.0, description="Total elapsed active time on checkout screen in seconds.")
    mouse_movement_entropy: float = Field(..., ge=0.0, le=1.0, description="Entropy score indicating naturalness of pointer trajectory (0.0 to 1.0).")
    keystroke_velocity: float = Field(..., ge=0.0, description="Average typing speed in characters per minute.")
    pages_visited: int = Field(..., ge=1, description="Number of site/app pages navigated prior to checkout submission.")
    failed_attempts: int = Field(default=0, ge=0, description="Count of prior failed booking or payment attempts in current session.")
    is_headless_browser: bool = Field(default=False, description="Flag indicating automated or headless browser detection.")
    device_fingerprint: Optional[str] = Field(None, description="Unique client hardware/browser persistent fingerprint hash.")

    class Config:
        json_schema_extra = {
            "example": {
                "time_spent_seconds": 12.5,
                "mouse_movement_entropy": 0.85,
                "keystroke_velocity": 140.0,
                "pages_visited": 3,
                "failed_attempts": 0,
                "is_headless_browser": False,
                "device_fingerprint": "dfp_a1b2c3d4e5f67890"
            }
        }
