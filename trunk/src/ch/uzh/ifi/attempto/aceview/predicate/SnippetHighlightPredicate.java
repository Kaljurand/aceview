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

import com.google.common.base.Predicate;

import ch.uzh.ifi.attempto.aceview.ACESnippet;

public class SnippetHighlightPredicate implements HighlightPredicate {

	private final Predicate<ACESnippet> predicate;
	private final int column;

	/**
	 * @param predicate Snippet predicate
	 * @param column Column index in model
	 */
	public SnippetHighlightPredicate(Predicate<ACESnippet> predicate, int column) {
		this.predicate = predicate;
		this.column = column;
	}

	public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
		int modelColumn = adapter.viewToModel(adapter.column);
		if (modelColumn == column) {
			Object value = adapter.getFilteredValueAt(adapter.row, column);
			return predicate.apply((ACESnippet) value);
		}
		return false;
	}
}