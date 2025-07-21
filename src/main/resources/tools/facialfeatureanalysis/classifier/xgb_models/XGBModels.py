from classifier.XGBClassifier import XGBClassifier

XGB_MODELS = {
    "CHEEKS": {
        "first": XGBClassifier(
            "xgb_20_6_cheeks_first",
            "classifier/xgb_models/xgb_20_6_cheeks_edgeDifference_first.json",
            20,
            6,
        ),
        "second": XGBClassifier(
            "xgb_20_6_cheeks_second",
            "classifier/xgb_models/xgb_20_6_cheeks_edgeDifference_second.json",
            20,
            6,
        ),
    },
    "CHIN": XGBClassifier(
        "xgb_20_6_chin",
        "classifier/xgb_models/xgb_20_6_chin_edgeDifference.json",
        20,
        6,
    ),
    "EYEBROWS": {
        "first": XGBClassifier(
            "xgb_20_6_eyebrows_first",
            "classifier/xgb_models/xgb_20_6_eyebrows_edgeDifference_first.json",
            20,
            6,
        ),
        "second": XGBClassifier(
            "xgb_20_6_eyebrows_second",
            "classifier/xgb_models/xgb_20_6_eyebrows_edgeDifference_second.json",
            20,
            6,
        ),
    },
    "EYESHAPE": {
        "first": XGBClassifier(
            "xgb_20_6_eyeshape_first",
            "classifier/xgb_models/xgb_20_6_eyeshape_edgeDifference_first.json",
            20,
            6,
        ),
        "second": XGBClassifier(
            "xgb_20_6_eyeshape_second",
            "classifier/xgb_models/xgb_20_6_eyeshape_edgeDifference_second.json",
            20,
            6,
        ),
    },
    "FACESHAPE": XGBClassifier(
        "xgb_20_6_faceshape",
        "classifier/xgb_models/xgb_20_6_faceshape_edgeDifference.json",
        20,
        6,
    ),
    "LIPS": {
        "first": XGBClassifier(
            "xgb_20_6_lips_first",
            "classifier/xgb_models/xgb_20_6_lips_edgeDifference_first.json",
            20,
            6,
        ),
        "second": XGBClassifier(
            "xgb_20_6_lips_second",
            "classifier/xgb_models/xgb_20_6_lips_edgeDifference_second.json",
            20,
            6,
        ),
    },
    "NOSE": XGBClassifier(
        "xgb_20_6_nose",
        "classifier/xgb_models/xgb_20_6_nose_edgeDifference.json",
        20,
        6,
    ),
}
