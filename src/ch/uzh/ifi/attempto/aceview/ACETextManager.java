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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.protege.editor.owl.model.parser.ProtegeOWLEntityChecker;
import org.protege.editor.owl.ui.renderer.OWLRendererPreferences;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.ace.ACESentence;
import ch.uzh.ifi.attempto.aceview.lexicon.EntryType;
import ch.uzh.ifi.attempto.aceview.lexicon.LexiconUtils;
import ch.uzh.ifi.attempto.aceview.lexicon.TokenMapper;
import ch.uzh.ifi.attempto.aceview.lexicon.TokenMapperImpl;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewListener;
import ch.uzh.ifi.attempto.aceview.model.event.TextEventType;
import ch.uzh.ifi.attempto.aceview.model.event.SnippetEventType;
import ch.uzh.ifi.attempto.aceview.util.OntologyUtils;
import ch.uzh.ifi.attempto.owl.VerbalizerWebservice;

/**
 * <p>The ACE text manager keeps track of the open ACE texts, how
 * they map to open OWL ontologies, and which one of them is active.</p>
 * 
 * <p>The ACE text manager allows snippets to be added to and removed from
 * the ACE texts so that the corresponding ontology is updated
 * by adding/removing the affected axioms.</p>
 * 
 * <p>Note: selected snippet is independent from the ACE text, e.g. we can select a snippet
 * which does not belong to any text, e.g. entailed snippets are such.</p>
 * 
 * @author Kaarel Kaljurand
 */
public final class ACETextManager {
	private static final Logger logger = Logger.getLogger(ACETextManager.class);

	public static final IRI acetextIRI = IRI.create("http://attempto.ifi.uzh.ch/acetext#acetext");
	private static final IRI timestampIRI = IRI.create("http://purl.org/dc/elements/1.1/date");

	private static final Map<OWLOntologyID, ACEText<OWLEntity, OWLLogicalAxiom>> acetexts = Maps.newHashMap();
	private static final Map<OWLOntologyID, TokenMapper> acelexicons = Maps.newHashMap();
	private static OWLModelManager owlModelManager;

	// BUG: maybe we should get a new instance whenever we need to query the renderer preferences?
	private static final OWLRendererPreferences owlRendererPreferences = OWLRendererPreferences.getInstance();

	// One snippet can be singled out by "selecting" it.
	// In this case, selectedSnippet refers to it.
	private static ACESnippet selectedSnippet;

	// One snippet can be singled out with the "Why?"-button.
	// In this case, whySnippet refers to it.
	private static ACESnippet whySnippet;


	private static final List<ACEViewListener<ACEViewEvent<TextEventType>>> aceTextManagerChangeListeners = Lists.newArrayList();
	private static final List<ACEViewListener<ACEViewEvent<SnippetEventType>>> snippetListeners = Lists.newArrayList();

	private static boolean isInitCompleted = false;

	// No instances allowed
	private ACETextManager() {}


	/**
	 * <p>If <code>acetiveACETextID == null</code> then it means that
	 * no ACE text has been created yet as one ACE text must
	 * always be active. In this case we create a new ACE text
	 * and set it active. Otherwise we change the active ACE text
	 * according to the given ID.</p>
	 * 
	 * @param id
	 */
	public static void setActiveACETextID(OWLOntologyID id) {
		logger.info("Active ontology ID changed to: " + id);
		fireEvent(TextEventType.ACTIVE_ACETEXT_CHANGED);
	}


	public static ACEText<OWLEntity, OWLLogicalAxiom> getActiveACEText() {
		OWLOntologyID id = getActiveID();
		if (id == null) {
			return null;
		}
		return getACEText(id);
	}


	public static ACEText<OWLEntity, OWLLogicalAxiom> getACEText(OWLOntologyID id) {
		if (id == null) {
			// TODO: throw exception here
			logger.error("getACEText: ID == null; THIS SHOULD NOT HAPPEN");
			return null;
		}
		ACEText<OWLEntity, OWLLogicalAxiom> acetext = acetexts.get(id);
		if (acetext == null) {
			logger.error("Creating a new ACE text and setting it active: " + id);
			acetext = new ACETextImpl();
			acetexts.put(id, acetext);
		}
		return acetext;
	}


