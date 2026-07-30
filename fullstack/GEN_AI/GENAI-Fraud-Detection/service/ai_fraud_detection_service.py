"""
AI Fraud Detection Service module.

This module provides the AIFraudDetectionService class, acting as the primary orchestrator
for the multi-agent ticket fraud detection workflow, integrating Google Gemini LLM reasoning.
"""

import json
import logging
import time
import uuid
from datetime import datetime
from typing import Any, Dict, Optional, Union

from agents.action_recommendation_agent import ActionRecommendationAgent
from agents.behaviour_analyzer_agent import BehaviourAnalyzerAgent
from agents.bot_detection_agent import BotDetectionAgent
from agents.data_collector_agent import DataCollectorAgent
from agents.decision_agent import DecisionAgent
from agents.response_formatter_agent import ResponseFormatterAgent
from agents.risk_score_agent import RiskScoreAgent
from config.gemini_config import get_gemini_config
from model.agent_response import AgentResponse
from model.behaviour_data import BehaviourData
from model.booking_data import BookingData
from model.decision_result import DecisionResult
from model.risk_result import RiskResult
from prompt.gemini_prompt_builder import GeminiPromptBuilder

# Configure module-level logger
logger = logging.getLogger(__name__)


class AIFraudDetectionServiceException(Exception):
    """Custom exception raised when fraud detection orchestration fails."""
    pass


