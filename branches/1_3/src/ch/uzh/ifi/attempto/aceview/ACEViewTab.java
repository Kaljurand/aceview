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
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.protege.editor.owl.ui.renderer.OWLEntityRenderer;
import org.protege.editor.owl.ui.renderer.OWLEntityRendererListener;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.SWRLRule;

import com.google.common.collect.Lists;

import ch.uzh.ifi.attempto.aceview.lexicon.MorphType;
import ch.uzh.ifi.attempto.aceview.lexicon.TokenMapper;
import ch.uzh.ifi.attempto.aceview.model.event.EventType;
import ch.uzh.ifi.attempto.aceview.util.OntologyUtils;
import ch.uzh.ifi.attempto.aceview.util.Showing;

/**
 * <p>The ACE View tab monitors several Protege events and generates
 * corresponding ACE View events.</p>
 * 
 * @author Kaarel Kaljurand
 */
public class ACEViewTab extends OWLWorkspaceViewsTab {

	// Initialized in initialise()
	private static OWLAnnotationProperty acetextAnnProp;

	private static final Logger logger = Logger.getLogger(ACEViewTab.class);

	// Fired when ontologies are loaded/created and reasoner is run
	private final OWLModelManagerListener modelManagerListener = new OWLModelManagerListener() {
		public void handleChange(OWLModelManagerChangeEvent event) {
			if (event.isType(org.protege.editor.owl.model.event.EventType.ACTIVE_ONTOLOGY_CHANGED)) {
				ACETextManager.setActiveACETextID(getOWLModelManager().getActiveOntology().getOntologyID());
			}
			else if (event.isType(org.protege.editor.owl.model.event.EventType.ONTOLOGY_LOADED)) {
				ACETextManager.createACEText(getOWLModelManager().getActiveOntology().getOntologyID());
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
				if (ACEViewPreferences.getInstance().isUpdateAnswersOnClassify()) {
					new UpdateAnswersUI(null, ACETextManager.getActiveACEText(), getOWLModelManager()).updateAnswers();
				}
			}
			else if (event.isType(org.protege.editor.owl.model.event.EventType.ONTOLOGY_CREATED)) {
				// If an ontology is created then this ontology is set as active, therefore
				// we can get the URI of this ontology by asking for the URI of the active ontology.
				ACETextManager.createACEText(getOWLModelManager().getActiveOntology().getOntologyID());
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


	// OWLEntityRendererListener is notified when an entity rendering is changed
	// (e.g. the label-annotation is modified).
	//
	// TODO: BUG (Protege bug?): whenever there is a change in the rendering
	// this is fired 4 times (for Class, OProp, DProp, NamedInd), which leads to complete
	// garbage on the ACE View side.
	// So we comment this out for the time being and look into this later.
	//
	// How to test?
	// 1. Set entity rendering to be based on "label"
	// 2. Create a class "man"
	// 3. (ACE View annotations CN_sg and CN_pl are automatically created)
	// 4. Create annotation "label"
	// 5. (ACE View annotations are removed and added back 4 times for all possible
	// entity types, only the last change (adding PN_sg) prevails.
	private final OWLEntityRendererListener entityRendererListener = new OWLEntityRendererListener() {
		public void renderingChanged(OWLEntity entity, OWLEntityRenderer renderer) {
			String entityRendering = renderer.render(entity);
			logger.info("Rendering for " + entity + " with type " + entity.getEntityType() + " changed to " + entityRendering);

			// TODO: BUG: only add morph annotations if prefs.isUseLexicon == true
			/*
			OWLModelManager mm = getOWLModelManager();
			OWLOntology ont = mm.getActiveOntology();

			List<OWLAxiomChange> changeList1 = removeMorfAnnotations(ont, entity);
			List<OWLAxiomChange> changeList2 = addMorfAnnotations(mm.getOWLDataFactory(), ont, entity, entityRendering);	
			changeList1.addAll(changeList2);

			OWLOntologyManager ontologyManager = mm.getOWLOntologyManager();
			OntologyUtils.changeOntology(ontologyManager, changeList1);
			 */
		}
	};


	@Override
	public void initialise() {
		super.initialise();

		try {
			ParserHolder.updateACEParser(ACEViewPreferences.getInstance());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		OWLModelManager mm = getOWLModelManager();
		acetextAnnProp = mm.getOWLDataFactory().getOWLAnnotationProperty(ACETextManager.acetextIRI);

		ACETextManager.setOWLModelManager(mm);
		ACETextManager.createACEText(mm.getActiveOntology().getOntologyID());
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
	 * <p>This method is called either when the ACE View tab is launched, or
	 * when the ONTOLOGY_LOADED even is fired. In both cases the ACE text
	 * must be generated that corresponds to the ontology.</p>
	 * 
	 * @throws OWLRendererException
	 * @throws OWLOntologyCreationException
	 * @throws OWLOntologyChangeException
	 */
	private static void initACEText() throws OWLRendererException, OWLOntologyCreationException, OWLOntologyChangeException {
		OWLModelManager mm = ACETextManager.getOWLModelManager();
		OWLOntologyManager mngr = mm.getOWLOntologyManager();
		OWLDataFactory df = mm.getOWLDataFactory();
		ACEViewPreferences prefs = ACEViewPreferences.getInstance();
		Set<OWLOntology> ontologies = mm.getOntologies();

		logger.info("Init: ontology count: " + ontologies.size());

		ACETextManager.setInitCompleted(false);
		for (OWLOntology ont : ontologies) {
			logger.info("Init: ontology: " + ont);
			initACETextFromOntology(mngr, df, prefs, ont);
		}
		ACETextManager.setInitCompleted(true);
	}


	private static void initACETextFromOntology(OWLOntologyManager mngr, OWLDataFactory df, ACEViewPreferences prefs, OWLOntology ont) throws OWLRendererException, OWLOntologyCreationException, OWLOntologyChangeException {

		if (prefs.isUseLexicon()) {
			Set<OWLEntity> entities = ont.getSignature();
			addMorfAnnotations(mngr, df, ont, entities);
		}

		// Translate every OWL logical axiom into ACE snippet and add it to the ACE text.
		// If the logical axiom is already annotated with an ACE snippet then add the snippet instead.
		OWLOntologyID iri = ont.getOntologyID();
		ACEText<OWLEntity, OWLLogicalAxiom> acetext = ACETextManager.getACEText(iri);

		AxiomVerbalizer axiomVerbalizer = new AxiomVerbalizer(prefs.getOwlToAce());
		for (OWLLogicalAxiom logicalAxiom : ont.getLogicalAxioms()) {
			logger.info("Init: Add axiom: " + logicalAxiom);
			processAxiom(ont, df, mngr, acetext, axiomVerbalizer, iri, logicalAxiom);
		}
	}


	/**
	 * <p>For each entity:
	 * add morphological annotations (CN_sg, CN_pl, TV_sg, TV_pl, TV_vbg, PN_sg) to the ontology.</p>
	 * 
	 * @param ontologyManager
	 * @param df
	 * @param ont
	 * @param entities
	 */
	private static void addMorfAnnotations(OWLOntologyManager ontologyManager, OWLDataFactory df, OWLOntology ont, Set<OWLEntity> entities) {
		List<AddAxiom> changes = Lists.newArrayList();
		for (OWLEntity entity : entities) {
			if (Showing.isShow(entity)) {
				Set<OWLAnnotationAssertionAxiom> annSet = MorphAnnotation.getMorphAnnotations(df, ont, entity);
				//logger.info("Init: entity " + entity + " adding annotations: " + annSet);
				for (OWLAnnotationAssertionAxiom ax : annSet) {
					changes.add(new AddAxiom(ont, ax));
				}
			}
		}
		OntologyUtils.changeOntology(ontologyManager, changes);
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
		ACEViewPreferences prefs = ACEViewPreferences.getInstance();

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
			OWLOntologyID oid = changeOnt.getOntologyID();
			OWLAxiom axiom = change.getAxiom();
			ACEText<OWLEntity, OWLLogicalAxiom> acetext = ACETextManager.getACEText(oid);
			TokenMapper acelexicon = acetext.getTokenMapper();

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
					Set<OWLEntity> entities = ((OWLAxiomChange) change).getEntities();
					logger.info("Add axiom: " + logicalAxiom + " (" + entities.size() + " entities)");
					try {
						AxiomVerbalizer axiomVerbalizer = new AxiomVerbalizer(prefs.getOwlToAce());
						processAxiom(changeOnt, df, ontologyManager, acetext, axiomVerbalizer, oid, logicalAxiom);
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
			else if (axiom instanceof OWLAnnotationAssertionAxiom) {
				//logger.info("Processing annotation: " + axiom);
				OWLAnnotationAssertionAxiom annAx = (OWLAnnotationAssertionAxiom) axiom;
				IRI annotationIRI = annAx.getProperty().getIRI();

				if (MorphType.isMorphTypeIRI(annotationIRI)) {
					OWLAnnotationSubject subject = annAx.getSubject();

					if (subject instanceof IRI) {
						String annValue = getAnnotationValueAsString(annAx.getValue());

						if (annValue == null) {
							// The annotation value is not a constant.
							logger.error("Malformed ACE lexicon annotation ignored: " + annAx);
						}
						else {
							lexiconAxiomCounter++;
							if (change instanceof AddAxiom) {
								acelexicon.addEntry(annValue, (IRI) subject, annotationIRI);
							}
							else if (change instanceof RemoveAxiom) {
								acelexicon.removeEntry(annValue, (IRI) subject, annotationIRI);
							}
						}
					}
				}
			}
			else if (axiom instanceof OWLDeclarationAxiom) {
				if (prefs.isUseLexicon()) {
					OWLDeclarationAxiom declarationAxiom = (OWLDeclarationAxiom) axiom;

					if (change instanceof AddAxiom) {
						OWLEntity entity = declarationAxiom.getEntity();
						logger.info("Add declaration axiom: " + entity);
						Set<OWLAnnotationAssertionAxiom> morphAnnotations = MorphAnnotation.getMorphAnnotations(df, changeOnt, entity);
						logger.info("Triggered: add: " + morphAnnotations);
						ACETextManager.addAxiomsToOntology(ontologyManager, changeOnt, morphAnnotations);
					}
					else if (change instanceof RemoveAxiom) {
						// TODO: BUG: We probably do not need to do anything here
						// as nothing changes for the ACE text and the lexicon is
						// changed if respective morph. annotations are removed (which
						// happens when an entity is undeclared).
						logger.info("Del declaration axiom (not handling): " + declarationAxiom);
					}
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
	 *    <li>add the verbalization as an annotation to the axiom</li>
	 *    <li>renew the axiom in the ontology (i.e. remove it and add it back with the annotation),
	 *    triggering a new AddAxiom-event which will be picked up by case 1.</li>
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
	private static void processAxiom(OWLOntology ont, OWLDataFactory df, OWLOntologyManager mngr, ACEText acetext, AxiomVerbalizer axiomVerbalizer, OWLOntologyID ns, OWLLogicalAxiom logicalAxiom) throws OWLRendererException, OWLOntologyCreationException, OWLOntologyChangeException {

		// TODO: BUG: This is here only to avoid an infinite loop
		// which occurs because annotation of SWRL-rules does not work
		// yet in Protege 4.1.
		if (logicalAxiom instanceof SWRLRule) {
			return;
		}

		ACESnippet newSnippet = null;

		for (OWLAnnotation annotation : logicalAxiom.getAnnotations(acetextAnnProp)) {
			String aceAnnotationValue = getAnnotationValueAsString(annotation.getValue());
			if (aceAnnotationValue == null) {
				logger.error("Malformed ACE annotation ignored: " + annotation);
			}
			else {
				logger.info("ACE annotation: " + aceAnnotationValue);
				newSnippet = new ACESnippetImpl(ns, aceAnnotationValue, logicalAxiom);
				acetext.add(newSnippet);
				ACETextManager.setSelectedSnippet(newSnippet);
			}
		}

		if (newSnippet == null) {
			newSnippet = axiomVerbalizer.verbalizeAxiom(logicalAxiom, ont);
			if (newSnippet == null) {
				logger.warn("AxiomVerbalizer produced a null-snippet for: " + logicalAxiom);
			}
			else {
				// TODO: use instead?: List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
				List<OWLAxiomChange> changes = Lists.newArrayList();
				// Remove the axiom from the ontology
				changes.add(new RemoveAxiom(ont, logicalAxiom));
				// Add back the axiom, but now with the additional annotation.
				changes.add(new AddAxiom(ont, ACETextManager.annotateAxiomWithSnippet(df, logicalAxiom, newSnippet)));
				// Apply the changes to the ontology
				OntologyUtils.changeOntology(mngr, changes);
			}
		}


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
		//		if (logicalAxiom instanceof OWLInverseObjectPropertiesAxiom) {
		//			OWLInverseObjectPropertiesAxiom iopa = (OWLInverseObjectPropertiesAxiom) logicalAxiom;
		//			if (! iopa.getFirstProperty().isAnonymous() && ! iopa.getSecondProperty().isAnonymous()) {
		//				OWLObjectProperty p1 = iopa.getFirstProperty().asOWLObjectProperty();
		//				OWLObjectProperty p2 = iopa.getSecondProperty().asOWLObjectProperty();
		//
		//				// BUG: removes all, not just the vbg-annotations
		//				OntologyUtils.changeOntology(mngr,
		//						ACETextManager.getRemoveChanges(ont, p1.getAnnotationAssertionAxioms(ont)));
		//				Set<OWLAxiom> set = Sets.newHashSet();
		//				set.add(getTV_vbg(df, p1, p2));
		//				set.add(getTV_vbg(df, p2, p1));
		//				ACETextManager.addAxiomsToOntology(mngr, ont, set);
		//			}
		//		}
	}


	/**
	 * <p>The annotation value can be either a literal or an anonymous
	 * individual. We are interested only in literals and only in their
	 * lexical value, and not the lang-attribute nor the datatype.
	 * If the annotation value is not a literal then <code>null</code>
	 * is returned.</p>
	 * 
	 * @param value Annotation value
	 * @return Lexical value of the literal or <code>null</code>
	 */
	private static String getAnnotationValueAsString(OWLAnnotationValue value) {
		if (value instanceof OWLLiteral) {
			return ((OWLLiteral) value).getLiteral();
		}
		return null;
	}


	/**
	 * 
	 * @param df
	 * @param p1
	 * @param p2
	 * @return A single <code>OWLEntityAnnotationAxiom</code>
	 */
	//	private static OWLAnnotationAssertionAxiom getTV_vbg(OWLDataFactory df, OWLObjectProperty p1, OWLObjectProperty p2) {
	//		String vbgForm = p2.getIRI().getFragment();
	//		// TODO: use the rendering instead
	//		//String vbgForm = getOWLModelManager().getRendering(p2);
	//		return OntologyUtils.createIRIAnnotationAxiom(df, MorphType.TV_VBG.getIRI(), p1.getIRI(), vbgForm);
	//	}


	private static List<OWLAxiomChange> addMorfAnnotations(OWLDataFactory df, OWLOntology ont, OWLEntity entity, String lemma) {
		List<OWLAxiomChange> addList = Lists.newArrayList();
		for (OWLAnnotationAssertionAxiom ax : MorphAnnotation.createMorphAnnotations(df, entity, lemma)) {
			addList.add(new AddAxiom(ont, ax));
		}
		return addList;
	}


	/**
	 * <p>Removes all the morph. annotation axioms that apply to
	 * the IRI of the given entity.</p>
	 * 
	 * @param ont
	 * @param entity
	 * @return List of RemoveAxiom-changes
	 */
	private static List<OWLAxiomChange> removeMorfAnnotations(OWLOntology ont, OWLEntity entity) {
		List<OWLAxiomChange> removeList = Lists.newArrayList();
		for (OWLAnnotationAssertionAxiom ax: entity.getAnnotationAssertionAxioms(ont)) {
			if (MorphType.isMorphTypeIRI(ax.getProperty().getIRI())) {
				removeList.add(new RemoveAxiom(ont, ax));
			}
		}

		return removeList;
	}

}