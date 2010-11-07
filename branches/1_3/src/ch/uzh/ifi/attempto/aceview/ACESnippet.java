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

package ch.uzh.ifi.attempto.aceview;

import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.SWRLRule;

import ch.uzh.ifi.attempto.ace.ACESentence;
import ch.uzh.ifi.attempto.aceview.lexicon.TokenMapper;
import ch.uzh.ifi.attempto.aceview.util.SnippetDate;
import ch.uzh.ifi.attempto.ape.Message;

import com.google.common.collect.Multimap;

/**
 * <p>Every ACE snippet is a list of ACE sentences.
 * Every snippet corresponds to a set of zero or
 * more OWL logical axioms and/or SWRL rules.</p>
 * 
 * @author Kaarel Kaljurand
 */
public interface ACESnippet {

	/**
	 * <p>Returns the list of sentences in this snippet.</p>
	 * 
	 * @return List of ACE sentences
	 */
	List<ACESentence> getSentences();

	/**
	 * <p>Returns <code>true</code> iff this snippet contains no sentences.</p>
	 * 
	 * @return <code>true</code> iff this snippet contains no sentences
	 */
	boolean isEmpty();

	/**
	 * <p>Returns a (possibly empty) set of (logical) axioms
	 * that this snippet corresponds to. If the returned set
	 * is empty then it means that the snippet either
	 * contains an ACE syntax error or
	 * expresses a DRS that OWL/SWRL cannot represent.</p>
	 * 
	 * @return Set of OWL logical axioms
	 */
	Set<OWLLogicalAxiom> getLogicalAxioms();

	/**
	 * <p>Returns a (possibly empty) set of SWRL rules
	 * that this snippet corresponds to. Because every
	 * SWRL rule is a logical axiom, the returned set is
	 * always a subset of the set returned by
	 * {@link #getLogicalAxioms()}.</p>
	 * 
	 * @return Set of SWRL rules
	 */
	Set<SWRLRule> getRules();

	/**
	 * <p>Returns a pretty-printed string representation
	 * of this snippet.</p>
	 * 
	 * @return This snippet as string
	 */
	String toString();

	/**
	 * <p>TODO: document</p>
	 * 
	 * @return This snippet as string
	 */
	String toStringID();

	/**
	 * <p>Returns an HTML-formatted representation of
	 * this snippet, but does not add the &lt;html&gt;-tags
	 * around the string.</p>
	 * 
	 * @param tokenMapper ACE lexicon that maps wordforms to entities
	 * @return This snippet formatted in HTML
	 */
	String toHtmlString(TokenMapper tokenMapper);

	/**
	 * <p>Returns an HTML-formatted string with some
	 * properties of the snippet (e.g. "nothing but",
	 * "unsatisfiable").
	 * 
	 * TODO: This should not be here maybe
	 * 
	 * @return Tags formatted in HTML
	 */
	String getTags();

	/**
	 * <p>Returns <code>true</code> iff an axiom of this snippet expresses
	 * unsatisfiability of some named class. Such axioms are:</p>
	 * 
	 * <ul>
	 * <li><code>SubClassOf(C, owl:Nothing)</code></li>
	 * <li><code>EquivalentClasses(..., owl:Nothing, ...)</code></li>
	 * </ul>
	 * 
	 * <p>An implementation this method might also check for other axioms.</p>
	 * 
	 * @return <code>true</code> iff this snippet's axiom expresses an unsatisfiable named class
	 */
	boolean isUnsatisfiable();

	/**
	 * <p>Returns <code>true</code> iff an axiom of this snippet expresses
	 * that some named class if equivalent to <code>owl:Thing</code>.
	 * Such axioms are:</p>
	 * 
	 * <ul>
	 * <li><code>SubClassOf(owl:Thing, C)</code></li>
	 * <li><code>EquivalentClasses(..., owl:Thing, ...)</code></li>
	 * </ul>
	 * 
	 * <p>An implementation this method might also check for other axioms.</p>
	 * 
	 * @return <code>true</code> iff this snippet's axiom gives another name to <code>owl:Thing</code>
	 */
	boolean isEqualToThing();

