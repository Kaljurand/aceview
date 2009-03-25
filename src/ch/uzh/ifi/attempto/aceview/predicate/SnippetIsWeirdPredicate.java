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

package ch.uzh.ifi.attempto.aceview.predicate;

import java.awt.Component;

import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

import ch.uzh.ifi.attempto.aceview.ACESnippet;

public class SnippetIsWeirdPredicate implements HighlightPredicate {

	private final int column;

	public SnippetIsWeirdPredicate(int column) {
		this.column = column;
	}

	/**
	 * <p>Decides if the snippet should be highlighted.
	 * A snippet should be highlighted if represents a class axiom
	 * whose superclass argument is equal to <code>owl:Thing</code>.</p>
	 * 
	 * <p>Note that we used to highlight also snippets which contain
	 * <code>owl:Thing</code> but this
	 * is now commented out as it is probably misleading.</p>
	 */
	public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
		int modelColumn = adapter.viewToModel(adapter.column);
		if (modelColumn == column) {
			Object value = adapter.getFilteredValueAt(adapter.row, column);
			if (value instanceof ACESnippet) {
				ACESnippet snippet = (ACESnippet) value;
				return snippet.isUnsatisfiable();
				//return (snippet.isUnsatisfiable() || snippet.isEqualToThing());
			}
		}
		return false;
	}
}