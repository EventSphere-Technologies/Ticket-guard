from datetime import datetime
from pydantic import BaseModel, Field


class BookingData(BaseModel):
    """
    Model representing ticket booking request details and payment metadata.
    """
    booking_id: str = Field(..., description="Unique identification key for the booking transaction.")
    user_id: str = Field(..., description="Unique identifier for the user placing the booking.")
    event_id: str = Field(..., description="Target event or show identifier.")
    ticket_quantity: int = Field(..., gt=0, description="Total number of tickets requested.")
    total_amount: float = Field(..., ge=0.0, description="Total monetary transaction amount.")
    currency: str = Field(default="USD", description="ISO 4217 currency code for the payment.")
    payment_method: str = Field(..., description="Payment channel or instrument used (e.g., CREDIT_CARD, PAYPAL).")
    timestamp: datetime = Field(..., description="UTC timestamp of the booking attempt.")
    ip_address: str = Field(..., description="Originating IPv4 or IPv6 address of the client request.")
    user_agent: str = Field(..., description="HTTP User-Agent string sent by the client browser or application.")

    class Config:
        json_schema_extra = {
            "example": {
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
            }
        }
