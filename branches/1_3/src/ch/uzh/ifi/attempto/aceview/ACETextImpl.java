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

package ch.uzh.ifi.attempto.aceview;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.ace.ACESentence;
import ch.uzh.ifi.attempto.aceview.lexicon.LexiconUtils;
import ch.uzh.ifi.attempto.aceview.lexicon.TokenMapper;
import ch.uzh.ifi.attempto.aceview.lexicon.TokenMapperImpl;
import ch.uzh.ifi.attempto.aceview.util.EntityComparator;
import ch.uzh.ifi.attempto.aceview.util.Showing;

public class ACETextImpl implements ACEText<OWLEntity, OWLLogicalAxiom> {

	private static final Logger logger = Logger.getLogger(ACETextImpl.class);

	// List of snippets in this ACE text.
	// TODO: It should actually be an "ordered set", or "list without duplicates",
	// but the important thing is that we want to access the snippets by index.
	private final List<ACESnippet> snippetList = Lists.newArrayList();

	// Maps every OWL entity to a set of ACE snippets that contain a word that corresponds to the entity. 
	private final SortedMap<OWLEntity, Set<ACESnippet>> entityToSnippets = new TreeMap<OWLEntity, Set<ACESnippet>>(new EntityComparator());
	// May it's better to do the sorting in the caller, i.e. define here:
	// private final Map<OWLEntity, Set<ACESnippet>> entityToSnippets = new HashMap<OWLEntity, Set<ACESnippet>>();

	// Maps every OWL axiom to a set of ACE snippets that correspond to the axiom.
	// TODO: clarify the meaning of this
	private final Multimap<OWLLogicalAxiom, ACESnippet> axiomToSnippets = HashMultimap.create();

	// Maps every ACE sentence to a set of snippets that contain the sentence.
	// Note that an ACE text is a set of snippets but some additions/removals operate with sentences.
	private final Multimap<ACESentence, ACESnippet> sentenceToSnippets = HashMultimap.create();

	private final Map<ACESnippet, ACEAnswer> questionToAnswer = Maps.newHashMap();

	// List of questions in this ACE text.
	private final List<ACESnippet> questions = Lists.newArrayList();

	// The ACE lexicon that decides the surface forms of the snippets in this text.
	private TokenMapper lexicon = null;

	// Number of ACE snippets that reference one or more SWRL rules
	private int ruleCount = 0;

	private int nothingbutCount = 0;

	private int emptySnippetCount = 0;

	private int axiomlessSnippetCount = 0;

	private final Joiner snippetJoiner = Joiner.on("\n\n");


	public ACETextImpl() {
		lexicon = new TokenMapperImpl();
	}


	public void add(ACESnippet snippet) {
		if (! contains(snippet)) {
			registerSnippet(snippet);
			snippetList.add(snippet);
		}
	}


	public void add(int index, ACESnippet snippet) {
		if (! contains(snippet)) {
			registerSnippet(snippet);
			snippetList.add(index, snippet);
		}
	}


	public int indexOf(ACESnippet snippet) {
		return snippetList.indexOf(snippet);
	}


	public void setAnswer(ACESnippet question, ACEAnswer answer) {
		questionToAnswer.put(question, answer);
	}


	public ACEAnswer getAnswer(ACESnippet question) {
		return questionToAnswer.get(question);
	}


	/**
	 * <p>Registers the snippet in several global tables of the
	 * ACE text, such as the mapping from entities to snippets.</p>
	 * 
	 * @param snippet The snippet to be registered.
	 */
	private void registerSnippet(ACESnippet snippet) {		

		Set<OWLLogicalAxiom> snippetAxioms = snippet.getLogicalAxioms();

		if (snippetAxioms.isEmpty()) {
			axiomlessSnippetCount++;
		}
		else {
			if (! snippet.getRules().isEmpty()) {
				ruleCount++;
			}
			for (OWLLogicalAxiom axiom : snippetAxioms) {
				for (OWLEntity entity : axiom.getSignature()) {
					if (Showing.isShow(entity)) {
						Set<ACESnippet> snippets = entityToSnippets.get(entity);
						if (snippets == null) {
							snippets = Sets.newHashSet();
							entityToSnippets.put(entity, snippets);
						}
						snippets.add(snippet);
					}
				}
				axiomToSnippets.put(axiom, snippet);
			}
		}

		for (ACESentence sentence : snippet.getSentences()) {
			sentenceToSnippets.put(sentence, snippet);
			if (sentence.isNothingbut()) {
				nothingbutCount++;
			}
		}

		if (snippet.isQuestion()) {
			questions.add(snippet);
		}

		if (snippet.isEmpty()) {
			emptySnippetCount++;
		}
	}


