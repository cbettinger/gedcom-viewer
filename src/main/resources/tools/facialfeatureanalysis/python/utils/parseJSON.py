import json

from base.config import MAX_IMAGES_PER_PERSON
from base.Person import Person


def parseFile(filePath, maxNumPortraits=MAX_IMAGES_PER_PERSON):
    f = open(filePath, encoding="utf-8")
    parsedObject = json.load(f)
    f.close()
    return Person.fromJSON(parsedObject, maxNumPortraits)
