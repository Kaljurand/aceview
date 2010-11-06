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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import ch.uzh.ifi.attempto.ace.ACESentence;

/**
 * <p>Every ACE text is a sequence of ACE snippets.
 * Snippets can be added or removed from the text.
 * Because every snippet includes (references) a set of logical
 * axioms (OWL axioms or SWRL rules),
 * the text also keeps track of the used axioms
 * and one can query the text for all the snippets that
 * include a given axiom, or a given entity, etc.</p>
 * 
 * @author Kaarel Kaljurand
 */
public interface ACEText<E, A> {

	/**
	 * <p>Adds the given snippet to the end of the text.
	 * Does nothing if the snippet is already in the text.</p>
	 * 
	 * TODO: We could return a boolean that would indicate
	 * if the snippet was actually added or not.
	 * 
	 * @param snippet ACE snippet
	 */
	void add(ACESnippet snippet);

	/**
	 * <p>Adds the given snippet to the given location in the text.
	 * Does nothing if the snippet is already in the text.</p>
	 * 
	 * TODO: We could return a boolean that would indicate
	 * if the snippet was actually added or not.
	 * 
	 * @param index Index of the position where the snippet is added
	 * @param snippet ACE snippet
	 */
	void add(int index, ACESnippet snippet);

	/**
	 * <p>Returns the index of the given snippet in this text.</p>
	 * 
	 * @param snippet ACE snippet
	 * @return Index of the given snippet in this text
	 */
	int indexOf(ACESnippet snippet);

	/**
	 * <p>Returns a subset of the axioms of the given snippet,
	 * such that the axioms are shared by some snippet in this
	 * text. The given snippet does not have to be part of this text.
	 * If it is, however, then we make sure that it does not
	 * "contribute" any shared axioms.</p>
	 *  
	 * @param snippet ACE snippet
	 * @return Set of OWL axioms
	 */
	Set<A> getSharedAxioms(ACESnippet snippet);

	/**
	 * <p>Removes the given snippet from this text. Returns a set
	 * of axioms that are not referenced by any snippets anymore.
	 * Note that the returned set is a subset of the snippet axioms
	 * because there might be other snippets that have been sharing
	 * axioms with the removed snippet. These shared axioms are not
	 * in the returned set.</p>
	 * 
	 * @param snippet ACE snippet
	 * @return Set of OWL axioms
	 */
	Set<A> remove(ACESnippet snippet);

	/**
	 * <p>Constructs and returns a map of snippets in this text
	 * that have no axioms attached. Each snippet is mapped to
	 * its index in this text.</p>
	 * 
	 * @return Map of axiomless snippets to their indices in the text
	 */
	Map<ACESnippet, Integer> getAxiomlessSnippets();

	/**
	 * <p>Returns <code>true</code> if there is a snippet in this
	 * text that references the given axiom.</p>
	 * 
	 * @param axiom OWL axiom
	 * @return <code>true</code> iff the axiom is referenced
	 */
	boolean containsAxiom(A axiom);

	/**
	 * <p>Returns a set of snippets that reference the given axiom.</p>
	 * 
	 * @param axiom OWL axiom
	 * @return Set of snippets
	 */
	Set<ACESnippet> getAxiomSnippets(A axiom);

	/**
	 * <p>Returns the string representation of this text.</p>
	 * 
	 * @return This text as a string
	 */
	String toString();

	/**
	 * <p>Returns the set of all the OWL entities (named classes,
	 * properties, individuals) that are referenced by the axioms
	 * that are referenced by the snippets of this text.</p>
	 * 
	 * TODO: We actually return a SortedSet
	 * 
	 * @return Set of OWL entities
	 */
	Set<E> getReferencedEntities();

	/**
	 * <p>Returns the set of pairs (Entity, Snippet set).</p>
	 * 
	 * @return Set of entity - snippet set map entries
	 */
	Set<Entry<E, Set<ACESnippet>>> getEntitySnippetSetPairs();

	/**
	 * <p>Returns the number of snippets that reference
	 * SWRL rules in this text.</p>
	 * 
	 * @return Number of ACE snippets that reference SWRL rules
	 */
	int getRuleCount();

	/**
	 * <p>Returns the number of snippets that do not have any axioms.</p>
	 * 
	 * TODO: what is the difference to getAxiomlessSnippets
	 * 
	 * @return Number of unparsed snippets
	 */
	int getUnparsedCount();

	/**
	 * <p>Returns the number of unverbalized (empty) snippets, i.e.
	 * snippets that have axioms but have no ACE content.</p>
	 * 
	 * TODO: return the actual set, not just the count
	 * 
	 * @return Number of unverbalized snippets
	 */
	int getUnverbalizedCount();

	/**
	 * <p>Returns the number of snippets that contain
	 * the words "nothing but".</p>
	 * 
	 * @return Number of "nothing but" snippets
	 */
	int getNothingbutCount();


	/**
	 * <p>Returns <code>true</code> if this text contains a snippet
	 * that contains the given sentence.
	 * There can be several of such snippets.</p>
	 * 
	 * TODO: we could return a set of just snippets, not just the count
	 * 
	 * @param sentence ACE sentence
	 * @return <code>true</code> iff this text contains the sentence
	 */
	boolean containsSentence(ACESentence sentence);