	public static TokenMapper getActiveACELexicon() {
		OWLOntologyID id = getActiveID();
		if (id == null) {
			return null;
		}
		return getACELexicon(id);
	}

	/**
	 * <p>Returns the ACE lexicon (TokenMapper)
	 * that decides the surface forms of the snippets in this text.</p>
	 * 
	 * @param id
	 * @return Lexicon
	 */
	public static TokenMapper getACELexicon(OWLOntologyID id) {
		if (id == null) {
			// TODO: throw exception here
			logger.error("getACELexicon: ID == null; THIS SHOULD NOT HAPPEN");
			return null;
		}
		TokenMapper tokenMapper = acelexicons.get(id);
		if (tokenMapper == null) {
			logger.error("Creating a new ACE lexicon and setting it active: " + id);
			tokenMapper = new TokenMapperImpl();
			acelexicons.put(id, tokenMapper);
		}
		return tokenMapper;
	}


	public static void setOWLModelManager(OWLModelManager mm) {
		owlModelManager = mm;
	}


	public static OWLModelManager getOWLModelManager() {
		return owlModelManager;
	}


	/**
	 * <p>Adds the given snippet to the active ACE text, and
	 * adds the axioms of the snippet to the ontology that corresponds
	 * to the ACE text.</p>
	 * 
	 * @param snippet ACE snippet
	 */
	public static void addSnippet(ACESnippet snippet) {
		addSnippet(getActiveACEText(), snippet);
	}


	/**
	 * <p>Removes the given snippet from the active ACE text, and
	 * removes the axioms of the snippet from the ontology that corresponds
	 * to the ACE text.</p>
	 * 
	 * @param snippet ACE snippet
	 */
	public static void removeSnippet(ACESnippet snippet) {
		Set<OWLLogicalAxiom> removedAxioms = getActiveACEText().remove(snippet);
		changeOntology(getRemoveChanges(owlModelManager.getActiveOntology(), removedAxioms));
		fireEvent(TextEventType.ACETEXT_CHANGED);
	}


	/**
	 * <p>Updates the given snippet in the active text at the given index
	 * by first removing the snippet,
	 * then creating a new snippet out of the set of given sentences, and then
	 * adding the new snippet to the text and setting it as the selected snippet.</p>
	 * 
	 * @param index Index of the snippet in the ACE text
	 * @param snippet Snippet to be updated (i.e replaced)
	 * @param sentences Sentences that form the new snippet
	 */
	public static void updateSnippet(int index, ACESnippet snippet, List<ACESentence> sentences) {
		ACESnippet newSnippet = new ACESnippetImpl(snippet.getDefaultNamespace(), sentences);
		ACEText<OWLEntity, OWLLogicalAxiom> acetext = getActiveACEText();
		logger.info("Del old snippet: " + snippet);
		Set<OWLLogicalAxiom> removedAxioms = acetext.remove(snippet);
		logger.info("Add new snippet: " + newSnippet);
		acetext.add(index, newSnippet);

		OWLOntology ontology = owlModelManager.getActiveOntology();
		List<OWLAxiomChange> changes = Lists.newArrayList();
		changes.addAll(getRemoveChanges(ontology, removedAxioms));
		changes.addAll(getAddChanges(ontology, newSnippet));
		changeOntology(changes);
		setSelectedSnippet(newSnippet);
		fireEvent(TextEventType.ACETEXT_CHANGED);
	}


