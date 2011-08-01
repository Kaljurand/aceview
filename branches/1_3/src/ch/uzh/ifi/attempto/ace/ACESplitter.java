package ch.uzh.ifi.attempto.ace;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ch.uzh.ifi.attempto.aceview.lexicon.TokenMapper;
import ch.uzh.ifi.attempto.aceview.lexicon.Triple;

import com.google.common.collect.Lists;

public class ACESplitter {

	private static final Logger logger = Logger.getLogger(ACESplitter.class);

	static final Pattern dotSeparator = Pattern.compile("([0-9])[.]([^0-9])");

	private final TokenMapper mTokenMapper;

	public ACESplitter(TokenMapper tokenMapper) {
		mTokenMapper = tokenMapper;
	}


	/**
	 * <p>Tokenizes the given string and returns the result
	 * as a list of lists of ACE sentences, where each sentence
	 * is a list of tokens. A list of sentences forms an ACE paragraph.
	 * A list of paragraphs is an ACE text.</p>
	 * 
	 * @param str ACE text as string
	 * @return ACE text as list of paragraphs (sentence lists)
	 */
	public List<List<ACESentence>> getParagraphs(String str) {

		Reader r = new StringReader(fixNumbers(str));
		StreamTokenizer t = new StreamTokenizer(r);

		t.eolIsSignificant(true);

		// We override some default values
		t.ordinaryChar('/');
		t.ordinaryChar('\'');
		t.ordinaryChar('.');

		// ACE supports Perl-style comments
		t.commentChar('#');
		// ACE supports C-style comments
		t.slashStarComments(true);

		// ACE supports quoted strings
		t.quoteChar('"');
		// ACE supports quoted words
		t.quoteChar('`');

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
					// TODO: get the IRI based on the wordfrom
					Collection<Triple> triples = mTokenMapper.getWordformEntries(t.sval);
					if (triples.isEmpty()) {
						logger.warn("Wordfrom not in lexicon: " + t.sval);
						// TODO: unknown wordform or function word
						tok = ACEToken.newToken(t.sval);
					} else if (triples.size() == 1) {
						Triple trip = triples.iterator().next();
						tok = ACEToken.newToken(trip.getSubjectIRI(), trip.getProperty().getEntryType(), trip.getProperty().getFieldType());
					} else {
						// TODO: ambiguous wordform, think what to do here,
						// currently taking the first reading, which is definitely a wrong approach.
						logger.warn("Wordfrom ambiguous in lexicon: " + t.sval + " " + triples);
						Triple trip = triples.iterator().next();
						tok = ACEToken.newToken(trip.getSubjectIRI(), trip.getProperty().getEntryType(), trip.getProperty().getFieldType());
					}
				}
				else if (t.ttype == '"') {
					tok = ACEToken.newQuotedString(t.sval);
				}
				else if (t.ttype == '`') {
					tok = ACEToken.newToken(t.sval);
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


	/**
	 * <p>Tokenizes the given string and returns the first sentence
	 * of the first paragraph of the resulting list of paragraphs.</p>
	 * 
	 * @param str ACE text as string
	 * @return ACE sentence (as a list of tokens)
	 */
	public List<ACEToken> getTokens(String str) {
		List<ACESentence> sentences = getSentences(str);
		if (sentences.isEmpty()) {
			return Lists.newArrayList();
		}
		return sentences.iterator().next().getTokens();
	}


	/**
	 * <p>Tokenizes the given string and returns the first paragraph
	 * of the resulting list of paragraphs.</p>
	 * 
	 * @param str ACE text as string
	 * @return ACE paragraph (sentence list)
	 */
	public List<ACESentence> getSentences(String str) {
		List<List<ACESentence>> paragraphs = getParagraphs(str);
		if (paragraphs.isEmpty()) {
			return Lists.newArrayList();
		}
		return paragraphs.iterator().next();
	}	


	// BUG: we fix the bug that the tokenizer has with sentences like
	// "John's age is 15." where it parses the dot as part of the number
	// (i.e. 15.0) and leaves the sentence without an end symbol.
	// It's not the best solution as it also modifies dots in strings.
	private static String fixNumbers(String str) {
		String fixedStr = dotSeparator.matcher(str).replaceAll("$1 .$2");
		return fixedStr;
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