from classifiers.XGBooster import XGBooster

XGB_CLASSIFIERS = {
    "CHEEKS": {"first": XGBooster("xgb_100_6_cheeks_geometry_first", "src/main/resources/xgbModels/xgb_100_6_cheeks_edgeDifference_geometry_first.json", 20, 6),
               "second": XGBooster("xgb_100_6_cheeks_geometry_second", "src/main/resources/xgbModels/xgb_100_6_cheeks_edgeDifference_geometry_second.json", 20, 6)},
    "CHIN": XGBooster("xgb_100_6_chin_geometry", "src/main/resources/xgbModels/xgb_100_6_chin_edgeDifference_geometry.json", 20, 6),
    "EYEBROWS": {"first": XGBooster("xgb_100_6_eyebrows_geometry_first", "src/main/resources/xgbModels/xgb_100_6_eyebrows_edgeDifference_geometry_first.json", 20, 6),
               "second": XGBooster("xgb_100_6_eyebrows_geometry_second", "src/main/resources/xgbModels/xgb_100_6_eyebrows_edgeDifference_geometry_second.json", 20, 6)},
    "EYESHAPE": {"first": XGBooster("xgb_100_6_eyes_geometry_first", "src/main/resources/xgbModels/xgb_100_6_eyes_edgeDifference_geometry_first.json", 20, 6),
               "second": XGBooster("xgb_100_6_eyes_geometry_second", "src/main/resources/xgbModels/xgb_100_6_eyes_edgeDifference_geometry_second.json", 20, 6)},
    "FACESHAPE": XGBooster("xgb_100_6_faceshape_geometry", "src/main/resources/xgbModels/xgb_100_6_faceshape_edgeDifference_geometry.json", 20, 6),
    "LIPS": {"first": XGBooster("xgb_100_6_lips_geometry_first", "src/main/resources/xgbModels/xgb_100_6_lips_edgeDifference_geometry_first.json", 20, 6),
               "second": XGBooster("xgb_100_6_lips_geometry_second", "src/main/resources/xgbModels/xgb_100_6_lips_edgeDifference_geometry_second.json", 20, 6)},
    "NOSE": XGBooster("xgb_100_6_nose_geometry", "src/main/resources/xgbModels/xgb_100_6_nose_edgeDifference_geometry.json", 20, 6)
}
