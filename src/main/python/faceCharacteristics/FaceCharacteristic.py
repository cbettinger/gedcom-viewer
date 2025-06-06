from abc import ABC, abstractmethod

class FaceCharacteristic(ABC):

    def __init__(self, name):
        super().__init__()
        self.name = name

    @abstractmethod
    def calculateSimilarityTo(self, other):
        pass