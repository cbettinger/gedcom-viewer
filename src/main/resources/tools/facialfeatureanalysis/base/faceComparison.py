from base import dictUtils
from base.Face import Face
from base.FaceAnalyser import FaceAnalyser

DEFAULT_DEPTH = 5


def getFaceAnalysisResult(proband, maxDepth=DEFAULT_DEPTH):
    results = FaceAnalyser.analyse(proband, maxDepth)

    if results is None:
        return {"error": True, "message": "Insufficient number of portraits"}
    paths = proband.paths(maxDepth)

    similarities = dictUtils.dicts(Face.FEATURES)
    lineSimilarities = dictUtils.dicts(Face.FEATURES)

    for c in Face.FEATURES:
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