	/**
	 * <p>Adds a collection of ACE sentences and removes another collection
	 * of ACE sentences to/from the active ACE text.</p>
	 * 
	 * @param addedSentences Collection of ACE sentences
	 * @param removedSentences Collection of ACE sentences
	 */
	public static void addAndRemoveSentences(Collection<ACESentence> addedSentences, Collection<ACESentence> removedSentences) {
		ACEText<OWLEntity, OWLLogicalAxiom> activeAceText = getActiveACEText();
		List<OWLAxiomChange> changes = Lists.newArrayList();
		OWLOntology ont = owlModelManager.getActiveOntology();

		for (ACESentence sentence : addedSentences) {
			ACESnippet snippet = new ACESnippetImpl(ont.getOntologyID(), sentence);
			activeAceText.add(snippet);
			changes.addAll(getAddChanges(ont, snippet));
		}

		for (ACESentence sentence : removedSentences) {
			changes.addAll(findAndRemove(sentence));
		}

		if (! (addedSentences.isEmpty() && removedSentences.isEmpty())) {
			changeOntology(changes);
			fireEvent(TextEventType.ACETEXT_CHANGED);
		}
	}

	public static void addAndRemoveItems(Collection<List<ACESentence>> addedSentences, Collection<ACESnippet> removedSnippets) {
		ACEText<OWLEntity, OWLLogicalAxiom> activeAceText = getActiveACEText();
		List<OWLAxiomChange> changes = Lists.newArrayList();
		OWLOntology ont = owlModelManager.getActiveOntology();

		for (List<ACESentence> sentenceList : addedSentences) {
			ACESnippet snippet = new ACESnippetImpl(ont.getOntologyID(), sentenceList);
			activeAceText.add(snippet);
			changes.addAll(getAddChanges(ont, snippet));
		}

		for (ACESnippet oldSnippet : removedSnippets) {
			Set<OWLLogicalAxiom> removedAxioms = activeAceText.remove(oldSnippet);
			changes.addAll(getRemoveChanges(ont, removedAxioms));
		}

		if (! (addedSentences.isEmpty() && removedSnippets.isEmpty())) {
			changeOntology(changes);
			fireEvent(TextEventType.ACETEXT_CHANGED);
		}
	}


	public static void addListener(ACEViewListener<ACEViewEvent<TextEventType>> listener) {
		aceTextManagerChangeListeners.add(listener);
	}

	public static void removeListener(ACEViewListener<ACEViewEvent<TextEventType>> listener) {
		aceTextManagerChangeListeners.remove(listener);
	}


	public static void addSnippetListener(ACEViewListener<ACEViewEvent<SnippetEventType>> listener) {
		snippetListeners.add(listener);
	}

	public static void removeSnippetListener(ACEViewListener<ACEViewEvent<SnippetEventType>> listener) {
		snippetListeners.remove(listener);
	}


	// TODO: should be private
	public static void fireEvent(TextEventType type) {
		if (isInitCompleted) {
			ACEViewEvent<TextEventType> event = new ACEViewEvent<TextEventType>(type);
			logger.info("Event: " + event.getType());
			for (ACEViewListener<ACEViewEvent<TextEventType>> listener : aceTextManagerChangeListeners) {
				try {
					listener.handleChange(event);
				}
				catch (Exception e) {
					logger.error("Detaching " + listener.getClass().getName() + " because it threw " + e.toString());
					ProtegeApplication.getErrorLog().logError(e);
					removeListener(listener);
				}
			}
		}
	}


	// TODO: This is called only from ACEViewTab
	public static void addAxiomsToOntology(OWLOntologyManager ontologyManager, OWLOntology ontology, Set<? extends OWLAxiom> axioms) {
		List<AddAxiomByACEView> changes = Lists.newArrayList();
		for (OWLAxiom ax : axioms) {
			changes.add(new AddAxiomByACEView(ontology, ax));
		}
		OntologyUtils.changeOntology(ontologyManager, changes);
	}


	/**
	 * <p>Creates a new OWL ontology manager, but uses an existing
	 * OWL data factory.</p>
	 * 
	 * @return OWL ontology manager
	 */
	public static OWLOntologyManager createOWLOntologyManager() {
		if (owlModelManager == null) {
			return OWLManager.createOWLOntologyManager();
		}
		return OWLManager.createOWLOntologyManager(owlModelManager.getOWLDataFactory());
	}


	public static String wrapInHtml(String body) {
		return wrapInHtml(getHtmlHead(), body);
	}


