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

import org.apache.log4j.Logger;
import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXBusyLabel;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLLogicalAxiom;

import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.ace.ACESentence;
import ch.uzh.ifi.attempto.ace.ACESentenceSplitter;
import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACEText;
import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.model.event.ACETextChangeEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACETextManagerListener;
import ch.uzh.ifi.attempto.aceview.model.event.EventType;
import ch.uzh.ifi.attempto.aceview.ui.ACESnippetEditor;
import ch.uzh.ifi.attempto.aceview.ui.util.ComponentFactory;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

/**
 * <p>This view component shows the ACE text in a simple text area
 * which can be edited to update the text. A double newline separates
 * the snippets.</p>
 * 
 * @author Kaarel Kaljurand
 */
public class ACETextViewComponent extends AbstractOWLViewComponent {

	private static final Logger logger = Logger.getLogger(ACETextViewComponent.class);

	private final ACESnippetEditor aceTextArea = new ACESnippetEditor(25, 80);
	private final JLabel labelMessage = new JLabel();
	private final JXBusyLabel labelBusyIndicator = new JXBusyLabel();
	private final JButton buttonUpdate = ComponentFactory.makeButton("Update");

	private final ACETextManagerListener aceTextManagerListener = new ACETextManagerListener() {
		public void handleChange(ACETextChangeEvent event) {
			if (event.isType(EventType.ACTIVE_ACETEXT_CHANGED) ||
					event.isType(EventType.ACETEXT_CREATED)) {
				aceTextArea.setAutocompleter(ACETextManager.getActiveACELexicon().getAutocompleter());
			}
			showText();
		}
	};


	@Override
	protected void disposeOWLView() {
		ACETextManager.removeListener(aceTextManagerListener);
	}


	@Override
	protected void initialiseOWLView() throws Exception {

		buttonUpdate.setToolTipText("Update the knowledge base on the basis of the changes done in the textarea.");
		buttonUpdate.setMnemonic(KeyEvent.VK_ENTER);

		buttonUpdate.addActionListener(new ActionListener() {
			/**
			 * <p>Updates the active ACE text on the basis of the textarea.</p>
			 * 
			 * <ol>
			 * <li>Checks if an existing snippet includes a sentence from the textarea.
			 * These textarea sentences that are not included by any snippet
			 * are collected into <code>addedSentences</code>.</li>
			 * <li>Check if an existing snippet contains a sentence that the textarea does not contain.
			 * Such sentences are collected into <code>removedSentences</code>.</li>
			 * <li><code>addedSentences</code> are added to the current text and
			 * <code>removedSentences</code> are removed.</li>
			 * </ol>
			 * 
			 * BUG: Does the order of add/delete matter (semantically and performancewise)?
			 */
			public void actionPerformed(ActionEvent e) {

				buttonUpdate.setEnabled(false);
				labelBusyIndicator.setBusy(true);
				labelBusyIndicator.setVisible(true);

				ACEText<OWLEntity, OWLLogicalAxiom> acetext = ACETextManager.getActiveACEText();

				logger.info("Updating active knowledge base.");

				final Set<List<ACESentence>> addedSentenceLists = new LinkedHashSet<List<ACESentence>>();
				final Set<List<ACESentence>> removedSentenceLists = new LinkedHashSet<List<ACESentence>>();

				final Set<ACESnippet> removedSnippets = Sets.newHashSet();

				// BUG: TODO: put this pattern into attempto-ape.jar to share with AceWiki
				Pattern snippetSeparator = Pattern.compile("\n\n");
				String[] textareaSentenceLists = snippetSeparator.split(aceTextArea.getText());
				for (String snippetAsString : textareaSentenceLists) {
					List<ACESentence> sentences = ACESentenceSplitter.splitSentences(snippetAsString);					
					if (acetext.contains(sentences)) {
						removedSentenceLists.add(sentences);
					}
					else {
						addedSentenceLists.add(sentences);
					}
				}

				logger.info("Add: " + addedSentenceLists);


				for (ACESnippet s : acetext.getSnippets()) {
					if (! removedSentenceLists.contains(s.getSentences())) {
						removedSnippets.add(s);
					}
				}

				logger.info("Del: " + removedSnippets);

				displayMessage("Adding " + addedSentenceLists.size() + " and deleting " + removedSnippets.size() + " snippet(s)");

				new SwingWorker<Double, Object>() {

					@Override
					public Double doInBackground() {
						Date dateBegin = new Date();
						ACETextManager.addAndRemoveItems(addedSentenceLists, removedSnippets);
						Date dateEnd = new Date();
						return (double) ((dateEnd.getTime() - dateBegin.getTime()) / 1000);
					}

					@Override
					public void done() {
						try {
							Double duration = get();
							displayMessage("Updated in " + duration + " seconds");
							aceTextArea.requestFocusInWindow();
						} catch (InterruptedException e) {
							displayMessage("InterruptedException");
							e.printStackTrace();
						} catch (ExecutionException e) {
							displayMessage("ExecutionException");
							e.printStackTrace();
						} finally {
							buttonUpdate.setEnabled(true);
							labelBusyIndicator.setBusy(false);
							labelBusyIndicator.setVisible(false);
						}
					}
				}.execute();
			}
		});

		aceTextArea.setAutocompleter(ACETextManager.getActiveACELexicon().getAutocompleter());


		JScrollPane scrollpaneAce = new JScrollPane(aceTextArea,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);


		labelBusyIndicator.setVisible(false);

		Box panelButtonAndLabel = new Box(BoxLayout.X_AXIS);
		panelButtonAndLabel.add(buttonUpdate);
		// Note: Glue does not seem to work with labels that contain HTML
		panelButtonAndLabel.add(Box.createHorizontalGlue());
		panelButtonAndLabel.add(labelMessage);
		panelButtonAndLabel.add(Box.createHorizontalGlue());
		panelButtonAndLabel.add(labelBusyIndicator);

		setLayout(new BorderLayout());
		add(scrollpaneAce, BorderLayout.CENTER);
		add(panelButtonAndLabel, BorderLayout.SOUTH);

		ACETextManager.addListener(aceTextManagerListener);
		showText();
	}


	private void showText() {
		ACEText acetext = ACETextManager.getActiveACEText();
		int numberOfSentences = acetext.getSentences().size();
		if (numberOfSentences == 1) {
			getView().setHeaderText("1 sentence");
		}
		else {
			getView().setHeaderText(numberOfSentences + " sentences");
		}
		aceTextArea.setText(acetext.toString());
	}


	private void displayMessage(String message) {
		labelMessage.setText(message);
		labelMessage.validate();
	}
}