from typing import Optional
from pydantic import BaseModel, Field, field_validator

class CostPredictionRequest(BaseModel):
    destination: str = Field(..., min_length=1, description="Destination city/region (e.g., Goa, Paris)")
    durationDays: int = Field(..., gt=0, description="Trip duration in calendar days (must be > 0)")
    travelers: int = Field(..., gt=0, description="Number of travelers (must be > 0)")
    budget: float = Field(default=0.0, ge=0.0, description="Planned trip budget (cannot be negative)")
    currency: str = Field(default="INR", description="Currency code (e.g., INR, USD, EUR)")
    accommodation: str = Field(default="Hotel", description="Accommodation tier (e.g., Hotel, Resort, Hostel, Luxury Hotel)")
    transport: str = Field(default="Flight", description="Transport mode (e.g., Flight, Train, Bus, Car)")
    tripType: str = Field(default="NATIONAL", description="Trip category: NATIONAL or INTERNATIONAL")
    month: Optional[int] = Field(default=None, ge=1, le=12, description="Travel month (1 to 12)")

    @field_validator('destination')
    @classmethod
    def validate_destination_not_blank(cls, v: str) -> str:
        if not v or not v.strip():
            raise ValueError("Destination cannot be empty or whitespace only")
        return v.strip()

    @field_validator('tripType')
    @classmethod
    def normalize_trip_type(cls, v: str) -> str:
        norm = v.strip().upper()
        if norm in ['DOMESTIC', 'NATIONAL']:
            return 'NATIONAL'
        if norm == 'INTERNATIONAL':
            return 'INTERNATIONAL'
        return 'NATIONAL'

class CostPredictionResponse(BaseModel):
    predictedCost: float = Field(..., description="Estimated total trip cost derived from ML model")
    currency: str = Field(default="INR", description="Currency of prediction")
    model: str = Field(..., description="Name of the trained regressor")
    modelVersion: str = Field(..., description="Model artifact version")
    unit: str = Field(default="total_trip", description="Scope of cost prediction")
    message: str = Field(default="Estimated trip cost based on the trained ML model.")

class HealthResponse(BaseModel):
    status: str
    modelLoaded: bool
    modelVersion: Optional[str] = None
    selectedModel: Optional[str] = None
