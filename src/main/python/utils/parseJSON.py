import json
import os
from base.Person import Person
from base.config import MAX_IMAGES_PER_PERSON

def parseFile(filePath):
    f = open(filePath, encoding='utf-8')
    parsedObject = json.load(f)
    f.close()
    rootDir = os.path.dirname(filePath)
    return Person.fromJSON(parsedObject, rootDir)

def parseString(jsonString, maxNumPortraits=MAX_IMAGES_PER_PERSON):
    parsedObject = json.loads(jsonString)
    return Person.fromJSON(parsedObject, maxNumPortraits)