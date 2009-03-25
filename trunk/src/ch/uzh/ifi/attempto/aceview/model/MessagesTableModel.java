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

import javax.swing.table.AbstractTableModel;

import com.google.common.collect.Lists;

import ch.uzh.ifi.attempto.ape.Message;


public class MessagesTableModel extends AbstractTableModel {

	private List<Message> messages = Lists.newArrayList();

	public enum Column implements TableColumn {
		IMPORTANCE("Importance", "I", false, Boolean.class),
		TYPE("Type", "T", true, String.class),
		SENTENCE_ID("Sentence", "S", true, Integer.class),
		TOKEN_ID("Token", "T", true, Integer.class),
		VALUE("Value", "V", true, String.class),
		REPAIR("Repair", "R", true, String.class);

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
		return messages.size();
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
		if (row >= 0 && row < messages.size()) {
			switch (Column.values()[column]) {
			case IMPORTANCE:
				return messages.get(row).isError();
			case TYPE:
				return messages.get(row).getType();
			case SENTENCE_ID:
				return messages.get(row).getSentenceId();
			case TOKEN_ID:
				return messages.get(row).getTokenId();
			case VALUE:
				return messages.get(row).getValue();
			case REPAIR:
				return messages.get(row).getRepair();
			default:
				// BUG: throw something instead?
				return "NULL";
			}
		}
		// TODO: throw exception instead
		return "NULL";
	}


	public void setMessages(List<Message> messages) {
		if (messages != null) {
			this.messages = messages;
			fireTableDataChanged();
		}
	}
}