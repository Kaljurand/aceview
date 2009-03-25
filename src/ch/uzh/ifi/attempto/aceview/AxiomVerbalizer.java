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

import java.io.StringWriter;
import java.net.URI;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.coode.owlapi.owlxml.renderer.OWLXMLRenderer;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.OWLRendererException;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEntityAnnotationAxiom;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLLogicalAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLSubClassAxiom;

import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.aceview.lexicon.ACELexicon;
import ch.uzh.ifi.attempto.aceview.lexicon.ACELexiconEntry;
import ch.uzh.ifi.attempto.owl.VerbalizerWebservice;

public class AxiomVerbalizer {

	private static final Logger logger = Logger.getLogger(AxiomVerbalizer.class);
	private final VerbalizerWebservice verbalizerWS;
	private final ACELexicon lexicon;

	public AxiomVerbalizer(String verbalizerWSURL, ACELexicon lexicon) {
		this.verbalizerWS = new VerbalizerWebservice(verbalizerWSURL);
		this.lexicon = lexicon;
	}

	public AxiomVerbalizer(VerbalizerWebservice verbalizerWS, ACELexicon lexicon) {
		this.verbalizerWS = verbalizerWS;
		this.lexicon = lexicon;
	}


	/**
	 * <p>Verbalizes a single logical OWL axiom as an ACE snippet.</p>
	 * 
	 * @param ns Namespace for the snippet to be created
	 * @param axiom OWL logical axiom to be verbalized
	 * @return ACE snippet containing the verbalization of the given axiom
	 * @throws OWLRendererException 
	 * @throws OWLOntologyChangeException 
	 * @throws OWLOntologyCreationException 
	 */
	public ACESnippet verbalizeAxiom(URI ns, OWLLogicalAxiom axiom) throws OWLRendererException, OWLOntologyCreationException, OWLOntologyChangeException {

		// BUG: this is a bit of a hack for performance reasons.
		// We verbalize simple axioms without having to use
		// the verbalizer webservice. It seems that about 50% of
		// the axioms in real-world ontologies are simple SubClassOf-axioms,
		// so it really pays off performancewise to verbalize them directly in Java.
		String verbalization = verbalizeSimpleSubClassAxiom(axiom);

		if (verbalization != null) {
			return new ACESnippetImpl(ns, verbalization, axiom);
		}

		logger.info("Verbalizing the axiom using WS");
		try {
			verbalization = verbalizeWithWS(ns, axiom);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(null, "OWL verbalizer error:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}

		ACESnippet snippet = null;
		if (verbalization == null) {
			// Axioms not verbalized, using Manchester Syntax rendering.
			logger.info("Axioms not verbalized, using Manchester Syntax rendering");
			String manSynRendering = ACETextManager.getOWLModelManager().getRendering(axiom);
			if (manSynRendering == null) {
				snippet = new ACESnippetImpl(ns, "", axiom, axiom.toString());
			}
			else {
				snippet = new ACESnippetImpl(ns, "", axiom, manSynRendering);
			}			
		}
		else {
			snippet = new ACESnippetImpl(ns, verbalization, axiom);
		}

		return snippet;
	}


	/**
	 * <p>Verbalizes an OWL ontology as an ACE text (a string).
	 * Uses the OWL->ACE verbalizer webservice.</p>
	 * 
	 * @param manager OWL ontology manager
	 * @param ont OWL ontology to be verbalized
	 * @return Verbalization of the input ontology
	 * @throws OWLRendererException 
	 */
	private String verbalizeOntology(OWLOntologyManager manager, OWLOntology ont) throws OWLRendererException {
		StringWriter sw = new StringWriter();
		OWLXMLRenderer renderer = new OWLXMLRenderer(manager);
		renderer.render(ont, sw);
		return verbalizerWS.call(sw.toString());
	}

	/**
	 * <p>Verbalizes the OWL axiom using the Verbalizer webservice.</p>
	 * 
	 * @param uri URI
	 * @param axiom OWL axiom
	 * @return Verbalization of the given axiom
	 * @throws OWLRendererException 
	 * @throws OWLOntologyChangeException 
	 * @throws OWLOntologyCreationException 
	 */
	private String verbalizeWithWS(URI uri, OWLLogicalAxiom axiom) throws OWLRendererException, OWLOntologyCreationException, OWLOntologyChangeException {
		Set<OWLAxiom> allAxioms = Sets.newHashSet((OWLAxiom) axiom);

		OWLDataFactory df = ACETextManager.getOWLModelManager().getOWLDataFactory();
		for (OWLEntity entity : axiom.getReferencedEntities()) {
			Set<OWLEntityAnnotationAxiom> annotationAxioms = MorphAnnotation.getMorphAnnotationsFromLexicon(df, lexicon, entity);
			allAxioms.addAll(annotationAxioms);
		}

		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = ACETextManager.createOntologyFromAxioms(ontologyManager, uri, allAxioms);
		return verbalizeOntology(ontologyManager, ontology);
	}


	/**
	 * <p>Verbalizes an <code>OWLAxiom</code> given that it is a simple
	 * SubClassOf-axiom (with named classes), or a simple ClassAssertion-axiom
	 * (with a named class) otherwise returns <code>null</code>.</p>
	 * 
	 * @param ax an instance of <code>OWLAxiom</code> to be verbalized
	 * @return sentence corresponding to the <code>OWLAxiom</code> or <code>null</code>
	 */
	private String verbalizeSimpleSubClassAxiom(OWLLogicalAxiom ax) {
		if (ax instanceof OWLSubClassAxiom) {
			OWLSubClassAxiom subClassAxiom = (OWLSubClassAxiom) ax;
			OWLDescription subClass = subClassAxiom.getSubClass();
			OWLDescription superClass = subClassAxiom.getSuperClass();
			// There is a higher chance that superClass.isAnonymous(), that is why we test it first.
			if (isAnonymousOrNothing(superClass)) {
				return null;
			}
			if (isAnonymousOrNothing(subClass)) {
				return null;
			}

			return getSimpleClassRelationVerbalization((OWLClass) subClass, (OWLClass) superClass, "Every");
		}
		else if (ax instanceof OWLClassAssertionAxiom) {
			OWLClassAssertionAxiom classAssertionAxiom = (OWLClassAssertionAxiom) ax;
			OWLDescription desc = classAssertionAxiom.getDescription();
			OWLIndividual ind = classAssertionAxiom.getIndividual();

			if (isAnonymousOrNothing(desc)) {
				return null;
			}

			if (desc.isOWLThing()) {
				return getSg(ind) + " is something.";
			}

			String descAsString = getSg((OWLClass) desc);
			return getSg(ind) + " is " + getIndefiniteArticle(descAsString) + " " + descAsString + ".";
		}
		else if (ax instanceof OWLObjectPropertyAssertionAxiom) {
			OWLObjectPropertyAssertionAxiom opAssertionAxiom = (OWLObjectPropertyAssertionAxiom) ax;
			OWLObjectPropertyExpression opExpression = opAssertionAxiom.getProperty();
			OWLIndividual subject = opAssertionAxiom.getSubject();
			OWLIndividual object = opAssertionAxiom.getObject();

			if (opExpression.isAnonymous()) {
				return null;
			}

			return getSg(subject) + " " + getSg(opExpression.asOWLObjectProperty()) + " " + getSg(object) + ".";
		}
		else if (ax instanceof OWLDisjointClassesAxiom) {
			OWLDisjointClassesAxiom disjointClassesAxiom = (OWLDisjointClassesAxiom) ax;
			Set<OWLDescription> descriptions = disjointClassesAxiom.getDescriptions();

			if (descriptions.size() != 2) {
				return null;
			}

			Iterator<OWLDescription> iterator = descriptions.iterator();

			OWLDescription desc1 = iterator.next();
			OWLDescription desc2 = iterator.next();

			if (isAnonymousOrNothing(desc1)) {
				return null;
			}
			if (isAnonymousOrNothing(desc2)) {
				return null;
			}

			return getSimpleClassRelationVerbalization((OWLClass) desc1, (OWLClass) desc2, "No");

		}
		return null;
	}


	/**
	 * <p>Decides which indefinite article (`a' vs `an') to use, given
	 * a noun (that would follow the article in the text).
	 * The patters have been written by Tobias Kuhn, in core/TextContainer.java
	 * in the AceWiki distribution.</p>
	 * 
	 * @param word is an English noun
	 * @return either 'a' or 'an' depending on the given noun
	 */
	private static String getIndefiniteArticle(String word) {
		String tn = word.toLowerCase();
		boolean an = false;
		if (tn.matches("[aeiou].*")) an = true;
		if (tn.matches("[fhlmnrsx]")) an = true;
		if (tn.matches("[fhlmnrsx]-.*")) an = true;
		if (tn.equals("u")) an = false;
		if (tn.matches("u-.*")) an = false;
		if (tn.matches("u[rtn]i.*")) an = false;
		if (tn.matches("use.*")) an = false;
		if (tn.matches("uk.*")) an = false;

		if (an) {
			return "an";
		}
		return "a";
	}


	private static boolean isAnonymousOrNothing(OWLDescription desc) {
		return (desc.isAnonymous() || desc.isOWLNothing());
	}


	private String getSimpleClassRelationVerbalization(OWLClass class1, OWLClass class2, String prefix) {
		String subClassAsString;
		String superClassAsString;

		if (class1.isOWLThing()) {
			subClassAsString = prefix + "thing";
		}
		else {
			subClassAsString = prefix + " " + getSg(class1);
		}

		if (class2.isOWLThing()) {
			superClassAsString = "something";
		}
		else {
			String superClassName = getSg(class2);
			superClassAsString = getIndefiniteArticle(superClassName) + " " + superClassName;
		}

		return subClassAsString + " is " + superClassAsString + ".";
	}


	/**
	 * <p>Convenience method that returns the singular form of the given OWL entity,
	 * or the toString() of the entity if the singular form is not defined.</p>
	 * 
	 * @param entity OWL entity
	 * @return Singular form of the entity
	 */
	private String getSg(OWLEntity entity) {
		ACELexiconEntry lexiconEntry = lexicon.getEntry(entity);
		if (lexiconEntry == null) {
			return entity.toString();
		}
		String sg = lexiconEntry.getSg();
		if (sg == null) {
			return entity.toString();
		}
		return sg;
	}
}