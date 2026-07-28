# GENAI-Fraud-Detection

An AI-powered Ticket Fraud Detection System built with Python, FastAPI, LangChain, LangGraph, Pydantic, and Google Gemini API.

---

## Project Overview

**GENAI-Fraud-Detection** is an enterprise-grade AI solution designed to detect fraudulent ticket bookings in real-time. By leveraging a multi-agent orchestration architecture powered by LangGraph and Google Gemini, the system analyzes booking metadata, user behavior, and bot indicators to produce actionable fraud risk scores and automated decision recommendations.

---

## Project Architecture

The system utilizes an agentic workflow to evaluate incoming ticket booking requests:

1. **Data Collector Agent**: Ingests and normalizes raw booking payload and behavioral data.
2. **Behavior Analyzer Agent**: Inspects user session telemetry, interaction frequency, and patterns.
3. **Bot Detection Agent**: Evaluates user-agent patterns, speed thresholds, and automated script markers.
4. **Risk Score Agent**: Combines telemetry outputs and assigns a calibrated risk score (0-100).
5. **Decision Agent**: Rules engine paired with Gemini LLM reasoning to determine action (`ALLOW`, `FLAG_FOR_REVIEW`, `BLOCK`).
6. **Action Recommendation Agent**: Suggests targeted mitigation steps (e.g., Step-Up Auth, 2FA, Manual Audit).
7. **Response Formatter Agent**: Formats the final diagnostic payload for backend consumers.

```
Incoming Booking Request
          │
          ▼
   [ FastAPI Endpoint ]
          │
          ▼
   [ LangGraph Engine ] ──► ( Multi-Agent Analysis Pipeline )
          │
          ▼
   [ Google Gemini LLM ] ──► Risk Scoring & Decision Engine
          │
          ▼
  Structured JSON Output
```

---

## Folder Structure

```text
GENAI-Fraud-Detection/
│
├── agents/                      # Specialized agent definitions
│   ├── data_collector_agent.py
│   ├── behaviour_analyzer_agent.py
│   ├── bot_detection_agent.py
│   ├── risk_score_agent.py
│   ├── decision_agent.py
│   ├── action_recommendation_agent.py
│   └── response_formatter_agent.py
│
├── model/                       # Pydantic data schemas
│   ├── booking_data.py
│   ├── behaviour_data.py
│   ├── risk_result.py
│   ├── decision_result.py
│   └── agent_response.py
│
├── prompt/                      # LLM Prompt builders
│   └── gemini_prompt_builder.py
│
├── service/                     # Core fraud detection service
│   └── ai_fraud_detection_service.py
│
├── config/                      # Application configuration & settings
│   └── gemini_config.py
│
├── utils/                       # Shared helper utilities
│   └── json_util.py
│
├── testdata/                    # Mock JSON payloads for testing
│   ├── normal_booking.json
│   ├── suspicious_booking.json
│   └── bot_booking.json
│
├── main.py                      # FastAPI app entry point
├── requirements.txt             # Python dependencies
├── .env                         # Environment configurations
├── .env.example                 # Sample environment template
├── README.md                    # Project documentation
└── IntegrationGuide.md          # Backend API integration guide
```

---

## Installation Steps

### Virtual Environment Setup

Ensure Python 3.10+ is installed on your system.

**On Linux/macOS:**
```bash
python3 -m venv venv
source venv/bin/activate
```

**On Windows (PowerShell):**
```powershell
python -m venv venv
.\venv\Scripts\Activate.ps1
```

### Install Dependencies

Copy the environment template and install requirements:

```bash
cp .env.example .env
pip install --upgrade pip
pip install -r requirements.txt
```

Set your Google Gemini API key inside `.env`:
```env
GOOGLE_API_KEY=your_actual_gemini_api_key_here
MODEL_NAME=gemini-2.5-flash
```

---

## Running the Project

Start the FastAPI service using Uvicorn:

```bash
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

Once running, access the interactive API documentation at:
- **Swagger UI**: `http://localhost:8000/docs`
- **ReDoc**: `http://localhost:8000/redoc`

---

## Sample API Flow

1. **Client Request**: External backend sends `POST /api/v1/detect-fraud` with booking telemetry.
2. **Validation**: Pydantic validates input schemas (`BookingData` and `BehaviourData`).
3. **Agent Orchestration**: LangGraph routes data across specialized evaluation agents.
4. **LLM Evaluation**: Google Gemini evaluates complex behavioral traits & risk flags.
5. **Response**: System returns standard decision schema including risk score, risk level, decision, and action recommendations.

---

## Future Integration

- **gRPC API Support**: High-throughput microservice communication.
- **Redis Caching**: Caching user reputation and IP velocity history.
- **Kafka Event Streaming**: Async ticket queue processing for high-volume sales events.
- **Database Persistence**: Historical fraud log storage & analytical dashboard integration.
