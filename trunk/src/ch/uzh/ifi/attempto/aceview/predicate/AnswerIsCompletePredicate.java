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

package ch.uzh.ifi.attempto.aceview.predicate;

import java.awt.Component;

import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

import ch.uzh.ifi.attempto.aceview.ACEAnswer;
import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACEText;
import ch.uzh.ifi.attempto.aceview.model.QuestionsTableModel;

public class AnswerIsCompletePredicate implements HighlightPredicate {

	private final ACEText acetext;

	public AnswerIsCompletePredicate(ACEText acetext) {
		this.acetext = acetext;
	}

	public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
		// row = view, column = model
		ACESnippet snippet = (ACESnippet) adapter.getFilteredValueAt(adapter.row, QuestionsTableModel.Column.SNIPPET.ordinal());

		if (snippet == null) {
			return false;
		}

		int modelColumn = adapter.viewToModel(adapter.column);

		if (modelColumn == QuestionsTableModel.Column.INDIVIDUALS.ordinal()) {
			ACEAnswer answer = acetext.getAnswer(snippet);
			if (answer == null || answer.getIndividualsCount() == -1) {
				return false;
			}
			return answer.isIndividualAnswersComplete();
		}
		else if (modelColumn == QuestionsTableModel.Column.SUBCLASSES.ordinal()) {
			ACEAnswer answer = acetext.getAnswer(snippet);
			if (answer == null || answer.getSubClassesCount() == -1) {
				return false;
			}
			return answer.isSubClassesAnswersComplete();
		}
		return false;
	}
}