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

import java.net.URI;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import simplenlg.lexicon.lexicalitems.Noun;

import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.ace.ACEVerb;
import ch.uzh.ifi.attempto.aceview.lexicon.ACELexicon;
import ch.uzh.ifi.attempto.aceview.lexicon.ACELexiconEntry;
import ch.uzh.ifi.attempto.aceview.lexicon.FieldType;
import ch.uzh.ifi.attempto.aceview.lexicon.MorphType;
import ch.uzh.ifi.attempto.aceview.util.OntologyUtils;
import ch.uzh.ifi.attempto.aceview.util.Showing;


/**
 * <p>Adds linguistic annotations (sg, pl, vbg) to OWL entities (named classes, properties, and
 * individuals). Object and data properties are currently treated in the same way,
 * i.e. as English transitive verbs.
 * In the future they will be considered to be of different ACE categories.</p>
 * 
 * <p>Note that the built-in entities <code>owl:Thing</code>, <code>owl:Nothing</code>,
 * are not linguistically annotated, see {@link Showing#isShow(OWLEntity)}.</p>
 * 
 * TODO: update link:
 * @see <a href="http://www.csd.abdn.ac.uk/~agatt/home/links.html">Albert Gatt's Lexicon Generation API</a>
 * 
 * @author Kaarel Kaljurand
 */
public class MorphAnnotation {

	/**
	 * <p>The "lemma" of an entity is its IRI fragment.</p>
	 * 
	 * @param df
	 * @param ont
	 * @param entity
	 * @return
	 */
	public static Set<OWLAnnotationAssertionAxiom> getMorphAnnotations(OWLDataFactory df, OWLOntology ont, OWLEntity entity) {
		String lemma = entity.getIRI().getFragment();
		return getMorphAnnotations(df, ont, entity, lemma);
	}

	/**
	 * <p>Returns an empty set of axioms if the input entity corresponds to
	 * a function word (<code>owl:Thing</code>, <code>owl:Nothing</code>, ...) which is not to be annotated
	 * with surface forms.</p>
	 * 
	 * <p>Otherwise returns a set of entity annotation axioms [BUG: update] for the given entity.
	 * An annotation is not generated in case the entity has already been annotated
	 * with the annotation. This means that surface form is a functional property of the entity.</p>
	 * 
	 * @param df OWLDataFactory
	 * @param ontology OWL ontology in which we check if the entity has already been annotated
	 * @param entity OWL entity to be annotated
	 * @param lemma Lemma form of the entity which should be taken as the basis when generating surface forms
	 * @return Set of OWL entity annotation axioms
	 */
	public static Set<OWLAnnotationAssertionAxiom> getMorphAnnotations(OWLDataFactory df, OWLOntology ontology, OWLEntity entity, String lemma) {

		Set<OWLAnnotationAssertionAxiom> axioms = Sets.newHashSet();

		if (! Showing.isShow(entity)) {
			return axioms;
		}

		// Existing annotation URIs for the entity.
		Set<URI> annotationURIs = OntologyUtils.getAnnotationURIs(ontology, entity);

		if (entity instanceof OWLClass) {

			if (! annotationURIs.contains(MorphType.CN_SG.getURI())) {
				axioms.add(OntologyUtils.createEntityAnnotationAxiom(df, MorphType.CN_SG.getURI(), entity, lemma));
			}

			if (! annotationURIs.contains(MorphType.CN_PL.getURI())) {
				Noun noun = new Noun(lemma);
				axioms.add(OntologyUtils.createEntityAnnotationAxiom(df, MorphType.CN_PL.getURI(), entity, noun.getPlural()));
			}
		}
		else if (isVerblike(entity)) {
			ACEVerb verb = new ACEVerb(lemma);

			if (! annotationURIs.contains(MorphType.TV_SG.getURI())) {
				axioms.add(OntologyUtils.createEntityAnnotationAxiom(df, MorphType.TV_SG.getURI(), entity, verb.getPresent3SG()));
			}

			if (! annotationURIs.contains(MorphType.TV_PL.getURI())) {
				axioms.add(OntologyUtils.createEntityAnnotationAxiom(df, MorphType.TV_PL.getURI(), entity, lemma));
			}

			if (! annotationURIs.contains(MorphType.TV_VBG.getURI())) {
				axioms.add(OntologyUtils.createEntityAnnotationAxiom(df, MorphType.TV_VBG.getURI(), entity, verb.getPastParticiple()));
			}
		}
		else if (entity instanceof OWLIndividual) {
			if (! annotationURIs.contains(MorphType.PN_SG.getURI())) {
				axioms.add(OntologyUtils.createEntityAnnotationAxiom(df, MorphType.PN_SG.getURI(), entity, lemma));
			}
		}

		return axioms;
	}


