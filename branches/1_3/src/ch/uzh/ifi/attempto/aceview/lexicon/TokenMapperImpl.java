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

import java.util.Collection;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.ape.Lexicon;
import ch.uzh.ifi.attempto.ape.LexiconEntry;

public class TokenMapperImpl implements TokenMapper {

	private static final Logger logger = Logger.getLogger(OwlApiACELexicon.class);

	private final Multimap<String, Triple> map = HashMultimap.create();
	private final Multimap<IRI, Triple> map2 = HashMultimap.create();

	private final Autocompleter ac;
	private int ambiguousWordformCount = 0;
	private int cnCount = 0;
	private int pnCount = 0;
	private int tvCount = 0;
	private int partialCount = 0;


	public TokenMapperImpl() {
		ac = new Autocompleter();
		// TODO: Maybe add all the function words to the auto-completion map.
		//ac.addAll(FunctionWords.getFunctionWords());

		// TODO: Or maybe add only the longer ones. Needs more testing, especially,
		// to find out how spaces in tokens behave.
		//ac.add("it is false that");
		//ac.add("nothing but");
		//ac.add("at least");
		//ac.add("at most");
		//ac.add("more than");
		//ac.add("less than");
	}


	public void addEntry(String wordform, IRI lemma, IRI morphIRI) {
		if (MorphType.isMorphTypeIRI(morphIRI)) {
			Triple triple = new Triple(lemma, morphIRI, wordform); 
			map.put(wordform, triple);
			map2.put(lemma, triple);
			ac.add(wordform);

			switch (MorphType.getWordClass(morphIRI)) {
			case CN:
				cnCount++; break;
			case TV:
				tvCount++; break;
			case PN:
				pnCount++; break;
			default:
				throw new RuntimeException("Programmer expected CN/TV/PN");
			}
			logger.info("Added: " + wordform + " -> " + triple);
		}
	}


	public void removeEntry(String wordform, IRI lemma, IRI morphIRI) {
		if (MorphType.isMorphTypeIRI(morphIRI)) {
			Triple triple = new Triple(lemma, morphIRI, wordform);
			map.remove(wordform, triple);
			map2.remove(lemma, triple);
			if (! map.containsKey(wordform)) {
				ac.remove(wordform);
				logger.info("Removed: " + wordform + " -> " + triple + " (no tokens remaining)");
			}
			switch (MorphType.getWordClass(morphIRI)) {
			case CN:
				cnCount--; break;
			case TV:
				tvCount--; break;
			case PN:
				pnCount--; break;
			default:
				throw new RuntimeException("Programmer expected CN/TV/PN");
			}
			logger.info("Removed: " + wordform + " -> " + triple);
		}
	}


	@Override
	public String toString() {
		return map.toString();
	}


	public String toACELexiconFormat() {
		return createLexicon().toString();		
	}


	public int size() {
		return map.size();
	}


	public boolean containsWordform(String wordform) {
		return map.containsKey(wordform);
	}


	public IRI getWordformEntity(String wordform) {
		Collection<Triple> entities = map.get(wordform);
		if (entities.isEmpty()) {
			return null;
		}
		return entities.iterator().next().getSubjectIRI();
	}


	public Autocompleter getAutocompleter() {
		return ac;
	}


	public Lexicon createLexicon() {
		Set<LexiconEntry> entries = Sets.newHashSet();
		for (Triple triple : map.values()) {
			entries.add(triple.getLexiconEntry());
		}
		Lexicon lexicon = new Lexicon();
		lexicon.addEntries(entries);
		return lexicon;
	}


	public Lexicon createLexicon(Set<String> wordforms) {
		Set<LexiconEntry> entries = Sets.newHashSet();
		for (String wordform : wordforms) {
			for (Triple triple : map.get(wordform)) {
				entries.add(triple.getLexiconEntry());
			}
		}
		Lexicon lexicon = new Lexicon();
		lexicon.addEntries(entries);
		return lexicon;
	}


	public int getAmbiguousWordformCount() {
		return ambiguousWordformCount;
	}


	/*
	// TODO: make more efficient
	public int getWordclassAmbiguousWordformCount() {
		int count = 0;
		for (String wordform : wordformToEntities.keySet()) {
			Collection<OWLEntity> entitySet = wordformToEntities.get(wordform);
			if (entitySet.size() > 1) {
				int cn = 0;
				int tv = 0;
				int pn = 0;
				for (OWLEntity entity : entitySet) {
					if (entityToEntry.get(entity) != null) {
						switch (entityToEntry.get(entity).getType()) {
						case CN:
							cn++; break;
						case TV:
							tv++; break;
						case PN:
							pn++; break;
						default:
							break;
						}
					}
				}
				if (cn > 1 || tv > 1 || pn > 1) {
					count++;
				}
			}
		}
		return count;
	}
	 */


	public int getWordformCount() {
		return map.size();
	}


	public int getCNCount() {
		return cnCount;
	}


	public int getPNCount() {
		return pnCount;
	}


	public int getTVCount() {
		return tvCount;
	}


	public int getPartialEntryCount() {
		return partialCount;
	}


	public int getWordclassAmbiguousWordformCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	public Set<OWLEntity> getWordformEntities(String string) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getWordform(IRI entityIRI, IRI morphIRI) {
		for (Triple t : map2.get(entityIRI)) {
			if (t.hasProperty(morphIRI)) {
				return t.getObject();
			}
		}
		return null;
	}
}