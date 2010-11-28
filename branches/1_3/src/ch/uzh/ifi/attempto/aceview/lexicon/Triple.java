package ch.uzh.ifi.attempto.aceview.lexicon;

import org.semanticweb.owlapi.model.IRI;

import ch.uzh.ifi.attempto.ape.Gender;
import ch.uzh.ifi.attempto.ape.LexiconEntry;

public class Triple {

	private final String object;
	private final MorphType property;
	private final IRI subject;
	private final int hashCode;

	public Triple(IRI subject, MorphType property, String object) {
		this.subject = subject;
		this.property = property;
		this.object = object;

		// TODO: BUG: is this the right way to do it?
		hashCode = 17 + 37 * subject.hashCode() + 37 * property.hashCode() + 37 * object.hashCode(); 
	}

	public IRI getSubjectIRI() {
		return subject;
	}

	public MorphType getProperty() {
		return property;
	}

	public String getObject() {
		return object;
	}

	public boolean hasProperty(MorphType morphType) {
		return morphType.equals(property);
	}


	/**
	 * <p>There are two conventions to pass the full IRI via the lexicon to
	 * the ACE->OWL/SWRL translator: iri('IRI') and 'iri|IRI', where IRI is the
	 * actual IRI. Neither is supported by the latest released APE and the first
	 * is not supported by the Attempto Java Packages.</p>
	 * 
	 * TODO: BUG: If ACE View is released before APE then revert to using the IRI fragment
	 * as the lemma (which works incorrectly in some cases).
	 * 
	 * @return ACE lexicon entry
	 */
	public LexiconEntry getLexiconEntry() {
		// String lemma = subject.getFragment();
		String lemma = "iri|" + subject.toString();

		switch (property) {
		case PN_SG:
			return LexiconEntry.createPropernameSgEntry(object, lemma, Gender.NEUTRAL);
		case CN_SG:
			return LexiconEntry.createNounSgEntry(object, lemma, Gender.NEUTRAL);
		case CN_PL:
			return LexiconEntry.createNounPlEntry(object, lemma, Gender.NEUTRAL);
		case TV_SG:
			return LexiconEntry.createTrVerbThirdEntry(object, lemma);
		case TV_PL:
			return LexiconEntry.createTrVerbInfEntry(object, lemma);
		case TV_VBG:
			return LexiconEntry.createTrVerbPPEntry(object, lemma);
		default:
			throw new RuntimeException("Programmer error: missed a case for: " + property);
		}
	}


	@Override
	public String toString() {
		return subject.getFragment() + " :: " + property + " :: " + object;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if ((obj == null) || (obj.getClass() != this.getClass())) return false;
		Triple t = (Triple) obj;
		return subject.equals(t.getSubjectIRI()) && object.equals(t.getObject()) && property.equals(t.getProperty());
	}


	@Override
	public int hashCode() {
		return hashCode;
	}
}