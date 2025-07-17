from base import dictUtils
from base.config import FACIAL_FEATURES
from base.Similarity import Similarity


class FaceAnalyser:
    @classmethod
    def analyse(cls, proband, maxDepth):
        if (
            not proband.hasFaces()
            or not cls._isComparable(proband.father)
            or not cls._isComparable(proband.mother)
        ):
            return None

        depth = 0
        similarities = {}
        individualsToCheck = [proband.father, proband.mother]

        while depth < maxDepth:
            depth += 1
            nextIndividualsToCheck = []

            for i in range(len(individualsToCheck)):
                p = individualsToCheck[i]
                sMax, sAvg = cls._getSimilaritiesToIndividual(proband, p)
                similarities.update({p.value: {"max": sMax, "avg": sAvg}})

                if p.father:
                    nextIndividualsToCheck.append(p.father)
                if p.mother:
                    nextIndividualsToCheck.append(p.mother)

            individualsToCheck = nextIndividualsToCheck

        return similarities

    @classmethod
    def _isComparable(cls, other):
        return other is not None and other.hasFaces()

    @classmethod
    def _getSimilaritiesToIndividual(cls, proband, other):
        maxSimilarities = dictUtils.zeros(FACIAL_FEATURES)
        mostSimilarFaces = {}
        maxResult = {}
        comparedPairs = []

        avgSimilarities = dictUtils.zeros(FACIAL_FEATURES)
        avgResult = {}

        avgMaxSimilarities = dictUtils.zeros(FACIAL_FEATURES)
        avgMaxResult = {}

        if cls._isComparable(other):
            for ownFace in proband.faces:
                maxSims = dictUtils.zeros(FACIAL_FEATURES)
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
                maxResult.update({c: Similarity(f1.image, f2.image, s)})
                avgResult.update({c: avgSimilarities[c] / len(comparedPairs)})
                avgMaxResult.update(
                    {
                        c: Similarity(
                            f1.image,
                            f2.image,
                            avgMaxSimilarities[c] / len(proband.faces),
                        )
                    }
                )

        return maxResult, avgResult
