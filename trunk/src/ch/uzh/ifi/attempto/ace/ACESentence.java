package ch.uzh.ifi.attempto.ace;

import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;
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

	private final Joiner joiner = Joiner.on(" ");


	public ACESentence(String str) {
		// BUG: we fix the bug that the tokenizer has with sentences like
		// "John's age is 15." where it parses the dot as part of the number
		// (i.e. 15.0) and leaves the sentence without an end symbol.
		// It's not the best solution as it also modifies dots in strings.
		this(ACETokenizer.tokenize(ACESentenceSplitter.dotSeparator.matcher(str).replaceAll(" . ")));
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
		int lastIndex = size() - 1;

		if (lastIndex == -1) {
			return "";
		}

		StringBuilder sb = new StringBuilder();

		ACEToken token = this.tokens.get(0);

		for (int i = 0; i <= lastIndex; i++) {
			sb.append(token);
			if (i != lastIndex) {
				ACEToken nextToken = this.tokens.get(i+1);
				if (!token.isApos() && !nextToken.isSymbol()) {
					sb.append(" ");
				}
				token = nextToken;
			}
		}

		return sb.toString();
	}


	public String toSimpleString() {
		return joiner.join(tokens);
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
}