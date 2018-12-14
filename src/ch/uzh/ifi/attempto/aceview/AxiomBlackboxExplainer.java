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
import java.util.Set;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.debugging.DebuggerClassExpressionGenerator;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import uk.ac.manchester.cs.bhig.util.Tree;
import uk.ac.manchester.cs.owl.explanation.ordering.DefaultExplanationOrderer;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationTree;

import com.clarkparsia.owlapi.explanation.BlackBoxExplanation;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * <p>Explains an OWL axiom by a set of sets of ACE snippets.</p>
 * 
 * TODO: this shouldn't depend on Protege libraries, i.e.
 * don't pass in OWLModelManager
 * 
 * @author Kaarel Kaljurand
 */
public class AxiomBlackboxExplainer {

	private static final Logger logger = Logger.getLogger(AxiomBlackboxExplainer.class);

	private final OWLModelManager modelManager;
	private final OWLAxiom axiom;


	public AxiomBlackboxExplainer(OWLModelManager modelManager, OWLAxiom axiom) {
		this.modelManager = modelManager;
		this.axiom = axiom;
	}


	/**
	 * <p>Finds explanations for this axiom and then converts the
	 * explanations into a set of sets of snippets.</p>
	 * 
	 * TODO: instead of a set of sets, try to return a set of lists, where
	 * the order of a list (i.e. inner order of an explanation) is somehow useful,
	 * e.g. simpler sentences come first.
	 * 
	 * @return Set of set of snippets
	 */
	public Set<Set<ACESnippet>> getExplanations() {
		ACEText<OWLEntity, OWLLogicalAxiom> activeAcetext = ACETextManager.getActiveACEText();
		Set<Set<OWLAxiom>> explanationSet = getAxiomSets();
		Set<Set<ACESnippet>> snippetSetSet = Sets.newHashSet();

		for (Set<OWLAxiom> explanation : explanationSet) {
			logger.info("Explanation: " + explanation);
			Set<ACESnippet> snippetSet = Sets.newHashSet();
			for (OWLAxiom ax : explanation) {
				Set<ACESnippet> snippets = activeAcetext.getAxiomSnippets((OWLLogicalAxiom) ax);
				if (snippets == null) {
					logger.error("Axiom does not have any corresponding snippets: " + ax);
				}
				else {
					if (snippets.size() > 1) {
						logger.info("Axiom has several corresponding snippets: " + ax);
					}
					snippetSet.addAll(snippets);
				}
			}
			snippetSetSet.add(snippetSet);
		}
		return snippetSetSet;
	}


	/**
	 * <p>Returns a set of sets of snippets which justify the entailment.
	 * These are asserted snippets which correspond to the asserted axioms which
	 * justify the entailment. These axioms are first ordered using the
	 * <code>DefaultExplanationOrderer</code> which actually creates a tree of axioms
	 * to group axioms which stand together. At the user level, one can use indentation
	 * to display this tree.</p>
	 * 
	 * TODO: Currently we flatten the tree to keep things simple. The flattening
	 * is done by {@link #getAllNodes(Tree)} which creates a list of tree nodes
	 * by traversing the tree in a depth-first manner.
	 * 
	 * TODO: Note that this code crashes more often than {@link #getExplanations()}
	 * (because of the ordering).
	 * 
	 * @return Set of sets of snippets
	 */
	public Set<Set<ACESnippet>> getOrderedExplanations() {
		ACEText<OWLEntity, OWLLogicalAxiom> activeAcetext = ACETextManager.getActiveACEText();
		Set<Set<OWLAxiom>> explanationSet = getAxiomSets();
		Set<Set<ACESnippet>> snippetSetSet = Sets.newHashSet();

		for (Set<OWLAxiom> explanation : explanationSet) {
			logger.info("Explanation: " + explanation);
			DefaultExplanationOrderer orderer = new DefaultExplanationOrderer();
			ExplanationTree tree = orderer.getOrderedExplanation(axiom, explanation);
			logger.info("Tree: " + printTree(tree));
			Set<ACESnippet> snippetSet = Sets.newHashSet();

			for (OWLAxiom ax : getAllNodes(tree.getChildren())) {
				Set<ACESnippet> snippets = activeAcetext.getAxiomSnippets((OWLLogicalAxiom) ax);
				if (snippets == null) {
					logger.error("Axiom does not have any corresponding snippets: " + ax);
				}
				else {
					if (snippets.size() > 1) {
						logger.info("Axiom has several corresponding snippets: " + ax);
					}
					snippetSet.addAll(snippets);
				}
			}
			snippetSetSet.add(snippetSet);
		}
		return snippetSetSet;
	}


	private static String printTree(Tree<OWLAxiom> tree) {
		StringBuilder sb = new StringBuilder();
		sb.append(tree.getUserObject());
		if (!tree.isLeaf()) {
			for (Tree<OWLAxiom> child : tree.getChildren()) {
				sb.append('\t');
				sb.append(printTree(child));
				sb.append('\n');
			}
		}
		return sb.toString();
	}


	private static List<OWLAxiom> getAllNodes(Tree<OWLAxiom> tree) {
		List<OWLAxiom> list = Lists.newArrayList(tree.getUserObject());
		if (! tree.isLeaf()) {
			list.addAll(getAllNodes(tree.getChildren()));
		}
		return list;
	}


	private static List<OWLAxiom> getAllNodes(List<Tree<OWLAxiom>> trees) {
		List<OWLAxiom> list = Lists.newArrayList();
		for (Tree<OWLAxiom> tree : trees) {
			list.addAll(getAllNodes(tree));
		}
		return list;
	}


	// TODO: BUG: the following two methods are modified versions of
	// similar methods in:
	// org.protege.editor.owl.ui.explanation.impl.BasicBlackboxExplanationService
	// They will be rewritten once explanation support in Protege is stable.
	private Set<Set<OWLAxiom>> getAxiomSets() {
		OWLOntology activeOntology = modelManager.getActiveOntology();
		OWLReasonerFactory rFactory = modelManager.getOWLReasonerManager().getCurrentReasonerFactory().getReasonerFactory();
		OWLReasoner reasoner = modelManager.getOWLReasonerManager().getCurrentReasoner();
		BlackBoxExplanation explain  = new BlackBoxExplanation(activeOntology, rFactory, reasoner);
		Set<OWLAxiom> axioms = explain.getExplanation(getClassExpression(axiom));
		Set<Set<OWLAxiom>> setOfSets = Sets.newHashSet();
		// BUG: we only return a single explanation for the time being
		setOfSets.add(axioms);
		return setOfSets;
	}


	private OWLClassExpression getClassExpression(OWLAxiom axiom) {
		/*
		 * there is no clear method...
		 */
		DebuggerClassExpressionGenerator classExpressionVisitor = new DebuggerClassExpressionGenerator(modelManager.getOWLDataFactory());
		axiom.accept(classExpressionVisitor);
		return classExpressionVisitor.getDebuggerClassExpression();
	}
}