package ch.uzh.ifi.attempto.ace;

import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.semanticweb.owlapi.model.IRI;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

public class ACESplitterFromCsv {

	private static ImmutableSet<String> sSymbols = ImmutableSet.of("'", ",", ":", "+", "-", "*", "/", "=", "(", ")");

	/**
	 * <p>Tokenizes the given string and returns the result
	 * as a list of lists of ACE sentences, where each sentence
	 * is a list of tokens. A list of sentences forms an ACE paragraph.
	 * A list of paragraphs is an ACE text.</p>
	 * 
	 * @param str ACE text as string
	 * @return ACE text as list of paragraphs (sentence lists)
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
		//System.out.println(type + " --- " + word);

		if ("qs".equals(type)) return ACEToken.newQuotedString(word);
		if ("cn_sg".equals(type)) return ACEToken.newToken(IRI.create(word), EntryType.CN, FieldType.SG);
		if ("cn_pl".equals(type)) return ACEToken.newToken(IRI.create(word), EntryType.CN, FieldType.PL);
		if ("tv_sg".equals(type)) return ACEToken.newToken(IRI.create(word), EntryType.TV, FieldType.SG);
		if ("tv_pl".equals(type)) return ACEToken.newToken(IRI.create(word), EntryType.TV, FieldType.PL);
		if ("tv_vbg".equals(type)) return ACEToken.newToken(IRI.create(word), EntryType.TV, FieldType.VBG);
		if ("pn_sg".equals(type)) return ACEToken.newToken(IRI.create(word), EntryType.PN, FieldType.SG);

		if ("f".equals(type)) {
			if (".".equals(word)) return ACEToken.DOT;
			if ("?".equals(word)) return ACEToken.newBorderToken('?');
			if ("!".equals(word)) return ACEToken.newBorderToken('!');

			if (sSymbols.contains(word)) {
				return ACEToken.newSymbol(word);
			}

			// If the token parses as number then we return it as a number token,
			// otherwise we continue in this method.
			try {
				Float.valueOf(word);
				return ACEToken.newNumber(word);
			} catch (NumberFormatException e) {}

			// TODO: capture bad tokens
			return ACEToken.newToken(word);
		}

		// unsupported, ignored, comment, ... -> null
		return null;
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