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

import java.net.URI;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.semanticweb.owlapi.model.OWLAnnotation;

import com.google.common.collect.Lists;

public class AnnotationsTableModel extends AbstractTableModel {

	private List<OWLAnnotation> comments = Lists.newArrayList();

	public enum Column implements TableColumn {
		VALUE("Value", null, true, String.class),
		URI("URI", null, true, URI.class),
		CLASS("Class", null, false, String.class);

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
		return comments.size();
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
	// TODO: rename the URI-field
	public Object getValueAt(int row, int column) {
		if (row >= 0 && row < comments.size()) {
			switch (Column.values()[column]) {
			case VALUE:
				return comments.get(row).getValue();
			case URI:
				return comments.get(row).getProperty();
			case CLASS:
				return comments.get(row).getClass().getSimpleName();
			default:
				// BUG: throw something instead?
				return "NULL";
			}
		}
		// TODO: throw exception instead
		return "NULL";
	}


	public void setComments(List<OWLAnnotation> comments) {
		if (comments != null) {
			this.comments = comments;
			fireTableDataChanged();
		}
	}
}