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

package ch.uzh.ifi.attempto.aceview.lexicon;

import java.util.Collection;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.ape.Lexicon;
import ch.uzh.ifi.attempto.ape.LexiconEntry;

public class TokenMapperImpl implements TokenMapper {

	private static final Logger logger = Logger.getLogger(TokenMapperImpl.class);

	private final Multimap<String, Triple> map = HashMultimap.create();
	private final Multimap<IRI, Triple> map2 = HashMultimap.create();

	private final Autocompleter ac;
	private int ambiguousWordformCount = 0;
	private int cnCount = 0;
	private int pnCount = 0;
	private int tvCount = 0;

	private int cnSgCount = 0;
	private int cnPlCount = 0;
	private int pnSgCount = 0;
	private int tvSgCount = 0;
	private int tvPlCount = 0;
	private int tvVbgCount = 0;

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

			// If there are now exactly 2 triples for the
			// same wordform then this wordform has become ambiguous.
			if (map.get(wordform).size() == 2) {
				ambiguousWordformCount++;
			}

			switch (MorphType.getMorphType(morphIRI)) {
			case PN_SG:
				pnSgCount++; break;
			case CN_SG:
				cnSgCount++; break;
			case CN_PL:
				cnPlCount++; break;
			case TV_SG:
				tvSgCount++; break;
			case TV_PL:
				tvPlCount++; break;
			case TV_VBG:
				tvVbgCount++; break;
			default:
				throw new RuntimeException("Programmer expected PN_SG/CN_SG/CN_PL/TV_SG/TV_PL/TV_VBG");
			}

			/*
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
			 */
			logger.info("Added: " + wordform + " -> " + triple);
		}
	}


	public void removeEntry(String wordform, IRI lemma, IRI morphIRI) {
		// TODO: BUG: check that the triple is in the map
		if (MorphType.isMorphTypeIRI(morphIRI)) {

			// If there are currently exactly 2 triples for the
			// same wordform then this wordform will become unambiguous.
			if (map.get(wordform).size() == 2) {
				ambiguousWordformCount--;
			}

			Triple triple = new Triple(lemma, morphIRI, wordform);
			map.remove(wordform, triple);
			map2.remove(lemma, triple);
			if (! map.containsKey(wordform)) {
				ac.remove(wordform);
				logger.info("Removed: " + wordform + " -> " + triple + " (no tokens remaining)");
			}
			else {
				logger.info("Removed: " + wordform + " -> " + triple);
			}


			switch (MorphType.getMorphType(morphIRI)) {
			case PN_SG:
				pnSgCount--; break;
			case CN_SG:
				cnSgCount--; break;
			case CN_PL:
				cnPlCount--; break;
			case TV_SG:
				tvSgCount--; break;
			case TV_PL:
				tvPlCount--; break;
			case TV_VBG:
				tvVbgCount--; break;
			default:
				throw new RuntimeException("Programmer expected PN_SG/CN_SG/CN_PL/TV_SG/TV_PL/TV_VBG");
			}

			/*
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
			 */
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
		int allCount = pnSgCount + cnSgCount + cnPlCount + tvSgCount + tvPlCount + tvVbgCount;
		int mapSize = map.size();
		if (allCount == mapSize) {
			return mapSize;
		}

		// This should never happen
		return -1234;
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


	public IRI getWordformIRI(String wordform) {
		Collection<Triple> triples = map.get(wordform);
		if (triples.isEmpty() || triples.size() > 1) {
			return null;
		}
		return triples.iterator().next().getSubjectIRI();
	}


	public String getWordform(IRI entityIRI, IRI morphIRI) {
		for (Triple t : map2.get(entityIRI)) {
			if (t.hasProperty(morphIRI)) {
				return t.getObject();
			}
		}
		return null;
	}


	public int getWordformPnSgCount() {
		return pnSgCount;
	}


	public int getWordformCnSgCount() {
		return cnSgCount;
	}


	public int getWordformCnPlCount() {
		return cnPlCount;
	}


	public int getWordformTvSgCount() {
		return tvSgCount;
	}


	public int getWordformTvPlCount() {
		return tvPlCount;
	}


	public int getWordformTvVbgCount() {
		return tvVbgCount;
	}
}