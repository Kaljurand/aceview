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

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.jdesktop.swingx.decorator.ColorHighlighter;

import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.model.QuestionsTableModel;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewListener;
import ch.uzh.ifi.attempto.aceview.model.event.SnippetEventType;
import ch.uzh.ifi.attempto.aceview.predicate.AnswerIsCompletePredicate;
import ch.uzh.ifi.attempto.aceview.ui.ACEAnswersPane;
import ch.uzh.ifi.attempto.aceview.ui.Colors;
import ch.uzh.ifi.attempto.aceview.ui.util.TableColumnHelper;

/**
 * <p>This view component contains a table with all the questions
 * in the ACE text, and a pane where the answers to the selected
 * question are shown.</p>
 * 
 * @author Kaarel Kaljurand
 */
public class ACEQandAViewComponent extends AbstractACESnippetsViewComponent {
	private ACEAnswersPane componentAnswers;
	private final QuestionsTableModel tableModel = new QuestionsTableModel();
	private final ColorHighlighter isCompleteHighlighter = new ColorHighlighter(new AnswerIsCompletePredicate(tableModel.getACEText()));


	private final ACEViewListener<ACEViewEvent<SnippetEventType>> snippetListener = new ACEViewListener<ACEViewEvent<SnippetEventType>>() {
		public void handleChange(ACEViewEvent<SnippetEventType> event) {
			if (isSynchronizing() && event.isType(SnippetEventType.SELECTED_SNIPPET_CHANGED)) {
				ACESnippet selectedSnippet = ACETextManager.getSelectedSnippet();
				if (selectedSnippet != null && selectedSnippet.isQuestion()) {
					componentAnswers.showAnswer(tableModel.getACEText(), selectedSnippet);
				}
			}
		}
	};


	@Override
	public void disposeView() {
		removeHierarchyListener(hierarchyListener);
		ACETextManager.removeSnippetListener(snippetListener);
	}

	@Override
	public void initialiseView() throws Exception {

		super.initialiseView();

		isCompleteHighlighter.setBackground(Colors.ANSWERS_COMPLETE_COLOR);

		componentAnswers = new ACEAnswersPane(getOWLWorkspace(), getOWLModelManager().getOWLDataFactory());

		JScrollPane scrollpaneAnswers = new JScrollPane(componentAnswers,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		tableSnippets.setModel(tableModel);

		TableColumnHelper.configureColumns(tableSnippets, QuestionsTableModel.Column.values());

		tableSnippets.setToolTipText("List of questions. Select a question to see its answers.");
		tableSnippets.addHighlighter(isCompleteHighlighter);

		JScrollPane scrollpaneSnippets = new JScrollPane(tableSnippets,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		JSplitPane splitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollpaneSnippets, scrollpaneAnswers);

		setLayout(new BorderLayout());
		add(BorderLayout.NORTH, panelButtons);
		add(BorderLayout.CENTER, splitpane);

		addHierarchyListener(hierarchyListener);
		ACETextManager.addSnippetListener(snippetListener);
		setHeaderText();
	}


	@Override
	public void refreshComponent() {
		if (tableSnippets != null) {
			tableSnippets.setFont(owlRendererPreferences.getFont());
		}
	}
}