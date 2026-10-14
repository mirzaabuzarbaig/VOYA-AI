import os

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MODEL_PATH = os.path.join(BASE_DIR, 'model', 'cost_prediction_pipeline.joblib')
METRICS_PATH = os.path.join(BASE_DIR, 'model', 'metrics.json')

HOST = os.getenv("ML_HOST", "0.0.0.0")
PORT = int(os.getenv("ML_PORT", "8000"))
MODEL_VERSION = "cost-model-v1"
