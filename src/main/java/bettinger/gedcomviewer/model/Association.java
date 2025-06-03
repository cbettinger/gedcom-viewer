package bettinger.gedcomviewer.model;

import java.util.Set;

import bettinger.gedcomviewer.Format;

public class Association extends Substructure {

	static final String TAG = "ASSO";

	private final org.folg.gedcom.model.Association wrappedAssociation;

	private final Individual source;
	private final Individual target;
	private final String relation;

	Association(final GEDCOM gedcom, final org.folg.gedcom.model.Association association, final Individual source) {
		super(gedcom, TAG, association, source);

		this.wrappedAssociation = association;

		this.source = source;
		this.target = (Individual) gedcom.getRecord(association.getRef());
		this.relation = wrappedAssociation.getRelation();
	}

	/* #region getter & setter */
	public Individual getSource() {
		return source;
	}

	public Individual getTarget() {
		return target;
	}

	public String getRelation() {
		return relation;
	}
	/* #endregion */

	/* #region toString & toHTML */
	@Override
	public String toString() {
		return String.format(Format.KEY_VALUE, relation.isEmpty() ? UNKNOWN_STRING : relation, target == null ? UNKNOWN_STRING : target.toString());
	}

	@Override
	public String toHTML(final Set<HTMLOption> options) {
		return String.format(Format.KEY_VALUE, relation.isEmpty() ? UNKNOWN_STRING : relation, target == null ? UNKNOWN_STRING : target.getLink());
	}
	/* #endregion */
}
