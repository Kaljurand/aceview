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

package ch.uzh.ifi.attempto.aceview.ui.view;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JScrollPane;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.semanticweb.owlapi.inference.OWLReasoner;
import org.semanticweb.owlapi.inference.OWLReasonerException;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNaryPropertyAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredAxiomGeneratorException;
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentObjectPropertyAxiomGenerator;
import org.semanticweb.owlapi.util.InferredObjectPropertyCharacteristicAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredPropertyAssertionGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredSubObjectPropertyAxiomGenerator;

import ch.uzh.ifi.attempto.aceview.ACEViewPreferences;
import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.AxiomVerbalizer;
import ch.uzh.ifi.attempto.aceview.lexicon.ACELexicon;
import ch.uzh.ifi.attempto.aceview.model.EntailmentsTableModel;
import ch.uzh.ifi.attempto.aceview.ui.ACETable;
import ch.uzh.ifi.attempto.aceview.ui.util.TableColumnHelper;
import ch.uzh.ifi.attempto.aceview.util.OntologyUtils;
import ch.uzh.ifi.attempto.aceview.util.Showing;
import ch.uzh.ifi.attempto.owl.VerbalizerWebservice;

/**
 * <p>This view component presents the snippets that are entailed by the
 * asserted snippets.</p>
 *  
 * @author Kaarel Kaljurand
 */
public class ACEEntailmentsViewComponent extends AbstractACESnippetsViewComponent {

	private static final Logger logger = Logger.getLogger(ACEEntailmentsViewComponent.class);

	private final EntailmentsTableModel dtm = new EntailmentsTableModel();

	private final OWLModelManagerListener listener = new OWLModelManagerListener() {
		public void handleChange(OWLModelManagerChangeEvent event) {
			if (event.isType(EventType.ONTOLOGY_CLASSIFIED)) {
				dtm.increaseCounter();
				showEntailments();
				setHeaderText();
			}
		}
	};


	@Override
	public void disposeView() {
		getOWLModelManager().removeListener(listener);
		removeHierarchyListener(hierarchyListener);
	}

