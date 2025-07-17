from base import dictUtils
from base.config import FACIAL_FEATURES
from base.FaceAnalyser import FaceAnalyser

DEFAULT_DEPTH = 5


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


def getFaceAnalysisResult(targetPerson, maxDepth=DEFAULT_DEPTH):
    results = FaceAnalyser.analyse(targetPerson, maxDepth)

    if results is None:
        return {"error": True, "message": "Insufficient number of portraits"}
    paths = targetPerson.get_paths(maxDepth)

    similarities = dictUtils.getDicts(FACIAL_FEATURES)
    lineSimilarities = dictUtils.getDicts(FACIAL_FEATURES)

    for c in FACIAL_FEATURES:
        avgPersonSimilarities = {}

        for id, personResults in results.items():
            maxSimRes = personResults["max"][c]
            avgSim = personResults["avg"][c]

            avgPersonSimilarities.update({id: avgSim})

            if maxSimRes.value is None:
                similarities[c].update({id: ""})
            else:
                individualResult = {
                    "maxSimilarity": str(maxSimRes.value),
                    "avgSimilarity": str(avgSim),
                    "maxSimilarProbandPortraitFilePath": maxSimRes.image1.filepath,
                    "maxSimilarAncestorPortraitFilePath": maxSimRes.image2.filepath,
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
