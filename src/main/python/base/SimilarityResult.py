class SimilarityResult:
    def __init__(self, value, srcImg1, srcImg2):
        self.value = value
        self.img1 = srcImg1
        self.img2 = srcImg2

    def __str__(self):
        return str(self.value)
    
    def __repr__(self):
        return self.__str__()