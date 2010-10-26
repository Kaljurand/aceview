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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JScrollPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.FilterPipeline;
import org.jdesktop.swingx.decorator.Highlighter;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;

import com.google.common.base.Predicate;

import ch.uzh.ifi.attempto.aceview.model.LexiconTableModel;
import ch.uzh.ifi.attempto.aceview.model.filter.PredicateFilter;
import ch.uzh.ifi.attempto.aceview.predicate.EntryHighlightPredicate;
import ch.uzh.ifi.attempto.aceview.predicate.EntryReferencesEntity;
import ch.uzh.ifi.attempto.aceview.ui.ACETable;
import ch.uzh.ifi.attempto.aceview.ui.Colors;
import ch.uzh.ifi.attempto.aceview.ui.util.TableColumnHelper;

/**
 * <p>This view component shows the ACE lexicon in a tabular form.</p>
 * 
 * @author Kaarel Kaljurand
 */
public class ACELexiconViewComponent extends AbstractACEFilterableViewComponent {

	// This is the model index of the column where the entity resides
	private static final int ENTITY_COLUMN = LexiconTableModel.Column.ENTITY.ordinal();

	private ACETable tableLexicon;
	private final LexiconTableModel lexiconTM = new LexiconTableModel();

	private final TableModelListener tableModelListener = new TableModelListener() {
		public void tableChanged(TableModelEvent event) {
			setHeaderText();
		}
	};

	@Override
	public void disposeView() {
		if (lexiconTM != null) {
			lexiconTM.removeTableModelListener(tableModelListener);
		}
		removeHierarchyListener(hierarchyListener);
		if (lexiconTM != null) {
			lexiconTM.dispose();
		}
	}

	@Override
	public void initialiseView() throws Exception {

		super.initialiseView();
		buttonHighlight.setToolTipText("Highlight the entries that contain the selected entity.");
		buttonFilter.setToolTipText("Show only the entries that contain the selected entity.");

		tableLexicon = new ACETable(lexiconTM);
		tableLexicon.setToolTipText("List of lexicon entries");
		TableColumnHelper.configureColumns(tableLexicon, LexiconTableModel.Column.values());

		JScrollPane scrollpaneLexicon = new JScrollPane(tableLexicon,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		setLayout(new BorderLayout());
		add(BorderLayout.NORTH, panelButtons);
		add(BorderLayout.CENTER, scrollpaneLexicon);


		tableLexicon.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount() > 0) {
					ACETable table = (ACETable) e.getSource();
					int row = table.rowAtPoint(e.getPoint());
					if (row != -1) {
						int rowModel = table.convertRowIndexToModel(row);
						int colModel = LexiconTableModel.Column.ENTITY.ordinal();

						OWLEntity entity = (OWLEntity) table.getModel().getValueAt(rowModel, colModel);
						if (entity != null) {
							getOWLWorkspace().getOWLSelectionModel().setSelectedEntity(entity);
						}
					}
				}
			}
		});

		lexiconTM.addTableModelListener(tableModelListener);
		addHierarchyListener(hierarchyListener);
		refreshComponent();
		setHeaderText();
	}


	private void setHeaderText() {
		int numberOfEntries = lexiconTM.getRowCount();
		String numberOfEntriesShown = "all";
		if (tableLexicon.getFilters() != null) {
			int numberOfEntriesShownInt = tableLexicon.getFilters().getOutputSize();
			if (numberOfEntries != numberOfEntriesShownInt) {
				numberOfEntriesShown = String.valueOf(numberOfEntriesShownInt);
			}
		}
		String form = " entry";
		if (numberOfEntries > 1) {
			form = " entries";
		}
		getView().setHeaderText(numberOfEntries + form + " (" + numberOfEntriesShown + " shown)");
	}


	@Override
	protected OWLObject updateView() {
		OWLEntity entity = getOWLWorkspace().getOWLSelectionModel().getSelectedEntity();
		if (isShowing() && entity != null) {
			Predicate<OWLEntity> entryReferencesEntity = new EntryReferencesEntity(entity);
			if (buttonFilter.isSelected()) {
				tableLexicon.setHighlighters(new Highlighter [] { });
				tableLexicon.setFilters(
						new FilterPipeline(
								new PredicateFilter<OWLEntity>(
										entryReferencesEntity, ENTITY_COLUMN)));
			}
			else if (buttonHighlight.isSelected()) {
				tableLexicon.setFilters(null);
				ColorHighlighter entityHightlighter =
					new ColorHighlighter(new EntryHighlightPredicate(entryReferencesEntity, ENTITY_COLUMN));
				entityHightlighter.setBackground(Colors.HIGHLIGHT_COLOR);
				tableLexicon.setHighlighters(new Highlighter [] { entityHightlighter });
			}
			setHeaderText();
		}
		return entity;
	}


	@Override
	public void refreshComponent() {
		if (tableLexicon != null) {
			tableLexicon.setFont(owlRendererPreferences.getFont());
		}
	}
}