	@Override
	public void initialiseView() throws Exception {

		super.initialiseView();

		tableSnippets.setModel(dtm);
		tableSnippets.setToolTipText("List of entailed snippets. Double-click on a snippet to see its explanation.");
		TableColumnHelper.configureColumns(tableSnippets, EntailmentsTableModel.Column.values());

		JScrollPane scrollpaneEntailments = new JScrollPane(tableSnippets,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);


		setLayout(new BorderLayout());
		add(BorderLayout.NORTH, panelButtons);
		add(BorderLayout.CENTER, scrollpaneEntailments);

		tableSnippets.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount() == 2) {
					ACETable table = (ACETable) e.getSource();
					int row = table.rowAtPoint(e.getPoint());
					if (row != -1) {
						int rowModel = table.convertRowIndexToModel(row);
						int colModel = EntailmentsTableModel.Column.SNIPPET.ordinal();
						ACESnippet snippet = (ACESnippet) table.getModel().getValueAt(rowModel, colModel);
						ACETextManager.setWhySnippet(snippet);
					}
				}
			}
		});


		getOWLModelManager().addListener(listener);
		addHierarchyListener(hierarchyListener);
		refreshComponent();
		setHeaderText();
		showEntailments();
	}


	private void showEntailments() {
		try {
			getInferredSentences();
		} catch (OWLOntologyCreationException e) {
			logger.error(e.getMessage());
		} catch (InferredAxiomGeneratorException e) {
			logger.error(e.getMessage());
		} catch (OWLOntologyChangeException e) {
			logger.error(e.getMessage());
		} catch (OWLReasonerException e) {
			logger.error(e.getMessage());
		} catch (OWLRendererException e) {
			logger.error(e.getMessage());
		}
	}


	/**
	 * <p>Generates an inferred ontology, walks over all its axioms, verbalizes some of the axioms and
	 * adds some of them to the table model. The axioms to be added to the table model</p>
	 * 
	 * <ul>
	 * <li>must not be present as asserted axioms</li>
	 * <li>must not have the verbalization that matches an asserted snippet</li>
	 * </ul>
	 * 
	 * <p>The axioms already asserted might be boring or confusing if shown to the user. This is
	 * the case even if the verbalization differs from the original snippet (this is covered anyway
	 * by paraphrasing). The axioms which are novel but whose verbalization matches an existing snippet
	 * would be confusing as well or would simply add noise. Entailment of such axioms happens e.g.
	 * in the following case.</p>
	 * 
	 * <ul>
	 * <li>The user asserts: "Everything knows itself."</li>
	 * <li>The corresponding axiom is stored as: "Thing SubClassOf know some Self"
	 * <li>The reasoner entails: "Reflexive(know)"
	 * <li>The verbalizer produces: "Everything knows itself."</li>
	 * </ul>
	 * 
	 * <p>Note that we provide all the entailment types that OWL API supports, apart from the
	 * data property entailments.
	 * (Note that Protege provides only 4 types of entailments in its Inferred Axioms view.)
	 * It is a bit unclear what is the impact on performance.
	 * We tried testing with Pellet and people_pets: fillOntology usually takes 2-3 seconds, but
	 * sometimes even 10 seconds. Initially it seemed that the culprit was the various data property
	 * entailments, but testing some more did not confirm that.</p>
	 * 
	 * TODO: BUG: do we want to consider only the active ontology when reasoning? can we?
	 * 
	 * @throws OWLOntologyCreationException
	 * @throws InferredAxiomGeneratorException
	 * @throws OWLOntologyChangeException
	 * @throws OWLReasonerException
	 * @throws OWLRendererException
	 */
	private void getInferredSentences() throws OWLOntologyCreationException, InferredAxiomGeneratorException, OWLOntologyChangeException, OWLReasonerException, OWLRendererException {

		OWLModelManager mm = getOWLModelManager();
		OWLReasoner reasoner = mm.getReasoner();

		if (reasoner.getLoadedOntologies().isEmpty()) {
			return;
		}

		logger.info("Reasoner ontologies: " + reasoner.getLoadedOntologies());

		// Creates an inferred ontology generator and configures it.
		InferredOntologyGenerator ontGen = new InferredOntologyGenerator(reasoner, new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>());

		// The following 4 generators are used by the Protege Inferred Axioms view.
		ontGen.addGenerator(new InferredClassAssertionAxiomGenerator());
		ontGen.addGenerator(new InferredSubClassAxiomGenerator());
		ontGen.addGenerator(new InferredSubObjectPropertyAxiomGenerator());
		//ontGen.addGenerator(new InferredSubDataPropertyAxiomGenerator());

		// ACE View provides more entailments.
		ontGen.addGenerator(new InferredEquivalentClassAxiomGenerator());
		ontGen.addGenerator(new InferredEquivalentObjectPropertyAxiomGenerator());
		//ontGen.addGenerator(new InferredInverseObjectPropertiesAxiomGenerator());
		ontGen.addGenerator(new InferredObjectPropertyCharacteristicAxiomGenerator());
		ontGen.addGenerator(new InferredPropertyAssertionGenerator());
		//ontGen.addGenerator(new InferredDataPropertyCharacteristicAxiomGenerator());
		//ontGen.addGenerator(new InferredEquivalentDataPropertiesAxiomGenerator());

		// Uses the inferred ontology generator to fill the new ontology.
		logger.info("fillOntology...");
		OWLOntologyManager ontologyManager = ACETextManager.createOWLOntologyManager();
		// Creates a new empty ontology.
		OWLOntology inferredOnt = OntologyUtils.createOntology(ontologyManager);
		ontGen.fillOntology(ontologyManager, inferredOnt);
		logger.info("Done.");

		if (inferredOnt == null || inferredOnt.getAxioms().size() == 0) {
			return;
		}

		ACEViewPreferences prefs = ACEViewPreferences.getInstance();
		ACELexicon<OWLEntity> lexicon = ACETextManager.getActiveACELexicon();
		AxiomVerbalizer axiomVerbalizer = new AxiomVerbalizer(new VerbalizerWebservice(prefs.getOwlToAce()));

		OWLOntology activeOntology = mm.getActiveOntology();

		for (OWLLogicalAxiom ax : inferredOnt.getLogicalAxioms()) {

			if (activeOntology.containsAxiom(ax)) {
				// Not showing the ASSERTED axiom.
			}
			else if (ax instanceof OWLSubClassOfAxiom && ((OWLSubClassOfAxiom) ax).getSuperClass().isOWLThing()) {
				logger.info("NOT showing: " + ax.toString());
			}
			else if (ax instanceof OWLClassAssertionAxiom && ((OWLClassAssertionAxiom) ax).getClassExpression().isOWLThing()) {
				logger.info("NOT showing: " + ax.toString());
			}
			else if (ax instanceof OWLEquivalentObjectPropertiesAxiom && ((OWLNaryPropertyAxiom) ax).getProperties().size() < 2) {
				// BUG: ignoring Protege/OWLAPI bug
				logger.info("NOT showing: " + ax.toString());
			}
			else if (ax instanceof OWLEquivalentDataPropertiesAxiom && ((OWLNaryPropertyAxiom) ax).getProperties().size() < 2) {
				// BUG: ignoring Protege/OWLAPI bug
				logger.info("NOT showing: " + ax.toString());
			}
			else if (! Showing.isShow(ax)) {
				logger.info("NOT showing: " + ax + ", contains tricks");
			}
			else {
				ACESnippet snippet = axiomVerbalizer.verbalizeAxiom(activeOntology, ax);
				if (ACETextManager.getActiveACEText().contains(snippet)) {
					logger.info("NOT showing: " + ax.toString() + ", snippet matches asserted snippet: " + snippet);
				}
				else {
					logger.info("showing: " + ax.toString());
					dtm.addSnippet(snippet);
				}
			}
		}

		logger.info("removing: " + inferredOnt.getOntologyID());

		// TODO: Check if this works like this as before removeOntology
		// took the URI as the argument, rather than the ontology object.
		ontologyManager.removeOntology(inferredOnt);
	}


	@Override
	public void refreshComponent() {
		if (tableSnippets != null) {
			tableSnippets.setFont(owlRendererPreferences.getFont());
		}
	}
}