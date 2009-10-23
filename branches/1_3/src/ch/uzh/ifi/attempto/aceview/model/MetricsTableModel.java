/*
 * This file is part of ACE View.
 * Copyright 2008-2009, Attempto Group, University of Zurich (see http://attempto.ifi.uzh.ch).
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
import ch.uzh.ifi.attempto.aceview.model.event.ACETextChangeEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACETextManagerListener;

public class MetricsTableModel extends AbstractTableModel {

	private ACEText acetext = ACETextManager.getActiveACEText();

	private final ACETextManagerListener aceTextManagerListener = new ACETextManagerListener() {
		public void handleChange(ACETextChangeEvent event) {
			acetext = ACETextManager.getActiveACEText();
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
		CONTENT_WORD_COUNT("Content words (CN + TV + PN)"),
		CN_COUNT("Common nouns (CN)"),
		TV_COUNT("Transitive verbs (TV)"),
		PN_COUNT("Proper names (PN)"),
		UNUSED_CONTENT_WORD_COUNT("Unused content words"),
		WORDFORM_COUNT("Wordforms"),
		AMBIGUOUS_WORDFORM_COUNT("Ambiguous wordforms"),
		WORDCLASS_AMBIGUOUS_WORDFORM_COUNT("Ambiguous wordforms in the same wordclass"),
		PARTIAL_ENTRY_COUNT("Incomplete lexicon entries");

		private final String name;

		private Row(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public static int getCount(ACEText acetext, int row) {
			switch (values()[row]) {
			case SNIPPET_COUNT:
				return acetext.size();
			case SENTENCE_COUNT:
				return acetext.getSentences().size();
			case QUESTION_COUNT:
				return acetext.getQuestions().size();
			case CONTENT_WORD_COUNT:
				return acetext.getTokenMapper().size();
			case CN_COUNT:
				return acetext.getTokenMapper().getCNCount();
			case TV_COUNT:
				return acetext.getTokenMapper().getTVCount();
			case PN_COUNT:
				return acetext.getTokenMapper().getPNCount();
			case NOTHING_BUT_COUNT:
				return acetext.getNothingbutCount();
			case SWRL_SNIPPET_COUNT:
				return acetext.getRuleCount();
			case NON_OWLSWRL_SNIPPET_COUNT:
				return acetext.getUnparsedCount();
			case UNVERBALIZED_AXIOM_COUNT:
				return acetext.getUnverbalizedCount();
			case UNUSED_CONTENT_WORD_COUNT:
				return (acetext.getTokenMapper().size() - acetext.getReferencedEntities().size());
			case WORDFORM_COUNT:
				return acetext.getTokenMapper().getWordformCount();
			case AMBIGUOUS_WORDFORM_COUNT:
				return acetext.getTokenMapper().getAmbiguousWordformCount();
			case WORDCLASS_AMBIGUOUS_WORDFORM_COUNT:
				return acetext.getTokenMapper().getWordclassAmbiguousWordformCount();
			case PARTIAL_ENTRY_COUNT:
				return acetext.getTokenMapper().getPartialEntryCount();
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
				return Row.getCount(acetext, row);
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