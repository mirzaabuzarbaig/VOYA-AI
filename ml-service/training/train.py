"""
Model Training and Evaluation Script for VoyaAI Trip Cost Prediction.
Trains and compares:
1. Linear Regression (Baseline)
2. Random Forest Regressor
3. Gradient Boosting Regressor

Metrics: MAE, RMSE, R²
Persists best model pipeline using joblib.
"""

import json
import os
import joblib
import numpy as np
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import GradientBoostingRegressor, RandomForestRegressor
from sklearn.linear_model import LinearRegression
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler

def train_and_evaluate():
    # 1. Load dataset
    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    data_path = os.path.join(base_dir, 'data', 'travel_cost_dataset.csv')
    
    if not os.path.exists(data_path):
        raise FileNotFoundError(f"Dataset not found at {data_path}. Run build_dataset.py first.")
        
    df = pd.read_csv(data_path)
    print(f"Loaded dataset from {data_path} with {len(df)} records.")
    
    # 2. Features and Target
    numeric_features = ['duration_days', 'travelers', 'budget', 'month']
    categorical_features = ['destination', 'accommodation', 'transport', 'trip_type']
    target = 'total_cost'
    
    X = df[numeric_features + categorical_features]
    y = df[target]
    
    # 3. Train-test split (80% train, 20% test)
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.20, random_state=42
    )
    print(f"Train samples: {len(X_train)}, Test samples: {len(X_test)}")
    
    # 4. Preprocessing Pipeline
    preprocessor = ColumnTransformer(
        transformers=[
            ('num', StandardScaler(), numeric_features),
            ('cat', OneHotEncoder(handle_unknown='ignore', sparse_output=False), categorical_features)
        ]
    )
    
    # 5. Model Candidates
    candidates = {
        'LinearRegression': LinearRegression(),
        'RandomForestRegressor': RandomForestRegressor(n_estimators=100, max_depth=12, random_state=42),
        'GradientBoostingRegressor': GradientBoostingRegressor(n_estimators=150, learning_rate=0.08, max_depth=5, random_state=42)
    }
    
    results = {}
    best_name = None
    best_r2 = -float('inf')
    best_pipeline = None
    
    print("\n--- Model Evaluation ---")
    for name, model in candidates.items():
        pipeline = Pipeline([
            ('preprocessor', preprocessor),
            ('regressor', model)
        ])
        
        pipeline.fit(X_train, y_train)
        y_pred = pipeline.predict(X_test)
        
        mae = float(mean_absolute_error(y_test, y_pred))
        rmse = float(np.sqrt(mean_squared_error(y_test, y_pred)))
        r2 = float(r2_score(y_test, y_pred))
        
        results[name] = {
            'MAE': round(mae, 2),
            'RMSE': round(rmse, 2),
            'R2': round(r2, 4)
        }
        
        print(f"[{name}] MAE: INR {mae:,.2f} | RMSE: INR {rmse:,.2f} | R2: {r2:.4f}")
        
        if r2 > best_r2:
            best_r2 = r2
            best_name = name
            best_pipeline = pipeline
            
    print(f"\nBest Selected Model: {best_name} (R2 = {best_r2:.4f})")
    
    # 6. Model Persistence
    model_dir = os.path.join(base_dir, 'model')
    os.makedirs(model_dir, exist_ok=True)
    model_save_path = os.path.join(model_dir, 'cost_prediction_pipeline.joblib')
    metrics_save_path = os.path.join(model_dir, 'metrics.json')
    
    joblib.dump(best_pipeline, model_save_path)
    print(f"Persisted pipeline to {model_save_path}")
    
    output_metadata = {
        'dataset_size': len(df),
        'train_samples': len(X_train),
        'test_samples': len(X_test),
        'models_evaluated': results,
        'selected_model': best_name,
        'selected_model_metrics': results[best_name],
        'model_version': 'cost-model-v1',
        'numeric_features': numeric_features,
        'categorical_features': categorical_features
    }
    
    with open(metrics_save_path, 'w') as f:
        json.dump(output_metadata, f, indent=2)
    print(f"Saved metrics metadata to {metrics_save_path}")
    
    return output_metadata

if __name__ == '__main__':
    train_and_evaluate()