	/**
	 * <p>Constructs and returns a list of sentences that
	 * form this snippet, but with the given sentence
	 * omitted.</p>
	 * 
	 * @param sentence ACE sentence
	 * @return List of ACE sentences
	 */
	List<ACESentence> getRest(ACESentence sentence);

	/**
	 * <p>Returns the number of content words in this snippet.
	 * If a content word occurs more than once, then it is
	 * also counted more than once.</p>
	 * 
	 * @return Number of content words in this snippet
	 */
	int getContentWordCount();

	/**
	 * <p>Returns the list of messages that were generated
	 * during the parsing of this snippet.</p>
	 * 
	 * @return List of ACE parser messages
	 */
	List<Message> getMessages();

	/**
	 * <p>Returns <code>true</code> iff this snippet
	 * contains ACE syntax errors. If a snippet has
	 * ACE errors, then it has no axioms attached, i.e.
	 * {@link #getLogicalAxioms()} returns an empty set.
	 * The reverse if not true however: a legal ACE snippet
	 * can also map to an empty set of OWL/SWRL axioms.</p>
	 * 
	 * @return <code>true</code> iff this snippet contains ACE syntax errors
	 */
	boolean hasACEErrors();

	/**
	 * <p>Returns <code>true</code> iff this snippet contains
	 * an interrogative sentence (i.e. a question).</p>
	 * 
	 * @return <code>true</code> iff this snippet contains a question
	 */
	boolean isQuestion();

	/**
	 * <p>Returns the logical axiom that corresponds to this snippet,
	 * given that there is exactly one corresponding axiom.
	 * Otherwise returns <code>null</code>.</p>
	 * 
	 * @return OWL logical axiom
	 */
	OWLLogicalAxiom getAxiom();

	/**
	 * <p>Returns the OWL class expression of the axiom
	 * that corresponds to this snippet, given that {@link #getAxiom()}
	 * does not return <code>null</code>, and that this axiom
	 * has the form</p>
	 * 
	 * <pre>
	 * SubClassOf(ClassExpression, owl:Thing)
	 * </pre>
	 * 
	 * <p>Note that this is the form how the semantics of interrogative
	 * snippets is stored, i.e. the DL-Query (<em>ClassExpression</em>)
	 * is wrapped into a dummy axiom.</p>
	 * 
	 * TODO: These technical details should be explained in ACESnippetImpl
	 * 
	 * @return OWL class expression representing the DL Query
	 */
	OWLClassExpression getDLQuery();

	/**
	 * <p>Convenience method that returns <code>true</code>
	 * if {@link #getLogicalAxioms()} returns a non-empty set.</p>
	 * 
	 * @return <code>true</code> if this snippet has axioms attached
	 */
	boolean hasAxioms();

	/**
	 * <p>Returns <code>true</code> iff at least one of the
	 * axioms of this snippet references the given entity.</p>
	 * 
	 * @param entity OWL entity
	 * @return <code>true</code> iff this snippet references the given entity
	 */
	boolean containsEntityReference(OWLEntity entity);

	/**
	 * <p>Returns a set of all the entities referenced by the
	 * axioms of this snippet.</p>
	 *  
	 * @return Set of OWL entities
	 */
	Set<OWLEntity> getReferencedEntities();

	/**
	 * <p>Returns a multimap that maps sentence IDs to
	 * token IDs, where the tokens start an error according
	 * to the ACE parser.</p>
	 * 
	 * @return Multimap of erroneous sentences/tokens
	 */
	Multimap<Integer, Integer> getErrorSpans();

	/**
	 * <p>Returns a list of ACE paragraphs (sentence lists) that represents the
	 * paraphrase of this snippet. In case the paraphrase has
	 * not been generated, then returns an empty list.</p>
	 * 
	 * @return List of ACE paragraphs (sentence lists)
	 */
	List<List<ACESentence>> getParaphrase();

	/**
	 * <p>Returns the default namespace of the snippet.</p>
	 * 
	 * TODO: think about it
	 * 
	 * @return IRI of the default namespace
	 */
	OWLOntologyID getDefaultNamespace();

	/**
	 * <p>Returns the timestamp of the snippet.
	 * This is set during construction to the current time.</p>
	 * 
	 * TODO: think about it
	 * 
	 * @return Timestamp of the creation time of the snippet
	 */
	SnippetDate getTimestamp();

}