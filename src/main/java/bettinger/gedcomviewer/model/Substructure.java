package bettinger.gedcomviewer.model;

import org.folg.gedcom.model.ExtensionContainer;

abstract class Substructure extends Structure {

	final Structure parentStructure;

	Substructure(final GEDCOM gedcom, final String idPrefix, final ExtensionContainer wrappedStructure, final Structure parentStructure) {
		super(gedcom, constructId(idPrefix, wrappedStructure), wrappedStructure);

		this.parentStructure = parentStructure;
	}
}
