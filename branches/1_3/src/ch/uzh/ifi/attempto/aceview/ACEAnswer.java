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

import java.awt.HeadlessException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.aceview.util.NodeComparator;
import ch.uzh.ifi.attempto.aceview.util.Showing;


/**
 * <p>Generates the answers to the given ACE question, i.e. to its
 * corresponding OWL class expression.</p>
 * 
 * <p>There are three types of answers</p>
 * 
 * <ul>
 * <li>a set of named individuals that belong to the class expression,</li>
 * <li>a set of named classes that are sub classes (possibly indirect) of this expression, and</li>
 * <li>a set of named classes that are super classes (possibly indirect) of this expression.</li>
 * </ul>
 * 
 * <p>The trivial solutions (<code>owl:Thing</code>, <code>owl:Nothing</code>) are
 * not returned as answers. Also, unsatisfiable classes (i.e. classes that are
 * equivalent to <code>owl:Nothing</code>) are not returned as sub classes.</p>
 * 
 * <p>Examples:</p>
 * 
 * <pre>
 * Question: What is a country? (i.e. class expression "country")
 * Individuals: Estonia = Estland, Latvia, Lithuania
 * Sub classes: baltic-state, EU-country, (owl:Nothing)
 * Sup classes: area, territory, (owl:Thing)
 * </pre>
 * 
 * @author Kaarel Kaljurand
 */
public class ACEAnswer {

	private static final Logger logger = Logger.getLogger(ACEAnswer.class);

	// TODO: make sure that individuals are returned so that sameAs-individuals
	// are in the same node

	// Set of all the entities that occur as answers
	private Set<OWLEntity> entities = Sets.newHashSet();
	private Set<Node<OWLClass>> subClasses = Sets.newTreeSet(new NodeComparator());
	private Set<Node<OWLClass>> superClasses = Sets.newTreeSet(new NodeComparator());
	private Set<Node<OWLNamedIndividual>> individuals = Sets.newTreeSet(new NodeComparator());

	private boolean isSatisfiable = true;
	private boolean isIndividualAnswersComplete = false;
	private boolean isSubClassesAnswersComplete = false;

	private IndividualNodeSetPolicy individualNodeSetPolicy = null;

