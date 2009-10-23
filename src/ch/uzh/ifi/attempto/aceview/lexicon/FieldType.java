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
	private final IRI uri;

	private FieldType(String name, String strURI) {
		this.name = name;
		this.uri = IRI.create(strURI);
	}

	public String getName() {
		return name;
	}

	public IRI getIRI() {
		return uri;
	}

	/**
	 * <p>Returns the {@link FieldType} that corresponds to the given URI.</p>
	 * 
	 * @param uri URI
	 * @return {@link FieldType} of the given URI
	 */
	public static FieldType getField(IRI uri) {
		if (uri.equals(SG.getIRI())) {
			return SG;
		}
		else if (uri.equals(PL.getIRI())) {
			return PL;
		}
		else if (uri.equals(VBG.getIRI())) {
			return VBG;
		}
		// BUG: throw an exception instead, e.g. UnsupportedURIException
		return SG;
	}

	public static boolean isLexiconEntryIRI(IRI uri) {
		return (uri.equals(SG.getIRI()) || uri.equals(PL.getIRI()) || uri.equals(VBG.getIRI()));
	}
}