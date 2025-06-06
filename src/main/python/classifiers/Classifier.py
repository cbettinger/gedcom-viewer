from abc import ABC, abstractmethod

class Classifier(ABC):
    def __init__(self, name, model, pretrainedFilename=None):
        super().__init__()
        self.name = name
        self.model = model

        if pretrainedFilename:
            self.load(pretrainedFilename)

    @abstractmethod
    def fit(self, x, y):
        pass

    @abstractmethod
    def predict(self, input):
        pass

    @abstractmethod
    def getMatchProbability(self, input):
        pass

    @abstractmethod
    def save(self, filename):
        pass

    @abstractmethod
    def load(self, filename):
        pass

    @classmethod
    @abstractmethod
    def loadData(cls, src):
        pass