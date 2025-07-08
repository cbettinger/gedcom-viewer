package bettinger.gedcomviewer.model;

import org.folg.gedcom.model.ExtensionContainer;
import org.folg.gedcom.model.GedcomTag;

abstract class Substructure extends Structure {

	private final Structure parentStructure;

	Substructure(final GEDCOM gedcom, final String idPrefix, final ExtensionContainer wrappedStructure, final Structure parentStructure) {
		super(gedcom, constructId(idPrefix, wrappedStructure), wrappedStructure);

		this.parentStructure = parentStructure;
	}

	Substructure(final GEDCOM gedcom, final GedcomTag wrappedTag, final Structure parentStructure) {
		super(gedcom, constructId(wrappedTag.getTag(), wrappedTag), null);

		this.parentStructure = parentStructure;
	}

	public Structure getParentStructure() {
		return parentStructure;
	}
}
