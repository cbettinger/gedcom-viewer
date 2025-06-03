package bettinger.gedcomviewer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.folg.gedcom.model.GedcomTag;

import bettinger.gedcomviewer.utils.HTMLUtils;

public class Address extends Substructure {

	static final String TAG = "ADDR";
	static final String TAG_PHONE = "PHON";
	static final String TAG_FAX = "FAX";
	static final String TAG_EMAIL = "EMAIL";
	static final String TAG_WWW = "WWW";

	private final org.folg.gedcom.model.Address wrappedAddress;

	private org.folg.gedcom.model.Submitter wrappedSubmitter;
	private org.folg.gedcom.model.Repository wrappedRepository;

	private List<String> phoneNumbers;
	private List<String> eMailAddresses;
	private List<String> www;

	Address(final GEDCOM gedcom, final org.folg.gedcom.model.Submitter submitter, final Structure parentStructure) {
		this(gedcom, submitter.getAddress(), parentStructure);

		this.wrappedSubmitter = submitter;

		parse();
	}

	Address(final GEDCOM gedcom, final org.folg.gedcom.model.Repository repository, final Structure parentStructure) {
		this(gedcom, repository.getAddress(), parentStructure);

		this.wrappedRepository = repository;

		parse();
	}

	private Address(final GEDCOM gedcom, final org.folg.gedcom.model.Address address, final Structure parentStructure) {
		super(gedcom, TAG, address, parentStructure);

		this.wrappedAddress = address;
	}

	private void parse() {
		phoneNumbers = new ArrayList<>();
		eMailAddresses = new ArrayList<>();
		www = new ArrayList<>();

		if (wrappedSubmitter != null) {
			if (wrappedSubmitter.getPhone() != null && !wrappedSubmitter.getPhone().isEmpty()) {
				phoneNumbers.add(wrappedSubmitter.getPhone());
			}

			if (wrappedSubmitter.getEmail() != null && !wrappedSubmitter.getEmail().isEmpty()) {
				eMailAddresses.add(wrappedSubmitter.getEmail());
			}

			if (wrappedSubmitter.getWww() != null && !wrappedSubmitter.getWww().isEmpty()) {
				www.add(wrappedSubmitter.getWww());
			}
		}

		if (wrappedRepository != null) {
			if (wrappedRepository.getPhone() != null && !wrappedRepository.getPhone().isEmpty()) {
				phoneNumbers.add(wrappedRepository.getPhone());
			}

			if (wrappedRepository.getEmail() != null && !wrappedRepository.getEmail().isEmpty()) {
				eMailAddresses.add(wrappedRepository.getEmail());
			}

			if (wrappedRepository.getWww() != null && !wrappedRepository.getWww().isEmpty()) {
				www.add(wrappedRepository.getWww());
			}
		}

		final var phon = parentStructure.getExtensionTags(TAG_PHONE);
		if (!phon.isEmpty()) {
			phon.stream().forEach(t -> {
				final var p = t.getValue();
				if (p != null && !p.isEmpty()) {
					phoneNumbers.add(p);
				}
			});
		}

		final var email = parentStructure.getExtensionTags(TAG_EMAIL);
		if (!email.isEmpty()) {
			email.stream().forEach(t -> {
				final var m = t.getValue();
				if (m != null && !m.isEmpty()) {
					eMailAddresses.add(m);
				}
			});
		}

		final var urls = parentStructure.getExtensionTags(TAG_WWW);
		if (!urls.isEmpty()) {
			urls.stream().forEach(t -> {
				final var w = t.getValue();
				if (w != null && !w.isEmpty()) {
					www.add(w);
				}
			});
		}
	}

	/* #region getter & setter */
	public String getValue() {
		return wrappedAddress.getValue() == null ? "" : wrappedAddress.getValue();
	}

	public void setValue(final String value) {
		wrappedAddress.setValue(value == null ? "" : value);
	}

	public String getLine1() {
		return wrappedAddress.getAddressLine1() == null ? "" : wrappedAddress.getAddressLine1();
	}

	public void setLine1(final String value) {
		wrappedAddress.setAddressLine1(value == null ? "" : value);
	}

	public String getLine2() {
		return wrappedAddress.getAddressLine2() == null ? "" : wrappedAddress.getAddressLine2();
	}

	public void setLine2(final String value) {
		wrappedAddress.setAddressLine2(value == null ? "" : value);
	}

	public String getLine3() {
		return wrappedAddress.getAddressLine3() == null ? "" : wrappedAddress.getAddressLine3();
	}

	public void setLine3(final String value) {
		wrappedAddress.setAddressLine3(value == null ? "" : value);
	}

	public String getPostalCode() {
		return wrappedAddress.getPostalCode() == null ? "" : wrappedAddress.getPostalCode();
	}

	public void setPostalCode(final String value) {
		wrappedAddress.setPostalCode(value == null ? "" : value);
	}

	public String getCity() {
		return wrappedAddress.getCity() == null ? "" : wrappedAddress.getCity();
	}

	public void setCity(final String value) {
		wrappedAddress.setCity(value == null ? "" : value);
	}

	public String getState() {
		return wrappedAddress.getState() == null ? "" : wrappedAddress.getState();
	}

	public void setState(final String value) {
		wrappedAddress.setState(value == null ? "" : value);
	}

	public String getCountry() {
		return wrappedAddress.getCountry() == null ? "" : wrappedAddress.getCountry();
	}

