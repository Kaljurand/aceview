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

package ch.uzh.ifi.attempto.ace;

/**
 * 
 * @author Kaarel Kaljurand
 *
 */
public enum EntryType {
	CN("Noun", "N", "noun"),
	TV("Verb", "V", "trverb"),
	PN("Name", "P", "propername");

	private final String name;
	private final String abbr;
	private final String awType;

	private EntryType(String name, String abbr, String awType) {
		this.name = name;
		this.abbr = abbr;
		this.awType = awType;
	}

	public String getName() {
		return name;
	}

	public String toAbbr() {
		return abbr;
	}

	public String toAceWikiType() {
		return awType;
	}
}