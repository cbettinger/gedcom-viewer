from classifiers.XGBooster import XGBooster

XGB_CLASSIFIERS = {
    'cheeks': {'first': XGBooster('xgb_20_6_cheeks_first', 'models/xgbs_20_6_cheeks_edgeDifference_first.json', 20, 6),
               'second': XGBooster('xgb_20_6_cheeks_second', 'models/xgbs_20_6_cheeks_edgeDifference_second.json', 20, 6)},
    'chin': XGBooster('xgb_20_6_chin', 'models/xgbs_20_6_chin_edgeDifference.json', 20, 6),
    'eyebrows': {'first': XGBooster('xgb_20_6_eyebrows_first', 'models/xgbs_20_6_eyebrows_edgeDifference_first.json', 20, 6),
               'second': XGBooster('xgb_20_6_eyebrows_second', 'models/xgbs_20_6_eyebrows_edgeDifference_second.json', 20, 6)},
    'eyes': {'first': XGBooster('xgb_20_6_eyes_first', 'models/xgbs_20_6_eyes_edgeDifference_first.json', 20, 6),
               'second': XGBooster('xgb_20_6_eyes_second', 'models/xgbs_20_6_eyes_edgeDifference_second.json', 20, 6)},
    'faceshape': XGBooster('xgb_20_6_faceshape', 'models/xgbs_20_6_faceshape_edgeDifference.json', 20, 6),
    'lips': {'first': XGBooster('xgb_20_6_lips_first', 'models/xgbs_20_6_lips_edgeDifference_first.json', 20, 6),
               'second': XGBooster('xgb_20_6_lips_second', 'models/xgbs_20_6_lips_edgeDifference_second.json', 20, 6)},
    'nose': XGBooster('xgb_20_6_nose', 'models/xgbs_20_6_nose_edgeDifference.json', 20, 6)
}