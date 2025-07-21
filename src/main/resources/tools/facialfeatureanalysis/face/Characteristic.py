from abc import ABC, abstractmethod


class Characteristic(ABC):

    def __init__(self, name):
        super().__init__()

        self.name = name

    @abstractmethod
    def similarity(self, other):
        pass
