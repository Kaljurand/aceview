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

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import org.protege.editor.owl.ui.renderer.OWLRendererPreferences;
import org.protege.editor.owl.ui.view.AbstractOWLSelectionViewComponent;

/**
 * <p>This view component listens to all the OWL entity selection events.</p>
 * 
 * @author Kaarel Kaljurand
 */
public abstract class AbstractACEViewComponent extends AbstractOWLSelectionViewComponent {

	protected final OWLRendererPreferences owlRendererPreferences = OWLRendererPreferences.getInstance();

	protected final HierarchyListener hierarchyListener = new HierarchyListener() {
		public void hierarchyChanged(HierarchyEvent hierarchyEvent) {
			updateView();
		}
	};

	@Override
	protected boolean isOWLClassView() {
		return true;
	}

	@Override
	protected boolean isOWLObjectPropertyView() {
		return true;
	}

	@Override
	protected boolean isOWLDataPropertyView() {
		return true;
	}

	@Override
	protected boolean isOWLIndividualView() {
		return true;
	}
}