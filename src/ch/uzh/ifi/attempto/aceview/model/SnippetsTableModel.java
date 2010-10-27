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

import java.util.Date;

import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewListener;
import ch.uzh.ifi.attempto.aceview.model.event.TextEventType;

/**
 * @author Kaarel Kaljurand
 */
public class SnippetsTableModel extends AbstractSnippetsTableModel {

	public enum Column implements TableColumn {
		SNIPPET("Snippet", "S", true, ACESnippet.class),
		CONTENT_WORDS("Words", "W", true, Integer.class),
		MESSAGES("Messages", "M", false, Integer.class),
		AXIOMS("Axioms", "A", false, Integer.class),
		SHARED("Shared", "S", false, Integer.class),
		TIMESTAMP("Timestamp", "T", false, Date.class),
		NAMESPACE("Namespace", "NS", false, String.class),
		ANNOTATIONS("Annotations", "An", false, Integer.class);

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


	public SnippetsTableModel() {
		snippets = ACETextManager.getActiveACEText().getSnippets();
		aceTextManagerListener = new ACEViewListener<ACEViewEvent<TextEventType>>() {
			public void handleChange(ACEViewEvent<TextEventType> event) {
				snippets = ACETextManager.getActiveACEText().getSnippets();
				// BUG: return something more precise here
				fireTableDataChanged();
			}
		};
		ACETextManager.addListener(aceTextManagerListener);
	}


	@Override
	public int getSnippetColumn() {
		return Column.SNIPPET.ordinal();
	}


	public int getColumnCount() {
		return Column.values().length;
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
		case AXIOMS:
			return snippet.getLogicalAxioms().size();
		case SHARED:
			return ACETextManager.getActiveACEText().getSharedAxioms(snippet).size();
		case MESSAGES:
			return snippet.getMessages().size();
		case CONTENT_WORDS:
			return snippet.getContentWordCount();
		case TIMESTAMP:
			return snippet.getTimestamp();
		case NAMESPACE:
			return snippet.getDefaultNamespace();
		case ANNOTATIONS:
			return ACETextManager.getAnnotationsExceptAcetext(snippet).size();
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