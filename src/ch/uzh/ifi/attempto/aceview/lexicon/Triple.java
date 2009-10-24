package ch.uzh.ifi.attempto.aceview.lexicon;

import org.semanticweb.owlapi.model.IRI;

import ch.uzh.ifi.attempto.ape.Gender;
import ch.uzh.ifi.attempto.ape.LexiconEntry;

public class Triple {

	private String object;
	private IRI property;
	private IRI subject;

	public Triple(IRI subject, IRI property, String object) {
		this.subject = subject;
		this.property = property;
		this.object = object;
	}

	public IRI getSubjectIRI() {
		return subject;
	}

	public String getObject() {
		return object;
	}

	public boolean hasProperty(IRI iri) {
		return iri.equals(property);
	}

	/**
	 * TODO: Note that the lemma is an IRI fragment, rather that the full
	 * IRI. This is not good as http://money#bank and http://river#bank
	 * would not be distinguished. We should use the full IRIs but currently
	 * the the "namespace" URI is sent to the ACE parser and this URI is
	 * treated as a prefix (e.g. as a namespace for unknown words). Think
	 * about it.
	 * 
	 * @return
	 */
	public LexiconEntry getLexiconEntry() {
		//String lemma = subject.toString();
		String lemma = subject.getFragment();
		switch (MorphType.getMorphType(property)) {
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
			throw new RuntimeException("Programmer error: missed a case for: " + MorphType.getMorphType(property));
		}
	}

	@Override
	public String toString() {
		return subject + " :: " + property + " :: " + object;
	}
}