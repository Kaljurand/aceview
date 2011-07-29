/*
 * This file is part of ACE View.
 * Copyright 2008-2011, Attempto Group, University of Zurich (see http://attempto.ifi.uzh.ch).
 *
 * ACE View is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * ACE View is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with ACE View.
 * If not, see http://www.gnu.org/licenses/.
 */

package ch.uzh.ifi.attempto.ace;

import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;


/**
 * <p>Pretty-prints the snippet by placing each sentence on a separate line, and
 * pretty-printing each sentence.
 * Pretty-prints each {@link ACESentence} by using line-breaks and indentation.</p>
 * 
 * <p>TODO: We want to increase the indentation when encountering
 * a relative clause pronoun (that, which, who, whose). The problem
 * is that many of these pronouns are used also as question pronouns
 * in which case we do not want to increase the indentation. The current
 * solution is not correct and complete. This kind of pretty-printing
 * is best done if the syntax tree is given as input.</p>
 * 
 * <p>TODO: Glue apostrophes and commas to the preceding word.</p>
 * 
 * @author Kaarel Kaljurand
 */
public class ACESentenceRenderer {

	private final Multimap<Integer, Integer> hl = HashMultimap.create();
	private final Multimap<Integer, Integer> spans;
	private final StringBuilder sb = new StringBuilder();
	private final ACETokenRenderer mTokenRenderer;

	private final static String TS = "    ";

	// Five levels of indentation are supported. Deeper structures should not be used anyway.
	private final static String[] INDENT_STRINGS = {
		"\n",
		"\n" + TS,
		"\n" + TS + TS,
		"\n" + TS + TS + TS,
		"\n" + TS + TS + TS + TS,
		"\n" + TS + TS + TS + TS + TS
	};


	public ACESentenceRenderer(ACETokenRenderer tokenRenderer, List<ACESentence> sentences, Multimap<Integer, Integer> spans) {
		this.mTokenRenderer = tokenRenderer;
		this.spans = spans;
		render(sentences);
	}

	public ACESentenceRenderer(ACETokenRenderer tokenRenderer, List<ACESentence> sentences) {
		this.mTokenRenderer = tokenRenderer;
		spans = null;
		render(sentences);
	}

	public String getRendering() {
		return sb.toString();
	}

	public Multimap<Integer, Integer> getSpans() {
		return hl;
	}


	private void render(List<ACESentence> sentences) {
		int lastIndex = sentences.size() - 1;
		int i = 0;

		for (ACESentence sentence : sentences) {
			renderSentence(sentence, i);
			if (i != lastIndex) {
				sb.append('\n');
			}
			i++;
		}
	}


	private String renderSentence(ACESentence sentence, int sId) {
		int indentLevel = 0;
		for (int i = 0; i < sentence.size(); i++) {
			ACEToken tok = sentence.getTokens().get(i);
			String tokStr = tok.toString();
			boolean isQuestion = sentence.isQuestion();
			boolean tokenIsRelClPronoun = tokStr.equals("that") ||
			(tokStr.equals("whose") && ! isQuestion) ||
			(tokStr.equals("which") && ! isQuestion) ||
			(tokStr.equals("who") && ! isQuestion);

			if (tokenIsRelClPronoun) {
				indentLevel++;
			}

			if (tokStr.equals("then")) {
				// BUG: this should also apply to the "invisible then-part border" of the every/no-sentences.
				// As there is unfortunately no explicit token standing on this border, we would
				// have to process the syntax tree to derive this information.
				sb.append('\n');
				appendToken(sId, i, tok);
				indentLevel = 0;
			}
			else if (i > 0 && tok.isOrdinationWord()) {
				sb.append(getIndentString(indentLevel));
				appendToken(sId, i, tok);
				if (indentLevel > 0) {
					indentLevel--;
				}
			}
			else if (tokenIsRelClPronoun) {
				sb.append(getIndentString(indentLevel));
				appendToken(sId, i, tok);
			}
			else {
				if (i > 0) {
					sb.append(' ');
				}
				appendToken(sId, i, tok);
			}
		}
		return sb.toString();
	}


	private static String getIndentString(int indentLevel) {
		if (indentLevel < INDENT_STRINGS.length) {
			return INDENT_STRINGS[indentLevel];
		}
		return INDENT_STRINGS[INDENT_STRINGS.length - 1];
	}


	private void appendToken(int sId, int tId, ACEToken token) {
		if (spans != null && spans.containsEntry(sId, tId)) {
			int before = sb.length();
			sb.append(mTokenRenderer.render(token));
			int after = sb.length();
			hl.put(before, after);
		}
		else {
			sb.append(mTokenRenderer.render(token));
		}
	}
}
