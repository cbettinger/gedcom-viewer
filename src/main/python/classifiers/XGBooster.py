import xgboost as xgb
from classifiers.Classifier import Classifier
from utils import csvUtils


class XGBooster(Classifier):
    def __init__(self, name, pretrainedFilename=None, numTrees=100, treeDepth=6):
        super().__init__(name, xgb.XGBClassifier(max_depth=treeDepth, n_estimators=numTrees, random_state=12), pretrainedFilename)

    def load(self, filename):
        self.model.load_model(filename)

    def getMatchProbability(self, input):
        return self.model.predict_proba(input)[:, 1]

    def fit(self, x, y):
        return self.model.fit(x, y)

    def predict(self, input):
        return self.model.predict(input)
    
    def save(self, filenameWithoutExtension):
        self.model.save_model("{}.json".format(filenameWithoutExtension))

    @classmethod
    def loadData(cls, src):
        return csvUtils.loadClassificationDataFromCSV(src)
