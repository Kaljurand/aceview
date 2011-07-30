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

package ch.uzh.ifi.attempto.aceview.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXHyperlink;
import org.protege.editor.owl.model.OWLWorkspace;
import org.protege.editor.owl.ui.UIHelper;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.Node;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.ace.ACESentenceRenderer;
import ch.uzh.ifi.attempto.aceview.ACEAnswer;
import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACEText;
import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.IriRenderer;
import ch.uzh.ifi.attempto.aceview.ui.util.ComponentFactory;

public class ACEAnswersPane extends JTextPane {

	private static final Logger logger = Logger.getLogger(ACEAnswersPane.class);

	private static final String LABEL_DECLARE_COMPLETE = "Declare complete!";

	private final OWLWorkspace ws;
	private final OWLDataFactory df;
	private StyledDocument doc;

	// TODO: clear the map if a lexical mapping changes (i.e. then verbalization should be recalculated)
	private final Map<OWLLogicalAxiom, ACESnippet> verbalizationCache = Maps.newHashMap();

	public ACEAnswersPane(OWLWorkspace ws, OWLDataFactory df) {
		setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		setBackground(Colors.BG_COLOR);
		setEditable(false);

		this.ws = ws;
		this.df = df;
	}

	/**
	 * <p>Shows the answer to the given snippet (i.e. a question) in the
	 * given ACE text.</p>
	 * 
	 * <p>Every answer is an OWL entity. The entities are grouped into
	 * three groups: individuals, sub classes, super classes. Clicking
	 * on the entity selects the ACE snippet that represents the
	 * full answer, and selects the clicked entity.</p>
	 * 
	 * @param acetext ACE text
	 * @param question ACE question
	 */
	public void showAnswer(ACEText acetext, ACESnippet question) {
		setDocument(new DefaultStyledDocument());
		doc = new DefaultStyledDocument();

		if (question.hasAxioms()) {
			ACEAnswer answer = acetext.getAnswer(question);
			if (answer == null) {
			}
			else {
				final OWLClassExpression dlquery = question.getDLQuery();
				if (answer.isSatisfiable()) {
					logger.info("Showing answers");
					showAnswers(dlquery, answer);
				}
				else {
					showNothingSnippet(dlquery);
				}
			}
		}
		else {
			addComponent(ComponentFactory.makeItalicLabel("This question could not be mapped to OWL (i.e. DL Query)."));
		}
		setDocument(doc);
	}


