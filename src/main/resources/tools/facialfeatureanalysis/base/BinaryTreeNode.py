from base.config import DEFAULT_DEPTH


class BinaryTreeNode:
    def __init__(self, id, parent1=None, parent2=None):
        self.id = id
        self.parent1 = parent1
        self.parent2 = parent2

    def __str__(self):
        s = "ID: {}".format(self.id)
        if self.parent1:
            s += "\nparent1: {}".format(self.parent1.value)
        if self.parent2:
            s += "\nparent2: {}".format(self.parent2.value)
        return s

    def tree(self, indentation=""):
        s = "\n{t}ID: {v}".format(t=indentation, v=self.id)
        if self.parent1:
            s += "\n{t}parent1: {v}".format(
                t=indentation, v=self.parent1.tree(indentation + "  ")
            )
        if self.parent2:
            s += "\n{t}parent2: {v}".format(
                t=indentation, v=self.parent2.tree(indentation + "  ")
            )
        return s

    def get_paths(self, include_self=False, max_depth=DEFAULT_DEPTH):
        if include_self:
            return list(BinaryTreeNode._paths(self, max_depth))
        else:
            return [p[1:] for p in BinaryTreeNode._paths(self, max_depth)]

    @classmethod
    def _paths(cls, node, depth=0, max_depth=None):
        if node is None:
            return

        v = node.value
        parents = [node.parent1, node.parent2]

        if any(parents) and (max_depth is None or depth < max_depth):
            for p in parents:
                for path in BinaryTreeNode._paths(p, depth + 1, max_depth):
                    yield [v] + path
        else:
            yield [v]
