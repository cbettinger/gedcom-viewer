package bettinger.gedcomviewer.tools.facialfeatureanalysis;

import java.util.Arrays;
import java.util.List;

public class AncestralLine {
	private final List<String> ids;

	private AncestralLine(final List<String> ids) {
		this.ids = ids;
	}

	public List<String> getIds() {
		return ids;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof AncestralLine line) {
			for (int i = 0; i < ids.size(); i++) {
				if (ids.get(i) != line.ids.get(i)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(ids.toArray(new String[0]));
	}

	public static AncestralLine parse(final String idList) {
		return new AncestralLine(Arrays.asList(idList.split("\\s*,\\s*")));
	}
}
