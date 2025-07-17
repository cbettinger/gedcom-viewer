from base.BinaryTreeNode import BinaryTreeNode
from base.config import DEFAULT_NUM_PORTRAITS, FACIAL_FEATURES
from base.Face import Face
from base.Image import Image
import json


class Person(BinaryTreeNode):

    PERSONS = {}

    def __init__(
        self,
        id,
        portraits,
        father=None,
        mother=None,
        num_portraits=DEFAULT_NUM_PORTRAITS,
    ):
        super().__init__(id, father, mother)

        Person.PERSONS.update({str(id): self})

        self.faces = []

        if portraits is not None:
            considered_filepaths = []

            for p in portraits:
                if num_portraits is not None and len(self.faces) == num_portraits:
                    print(
                        "For the individual {} there are more portraits than configured to use. The following portraits are used: {}".format(
                            id, considered_filepaths
                        )
                    )
                    break

                filepath = p.get("filePath")

                try:
                    self.faces.append(
                        Face(
                            Image(filepath, p.get("clip")),
                            FACIAL_FEATURES,
                        )
                    )
                    considered_filepaths.append(filepath)
                except Exception as e:
                    print(
                        "Unable to load portrait {}: {}".format(filepath, e),
                    )

    def hasFaces(self):
        return len(self.faces) > 0

    @classmethod
    def parse(cls, filepath, num_portraits=DEFAULT_NUM_PORTRAITS):
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
