package ch.uzh.ifi.attempto.aceview.lexicon;

import java.net.URI;

public enum MorphType {

	CN_SG("http://attempto.ifi.uzh.ch/ace_lexicon#CN_sg", EntryType.CN, FieldType.SG),
	CN_PL("http://attempto.ifi.uzh.ch/ace_lexicon#CN_pl", EntryType.CN, FieldType.PL),
	PN_SG("http://attempto.ifi.uzh.ch/ace_lexicon#PN_sg", EntryType.PN, FieldType.SG),
	TV_SG("http://attempto.ifi.uzh.ch/ace_lexicon#TV_sg", EntryType.TV, FieldType.SG),
	TV_PL("http://attempto.ifi.uzh.ch/ace_lexicon#TV_pl", EntryType.TV, FieldType.PL),
	TV_VBG("http://attempto.ifi.uzh.ch/ace_lexicon#TV_vbg", EntryType.TV, FieldType.VBG);

	private final URI uri;
	private final EntryType entryType;
	private final FieldType fieldType;

	private MorphType(String uriAsString, EntryType entryType, FieldType fieldType) {
		this.uri = URI.create(uriAsString);
		this.entryType = entryType;
		this.fieldType = fieldType;
	}

	public URI getURI() {
		return uri;
	}

	public EntryType getEntryType() {
		return entryType;
	}

	public FieldType getFieldType() {
		return fieldType;
	}

	public boolean hasURI(URI uri) {
		return this.uri.equals(uri);
	}

	/**
	 * <p>Returns the {@link FieldType} that corresponds to the given URI.</p>
	 * 
	 * @param uri URI
	 * @return {@link FieldType} of the given URI
	 */
	public static MorphType getMorphType(URI uri) {
		if (uri.equals(CN_SG.getURI())) {
			return CN_SG;
		}
		else if (uri.equals(CN_PL.getURI())) {
			return CN_PL;
		}
		else if (uri.equals(TV_SG.getURI())) {
			return TV_SG;
		}
		else if (uri.equals(TV_PL.getURI())) {
			return TV_PL;
		}
		else if (uri.equals(TV_VBG.getURI())) {
			return TV_VBG;
		}
		else if (uri.equals(PN_SG.getURI())) {
			return PN_SG;
		}

		return null;
	}

	public static boolean isMorphTypeURI(URI annotationURI) {
		return getMorphType(annotationURI) != null;
	}
}