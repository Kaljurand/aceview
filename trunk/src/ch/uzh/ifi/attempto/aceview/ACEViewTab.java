/*
 * This file is part of ACE View.
 * Copyright 2008, Attempto Group, University of Zurich (see http://attempto.ifi.uzh.ch).
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
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.protege.editor.owl.ui.renderer.OWLEntityRenderer;
import org.protege.editor.owl.ui.renderer.OWLEntityRendererListener;
import org.semanticweb.owl.io.OWLRendererException;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAnnotation;
import org.semanticweb.owl.model.OWLAnnotationAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLAxiomAnnotationAxiom;
import org.semanticweb.owl.model.OWLAxiomChange;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDeclarationAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEntityAnnotationAxiom;
import org.semanticweb.owl.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLLogicalAxiom;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyChangeListener;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.RemoveAxiom;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.aceview.lexicon.ACELexicon;
import ch.uzh.ifi.attempto.aceview.lexicon.IncompatibleMorphTagException;
import ch.uzh.ifi.attempto.aceview.lexicon.FieldType;
import ch.uzh.ifi.attempto.aceview.model.event.EventType;

/**
 * <p>The ACE View tab monitors several Protege events and generates
 * corresponding ACE View events.</p>
 * 
 * @author Kaarel Kaljurand
 */
public class ACEViewTab extends OWLWorkspaceViewsTab {

	private static final Logger logger = Logger.getLogger(ACEViewTab.class);

