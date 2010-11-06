/*
 * This file is part of ACE View.
 * Copyright 2008-2010, Attempto Group, University of Zurich (see http://attempto.ifi.uzh.ch).
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

package ch.uzh.ifi.attempto.aceview.model;

import javax.swing.table.AbstractTableModel;

import ch.uzh.ifi.attempto.aceview.ACEText;
import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.lexicon.TokenMapper;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewListener;
import ch.uzh.ifi.attempto.aceview.model.event.TextEventType;

public class MetricsTableModel extends AbstractTableModel {

	private ACEText acetext = ACETextManager.getActiveACEText();
	private TokenMapper tokenMapper = ACETextManager.getActiveACELexicon();

	private final ACEViewListener<ACEViewEvent<TextEventType>> aceTextManagerListener = new ACEViewListener<ACEViewEvent<TextEventType>>() {
		public void handleChange(ACEViewEvent<TextEventType> event) {
			acetext = ACETextManager.getActiveACEText();
			tokenMapper = ACETextManager.getActiveACELexicon();
			fireTableDataChanged();
		}
	};

	private enum Row {
		SNIPPET_COUNT("Snippets"),
		SENTENCE_COUNT("Sentences"),
		QUESTION_COUNT("Questions"),
		SWRL_SNIPPET_COUNT("SWRL snippets"),
		NON_OWLSWRL_SNIPPET_COUNT("Non OWL/SWRL snippets"),
		UNVERBALIZED_AXIOM_COUNT("Unverbalized axioms"),
		NOTHING_BUT_COUNT("<html>Snippets that contain <i>nothing but</i></html>"),
		CONTENT_WORD_COUNT("!Content words (CN + TV + PN)"),
		CN_COUNT("!Common nouns (CN)"),
		TV_COUNT("!Transitive verbs (TV)"),
		PN_COUNT("!Proper names (PN)"),
		UNUSED_CONTENT_WORD_COUNT("!Unused content words"),
		WORDFORM_COUNT("Wordforms"),
		WORDFORM_PN_SG_COUNT("<html><code>PN_sg</code></html>"),
		WORDFORM_CN_SG_COUNT("<html><code>CN_sg</code></html>"),
		WORDFORM_CN_PL_COUNT("<html><code>CN_pl</code></html>"),
		WORDFORM_TV_SG_COUNT("<html><code>TV_sg</code></html>"),
		WORDFORM_TV_PL_COUNT("<html><code>TV_pl</code></html>"),
		WORDFORM_TV_VBG_COUNT("<html><code>TV_vbg</code></html>"),
		AMBIGUOUS_WORDFORM_COUNT("Ambiguous wordforms"),
		WORDCLASS_AMBIGUOUS_WORDFORM_COUNT("!Ambiguous wordforms in the same wordclass"),
		PARTIAL_ENTRY_COUNT("!Incomplete lexicon entries");

		private final String name;

		private Row(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public static int getCount(ACEText acetext, TokenMapper tokenMapper, int row) {
			switch (values()[row]) {
			case SNIPPET_COUNT:
				return acetext.size();
			case SENTENCE_COUNT:
				return acetext.getSentences().size();
			case QUESTION_COUNT:
				return acetext.getQuestions().size();
			case CONTENT_WORD_COUNT:
				return tokenMapper.size();
			case CN_COUNT:
				return tokenMapper.getCNCount();
			case TV_COUNT:
				return tokenMapper.getTVCount();
			case PN_COUNT:
				return tokenMapper.getPNCount();
			case NOTHING_BUT_COUNT:
				return acetext.getNothingbutCount();
			case SWRL_SNIPPET_COUNT:
				return acetext.getRuleCount();
			case NON_OWLSWRL_SNIPPET_COUNT:
				return acetext.getUnparsedCount();
			case UNVERBALIZED_AXIOM_COUNT:
				return acetext.getUnverbalizedCount();
			case UNUSED_CONTENT_WORD_COUNT:
				return (tokenMapper.size() - acetext.getReferencedEntities().size());
			case WORDFORM_COUNT:
				return tokenMapper.getWordformCount();
			case WORDFORM_PN_SG_COUNT:
				return tokenMapper.getWordformPnSgCount();
			case WORDFORM_CN_SG_COUNT:
				return tokenMapper.getWordformCnSgCount();
			case WORDFORM_CN_PL_COUNT:
				return tokenMapper.getWordformCnPlCount();
			case WORDFORM_TV_SG_COUNT:
				return tokenMapper.getWordformTvSgCount();
			case WORDFORM_TV_PL_COUNT:
				return tokenMapper.getWordformTvPlCount();
			case WORDFORM_TV_VBG_COUNT:
				return tokenMapper.getWordformTvVbgCount();
			case AMBIGUOUS_WORDFORM_COUNT:
				return tokenMapper.getAmbiguousWordformCount();
			case WORDCLASS_AMBIGUOUS_WORDFORM_COUNT:
				return tokenMapper.getWordclassAmbiguousWordformCount();
			case PARTIAL_ENTRY_COUNT:
				return tokenMapper.getPartialEntryCount();
			default:
				throw new RuntimeException("Programmer error.");
			}
		}
	}


	public enum Column implements TableColumn {
		METRIC("Metric", null, true, String.class),
		COUNT("Count", null, true, Integer.class);

		private final String name;
		private final String abbr;
		private final Class<?> dataClass;
		private final boolean isVisible;

		private Column(String name, String abbr, boolean isVisible, Class<?> dataClass) {
			this.name = name;
			this.abbr = abbr;
			this.isVisible = isVisible;
			this.dataClass = dataClass;
		}

		public String getName() {
			return name;
		}

		public String getAbbr() {
			return abbr;
		}

		public Class<?> getDataClass() {
			return dataClass;
		}

		public boolean isVisible() {
			return isVisible;
		}
	}


	public MetricsTableModel() {
		ACETextManager.addListener(aceTextManagerListener);
	}

	@Override
	public Class<?> getColumnClass(int column) {
		return Column.values()[column].getDataClass();
	}

	public int getColumnCount() {
		return Column.values().length;
	}

	public int getRowCount() {
		return Row.values().length;
	}

	@Override
	public String getColumnName(int column) {
		return Column.values()[column].getName();
	}

	public Object getValueAt(int row, int column) {
		if (row >= 0 && row < getRowCount()) {
			switch (Column.values()[column]) {
			case METRIC:
				return Row.values()[row].getName();
			case COUNT:
				return Row.getCount(acetext, tokenMapper, row);
			default:
				throw new RuntimeException("Programmer error.");
			}
		}
		// TODO: throw exception instead
		return "NULL";
	}


	public void dispose() {
		ACETextManager.removeListener(aceTextManagerListener);
	}
}