"""
Unit and Integration Tests for Python FastAPI ML Cost Prediction Service.
Tests:
- GET /health
- POST /predict with valid National and International payloads
- Validation errors (duration=0, travelers=0, missing destination, month out of bounds)
- Model sanity checks (different inputs yield different non-budget estimates)
"""

import sys
import os
import unittest

# Add ml-service to sys.path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from fastapi.testclient import TestClient
from app.main import app
from app.predictor import predictor_instance

class TestMLService(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.client = TestClient(app)

    def test_01_health_endpoint(self):
        response = self.client.get("/health")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data["status"], "UP")
        self.assertTrue(data["modelLoaded"])
        self.assertEqual(data["modelVersion"], "cost-model-v1")
        self.assertIsNotNone(data["selectedModel"])
        print("\n[TEST PASS] GET /health returned 200 with modelLoaded=True")

    def test_02_valid_national_prediction(self):
        payload = {
            "destination": "Goa",
            "durationDays": 4,
            "travelers": 2,
            "budget": 45000.0,
            "currency": "INR",
            "accommodation": "Hotel",
            "transport": "Flight",
            "tripType": "NATIONAL",
            "month": 11
        }
        response = self.client.post("/predict", json=payload)
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertIn("predictedCost", data)
        self.assertIsInstance(data["predictedCost"], (int, float))
        self.assertGreater(data["predictedCost"], 0)
        self.assertEqual(data["currency"], "INR")
        # Ensure prediction is not just returning budget
        self.assertNotEqual(data["predictedCost"], payload["budget"])
        print(f"[TEST PASS] Valid National prediction: INR {data['predictedCost']:,} (Budget was INR {payload['budget']:,})")

    def test_03_valid_international_prediction(self):
        payload = {
            "destination": "Paris",
            "durationDays": 5,
            "travelers": 2,
            "budget": 200000.0,
            "currency": "INR",
            "accommodation": "Hotel",
            "transport": "Flight",
            "tripType": "INTERNATIONAL",
            "month": 5
        }
        response = self.client.post("/predict", json=payload)
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertIn("predictedCost", data)
        self.assertGreater(data["predictedCost"], 0)
        print(f"[TEST PASS] Valid International prediction: INR {data['predictedCost']:,}")

    def test_04_invalid_duration_rejected(self):
        payload = {
            "destination": "Goa",
            "durationDays": 0,  # Invalid
            "travelers": 2,
            "budget": 30000.0
        }
        response = self.client.post("/predict", json=payload)
        self.assertEqual(response.status_code, 422)
        print("[TEST PASS] durationDays=0 rejected with HTTP 422")

    def test_05_invalid_travelers_rejected(self):
        payload = {
            "destination": "Goa",
            "durationDays": 3,
            "travelers": 0,  # Invalid
            "budget": 30000.0
        }
        response = self.client.post("/predict", json=payload)
        self.assertEqual(response.status_code, 422)
        print("[TEST PASS] travelers=0 rejected with HTTP 422")

    def test_06_blank_destination_rejected(self):
        payload = {
            "destination": "   ",  # Blank
            "durationDays": 3,
            "travelers": 1,
            "budget": 20000.0
        }
        response = self.client.post("/predict", json=payload)
        self.assertEqual(response.status_code, 422)
        print("[TEST PASS] Blank destination rejected with HTTP 422")

    def test_07_invalid_month_rejected(self):
        payload = {
            "destination": "Goa",
            "durationDays": 3,
            "travelers": 1,
            "month": 13  # Invalid month
        }
        response = self.client.post("/predict", json=payload)
        self.assertEqual(response.status_code, 422)
        print("[TEST PASS] month=13 rejected with HTTP 422")

    def test_08_sanity_checks_different_predictions(self):
        # 2 days vs 7 days
        res_2d = self.client.post("/predict", json={
            "destination": "Jaipur", "durationDays": 2, "travelers": 2, "budget": 20000.0
        }).json()["predictedCost"]

        res_7d = self.client.post("/predict", json={
            "destination": "Jaipur", "durationDays": 7, "travelers": 2, "budget": 20000.0
        }).json()["predictedCost"]

        self.assertGreater(res_7d, res_2d, "Longer trip should predict higher cost than short trip")
        print(f"[TEST PASS] Duration sanity check: 2 days (INR {res_2d:,}) < 7 days (INR {res_7d:,})")

        # 1 traveler vs 4 travelers
        res_1t = self.client.post("/predict", json={
            "destination": "Goa", "durationDays": 4, "travelers": 1, "budget": 30000.0
        }).json()["predictedCost"]

        res_4t = self.client.post("/predict", json={
            "destination": "Goa", "durationDays": 4, "travelers": 4, "budget": 30000.0
        }).json()["predictedCost"]

        self.assertGreater(res_4t, res_1t, "More travelers should predict higher total cost")
        print(f"[TEST PASS] Travelers sanity check: 1 traveler (INR {res_1t:,}) < 4 travelers (INR {res_4t:,})")

if __name__ == "__main__":
    unittest.main()
