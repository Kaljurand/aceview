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

public interface TableColumn {

	/**
	 * <p>Returns the name of this table column.</p>
	 * 
	 * @return Name as a string
	 */
	String getName();

	/**
	 * <p>Returns an alternative and shorter name
	 * of this table column.</p>
	 * 
	 * @return Abbreviation as a string
	 */
	String getAbbr();

	/**
	 * <p>Returns <code>true</code> iff this table column is
	 * supposed to be visible by default.</p>
	 * 
	 * @return <code>true</code> iff this table column is visible by default
	 */
	boolean isVisible();

	/**
	 * <p>Returns the Java class of the data that is shown in this column.</p>
	 * 
	 * @return Java class of the data that is shown in this column
	 */
	Class<?> getDataClass();
}
