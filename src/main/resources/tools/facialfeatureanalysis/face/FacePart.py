from abc import ABC, abstractmethod


class FacePart(ABC):

    def __init__(self, name):
        super().__init__()

        self.name = name

    @abstractmethod
    def similarity(self, other):
        pass
