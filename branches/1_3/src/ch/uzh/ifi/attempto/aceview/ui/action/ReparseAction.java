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

package ch.uzh.ifi.attempto.aceview.ui.action;

import java.awt.event.ActionEvent;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLLogicalAxiom;

import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACEText;
import ch.uzh.ifi.attempto.aceview.ACETextManager;

/**
 * <p>Reparses all the active ACE text snippets that do not have any
 * corresponding logical axioms.</p>
 * 
 * @author Kaarel Kaljurand
 */
public class ReparseAction extends ProtegeOWLAction {

	private static final Logger logger = Logger.getLogger(ReparseAction.class);

	private static final String ACTION_TITLE = "Reparse Action";


	public void dispose() throws Exception {
	}

	public void initialise() throws Exception {
	}

	public void actionPerformed(ActionEvent actionEvent) {
		ACEText<OWLEntity, OWLLogicalAxiom> acetext = ACETextManager.getActiveACEText();
		if (acetext == null) return;

		Map<ACESnippet, Integer> axiomlessSnippets = acetext.getAxiomlessSnippets();
		if (axiomlessSnippets.isEmpty()) {
			showMessage(JOptionPane.INFORMATION_MESSAGE, "There are no axiomless snippets. Nothing to reparse.");
		}
		else {
			for (Map.Entry<ACESnippet, Integer> entry : axiomlessSnippets.entrySet()) {
				ACESnippet oldSnippet = entry.getKey();
				logger.info("Reparsing: " + oldSnippet);
				ACETextManager.updateSnippet(entry.getValue(), oldSnippet, oldSnippet.getSentences());
			}
			int counterFailedBefore = axiomlessSnippets.size();
			int counterFailedAgain = acetext.getAxiomlessSnippets().size();
			logger.info("FailedBefore snippet count: " + counterFailedBefore);
			logger.info("FailedAgain snippet count: " + counterFailedAgain);
			showMessage(JOptionPane.INFORMATION_MESSAGE, "Snippets reparsed: " + counterFailedBefore + "\nSnippets that failed again: " + counterFailedAgain);
		}}


	private void showMessage(int messageType, String str) {
		JOptionPane.showMessageDialog(null, str, ACTION_TITLE, messageType);
	}
}