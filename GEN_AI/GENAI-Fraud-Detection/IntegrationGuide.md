# GENAI-Fraud-Detection Integration Guide

This guide details how external backend services (e.g., Node.js, Java, Python, Go) can integrate with the **GENAI-Fraud-Detection** microservice via RESTful APIs.

---

## API Endpoint

### Detect Fraud Endpoint

- **HTTP Method**: `POST`
- **Path**: `/api/v1/detect-fraud`
- **Content-Type**: `application/json`
- **Authentication**: Bearer Token or API Key header (configured per environment)

---

## Expected Input JSON

```json
{
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
    "keystroke_velocity": 140,
    "pages_visited": 3,
    "failed_attempts": 0,
    "is_headless_browser": false,
    "device_fingerprint": "dfp_a1b2c3d4e5f67890"
  }
}
```

---

## Expected Output JSON

```json
{
  "request_id": "req-7c9b2e1a-4d3f",
  "booking_id": "BK-987654321",
  "status": "SUCCESS",
  "risk_assessment": {
    "risk_score": 15,
    "risk_level": "LOW",
    "is_bot": false
  },
  "decision_result": {
    "decision": "ALLOW",
    "reasoning": "Standard user behavior pattern, normal checkout duration, low velocity score."
  },
  "recommended_actions": [
    "PROCEED_TO_CHECKOUT"
  ],
  "evaluated_at": "2026-07-21T22:45:02Z"
}
```

---

## Backend Integration Steps

### Step 1: Pre-Checkout Trigger

Invoke the detection API synchronously during checkout confirmation or asynchronously right before payment authorization.

### Step 2: Payload Construction

Assemble the `booking` metadata alongside telemetry collected from `behaviour` parameters (client-side analytics script or frontend SDK).

### Step 3: Call the Fraud Detection API

Make an HTTP POST request to `http://<service-host>:8000/api/v1/detect-fraud`.

### Step 4: Handle the Decision Response

Inspect the `decision` field in the response:
- **`ALLOW`**: Proceed with booking and payment capture.
- **`FLAG_FOR_REVIEW`**: Route booking to manual review queue or trigger step-up verification (3DS / SMS OTP).
- **`BLOCK`**: Decline the booking immediately and log security incident.

---

## Error Handling

When the service encounters an error, it returns a standard JSON error response object:

```json
{
  "error": {
    "code": "INVALID_PAYLOAD",
    "message": "Field 'ticket_quantity' must be greater than 0.",
    "details": [
      {
        "loc": ["body", "booking", "ticket_quantity"],
        "msg": "Input should be greater than 0",
        "type": "greater_than"
      }
    ]
  }
}
```

---

## Response Codes

| Status Code | Description | Scenario |
| :--- | :--- | :--- |
| **`200 OK`** | Success | Request was evaluated successfully. |
| **`400 Bad Request`** | Client Error | Payload schema validation failure or missing fields. |
| **`401 Unauthorized`** | Auth Error | Missing or invalid API key/Bearer token. |
| **`422 Unprocessable Entity`** | Validation Error | Malformed JSON or invalid parameter types. |
| **`500 Internal Server Error`** | Server Error | AI engine failure or downstream LLM connection issue. |
| **`503 Service Unavailable`** | Service Timeout | Downstream Gemini LLM service overload or rate limit. |