	/**
	 * @deprecated
	 * 
	 * <p>Finds (a single) OWL entity based on the <code>EntryType</code> and a lemma of
	 * an ACE word.</p>
	 * 
	 * TODO: "lemma of an ACE word" should really be "IRI of an OWL entity"!
	 * 
	 * FIXED: now using "false" in the getMatching*() calls.
	 * We are interested in an exact match and not a prefix or regexp match.
	 * Note that <code>getEntities(String)</code> does either wildcard or regexp matching,
	 * depending on the preferences. Therefore, we should escape all the
	 * wildcard symbols in the content words before we start matching.
	 * Maybe there is a less powerful entity finder somewhere, we don't really
	 * need regexp support when clicking on the words.
	 * 
	 * TODO: Get rid of this method. It is only used by WordsHyperlinkListener, which
	 * we should also remove, and replace it with a view which can hold the entities and
	 * thus does not have search them via some string-based encoding (which is slow).
	 *
	 * @param type Type (word class) of the lemma
	 * @param lemma Lemma of a word
	 * @return A single OWL entity that corresponds to the type-lemma combination
	 */
	public static OWLEntity findEntity(EntryType type, String lemma) {
		if (lemma != null) {
			Set<? extends OWLEntity> entities;
			OWLEntityFinder entityFinder = getOWLModelManager().getOWLEntityFinder();
			switch (type) {
			case CN:
				entities = entityFinder.getMatchingOWLClasses(lemma, false);
				break;
			case TV:
				entities = entityFinder.getMatchingOWLObjectProperties(lemma, false);
				if (entities == null || entities.isEmpty()) {
					entities = entityFinder.getMatchingOWLDataProperties(lemma, false);
				}
				break;
			case PN:
				entities = entityFinder.getMatchingOWLIndividuals(lemma, false);
				break;
			default:
				throw new RuntimeException("findEntity: Programmer error");
			}

			if (entities != null) {
				for (OWLEntity entity : entities) {
					if (getRendering(entity).equals(lemma)) {
						return entity;
					}
				}
			}
		}
		return null;
	}


	/**
	 * <p>Returns the entity that matches the IRI and the ACE word class
	 * (CN, TV, PN).</p>
	 * 
	 * TODO: Think about object and data properties. If TV corresponds to both
	 * then this method has to deal with the ambiguity.
	 * 
	 * @param type ACE word class (CN, TV, PN)
	 * @param iri OWL entity IRI
	 * @return OWL entity that matches the ACE word class and the IRI
	 */
	public static OWLEntity findEntity(EntryType type, IRI iri) {
		OWLEntityFinder entityFinder = getOWLModelManager().getOWLEntityFinder();
		Set<? extends OWLEntity> entities = entityFinder.getEntities(iri);

		for (OWLEntity entity : entities) {
			if (LexiconUtils.getLexiconEntryType(entity).equals(type)) {
				return entity;
			}
		}
		return null;
	}


	/**
	 * <p>Creates an ACE snippet from the given OWL axioms,
	 * and selects the created snippet.</p>
	 *
	 * @param axiom OWL axiom
	 * @throws OWLRendererException
	 * @throws OWLOntologyCreationException
	 * @throws OWLOntologyChangeException
	 */
	public static void setSelectedSnippet(OWLLogicalAxiom axiom) throws OWLRendererException, OWLOntologyCreationException, OWLOntologyChangeException {
		ACESnippet snippet = makeSnippetFromAxiom(axiom);
		setSelectedSnippet(snippet);
	}


	/**
	 * <p>Selects the given snippet. Note that if the given snippet
	 * is already selected, then it is reselected. This is needed
	 * in order to refresh the views, if the only change was
	 * in terms of axiom annotations added to the snippet's axiom.</p>
	 * 
	 * @param snippet ACE snippet
	 */
	public static void setSelectedSnippet(ACESnippet snippet) {
		selectedSnippet = snippet;
		logger.info("Selected: " + snippet);
		fireSnippetEvent(SnippetEventType.SELECTED_SNIPPET_CHANGED);
	}


	public static ACESnippet getSelectedSnippet() {
		return selectedSnippet;
	}


