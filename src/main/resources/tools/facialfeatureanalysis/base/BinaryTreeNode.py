from base.config import DEFAULT_DEPTH


class BinaryTreeNode:
    def __init__(self, id, father=None, mother=None):
        self.id = id
        self.father = father
        self.mother = mother

    def __str__(self):
        s = "id: {}".format(self.id)
        if self.father:
            s += "\nfather: {}".format(self.father.id)
        if self.mother:
            s += "\nmother: {}".format(self.mother.id)
        return s

    def tree(self, indentation=""):
        s = "\n{t}id: {v}".format(t=indentation, v=self.id)
        if self.father:
            s += "\n{t}father: {v}".format(
                t=indentation, v=self.father.tree(indentation + "  ")
            )
        if self.mother:
            s += "\n{t}mother: {v}".format(
                t=indentation, v=self.mother.tree(indentation + "  ")
            )
        return s

    def get_paths(self, include_self=False, max_depth=DEFAULT_DEPTH):
        if include_self:
            return list(BinaryTreeNode._paths(self, max_depth=max_depth))
        else:
            return [paths[1:] for paths in BinaryTreeNode._paths(self, max_depth=max_depth)]

    @classmethod
    def _paths(cls, node, depth=0, max_depth=None):
        if node is None:
            return

        nid = node.id
        parents = [node.father, node.mother]

        if any(parents) and (max_depth is None or depth < max_depth):
            for parent in parents:
                for path in BinaryTreeNode._paths(parent, depth + 1, max_depth):
                    yield [nid] + path
        else:
            yield [nid]
