import os
from base.Face import Face
from base.Image import Image
from base.BinaryTreeItem import BinaryTreeItem
from base.config import MAX_IMAGES_PER_PERSON, FACE_CHARACTERISTICS_OF_INTEREST

class Person(BinaryTreeItem):

    PERSONS = {}

    def __init__(self, id, portraits, father=None, mother=None, maxNumPortraits=MAX_IMAGES_PER_PERSON):
        super().__init__("ID", id, "Father", "Mother", father, mother)
        Person.PERSONS.update({str(id): self})

        self.faces = []
        if portraits is not None:
            usedFiles = []
            for p in portraits:
                if maxNumPortraits is not None and len(self.faces) == maxNumPortraits:
                    print("Für Individuum {} sind mehr Fotos verfügbar als verwendet werden. Es werden die folgenden Fotos genutzt: {}".format(id, usedFiles))
                    break
                filePath = p.get("filePath")
                try:
                    self.faces.append(Face(Image(filePath, p.get("boxPoints")), FACE_CHARACTERISTICS_OF_INTEREST))
                    usedFiles.append(filePath)
                except Exception as e:
                    print(filePath, "konnte nicht geladen werden oder es war kein Gesicht erkennbar ({})".format(e))

    def hasFaces(self):
        return len(self.faces) > 0
    
    @classmethod
    def fromJSON(cls, jsonObject, maxNumPortraits=None):
        if jsonObject is None or jsonObject.get("id") in Person.PERSONS.keys():
            return None
        else:
            return Person(jsonObject.get("id"), jsonObject.get("portraits"), Person.fromJSON(jsonObject.get("father"), maxNumPortraits), Person.fromJSON(jsonObject.get("mother"), maxNumPortraits), maxNumPortraits)
