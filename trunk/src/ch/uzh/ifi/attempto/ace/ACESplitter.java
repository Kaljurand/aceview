package ch.uzh.ifi.attempto.ace;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.List;

import com.google.common.collect.Lists;

public class ACESplitter {

	public static List<List<ACESentence>> getParagraphs(String str) {
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
		List<ACESentence> sentences = Lists.newArrayList();
		List<List<ACESentence>> paragraphs = Lists.newArrayList();

		int newLineCounter = 0;
		boolean textHasStarted = false;

		try {
			while (t.nextToken() != StreamTokenizer.TT_EOF) {
				if (t.ttype == StreamTokenizer.TT_EOL) {
					newLineCounter++;
					continue;
				}


				if (textHasStarted && newLineCounter > 1) {
					updateParagraphs(paragraphs, sentences, tokens);
					sentences = Lists.newArrayList();
				}

				newLineCounter = 0;
				textHasStarted = true;

				ACEToken tok;

				if (t.ttype == StreamTokenizer.TT_WORD) {
					tok = ACEToken.newToken(t.sval);
				}
				else if (t.ttype == '"') {
					tok = ACEToken.newQuotedString(t.sval);
				}
				else if (t.ttype == StreamTokenizer.TT_NUMBER) {
					tok = ACEToken.newNumber(t.nval);
				}
				else if (t.ttype == '.') {
					tok = ACEToken.DOT;
				}
				else if (t.ttype == '?'  || t.ttype == '!') {
					tok = ACEToken.newBorderToken((char) t.ttype);
				}
				else if (t.ttype == '\'' || t.ttype == ',' || t.ttype == ':' ||
						t.ttype == '+' || t.ttype == '-' || t.ttype == '*' || t.ttype == '/' ||
						t.ttype == '=' || t.ttype == '(' || t.ttype == ')') {
					tok = ACEToken.newSymbol((char) t.ttype);
				}
				else {
					tok = ACEToken.newBadToken((char) t.ttype);
				}

				if (tok.isBorderToken()) {
					updateSentences(sentences, tokens, tok);
				}
				else {
					tokens.add(tok);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		updateParagraphs(paragraphs, sentences, tokens);

		return paragraphs;
	}


	private static void updateSentences(List<ACESentence> sentences, List<ACEToken> tokens, ACEToken tok) {
		tokens.add(tok);
		sentences.add(new ACESentence(tokens));
		tokens.clear();
	}


	private static void updateParagraphs(List<List<ACESentence>> paragraphs, List<ACESentence> sentences, List<ACEToken> tokens) {
		if (! tokens.isEmpty()) {
			updateSentences(sentences, tokens, ACEToken.DOT);
		}
		paragraphs.add(sentences);	
	}
}