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

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import uk.ac.manchester.cs.bhig.util.Tree;
import uk.ac.manchester.cs.owl.explanation.ordering.DefaultExplanationOrderer;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationTree;

import com.clarkparsia.explanation.BlackBoxExplanation;
import com.clarkparsia.explanation.ExplanationGenerator;
import com.clarkparsia.explanation.HSTExplanationGenerator;
import com.clarkparsia.explanation.SatisfiabilityConverter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * <p>Explains an OWL axiom by a set of sets of ACE snippets.</p>
 * 
 * @author Kaarel Kaljurand
 */
public class AxiomExplainer {

	private static final Logger logger = Logger.getLogger(AxiomExplainer.class);

	private final OWLModelManager modelManager;
	private final OWLAxiom axiom;


	public AxiomExplainer(OWLModelManager modelManager, OWLAxiom axiom) {
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


	/**
	 * <p>Finds explanations for this axiom. Each explanation is a set of
	 * axioms that entail this axiom. There can be many (equally good)
	 * explanations, therefore a set of sets of axioms is returned.</p>
	 * 
	 * TODO: Study the formal properties of the result:
	 * 1. Result is complete and correct?
	 * 2. There cannot exist two explanations such that one is a subset of the other?
	 * 3. Why is the explanation a set of axioms and not an ordered list?
	 * 4. When deleting at least one axiom from each explanation,
	 * the entailment will stop to exist.
	 * 
	 * @return Set of sets of axioms
	 */
	private Set<Set<OWLAxiom>> getAxiomSets() {
		// Converts an axiom into an unsatisfiable class expression.
		SatisfiabilityConverter satCon = new SatisfiabilityConverter(modelManager.getOWLDataFactory());
		OWLClassExpression desc = satCon.convert(axiom);

		// Sets up the BlackBoxExplanation
		BlackBoxExplanation exp = new BlackBoxExplanation(ACETextManager.createOWLOntologyManager());
		exp.setOntology(modelManager.getActiveOntology());
		exp.setReasoner(modelManager.getReasoner());
		exp.setReasonerFactory(modelManager.getOWLReasonerManager().getCurrentReasonerFactory());

		// Generates explanations on the the basis of the BlackBoxExplanation
		ExplanationGenerator gen = new HSTExplanationGenerator(exp);

		return gen.getExplanations(desc);
	}
}
