package ch.uzh.ifi.attempto.ace;

import simplenlg.features.Feature;
import simplenlg.features.Form;
import simplenlg.features.Person;
import simplenlg.framework.InflectedWordElement;
import simplenlg.framework.LexicalCategory;
import simplenlg.framework.WordElement;
import simplenlg.lexicon.Lexicon;
import simplenlg.realiser.english.Realiser;

import com.google.common.collect.ImmutableSet;

public class ACEVerb {

	private static Lexicon sLexicon = Lexicon.getDefaultLexicon();
	private static Realiser sRealiser = new Realiser(sLexicon);

	private final WordElement mWord;
	private boolean isPrepositional = false;
	private String preposition = "";
	private String end = "";

	// English prepositions.
	// This list is used to morphologically analyze verbs like `live-in'.
	// Note that 'of' is not among these prepositions.	
	private static final ImmutableSet<String> prepositions = ImmutableSet.of(
			"aboard", "about", "above", "across", "after", "against", "along", "alongside", "amid", "among", "amongst", "around", "as", "at",
			"before", "behind", "below", "beneath", "beside", "between", "beyond",
			"despite", "down", "during",
			"for", "from", "in", "inside", "into",
			"near",
			"off", "on", "onto", "out", "outside", "over",
			"past",
			"through", "throughout", "till", "to", "toward", "towards",
			"under", "until", "up", "upon",
			"via",
			"with", "within", "without");


	/**
	 * <p>Splits the string into pieces on the bases of separators (-, _, +);
	 * scans from the end to the beginning until to the piece that is not a preposition;
	 * this position is the border of the string.</p>
	 * 
	 * TODO: BUG: better start scanning from the beginning to support phrases
	 * like: fall-in-love-with (falls/fallen-in-love-with)
	 */
	public ACEVerb(String lemma) {
		String lemmaWithoutPreposition = lemma;
		String [] pieces = lemma.split("[+_-]");

		int last = 0;

		for (int i = pieces.length - 1; i >= 0; i--) {
			if (prepositions.contains(pieces[i])) {
				isPrepositional = true;
			} else {
				last = i;
				break;
			}
		}

		if (isPrepositional) {
			int lemmaLength = last;
			for (int j = 0; j <= last; j++) {
				lemmaLength += pieces[j].length();
			}
			lemmaWithoutPreposition = lemma.substring(0, lemmaLength);
			// BUG: We do this check to avoid "String index out of range"
			// in case the input lemma IS a preposition (e.g. `from').
			if (lemma.length() != lemmaLength) {
				preposition = lemma.substring(lemmaLength + 1);
				end = lemma.substring(lemmaLength);
			}
		}

		// mWord = (WordElement) sNlgFactory.createWord(lemmaWithoutPreposition, LexicalCategory.VERB);
		mWord = sLexicon.lookupWord(lemmaWithoutPreposition, LexicalCategory.VERB);
	}

	public boolean isPrepositional() {
		return isPrepositional;
	}


	/**
	 * <p>Returns the preposition of this verb. Returns an empty
	 * string if the verb is not prepositional.</p>
	 * 
	 * @return preposition
	 */
	public String getPreposition() {
		return preposition;
	}


	public String getPresent3SG() {
		if (isPrepositional()) {
			return makePresent3SG(mWord) + end;
		}
		return makePresent3SG(mWord);
	}


	public String getPastParticiple() {
		if (isPrepositional()) {
			return makePastParticiple(mWord) + end;
		}
		return makePastParticiple(mWord);
	}

	private static String makePresent3SG(WordElement word) {
		InflectedWordElement inflectedWord = new InflectedWordElement(word);
		inflectedWord.setFeature(Feature.PERSON, Person.THIRD);
		return sRealiser.realise(inflectedWord).getRealisation();
	}

	private static String makePastParticiple(WordElement word) {
		InflectedWordElement inflectedWord = new InflectedWordElement(word);
		inflectedWord.setFeature(Feature.FORM, Form.PAST_PARTICIPLE);
		return sRealiser.realise(inflectedWord).getRealisation();
	}
}