	private void showNothingSnippet(OWLClassExpression dlquery) {
		addComponent(ComponentFactory.makeItalicLabel("This question is unsatisfiable because"));
		OWLLogicalAxiom axiom = df.getOWLSubClassOfAxiom(dlquery, df.getOWLNothing());
		try {
			ACESnippet nothingSnippet = verbalizationCache.get(axiom);
			if (nothingSnippet == null) {
				nothingSnippet = ACETextManager.makeSnippetFromAxiom(axiom);
				verbalizationCache.put(axiom, nothingSnippet);
			}

			final ACESnippet finalNothingSnippet = nothingSnippet;

			JButton buttonWhy = ComponentFactory.makeButton("Why?");
			buttonWhy.setToolTipText("Why is this snippet entailed?");
			buttonWhy.setBackground(Colors.BG_COLOR);

			buttonWhy.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					ACETextManager.setWhySnippet(finalNothingSnippet);
				}
			});
			addLinebreak();
			// TODO: BUG: The next line creates some HTML with words linked to entities,
			// but these links will not be clickable because there is no listener.
			addComponent(new JLabel("<html>" + nothingSnippet.toHtmlString(ACETextManager.getActiveACELexicon()) + "</html>"));
			addLinebreak();
			addComponent(buttonWhy);
		} catch (OWLRendererException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		}
	}


	private void showAnswers(final OWLClassExpression dlquery, final ACEAnswer answer) {

		final Set<Node<OWLNamedIndividual>> individuals = answer.getIndividuals();
		final Set<Node<OWLClass>> subclasses = answer.getSubClasses();
		final Set<Node<OWLClass>> superclasses = answer.getSuperClasses();

		int ic = individuals.size();
		int dc = subclasses.size();
		int ac = superclasses.size();

		if (ic == 0 && dc == 0 && ac == 0) {
			addComponent(ComponentFactory.makeItalicLabel("This question has no known answers."));
		}
		else {
			addComponent(ComponentFactory.makeItalicLabel(ic + " named individuals (" + answer.getIndividualNodeSetPolicy() + "): "));
			final Set<OWLNamedIndividual> completeIndividuals = renderIndividuals(individuals, dlquery);

			if (answer.isIndividualAnswersComplete()) {
				addLinebreak();
				addComponent(ComponentFactory.makeItalicLabel("[This individuals answer is complete.]"));
				addLinebreak();
			}
			else if (! completeIndividuals.isEmpty()) {
				final JButton buttonCompleter = ComponentFactory.makeButton(LABEL_DECLARE_COMPLETE);
				buttonCompleter.setToolTipText("Add a new snippet asserting that this answer is complete.");
				buttonCompleter.setBackground(Colors.BG_COLOR);

				buttonCompleter.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						OWLLogicalAxiom axiom = df.getOWLSubClassOfAxiom(dlquery, df.getOWLObjectOneOf(completeIndividuals));
						if (confirmAndAdd(buttonCompleter, axiom)) {
							answer.setIndividualAnswersComplete(true);
						}
					}
				});

				addLinebreak();
				addComponent(buttonCompleter);
				addLinebreak();
			}
			else {
				addLinebreak();
			}

			addComponent(ComponentFactory.makeItalicLabel(dc + " named classes:"));
			final Set<OWLClass> completeSubClasses = renderSubClasses(subclasses, dlquery);

			if (answer.isSubClassesAnswersComplete()) {
				addLinebreak();
				addComponent(ComponentFactory.makeItalicLabel("[This subclasses answer is complete.]"));
				addLinebreak();
			}
			else if (! completeSubClasses.isEmpty()) {
				final JButton buttonCompleter = ComponentFactory.makeButton(LABEL_DECLARE_COMPLETE);
				buttonCompleter.setToolTipText("Add a new snippet asserting that this answer is complete.");
				buttonCompleter.setBackground(Colors.BG_COLOR);

				buttonCompleter.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						OWLLogicalAxiom axiom = df.getOWLSubClassOfAxiom(dlquery, df.getOWLObjectUnionOf(completeSubClasses));
						if (confirmAndAdd(buttonCompleter, axiom)) {
							answer.setSubClassesAnswersComplete(true);
						}
					}
				});

				addLinebreak();
				addComponent(buttonCompleter);
				addLinebreak();
			}
			else {
				addLinebreak();
			}

			addComponent(ComponentFactory.makeItalicLabel("Every answer is:"));
			final Set<OWLClass> completeSupClasses = renderSupClasses(superclasses, dlquery);
			addComponent(ComponentFactory.makeItalicLabel("(" + ac + " named classes)"));
		}
	}


	private void addLinebreak() {
		try {
			doc.insertString(doc.getLength(), "\n\n", null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}


	private void addComponent(JComponent component) {
		// The component must first be wrapped in a style
		Style style = doc.addStyle("StyleName", null);
		StyleConstants.setComponent(style, component);

		try {
			// TODO: BUG: Can't be an empty string? Nothing shows up then.
			// Maybe use the string content of the component.
			// It would make it copyable?
			doc.insertString(doc.getLength(), "_", style);
			doc.insertString(doc.getLength(), " ", null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}


	private JXHyperlink getHyperlink(OWLEntity entity, OWLLogicalAxiom axiom) {
		return new JXHyperlink(new AxiomLinkAction(ws, entity, axiom));
	}


	private void removeButton(JButton button) {
		button.setText("[This answer is now complete.]");
		button.setOpaque(false);
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
	}


	private boolean confirmAndAdd(final JButton buttonCompleter, OWLLogicalAxiom axiom) {
		ACESnippet snippet = null;
		try {
			snippet = ACETextManager.makeSnippetFromAxiom(axiom);
		} catch (OWLRendererException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		}
		if (snippet != null) {
			ACESentenceRenderer snippetRenderer = new ACESentenceRenderer(new IriRenderer(ACETextManager.getActiveACELexicon()), snippet.getSentences());
			JComponent comp = ComponentFactory.makeSnippetDialogPanel("Add this snippet to the active ACE text?", snippetRenderer.getRendering());
			int ret = new UIHelper(ws.getOWLEditorKit()).showDialog(LABEL_DECLARE_COMPLETE, comp, JOptionPane.OK_CANCEL_OPTION);
			if (ret == JOptionPane.OK_OPTION) {
				buttonCompleter.setEnabled(false);
				ACETextManager.addSnippet(snippet);
				removeButton(buttonCompleter);
				return true;
			}
		}
		return false;
	}


	private Set<OWLNamedIndividual> renderIndividuals(Set<Node<OWLNamedIndividual>> entityNodes, OWLClassExpression dlquery) {
		Set<OWLNamedIndividual> repEntities = Sets.newHashSet();
		for (Node<OWLNamedIndividual> node : entityNodes) {
			OWLNamedIndividual repEntity = node.getRepresentativeElement();
			repEntities.add(repEntity);

			if (node.isSingleton()) {
				addComponent(getHyperlink(repEntity, df.getOWLClassAssertionAxiom(dlquery, repEntity)));					
			}
			else {
				addComponent(ComponentFactory.makeItalicLabel("{"));
				for (OWLNamedIndividual ind : node.getEntities()) {
					addComponent(getHyperlink(ind, df.getOWLClassAssertionAxiom(dlquery, ind)));
				}
				addComponent(ComponentFactory.makeItalicLabel("}"));
			}
		}
		return repEntities;
	}

	private Set<OWLClass> renderSubClasses(Set<Node<OWLClass>> entityNodes, OWLClassExpression dlquery) {
		Set<OWLClass> repEntities = Sets.newHashSet();
		for (Node<OWLClass> node : entityNodes) {
			OWLClass repEntity = node.getRepresentativeElement();
			repEntities.add(repEntity);

			if (node.isSingleton()) {
				addComponent(getHyperlink(repEntity, df.getOWLSubClassOfAxiom(repEntity, dlquery)));
			}
			else {
				addComponent(ComponentFactory.makeItalicLabel("{"));
				for (OWLClass entity : node.getEntities()) {
					addComponent(getHyperlink(entity, df.getOWLSubClassOfAxiom(entity, dlquery)));
				}
				addComponent(ComponentFactory.makeItalicLabel("}"));
			}
		}
		return repEntities;
	}

	private Set<OWLClass> renderSupClasses(Set<Node<OWLClass>> entityNodes, OWLClassExpression dlquery) {
		Set<OWLClass> repEntities = Sets.newHashSet();
		for (Node<OWLClass> node : entityNodes) {
			OWLClass repEntity = node.getRepresentativeElement();
			repEntities.add(repEntity);

			if (node.isSingleton()) {
				addComponent(getHyperlink(repEntity, df.getOWLSubClassOfAxiom(dlquery, repEntity)));
			}
			else {
				addComponent(ComponentFactory.makeItalicLabel("{"));
				for (OWLClass entity : node.getEntities()) {
					addComponent(getHyperlink(entity, df.getOWLSubClassOfAxiom(dlquery, entity)));
				}
				addComponent(ComponentFactory.makeItalicLabel("}"));
			}
		}
		return repEntities;
	}
}