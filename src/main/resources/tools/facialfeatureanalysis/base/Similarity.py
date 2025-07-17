class Similarity:
    def __init__(self, image1, image2, value):
        self.image1 = image1
        self.image2 = image2
        self.value = value

    def __str__(self):
        return str(self.value)

    def __repr__(self):
        return self.__str__()
