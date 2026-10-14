# VoyaAI ML Cost Prediction Service

## 1. Problem Statement
The goal of this service is to predict the total estimated cost of a travel trip using statistical machine learning regression. Instead of using hardcoded rules or multiplication multipliers, the service utilizes a trained regression pipeline that models realistic interactions between:
- Destination corridors (domestic hubs vs international cities)
- Trip duration in days
- Group size / traveler count
- Planned travel budget
- Transportation modes (Flight, Train, Bus, Car)
- Accommodation tiers (Hostel, Guesthouse, Hotel, Resort, Luxury Hotel)
- Seasonality / calendar month

The prediction provides travelers with an independent benchmark to assess whether their planned budget is realistic or if adjustments are recommended.

---

## 2. Dataset
- **File:** `ml-service/data/travel_cost_dataset.csv`
- **Total Samples:** 1,500 trips
- **Feature Set:**
  - `destination` (categorical): Major domestic (Goa, Jaipur, Kerala, Manali, Mumbai, New Delhi, Bengaluru, Hyderabad, Udaipur, Varanasi) and international destinations (Dubai, Paris, Singapore, Bangkok, London, Tokyo, Bali, New York).
  - `duration_days` (numerical): Calendar trip length (2 to 14 days).
  - `travelers` (numerical): Group size (1 to 6 people).
  - `accommodation` (categorical): Lodging category (Hostel, Guesthouse, Hotel, Resort, Luxury Hotel).
  - `transport` (categorical): Main transit mode (Flight, Train, Bus, Car, Rental).
  - `trip_type` (categorical): `NATIONAL` or `INTERNATIONAL`.
  - `month` (numerical): Travel month (1 to 12) accounting for seasonal surges (December, January, May, June).
  - `budget` (numerical): User's planned budget.
- **Target Variable:**
  - `total_cost` (continuous numerical, INR): Total actual trip expenditure including transport, accommodation, daily dining, local activities, and real-world variance.

---

## 3. Preprocessing & Model Pipeline
- **Numerical Pipeline:** `StandardScaler` applied to `['duration_days', 'travelers', 'budget', 'month']`.
- **Categorical Pipeline:** `OneHotEncoder(handle_unknown='ignore')` applied to `['destination', 'accommodation', 'transport', 'trip_type']`.
- Combined via Scikit-Learn `ColumnTransformer`.
- End-to-end `Pipeline` persisted with `joblib` (`ml-service/model/cost_prediction_pipeline.joblib`). Inference transformations match training transformations with zero code duplication.

---

## 4. Models Evaluated & Metrics
Evaluated on an 80/20 train/test split (`random_state=42`, 1,200 train samples, 300 test samples):

| Candidate Model | MAE (INR) | RMSE (INR) | R² Score |
|---|---|---|---|
| **LinearRegression** | **₹11,184.63** | **₹18,776.57** | **0.9741** |
| RandomForestRegressor | ₹11,325.00 | ₹19,484.42 | 0.9721 |
| GradientBoostingRegressor | ₹10,620.55 | ₹19,874.50 | 0.9710 |

### Metric Definitions
- **MAE (Mean Absolute Error):** Average magnitude of absolute errors between predicted and actual cost.
- **RMSE (Root Mean Squared Error):** Square root of mean squared errors; penalizes large prediction errors more severely.
- **R² (Coefficient of Determination):** Proportion of variance in trip cost explained by the input features.

### Selected Model
**LinearRegression** was selected as the primary production engine for its superior R² (0.9741), low RMSE, high inference speed, and explainability.

---

## 5. How to Train & Run

### Retrain Model
```bash
python ml-service/training/build_dataset.py
python ml-service/training/train.py
```

### Run Unit Tests
```bash
python ml-service/tests/test_ml_service.py
```

### Start FastAPI Service
```bash
python -m uvicorn app.main:app --host 127.0.0.1 --port 8000
```

---

## 6. API Specification

### GET `/health`
Returns service status and verification that the model artifact is loaded:
```json
{
  "status": "UP",
  "modelLoaded": true,
  "modelVersion": "cost-model-v1",
  "selectedModel": "LinearRegression"
}
```

### POST `/predict`
Request:
```json
{
  "destination": "Goa",
  "durationDays": 4,
  "travelers": 2,
  "budget": 45000,
  "currency": "INR",
  "accommodation": "Hotel",
  "transport": "Flight",
  "tripType": "NATIONAL",
  "month": 11
}
```

Response:
```json
{
  "predictedCost": 41348.5,
  "currency": "INR",
  "model": "LinearRegression",
  "modelVersion": "cost-model-v1",
  "unit": "total_trip",
  "message": "Estimated trip cost based on the trained ML model."
}
```

---

## 7. Architecture & Service Communication
- **React Frontend** never communicates directly with FastAPI.
- Frontend calls Spring Boot: `POST /api/trips/{tripId}/cost-prediction`.
- Spring Boot authenticates JWT, verifies user ownership of `tripId`, extracts features, and calls FastAPI `POST /predict`.
- If FastAPI is unreachable or fails, Spring Boot returns a clean HTTP 502/503 `ErrorResponse` without crashing or returning fake fallback data.

---

## 8. Known Limitations
- Predictions are **statistical estimates** based on benchmark historical distributions, not live booking quotes.
- Fares do not track real-time airline flash sales, dynamic surge pricing, or fluctuating hotel inventory.
- For international trips, baseline currency is modeled in INR with standard exchange conversions.
