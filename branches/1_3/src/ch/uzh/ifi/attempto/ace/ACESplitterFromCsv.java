package ch.uzh.ifi.attempto.ace;

import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class ACESplitterFromCsv {

	/**
	 * <p>Tokenizes the given string and returns the result
	 * as a list of lists of ACE sentences, where each sentence
	 * is a list of tokens. A list of sentences forms an ACE paragraph.
	 * A list of paragraphs is an ACE text.</p>
	 * 
	 * @param str ACE text as string
	 * @return ACE text as list of paragraphs (sentence lists)
	 * 
	 * TODO: number and symbols
	 */
	public static List<List<ACESentence>> getParagraphs(String str) {

		List<ACEToken> tokens = Lists.newArrayList();
		List<ACESentence> sentences = Lists.newArrayList();
		List<List<ACESentence>> paragraphs = Lists.newArrayList();

		boolean textHasStarted = false;

		Scanner scanner = new Scanner(str);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (textHasStarted && line.length() == 0) {
				updateParagraphs(paragraphs, sentences, tokens);
				sentences = Lists.newArrayList();
			} else {
				textHasStarted = true;
				ACEToken tok = getToken(line);

				if (tok == null) {
					continue;
				}

				if (tok.isBorderToken()) {
					updateSentences(sentences, tokens, tok);
				} else {
					tokens.add(tok);
				}
			}
		}

		updateParagraphs(paragraphs, sentences, tokens);
		return paragraphs;
	}


	/**
	 * TODO: remove: getTokens
	 * <p>Tokenizes the given string and returns the first sentence
	 * of the first paragraph of the resulting list of paragraphs.</p>
	 * 
	 * @param str ACE text as string
	 * @return ACE sentence (as a list of tokens)
	 */
	/*
	public static List<ACEToken> getTokens(String str) {
		List<ACESentence> sentences = getSentences(str);
		if (sentences.isEmpty()) {
			return Lists.newArrayList();
		}
		return sentences.iterator().next().getTokens();
	}
	 */


	/**
	 * <p>Tokenizes the given string and returns the first paragraph
	 * of the resulting list of paragraphs.</p>
	 * 
	 * @param str ACE text as string
	 * @return ACE paragraph (sentence list)
	 */
	public static List<ACESentence> getSentences(String str) {
		List<List<ACESentence>> paragraphs = getParagraphs(str);
		if (paragraphs.isEmpty()) {
			return Lists.newArrayList();
		}
		return paragraphs.iterator().next();
	}


	private static ACEToken getToken(String line) {
		Iterable<String> it = Splitter.on('\t').split(line);
		Iterator<String> itt = it.iterator();
		String type = itt.next();
		String word = itt.next();
		System.out.println(type + " --- " + word);

		ACEToken tok;
		if ("qs".equals(type)) {
			tok = ACEToken.newQuotedString(word);
		} else if (".".equals(word)) {
			tok = ACEToken.DOT;
		} else if ("?".equals(word)) {
			tok = ACEToken.newBorderToken('?');
		} else if ("!".equals(word)) {
			tok = ACEToken.newBorderToken('!');
		} else if ("cn_sg".equals(type)) {
			tok = ACEToken.newToken(word, EntryType.CN, FieldType.SG);
		} else if ("cn_pl".equals(type)) {
			tok = ACEToken.newToken(word, EntryType.CN, FieldType.PL);
		} else if ("tv_sg".equals(type)) {
			tok = ACEToken.newToken(word, EntryType.TV, FieldType.SG);
		} else if ("tv_pl".equals(type)) {
			tok = ACEToken.newToken(word, EntryType.TV, FieldType.SG);
		} else if ("tv_vbg".equals(type)) {
			tok = ACEToken.newToken(word, EntryType.TV, FieldType.PL);
		} else if ("pn_sg".equals(type)) {
			tok = ACEToken.newToken(word, EntryType.PN, FieldType.VBG);
		} else if ("ignored".equals(type)) {
			return null;
		} else if ("unsupported".equals(type)) {
			return null;
		} else if ("comment".equals(type)) {
			return null;
		} else {
			// TODO: capture bad tokens
			tok = ACEToken.newToken(word);
		}
		/*
		else if (t.ttype == StreamTokenizer.TT_NUMBER) {
			tok = ACEToken.newNumber(t.nval);
		}
		else if (t.ttype == '\'' || t.ttype == ',' || t.ttype == ':' ||
				t.ttype == '+' || t.ttype == '-' || t.ttype == '*' || t.ttype == '/' ||
				t.ttype == '=' || t.ttype == '(' || t.ttype == ')') {
			tok = ACEToken.newSymbol((char) t.ttype);
		}
		 */
		return tok;
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
		if (! sentences.isEmpty()) {
			paragraphs.add(sentences);
		}
	}
}