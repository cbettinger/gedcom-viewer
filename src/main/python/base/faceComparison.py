from base.config import FACE_CHARACTERISTICS_OF_INTEREST
from base.FaceAnalyser import FaceAnalyser
from utils import dictUtils
import sys

def getFaceAnalysisResult(targetPerson, maxDepth=None):
    directSimilaritiesToTarget, similaritiesToChildInLineToTarget, correctedSimilaritiesToTarget = FaceAnalyser.analyse(targetPerson, maxDepth)
    
    if directSimilaritiesToTarget is None:
        return {"isError": "y", "messageKey": "NotEnoughUsablePortraits"}
    paths = targetPerson.getComparablePaths(maxDepth)

    nodes = dictUtils.getDicts(FACE_CHARACTERISTICS_OF_INTEREST)
    edgeSimilarities = dictUtils.getDicts(FACE_CHARACTERISTICS_OF_INTEREST)
    bestPaths = dictUtils.getLists(FACE_CHARACTERISTICS_OF_INTEREST)

    for c in FACE_CHARACTERISTICS_OF_INTEREST:
        correctedSimilaritiesForCharacteristic = {}

        for id, personResults in directSimilaritiesToTarget.items():
            maxSimRes = personResults["max"][c]
            avgSim = personResults["avg"][c]

            if id in similaritiesToChildInLineToTarget.keys():
                correctedSimilaritiesForCharacteristic.update({id: correctedSimilaritiesToTarget[id][c]})
                edgeSimilarities[c].update({id: str(similaritiesToChildInLineToTarget[id][c])})

            if maxSimRes is None:
                nodes[c].update({id: ""})
            else:
                individualResult = {"maxSimilarity": str(maxSimRes.value), "avgSimilarity": str(avgSim), "maxMatchImgTarget": {"filename": maxSimRes.img1.fileName, "box": maxSimRes.bbox1}, "maxMatchImgAncestor": {"filename": maxSimRes.img2.fileName, "box": maxSimRes.bbox2}}
                nodes[c].update({id: individualResult})
        
        bestPaths[c] = getBestPaths(paths, correctedSimilaritiesForCharacteristic)

    return {"bestPaths": bestPaths, "nodes": nodes, "edgeSimilarities": edgeSimilarities}

def getBestPaths(paths, similaritiesToTarget):
    paths.sort(key=len)
    decisionPathLen = len(paths[0])
    pathsToConsider = [p[:decisionPathLen] for p in paths]
    pathsWithMaxSimIndex = []
    consideredOriginalPaths = paths

    while True:
        maxValue = None
        bestPaths = []
        for path in pathsToConsider:
            val = getPathSimilarityIndex(path, similaritiesToTarget)
            if maxValue is None or val > maxValue:
                bestPaths = [path]
                maxValue = val
            elif abs(val - maxValue) < sys.float_info.epsilon:
                bestPaths.append(path)

        nextOriginalPathsToConsider = []
        for originalPath in consideredOriginalPaths:
            if originalPath in bestPaths:
                pathsWithMaxSimIndex.append(originalPath)
            elif len(originalPath) > decisionPathLen and any(all(id in originalPath for id in bestPath) for bestPath in bestPaths):
                nextOriginalPathsToConsider.append(originalPath)

        if len(nextOriginalPathsToConsider) == 0:
            return pathsWithMaxSimIndex
        
        decisionPathLen = len(nextOriginalPathsToConsider[0])
        consideredOriginalPaths = nextOriginalPathsToConsider
        pathsToConsider = [p[:decisionPathLen] for p in nextOriginalPathsToConsider]


def getPathSimilarityIndex(path, similartiesToTarget):
    return getPathSum(path, similartiesToTarget)

def getPathSum(path, similaritiesToTarget):
    result = 0
    for id in path:
        result += similaritiesToTarget[id]
    return result
