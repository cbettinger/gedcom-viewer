from base.BinaryTreeItem import BinaryTreeItem
from base.config import MAX_IMAGES_PER_PERSON, FACE_CHARACTERISTICS_OF_INTEREST
from base.Face import Face
from base.Image import Image
import json


class Person(BinaryTreeItem):

    PERSONS = {}

    def __init__(
        self,
        id,
        portraits,
        father=None,
        mother=None,
        num_portraits=MAX_IMAGES_PER_PERSON,
    ):
        super().__init__("ID", id, "Father", "Mother", father, mother)
        Person.PERSONS.update({str(id): self})

        self.faces = []
        if portraits is not None:
            usedFiles = []
            for p in portraits:
                if num_portraits is not None and len(self.faces) == num_portraits:
                    print(
                        "For the individual {} there are more portraits than configured to use. The following portraits are used: {}".format(
                            id, usedFiles
                        )
                    )
                    break
                filePath = p.get("filePath")
                try:
                    self.faces.append(
                        Face(
                            Image(filePath, p.get("boxPoints")),
                            FACE_CHARACTERISTICS_OF_INTEREST,
                        )
                    )
                    usedFiles.append(filePath)
                except Exception as e:
                    print(
                        "Unable to load portrait file {}: {}".format(filePath, e),
                    )

    def hasFaces(self):
        return len(self.faces) > 0

    @classmethod
    def parse(cls, filepath, num_portraits=MAX_IMAGES_PER_PERSON):
        f = open(filepath, encoding="utf-8")
        json_object = json.load(f)
        f.close()
        return Person.from_json(json_object, num_portraits)

    @classmethod
    def from_json(cls, json_object, num_portraits):
        if json_object is None or json_object.get("id") in Person.PERSONS.keys():
            return None
        else:
            return Person(
                json_object.get("id"),
                json_object.get("portraits"),
                Person.from_json(json_object.get("father"), num_portraits),
                Person.from_json(json_object.get("mother"), num_portraits),
                num_portraits,
            )
