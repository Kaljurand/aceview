package ch.uzh.ifi.attempto.ace;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.IriRenderer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * <p>Every ACE sentence is a list of tokens.</p>
 * 
 * <p>TODO: Make all the fields immutable.</p>
 * 
 * @author Kaarel Kaljurand
 */
public class ACESentence {

	private final ImmutableList<ACEToken> tokens;
	private final Set<ACEToken> badChars = Sets.newHashSet();
	private final List<ACEToken> contentWords = Lists.newArrayList();
	private boolean isNothingbut = false;
	private boolean isQuestion = false;

	// TODO: remove this method, or at least do not call getActiveACELexicon
	public ACESentence(String str) {
		this(new ACESplitter(ACETextManager.getActiveACELexicon()).getTokens(str));
	}

	public ACESentence(List<ACEToken> tokens) {
		this.tokens = ImmutableList.copyOf(tokens);

		for (ACEToken token : this.tokens) {
			if (token.isButToken()) {
				isNothingbut = true;
			}
			else if (token.isQuestionMark()) {
				isQuestion = true;
			}
			else if (token.isBadToken()) {
				badChars.add(token);
			}
			else if (token.isContentWord()) {
				contentWords.add(token);
			}
		}
	}


	public List<ACEToken> getTokens() {
		return tokens;
	}


	public int size() {
		return tokens.size();
	}


	/**
	 * <p>Pretty-prints the ACE sentence so that the punctuation marks
	 * are attached to the preceding tokens.</p>
	 * 
	 * @return String representation of this sentence
	 */
	@Override
	public String toString() {
		return createString(this.tokens);
	}


	public Set<ACEToken> getBadChars() {
		return badChars;
	}

	/**
	 * <p>Returns the list of all the content words in this sentence.
	 * The words are returned in the order as they appear in the
	 * sentence, with multiple occurrences of the same word
	 * present multiple times.</p>
	 * 
	 * @return List of all content words in this sentence
	 */
	public List<ACEToken> getContentWords() {
		return contentWords;
	}


	public boolean isNothingbut() {
		return isNothingbut;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if ((obj == null) || (obj.getClass() != this.getClass())) return false;
		ACESentence s = (ACESentence) obj;

		return tokens.equals(s.getTokens());
	}


	@Override
	public int hashCode() {
		return tokens.hashCode();
	}


	public boolean isQuestion() {
		return isQuestion;
	}


	/**
	 * <p>We just drop the last token which is a sentence end marker.</p>
	 *
	 * @return Manchester OWL Syntax string
	 */
	public String toMOSString() {
		if (size() < 2) {
			return "";
		}

		StringBuilder sb = new StringBuilder();

		int lastIndex = size() - 2;

		int counter = 0;
		for (ACEToken token : tokens) {
			if (counter > lastIndex) {
				break;
			}
			sb.append(token);
			sb.append(' ');
			counter++;
		}

		return sb.toString();
	}


	/**
	 * <p>Pretty-prints the ACE tokens so that the punctuation marks
	 * are attached to the preceding tokens. The resulting
	 * string is for example:</p>
	 * 
	 * <pre>
	 * John's pet is a cat or knows Jonas' pet, and is an animal.
	 * </pre>
	 * 
	 * @param tokens List of ACE tokens
	 * @return Pretty-printed token list
	 */
	private static String createString(List<ACEToken> tokens) {
		IriRenderer renderer = new IriRenderer(ACETextManager.getActiveACELexicon());

		if (tokens.isEmpty()) {
			return "";
		}

		StringBuilder sb = new StringBuilder();

		Iterator<ACEToken> it = tokens.iterator();
		ACEToken token = it.next();

		while (it.hasNext()) {
			sb.append(renderer.render(token));
			ACEToken nextToken = it.next();
			if (! (nextToken.isSymbol() || token.isApos() && nextToken.toString().equals("s"))) {
				sb.append(' ');
			}
			token = nextToken;
		}
		sb.append(token);

		return sb.toString();
	}
}