import xgboost as xgb
from classifiers.Classifier import Classifier
from utils import csvUtils


class XGBooster(Classifier):
    def __init__(self, name, filename=None, num_trees=100, tree_depth=6):
        super().__init__(
            name,
            xgb.XGBClassifier(max_depth=tree_depth, n_estimators=num_trees),
            filename,
        )

    def fit(self, x, y):
        return self.model.fit(x, y)

    def predict(self, input):
        return self.model.predict(input)

    def getMatchProbability(self, input):
        return self.model.predict_proba(input)[:, 1]

    def save(self, filebasename):
        self.model.save_model("{}.json".format(filebasename))

    def load(self, filename):
        self.model.load_model(filename)

    @classmethod
    def loadData(cls, src):
        return csvUtils.loadClassificationDataFromCSV(src)
