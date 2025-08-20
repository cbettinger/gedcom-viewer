from base.config import FACE_CHARACTERISTICS_OF_INTEREST
from base.FaceAnalyser import FaceAnalyser
from utils import dictUtils
import sys

def getAvgPathSimilarity(path, personSimilarities):
    numEntries = len(path)
    pathSim = 0
    for individualID in path:
        s = personSimilarities.get(individualID)
        if s is None:
            numEntries -=1
        else:
            pathSim += s
    return pathSim/numEntries

def getAverageSim(path, similaritiesToTarget):
    return getPathSum(path, similaritiesToTarget)/len(path)

def getPathSum(path, similaritiesToTarget):
    result = 0
    for id in path:
        result += similaritiesToTarget[id]
    return result

def getProductSim(path, similaritiesToTarget):
    sim = 1
    for id in path:
        sim *= similaritiesToTarget[id]
    return sim

def getPathSimilarityIndex(path, similartiesToTarget):
    # TODO entweder multiplizieren oder Summe bilden (oder gewichtete Summe)
    return getPathSum(path, similartiesToTarget)

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
                individualResult = {"maxSimilarity": str(maxSimRes.value), "avgSimilarity": str(avgSim), "maxMatchImgTarget": maxSimRes.img1.fileName, "maxMatchImgAncestor": maxSimRes.img2.fileName}
                nodes[c].update({id: individualResult})
        
        bestPaths[c] = getBestPaths(paths, correctedSimilaritiesForCharacteristic)

    return {"bestPaths": bestPaths, "nodes": nodes, "edgeSimilarities": edgeSimilarities}