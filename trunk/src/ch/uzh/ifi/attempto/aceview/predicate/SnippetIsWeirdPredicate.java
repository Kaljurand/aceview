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

import com.google.common.base.Predicate;

import ch.uzh.ifi.attempto.aceview.ACESnippet;

public class SnippetIsWeirdPredicate implements Predicate<ACESnippet> {

	/**
	 * <p>True if this snippet represents a class axiom
	 * whose superclass argument is equal to <code>owl:Thing</code>.</p>
	 * 
	 * <p>Note that we used to consider "weird" also snippets which contain
	 * <code>owl:Thing</code> but this
	 * is now commented out as it is probably misleading.</p>
	 */
	public boolean apply(ACESnippet snippet) {
		if (snippet != null) {
			return snippet.isUnsatisfiable();
			//return (snippet.isUnsatisfiable() || snippet.isEqualToThing());
		}
		return false;
	}
}