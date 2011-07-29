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

import java.util.Set;

import ch.uzh.ifi.attempto.ace.FieldType;
import ch.uzh.ifi.attempto.ape.Lexicon;

/**
 * <p>ACE lexicon maps OWL entities to ACE lexicon entries.
 * Each lexicon entry assigns a type: common noun (CN), transitive verb (TV),
 * proper name (PN) to the entity, and describes the surface wordforms
 * (singular, plural, past participle) via which the entity can be
 * referred to in ACE sentences.</p>
 * 
 * @author Kaarel Kaljurand
 */
public interface ACELexicon<E> {

	/**
	 * <p>Returns the corresponding lexicon entry for the
	 * given entity. Note that the mapping of entities to
	 * entries is functional. If the lexicon does not describe
	 * this entity, then returns <code>null</code>.</p>
	 * 
	 * @param entity OWL entity
	 * @return ACE lexicon entry
	 */
	ACELexiconEntry getEntry(E entity);


	/**
	 * <p>Creates or modifies the lexicon entry for the given entity.
	 * Only one entry field (sg, pl, vbg) is changed. Both the field type
	 * and the value for the field are given as parameters.</p>
	 * 
	 * @param entity OWL entity
	 * @param field Field to be added or modified
	 * @param wordform Wordform to occupy the field
	 * @throws IncompatibleMorphTagException
	 */
	void addEntry(E entity, FieldType field, String wordform) throws IncompatibleMorphTagException;


	/**
	 * <p>Removes the given morphological mapping for the given entity.
	 * If no mappings are left then removes the complete entry from
	 * the lexicon.</p>
	 * 
	 * @param entity OWL entity
	 * @param field Field to be removed
	 * @throws IncompatibleMorphTagException
	 */
	void removeEntry(E entity, FieldType field) throws IncompatibleMorphTagException;


	/**
	 * <p>Returns <code>true</code> if the given wordform is in the lexicon,
	 * i.e. described by at least one lexicon entry. Returns <code>false</code>
	 * otherwise.</p>
	 * 
	 * @param wordform ACE wordform
	 * @return <code>true</code> if wordform is in the lexicon
	 */
	boolean containsWordform(String wordform);


	/**
	 * <p>Returns a set of OWL entities that the given wordform can
	 * refer to. For example, the wordform `leaves' could refer
	 * to a class "leaf" or an object property "leave".</p>
	 * 
	 * @param wordform ACE wordform
	 * @return Set of OWL entities that the wordform can refer to
	 */
	Set<E> getWordformEntities(String wordform);


	/**
	 * <p>Returns a set of OWL entities that the set of given wordforms can
	 * refer to.</p>
	 * 
	 * @param wordforms Set of ACE wordforms
	 * @return Set of OWL entities that the wordform can refer to
	 */
	Set<E> getWordformEntities(Set<String> wordforms);


	/**
	 * <p>Returns the {@link Autocompleter} object which can be
	 * used to autocomplete wordforms to the point where the ambiguity starts,
	 * or to retrieve the list of all possible full completions (which are
	 * wordforms registered in the lexicon.</p>
	 * 
	 * @return Auto-completer
	 */
	Autocompleter getAutocompleter();


	/**
	 * <p>Returns the number of lexicon entries in the lexicon.
	 * In other words, returns the number of entities that have a mapping
	 * to a lexicon entry in this lexicon.</p>
	 * 
	 * @return Number of lexicon entries
	 */
	int size();


	/**
	 * <p>Returns the number of CN entries in the lexicon.</p>
	 * 
	 * @return Number of CN entries
	 */
	int getCNCount();


	/**
	 * <p>Returns the number of TV entries in the lexicon.</p>
	 * 
	 * @return Number of TV entries
	 */
	int getTVCount();


	/**
	 * <p>Returns the number of PN entries in the lexicon.</p>
	 * 
	 * @return Number of PN entries
	 */
	int getPNCount();


	/**
	 * <p>Returns the number of partial (incomplete) entries in the lexicon.</p>
	 * 
	 * @return Number of partial entries
	 */
	int getPartialEntryCount();


	/**
	 * <p>Serializes the complete lexicon in ACE lexicon format.</p>
	 * 
	 * @return String in ACE lexicon format
	 */
	String toACELexiconFormat();


	/**
	 * <p>Creates the <code>Lexicon</code> on the basis of the given
	 * set of wordforms.</p>
	 * 
	 * @param contentWordforms Set of ACE wordforms
	 * @return Lexicon
	 */
	Lexicon createLexicon(Set<String> contentWordforms);


	/**
	 * <p>Creates the <code>Lexicon</code> from all the lexicon
	 * entries.</p>
	 * 
	 * @return Lexicon
	 */
	Lexicon createLexicon();


	/** <p>Returns the number of wordforms in this lexicon.</p>
	 * 
	 * @return Number of wordforms
	 */
	int getWordformCount();


	/** <p>Returns the number of wordforms that are ambiguous (i.e. can denote
	 * several different entities) according to this lexicon.</p>
	 * 
	 * @return Number of ambiguous wordforms
	 */
	int getAmbiguousWordformCount();


	/** <p>Returns the number of wordforms that are ambiguous in the
	 * same wordclass (e.g. can denote several different entities that all
	 * map to common nouns) according to this lexicon.</p>
	 * 
	 * <p>Note that this is a dangerous ambiguity which no ontology
	 * should contain.</p>
	 * 
	 * @return Number of ambiguous wordforms in the same wordclass
	 */
	int getWordclassAmbiguousWordformCount();


	/**
	 * <p>Returns the entity that corresponds to the wordform.
	 * If none correspond then <code>null</code> is returned.
	 * If more than one corresponds then an arbitrary one is returned.</p>
	 * 
	 * @param wordform ACE wordform
	 * @return OWLEntity that corresponds to the given wordform
	 */
	E getWordformEntity(String wordform);
}
