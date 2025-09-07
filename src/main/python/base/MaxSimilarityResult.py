class MaxSimilarityResult:
    def __init__(self, value, srcImg1, srcImg2, bbox1, bbox2):
        self.value = value
        self.img1 = srcImg1
        self.img2 = srcImg2

        [x1, y1], [x2, y2] = bbox1
        self.bbox1 = [[str(x1), str(y1)], [str(x2), str(y2)]]

        [x1, y1], [x2, y2] = bbox2
        self.bbox2 = [[str(x1), str(y1)], [str(x2), str(y2)]]

    def __str__(self):
        return str(self.value)
    
    def __repr__(self):
        return self.__str__()