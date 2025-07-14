from base.config import MAX_COMPARISON_DEPTH


class BinaryTreeItem:

    def __init__(
        self, name, value, parent1Name, parent2Name, parent1=None, parent2=None
    ):
        self.name = name
        self.value = value
        self.parent1Name = parent1Name
        self.parent2Name = parent2Name
        self.parent1 = parent1
        self.parent2 = parent2

    def __str__(self):
        s = s = "{}: {}".format(self.name, self.value)
        if self.parent1:
            s += "\n{}: {}".format(self.parent1Name, self.parent1.value)
        if self.parent2:
            s += "\n{}: {}".format(self.parent2Name, self.parent2.value)
        return s

    def tree(self, tabbing=""):
        s = "\n{t}{n}: {v}".format(t=tabbing, n=self.name, v=self.value)
        if self.parent1:
            s += "\n{t}{n}: {v}".format(
                t=tabbing, n=self.parent1Name, v=self.parent1.tree(tabbing + "  ")
            )
        if self.parent2:
            s += "\n{t}{n}: {v}".format(
                t=tabbing, n=self.parent2Name, v=self.parent2.tree(tabbing + "  ")
            )
        return s

    def getPaths(self, includeSelf=False, maxDepth=MAX_COMPARISON_DEPTH):
        if includeSelf:
            return [p for p in BinaryTreeItem._paths(self, maxDepth=maxDepth)]
        else:
            return [p[1:] for p in BinaryTreeItem._paths(self, maxDepth=maxDepth)]

    @classmethod
    def _paths(cls, node, depth=0, maxDepth=None):
        if node is None:
            return
        v = node.value
        parents = [node.parent1, node.parent2]
        if any(parents) and (maxDepth is None or depth < maxDepth):
            for p in parents:
                for path in BinaryTreeItem._paths(p, depth + 1, maxDepth):
                    yield [v] + path
        else:
            yield [v]
