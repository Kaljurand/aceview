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

	public LexiconEntry getLexiconEntry() {
		String lemma = subject.toString();
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