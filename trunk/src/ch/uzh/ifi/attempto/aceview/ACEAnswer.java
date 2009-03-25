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

import java.awt.HeadlessException;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.inference.OWLReasonerAdapter;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLIndividual;

import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.aceview.util.EntityComparator;


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
 * Individuals: Estonia, Latvia, Lithuania
 * Sub classes: baltic-state, EU-country, (owl:Nothing)
 * Sup classes: area, territory, (owl:Thing)
 * </pre>
 * 
 * TODO: We should return a structured set of individuals, where known sameAs individuals
 * are grouped together
 * 
 * TODO: We could also return a structured result where equivalent classes are grouped together.
 * Set<Set<OWLClass>> classes = reasoner.getDescendantClasses(desc);
 * 
 * @author Kaarel Kaljurand
 */
public class ACEAnswer {
	private Set<OWLIndividual> individuals = Sets.newTreeSet(new EntityComparator());
	private Set<OWLClass> subClasses = Sets.newTreeSet(new EntityComparator());
	private Set<OWLClass> supClasses = Sets.newTreeSet(new EntityComparator());
	private boolean isSatisfiable = true;

	private boolean isIndividualAnswersComplete = false;
	private boolean isSubClassesAnswersComplete = false;

	public ACEAnswer(OWLModelManager mngr, ACESnippet snippet) {
		OWLDescription dlquery = snippet.getDLQuery();
		if (dlquery == null) {
			setAnswersToNull();
		}
		else {
			try {
				OWLReasoner reasoner = mngr.getReasoner();

				if (isSatisfiable(reasoner, dlquery)) {
					setAnswerLists(mngr, reasoner, dlquery);
				}
				else {
					isSatisfiable = false;
					setAnswersToNull();
				}
			} catch (HeadlessException e) {
				e.printStackTrace();
			} catch (OWLReasonerException e) {
				e.printStackTrace();
			}
		}
	}

	public Set<OWLIndividual> getIndividuals() {
		return individuals;
	}

	public Set<OWLClass> getSubClasses() {
		return subClasses;
	}

	public Set<OWLClass> getSuperClasses() {
		return supClasses;
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
		if (supClasses == null) {
			return -1;
		}
		return supClasses.size();
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


	private void setAnswersToNull() {
		individuals = null;
		subClasses = null;
		supClasses = null;
	}

	private void setAnswerLists(OWLModelManager mngr, OWLReasoner reasoner, OWLDescription desc) throws OWLReasonerException {
		Set<OWLIndividual> answerIndividuals = reasoner.getIndividuals(desc, false);

		setIndividualAnswerList(individuals, answerIndividuals);
		setClassAnswerList(subClasses, OWLReasonerAdapter.flattenSetOfSets(reasoner.getDescendantClasses(desc)));
		setClassAnswerList(supClasses, OWLReasonerAdapter.flattenSetOfSets(reasoner.getAncestorClasses(desc)));

		// We remove inconsistent classes as they might be confusing when presented as answers.
		subClasses.removeAll(reasoner.getInconsistentClasses());

		isIndividualAnswersComplete = isCompleteIndividuals(mngr.getOWLDataFactory(), reasoner, desc, individuals);
		isSubClassesAnswersComplete = isCompleteSubClasses(mngr.getOWLDataFactory(), reasoner, desc, subClasses);
	}


	private void setClassAnswerList(Set<OWLClass> answerList, Set<OWLClass> classes) {
		for (OWLClass entity : classes) {
			if (ACETextManager.isShow(entity)) {
				answerList.add(entity);
			}
		}
	}


	private void setIndividualAnswerList(Set<OWLIndividual> answerList, Set<OWLIndividual> individuals) {
		for (OWLIndividual answer : individuals) {
			if (ACETextManager.isShow(answer)) {
				answerList.add(answer);
			}
		}
	}


	private boolean isCompleteIndividuals(OWLDataFactory df, OWLReasoner reasoner, OWLDescription desc, Set<OWLIndividual> answers) {
		OWLDescription completenessTest =
			df.getOWLObjectIntersectionOf(
					desc,
					df.getOWLObjectComplementOf(
							df.getOWLObjectOneOf(answers)));

		return (! isSatisfiable(reasoner, completenessTest));
	}


	private boolean isCompleteSubClasses(OWLDataFactory df, OWLReasoner reasoner, OWLDescription desc, Set<OWLClass> answers) {
		OWLDescription completenessTest =
			df.getOWLObjectIntersectionOf(
					desc,
					df.getOWLObjectComplementOf(
							df.getOWLObjectUnionOf(answers)));

		return (! isSatisfiable(reasoner, completenessTest));
	}


	private boolean isSatisfiable(OWLReasoner reasoner, OWLDescription desc) {
		try {
			return reasoner.isSatisfiable(desc);
		} catch (OWLReasonerException e) {
			e.printStackTrace();
			return false;
		}	
	}
}