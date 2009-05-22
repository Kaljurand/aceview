package ch.uzh.ifi.attempto.ace;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * <p>ACE tokenizer</p>
 * 
 * TODO: This tokenizer incorrectly tokenizes "John sees 42. John sees Mary." into
 * [John, sees, 42, John, sees, Mary, .], i.e. it removes the period of the first
 * sentences, thinking that it is a part of the number. It seems quite impossible
 * to fix this problem.
 * 
 * <p>Note that we do not represent quotation marks as separate tokens,
 * we just record that certain tokens are quoted strings.</p>
 * 
 * @author Kaarel Kaljurand
 */
public class ACETokenizer {

	public static List<ACEToken> tokenize(String str) {
		Reader r = new StringReader(str);
		StreamTokenizer t = new StreamTokenizer(r);

		t.eolIsSignificant(true);

		// We override some default values
		t.ordinaryChar('/');
		t.ordinaryChar('\'');
		t.ordinaryChar('.');

		// ACE supports Perl-style comments
		t.commentChar('#');
		// ACE supports quoted strings
		t.quoteChar('"');
		// ACE supports C-style comments
		t.slashStarComments(true);

		t.wordChars('_', '_');
		t.wordChars('$', '$');

		List<ACEToken> tokens = Lists.newArrayList();
		int newLineCounter = 0;
		boolean textHasStarted = false;

		try {
			while (t.nextToken() != StreamTokenizer.TT_EOF) {
				if (t.ttype == StreamTokenizer.TT_EOL) {
					newLineCounter++;
					continue;
				}

				if (textHasStarted && newLineCounter > 1) {
					tokens.add(ACEToken.PARA);
				}
				newLineCounter = 0;
				textHasStarted = true;

				if (t.ttype == StreamTokenizer.TT_WORD) {
					tokens.add(ACEToken.newToken(t.sval));
				}
				else if (t.ttype == '"') {
					tokens.add(ACEToken.newQuotedString(t.sval));
				}
				else if (t.ttype == StreamTokenizer.TT_NUMBER) {
					tokens.add(ACEToken.newNumber(t.nval));
				}
				else if (t.ttype == '.') {
					tokens.add(ACEToken.DOT);
				}
				else if (t.ttype == '?'  || t.ttype == '!') {
					tokens.add(ACEToken.newBorderToken((char) t.ttype));
				}
				else if (t.ttype == '\'' || t.ttype == ',' || t.ttype == ':' ||
						t.ttype == '+' || t.ttype == '-' || t.ttype == '*' || t.ttype == '/' ||
						t.ttype == '=' || t.ttype == '(' || t.ttype == ')') {
					tokens.add(ACEToken.newSymbol((char) t.ttype));
				}
				else {
					tokens.add(ACEToken.newBadToken((char) t.ttype));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return tokens;
	}
}