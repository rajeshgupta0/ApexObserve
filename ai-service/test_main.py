import pytest
from fastapi.testclient import TestClient
from unittest.mock import patch
import pandas as pd
from main import app

client = TestClient(app)

@pytest.fixture
def mock_history_data():
    return pd.DataFrame({
        'time': pd.date_range(start='1/1/2026', periods=50, freq='h'),
        'value': [10.0] * 49 + [100.0]  # One big anomaly
    }).set_index('time')

@pytest.fixture
def empty_history_data():
    return pd.DataFrame()

@patch('main.fetch_historical_metrics')
def test_detect_anomaly_normal(mock_fetch, mock_history_data):
    mock_fetch.return_value = mock_history_data
    response = client.get("/api/ai/anomaly?serviceId=service-a&metricName=cpu&currentValue=10.0")
    assert response.status_code == 200
    data = response.json()
    assert data["service"] == "service-a"
    assert data["is_anomaly"] is False
    assert data["confidence"] > 0

@patch('main.fetch_historical_metrics')
def test_detect_anomaly_abnormal(mock_fetch, mock_history_data):
    mock_fetch.return_value = mock_history_data
    response = client.get("/api/ai/anomaly?serviceId=service-a&metricName=cpu&currentValue=100.0")
    assert response.status_code == 200
    data = response.json()
    assert data["is_anomaly"] is True

@patch('main.fetch_historical_metrics')
def test_detect_anomaly_insufficient_data(mock_fetch, empty_history_data):
    mock_fetch.return_value = empty_history_data
    response = client.get("/api/ai/anomaly?serviceId=service-a&metricName=cpu&currentValue=10.0")
    assert response.status_code == 200
    data = response.json()
    assert data["confidence"] == 0.1
    assert "Insufficient historical data" in data["evidence"]

@patch('main.fetch_historical_metrics')
def test_predict_metric_normal(mock_fetch, mock_history_data):
    mock_fetch.return_value = mock_history_data
    response = client.get("/api/ai/predict?serviceId=service-a&metricName=cpu&horizon_minutes=60")
    assert response.status_code == 200
    data = response.json()
    assert data["service"] == "service-a"
    assert data["forecast"] > 0
    assert data["confidence"] > 0

@patch('main.fetch_historical_metrics')
def test_predict_metric_insufficient_data(mock_fetch, empty_history_data):
    mock_fetch.return_value = empty_history_data
    response = client.get("/api/ai/predict?serviceId=service-a&metricName=cpu&horizon_minutes=60")
    assert response.status_code == 200
    data = response.json()
    assert data["confidence"] == 0.1
    assert "Insufficient historical data" in data["evidence"]
