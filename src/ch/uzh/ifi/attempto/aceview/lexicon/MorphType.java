package ch.uzh.ifi.attempto.aceview.lexicon;

import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

import com.google.common.collect.Sets;

public enum MorphType {

	CN_SG("http://attempto.ifi.uzh.ch/ace_lexicon#CN_sg", EntryType.CN, FieldType.SG),
	CN_PL("http://attempto.ifi.uzh.ch/ace_lexicon#CN_pl", EntryType.CN, FieldType.PL),
	PN_SG("http://attempto.ifi.uzh.ch/ace_lexicon#PN_sg", EntryType.PN, FieldType.SG),
	TV_SG("http://attempto.ifi.uzh.ch/ace_lexicon#TV_sg", EntryType.TV, FieldType.SG),
	TV_PL("http://attempto.ifi.uzh.ch/ace_lexicon#TV_pl", EntryType.TV, FieldType.PL),
	TV_VBG("http://attempto.ifi.uzh.ch/ace_lexicon#TV_vbg", EntryType.TV, FieldType.VBG);

	private final IRI iri;
	private final EntryType entryType;
	private final FieldType fieldType;


	private MorphType(String iriAsString, EntryType entryType, FieldType fieldType) {
		this.iri = IRI.create(iriAsString);
		this.entryType = entryType;
		this.fieldType = fieldType;
	}

	public IRI getIRI() {
		return iri;
	}

	public EntryType getEntryType() {
		return entryType;
	}

	public FieldType getFieldType() {
		return fieldType;
	}

	public boolean hasIRI(IRI iri) {
		return this.iri.equals(iri);
	}


	/**
	 * <p>Returns the {@link MorphType} that corresponds to the given IRI.</p>
	 * 
	 * @param iri IRI
	 * @return {@link MorphType} of the given IRI
	 */
	public static MorphType getMorphType(IRI iri) {
		if (iri.equals(CN_SG.getIRI())) {
			return CN_SG;
		}
		else if (iri.equals(CN_PL.getIRI())) {
			return CN_PL;
		}
		else if (iri.equals(TV_SG.getIRI())) {
			return TV_SG;
		}
		else if (iri.equals(TV_PL.getIRI())) {
			return TV_PL;
		}
		else if (iri.equals(TV_VBG.getIRI())) {
			return TV_VBG;
		}
		else if (iri.equals(PN_SG.getIRI())) {
			return PN_SG;
		}

		return null;
	}


	public static EntryType getWordClass(IRI iri) {
		if (iri.equals(CN_SG.getIRI())) {
			return EntryType.CN;
		}
		else if (iri.equals(CN_PL.getIRI())) {
			return EntryType.CN;
		}
		else if (iri.equals(TV_SG.getIRI())) {
			return EntryType.TV;
		}
		else if (iri.equals(TV_PL.getIRI())) {
			return EntryType.TV;
		}
		else if (iri.equals(TV_VBG.getIRI())) {
			return EntryType.TV;
		}
		else if (iri.equals(PN_SG.getIRI())) {
			return EntryType.PN;
		}

		return null;
	}


	public static EntryType getWordClass(MorphType morphType) {
		switch (morphType) {
		case CN_SG:
			return EntryType.CN;
		case CN_PL:
			return EntryType.CN;
		case TV_SG:
			return EntryType.TV;
		case TV_PL:
			return EntryType.TV;
		case TV_VBG:
			return EntryType.TV;
		case PN_SG:
			return EntryType.PN;
		default:
			throw new RuntimeException("Programmer expected something else...");
		}
	}


	public static MorphType getMorphType(EntryType entryType, FieldType fieldType) {
		if (entryType == EntryType.CN && fieldType == FieldType.SG) {
			return CN_SG;
		}
		else if (entryType == EntryType.CN && fieldType == FieldType.PL) {
			return CN_PL;
		}
		else if (entryType == EntryType.PN && fieldType == FieldType.SG) {
			return PN_SG;
		}
		else if (entryType == EntryType.TV && fieldType == FieldType.SG) {
			return TV_SG;
		}
		else if (entryType == EntryType.TV && fieldType == FieldType.PL) {
			return TV_PL;
		}
		else if (entryType == EntryType.TV && fieldType == FieldType.VBG) {
			return TV_VBG;
		}

		return null;
	}


	/**
	 * TODO: BUG: initialize these sets at construction time
	 * @param entryType
	 * @return
	 */
	public static Set<MorphType> getMorphTypeSet(EntryType entryType) {
		if (entryType == null) {
			throw new Error("entryType Cant be null");
		}

		switch (entryType) {
		case CN:
			return Sets.immutableEnumSet(CN_SG, CN_PL);
		case TV:
			return Sets.immutableEnumSet(TV_SG, TV_PL, TV_VBG);
		case PN:
			return Sets.immutableEnumSet(PN_SG);
		default:
			throw new RuntimeException("Programmer expected CN, TV, or PN");
		}
	}


	/**
	 * @deprecated
	 * 
	 * TODO: BUG: This is slow, we should match against a set.
	 */
	public static boolean isMorphTypeIRI(IRI annotationIRI) {
		return getMorphType(annotationIRI) != null;
	}
}