	public void setCountry(final String value) {
		wrappedAddress.setCountry(value == null ? "" : value);
	}

	public List<String> getPhoneNumbers() {
		return phoneNumbers;
	}

	public void setPhoneNumbers(final List<String> values) {
		if (!values.isEmpty()) {
			final var first = values.get(0);
			if (wrappedRepository != null) {
				wrappedRepository.setPhone(first);
			}
			if (wrappedSubmitter != null) {
				wrappedSubmitter.setPhone(first);
			}

			if (values.size() > 1) {
				parentStructure.replaceExtensionTags(TAG_PHONE, values.stream().skip(1).map(s -> {
					final var tag = new GedcomTag(null, TAG_PHONE, null);
					tag.setValue(s);
					return tag;
				}).toList());
			}

			parse();
		}
	}

	public List<String> getEMailAddresses() {
		return eMailAddresses;
	}

	public void setEMailAddresses(final List<String> values) {
		if (!values.isEmpty()) {
			final var first = values.get(0);
			if (wrappedRepository != null) {
				wrappedRepository.setEmail(first);
			}
			if (wrappedSubmitter != null) {
				wrappedSubmitter.setEmail(first);
			}

			if (values.size() > 1) {
				parentStructure.replaceExtensionTags(TAG_EMAIL, values.stream().skip(1).map(s -> {
					final var tag = new GedcomTag(null, TAG_EMAIL, null);
					tag.setValue(s);
					return tag;
				}).toList());
			}

			parse();
		}
	}

	public List<String> getWWW() {
		return www;
	}

	public void setWWW(final List<String> values) {
		if (!values.isEmpty()) {
			final var first = values.get(0);
			if (wrappedRepository != null) {
				wrappedRepository.setWww(first);
			}
			if (wrappedSubmitter != null) {
				wrappedSubmitter.setWww(first);
			}

			if (values.size() > 1) {
				parentStructure.replaceExtensionTags(TAG_WWW, values.stream().skip(1).map(s -> {
					final var tag = new GedcomTag(null, TAG_WWW, null);
					tag.setValue(s);
					return tag;
				}).toList());
			}

			parse();
		}
	}
	/* #endregion */

	/* #region toString & toHTML */
	@Override
	public String toString() {
		final var sb = new StringBuilder();

		var wasAppended = false;
		var postalAppended = false;

		final var value = getValue();
		if (!value.isEmpty()) {
			sb.append(value);
			wasAppended = true;
		}

		final var line1 = getLine1();
		if (!line1.isEmpty() && (value.isEmpty() || !value.contains(line1))) {
			if (wasAppended) {
				sb.append(System.lineSeparator());
			}
			sb.append(line1);
			wasAppended = true;
		}

		final var line2 = getLine2();
		if (!line2.isEmpty() && (value.isEmpty() || !value.contains(line2))) {
			if (wasAppended) {
				sb.append(System.lineSeparator());
			}
			sb.append(line2);
			wasAppended = true;
		}

		final var line3 = getLine3();
		if (!line3.isEmpty() && (value.isEmpty() || !value.contains(line3))) {
			if (wasAppended) {
				sb.append(System.lineSeparator());
			}
			sb.append(line3);
			wasAppended = true;
		}

		final var postalCode = getPostalCode();
		if (!postalCode.isEmpty() && (value.isEmpty() || !value.contains(postalCode))) {
			if (wasAppended) {
				sb.append(System.lineSeparator());
			}
			sb.append(postalCode);
			wasAppended = true;
			postalAppended = true;
		}

		final var city = getCity();
		if (!city.isEmpty() && (value.isEmpty() || !value.contains(city))) {
			if (postalAppended) {
				sb.append(" ");
			} else if (wasAppended) {
				sb.append(System.lineSeparator());
			}
			sb.append(city);
			wasAppended = true;
		}

		final var state = getState();
		if (!state.isEmpty() && (value.isEmpty() || !value.contains(state))) {
			if (wasAppended) {
				sb.append(System.lineSeparator());
			}
			sb.append(state);
			wasAppended = true;
		}

		final var country = getCountry();
		if (!country.isEmpty() && (value.isEmpty() || !value.contains(country))) {
			if (wasAppended) {
				sb.append(System.lineSeparator());
			}
			sb.append(country);
			wasAppended = true;
		}

		if (!phoneNumbers.isEmpty()) {
			if (wasAppended) {
				sb.append(System.lineSeparator());
				sb.append(System.lineSeparator());
			}
			sb.append(String.join(System.lineSeparator(), phoneNumbers));
			wasAppended = true;
		}

		if (!eMailAddresses.isEmpty()) {
			if (wasAppended) {
				sb.append(System.lineSeparator());
				sb.append(System.lineSeparator());
			}
			sb.append(String.join(System.lineSeparator(), eMailAddresses));
			wasAppended = true;
		}

		if (!www.isEmpty()) {
			if (wasAppended) {
				sb.append(System.lineSeparator());
				sb.append(System.lineSeparator());
			}
			sb.append(String.join(System.lineSeparator(), www));
		}

		return sb.toString();
	}

	@Override
	public String toHTML(final Set<HTMLOption> options) {
		final var str = toString();
		return str.isEmpty() ? str : HTMLUtils.createElement("div", HTMLUtils.convertStringToHTML(toString()));
	}
	/* #endregion */
}