	public Set<OWLLogicalAxiom> getSharedAxioms(ACESnippet snippet) {

		Set<OWLLogicalAxiom> sharedAxioms = Sets.newHashSet();
		int sizeComparison = 0;

		if (contains(snippet)) {
			sizeComparison = 1;
		}

		for (OWLLogicalAxiom axiom : snippet.getLogicalAxioms()) {
			Set<ACESnippet> snippetSet = (Set<ACESnippet>) axiomToSnippets.get(axiom);
			if (snippetSet.size() > sizeComparison) {
				sharedAxioms.add(axiom);
			}
		}

		return sharedAxioms;
	}


	public Set<OWLLogicalAxiom> remove(ACESnippet snippet) {
		if (snippet.isEmpty()) {
			emptySnippetCount--;
		}

		if (snippet.isQuestion()) {
			questions.remove(snippet);
		}

		for (ACESentence sentence : snippet.getSentences()) {
			sentenceToSnippets.remove(sentence, snippet);

			if (sentence.isNothingbut()) {
				nothingbutCount--;
			}
		}

		Set<OWLLogicalAxiom> snippetAxioms = snippet.getLogicalAxioms();

		// A set of axioms to be removed from the ontology as a result
		// of removing this snippet.
		Set<OWLLogicalAxiom> removedAxioms = Sets.newHashSet();

		if (snippetAxioms.isEmpty()) {
			axiomlessSnippetCount--;
		}
		else {
			if (! snippet.getRules().isEmpty()) {
				ruleCount--;
			}
			for (OWLLogicalAxiom axiom : snippetAxioms) {
				for (OWLEntity entity : axiom.getSignature()) {
					if (Showing.isShow(entity)) {
						Set<ACESnippet> snippets = entityToSnippets.get(entity);
						if (snippets == null) {
							logger.error("Lemma `" + entity + "' not found in hash!");
						}
						else {
							snippets.remove(snippet);
							if (snippets.isEmpty()) {
								entityToSnippets.remove(entity);
							}
						}
					}
				}

				axiomToSnippets.remove(axiom, snippet);
				if (axiomToSnippets.containsKey(axiom)) {
					logger.info("Axiom preserved, other snippets account for it: " + axiomToSnippets.get(axiom));
				}
				else {
					removedAxioms.add(axiom);
				}
			}
		}

		snippetList.remove(snippet);
		return removedAxioms;
	}


	/*
	 * TODO: create this during construction time. Actually it's not
	 * so easy because indexes change. So this needs to be done after
	 * every add/remove.
	 */
	public Map<ACESnippet, Integer> getAxiomlessSnippets() {
		int index = 0;
		Map<ACESnippet, Integer> snippetToIndex = Maps.newHashMap();
		for (ACESnippet snippet : snippetList) {
			if (! snippet.hasAxioms()) {
				snippetToIndex.put(snippet, index);
			}
			index++;
		}
		return snippetToIndex;
	}


	public int getUnparsedCount() {
		return axiomlessSnippetCount ;
	}


	/**
	 * <p>Removes all given snippets from this text and
	 * returns the set of axioms that were left over, i.e.
	 * are not referenced by any remaining snippet.</p>
	 * 
	 * @param snippets Set of ACE snippets
	 * @return Set of unreferenced OWL axioms
	 */
	private Set<OWLLogicalAxiom> removeAll(Set<ACESnippet> snippets) {
		Set<OWLLogicalAxiom> removedAxioms = Sets.newHashSet();
		for (ACESnippet snippet : snippets) {
			removedAxioms.addAll(remove(snippet));
		}
		return removedAxioms;
	}


	public boolean containsAxiom(OWLLogicalAxiom ax) {
		return axiomToSnippets.containsKey(ax.getAxiomWithoutAnnotations());
	}


	public Set<ACESnippet> getAxiomSnippets(OWLLogicalAxiom axiom) {
		return (Set<ACESnippet>) axiomToSnippets.get((OWLLogicalAxiom) axiom.getAxiomWithoutAnnotations());
	}


	// TODO: set it during snippet add/remove
	@Override
	public String toString() {
		return snippetJoiner.join(snippetList);
	}


	public Set<OWLEntity> getReferencedEntities() {
		return entityToSnippets.keySet();
	}


	// TODO: nothing calls it
	public Set<Entry<OWLEntity, Set<ACESnippet>>> getEntitySnippetSetPairs() {
		return entityToSnippets.entrySet();
	}


	public int getRuleCount() {
		return ruleCount;
	}


	public int getUnverbalizedCount() {
		return emptySnippetCount;
	}


	public int getNothingbutCount() {
		return nothingbutCount;
	}


	public String getIndexBody() {
		StringBuilder html = new StringBuilder();
		for (Map.Entry<OWLEntity, Set<ACESnippet>> entry : entityToSnippets.entrySet()) {
			OWLEntity entity = entry.getKey();
			String entityRendering = ACETextManager.getRendering(entity);
			Set<ACESnippet> snippets = entry.getValue();
			html.append("<p><strong><a name='");
			html.append(LexiconUtils.getHrefId(entity));
			html.append("'>");
			html.append(entityRendering);
			html.append("</a></strong> (");
			html.append(snippets.size());
			html.append(")</p>\n");
			html.append(snippetsToHtml(snippets, lexicon));
		}
		return html.toString();
	}