	public static void setWhySnippet(ACESnippet snippet) {
		whySnippet = snippet;
		logger.info("Why: " + snippet);
		fireSnippetEvent(SnippetEventType.WHY_SNIPPET_CHANGED);
	}


	public static ACESnippet getWhySnippet() {
		return whySnippet;
	}


	public static void setInitCompleted(boolean b) {
		isInitCompleted = b;
	}


	/**
	 * <p>Remove the set of given logical axioms from the ontology, i.e. generate the respective
	 * list of changes. Note that the removed axioms do not have to match structurally against
	 * the given axioms.
	 * It is only important that the logical part matches, i.e. annotations are ignored.</p>
	 * 
	 * @param ont OWL ontology to be modified
	 * @param axioms Set of logical axioms to be removed
	 * @return List of axiom removal changes
	 */
	public static List<? extends OWLAxiomChange> getRemoveChanges(OWLOntology ont, Set<OWLLogicalAxiom> axioms) {
		List<RemoveAxiomByACEView> changes = Lists.newArrayList();
		for (OWLAxiom ax : axioms) {
			Set<OWLAxiom> axiomsToBeRemoved = ont.getAxiomsIgnoreAnnotations(ax);
			if (axiomsToBeRemoved.isEmpty()) {
				logger.error("Cannot remove, ontology does not contain: " + ax.getAxiomWithoutAnnotations());
			}
			else {
				for (OWLAxiom axToBeRemoved : axiomsToBeRemoved) {
					changes.add(new RemoveAxiomByACEView(ont, axToBeRemoved));
				}
			}
		}
		return changes;
	}


	/**
	 * <p>Converts the given OWL logical axiom into its corresponding ACE snippet.</p>
	 *  
	 * TODO: We should not necessarily set the namespace to be equivalent to the
	 * active ontology namespace.
	 * 
	 * @param axiom OWL axiom
	 * @return ACE snippet that corresponds to the given OWL axiom
	 * @throws OWLRendererException
	 * @throws OWLOntologyCreationException
	 * @throws OWLOntologyChangeException
	 */
	public static ACESnippet makeSnippetFromAxiom(OWLLogicalAxiom axiom) throws OWLRendererException, OWLOntologyCreationException, OWLOntologyChangeException {
		AxiomVerbalizer axiomVerbalizer = createAxiomVerbalizer();
		return axiomVerbalizer.verbalizeAxiom(axiom, getOWLModelManager().getActiveOntology());
	}


	public static void resetSelectedSnippet() {
		selectedSnippet = null;
		fireSnippetEvent(SnippetEventType.SELECTED_SNIPPET_CHANGED);		
	}


	public static void processTanglingAxioms(ACEText<OWLEntity, OWLLogicalAxiom> acetext, Set<OWLLogicalAxiom> tanglingAxioms) {		
		AxiomVerbalizer axiomVerbalizer = createAxiomVerbalizer();
		OWLOntology ont = getOWLModelManager().getActiveOntology();

		for (OWLLogicalAxiom newAxiom : tanglingAxioms) {
			logger.info("Adding back: " + newAxiom);
			verbalizeAndAdd(acetext, ont, axiomVerbalizer, newAxiom);
		}
	}


	/**
	 * <p>Returns a list of annotations that annotate the logical axioms of the given snippet.
	 * Only the ACE text annotation is not returned because this is already
	 * explicitly present in the snippet.</p>
	 *  
	 * TODO: Why do we return a list? Because it is simpler to update a table model in this way
	 * 
	 * @param snippet ACE snippet
	 * @return List of annotations for the given snippet
	 */
	public static List<OWLAnnotation> getAnnotationsExceptAcetext(ACESnippet snippet) {
		List<OWLAnnotation> annotations = Lists.newArrayList();
		for (OWLLogicalAxiom ax : snippet.getLogicalAxioms()) {
			for (OWLAnnotation annotation : ax.getAnnotations()) {
				if (! annotation.getProperty().getIRI().equals(ACETextManager.acetextIRI)) {
					annotations.add(annotation);
				}
			}
		}
		return annotations;
	}


