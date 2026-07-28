"""
Gemini Prompt Builder module.

This module provides utility functions to construct structured prompts for
the Google Gemini LLM during fraud evaluation.
"""

import json
from typing import Any, Dict

from model.booking_data import BookingData
from model.risk_result import RiskResult


class GeminiPromptBuilder:
    """
    Utility class for constructing prompts sent to the Google Gemini API.
    """

    @staticmethod
    def build_fraud_evaluation_prompt(
        booking_data: BookingData,
        behaviour_analysis: Dict[str, Any],
        bot_detection: Dict[str, Any],
        risk_result: RiskResult
    ) -> str:
        """
        Builds a structured evaluation prompt asking Gemini for decision, reasoning, and risk analysis.

        Args:
            booking_data: Validated BookingData object.
            behaviour_analysis: Output dictionary from BehaviourAnalyzerAgent.
            bot_detection: Output dictionary from BotDetectionAgent.
            risk_result: Output RiskResult from RiskScoreAgent.

        Returns:
            Formatted prompt string for Gemini LLM.
        """
        booking_dict = booking_data.model_dump() if hasattr(booking_data, "model_dump") else booking_data.dict()

        prompt = f"""
You are an expert AI Fraud Analyst specializing in event ticket booking fraud detection.
Analyze the following ticket transaction telemetry and automated multi-agent risk signals:

--- TRANSACTION DETAILS ---
{json.dumps(booking_dict, indent=2, default=str)}

--- BEHAVIORAL ANALYSIS ---
{json.dumps(behaviour_analysis, indent=2, default=str)}

--- BOT DETECTION RESULTS ---
{json.dumps(bot_detection, indent=2, default=str)}

--- CALCULATED RISK SCORE ---
Numeric Risk Score: {risk_result.risk_score}/100
Risk Level: {risk_result.risk_level.value}
Is Bot Flag: {risk_result.is_bot}
Risk Factors: {json.dumps(risk_result.risk_factors, indent=2)}

--- INSTRUCTIONS ---
Perform an expert synthesis of these signals. Determine if this transaction is legitimate or fraudulent.

You MUST respond strictly in valid JSON format with the following keys:
{{
  "decision": "ALLOW" | "REVIEW" | "BLOCK",
  "reasoning": "<Detailed 2-3 sentence technical justification of the decision>",
  "confidence": <float between 0.0 and 100.0>,
  "risk_assessment": "<Brief summary of main risk factors identified>"
}}

Respond ONLY with the raw JSON object. Do not include markdown code block formatting or extra commentary.
"""
        return prompt.strip()
