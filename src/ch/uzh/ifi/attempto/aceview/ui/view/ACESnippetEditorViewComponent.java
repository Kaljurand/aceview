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

package ch.uzh.ifi.attempto.aceview.ui.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.VerticalLayout;
import org.protege.editor.core.ui.util.Icons;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.util.OWLAxiomInstance;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.editor.owl.ui.renderer.OWLRendererPreferences;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;

import com.google.common.collect.Multimap;

import ch.uzh.ifi.attempto.ace.ACESentence;
import ch.uzh.ifi.attempto.ace.ACESplitter;
import ch.uzh.ifi.attempto.ace.ACESentenceRenderer;
import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACESnippetImpl;
import ch.uzh.ifi.attempto.aceview.ACEText;
import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewListener;
import ch.uzh.ifi.attempto.aceview.model.event.TextEventType;
import ch.uzh.ifi.attempto.aceview.ui.ACESnippetEditor;
import ch.uzh.ifi.attempto.aceview.ui.AxiomAnnotationPanel;
import ch.uzh.ifi.attempto.aceview.ui.Colors;
import ch.uzh.ifi.attempto.aceview.ui.util.ComponentFactory;


/**
 * <p>This view component allows one to view and edit the selected snippet.</p>
 * 
 * <p>The editor auto-completes ACE words (if the TAB-key is pressed). The auto-completion
 * is done on the basis of the wordforms in the active ACE text's lexicon.</p>
 *
 * <p>Three buttons enable the active ACE text to be modified:</p>
 * 
 * <ul>
 * <li><code>Add as new</code>: adds the selected snippet to the text (if it is not there already)</li>
 * <li><code>Update</code>: updates the selected snippet (if it is in the text)</li>
 * <li><code>Delete</code>: deletes the selected snippet (if it is in the text)</li>
 * </ul>
 * 
 * <p>In addition, there are two buttons:</p>
 * 
 * <ul>
 * <li>The <code>Annotate</code>-button allows one to annotate the
 * snippet, i.e. add an axiom annotation axiom (that annotates the snippet's axiom)
 * into the underlying ontology of the active ACE text.</li>
 * <li>The <code>Why?</code>-button triggers the "Why? event", asking the reasoner to
 * explain the selected snippet on the basis of the active ACE text.</li>
 * </ul>
 * 
 * @author Kaarel Kaljurand
 */
public class ACESnippetEditorViewComponent extends AbstractACESnippetSelectionViewComponent {

	private static final Logger logger = Logger.getLogger(ACESnippetEditorViewComponent.class);

	private final OWLRendererPreferences owlRendererPreferences = OWLRendererPreferences.getInstance();

	private final int PADDING = 2;

	private JButton buttonNew;
	private JButton buttonUpdate;
	private JButton buttonDelete;
	private JButton buttonWhy;
	private JButton buttonAnnotate;

	private ACESnippetEditor snippetEditor;

	private JLabel labelMessage;

	private final Icon iconError = Icons.getIcon("error.png");
	private final Icon iconWarning = Icons.getIcon("warning.png");

	private AxiomAnnotationPanel axiomAnnotationPanel;


	private final ACEViewListener<ACEViewEvent<TextEventType>> aceTextManagerListener = new ACEViewListener<ACEViewEvent<TextEventType>>() {
		public void handleChange(ACEViewEvent<TextEventType> event) {
			// We update the auto-completer if the lexicon changed,
			// or the active ACE text changed.
			// TODO: maybe we should make sure that the lexicon change is
			// the change of the active ACE text's lexicon.
			if (event.isType(TextEventType.ACTIVE_ACETEXT_CHANGED) ||
					event.isType(TextEventType.ACELEXICON_CHANGED)) {
				snippetEditor.setAutocompleter(ACETextManager.getActiveACELexicon().getAutocompleter());
			}
		}
	};


