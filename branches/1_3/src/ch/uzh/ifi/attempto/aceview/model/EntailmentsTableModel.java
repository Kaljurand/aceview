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

import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import ch.uzh.ifi.attempto.aceview.ACESnippet;


public class EntailmentsTableModel extends AbstractTableModel {

	private final List<ACESnippet> snippets = Lists.newArrayList();
	private final Map<ACESnippet, Integer> snippetToCounter = Maps.newHashMap();

	private int counter = 0;

	public enum Column implements TableColumn {
		SNIPPET("Snippet", "SN", true, ACESnippet.class),
		TIMESTAMP("Timestamp", "TS", true, Integer.class);

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

	@Override
	public Class<?> getColumnClass(int column) {
		return Column.values()[column].getDataClass();
	}

	public int getColumnCount() {
		return Column.values().length;
	}

	public int getRowCount() {
		return snippets.size();
	}

	@Override
	public String getColumnName(int column) {
		if (column >= getColumnCount()) {
			// BUG: I guess this can never happen.
			return "NULL";
		}
		return Column.values()[column].getName();
	}

	// BUG: we ignore the column, otherwise there were refreshing problems
	public Object getValueAt(int row, int column) {
		if (row >= 0 && row < snippets.size()) {
			switch (Column.values()[column]) {
			case SNIPPET:
				return snippets.get(row);
			case TIMESTAMP:
				return snippetToCounter.get(snippets.get(row)) - counter;
			default:
				// BUG: throw something instead?
				return "NULL";
			}
		}
		// TODO: throw exception instead
		return "NULL";
	}

	public void addSnippet(ACESnippet snippet) {
		snippetToCounter.put(snippet, counter);
		if (snippets.contains(snippet)) {
			fireTableCellUpdated(snippets.indexOf(snippet), 1);
		}
		else {
			snippets.add(snippet);
			fireTableCellUpdated(snippets.size() - 1, 0);
		}
	}

	public void increaseCounter() {
		counter++;
	}
}