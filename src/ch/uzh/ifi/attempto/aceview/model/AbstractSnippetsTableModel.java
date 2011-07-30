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

package ch.uzh.ifi.attempto.aceview.model;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import ch.uzh.ifi.attempto.ace.ACESentence;
import ch.uzh.ifi.attempto.ace.ACESplitter;
import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewListener;
import ch.uzh.ifi.attempto.aceview.model.event.TextEventType;

/**
 * <p>Table of active ACE text snippets (e.g. declarative, or interrogative).
 * The snippet-column of the table is editable.</p>
 * 
 * TODO: would be cool if we could define here also: getColumnCount, getColumnName
 * 
 * @author Kaarel Kaljurand
 */
public abstract class AbstractSnippetsTableModel extends AbstractTableModel {

	protected List<ACESnippet> snippets;
	protected ACEViewListener<ACEViewEvent<TextEventType>> aceTextManagerListener;

	private static final Logger logger = Logger.getLogger(AbstractSnippetsTableModel.class);

	public abstract int getSnippetColumn();

	/**
	 * <p>Returns <code>true</code> if the given cell
	 * is in the snippet-column, otherwise returns <code>false</code>.
	 * In other words, only the snippet-column is editable.</p>
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		return (getSnippetColumn() == column);
	}


	public int getRowCount() {
		return snippets.size();
	}


	/**
	 * <p>Updates a table cell that contains the snippet. The old snippet
	 * is removed from the text and the new snippet is added. Three things
	 * can happen.</p>
	 * 
	 * <ul>
	 * <li>The old snippet and the new snippet are equal. Then does nothing.</li>
	 * <li>The new snippet is empty (i.e. the user just deleted the cell content).
	 * Then deletes the old snippet from the text.</li>
	 * <li>Otherwise deletes the old snippet and adds the new snippet.</li>
	 * </ul>
	 * 
	 * <p>Note that the order of remove+add is important. Consider the
	 * case when the user deletes "Every fireman is a man." and types in
	 * "If there is a fireman then the fireman is a man." (i.e. replaces a sentence
	 * with its paraphrase). In this case, we do not want to add a redundant axiom
	 * <code>SubClassOf(fireman man)</code> and then immediately delete it. Rather,
	 * we want to delete the axiom, and then add it immediately back. This is what the
	 * user intended.</p>
	 */
	@Override
	public void setValueAt(Object text, int row, int column) {
		if (column == 0 && text instanceof String) {
			Object object = getValueAt(row, column);
			if (object != null && object instanceof ACESnippet) {
				ACESnippet oldSnippet = (ACESnippet) object;
				ACESplitter splitter = new ACESplitter(ACETextManager.getActiveACELexicon());
				List<ACESentence> newSentences = splitter.getSentences((String) text);

				if (oldSnippet.getSentences().equals(newSentences)) {
					logger.info("Del/Add nothing: oldSnippet = newSentences = " + oldSnippet.getSentences());
				}
				else if (newSentences.isEmpty()) {
					logger.info("Add nothing: newSentences is empty");
					logger.info("Del old snippet: " + oldSnippet);
					ACETextManager.removeSnippet(oldSnippet);
					ACETextManager.resetSelectedSnippet();
					fireTableCellUpdated(row, column);
				}
				else {
					ACETextManager.updateSnippet(row, oldSnippet, newSentences);
					fireTableCellUpdated(row, column);
				}
			}
		}
	}	


	public void dispose() {
		ACETextManager.removeListener(aceTextManagerListener);
	}
}