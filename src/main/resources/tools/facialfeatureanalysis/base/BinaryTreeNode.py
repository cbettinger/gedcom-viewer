from base.config import DEFAULT_DEPTH


class BinaryTreeNode:
    def __init__(self, value, father=None, mother=None):
        self.value = value
        self.father = father
        self.mother = mother

    def __str__(self):
        s = "value: {}".format(self.value)
        if self.father:
            s += "\n  father: {}".format(self.father.value)
        if self.mother:
            s += "\n  mother: {}".format(self.mother.value)
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

        v = node.value
        parents = [node.father, node.mother]

        if any(parents) and (max_depth is None or depth < max_depth):
            for parent in parents:
                for path in BinaryTreeNode._paths(parent, depth + 1, max_depth):
                    yield [v] + path
        else:
            yield [v]
