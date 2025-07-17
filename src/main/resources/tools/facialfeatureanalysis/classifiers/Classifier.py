from abc import ABC, abstractmethod


class Classifier(ABC):
    def __init__(self, name, model, filepath=None):
        super().__init__()

        self.name = name
        self.model = model

        if filepath:
            self.load(filepath)

    @abstractmethod
    def fit(self, x, y):
        pass

    @abstractmethod
    def predict(self, input):
        pass

    @abstractmethod
    def get_match_probability(self, input):
        pass

    @abstractmethod
    def save(self, filebasename):
        pass

    @abstractmethod
    def load(self, filepath):
        pass

    @classmethod
    @abstractmethod
    def load_data(cls, filepath):
        pass
