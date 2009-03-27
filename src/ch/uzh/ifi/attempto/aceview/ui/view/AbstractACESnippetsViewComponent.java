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

import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.FilterPipeline;
import org.jdesktop.swingx.decorator.PatternFilter;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLObject;

import ch.uzh.ifi.attempto.aceview.predicate.SnippetContainsEntityPredicate;
import ch.uzh.ifi.attempto.aceview.predicate.SnippetIsWeirdPredicate;
import ch.uzh.ifi.attempto.aceview.ui.ACESnippetTable;
import ch.uzh.ifi.attempto.aceview.ui.Colors;


/**
 * <p>This view component has a snippets table as its main component.</p>
 * 
 * @author Kaarel Kaljurand
 */
public abstract class AbstractACESnippetsViewComponent extends AbstractACEFilterableViewComponent {

	// This is the model index of the column where the snippet resides (in all snippet views)
	private static final int SNIPPET_COLUMN = 0;

	protected ACESnippetTable tableSnippets;
	private final ColorHighlighter isWeirdHighlighter = new ColorHighlighter(new SnippetIsWeirdPredicate(SNIPPET_COLUMN));

	private ColorHighlighter entityHightlighter;

	@Override
	public void initialiseView() throws Exception {
		super.initialiseView();
		buttonHighlight.setToolTipText("Highlight the snippets that contain the selected word.");
		buttonFilter.setToolTipText("Show only the snippets that contain the selected word.");
		isWeirdHighlighter.setForeground(Colors.WEIRD_COLOR);
		tableSnippets = new ACESnippetTable();
		tableSnippets.addHighlighter(isWeirdHighlighter);
		refreshComponent();
	}


	@Override
	protected OWLObject updateView() {
		OWLEntity entity = getOWLWorkspace().getOWLSelectionModel().getSelectedEntity();

		if (isShowing() && entity != null) {
			if (buttonFilter.isSelected()) {
				String entityRendering = getOWLModelManager().getRendering(entity);
				if (entityHightlighter != null) {
					tableSnippets.removeHighlighter(entityHightlighter);
				}
				tableSnippets.setFilters(new FilterPipeline(new PatternFilter("\\b" + entityRendering, 0, SNIPPET_COLUMN)));
			}
			else if (buttonHighlight.isSelected()) {
				tableSnippets.setFilters(null);
				if (entityHightlighter != null) {
					tableSnippets.removeHighlighter(entityHightlighter);
				}
				entityHightlighter = new ColorHighlighter(new SnippetContainsEntityPredicate(entity, SNIPPET_COLUMN));
				entityHightlighter.setBackground(Colors.HIGHLIGHT_COLOR);
				tableSnippets.addHighlighter(entityHightlighter);
			}
			setHeaderText();
		}
		return entity;
	}


	protected void setHeaderText() {
		int numberOfSnippets = tableSnippets.getModel().getRowCount();
		if (numberOfSnippets == 0) {
			getView().setHeaderText("There are no snippets.");
		}
		else {
			String numberOfSnippetsShown = "all";
			if (tableSnippets.getFilters() != null) {
				int numberOfSnippetsShownInt = tableSnippets.getFilters().getOutputSize();
				if (numberOfSnippets != numberOfSnippetsShownInt) {
					numberOfSnippetsShown = String.valueOf(numberOfSnippetsShownInt);
				}
			}
			String pl1 = "";
			if (numberOfSnippets > 1) {
				pl1 = "s";
			}
			getView().setHeaderText(numberOfSnippets + " snippet" + pl1 + " (" + numberOfSnippetsShown + " shown)");
		}
	}
}