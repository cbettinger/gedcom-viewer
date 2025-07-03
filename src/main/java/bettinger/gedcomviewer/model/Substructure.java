package bettinger.gedcomviewer.model;

import org.folg.gedcom.model.ExtensionContainer;

abstract class Substructure extends Structure {

	private final Structure parentStructure;

	Substructure(final GEDCOM gedcom, final String idPrefix, final ExtensionContainer wrappedStructure, final Structure parentStructure) {
		super(gedcom, constructId(idPrefix, wrappedStructure), wrappedStructure);

		this.parentStructure = parentStructure;
	}

	public Structure getParentStructure() {
		return parentStructure;
	}
}
