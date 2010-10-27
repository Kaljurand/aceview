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

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import org.apache.log4j.Logger;
import org.jdesktop.swingworker.SwingWorker;
import org.protege.editor.owl.model.OWLModelManager;

import ch.uzh.ifi.attempto.aceview.model.event.TextEventType;


public class UpdateAnswersUI {

	private static final Logger logger = Logger.getLogger(UpdateAnswersUI.class);

	private static final String ACTION_TITLE = "Update Answers";
	private static final String NO_QUESTIONS_MESSAGE = "There are no questions. Nothing to update.";
	private static final String NSYNC_MESSAGE = "The reasoner is not synchronized. This may produce misleading results.";

	private final Component parent;
	private final OWLModelManager mngr;
	private final ACEText acetext;
	private final List<ACESnippet> questions;

	private ProgressMonitor progressMonitor;
	private Task task;

	public UpdateAnswersUI(Component parent, ACEText<?, ?> acetext, OWLModelManager mngr) {
		this.parent = parent;
		this.mngr = mngr;
		this.acetext = acetext;
		this.questions = acetext.getQuestions();
	}


	public void updateAnswers() {

		final int questionCount = questions.size();

		if (questionCount == 0) {
			showMessage(JOptionPane.INFORMATION_MESSAGE, NO_QUESTIONS_MESSAGE);
			return;
		}


		/*
		TODO: BUG: This doesn't work anymore, maybe not needed
		try {
			if (! mngr.getReasoner().isClassified()) {
				showMessage(JOptionPane.WARNING_MESSAGE, NSYNC_MESSAGE);
			}
		} catch (OWLReasonerException e) {
			showMessage(JOptionPane.ERROR_MESSAGE, e.getMessage());
			return;
		}
		 */

		progressMonitor = new ProgressMonitor(parent, "Updating answers to " + questionCount + " questions...", "", 0, questionCount);
		progressMonitor.setProgress(0);

		task = new Task();
		task.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if ("progress" == evt.getPropertyName()) {
					int progress = (Integer) evt.getNewValue();
					progressMonitor.setProgress(progress);
					String message = String.format("Completed %d of %d.\n", progress, questionCount);
					progressMonitor.setNote(message);
					if (progressMonitor.isCanceled()) {
						task.cancel(true);
						logger.info("Task cancelled");
					}
				}
			}
		});
		task.execute();
	}


	private static void showMessage(int messageType, String str) {
		JOptionPane.showMessageDialog(null, str, ACTION_TITLE, messageType);
	}


	private class Task extends SwingWorker<Void, Void> {

		@Override
		public Void doInBackground() {
			int progress = 0;
			setProgress(0);

			for (ACESnippet question : questions) {
				if (isCancelled()) {
					break;
				}
				acetext.setAnswer(question, new ACEAnswer(mngr, question));
				progress++;
				setProgress(progress);
			}
			return null;
		}

		@Override
		public void done() {
			// TODO: BUG: nothing is monitoring this event
			//ACETextManager.fireEvent(EventType.ACETEXT_ANSWERS_CHANGED);
			progressMonitor.setProgress(0);
		}
	}
}