	/**
	 * <p>Returns the (single) snippet that contains the given list of sentences
	 * in the given order, and nothing more. If such a snippet is not
	 * found then returns <code>null</code>.</p>
	 * 
	 * @param sentences List of ACE sentences
	 * @return ACE snippet that contains the given sentences and nothing more
	 */
	ACESnippet find(List<ACESentence> sentences);

	/**
	 * <p>Returns the set of snippets that contain the given sentence.</p>
	 * 
	 * @param sentence ACE sentence
	 * @return Set of snippets that contain the given sentence
	 */
	Set<ACESnippet> getSentenceSnippets(ACESentence sentence);

	/**
	 * <p>Returns a set of sentences in this text. Each snippet
	 * contains in general zero or more sentences. Usually, however,
	 * a snippet has exactly one sentence.</p>
	 * 
	 * @return Set of sentences that this text contains.
	 */
	Set<ACESentence> getSentences();

	/**
	 * <p>Removes all the ACE snippets that reference the given axiom.
	 * Because in general, a snippet can reference several axioms, removing
	 * a snippet can leave around a set of unreferenced axioms. Therefore, this
	 * method returns a set of OWL axioms that were left over after the removal
	 * of the snippets. The caller is expected
	 * to form new snippets from the returned axioms, and add these snippets
	 * back into the text. In this way, the state of the OWL ontology and the
	 * state of the ACE text will stay in correspondence.</p>
	 * 
	 * <p>For example, given the axiom</p>
	 * 
	 * <pre>
	 * SubClassOf(dog animal)
	 * </pre>
	 * 
	 * <p>we might remove the following snippets</p>
	 * 
	 * <pre>
	 * [If there is a dog then the dog is an animal.]
	 * [SubClassOf(dog animal)]
	 * </pre>
	 * 
	 * <pre>
	 * [Every man is a human and every dog is an animal.]
	 * [SubClassOf(man human), SubClassOf(dog animal)]
	 * </pre>
	 * 
	 * <p>The first of these snippets can be safely removed because it
	 * references only one axiom (the given one). The second snippet, however,
	 * references also another axiom, <code>SubClassOf(man human)</code>.
	 * This we call an unreferenced axiom and this will be returned (in a set).
	 * Note that this axiom is not returned if there exists in the text
	 * another snippet that references it, e.g. if there is a snippet</p>
	 * 
	 * <pre>
	 * [For every man the man is a human.]
	 * [SubClassOf(man human)]
	 * </pre>
	 * 
	 * @param axiom OWL axiom
	 * @return Set of "unreferenced" OWL axioms
	 */
	Set<A> removeAxiom(A axiom);

	/**
	 * <p>Returns a list of questions (interrogative snippets) in this text.</p>
	 * 
	 * @return List of ACE questions
	 */
	List<ACESnippet> getQuestions();

	/**
	 * <p>Returns the set of snippets whose axioms reference
	 * the given entity.</p>
	 * 
	 * @param entity OWL entity
	 * @return Set of snippets that reference the given entity
	 */
	Set<ACESnippet> getSnippets(E entity);

	/**
	 * <p>Returns the number of snippets whose axioms reference
	 * the given entity.</p>
	 * 
	 * @param entity OWL entity
	 * @return Number of snippets that reference the given entity
	 */
	int getSnippetCount(E entity);

	/**
	 * <p>Returns a set of snippets that reference the same entities
	 * as the given snippet. The returned snippets can additionally
	 * reference other entities as well. The returned set does not contain
	 * the given snippet, even if it is part of this text.</p>
	 * 
	 * <p>Note that this is one way to define similarity. There are lots
	 * of other ways which might be more useful in some contexts.</p>
	 * 
	 * @param snippet ACE snippet
	 * @return Set of similar snippets
	 */
	Set<ACESnippet> getSimilarSnippets(ACESnippet snippet);

	/**
	 * <p>Returns <code>true</code> iff this text contains
	 * the given snippet.</p>
	 * 
	 * @param snippet ACE snippet
	 * @return <code>true</code> iff this text contains the given snippet
	 */
	boolean contains(ACESnippet snippet);

	/**
	 * <p>Returns <code>true</code> iff this text contains
	 * the given list of sentences in the form of a snippet.</p>
	 * 
	 * @param sentences ACE sentences
	 * @return <code>true</code> iff this text contains the given list of sentences
	 */
	boolean contains(List<ACESentence> sentences);

	/**
	 * <p>Returns the number of snippets in this text.</p>
	 * 
	 * @return Number of snippets in this text
	 */
	int size();

	/**
	 * <p>Returns the list of snippets in this text.</p>
	 * 
	 * @return List of ACE snippets in this text
	 */
	List<ACESnippet> getSnippets();

	/**
	 * <p>Sets given the answer to be the answer to the given
	 * question.</p>
	 * 
	 * TODO: What if the question is not part of the text?
	 * Raise exception?
	 * 
	 * @param question ACE question
	 * @param answer ACE answer
	 */
	void setAnswer(ACESnippet question, ACEAnswer answer);

	/**
	 * <p>Returns the answer of the given question, or
	 * <code>null</code> if there is no answer available.</p>
	 * 
	 * TODO: What if the question is not part of the text?
	 * Raise exception?
	 * 
	 * @param question ACE question
	 * @return ACEAnswer of the given question
	 */
	ACEAnswer getAnswer(ACESnippet question);

}