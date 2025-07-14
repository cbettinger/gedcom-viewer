from classifiers.XGBooster import XGBooster

XGB_CLASSIFIERS = {
    "CHEEKS": {
        "first": XGBooster(
            "xgb_20_6_cheeks_first",
            "classifiers/xgboost/xgb_20_6_cheeks_edgeDifference_first.json",
            20,
            6,
        ),
        "second": XGBooster(
            "xgb_20_6_cheeks_second",
            "classifiers/xgboost/xgb_20_6_cheeks_edgeDifference_second.json",
            20,
            6,
        ),
    },
    "CHIN": XGBooster(
        "xgb_20_6_chin",
        "classifiers/xgboost/xgb_20_6_chin_edgeDifference.json",
        20,
        6,
    ),
    "EYEBROWS": {
        "first": XGBooster(
            "xgb_20_6_eyebrows_first",
            "classifiers/xgboost/xgb_20_6_eyebrows_edgeDifference_first.json",
            20,
            6,
        ),
        "second": XGBooster(
            "xgb_20_6_eyebrows_second",
            "classifiers/xgboost/xgb_20_6_eyebrows_edgeDifference_second.json",
            20,
            6,
        ),
    },
    "EYES": {
        "first": XGBooster(
            "xgb_20_6_eyes_first",
            "classifiers/xgboost/xgb_20_6_eyes_edgeDifference_first.json",
            20,
            6,
        ),
        "second": XGBooster(
            "xgb_20_6_eyes_second",
            "classifiers/xgboost/xgb_20_6_eyes_edgeDifference_second.json",
            20,
            6,
        ),
    },
    "FACESHAPE": XGBooster(
        "xgb_20_6_faceshape",
        "classifiers/xgboost/xgb_20_6_faceshape_edgeDifference.json",
        20,
        6,
    ),
    "LIPS": {
        "first": XGBooster(
            "xgb_20_6_lips_first",
            "classifiers/xgboost/xgb_20_6_lips_edgeDifference_first.json",
            20,
            6,
        ),
        "second": XGBooster(
            "xgb_20_6_lips_second",
            "classifiers/xgboost/xgb_20_6_lips_edgeDifference_second.json",
            20,
            6,
        ),
    },
    "NOSE": XGBooster(
        "xgb_20_6_nose",
        "classifiers/xgboost/xgb_20_6_nose_edgeDifference.json",
        20,
        6,
    ),
}
