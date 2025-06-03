package bettinger.gedcomviewer.model;

import org.folg.gedcom.model.ExtensionContainer;

import bettinger.gedcomviewer.Format;

abstract class Substructure extends Structure {

	final Structure parentStructure;

	Substructure(final GEDCOM gedcom, final String idPrefix, final ExtensionContainer wrappedStructure, final Structure parentStructure) {
		super(gedcom, String.format(Format.PIPE_SEPARATED, idPrefix, wrappedStructure.hashCode()), wrappedStructure);

		this.parentStructure = parentStructure;
	}
}
