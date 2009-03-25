package ch.uzh.ifi.attempto.ace;

import simplenlg.lexicon.lexicalitems.Verb;

import com.google.common.collect.ImmutableSet;

public class ACEVerb {

	private final Verb verb;
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
			}
			else {
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

		verb = new Verb(lemmaWithoutPreposition);
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
			return verb.getPresent3SG() + end;
		}
		return verb.getPresent3SG();
	}


	public String getPastParticiple() {
		if (isPrepositional()) {
			return verb.getPastParticiple() + end;
		}
		return verb.getPastParticiple();
	}
}