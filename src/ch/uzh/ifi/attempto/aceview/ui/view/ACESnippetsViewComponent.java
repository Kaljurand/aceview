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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import ch.uzh.ifi.attempto.aceview.model.SnippetsTableModel;
import ch.uzh.ifi.attempto.aceview.ui.util.TableColumnHelper;

/**
 * <p>This view component shows a table with all the snippets
 * in the ACE text.</p>
 * 
 * @author Kaarel Kaljurand
 */
public class ACESnippetsViewComponent extends AbstractACESnippetsViewComponent {

	private final SnippetsTableModel stm = new SnippetsTableModel();

	private final TableModelListener tableModelListener = new TableModelListener() {
		public void tableChanged(TableModelEvent event) {
			setHeaderText();
		}
	};

	@Override
	public void disposeView() {
		stm.removeTableModelListener(tableModelListener);
		removeHierarchyListener(hierarchyListener);
		stm.dispose();
	}

	@Override
	public void initialiseView() throws Exception {
		super.initialiseView();

		tableSnippets.setModel(stm);
		tableSnippets.setToolTipText("List of asserted snippets.");
		//tableSnippets.getColumnExt(SnippetsTableModel.Column.CONTENT_WORDS.getName()).setMaxWidth(35);
		//tableSnippets.getColumnExt(SnippetsTableModel.Column.MESSAGES.getName()).setMaxWidth(35);
		//tableSnippets.getColumnExt(SnippetsTableModel.Column.TIMESTAMP.getName()).setPrototypeValue(new Date());

		TableColumnHelper.configureColumns(tableSnippets, SnippetsTableModel.Column.values());

		//ACECellRenderer cellRenderer = new ACECellRenderer(getOWLEditorKit());
		//cellRenderer.setHighlightKeywords(true);
		//cellRenderer.setWrap(false);
		//tableSnippets.getColumnExt(SnippetsTableModel.Column.SNIPPET.getName()).setCellRenderer(cellRenderer);

		JScrollPane scrollpaneSnippets = new JScrollPane(tableSnippets,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		setLayout(new BorderLayout());
		add(BorderLayout.NORTH, panelButtons);
		add(BorderLayout.CENTER, scrollpaneSnippets);

		stm.addTableModelListener(tableModelListener);
		addHierarchyListener(hierarchyListener);
		setHeaderText();
	}


	@Override
	public void refreshComponent() {
		if (tableSnippets != null) {
			tableSnippets.setFont(owlRendererPreferences.getFont());
		}
	}
}