	/**
	 * @deprecated
	 * 
	 * TODO: Remove: nothing is calling it.
	 * 
	 * <p>Returns a list of changes that would remove all the annotations
	 * from the given ontology, that annotate the given entity and have
	 * the given IRI as the annotation IRI.</p>
	 * 
	 * @param ont OWL ontology
	 * @param entity OWL entity
	 * @param iri IRI of the annotation
	 * @return List of remove-changes
	 */
	private static List<RemoveAxiomByACEView> findEntityAnnotationAxioms(OWLOntology ont, OWLEntity entity, IRI iri) {
		List<RemoveAxiomByACEView> axioms = Lists.newArrayList();
		for (OWLAnnotationAssertionAxiom axiom : entity.getAnnotationAssertionAxioms(ont)) {
			if (axiom.getProperty().getIRI().equals(iri)) {
				axioms.add(new RemoveAxiomByACEView(ont, axiom));
			}
		}
		return axioms;
	}


	/**
	 * <p>Returns the rendering of the entity, as decided by the current renderer
	 * in the model manager.</p>
	 * <p>The renderer adds quotes around strings that contains spaces
	 * (e.g. a label like "Eesti Vabariik"). We remove such quotes,
	 * otherwise it would confuse the sorter.</p>
	 * 
	 * @param entity OWL entity to be rendered
	 * @return Rendering without quotes
	 */
	public static String getRendering(OWLEntity entity) {
		String rendering = getOWLModelManager().getRendering(entity);
		if (rendering == null || rendering.length() == 0) {
			return "ACEVIEW_EMPTY_STRING";

		}
		return rendering.replace("'", "");
	}


	/**
	 * <p>Interprets the given ACE sentence as an expression in
	 * Manchester OWL Syntax and parses it to an OWL logical axiom.</p>
	 * 
	 * @param sentence
	 * @param base
	 * @return OWL logical axiom
	 * @throws ParserException
	 */
	public static OWLLogicalAxiom parseWithMos(ACESentence sentence, String base) throws ParserException {
		// Remove the last token (a dot or a question mark) of the given sentence.
		String mosStr = sentence.toMOSString();
		logger.info("Parsing with the MOS parser: " + mosStr);
		OWLModelManager mngr = getOWLModelManager();
		return OntologyUtils.parseWithMosParser(mngr.getOWLDataFactory(), new ProtegeOWLEntityChecker(mngr.getOWLEntityFinder()), base, mosStr);
	}


	/**
	 * <p>Adds the given snippet to the given ACE text, and
	 * adds the axioms of the snippet to the ontology that corresponds
	 * to the ACE text.</p>
	 * 
	 * @param acetext ACE text
	 * @param snippet ACE snippet
	 */
	private static void addSnippet(ACEText<OWLEntity, OWLLogicalAxiom> acetext, ACESnippet snippet) {
		acetext.add(snippet);
		// TODO: BUG: we should pick the ontology that corresponds to the
		// ACE text. This is not always the active ontology.
		changeOntology(getAddChanges(owlModelManager.getActiveOntology(), snippet));
		fireEvent(TextEventType.ACETEXT_CHANGED);
	}


	private static void changeOntology(List<? extends OWLAxiomChange> changes) {
		OntologyUtils.changeOntology(owlModelManager.getOWLOntologyManager(), changes);
	}


	// We make a defensive copy here, otherwise we would get a
	// ConcurrentModificationException
	private static List<OWLAxiomChange> findAndRemove(ACESentence sentence) {
		ACEText<OWLEntity, OWLLogicalAxiom> acetext = getActiveACEText();
		OWLOntology ontology = owlModelManager.getActiveOntology();
		List<OWLAxiomChange> changes = Lists.newArrayList();

		for (ACESnippet oldSnippet : ImmutableSet.copyOf(acetext.getSentenceSnippets(sentence))) {
			Set<OWLLogicalAxiom> removedAxioms = acetext.remove(oldSnippet);
			changes.addAll(getRemoveChanges(ontology, removedAxioms));

			if (oldSnippet.getSentences().size() > 1) {
				logger.info("Found super snippet: " + oldSnippet.toString());
				List<ACESentence> sentences = oldSnippet.getRest(sentence);
				ACESnippet snippet = new ACESnippetImpl(ontology.getOntologyID(), sentences);
				acetext.add(snippet);
				changes.addAll(getAddChanges(ontology, snippet));
			}
		}
		return changes;
	}