	/**
	 * <p>Creates morphological annotations from the lemma, using English morphological
	 * synthesis. The lemma should normally be the rendering of the given OWL entity.</p>
	 * 
	 * @param df OWL data factory
	 * @param entity OWL entity
	 * @param lemma OWL entity rendering
	 * @return Set of OWL entity annotation axioms
	 */
	public static Set<OWLAnnotationAssertionAxiom> createMorphAnnotations(OWLDataFactory df, OWLEntity entity, String lemma) {
		Set<OWLAnnotationAssertionAxiom> axioms = Sets.newHashSet();

		if (! Showing.isShow(entity)) {
			return axioms;
		}

		if (entity instanceof OWLClass) {
			Noun noun = new Noun(lemma);
			axioms.add(OntologyUtils.createEntityAnnotationAxiom(df, FieldType.SG.getURI(), entity, lemma));
			axioms.add(OntologyUtils.createEntityAnnotationAxiom(df, FieldType.PL.getURI(), entity, noun.getPlural()));
		}
		else if (isVerblike(entity)) {
			ACEVerb verb = new ACEVerb(lemma);
			axioms.add(OntologyUtils.createEntityAnnotationAxiom(df, FieldType.SG.getURI(), entity, verb.getPresent3SG()));
			axioms.add(OntologyUtils.createEntityAnnotationAxiom(df, FieldType.PL.getURI(), entity, lemma));
			axioms.add(OntologyUtils.createEntityAnnotationAxiom(df, FieldType.VBG.getURI(), entity, verb.getPastParticiple()));
		}
		else if (entity instanceof OWLIndividual) {
			axioms.add(OntologyUtils.createEntityAnnotationAxiom(df, FieldType.SG.getURI(), entity, lemma));
		}

		return axioms;
	}


	/**
	 * <p>Creates a set of entity annotation axioms on the basis the lexicon entries
	 * for the given entity.</p>
	 * 
	 * @param df OWLDataFactory
	 * @param lexicon Lexicon from where we take the entity's surface forms
	 * @param entity OWL entity to be annotated
	 * @return Set of OWL entity annotation axioms
	 */
	public static Set<OWLAnnotationAssertionAxiom> getMorphAnnotationsFromLexicon(OWLDataFactory df, ACELexicon<OWLEntity> lexicon, OWLEntity entity) {

		Set<OWLAnnotationAssertionAxiom> axioms = Sets.newHashSet();

		if (! Showing.isShow(entity)) {
			return axioms;
		}

		ACELexiconEntry entry = lexicon.getEntry(entity);

		if (entry == null) {
			return axioms;
		}


		if (entity instanceof OWLClass) {
			addToAxioms(df, axioms, FieldType.SG.getURI(), entity, entry.getSg());
			addToAxioms(df, axioms, FieldType.PL.getURI(), entity, entry.getPl());
		}
		else if (isVerblike(entity)) {
			addToAxioms(df, axioms, FieldType.SG.getURI(), entity, entry.getSg());
			addToAxioms(df, axioms, FieldType.PL.getURI(), entity, entry.getPl());
			addToAxioms(df, axioms, FieldType.VBG.getURI(), entity, entry.getVbg());
		}
		else if (entity instanceof OWLIndividual) {
			addToAxioms(df, axioms, FieldType.SG.getURI(), entity, entry.getSg());
		}

		return axioms;
	}


	private static void addToAxioms(OWLDataFactory df, Set<OWLAnnotationAssertionAxiom> axioms, URI uri, OWLEntity entity, String form) {
		if (form != null) {
			axioms.add(OntologyUtils.createEntityAnnotationAxiom(df, uri, entity, form));
		}
	}


	private static boolean isVerblike(OWLEntity entity) {
		return (entity instanceof OWLObjectProperty || entity instanceof OWLDataProperty);
	}
}