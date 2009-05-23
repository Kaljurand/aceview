package ch.uzh.ifi.attempto.ace;

import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

public class ACESentenceSplitter {

	static final Pattern dotSeparator = Pattern.compile("[.]([ \b\t\n\f\r]|$)");

	public static List<ACESentence> splitSentences(String str) {

		// BUG: we fix the bug that the tokenizer has with sentences like
		// "John's age is 15." where it parses the dot as part of the number
		// (i.e. 15.0) and leaves the sentence without an end symbol.
		// It's not the best solution as it also modifies dots in strings.
		String fixedString = dotSeparator.matcher(str).replaceAll(" . ");

		List<ACEToken> tokens = ACETokenizer.tokenize(fixedString);

		if (tokens.isEmpty()) {
			return Lists.newArrayList();
		}

		List<ACEToken> tmp = Lists.newArrayList();
		List<ACESentence> sentences = Lists.newArrayList();

		for (ACEToken token : tokens) {
			tmp.add(token);
			if (token.isBorderToken()) {
				sentences.add(new ACESentence(tmp));
				tmp.clear();
			}
		}

		// In case the token-stream does not end with a border symbol then
		// we assume that the user forgot to add a period at the end of the
		// sentence(s). We will automatically add it in order to avoid
		// the confusing disappearance of the (partial) sentence.
		if (! tmp.isEmpty()) {
			tmp.add(ACEToken.DOT);
			sentences.add(new ACESentence(tmp));
		}

		return sentences;
	}
}