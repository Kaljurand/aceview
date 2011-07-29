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
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLEntity;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.ace.EntryType;
import ch.uzh.ifi.attempto.ace.FieldType;
import ch.uzh.ifi.attempto.ape.Gender;
import ch.uzh.ifi.attempto.ape.Lexicon;
import ch.uzh.ifi.attempto.ape.LexiconEntry;

/**
 * @deprecated
 *
 */
public class OwlApiACELexicon implements ACELexicon<OWLEntity> {

	private static final Logger logger = Logger.getLogger(OwlApiACELexicon.class);

	private final Multimap<String, OWLEntity> wordformToEntities = HashMultimap.create();
	private final Map<OWLEntity, ACELexiconEntry> entityToEntry = Maps.newHashMap();

	private final Autocompleter ac;
	private int ambiguousWordformCount = 0;
	private int cnCount = 0;
	private int pnCount = 0;
	private int tvCount = 0;
	private int partialCount = 0;


	public OwlApiACELexicon() {
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


	public ACELexiconEntry getEntry(OWLEntity entity) {
		return entityToEntry.get(entity);
	}


	// Note: wordform should not be null
	public void addEntry(OWLEntity entity, FieldType field, @NotNull String wordform) throws IncompatibleMorphTagException {
		ACELexiconEntry entry = getEntry(entity);

		if (entry == null) {
			switch (LexiconUtils.getLexiconEntryType(entity)) {
			case CN:
				entry = new NounEntry(); cnCount++; break;
			case TV:
				entry = new VerbEntry(); tvCount++; break;
			case PN:
				entry = new PropernameEntry(); pnCount++; break;
			default:
				throw new RuntimeException("Programmer expected CN/TV/PN");
			}
			entityToEntry.put(entity, entry);
			entry.set(field, wordform);
			if (entry.isPartial()) partialCount++;
		}
		else {
			boolean wasPartial = entry.isPartial();
			entry.set(field, wordform);
			if (wasPartial && ! entry.isPartial()) partialCount--;
		}

		wordformToEntities.put(wordform, entity);
		if (wordformToEntities.get(wordform).size() == 2) {
			ambiguousWordformCount++;
		}

		ac.add(wordform);
	}

	public void removeEntry(OWLEntity entity, FieldType field) throws IncompatibleMorphTagException {
		ACELexiconEntry entry = getEntry(entity);

		// Does the lexicon contain an entry for the entity? It should normally.
		if (entry != null) {
			boolean wasPartial = entry.isPartial();
			String wordform = entry.get(field);

			wordformToEntities.remove(wordform, entity);
			int size = wordformToEntities.get(wordform).size();
			if (size == 0) {
				ac.remove(wordform);
			}
			else if (size == 1) {
				ambiguousWordformCount--;
			}


			// We remove the lexicon entry.
			entityToEntry.remove(entity);
			// We modify the entry to null the respective morph annotation (sg, pl, vbg).
			logger.info("Setting to null: " + field);
			entry.set(field, null);
			// If the entry has no morph annotations left then we don't do anything as
			// it has already been removed from all the maps. Otherwise we put the entry
			// back into the lexicon, and we put it back into the wordform2entries map.
			if (entry.isEmpty()) {
				logger.info("No entries left");
				switch (LexiconUtils.getLexiconEntryType(entity)) {
				case CN:
					cnCount--; break;
				case TV:
					tvCount--; break;
				case PN:
					pnCount--; break;
				default:
					throw new RuntimeException("Programmer expected CN/TV/PN");
				}
				if (wasPartial) partialCount--;
			}
			else {
				entityToEntry.put(entity, entry);
				if (! wasPartial) partialCount++;
			}
		}
		else {
			logger.warn("Lexicon does not contain the key `" + entity + "' whose feature `" + field + "' is being removed.");
		}
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (ACELexiconEntry lexEntry : entityToEntry.values()) {
			sb.append(lexEntry);
			sb.append('\n');
		}
		return sb.toString();
	}


	public String toACELexiconFormat() {
		return createLexicon().toString();		
	}


	public int size() {
		return entityToEntry.size();
	}


	public boolean containsWordform(String wordform) {
		return wordformToEntities.containsKey(wordform);
	}


	public OWLEntity getWordformEntity(String wordform) {
		Collection<OWLEntity> entities = wordformToEntities.get(wordform);
		if (entities.isEmpty()) {
			return null;
		}
		return entities.iterator().next();
	}


	public Set<OWLEntity> getWordformEntities(String wordform) {
		return (Set<OWLEntity>) wordformToEntities.get(wordform);
	}


	public Set<OWLEntity> getWordformEntities(Set<String> wordforms) {
		Set<OWLEntity> entries = Sets.newHashSet();
		for (String wordform : wordforms) {
			entries.addAll(wordformToEntities.get(wordform));
		}
		return entries;
	}


	public Autocompleter getAutocompleter() {
		return ac;
	}


	public Lexicon createLexicon() {
		return createLexiconFromEntities(entityToEntry.keySet());
	}


	public Lexicon createLexicon(Set<String> contentWordForms) {
		return createLexiconFromEntities(getWordformEntities(contentWordForms));
	}


	private Lexicon createLexiconFromEntities(Set<OWLEntity> entities) {
		Set<LexiconEntry> entries = Sets.newHashSet();

		for (OWLEntity entity : entities) {
			// TODO: try using URI as lemma, this would be more correct
			String lemma = entity.toString();
			//String lemma = entity.getURI().toString();
			ACELexiconEntry aceLexiconEntry = getEntry(entity);
			EntryType type = aceLexiconEntry.getType();

			if (type == EntryType.CN) {
				String sg = aceLexiconEntry.getSg();
				String pl = aceLexiconEntry.getPl();
				if (sg != null) {
					entries.add(LexiconEntry.createNounSgEntry(sg, lemma, Gender.NEUTRAL));
				}
				if (pl != null) {
					entries.add(LexiconEntry.createNounPlEntry(pl, lemma, Gender.NEUTRAL));
				}
			}
			else if (type == EntryType.TV) {
				String sg = aceLexiconEntry.getSg();
				String pl = aceLexiconEntry.getPl();
				String vbg = aceLexiconEntry.getVbg();
				if (sg != null) {
					entries.add(LexiconEntry.createTrVerbThirdEntry(sg, lemma));
				}
				if (pl != null) {
					entries.add(LexiconEntry.createTrVerbInfEntry(pl, lemma));
				}
				if (vbg != null) {
					entries.add(LexiconEntry.createTrVerbPPEntry(vbg, lemma));
				}
			}
			else if (type == EntryType.PN) {
				String sg = aceLexiconEntry.getSg();
				if (sg != null) {
					entries.add(LexiconEntry.createPropernameSgEntry(sg, lemma, Gender.NEUTRAL));
				}
			}
		}
		Lexicon lexicon = new Lexicon();
		lexicon.addEntries(entries);
		return lexicon;
	}


	public int getAmbiguousWordformCount() {
		return ambiguousWordformCount;
	}


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


	public int getWordformCount() {
		return wordformToEntities.size();
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
}