class AIFraudDetectionService:
    """
    Orchestrator service executing the end-to-end AI ticket fraud detection pipeline
    by coordinating multi-agent workflows and Google Gemini LLM risk reasoning.
    """

    def __init__(
        self,
        data_collector_agent: Optional[DataCollectorAgent] = None,
        behaviour_analyzer_agent: Optional[BehaviourAnalyzerAgent] = None,
        bot_detection_agent: Optional[BotDetectionAgent] = None,
        risk_score_agent: Optional[RiskScoreAgent] = None,
        decision_agent: Optional[DecisionAgent] = None,
        action_recommendation_agent: Optional[ActionRecommendationAgent] = None,
        response_formatter_agent: Optional[ResponseFormatterAgent] = None
    ) -> None:
        """
        Initializes AIFraudDetectionService using dependency injection for agents
        and configures the singleton Google Gemini model client.
        """
        self.data_collector_agent = data_collector_agent or DataCollectorAgent()
        self.behaviour_analyzer_agent = behaviour_analyzer_agent or BehaviourAnalyzerAgent()
        self.bot_detection_agent = bot_detection_agent or BotDetectionAgent()
        self.risk_score_agent = risk_score_agent or RiskScoreAgent()
        self.decision_agent = decision_agent or DecisionAgent()
        self.action_recommendation_agent = action_recommendation_agent or ActionRecommendationAgent()
        self.response_formatter_agent = response_formatter_agent or ResponseFormatterAgent()

        # Initialize Gemini Model once and reuse across requests
        try:
            gemini_cfg = get_gemini_config()
            self.gemini_model = gemini_cfg.get_generative_model()
            logger.info("AIFraudDetectionService initialized Gemini model successfully.")
        except Exception as e:
            logger.warning(
                "Failed to initialize Gemini model in AIFraudDetectionService: %s. "
                "The pipeline will operate using rule-based decision fallback.",
                str(e)
            )
            self.gemini_model = None

        logger.info("AIFraudDetectionService initialized successfully with agent dependencies.")

    def _evaluate_with_gemini(
        self,
        booking_data: BookingData,
        behaviour_analysis: Dict[str, Any],
        bot_detection: Dict[str, Any],
        risk_result: RiskResult
    ) -> Optional[Dict[str, Any]]:
        """
        Invokes Google Gemini API to analyze transaction risk and return structured reasoning.

        Args:
            booking_data: Validated BookingData object.
            behaviour_analysis: Output dictionary from BehaviourAnalyzerAgent.
            bot_detection: Output dictionary from BotDetectionAgent.
            risk_result: Calculated RiskResult object from RiskScoreAgent.

        Returns:
            Dictionary containing parsed Gemini decision and reasoning, or None if evaluation fails.
        """
        if self.gemini_model is None:
            logger.info("Gemini model is uninitialized. Skipping Gemini LLM evaluation.")
            return None

        logger.info("Calling Google Gemini API for fraud evaluation (Booking ID: %s)...", booking_data.booking_id)

        try:
            prompt_text = GeminiPromptBuilder.build_fraud_evaluation_prompt(
                booking_data=booking_data,
                behaviour_analysis=behaviour_analysis,
                bot_detection=bot_detection,
                risk_result=risk_result
            )

            response = self.gemini_model.generate_content(prompt_text)
            if not response or not response.text:
                logger.warning("Empty response received from Google Gemini API.")
                return None

            response_text = response.text.strip()
            logger.info("Gemini API raw response received: %s", response_text[:120] + "...")

            # Clean JSON response string (stripping code block markdown if present)
            clean_json_str = response_text
            if clean_json_str.startswith("```"):
                lines = clean_json_str.split("\n")
                if lines[0].startswith("```"):
                    lines = lines[1:]
                if lines and lines[-1].startswith("```"):
                    lines = lines[:-1]
                clean_json_str = "\n".join(lines).strip()

            parsed_data = json.loads(clean_json_str)
            logger.info(
                "Gemini evaluation parsed successfully: Decision=%s, Confidence=%s",
                parsed_data.get("decision"),
                parsed_data.get("confidence")
            )
            return parsed_data

        except Exception as e:
            logger.warning(
                "Gemini API invocation failed for booking %s: %s. Falling back smoothly to rule-based evaluation.",
                booking_data.booking_id,
                str(e)
            )
            return None

    def detect_fraud(
        self,
        raw_booking: Union[Dict[str, Any], BookingData],
        raw_behaviour: Union[Dict[str, Any], BehaviourData, None] = None,
        request_id: Optional[str] = None
    ) -> AgentResponse:
        """
        Executes the full multi-agent fraud evaluation workflow for an incoming booking,
        augmented with Google Gemini AI reasoning.

        Workflow steps:
        1. Validate & collect booking and behavioral telemetry (DataCollectorAgent).
        2. Analyze user behavior metrics and session patterns (BehaviourAnalyzerAgent).
        3. Detect bot signatures and automated checkout scripts (BotDetectionAgent).
        4. Calculate weighted composite fraud risk score (RiskScoreAgent).
        5. Invoke Google Gemini API for LLM risk reasoning and decision synthesis.
        6. Evaluate operational enforcement decision (DecisionAgent).
        7. Generate targeted action recommendations (ActionRecommendationAgent).
        8. Format unified JSON response payload (ResponseFormatterAgent).
        9. Return final AgentResponse model.

        Args:
            raw_booking: Booking details payload (dict or BookingData).
            raw_behaviour: Behavioral telemetry payload (dict or BehaviourData).
            request_id: Optional tracking identifier for request correlation.

        Returns:
            Populated AgentResponse instance.

        Raises:
            AIFraudDetectionServiceException: If pipeline execution fails at any stage.
        """
        start_time = time.time()
        req_id = request_id or f"req-{uuid.uuid4().hex[:12]}"

        logger.info("Starting AI fraud detection pipeline for request_id: %s", req_id)

        try:
            # Step 1: Data Collection & Validation
            booking_data, behaviour_data = self.data_collector_agent.process_request(
                raw_booking=raw_booking,
                raw_behaviour=raw_behaviour
            )

            # Step 2: Behavioral Analysis
            behaviour_analysis = self.behaviour_analyzer_agent.analyze(
                booking_data=booking_data,
                behaviour_data=behaviour_data
            )

            # Step 3: Bot Detection
            bot_detection = self.bot_detection_agent.detect(
                booking_data=booking_data,
                behaviour_data=behaviour_data
            )

            # Step 4: Risk Scoring
            risk_result = self.risk_score_agent.calculate_risk(
                booking_data=booking_data,
                behaviour_data=behaviour_data,
                behaviour_analysis=behaviour_analysis,
                bot_detection=bot_detection
            )

            # Step 5: Google Gemini LLM Synthesis
            gemini_analysis = self._evaluate_with_gemini(
                booking_data=booking_data,
                behaviour_analysis=behaviour_analysis,
                bot_detection=bot_detection,
                risk_result=risk_result
            )

            # Step 6: Operational Decision Evaluation (Passing Gemini AI Analysis)
            decision_result = self.decision_agent.evaluate_decision(
                risk_result=risk_result,
                gemini_analysis=gemini_analysis
            )

            # Step 7: Action Recommendations
            action_recommendation = self.action_recommendation_agent.recommend(
                decision_result=decision_result
            )

            # Step 8: Response Formatting
            formatted_payload = self.response_formatter_agent.format_response(
                booking_data=booking_data,
                behaviour_data=behaviour_data,
                behaviour_analysis=behaviour_analysis,
                bot_detection=bot_detection,
                risk_result=risk_result,
                decision_result=decision_result,
                action_recommendation=action_recommendation,
                start_time=start_time
            )

            # Step 9: Return Final AgentResponse model
            agent_response = AgentResponse(
                request_id=req_id,
                booking_id=booking_data.booking_id,
                status="SUCCESS",
                risk_assessment=risk_result,
                decision_result=decision_result,
                evaluated_at=datetime.utcnow()
            )

            # Attach formatted payload and recommendations for direct API access
            object.__setattr__(agent_response, "formatted_payload", formatted_payload)
            object.__setattr__(agent_response, "action_recommendation", action_recommendation)

            duration = round(time.time() - start_time, 4)
            logger.info(
                "AI fraud detection pipeline completed for booking %s in %.4fs: Decision=%s, Score=%.2f, GeminiUsed=%s",
                booking_data.booking_id,
                duration,
                formatted_payload.get("decision"),
                risk_result.risk_score,
                bool(gemini_analysis)
            )

            return agent_response

        except Exception as e:
            msg = f"Fraud detection pipeline failed for request {req_id}: {str(e)}"
            logger.error(msg)
            raise AIFraudDetectionServiceException(msg) from e
