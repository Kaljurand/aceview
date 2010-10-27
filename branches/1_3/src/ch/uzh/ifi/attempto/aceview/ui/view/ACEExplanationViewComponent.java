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
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;
import org.jdesktop.swingworker.SwingWorker;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLRuntimeException;

import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.AxiomBlackboxExplainer;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewListener;
import ch.uzh.ifi.attempto.aceview.model.event.SnippetEventType;


/**
 * <p>This view component explains why a given snippet
 * is entailed by the knowledge base.</p>
 *  
 * @author Kaarel Kaljurand
 */
public class ACEExplanationViewComponent extends AbstractOWLViewComponent {

	private static final Logger logger = Logger.getLogger(ACEExplanationViewComponent.class);

	private JTree treeExplanations;
	private final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("");
	private final DefaultTreeModel model = new DefaultTreeModel(rootNode);

	private final TreeSelectionListener treeSelectionListener = new TreeSelectionListener() {
		public void valueChanged(TreeSelectionEvent e) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeExplanations.getLastSelectedPathComponent();
			if (node != null) {
				Object nodeInfo = node.getUserObject();
				if ((node.isLeaf() || node.isRoot()) && nodeInfo instanceof ACESnippet) {
					ACESnippet snippet = (ACESnippet) nodeInfo;
					logger.info("Selected: " + snippet);
					ACETextManager.setSelectedSnippet(ACETextManager.getActiveACEText().find(snippet.getSentences()));
				}
			}
		}
	};

	private final ACEViewListener<ACEViewEvent<SnippetEventType>> snippetListener = new ACEViewListener<ACEViewEvent<SnippetEventType>>() {
		public void handleChange(ACEViewEvent<SnippetEventType> event) {
			if (isSynchronizing() && event.isType(SnippetEventType.WHY_SNIPPET_CHANGED)) {
				ACESnippet whySnippet = ACETextManager.getWhySnippet();
				if (whySnippet != null) {
					getView().setHeaderText(whySnippet.toString());
					calculateAndShowExplanations(whySnippet);
				}
			}
		}
	};


	@Override
	protected void disposeOWLView() {
		ACETextManager.removeSnippetListener(snippetListener);
		treeExplanations.removeTreeSelectionListener(treeSelectionListener);
	}


	@Override
	protected void initialiseOWLView() throws Exception {
		treeExplanations = new JTree(model);
		treeExplanations.setRowHeight(-1);
		treeExplanations.setRootVisible(true);
		treeExplanations.setShowsRootHandles(true);

		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setOpenIcon(null);
		renderer.setClosedIcon(null);
		renderer.setLeafIcon(null);
		treeExplanations.setCellRenderer(renderer);

		setLayout(new BorderLayout());
		add(BorderLayout.CENTER, new JScrollPane(treeExplanations));

		treeExplanations.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		treeExplanations.addTreeSelectionListener(treeSelectionListener);
		ACETextManager.addSnippetListener(snippetListener);
	}


	/**
	 * <p>Calculates the explanations in a separate thread, and shows
	 * the explanations in the EDT.</p>
	 * 
	 * @param snippet ACE snippet to be explained
	 */
	private void calculateAndShowExplanations(final ACESnippet snippet) {

		OWLAxiom axiom = snippet.getAxiom();
		if (axiom == null) {
			model.setRoot(new DefaultMutableTreeNode("<html><i>Cannot explain. Snippet corresponds to no or more than one axioms.</i></html>"));
			return;
		}

		final AxiomBlackboxExplainer axex = new AxiomBlackboxExplainer(getOWLModelManager(), axiom);
		model.setRoot(new DefaultMutableTreeNode("<html><i>Working ...</i></html>"));

		new SwingWorker<Set<Set<ACESnippet>>, Object>() {

			@Override
			public Set<Set<ACESnippet>> doInBackground() {
				Set<Set<ACESnippet>> explanations = null;

				/*
				try {
					explanations = axex.getOrderedExplanations();
				}
				catch (OWLRuntimeException e1) {
					// BUG: crashed while getOrderedExplanations()
				}
				 */

				// BUG: Note that we are catching an unchecked exception here
				try {
					explanations = axex.getExplanations();
				}
				catch (OWLRuntimeException e) {
					return null;
				}
				return explanations;
			}


			@Override
			public void done() {
				try {
					Set<Set<ACESnippet>> explanations = get();

					if (explanations == null) {
						model.setRoot(new DefaultMutableTreeNode("<html><i>BUG: failed to generate explanations</i></html>"));
					}
					else if (explanations.size() == 0) {			
						// BUG: This should never happen I guess.
						model.setRoot(new DefaultMutableTreeNode("<html><i>No explanations</i></html>"));
					}
					else if (explanations.size() == 1) {
						model.setRoot(new DefaultMutableTreeNode(snippet));
						DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();
						populateTree(model, rootNode, explanations.iterator().next());
						treeExplanations.scrollPathToVisible(new TreePath(rootNode.getLastLeaf().getPath()));
					}
					else {
						int i = 0;
						model.setRoot(new DefaultMutableTreeNode(snippet));
						DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();
						for (Set<ACESnippet> explanation : explanations) {
							i++;
							DefaultMutableTreeNode exNode = new DefaultMutableTreeNode("<html><i>Explanation " + i + " (snippet count: " + explanation.size() + ")</i></html>");
							model.insertNodeInto(exNode, rootNode, 0);
							populateTree(model, exNode, explanation);
						}
						treeExplanations.scrollPathToVisible(new TreePath(rootNode.getLastLeaf().getPath()));
					}

				} catch (InterruptedException e) {
					model.setRoot(new DefaultMutableTreeNode("<html><i>BUG: InterruptedException</i></html>"));
					e.printStackTrace();
				} catch (ExecutionException e) {
					model.setRoot(new DefaultMutableTreeNode("<html><i>BUG: ExecutionException</i></html>"));
					e.printStackTrace();
				}
			}
		}.execute();
	}


	private void populateTree(DefaultTreeModel model, DefaultMutableTreeNode node, Collection<ACESnippet> snippets) {
		for (ACESnippet snippet : snippets) {
			model.insertNodeInto(new DefaultMutableTreeNode(snippet), node, 0);
		}
	}
}