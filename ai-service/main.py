import logging
from fastapi import FastAPI, HTTPException, Header
from pydantic import BaseModel
from typing import List, Optional
import httpx
import numpy as np
import pandas as pd
from sklearn.ensemble import IsolationForest
from sklearn.linear_model import LinearRegression

app = FastAPI(title="ApexObserve AI Service")
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

import os
QUERY_SERVICE_URL = os.getenv("QUERY_SERVICE_URL", "http://localhost:8083")

class DetectionResponse(BaseModel):
    service: str
    metric: str
    window: str
    observed_value: float
    expected_baseline: float
    anomaly_score: float
    confidence: float
    evidence: str
    is_anomaly: bool

class PredictionResponse(BaseModel):
    service: str
    metric: str
    forecast: float
    forecast_window: str
    confidence: float
    evidence: str

async def fetch_historical_metrics(tenant_id: str, service_id: str, metric_name: str, hours: int = 24) -> pd.DataFrame:
    # We call a yet-to-be-created API in query-service to get historical metrics for a specific service/metric
    # For now, we simulate the fetch if the endpoint doesn't exist, but we must use real data if possible
    # We'll use a try/except to call query-service. If it fails, we return an empty dataframe
    try:
        async with httpx.AsyncClient() as client:
            res = await client.get(
                f"{QUERY_SERVICE_URL}/api/metrics/history",
                headers={"X-Tenant-ID": tenant_id},
                params={"serviceId": service_id, "metricName": metric_name, "hours": hours}
            )
            res.raise_for_status()
            data = res.json()
            if not data:
                return pd.DataFrame()
            
            df = pd.DataFrame(data)
            df['time'] = pd.to_datetime(df['time'])
            df.set_index('time', inplace=True)
            df.sort_index(inplace=True)
            return df
    except Exception as e:
        logger.error(f"Failed to fetch historical metrics: {e}")
        return pd.DataFrame()

@app.get("/api/ai/anomaly")
async def detect_anomaly(
    serviceId: str, 
    metricName: str, 
    currentValue: float,
    x_tenant_id: str = Header(default="default")
) -> DetectionResponse:
    
    df = await fetch_historical_metrics(x_tenant_id, serviceId, metricName, hours=24)
    
    if df.empty or len(df) < 10:
        # Insufficient data
        return DetectionResponse(
            service=serviceId,
            metric=metricName,
            window="24h",
            observed_value=currentValue,
            expected_baseline=currentValue,
            anomaly_score=0.0,
            confidence=0.1,
            evidence="Insufficient historical data for anomaly detection.",
            is_anomaly=False
        )

    # Use Isolation Forest for anomaly detection
    values = df['value'].values.reshape(-1, 1)
    model = IsolationForest(contamination=0.05, random_state=42)
    model.fit(values)
    
    score = model.decision_function([[currentValue]])[0]
    is_anomaly = model.predict([[currentValue]])[0] == -1
    
    baseline = float(np.mean(values))
    
    # decision_function returns positive for normal, negative for anomaly.
    # Convert to a 0-1 anomaly score
    normalized_score = float(1.0 / (1.0 + np.exp(score))) 
    
    return DetectionResponse(
        service=serviceId,
        metric=metricName,
        window="24h",
        observed_value=currentValue,
        expected_baseline=baseline,
        anomaly_score=normalized_score,
        confidence=0.85 if len(df) > 50 else 0.5,
        evidence=f"Based on {len(df)} historical data points, baseline is {baseline:.2f}.",
        is_anomaly=bool(is_anomaly)
    )

@app.get("/api/ai/predict")
async def predict_metric(
    serviceId: str,
    metricName: str,
    horizon_minutes: int = 60,
    x_tenant_id: str = Header(default="default")
) -> PredictionResponse:

    df = await fetch_historical_metrics(x_tenant_id, serviceId, metricName, hours=72)
    
    if df.empty or len(df) < 10:
        return PredictionResponse(
            service=serviceId,
            metric=metricName,
            forecast=0.0,
            forecast_window=f"{horizon_minutes}m",
            confidence=0.1,
            evidence="Insufficient historical data to make a prediction."
        )

    # Prepare data for simple Linear Regression
    df['timestamp_numeric'] = df.index.astype(np.int64) // 10**9
    X = df[['timestamp_numeric']].values
    y = df['value'].values
    
    model = LinearRegression()
    model.fit(X, y)
    
    # Predict future value
    future_time = pd.Timestamp.now('UTC') + pd.Timedelta(minutes=horizon_minutes)
    future_ts = future_time.timestamp()
    
    forecast = model.predict([[future_ts]])[0]
    
    return PredictionResponse(
        service=serviceId,
        metric=metricName,
        forecast=float(forecast),
        forecast_window=f"{horizon_minutes}m",
        confidence=0.75,
        evidence=f"Linear regression forecast based on {len(df)} data points."
    )

@app.get("/health")
def health_check():
    return {"status": "up"}
