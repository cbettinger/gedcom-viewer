class Similarity:
    def __init__(self, value, image1, image2):
        self.value = value
        self.image1 = image1
        self.image2 = image2

    def __str__(self):
        return str(self.value)

    def __repr__(self):
        return self.__str__()