	// BUG: these HTML-generators should be in some other class
	private static String getHtmlHead() {
		String fontName = owlRendererPreferences.getFontName();
		int fontSize = owlRendererPreferences.getFontSize();
		return 	"<style type='text/css'>" +
		"	body { font-size: " + fontSize +"; font-family: " + fontName + "; margin-left: 4px; margin-right: 4px; margin-top: 4px; margin-bottom: 4px; background-color: #ffffee }" +
		"	table { border-width: 1px; border-style: solid; border-color: silver; empty-cells: show; border-collapse: collapse; margin-bottom: 1em }" +
		"	td { border-width: 1px; border-style: solid; border-color: silver }" +
		"	div { padding-left: 4px; padding-right: 4px; padding-top: 4px; padding-bottom: 4px;" +
		"		margin-left: 4px; margin-right: 4px; margin-top: 4px; margin-bottom: 4px }" +
		"	div.messages { border-width: 3px; border-color: red  }" +
		"	p.question { color: olive }" +
		"	.indent { margin-left: 15px }" +
		"	.error { color: red }" +
		"	a { text-decoration: none }" +
		"</style>";
	}


	private static String wrapInHtml(String head, String body) {
		return "<html><head>" + head + "</head><body>" + body + "</body></html>";
	}


	/**
	 * TODO: Currently we can only store the text and timestamp of the snippet
	 * if the snippet corresponds to a single axiom. Would be nice if a group of
	 * axioms could be annotated as well.
	 * 
	 * @param ontology OWL ontology
	 * @param snippet ACE snippet
	 */
	private static List<? extends OWLAxiomChange> getAddChanges(OWLOntology ontology, ACESnippet snippet) {
		List<AddAxiomByACEView> changes = Lists.newArrayList();
		Set<OWLLogicalAxiom> snippetAxioms = snippet.getLogicalAxioms();

		// If the snippet corresponds to a single axiom, then we
		// annotate this axiom with ACE View specific annotations.
		if (snippetAxioms.size() == 1) {
			OWLLogicalAxiom axiom = snippetAxioms.iterator().next();
			//OWLLogicalAxiom annotatedAxiom = annotateAxiomWithSnippet(owlModelManager.getOWLDataFactory(), axiom, snippet);
			//changes.add(new AddAxiomByACEView(ontology, annotatedAxiom));
			changes.add(new AddAxiomByACEView(ontology, axiom));
		}
		else {
			for (OWLLogicalAxiom axiom : snippetAxioms) {
				changes.add(new AddAxiomByACEView(ontology, axiom));
			}
		}

		return changes;
	}


