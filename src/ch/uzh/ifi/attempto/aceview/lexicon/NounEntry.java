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

package ch.uzh.ifi.attempto.aceview.lexicon;

import ch.uzh.ifi.attempto.ace.EntryType;
import ch.uzh.ifi.attempto.ace.FieldType;

public class NounEntry extends AbstractEntry {

	public NounEntry() {}

	@Override
	public String get(FieldType fieldType) throws IncompatibleMorphTagException {		
		switch (fieldType) {
		case SG:
			return sg;
		case PL:
			return pl;
		default:
			throw new IncompatibleMorphTagException(EntryType.CN, fieldType);
		}
	}

	@Override
	public void set(FieldType fieldType, String fieldValue) throws IncompatibleMorphTagException {		
		switch (fieldType) {
		case SG:
			sg = fieldValue; break;
		case PL:
			pl = fieldValue; break;
		default:
			throw new IncompatibleMorphTagException(EntryType.CN, fieldType);
		}
	}

	@Override
	public boolean isEmpty() {
		return (sg == null && pl == null);
	}

	@Override
	public boolean isPartial() {
		return (sg == null || pl == null);
	}

	@Override
	public EntryType getType() {
		return EntryType.CN;
	}

	@Override
	public String toAceWikiFormat() {
		return sg + ";" + pl + ";";
	}


	@Override
	public FieldType getFieldType(String str) {
		if (str.equals(sg)) {
			return FieldType.SG;
		}
		else if (str.equals(pl)) {
			return FieldType.PL;
		}
		else {
			return null;
		}
	}
}