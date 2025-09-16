from fastapi import FastAPI
from pydantic import BaseModel
from typing import List, Tuple
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline
import joblib
import os

app = FastAPI(title="Grievance Categorization Service")

CATEGORIES = [
    "INFRASTRUCTURE", "HEALTHCARE", "EDUCATION", "TRANSPORTATION",
    "UTILITIES", "ENVIRONMENT", "SAFETY_SECURITY", "CORRUPTION",
    "CIVIL_RIGHTS", "HOUSING", "EMPLOYMENT", "OTHER"
]

MODEL_PATH = os.environ.get("MODEL_PATH", "model.joblib")


class PredictRequest(BaseModel):
    title: str
    description: str


class PredictResponse(BaseModel):
    category: str
    confidence: float


def seed_data() -> Tuple[List[str], List[str]]:
    # Minimal seed dataset; extend with real data as needed
    texts = [
        "potholes on the road near the bridge broken street light",
        "hospital staff not available poor medical service",
        "school bus route issue and teacher shortage",
        "bus stop overcrowded public transport delay",
        "no water supply electricity power outage gas leak",
        "garbage not collected pollution and waste problem",
        "police not responding theft incident safety issue",
        "bribe asked by officer corruption complaint",
        "discrimination and civil rights violation",
        "housing application pending slum rehabilitation",
        "job unemployment salary not paid",
        "miscellaneous other complaint",
    ]
    labels = [
        "INFRASTRUCTURE", "HEALTHCARE", "EDUCATION", "TRANSPORTATION",
        "UTILITIES", "ENVIRONMENT", "SAFETY_SECURITY", "CORRUPTION",
        "CIVIL_RIGHTS", "HOUSING", "EMPLOYMENT", "OTHER"
    ]
    return texts, labels


def make_pipeline() -> Pipeline:
    return Pipeline([
        ("tfidf", TfidfVectorizer(ngram_range=(1, 2), min_df=1, max_features=20000)),
        ("clf", LogisticRegression(max_iter=1000, n_jobs=None))
    ])


def load_or_train() -> Pipeline:
    if os.path.exists(MODEL_PATH):
        return joblib.load(MODEL_PATH)
    X, y = seed_data()
    pipe = make_pipeline()
    pipe.fit(X, y)
    joblib.dump(pipe, MODEL_PATH)
    return pipe


model: Pipeline = load_or_train()


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/predict", response_model=PredictResponse)
def predict(req: PredictRequest):
    text = f"{req.title} {req.description}".strip()
    proba = None
    label = model.predict([text])[0]
    # Try to compute confidence if classifier supports predict_proba
    if hasattr(model.named_steps["clf"], "predict_proba"):
        probs = model.predict_proba([text])[0]
        confidence = float(max(probs))
    else:
        # fall back to decision function transformed via softmax-like scaling
        try:
            import numpy as np
            scores = model.decision_function([text])[0]
            if scores.ndim == 0:
                confidence = 0.5
            else:
                # scale to a probability-like score
                exp = np.exp(scores - np.max(scores))
                confidence = float(np.max(exp / np.sum(exp)))
        except Exception:
            confidence = 0.7
    if label not in CATEGORIES:
        label = "OTHER"
    return PredictResponse(category=label, confidence=confidence)