	/**
	 * <p>Note that we do not add the axiom into the ontology, because
	 * we expect it to be there already, as it is one of the tangling
	 * axioms.</p>
	 * 
	 * @param acetext ACE text
	 * @param ont OWL ontology
	 * @param axiomVerbalizer AxiomVerbalizer
	 * @param axiom OWL axiom
	 */
	private static void verbalizeAndAdd(ACEText<OWLEntity, OWLLogicalAxiom> acetext, OWLOntology ont, AxiomVerbalizer axiomVerbalizer, OWLLogicalAxiom axiom) {
		ACESnippet snippet = null;
		try {
			snippet = axiomVerbalizer.verbalizeAxiom(axiom, ont);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (snippet != null) {
			acetext.add(snippet);
			// TODO: remove the axiom and add it back with the ACE annotation
			/*
			OWLDataFactory df = owlModelManager.getOWLDataFactory();
			OWLAxiomAnnotationAxiom annAcetext = OntologyUtils.createAxiomAnnotation(df, axiom, acetextIRI, snippet.toString());
			OWLAxiomAnnotationAxiom annTimestamp = OntologyUtils.createAxiomAnnotation(df, axiom, timestampURI, snippet.getTimestamp().toString());
			List<OWLAxiomChange> changes = Lists.newArrayList();
			changes.add(new AddAxiomByACEView(ont, annAcetext));
			changes.add(new AddAxiomByACEView(ont, annTimestamp));
			changeOntology(changes);
			 */
		}
		else {
			logger.warn("AxiomVerbalizer produced a null-snippet for: " + axiom.toString());
		}
	}


	private static AxiomVerbalizer createAxiomVerbalizer() {
		return new AxiomVerbalizer(
				new VerbalizerWebservice(ACEViewPreferences.getInstance().getOwlToAce()));
	}


	/**
	 * <p>Creates a new snippet from the given OWL axiom, and
	 * adds the snippet to the given ACE text.
	 * See also {@link #addSnippet(ACEText, ACESnippet)}.</p>
	 * 
	 * @param acetext ACE text
	 * @param axiom OWL logical axiom
	 */
	/*
	private static void addAxiom(ACEText<OWLEntity, OWLLogicalAxiom> acetext, OWLLogicalAxiom axiom) {		
		AxiomVerbalizer axiomVerbalizer = createAxiomVerbalizer(acetext.getACELexicon());
		OWLModelManager mm = getOWLModelManager();
		OWLOntology ont = mm.getActiveOntology();
		ACESnippet snippet = null;
		try {
			snippet = axiomVerbalizer.verbalizeAxiom(ont.getURI(), axiom);
			addSnippet(acetext, snippet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	 */


	/**
	 * @deprecated
	 * 
	 * <p>Constructs a new axiom on the basis of the given axiom.
	 * The new axiom will have some additional annotations based on
	 * the given snippet, namely the textual content of the snippet
	 * and the timestamp of the snippet.</p>
	 * 
	 * @param df OWLDataFactory
	 * @param axiom Axiom to be annotated
	 * @param snippet Snippet that provides the content of the annotations
	 * @return New axiom (i.e. old axiom with new annotations)
	 */
	public static OWLLogicalAxiom annotateAxiomWithSnippet(OWLDataFactory df, OWLLogicalAxiom axiom, ACESnippet snippet) {
		OWLAnnotationProperty acetextAnnProp = df.getOWLAnnotationProperty(acetextIRI);
		OWLAnnotationProperty timestampAnnProp = df.getOWLAnnotationProperty(timestampIRI);

		// Create a new annotation based on the snippet (i.e. the verbalization)
		OWLAnnotation acetextAnn = df.getOWLAnnotation(acetextAnnProp, df.getOWLLiteral(snippet.toString()));
		// and the snippet timestamp (TODO: use a more specific type, i.e. date type)
		OWLAnnotation timestampAnn = df.getOWLAnnotation(timestampAnnProp, df.getOWLLiteral(snippet.getTimestamp().toString()));

		// Add the new ACE text annotation to the existing annotations
		Set<OWLAnnotation> annotations = Sets.newHashSet(axiom.getAnnotations());
		annotations.add(acetextAnn);
		annotations.add(timestampAnn);
		return (OWLLogicalAxiom) axiom.getAxiomWithoutAnnotations().getAnnotatedAxiom(annotations);
	}


	private static void fireSnippetEvent(SnippetEventType type) {
		ACEViewEvent<SnippetEventType> event = new ACEViewEvent<SnippetEventType>(type);
		logger.info("Event: " + event.getType());
		for (ACEViewListener<ACEViewEvent<SnippetEventType>> listener : snippetListeners) {
			try {
				listener.handleChange(event);
			}
			catch (Exception e) {
				logger.error("Detaching " + listener.getClass().getName() + " because it threw " + e.toString());
				ProtegeApplication.getErrorLog().logError(e);
				removeSnippetListener(listener);
			}
		}
	}


	/**
	 * <p>Returns the id (OWLOntologyID) of the active ACE text.
	 * In case no ACE text has been set as active
	 * then returns <code>null</code>.</p>
	 * 
	 * @return ID of the active ACE text.
	 */
	private static OWLOntologyID getActiveID() {
		return owlModelManager.getActiveOntology().getOntologyID();
	}
}