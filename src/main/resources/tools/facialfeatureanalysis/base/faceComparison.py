from base.config import FACE_CHARACTERISTICS_OF_INTEREST
from base.FaceAnalyser import FaceAnalyser
from utils import dictUtils


def getAvgLineSimilarity(path, personSimilarities):
    numEntries = len(path)
    lineSim = 0
    for individualID in path:
        s = personSimilarities.get(individualID)
        if s is None:
            numEntries -= 1
        else:
            lineSim += s
    return lineSim / numEntries


def getFaceAnalysisResult(targetPerson, maxDepth=None):
    similarities = FaceAnalyser.analyse(targetPerson, maxDepth)

    if similarities is None:
        return {"error": True, "message": "Insufficient number of portraits"}
    paths = targetPerson.getPaths()

    similarities = dictUtils.getDicts(FACE_CHARACTERISTICS_OF_INTEREST)
    lineSimilarities = dictUtils.getDicts(FACE_CHARACTERISTICS_OF_INTEREST)

    for c in FACE_CHARACTERISTICS_OF_INTEREST:
        avgPersonSimilarities = {}

        for id, personResults in similarities.items():
            maxSimRes = personResults["max"][c]
            avgSim = personResults["avg"][c]

            avgPersonSimilarities.update({id: avgSim})

            if maxSimRes.value is None:
                similarities[c].update({id: ""})
            else:
                individualResult = {
                    "maxSimilarity": str(maxSimRes.value),
                    "avgSimilarity": str(avgSim),
                    "maxSimilarProbandPortraitFilePath": maxSimRes.image1.fileName,
                    "maxSimilarAncestorPortraitFilePath": maxSimRes.image2.fileName,
                }
                similarities[c].update({id: individualResult})

        for path in paths:
            lineSimilarities[c].update(
                {
                    str(path)
                    .replace("[", "")
                    .replace("]", "")
                    .replace("'", ""): str(
                        getAvgLineSimilarity(path, avgPersonSimilarities)
                    )
                }
            )

    return {"lineSimilarities": lineSimilarities, "similarities": similarities}
