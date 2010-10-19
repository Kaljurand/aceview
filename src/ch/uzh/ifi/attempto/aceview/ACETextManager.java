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
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
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

import ch.uzh.ifi.attempto.ace.ACESentence;
import ch.uzh.ifi.attempto.aceview.lexicon.EntryType;
import ch.uzh.ifi.attempto.aceview.lexicon.MorphType;
import ch.uzh.ifi.attempto.aceview.lexicon.TokenMapper;
import ch.uzh.ifi.attempto.aceview.model.event.ACESnippetEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACESnippetListener;
import ch.uzh.ifi.attempto.aceview.model.event.ACETextChangeEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACETextManagerListener;
import ch.uzh.ifi.attempto.aceview.model.event.EventType;
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
	//public static final URI acetextURI = URI.create("http://attempto.ifi.uzh.ch/acetext#acetext");

	//private static final IRI timestampIRI = IRI.create("http://purl.org/dc/elements/1.1/date");
	//private static final URI timestampURI = URI.create("http://purl.org/dc/elements/1.1/date");

	private static final Map<OWLOntologyID, ACEText<OWLEntity, OWLLogicalAxiom>> acetexts = Maps.newHashMap();
	private static OWLModelManager owlModelManager;
	private static OWLOntologyID activeACETextID;

	// BUG: maybe we should get a new instance whenever we need to query the renderer preferences?
	private static final OWLRendererPreferences owlRendererPreferences = OWLRendererPreferences.getInstance();

	// One snippet can be singled out by "selecting" it.
	// In this case, selectedSnippet refers to it.
	private static ACESnippet selectedSnippet;

	// One snippet can be singled out with the "Why?"-button.
	// In this case, whySnippet refers to it.
	private static ACESnippet whySnippet;


	private static final List<ACETextManagerListener> aceTextManagerChangeListeners = Lists.newArrayList();
	private static final List<ACESnippetListener> snippetListeners = Lists.newArrayList();

	private static boolean isInitCompleted = false;

	// No instances allowed
	private ACETextManager() {}

	public static void createACEText(OWLOntologyID id) {
		ACEText<OWLEntity, OWLLogicalAxiom> acetext = new ACETextImpl();
		acetexts.put(id, acetext);
		// BUG: would be better if we didn't have to set the active URI here
		activeACETextID = id;
	}


	public static void setActiveACETextID(OWLOntologyID id) {
		if (activeACETextID.compareTo(id) != 0) {
			activeACETextID = id;
			fireEvent(EventType.ACTIVE_ACETEXT_CHANGED);
		}
	}


	/**
	 * <p>Returns the URI of the active ACE text.
	 * In case no ACE text has been set as active
	 * then returns <code>null</code>.</p>
	 * 
	 * @return URI of the active ACE text.
	 */
	public static OWLOntologyID getActiveACETextID() {
		return activeACETextID;
	}


	public static ACEText<OWLEntity, OWLLogicalAxiom> getActiveACEText() {
		return getACEText(getActiveACETextID());
	}


	public static ACEText<OWLEntity, OWLLogicalAxiom> getACEText(OWLOntologyID id) {
		if (id == null) {
			logger.error("getACEText: ID == null; THIS SHOULD NOT HAPPEN");
			return new ACETextImpl();
		}
		ACEText<OWLEntity, OWLLogicalAxiom> acetext = acetexts.get(id);
		if (acetext == null) {
			logger.error("getACEText: acetext == null, where ID: " + id);
			createACEText(id);
			return getACEText(id);
		}
		return acetext;
	}


	public static TokenMapper getActiveACELexicon() {
		return getACEText(activeACETextID).getTokenMapper();
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
		fireEvent(EventType.ACETEXT_CHANGED);
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
		fireEvent(EventType.ACETEXT_CHANGED);
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

		for (ACESentence sentence : addedSentences) {
			ACESnippet snippet = new ACESnippetImpl(activeACETextID, sentence);
			activeAceText.add(snippet);
			changes.addAll(getAddChanges(owlModelManager.getActiveOntology(), snippet));
		}

		for (ACESentence sentence : removedSentences) {
			changes.addAll(findAndRemove(sentence));
		}

		if (! (addedSentences.isEmpty() && removedSentences.isEmpty())) {
			changeOntology(changes);
			fireEvent(EventType.ACETEXT_CHANGED);
		}
	}

	public static void addAndRemoveItems(Collection<List<ACESentence>> addedSentences, Collection<ACESnippet> removedSnippets) {
		ACEText<OWLEntity, OWLLogicalAxiom> activeAceText = getActiveACEText();
		List<OWLAxiomChange> changes = Lists.newArrayList();

		for (List<ACESentence> sentenceList : addedSentences) {
			ACESnippet snippet = new ACESnippetImpl(activeACETextID, sentenceList);
			activeAceText.add(snippet);
			changes.addAll(getAddChanges(owlModelManager.getActiveOntology(), snippet));
		}

		for (ACESnippet oldSnippet : removedSnippets) {
			Set<OWLLogicalAxiom> removedAxioms = activeAceText.remove(oldSnippet);
			changes.addAll(getRemoveChanges(owlModelManager.getActiveOntology(), removedAxioms));
		}

		if (! (addedSentences.isEmpty() && removedSnippets.isEmpty())) {
			changeOntology(changes);
			fireEvent(EventType.ACETEXT_CHANGED);
		}
	}


	public static void addListener(ACETextManagerListener listener) {
		aceTextManagerChangeListeners.add(listener);
	}

	public static void removeListener(ACETextManagerListener listener) {
		aceTextManagerChangeListeners.remove(listener);
	}


	public static void addSnippetListener(ACESnippetListener listener) {
		snippetListeners.add(listener);
	}

	public static void removeSnippetListener(ACESnippetListener listener) {
		snippetListeners.remove(listener);
	}


	// TODO: should be private
	public static void fireEvent(EventType type) {
		if (isInitCompleted) {
			ACETextChangeEvent event = new ACETextChangeEvent(type);
			logger.info("Event: " + event.getType());
			for (ACETextManagerListener listener : aceTextManagerChangeListeners) {
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
		return OWLManager.createOWLOntologyManager();
		//if (owlModelManager == null) {
		//	OWLManager.createOWLOntologyManager();
		//}
		//return OWLManager.createOWLOntologyManager(owlModelManager.getOWLDataFactory());
	}


	public static String wrapInHtml(String body) {
		return wrapInHtml(getHtmlHead(), body);
	}


	/**
	 * <p>Finds (a single) OWL entity based on the <code>EntryType</code> and a lemma of
	 * an ACE word.</p>
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
					if (entity.toString().equals(lemma)) {
						return entity;
					}
				}
			}
		}
		return null;
	}

	// TODO: there must be a better way to do this
	public static OWLEntity mapAnnotationSubjectToEntity(OWLAnnotationSubject subject, IRI uri) {
		if (subject instanceof IRI) {
			IRI iri = (IRI) subject;
			return findEntity(MorphType.getMorphType(uri).getEntryType(), iri.getFragment());
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


	public static List<? extends OWLAxiomChange> getRemoveChanges(OWLOntology ontology, Set<? extends OWLAxiom> axioms) {
		List<RemoveAxiomByACEView> changes = Lists.newArrayList();
		for (OWLAxiom ax : axioms) {
			// Remove only if contains.
			if (ontology.containsAxiom(ax)) {
				changes.add(new RemoveAxiomByACEView(ontology, ax));
			}
			else {
				logger.error("Cannot remove, ontology does not contain: " + ax);
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
	 * @return List of annotations for the given snippet
	 */
	public static List<OWLAnnotation> getAnnotations(ACESnippet snippet) {
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
	 * <p>Returns a list of changes that would remove all the annotations
	 * from the given ontology, that annotate the given entity and have
	 * the given IRI as the annotation IRI.</p>
	 * 
	 * @param ont OWL ontology
	 * @param entity OWL entity
	 * @param iri IRI of the annotation
	 * @return List of remove-changes
	 */
	public static List<RemoveAxiomByACEView> findEntityAnnotationAxioms(OWLOntology ont, OWLEntity entity, IRI iri) {
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
		changeOntology(getAddChanges(owlModelManager.getActiveOntology(), snippet));
		fireEvent(EventType.ACETEXT_CHANGED);
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
				ACESnippet snippet = new ACESnippetImpl(activeACETextID, sentences);
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

		for (OWLLogicalAxiom axiom : snippetAxioms) {
			changes.add(new AddAxiomByACEView(ontology, axiom));
		}

		// In case the snippet has only one axiom, then we annotate it as well
		if (snippetAxioms.size() == 1) {
			// TODO: add the ACE annotation together with the axiom
			/*
			OWLDataFactory df = owlModelManager.getOWLDataFactory();
			OWLLogicalAxiom axiom = snippetAxioms.iterator().next();
			OWLAxiomAnnotationAxiom annAcetext = OntologyUtils.createAxiomAnnotation(df, axiom, acetextURI, snippet.toString());
			OWLAxiomAnnotationAxiom annTimestamp = OntologyUtils.createAxiomAnnotation(df, axiom, timestampURI, snippet.getTimestamp().toString());
			changes.add(new AddAxiomByACEView(ontology, annAcetext));
			changes.add(new AddAxiomByACEView(ontology, annTimestamp));
			 */
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


	private static void fireSnippetEvent(SnippetEventType type) {
		ACESnippetEvent event = new ACESnippetEvent(type);
		logger.info("Event: " + event.getType());
		for (ACESnippetListener listener : snippetListeners) {
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
}