	public String getIndexEntry(OWLEntity entity) {
		Set<ACESnippet> snippets = entityToSnippets.get(entity);
		if (snippets == null) {
			return null;
		}
		SortedSet<ACESnippet> snippetsSorted = new TreeSet<ACESnippet>(new SnippetComparator());
		snippetsSorted.addAll(snippets);
		return snippetsToHtml(snippetsSorted, lexicon);
	}


	/**
	 * <p>Generates an HTML list on the basis of a set of snippets.</p>
	 * 
	 * @param snippets Set of ACE snippets
	 * @return <code>String</code> representing an HTML list
	 */
	private static String snippetsToHtml(Set<ACESnippet> snippets, TokenMapper lexicon) {
		if (snippets.isEmpty()) {
			return "<em>No snippets.</em>";
		}
		StringBuilder sb = new StringBuilder();
		for (ACESnippet snippet : snippets) {
			sb.append("<p>");
			sb.append(snippet.toHtmlString(lexicon));
			sb.append(' ');
			sb.append(snippet.getTags());
			sb.append("</p>");
		}
		return sb.toString();
	}


	public boolean containsSentence(ACESentence sentence) {
		return sentenceToSnippets.containsKey(sentence);
	}


	public boolean contains(List<ACESentence> sentences) {
		if (sentences.isEmpty()) {
			return false;
		}
		ACESentence sent = sentences.iterator().next();
		for (ACESnippet s : sentenceToSnippets.get(sent)) {
			if (s.getSentences().equals(sentences)) {
				return true;
			}
		}
		return false;
	}


	public ACESnippet find(List<ACESentence> sentences) {
		int sentencesSize = sentences.size();
		if (sentencesSize == 0) {
			return null;
		}

		// We iterate over the snippets that contain the first sentence
		for (ACESnippet snippet : sentenceToSnippets.get(sentences.iterator().next())) {
			List<ACESentence> snippetSentences = snippet.getSentences();
			if (snippetSentences.size() != sentencesSize) {
				continue;
			}
			int counter = 0;
			boolean found = true;
			for (ACESentence snippetSentence : snippetSentences) {
				if (snippetSentence.equals(sentences.get(counter))) {
					counter++;
				}
				else {
					found = false;
					break;
				}
			}

			if (found) {
				return snippet;
			}
		}

		return null;
	}


	public Set<ACESnippet> getSentenceSnippets(ACESentence sentence) {
		return (Set<ACESnippet>) sentenceToSnippets.get(sentence);
	}


	public Set<ACESentence> getSentences() {
		return sentenceToSnippets.keySet();
	}


	public Set<OWLLogicalAxiom> removeAxiom(OWLLogicalAxiom axiom) {
		OWLLogicalAxiom ax = (OWLLogicalAxiom) axiom.getAxiomWithoutAnnotations();
		Set<ACESnippet> snippets = getAxiomSnippets(ax);
		if (snippets.isEmpty()) {
			return Collections.<OWLLogicalAxiom>emptySet();
		}
		logger.info("We remove snippets: " + snippets);
		Set<OWLLogicalAxiom> tanglingAxioms = removeAll(snippets);
		tanglingAxioms.remove(ax);
		return tanglingAxioms;
	}


	public List<ACESnippet> getQuestions() {
		return questions;
	}


	public TokenMapper getTokenMapper() {
		return lexicon;
	}


	class SnippetComparator implements Comparator<ACESnippet> {
		public int compare(ACESnippet s1, ACESnippet s2) {
			return s1.toString().compareToIgnoreCase(s2.toString());
		}
	}

	public int getSnippetCount(OWLEntity entity) {
		Set<ACESnippet> snippets = entityToSnippets.get(entity);
		if (snippets == null) {
			return 0;
		}
		return snippets.size();
	}


	public Set<ACESnippet> getSimilarSnippets(ACESnippet snippet) {
		Set<OWLEntity> entities = snippet.getReferencedEntities();
		Set<ACESnippet> similarSnippets = Sets.newHashSet();
		boolean isFirst = true;
		for (OWLEntity entity : entities) {
			if (Showing.isShow(entity)) {
				Set<ACESnippet> snippets = entityToSnippets.get(entity);
				if (snippets != null) {
					if (isFirst) {
						similarSnippets.addAll(snippets);
						similarSnippets.remove(snippet); // Remove itself if present
						isFirst = false;
					}
					else {
						similarSnippets.retainAll(snippets);
					}
				}
			}
		}
		return similarSnippets;
	}


	public boolean contains(ACESnippet snippet) {
		return snippetList.contains(snippet);
	}


	public int size() {
		return snippetList.size();
	}


	public List<ACESnippet> getSnippets() {
		return snippetList;
	}
}