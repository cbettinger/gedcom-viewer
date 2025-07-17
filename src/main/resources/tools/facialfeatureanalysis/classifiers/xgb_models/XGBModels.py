from classifiers.XGBClassifier import XGBClassifier

XGB_MODELS = {
    "CHEEKS": {
        "first": XGBClassifier(
            "xgb_20_6_cheeks_first",
            "classifiers/xgb_models/xgb_20_6_cheeks_edgeDifference_first.json",
            20,
            6,
        ),
        "second": XGBClassifier(
            "xgb_20_6_cheeks_second",
            "classifiers/xgb_models/xgb_20_6_cheeks_edgeDifference_second.json",
            20,
            6,
        ),
    },
    "CHIN": XGBClassifier(
        "xgb_20_6_chin",
        "classifiers/xgb_models/xgb_20_6_chin_edgeDifference.json",
        20,
        6,
    ),
    "EYEBROWS": {
        "first": XGBClassifier(
            "xgb_20_6_eyebrows_first",
            "classifiers/xgb_models/xgb_20_6_eyebrows_edgeDifference_first.json",
            20,
            6,
        ),
        "second": XGBClassifier(
            "xgb_20_6_eyebrows_second",
            "classifiers/xgb_models/xgb_20_6_eyebrows_edgeDifference_second.json",
            20,
            6,
        ),
    },
    "EYESHAPE": {
        "first": XGBClassifier(
            "xgb_20_6_eyeshape_first",
            "classifiers/xgb_models/xgb_20_6_eyeshape_edgeDifference_first.json",
            20,
            6,
        ),
        "second": XGBClassifier(
            "xgb_20_6_eyeshape_second",
            "classifiers/xgb_models/xgb_20_6_eyeshape_edgeDifference_second.json",
            20,
            6,
        ),
    },
    "FACESHAPE": XGBClassifier(
        "xgb_20_6_faceshape",
        "classifiers/xgb_models/xgb_20_6_faceshape_edgeDifference.json",
        20,
        6,
    ),
    "LIPS": {
        "first": XGBClassifier(
            "xgb_20_6_lips_first",
            "classifiers/xgb_models/xgb_20_6_lips_edgeDifference_first.json",
            20,
            6,
        ),
        "second": XGBClassifier(
            "xgb_20_6_lips_second",
            "classifiers/xgb_models/xgb_20_6_lips_edgeDifference_second.json",
            20,
            6,
        ),
    },
    "NOSE": XGBClassifier(
        "xgb_20_6_nose",
        "classifiers/xgb_models/xgb_20_6_nose_edgeDifference.json",
        20,
        6,
    ),
}
