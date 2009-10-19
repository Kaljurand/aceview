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

import org.jdesktop.swingx.JXTable;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

import ch.uzh.ifi.attempto.aceview.model.MetricsTableModel;
import ch.uzh.ifi.attempto.aceview.ui.Colors;
import ch.uzh.ifi.attempto.aceview.ui.util.TableColumnHelper;

/**
 * <p>This view component shows some metrics of the ACE text.</p>
 * 
 * @author Kaarel Kaljurand
 */
public class ACEMetricsViewComponent extends AbstractOWLViewComponent {

	private final JXTable tableMetrics = new JXTable(new MetricsTableModel());

	@Override
	protected void disposeOWLView() {
		((MetricsTableModel) tableMetrics.getModel()).dispose();
	}

	@Override
	protected void initialiseOWLView() throws Exception {
		tableMetrics.setShowGrid(true);
		tableMetrics.setGridColor(Colors.GRID_COLOR);
		tableMetrics.setRowHeight(tableMetrics.getRowHeight() + 1);
		tableMetrics.setSortable(false);

		// Gets rid of the UI for sorting/reordering
		tableMetrics.setTableHeader(null);

		TableColumnHelper.configureColumns(tableMetrics, MetricsTableModel.Column.values());

		setLayout(new BorderLayout());
		add(new JScrollPane(tableMetrics));
	}
}