	@Override
	protected void initialiseOWLView() throws Exception {
		snippetEditor = new ACESnippetEditor(4, 50);
		snippetEditor.setToolTipText("Selected ACE snippet.");
		snippetEditor.setAutocompleter(ACETextManager.getActiveACELexicon().getAutocompleter());

		JScrollPane scrollpaneAce = new JScrollPane(snippetEditor,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		labelMessage = new JLabel("");
		labelMessage.setFont(labelMessage.getFont().deriveFont(10.0f));
		labelMessage.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
		labelMessage.setPreferredSize(new Dimension(labelMessage.getPreferredSize().width, 40));

		buttonNew = ComponentFactory.makeButton("Add as new");
		buttonNew.setMnemonic(KeyEvent.VK_ENTER);
		buttonNew.setToolTipText("Create a new snippet on the basis of the text in the editor window.");

		buttonNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				List<ACESentence> sentences = ACESplitter.getSentences(snippetEditor.getText());
				if (sentences.isEmpty()) {
					displayWarningMessage("Not added. There are no sentences.");
				}
				else {
					ACEText<OWLEntity, OWLLogicalAxiom> acetext = ACETextManager.getActiveACEText();
					ACESnippet oldSnippet = acetext.find(sentences);
					if (oldSnippet == null) {
						OWLOntologyID name = ACETextManager.getActiveACETextID();
						ACESnippetImpl newSnippet = new ACESnippetImpl(name, sentences);
						ACETextManager.addSnippet(newSnippet);
						ACETextManager.setSelectedSnippet(newSnippet);
					}
					else {
						displayWarningMessage("Not added. These sentences are already in the text.");
					}
				}
				snippetEditor.requestFocusInWindow(); // Get focus back
			}
		});


		buttonUpdate = ComponentFactory.makeButton("Update");
		buttonUpdate.setToolTipText("Update the selected snippet on the basis of the text in the editor window.");

		buttonUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				ACESnippet selectedSnippet = ACETextManager.getSelectedSnippet();
				if (selectedSnippet != null) {
					List<ACESentence> sentences = ACESplitter.getSentences(snippetEditor.getText());
					if (sentences.isEmpty()) {
						deleteSnippet(selectedSnippet);
					}
					else {
						ACEText<OWLEntity, OWLLogicalAxiom> acetext = ACETextManager.getActiveACEText();
						ACESnippet oldSnippet = acetext.find(sentences);
						if (oldSnippet == null) {
							ACETextManager.updateSnippet(acetext.indexOf(selectedSnippet), selectedSnippet, sentences);
						}
						else {
							displayWarningMessage("Selected snippet <b>not</b> updated. These sentences are already in the text.");
						}
					}
					snippetEditor.requestFocusInWindow(); // Get focus back
				}
			}
		});


		buttonDelete = ComponentFactory.makeButton("Delete");
		buttonDelete.setToolTipText("Delete the selected snippet from the ACE text.");
		buttonDelete.setMnemonic(KeyEvent.VK_BACK_SPACE);

		buttonDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				ACESnippet selectedSnippet = ACETextManager.getSelectedSnippet();
				if (selectedSnippet != null) {
					deleteSnippet(selectedSnippet);
					snippetEditor.requestFocusInWindow(); // Get focus back
				}
			}
		});


		buttonWhy = ComponentFactory.makeButton("Why?");
		buttonWhy.setToolTipText("Why is the selected snippet entailed?");

		buttonWhy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				ACESnippet selectedSnippet = ACETextManager.getSelectedSnippet();
				if (selectedSnippet != null) {
					ACETextManager.setWhySnippet(selectedSnippet);
				}
			}
		});


		buttonAnnotate = ComponentFactory.makeButton("Annotate");
		buttonAnnotate.setToolTipText("Annotate the selected snippet.");

		buttonAnnotate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				ACESnippet selectedSnippet = ACETextManager.getSelectedSnippet();
				if (selectedSnippet != null) {
					OWLLogicalAxiom ax = selectedSnippet.getAxiom();
					if (ax != null) {
						invokeAxiomAnnotationHandler(ax);
					}
				}
			}
		});


		JXPanel boxButtons = new JXPanel();
		boxButtons.setLayout(new VerticalLayout());
		boxButtons.add(buttonNew);
		boxButtons.add(buttonUpdate);
		boxButtons.add(buttonDelete);
		boxButtons.add(buttonAnnotate);
		boxButtons.add(buttonWhy);


		// Editor panel
		JPanel panelEditor = new JPanel(new BorderLayout());
		panelEditor.add(scrollpaneAce, BorderLayout.CENTER);
		panelEditor.add(labelMessage, BorderLayout.SOUTH);

		// Editor and buttons box
		Box boxEditor = new Box(BoxLayout.X_AXIS);
		boxEditor.setBorder(ComponentFactory.makeBorder());
		boxEditor.add(panelEditor);
		boxEditor.add(boxButtons);

		setLayout(new BorderLayout());
		add(boxEditor);
		refreshComponent();
		resetUI();
		ACETextManager.addListener(aceTextManagerListener);
		ACETextManager.addSnippetListener(getACESnippetListener());
		getOWLWorkspace().getOWLSelectionModel().addListener(getOWLSelectionModelListener());
	}


	private void refreshComponent() {
		if (snippetEditor != null) {
			snippetEditor.setFont(owlRendererPreferences.getFont());
		}
	}


	@Override
	protected void disposeOWLView() {
		if (axiomAnnotationPanel != null) {
			axiomAnnotationPanel.dispose();
		}
		getOWLWorkspace().getOWLSelectionModel().removeListener(getOWLSelectionModelListener());
		ACETextManager.removeListener(aceTextManagerListener);
		ACETextManager.removeSnippetListener(getACESnippetListener());
	}


	/**
	 * Resets the snippet editor UI.
	 */
	private void resetUI() {
		// Header
		getView().setHeaderText("(No snippet selected.)");

		// Snippet editor text-area
		snippetEditor.setText("");
		snippetEditor.getHighlighter().removeAllHighlights();
		clearMessage();

		// Snippet editor buttons
		buttonNew.setEnabled(true);
		buttonUpdate.setEnabled(false);
		buttonDelete.setEnabled(false);
		buttonWhy.setEnabled(false);
		buttonAnnotate.setEnabled(false);
	}


	@Override
	protected void displaySnippet(ACESnippet snippet) {
		if (! isSynchronizing()) return;

		if (snippet == null) {
			resetUI();
			return;
		}

		getView().setHeaderText(snippet.toString());

		ACESentenceRenderer snippetRenderer = new ACESentenceRenderer(snippet.getSentences(), snippet.getErrorSpans());
		snippetEditor.setText(snippetRenderer.getRendering());
		snippetEditor.getHighlighter().removeAllHighlights();
		// Snippet editor buttons
		buttonNew.setEnabled(true);

		if (ACETextManager.getActiveACEText().contains(snippet)) {
			buttonUpdate.setEnabled(true);
			buttonDelete.setEnabled(true);
			if (snippet.getAxiom() == null) {
				buttonAnnotate.setEnabled(false);
			}
			else {
				buttonAnnotate.setEnabled(true);
			}
		}
		else {
			buttonUpdate.setEnabled(false);
			buttonDelete.setEnabled(false);
			buttonAnnotate.setEnabled(false);
		}

		if (snippet.isQuestion() || ! snippet.hasAxioms()) {
			buttonWhy.setEnabled(false);
		}
		else {
			buttonWhy.setEnabled(true);
		}

		if (snippet.hasACEErrors()) {
			displayErrorMessage("The snippet contains ACE syntax errors.");
			Multimap<Integer, Integer> hlCoords = snippetRenderer.getSpans();

			for (Entry<Integer, Integer> entry : hlCoords.entries()) {
				try {
					logger.info("Added highlighter: coords: " + entry.getKey() + " " + entry.getValue());
					snippetEditor.getHighlighter().addHighlight(entry.getKey(), entry.getValue(),
							new DefaultHighlighter.DefaultHighlightPainter(Colors.ERROR_COLOR));
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
		else if (! snippet.hasAxioms()) {
			displayErrorMessage("The snippet contains no ACE syntax errors, but cannot be expressed in OWL/SWRL.");
		}
		else {
			clearMessage();
		}
	}


	private void displayErrorMessage(String message) {
		displayMessage(iconError, "<html>" + message + "</html>");
	}


	private void displayWarningMessage(String message) {
		displayMessage(iconWarning, "<html>" + message + "</html>");
	}


	private void clearMessage() {
		displayMessage(null, "");
	}


	private void displayMessage(Icon icon, String text) {
		labelMessage.setIcon(icon);
		labelMessage.setText(text);
		labelMessage.validate();
	}


	private void invokeAxiomAnnotationHandler(OWLAxiom ax) {
		OWLEditorKit editorKit = getOWLEditorKit();
		if (axiomAnnotationPanel == null) {
			axiomAnnotationPanel = new AxiomAnnotationPanel(editorKit);
		}
		// TODO: BUG: think about it, we should get the ontology that contains the axiom,
		// not the active ontology
		axiomAnnotationPanel.setAxiomInstance(new OWLAxiomInstance(ax, getOWLModelManager().getActiveOntology()));
		new UIHelper(editorKit).showDialog("Annotations", axiomAnnotationPanel, JOptionPane.CLOSED_OPTION);
	}


	/**
	 * <p>Convenience method the delete a snippet and
	 * update the GUI.</p>
	 * 
	 * @param snippet ACE snippet
	 */
	private void deleteSnippet(ACESnippet snippet) {
		ACETextManager.removeSnippet(snippet);
		ACETextManager.resetSelectedSnippet();
		displayWarningMessage("Selected snippet deleted.");
	}
}