import logging
from fastapi import FastAPI, HTTPException, status
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse

from app.config import MODEL_VERSION
from app.predictor import predictor_instance
from app.schemas import CostPredictionRequest, CostPredictionResponse, HealthResponse

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)
logger = logging.getLogger("ml_service")

from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(
    title="VoyaAI Trip Cost Prediction Service",
    description="Dedicated Machine Learning service predicting total trip costs using trained regression pipelines.",
    version=MODEL_VERSION
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

from fastapi.encoders import jsonable_encoder

@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request, exc):
    logger.warning(f"Validation error on {request.url.path}: {exc.errors()}")
    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        content=jsonable_encoder({
            "error": "Validation Error",
            "message": "Invalid trip parameters supplied for ML prediction.",
            "details": exc.errors()
        })
    )

@app.get("/", tags=["Info"])
def root():
    return {
        "service": "VoyaAI ML Cost Prediction Service",
        "version": MODEL_VERSION,
        "status": "UP" if predictor_instance.is_loaded else "DEGRADED"
    }

@app.get("/health", response_model=HealthResponse, tags=["Health"])
def health():
    is_loaded = predictor_instance.is_loaded
    if not is_loaded:
        logger.warning("Health check failed: ML model artifact is not loaded.")
        return JSONResponse(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            content={
                "status": "DOWN",
                "modelLoaded": False,
                "modelVersion": MODEL_VERSION,
                "selectedModel": None
            }
        )
    return HealthResponse(
        status="UP",
        modelLoaded=True,
        modelVersion=MODEL_VERSION,
        selectedModel=predictor_instance.model_name
    )

@app.post("/predict", response_model=CostPredictionResponse, tags=["Prediction"])
def predict_cost(request: CostPredictionRequest):
    logger.info(f"Received ML cost prediction request for destination='{request.destination}', duration={request.durationDays}d, travelers={request.travelers}, type={request.tripType}")
    
    if not predictor_instance.is_loaded:
        logger.error("Prediction request rejected: ML model is not loaded.")
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="ML cost prediction model is currently unavailable on the server."
        )

    try:
        result = predictor_instance.predict(request)
        logger.info(f"ML prediction computed: INR {result['predictedCost']} via {result['model']}")
        return CostPredictionResponse(**result)
    except Exception as e:
        logger.error(f"Inference error during cost prediction: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to generate ML cost prediction: {str(e)}"
        )

if __name__ == "__main__":
    import uvicorn
    from app.config import HOST, PORT
    uvicorn.run("app.main:app", host=HOST, port=PORT, reload=False)