	// Fired when ontologies are loaded/created and reasoner is run
	private final OWLModelManagerListener modelManagerListener = new OWLModelManagerListener() {
		public void handleChange(OWLModelManagerChangeEvent event) {
			if (event.isType(org.protege.editor.owl.model.event.EventType.ACTIVE_ONTOLOGY_CHANGED)) {
				ACETextManager.setActiveACETextURI(getOWLModelManager().getActiveOntology().getURI());
			}
			else if (event.isType(org.protege.editor.owl.model.event.EventType.ONTOLOGY_LOADED)) {
				ACETextManager.createACEText(getOWLModelManager().getActiveOntology().getURI());
				try {
					initACEText();
					ACETextManager.fireEvent(EventType.ACETEXT_LOADED);
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
			else if (event.isType(org.protege.editor.owl.model.event.EventType.ONTOLOGY_VISIBILITY_CHANGED)) {
				logger.info("ONTOLOGY_VISIBILITY_CHANGED");
			}
			else if (event.isType(org.protege.editor.owl.model.event.EventType.ONTOLOGY_CLASSIFIED)) {
				logger.info("ONTOLOGY_CLASSIFIED");
				if (ACEPreferences.getInstance().isUpdateAnswersOnClassify()) {
					new UpdateAnswersUI(null, ACETextManager.getActiveACEText(), getOWLModelManager()).updateAnswers();
				}
			}
			else if (event.isType(org.protege.editor.owl.model.event.EventType.ONTOLOGY_CREATED)) {
				// If an ontology is created then this ontology is set as active, therefore
				// we can get the URI of this ontology by asking for the URI of the active ontology.
				ACETextManager.createACEText(getOWLModelManager().getActiveOntology().getURI());
				ACETextManager.fireEvent(EventType.ACETEXT_CREATED);
			}
			else {
				logger.info("OWLModelManagerChangeEvent: " + event.getType());
			}
		}
	};


	// Fired when axioms are added and removed
	private final OWLOntologyChangeListener ontologyChangeListener = new OWLOntologyChangeListener() {
		public void ontologiesChanged(List<? extends OWLOntologyChange> changes) {
			processChanges(getOWLModelManager(), changes);
		}
	};


	// Fired when entity rendering is changed (e.g. the label-annotation in modified)
	private final OWLEntityRendererListener entityRendererListener = new OWLEntityRendererListener() {
		public void renderingChanged(OWLEntity entity, OWLEntityRenderer renderer) {
			String entityRendering = renderer.render(entity);
			logger.info("Rendering for " + entity + " changed to " + entityRendering);

			OWLModelManager mm = getOWLModelManager();
			OWLOntology ont = mm.getActiveOntology();
			OWLOntologyManager ontologyManager = mm.getOWLOntologyManager();

			List<OWLAxiomChange> changeList1 = removeMorfAnnotations(ont, entity);
			List<OWLAxiomChange> changeList2 = addMorfAnnotations(mm.getOWLDataFactory(), ont, entity, entityRendering);
			changeList1.addAll(changeList2);

			ACETextManager.changeOntology(ontologyManager, changeList1);
		}
	};


	@Override
	public void initialise() {
		super.initialise();
		ACETextManager.setOWLModelManager(getOWLModelManager());
		ACETextManager.createACEText(getOWLModelManager().getActiveOntology().getURI());
		// Note: We start to listen before filling the ACE text, because
		// we want to add entity annotations to the lexicon.
		getOWLModelManager().addOntologyChangeListener(ontologyChangeListener);
		try {
			initACEText();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		getOWLModelManager().getOWLEntityRenderer().addListener(entityRendererListener);
		getOWLModelManager().addListener(modelManagerListener);
	}


	@Override
	public void dispose() {
		getOWLModelManager().removeOntologyChangeListener(ontologyChangeListener);
		getOWLModelManager().removeListener(modelManagerListener);
		getOWLModelManager().getOWLEntityRenderer().removeListener(entityRendererListener);
		super.dispose();
	}


	/**
	 * 
	 * @throws OWLRendererException
	 * @throws OWLOntologyCreationException
	 * @throws OWLOntologyChangeException
	 */
	private static void initACEText() throws OWLRendererException, OWLOntologyCreationException, OWLOntologyChangeException {
		OWLModelManager mm = ACETextManager.getOWLModelManager();
		OWLOntologyManager ontologyManager = mm.getOWLOntologyManager();
		OWLDataFactory df = mm.getOWLDataFactory();
		ACEPreferences prefs = ACEPreferences.getInstance();
		Set<OWLOntology> ontologies = mm.getOntologies();

		logger.info("Init: ontologies: " + ontologies);
		ACETextManager.setInitCompleted(false);

		for (OWLOntology ont : ontologies) {
			URI uri = ont.getURI();

			Set<OWLEntity> entities = ont.getReferencedEntities();

			logger.info("Init: ontology " + uri + " contains " + entities.size() + " referenced entities");

			// Add morphological annotations (sg, pl, vbg) to each entity.
			List<AddAxiomByACEView> changes = Lists.newArrayList();
			for (OWLEntity entity : entities) {
				// addMorfAnnotations(ontologyManager, df, ont, entity, entity.toString());

				// BUG: replace toString() with something else, maybe
				Set<OWLEntityAnnotationAxiom> annSet = MorphAnnotation.getMorphAnnotations(df, ont, entity, entity.toString());
				logger.info("Init: entity " + entity + " adding annotations: " + annSet);
				for (OWLEntityAnnotationAxiom ax : annSet) {
					changes.add(new AddAxiomByACEView(ont, ax));
				}
			}
			ACETextManager.changeOntology(ontologyManager, changes);


			// Translate every OWLEntityAnnotationAxiom into the corresponding lexicon entry.
			ACEText acetext = ACETextManager.getACEText(uri);
			ACELexicon acelexicon = acetext.getACELexicon();
			Set<OWLAnnotationAxiom>	annotationAxioms = ont.getAnnotationAxioms();

			for (OWLAnnotationAxiom annotationAxiom : annotationAxioms) {
				if (annotationAxiom instanceof OWLEntityAnnotationAxiom) {

					OWLEntityAnnotationAxiom annAx = (OWLEntityAnnotationAxiom) annotationAxiom;
					OWLAnnotation ann = annAx.getAnnotation();
					URI annotationURI = ann.getAnnotationURI();

					if (FieldType.isLexiconEntryURI(annotationURI)) {

						String annValue = getAnnotationValueAsString(ann);

						if (annValue == null) {
							logger.info("Malformed ACE lexicon annotation ignored: " + ann);
						}
						else {
							OWLEntity entity = annAx.getSubject();
							try {
								acelexicon.addEntry(entity, FieldType.getField(annotationURI), annValue);
							} catch (IncompatibleMorphTagException e) {
								logger.warn(e.getMessage());
							}
						}
					}
				}
			}


			// Translate every OWL logical axiom into ACE snippet and add it to the ACE text.
			// If the logical axiom is already annotated with an ACE snippet then add the snippet instead.
			AxiomVerbalizer axiomVerbalizer = new AxiomVerbalizer(prefs.getOwlToAce(), acelexicon);

			for (OWLLogicalAxiom logicalAxiom : ont.getLogicalAxioms()) {
				Set<OWLAxiomAnnotationAxiom> existingAnnotations = logicalAxiom.getAnnotationAxioms(ont);
				logger.info("Init: Add axiom: " + logicalAxiom + " (" + existingAnnotations.size() + " annotations)");
				processAxiom(ont, df, ontologyManager, acetext, axiomVerbalizer, uri, logicalAxiom, existingAnnotations);
			}
		}

		ACETextManager.setInitCompleted(true);
	}


	/**
	 * <p>Monitors all the changes that happen in the model and reacts to them,
	 * e.g. updates the ACE text. Only axiom changes (additions, removals) are handled.
	 * The respective axiom can be a logical or an annotation axiom.</p>
	 * 
	 * @param changes List of ontology changes
	 */
	private static void processChanges(OWLModelManager mngr, List<? extends OWLOntologyChange> changes) {
		OWLDataFactory df = mngr.getOWLDataFactory();
		OWLOntologyManager ontologyManager = mngr.getOWLOntologyManager();
		ACEPreferences prefs = ACEPreferences.getInstance();

		// Counts axioms that cause the ACE text to change.
		int textAxiomCounter = 0;
		// Counts axioms that cause the ACE lexicon to change.
		int lexiconAxiomCounter = 0;

		for (OWLOntologyChange change : changes) {

			if (! change.isAxiomChange()) {
				logger.warn("Not handling change: " + change.getClass());
				continue;
			}

			OWLOntology changeOnt = change.getOntology();
			URI uri = changeOnt.getURI();
			OWLAxiom axiom = change.getAxiom();
			ACEText acetext = ACETextManager.getACEText(uri);
			ACELexicon acelexicon = acetext.getACELexicon();

			if (axiom instanceof OWLLogicalAxiom) {

				OWLLogicalAxiom logicalAxiom = (OWLLogicalAxiom) axiom;

				if (change instanceof AddAxiomByACEView) {
					// If an axiom was added in the "ACE view" then do nothing. We don't even have
					// to annotate the entities because such axioms cannot introduce new entities,
					// i.e. sentences that contain words which are not covered by the lexicon (i.e.
					// entity annotations) are not sent to the parser at all, they are rejected before that.
					continue;
				}

				else if (change instanceof RemoveAxiomByACEView) {
					continue;
				}

				else if (change instanceof AddAxiom) {
					textAxiomCounter++;
					Set<OWLAxiomAnnotationAxiom> existingAnnotations = logicalAxiom.getAnnotationAxioms(changeOnt);
					Set<OWLEntity> entities = ((OWLAxiomChange) change).getEntities();
					logger.info("Add axiom: " + logicalAxiom + " (" + existingAnnotations.size() + " annotations)" + " (" + entities.size() + " entities)");
					try {
						AxiomVerbalizer axiomVerbalizer = new AxiomVerbalizer(prefs.getOwlToAce(), acelexicon);
						processAxiom(changeOnt, df, ontologyManager, acetext, axiomVerbalizer, uri, logicalAxiom, existingAnnotations);
					} catch (Exception e) {
						logger.error(e.getMessage());
					}
				}
				else if (change instanceof RemoveAxiom) {
					textAxiomCounter++;
					logger.info("Del axiom: " + logicalAxiom);
					Set<OWLLogicalAxiom> tanglingAxioms = acetext.removeAxiom(logicalAxiom);
					ACETextManager.processTanglingAxioms(acetext, tanglingAxioms);
				}
				else {
					logger.warn("AxiomChange was neither addition nor removal: " + change.getClass());
				}
			}
			else if (axiom instanceof OWLAxiomAnnotationAxiom && !(change instanceof AddAxiomByACEView)) {
				OWLAxiomAnnotationAxiom axannax = (OWLAxiomAnnotationAxiom) axiom;
				OWLAnnotation annotation = axannax.getAnnotation();
				OWLLogicalAxiom annotatedAxiom = (OWLLogicalAxiom) axannax.getSubject();
				if (annotation.getAnnotationURI().equals(ACETextManager.acetextURI)) {
					String text = getAnnotationValueAsString(annotation);
					if (text == null) {
						logger.error("Malformed ACE annotation ignored: " + annotation);
					}
					else {
						logger.info("Axiom is annotated with ACE text.");
						// BUG: if there is already a snippet that corresponds to the
						// axiom that this annotation annotates, then we should just change
						// the text of this snippet, rather than create a new snippet.
						ACESnippet snippet = new ACESnippetImpl(uri, text, annotatedAxiom); 
						acetext.add(snippet);
						ACETextManager.setSelectedSnippet(snippet);
					}
				}
				else {
					// Select the snippet that corresponds to the annotated axiom.
					// If more than one snippets correspond, then select the snippet
					// that comes first in the iteration order.
					Set<ACESnippet> snippets = acetext.getAxiomSnippets(annotatedAxiom);
					logger.info("Axiom annotation is not ACE text. Selecting corresponding snippet from: " + snippets);
					if (! snippets.isEmpty()) {
						ACETextManager.setSelectedSnippet(snippets.iterator().next());
					}
				}
			}
			// TODO: BUG: We process also the AxiomByACEView axioms?
			else if (axiom instanceof OWLEntityAnnotationAxiom) {

				OWLEntityAnnotationAxiom annAx = (OWLEntityAnnotationAxiom) axiom;
				OWLAnnotation ann = annAx.getAnnotation();
				URI annotationURI = ann.getAnnotationURI();

				if (FieldType.isLexiconEntryURI(annotationURI)) {

					String annValue = getAnnotationValueAsString(ann);

					if (annValue == null) {
						// The annotation value is not a constant.
						logger.error("Malformed ACE lexicon annotation ignored: " + ann);
					}
					else {
						lexiconAxiomCounter++;
						OWLEntity entity = annAx.getSubject();

						if (change instanceof AddAxiom) {
							logger.info("Add ann axiom: " + annAx);
							try {
								acelexicon.addEntry(entity, FieldType.getField(annotationURI), annValue);
							} catch (IncompatibleMorphTagException e) {
								logger.warn(e.getMessage());
							}
						}
						else if (change instanceof RemoveAxiom) {
							logger.info("Del ann axiom: " + annAx);
							try {
								acelexicon.removeEntry(entity, FieldType.getField(annotationURI));
							} catch (IncompatibleMorphTagException e) {
								logger.warn(e.getMessage());
							}
						}
					}
				}
			}
			else if (axiom instanceof OWLDeclarationAxiom) {
				OWLDeclarationAxiom declarationAxiom = (OWLDeclarationAxiom) axiom;

				if (change instanceof AddAxiom) {
					logger.info("Add declaration axiom: " + declarationAxiom);
					OWLEntity entity = declarationAxiom.getEntity();
					// BUG: replace toString() with something else, maybe?
					ACETextManager.addToOntology(ontologyManager, changeOnt, MorphAnnotation.getMorphAnnotations(df, changeOnt, entity, entity.toString()));
				}
				else if (change instanceof RemoveAxiom) {
					logger.info("Del declaration axiom: " + declarationAxiom);
					logger.warn("NOT IMPLEMENTED");
					// TODO: BUG: not implemented
				}
			}
			else {
				// logger.warn("Not handling axiom change for " + axiom.getClass());
			}
		}

		if (textAxiomCounter > 0) {
			ACETextManager.fireEvent(EventType.ACETEXT_CHANGED);
		}
		if (lexiconAxiomCounter > 0) {
			ACETextManager.fireEvent(EventType.ACELEXICON_CHANGED);
		}
	}


	/**
	 * <p>Processes a logical axiom that has been added by Protege, either
	 * via the GUI or when loading an ontology.
	 * The input axiom undergoes the following processing.</p>
	 * 
	 * <ol>
	 *  <li>Does it contain any ACE annotations (i.e. has this axiom been already ACEified)?
	 *  <ol>
	 *   <li>If yes then convert the annotations (there can be more than one)
	 *   into ACE snippets and add the snippets to the ACE text</li>
	 *   <li>If not then
	 *   <ol>
	 *    <li>verbalize the axiom</li>
	 *    <li>add the verbalization to the ACE text</li>
	 *    <li>add the verbalization as an annotation to the axiom</li>
	 *    <li>put the new annotation axiom into the ontology</li>
	 *   </ol>
	 *   </li>
	 *  </ol>
	 * </li>
	 * </ul>
	 * 
	 * @param ont
	 * @param df
	 * @param ontologyManager
	 * @param acetext
	 * @param axiomVerbalizer
	 * @param ns
	 * @param logicalAxiom
	 * @param existingAnnotations
	 * @throws OWLRendererException
	 * @throws OWLOntologyCreationException
	 * @throws OWLOntologyChangeException
	 */
	private static void processAxiom(OWLOntology ont, OWLDataFactory df, OWLOntologyManager ontologyManager, ACEText acetext, AxiomVerbalizer axiomVerbalizer, URI ns, OWLLogicalAxiom logicalAxiom, Set<OWLAxiomAnnotationAxiom> existingAnnotations) throws OWLRendererException, OWLOntologyCreationException, OWLOntologyChangeException {

		ACESnippet newSnippet = null;

		for (OWLAxiomAnnotationAxiom axiom : existingAnnotations) {
			OWLAnnotation annotation = axiom.getAnnotation();
			if (annotation.getAnnotationURI().equals(ACETextManager.acetextURI)) {
				String aceAnnotationValue = getAnnotationValueAsString(annotation);
				if (aceAnnotationValue == null) {
					logger.error("Malformed ACE annotation ignored: " + annotation);
				}
				else {
					logger.info("ACE annotation: " + aceAnnotationValue);
					newSnippet = new ACESnippetImpl(ns, aceAnnotationValue, logicalAxiom);
					acetext.add(newSnippet);
				}
			}
		}

		if (newSnippet == null) {
			newSnippet = axiomVerbalizer.verbalizeAxiom(ns, logicalAxiom);
			if (newSnippet == null) {
				logger.warn("AxiomVerbalizer produced a null-snippet for: " + logicalAxiom);
			}
			else {
				addToText(newSnippet, ont, df, ontologyManager, acetext, logicalAxiom);
			}
		}

		ACETextManager.setSelectedSnippet(newSnippet);


		// If we have a OWLInverseObjectPropertiesAxiom where both properties
		// are named then we set the vbg-annotation of each property to contain
		// the name of the other property. This way the verbalization comes out nicer, e.g.
		// Jupiter is-larger-than every planet that is not Jupiter.
		// could be paraphrased as
		// Every planet that is not Jupiter is-smaller-than Jupiter.
		// BUG: this won't really work because
		// 1. the verbalizer adds "is/are ... by" around the form
		// 2. the lexicon might not like 'is-larger-than' used both as sg and vbg (in different entries)
		// TODO: This if-sentence is currently switched off.
		if (false && logicalAxiom instanceof OWLInverseObjectPropertiesAxiom) {
			OWLInverseObjectPropertiesAxiom iopa = (OWLInverseObjectPropertiesAxiom) logicalAxiom;
			if (! iopa.getFirstProperty().isAnonymous() && ! iopa.getSecondProperty().isAnonymous()) {
				OWLObjectProperty p1 = iopa.getFirstProperty().asOWLObjectProperty();
				OWLObjectProperty p2 = iopa.getSecondProperty().asOWLObjectProperty();

				// BUG: removes all, not just the vbg-annotations
				ACETextManager.changeOntology(ontologyManager,
						ACETextManager.getRemoveChanges(ont, p1.getAnnotationAxioms(ont)));
				Set<OWLAxiom> set = Sets.newHashSet();
				set.add(getVbg(df, p1, p2));
				set.add(getVbg(df, p2, p1));
				ACETextManager.addToOntology(ontologyManager, ont, set);
			}
		}
	}

	/**
	 * 
	 * @param addedSnippet
	 * @param ont
	 * @param df
	 * @param ontologyManager
	 * @param acetext
	 * @param logicalAxiom
	 */
	private static void addToText(ACESnippet snippet, OWLOntology ont, OWLDataFactory df, OWLOntologyManager ontologyManager, ACEText acetext, OWLLogicalAxiom logicalAxiom) {
		acetext.add(snippet);
		OWLAnnotation ann = df.getOWLConstantAnnotation(ACETextManager.acetextURI, df.getOWLUntypedConstant(snippet.toString()));
		OWLAxiomAnnotationAxiom axannax = df.getOWLAxiomAnnotationAxiom(logicalAxiom, ann);
		ACETextManager.addToOntology(ontologyManager, ont, Sets.newHashSet(axannax));
	}


	/**
	 * <p>Returns the content of the annotation (i.e. the annotation value) as
	 * a string. The type of the annotation is ignored. In case the value is
	 * not a constant but e.g. an OWL individual, then returns <code>null</code>.</p>
	 * 
	 * @param annotation OWL annotation
	 * @return String that represents the value of the annotation
	 */
	private static String getAnnotationValueAsString(OWLAnnotation annotation) {
		String str = null;
		if (annotation.isAnnotationByConstant()) {
			String literal = annotation.getAnnotationValueAsConstant().getLiteral();
			//String string = annotation.getAnnotationValueAsConstant().toString();
			//logger.info("Annotation value: literal = [" + literal + "] and string = [" + string + "]");
			return literal;
		}
		return str;
	}


	/**
	 * 
	 * @param df
	 * @param p1
	 * @param p2
	 * @return A single <code>OWLEntityAnnotationAxiom</code>
	 */
	private static OWLEntityAnnotationAxiom getVbg(OWLDataFactory df, OWLObjectProperty p1, OWLObjectProperty p2) {
		String vbgForm = p2.toString();
		//String vbgForm = getOWLModelManager().getRendering(p2);
		return df.getOWLEntityAnnotationAxiom(p1, FieldType.VBG.getURI(), df.getOWLUntypedConstant(vbgForm));
	}


	private static List<OWLAxiomChange> addMorfAnnotations(OWLDataFactory df, OWLOntology ont, OWLEntity entity, String lemma) {
		List<OWLAxiomChange> addList = Lists.newArrayList();
		Set<OWLEntityAnnotationAxiom> entityAnnotationAxioms = MorphAnnotation.createMorphAnnotations(df, entity, lemma);
		if (entityAnnotationAxioms.isEmpty()) {
			logger.info("Init: entity " + entity + " is already annotated");
		}
		else {
			logger.info("Init: entity " + entity + " adding annotations: " + entityAnnotationAxioms);
			for (OWLEntityAnnotationAxiom ax : entityAnnotationAxioms) {
				addList.add(new AddAxiom(ont, ax));
			}
		}
		return addList;
	}


	private static List<OWLAxiomChange> removeMorfAnnotations(OWLOntology ont, OWLEntity entity) {
		List<OWLAxiomChange> removeList = Lists.newArrayList();
		Set<OWLAxiom> rAxioms = ont.getReferencingAxioms(entity);
		for (OWLAxiom rAx : rAxioms) {
			if (rAx instanceof OWLEntityAnnotationAxiom &&
					FieldType.isLexiconEntryURI(((OWLEntityAnnotationAxiom) rAx).getAnnotation().getAnnotationURI())) {
				removeList.add(new RemoveAxiom(ont, rAx));
			}
		}
		return removeList;
	}
}