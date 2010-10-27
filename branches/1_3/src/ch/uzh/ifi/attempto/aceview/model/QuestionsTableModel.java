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

import ch.uzh.ifi.attempto.aceview.ACEAnswer;
import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACEText;
import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewListener;
import ch.uzh.ifi.attempto.aceview.model.event.TextEventType;

/**
 * @author Kaarel Kaljurand
 */
public class QuestionsTableModel extends AbstractSnippetsTableModel {

	private ACEText<?, ?> acetext;

	public enum Column implements TableColumn {
		SNIPPET("Question", "Q", true, ACESnippet.class),
		INDIVIDUALS("Individuals", "I", true, Integer.class),
		INDIVIDUALS_COMPLETE("Icomp?", "Ic?", false, Boolean.class),
		SUBCLASSES("Sub classes", "B", true, Integer.class),
		SUBCLASSES_COMPLETE("Bcomp?", "Bc?", false, Boolean.class),
		SUPERCLASSES("Super classes", "P", true, Integer.class);

		private final String name;
		private final String abbr;
		private final boolean isVisible;
		private final Class<?> dataClass;

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

		public boolean isVisible() {
			return isVisible;
		}

		public boolean isNumeric() {
			return dataClass.equals(Integer.class);
		}

		public Class<?> getDataClass() {
			return dataClass;
		}
	}


	public QuestionsTableModel() {
		acetext = ACETextManager.getActiveACEText();
		snippets = acetext.getQuestions();
		aceTextManagerListener = new ACEViewListener<ACEViewEvent<TextEventType>>() {
			public void handleChange(ACEViewEvent<TextEventType> event) {
				acetext = ACETextManager.getActiveACEText();
				snippets = acetext.getQuestions();
				fireTableDataChanged();
			}
		};
		ACETextManager.addListener(aceTextManagerListener);
	}


	public ACEText getACEText() {
		return acetext;
	}


	@Override
	public int getSnippetColumn() {
		return Column.SNIPPET.ordinal();
	}


	public int getColumnCount() {
		return Column.values().length;
	}

	@Override
	public Class<?> getColumnClass(int column) {
		return Column.values()[column].getDataClass();
	}

	public Object getValueAt(int row, int column) {
		if (row < 0 || row > snippets.size() - 1) {
			return "NULL";
		}

		ACESnippet snippet = snippets.get(row);

		if (snippet == null) {
			return "NULL";
		}


		switch (Column.values()[column]) {
		case SNIPPET:
			return snippet;
		case INDIVIDUALS:
			ACEAnswer answer1 = acetext.getAnswer(snippet);
			if (answer1 == null) return "?";
			if (answer1.getIndividualsCount() == -1) return "";
			return answer1.getIndividualsCount();
		case INDIVIDUALS_COMPLETE:
			ACEAnswer answer1c = acetext.getAnswer(snippet);
			if (answer1c == null) return false;
			if (answer1c.getIndividualsCount() == -1) return false;
			return answer1c.isIndividualAnswersComplete();
		case SUBCLASSES:
			ACEAnswer answer2 = acetext.getAnswer(snippet);
			if (answer2 == null) return "?";
			if (answer2.getSubClassesCount() == -1) return "";
			return answer2.getSubClassesCount();
		case SUBCLASSES_COMPLETE:
			ACEAnswer answer2c = acetext.getAnswer(snippet);
			if (answer2c == null) return false;
			if (answer2c.getSubClassesCount() == -1) return false;
			return answer2c.isSubClassesAnswersComplete();
		case SUPERCLASSES:
			ACEAnswer answer3 = acetext.getAnswer(snippet);
			if (answer3 == null) return "?";
			if (answer3.getSuperClassesCount() == -1) return "";
			return answer3.getSuperClassesCount();
		default:
			throw new RuntimeException("Programmer error.");
		}
	}


	@Override
	public String getColumnName(int column) {
		if (column >= getColumnCount()) {
			throw new RuntimeException("Programmer error.");
		}
		return Column.values()[column].getName();
	}
}