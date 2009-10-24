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

import org.semanticweb.owlapi.model.IRI;

/**
 * 
 * @author Kaarel Kaljurand
 *
 */
public enum FieldType {
	SG("Singular", "http://attempto.ifi.uzh.ch/ace_lexicon#sg"),
	PL("Plural", "http://attempto.ifi.uzh.ch/ace_lexicon#pl"),
	VBG("P. participle", "http://attempto.ifi.uzh.ch/ace_lexicon#vbg");

	private final String name;
	private final IRI iri;

	private FieldType(String name, String iriAsString) {
		this.name = name;
		this.iri = IRI.create(iriAsString);
	}

	public String getName() {
		return name;
	}

	public IRI getIRI() {
		return iri;
	}

	/**
	 * <p>Returns the {@link FieldType} that corresponds to the given URI.</p>
	 * 
	 * @param iri IRI
	 * @return {@link FieldType} of the given IRI
	 */
	public static FieldType getField(IRI iri) {
		if (iri.equals(SG.getIRI())) {
			return SG;
		}
		else if (iri.equals(PL.getIRI())) {
			return PL;
		}
		else if (iri.equals(VBG.getIRI())) {
			return VBG;
		}
		// BUG: throw an exception instead, e.g. UnsupportedURIException
		return SG;
	}

	public static boolean isLexiconEntryIRI(IRI iri) {
		return (iri.equals(SG.getIRI()) || iri.equals(PL.getIRI()) || iri.equals(VBG.getIRI()));
	}
}