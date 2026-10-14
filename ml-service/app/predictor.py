import json
import logging
import os
import joblib
import pandas as pd
from typing import Dict, Any, Optional

from app.config import MODEL_PATH, METRICS_PATH, MODEL_VERSION
from app.schemas import CostPredictionRequest

logger = logging.getLogger("ml_service.predictor")

class TripCostPredictor:
    def __init__(self):
        self.pipeline = None
        self.metadata = {}
        self.model_name = "UnknownModel"
        self._load_model()

    def _load_model(self):
        if not os.path.exists(MODEL_PATH):
            logger.error(f"Trained model artifact not found at {MODEL_PATH}")
            self.pipeline = None
            return

        try:
            self.pipeline = joblib.load(MODEL_PATH)
            logger.info(f"Loaded ML model pipeline from {MODEL_PATH}")
        except Exception as e:
            logger.error(f"Failed to load model artifact: {e}")
            self.pipeline = None
            return

        if os.path.exists(METRICS_PATH):
            try:
                with open(METRICS_PATH, 'r') as f:
                    self.metadata = json.load(f)
                    self.model_name = self.metadata.get('selected_model', 'LinearRegression')
            except Exception as e:
                logger.warning(f"Could not load metrics metadata: {e}")

    @property
    def is_loaded(self) -> bool:
        return self.pipeline is not None

    def predict(self, req: CostPredictionRequest) -> Dict[str, Any]:
        if not self.is_loaded:
            raise RuntimeError("ML model pipeline is not loaded or unavailable")

        # 1. Clean destination (e.g. "Goa, India" -> "Goa")
        raw_dest = req.destination.strip()
        dest_clean = raw_dest.split(',')[0].strip()

        # 2. Derive travel month default if omitted (default: 6)
        month = req.month if req.month is not None else 6

        # 3. Normalize accommodation and transport
        accom = req.accommodation.strip().title() if req.accommodation else "Hotel"
        trans = req.transport.strip().title() if req.transport else "Flight"

        # 4. Construct feature DataFrame matching pipeline schema
        feature_dict = {
            'duration_days': [req.durationDays],
            'travelers': [req.travelers],
            'budget': [req.budget],
            'month': [month],
            'destination': [dest_clean],
            'accommodation': [accom],
            'transport': [trans],
            'trip_type': [req.tripType]
        }

        df_input = pd.DataFrame(feature_dict)

        # 5. Execute model inference using the loaded pipeline
        raw_prediction = float(self.pipeline.predict(df_input)[0])
        
        # Floor prediction at a reasonable minimum based on duration and travelers
        min_floor = max(3000.0, req.durationDays * req.travelers * 800.0)
        final_cost = max(min_floor, raw_prediction)

        return {
            "predictedCost": round(final_cost, 2),
            "currency": req.currency,
            "model": self.model_name,
            "modelVersion": MODEL_VERSION,
            "unit": "total_trip",
            "message": "Estimated trip cost based on the trained ML model."
        }

predictor_instance = TripCostPredictor()
