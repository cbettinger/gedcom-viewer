from base.Similarity import Similarity
from base.config import MAX_COMPARISON_DEPTH, FACE_CHARACTERISTICS_OF_INTEREST
from utils import dictUtils


class FaceAnalyser:

    @classmethod
    def analyse(cls, targetPerson, maxDepth=MAX_COMPARISON_DEPTH):
        if (
            not targetPerson.hasFaces()
            or not cls._isComparable(targetPerson.parent1)
            or not cls._isComparable(targetPerson.parent2)
        ):
            return None

        depth = 0
        similarities = {}
        individualsToCheck = [targetPerson.parent1, targetPerson.parent2]

        while depth < maxDepth:
            depth += 1
            nextIndividualsToCheck = []

            for i in range(len(individualsToCheck)):
                p = individualsToCheck[i]
                sMax, sAvg = cls._getSimilaritiesToIndividual(targetPerson, p)
                similarities.update({p.value: {"max": sMax, "avg": sAvg}})

                if p.parent1:
                    nextIndividualsToCheck.append(p.parent1)
                if p.parent2:
                    nextIndividualsToCheck.append(p.parent2)

            individualsToCheck = nextIndividualsToCheck

        return similarities

    @classmethod
    def _isComparable(cls, other):
        return other is not None and other.hasFaces()

    @classmethod
    def _getSimilaritiesToIndividual(cls, targetPerson, other):
        maxSimilarities = dictUtils.getZeros(FACE_CHARACTERISTICS_OF_INTEREST)
        mostSimilarFaces = {}
        maxResult = {}
        comparedPairs = []

        avgSimilarities = dictUtils.getZeros(FACE_CHARACTERISTICS_OF_INTEREST)
        avgResult = {}

        avgMaxSimilarities = dictUtils.getZeros(FACE_CHARACTERISTICS_OF_INTEREST)
        avgMaxResult = {}

        if cls._isComparable(other):
            for ownFace in targetPerson.faces:
                maxSims = dictUtils.getZeros(FACE_CHARACTERISTICS_OF_INTEREST)
                for otherFace in other.faces:
                    if ownFace is otherFace or {ownFace, otherFace} in comparedPairs:
                        continue
                    similarities = ownFace.getSimilaritiesTo(otherFace)
                    for c in ownFace.characteristics.keys():
                        s = similarities.get(c)
                        avgSimilarities[c] += s
                        if s > maxSimilarities.get(c):
                            maxSimilarities.update({c: s})
                            mostSimilarFaces.update({c: [ownFace, otherFace]})
                        if s > maxSims[c]:
                            maxSims[c] = s
                    comparedPairs.append({ownFace, otherFace})
                for c in avgMaxSimilarities.keys():
                    avgMaxSimilarities[c] += maxSims[c]

        for c, s in maxSimilarities.items():
            faces = mostSimilarFaces.get(c)
            if faces is None:
                maxResult.update({c: Similarity(None, None, None)})
                avgResult.update({c: None})
                avgMaxResult.update({c: Similarity(None, None, None)})
            else:
                f1, f2 = faces
                maxResult.update({c: Similarity(s, f1.image, f2.image)})
                avgResult.update({c: avgSimilarities[c] / len(comparedPairs)})
                avgMaxResult.update(
                    {
                        c: Similarity(
                            avgMaxSimilarities[c] / len(targetPerson.faces),
                            f1.image,
                            f2.image,
                        )
                    }
                )

        return maxResult, avgResult
