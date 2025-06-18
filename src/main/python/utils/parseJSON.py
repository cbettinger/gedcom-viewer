import json
import os
from base.Person import Person
from base.config import MAX_IMAGES_PER_PERSON

def parseFile(filePath, maxNumPortraits=MAX_IMAGES_PER_PERSON):
    f = open(filePath, encoding='utf-8')
    parsedObject = json.load(f)
    f.close()
    return Person.fromJSON(parsedObject, maxNumPortraits)

def parseString(jsonString, maxNumPortraits=MAX_IMAGES_PER_PERSON):
    parsedObject = json.loads(jsonString)
    return Person.fromJSON(parsedObject, maxNumPortraits)