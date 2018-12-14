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
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.protege.editor.core.ui.util.UIUtil;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.aceview.ACEText;
import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.lexicon.TokenMapper;
import ch.uzh.ifi.attempto.aceview.util.AceWikiRenderer;

/**
 * <p>Action that saves the active ACE text into AceWiki format.</p>
 * 
 * @author Kaarel Kaljurand
 */
public class CreateAceWikiAction extends ProtegeOWLAction {

	private static final String ACTION_TITLE = "Export as AceWiki";

	public void initialise() throws Exception {}
	public void dispose() throws Exception {}

	public void actionPerformed(ActionEvent actionEvent) {
		ACEText<OWLEntity, OWLLogicalAxiom> acetext = ACETextManager.getActiveACEText();
		TokenMapper tokenMapper = ACETextManager.getActiveACELexicon();
		File f = saveZipFile(ACTION_TITLE);

		if (f != null) {
			String absolutePath = f.getAbsolutePath();
			String fileName = f.getName();
			String directoryName = "";
			if (absolutePath.endsWith(".zip")) {
				directoryName = fileName.substring(0, fileName.length() - 4);
			}
			else {
				absolutePath = absolutePath + ".zip";
				directoryName = fileName;
			}
			AceWikiRenderer renderer = new AceWikiRenderer(acetext, tokenMapper);
			try {
				renderer.createZipFile(absolutePath, directoryName);
				showMessage(JOptionPane.INFORMATION_MESSAGE, "AceWiki saved into " + absolutePath + ".");
			} catch (IOException e) {
				showMessage(JOptionPane.ERROR_MESSAGE, e.getMessage());
			}
		}
	}


	private void showMessage(int messageType, String str) {
		JOptionPane.showMessageDialog(null, str, ACTION_TITLE, messageType);
	}


	private File saveZipFile(String title) {
		JFrame frame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, getOWLWorkspace().getParent());
		return UIUtil.saveFile(frame, title, Sets.newHashSet("zip"));
	}


	/*
	public void actionPerformed(ActionEvent actionEvent) {

		JFileChooser fc = new JFileChooser();

		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String filename = fc.getSelectedFile().getAbsolutePath();
			if (filename != null && filename.length() > 0) {
				AceWikiRenderer renderer = new AceWikiRenderer(ACETextManager.getActiveACEText());
				try {
					renderer.createZipFile(filename);
					showMessage(JOptionPane.INFORMATION_MESSAGE, "AceWiki saved into " + filename + ".");
				} catch (IOException e) {
					showMessage(JOptionPane.ERROR_MESSAGE, e.getMessage());
				}
			}
		}
	}
	 */
}