/*
 * This file is part of ACE View.
 * Copyright 2008-2010, Attempto Group, University of Zurich (see http://attempto.ifi.uzh.ch).
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

package ch.uzh.ifi.attempto.aceview.util;

import java.util.Comparator;

import org.semanticweb.owlapi.model.OWLEntity;

import ch.uzh.ifi.attempto.aceview.ACETextManager;

public class EntityComparator implements Comparator<OWLEntity> {

	public int compare(OWLEntity e1, OWLEntity e2) {
		if (e1.equals(e2)) {
			return 0;
		}

		String r1 = getRenderingAndClass(e1);
		String r2 = getRenderingAndClass(e2);

		int c = r1.compareTo(r2);

		if (c == 0) {
			return 1;
		}
		return c;
	}


	private String getRenderingAndClass(OWLEntity entity) {
		return ACETextManager.getRendering(entity).toLowerCase() + "_" + entity.getClass();
	}
}