	public ACEAnswer(OWLModelManager mngr, ACESnippet snippet) {
		OWLClassExpression dlquery = snippet.getDLQuery();
		if (dlquery == null) {
			setAnswersToNull();
		}
		else {
			try {
				OWLReasoner reasoner = mngr.getReasoner();

				individualNodeSetPolicy = reasoner.getIndividualNodeSetPolicy();

				if (isSatisfiable(reasoner, dlquery)) {
					setAnswerLists(mngr, reasoner, dlquery);
				}
				else {
					isSatisfiable = false;
					setAnswersToNull();
				}
				logger.info("new ACEAnswer:\n" + toString());
			} catch (HeadlessException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean containsEntity(OWLEntity entity) {
		return entities.contains(entity);
	}

	public Set<Node<OWLNamedIndividual>> getIndividuals() {
		return individuals;
	}

	public Set<Node<OWLClass>> getSubClasses() {
		return subClasses;
	}

	public Set<Node<OWLClass>> getSuperClasses() {
		return superClasses;
	}

	public int getIndividualsCount() {
		if (individuals == null) {
			return -1;
		}
		return individuals.size();
	}

	public int getSubClassesCount() {
		if (subClasses == null) {
			return -1;
		}
		return subClasses.size();
	}

	public int getSuperClassesCount() {
		if (superClasses == null) {
			return -1;
		}
		return superClasses.size();
	}

	public boolean isSatisfiable() {
		return isSatisfiable;
	}

	public boolean isIndividualAnswersComplete() {
		return isIndividualAnswersComplete;
	}

	public void setIndividualAnswersComplete(boolean b) {
		isIndividualAnswersComplete = b;
	}

	public boolean isSubClassesAnswersComplete() {
		return isSubClassesAnswersComplete;
	}

	public void setSubClassesAnswersComplete(boolean b) {
		isSubClassesAnswersComplete = b;
	}

	public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
		return individualNodeSetPolicy;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("==== ANSWER ====\n");
		sb.append("Satisfiable: " + isSatisfiable + "\n");

		sb.append("Individuals complete: " + isIndividualAnswersComplete + "\n");
		sb.append("Subclasses complete: " + isSubClassesAnswersComplete + "\n");
		sb.append("Superclasses complete: " + "???\n");

		sb.append("Individuals: " + getIndividualsCount() + ": " + getIndividuals() + "\n");
		sb.append("Subclasses: " + getSubClassesCount() + ": " + getSubClasses() + "\n");
		sb.append("Superclasses: " + getSuperClassesCount() + ": " + getSuperClasses() + "\n");
		sb.append("================\n");

		return sb.toString();
	}


	private void setAnswersToNull() {
		individuals = null;
		subClasses = null;
		superClasses = null;
		entities.clear();
	}

	private void setAnswerLists(OWLModelManager mngr, OWLReasoner reasoner, OWLClassExpression desc) {
		// false gives more answers (but might be slower)
		NodeSet<OWLNamedIndividual> indNodeSet = reasoner.getInstances(desc, false);
		setIndividualsAnswerList(indNodeSet.getNodes());

		// false = get all the descendant classes, not just the direct ones
		NodeSet<OWLClass> subNodeSet = reasoner.getSubClasses(desc, false);
		setClassAnswerList(subClasses, subNodeSet.getNodes(), true);

		NodeSet<OWLClass> superNodeSet = reasoner.getSuperClasses(desc, false);
		setClassAnswerList(superClasses, superNodeSet.getNodes(), false);

		isIndividualAnswersComplete = isCompleteIndividuals(mngr.getOWLDataFactory(), reasoner, desc, indNodeSet.getFlattened());
		isSubClassesAnswersComplete = isCompleteSubClasses(mngr.getOWLDataFactory(), reasoner, desc, subNodeSet.getFlattened());
	}


	private void setIndividualsAnswerList(Set<Node<OWLNamedIndividual>> indNodes) {
		for (Node<OWLNamedIndividual> node : indNodes) {
			if (Showing.isShow(node)) {
				individuals.add(node);
				entities.addAll(node.getEntities());
			}
		}
	}


	// TODO: exclude also based on Showing.isShow(node)
	private void setClassAnswerList(Set<Node<OWLClass>> answerList, Set<Node<OWLClass>> classNodes, boolean sub) {
		for (Node<OWLClass> node : classNodes) {
			if (sub && ! node.isBottomNode() || ! sub && ! node.isTopNode()) {
				answerList.add(node);
				entities.addAll(node.getEntities());
			}
		}
	}


	private boolean isCompleteIndividuals(OWLDataFactory df, OWLReasoner reasoner, OWLClassExpression desc, Set<OWLNamedIndividual> answers) {
		OWLClassExpression completenessTest =
			df.getOWLObjectIntersectionOf(
					desc,
					df.getOWLObjectComplementOf(
							df.getOWLObjectOneOf(answers)));

		return (! isSatisfiable(reasoner, completenessTest));
	}


	private boolean isCompleteSubClasses(OWLDataFactory df, OWLReasoner reasoner, OWLClassExpression desc, Set<OWLClass> answers) {
		OWLClassExpression completenessTest =
			df.getOWLObjectIntersectionOf(
					desc,
					df.getOWLObjectComplementOf(
							df.getOWLObjectUnionOf(answers)));

		return (! isSatisfiable(reasoner, completenessTest));
	}


	private boolean isSatisfiable(OWLReasoner reasoner, OWLClassExpression desc) {
		return reasoner.isSatisfiable(desc);
	}
}