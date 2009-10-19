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

import java.util.Set;

import javax.swing.table.AbstractTableModel;

import ch.uzh.ifi.attempto.aceview.ACESnippet;

public class SimilarSnippetsTableModel extends AbstractTableModel {

	private Object[] snippetArray = new Object[0];

	public int getColumnCount() {
		return 1;
	}

	public int getRowCount() {
		return snippetArray.length;
	}

	public Object getValueAt(int row, int column) {
		if (0 <= row && row < snippetArray.length) {
			return snippetArray[row];
		}
		return "NULL";
	}

	@Override
	public String getColumnName(int column) {
		return "Snippet";
	}

	public void setData(Set<ACESnippet> snippets) {
		snippetArray = snippets.toArray();
		fireTableDataChanged();
	}
}