"""
Gemini Configuration module.

This module loads environment variables via python-dotenv, validates the Gemini API key,
and initializes the Google Gemini LLM client configuration for the project.
"""

import logging
import os
from typing import Optional
from dotenv import load_dotenv

import google.generativeai as genai

# Configure module logger
logger = logging.getLogger(__name__)


class GeminiConfigException(Exception):
    """Custom exception raised when Gemini configuration or environment variable loading fails."""
    pass


class GeminiConfig:
    """
    Configuration manager for Google Gemini API and model initialization.
    """

    def __init__(self, env_path: Optional[str] = None) -> None:
        """
        Loads environment variables and configures the Gemini API client.

        Args:
            env_path: Optional custom path to .env file.

        Raises:
            GeminiConfigException: If API key is missing or configuration fails.
        """
        # Load environment variables from .env file
        if env_path:
            load_dotenv(dotenv_path=env_path)
        else:
            load_dotenv()

        # Retrieve Gemini API Key (checking GEMINI_API_KEY first, falling back to GOOGLE_API_KEY)
        self.api_key: Optional[str] = os.getenv("GEMINI_API_KEY") or os.getenv("GOOGLE_API_KEY")

        if not self.api_key or not self.api_key.strip():
            error_msg = (
                "Gemini API key is missing! Please set 'GEMINI_API_KEY' (or 'GOOGLE_API_KEY') "
                "in your .env file before running the application."
            )
            logger.error(error_msg)
            raise GeminiConfigException(error_msg)

        # Retrieve Model Name (defaulting to gemini-2.5-flash)
        self.model_name: str = os.getenv("MODEL_NAME", "gemini-2.5-flash").strip()

        # Configure Google Generative AI SDK
        try:
            genai.configure(api_key=self.api_key)
            logger.info("Google Gemini SDK configured successfully with model: %s", self.model_name)
        except Exception as e:
            error_msg = f"Failed to configure Google Gemini SDK: {str(e)}"
            logger.error(error_msg)
            raise GeminiConfigException(error_msg) from e

    def get_generative_model(self, model_override: Optional[str] = None) -> genai.GenerativeModel:
        """
        Instantiates and returns a Google Gemini GenerativeModel instance.

        Args:
            model_override: Optional specific model name override.

        Returns:
            genai.GenerativeModel object configured for inference.
        """
        target_model = model_override or self.model_name
        logger.info("Initializing GenerativeModel: %s", target_model)
        return genai.GenerativeModel(model_name=target_model)


# Singleton instance container
_config_instance: Optional[GeminiConfig] = None


def get_gemini_config() -> GeminiConfig:
    """
    Returns a global GeminiConfig instance, initializing it if necessary.

    Returns:
        GeminiConfig instance.
    """
    global _config_instance
    if _config_instance is None:
        _config_instance = GeminiConfig()
    return _config_instance
