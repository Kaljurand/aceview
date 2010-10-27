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

package ch.uzh.ifi.attempto.aceview;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.VerticalLayout;
import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.core.ui.util.UIUtil;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.aceview.ui.HideablePane;
import ch.uzh.ifi.attempto.aceview.ui.ServiceSelectionPane;

/**
 * <p>GUI for setting the ACE View preferences.</p>
 * 
 * @author Kaarel Kaljurand
 */
public class ACEViewPreferencesPanel extends OWLPreferencesPanel {

	private ServiceSelectionPane serviceSelectionPaneAceToOwl;
	private final JCheckBox checkboxGuessingEnabled = new JCheckBox();
	private final JCheckBox checkboxClexEnabled = new JCheckBox();
	private final JCheckBox checkboxParaphrase1Enabled = new JCheckBox();
	private final JCheckBox checkboxParseWithUndefinedTokens = new JCheckBox();
	private final JCheckBox checkboxUseMos = new JCheckBox();
	private final JCheckBox checkboxUpdateAnswersOnClassify = new JCheckBox();
	private final JCheckBox checkboxUseLexicon = new JCheckBox();

	private JTextField textfieldApe;

	private JComboBox cbApeWebservice;
	private JComboBox cbApeSocket;

	private JComboBox comboboxOwlToAce;


	//private JButton buttonBrowseApePath = new JButton(UIManager.getIcon("FileView.directoryIcon"));
	private final JButton buttonBrowseApePath = new JButton("Choose...");

	private JTextField tfApeSocket;


	@Override
	public void applyChanges() {
		ACEViewPreferences prefs = ACEViewPreferences.getInstance();

		// ACE to OWL
		prefs.setAceToOwl(serviceSelectionPaneAceToOwl.getSelectedItem().toString());

		prefs.setApePath(textfieldApe.getText());

		prefs.setAceToOwlWebservices(getComboBoxItemsAsStrings(cbApeWebservice));
		prefs.setAceToOwlWebserviceUrl(cbApeWebservice.getSelectedItem().toString());

		prefs.setAceToOwlSockets(getComboBoxItemsAsStrings(cbApeSocket));
		prefs.setAceToOwlSocketHost(cbApeSocket.getSelectedItem().toString());
		prefs.setAceToOwlSocketPort(Integer.parseInt(tfApeSocket.getText()));

		prefs.setParaphrase1Enabled(checkboxParaphrase1Enabled.isSelected());
		prefs.setGuessingEnabled(checkboxParseWithUndefinedTokens.isSelected() && checkboxGuessingEnabled.isSelected());
		prefs.setClexEnabled(checkboxParseWithUndefinedTokens.isSelected() && checkboxClexEnabled.isSelected());

		// OWL to ACE
		prefs.setOwlToAceWebservices(getComboBoxItemsAsStrings(comboboxOwlToAce));
		prefs.setOwlToAce(comboboxOwlToAce.getSelectedItem().toString());

		// OTHER
		prefs.setParseWithUndefinedTokens(checkboxParseWithUndefinedTokens.isSelected());
		prefs.setUseMos(checkboxUseMos.isSelected());
		prefs.setUpdateAnswersOnClassify(checkboxUpdateAnswersOnClassify.isSelected());
		prefs.setUseLexicon(checkboxUseLexicon.isSelected());

		// Because the parser settings might have changed, we update the parser holder.
		try {
			ParserHolder.updateACEParser(prefs);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
		}
	}

	public void dispose() throws Exception {
	}

	public void initialise() throws Exception {

		ACEViewPreferences prefs = ACEViewPreferences.getInstance();

		// OWL->ACE webservice URLs
		comboboxOwlToAce = new JComboBox(prefs.getOwlToAceWebservices().toArray());
		comboboxOwlToAce.setSelectedItem(prefs.getOwlToAce());
		comboboxOwlToAce.setEditable(true);

		checkboxParaphrase1Enabled.setSelected(prefs.isParaphrase1Enabled());
		checkboxParaphrase1Enabled.setToolTipText("Generate a Core ACE paraphrase for every snippet.");

		checkboxParseWithUndefinedTokens.setSelected(prefs.getParseWithUndefinedTokens());
		checkboxParseWithUndefinedTokens.setToolTipText("<html>Call the ACE parser even if some wordforms are undefined in the lexicon.<br>In this case you can rely on Clex and guessing.</html>");
		checkboxParseWithUndefinedTokens.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (checkboxParseWithUndefinedTokens.isSelected()) {
					checkboxGuessingEnabled.setEnabled(true);
					checkboxClexEnabled.setEnabled(true);
				}
				else {
					checkboxGuessingEnabled.setEnabled(false);
					checkboxClexEnabled.setEnabled(false);
				}
			}
		});

		checkboxUseMos.setSelected(prefs.getUseMos());
		checkboxUseMos.setToolTipText("Try to parse the snippets with the Manchester OWL Syntax parser first.");

		checkboxUpdateAnswersOnClassify.setSelected(prefs.isUpdateAnswersOnClassify());
		checkboxUpdateAnswersOnClassify.setToolTipText("Update answers automatically after each classification.");

		checkboxUseLexicon.setSelected(prefs.isUseLexicon());
		checkboxUseLexicon.setToolTipText("For each entity generate morphological annotations (e.g. plural forms) and use them when displaying the entity in ACE sentences.");

		textfieldApe = new JTextField(prefs.getApePath());

		buttonBrowseApePath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File f = chooseApeExecutable();
				if (f != null) {
					textfieldApe.setText(f.getAbsolutePath());
				}
			}
		});


		Box boxApe = new Box(BoxLayout.X_AXIS);
		boxApe.add(textfieldApe);
		boxApe.add(buttonBrowseApePath);

		// Configuration of APE Local
		JPanel panelSwiApe = new JPanel(new GridLayout(2, 1));
		panelSwiApe.add(new JLabel("APE:"));
		panelSwiApe.add(boxApe);

		// ACE->OWL webservice URLs
		cbApeWebservice = new JComboBox(prefs.getAceToOwlWebservices().toArray());
		cbApeWebservice.setSelectedItem(prefs.getAceToOwlWebserviceUrl());
		cbApeWebservice.setEditable(true);

		// Configuration of APE Webservice
		JPanel panelApeWebservice = new JPanel(new GridLayout(2, 1));
		panelApeWebservice.add(new JLabel("URL:"));
		panelApeWebservice.add(cbApeWebservice);

		// ACE->OWL socket service host + port 
		cbApeSocket = new JComboBox(prefs.getAceToOwlSockets().toArray());
		cbApeSocket.setSelectedItem(prefs.getAceToOwlSocketHost());
		cbApeSocket.setEditable(true);

		// Note: important: we convert int to String
		tfApeSocket = new JTextField("" + prefs.getAceToOwlSocketPort());

		// Configuration of APE Socket
		JPanel panelApeSocket = new JPanel(new GridLayout(4, 1));
		panelApeSocket.add(new JLabel("Host:"));
		panelApeSocket.add(cbApeSocket);
		panelApeSocket.add(new JLabel("Port:"));
		panelApeSocket.add(tfApeSocket);

		serviceSelectionPaneAceToOwl = new ServiceSelectionPane(
				new String[] {
						"APE Local",
						"APE Webservice",
						"APE Socket"
				},
				prefs.getAceToOwl(),
				new HideablePane[] {
					new HideablePane(panelSwiApe),
					new HideablePane(panelApeWebservice),
					new HideablePane(panelApeSocket)
				}
		);


		// ACE->OWL/SWRL configuration panel

		// checkboxClexEnabled is enabled only is prefs.getParseWithUndefinedTokens() is true
		checkboxClexEnabled.setSelected(prefs.isClexEnabled());
		checkboxClexEnabled.setToolTipText("Use ACE parser's built-in large English common words lexicon.");
		checkboxClexEnabled.setEnabled(prefs.getParseWithUndefinedTokens());
		Box clexBox = new Box(BoxLayout.X_AXIS);
		clexBox.add(checkboxClexEnabled);
		clexBox.add(new JLabel("Use Clex (large English common words lexicon)"));

		// checkboxGuessingEnabled is enabled only is prefs.getParseWithUndefinedTokens() is true
		checkboxGuessingEnabled.setSelected(prefs.isGuessingEnabled());
		checkboxGuessingEnabled.setToolTipText("Make ACE parser guess the word class of unknown words.");
		checkboxGuessingEnabled.setEnabled(prefs.getParseWithUndefinedTokens());
		Box guessBox = new Box(BoxLayout.X_AXIS);
		guessBox.add(checkboxGuessingEnabled);
		guessBox.add(new JLabel("Guess unknown words"));


		JPanel panelAceToOwl = new JPanel(new VerticalLayout());
		panelAceToOwl.setBorder(ComponentFactory.createTitledBorder("ACE\u2192OWL/SWRL service"));
		panelAceToOwl.add(serviceSelectionPaneAceToOwl);
		panelAceToOwl.add(clexBox);
		panelAceToOwl.add(guessBox);


		// OWL->ACE configuration panel
		JPanel panelOwlToAce = new JPanel(new GridLayout(1, 1));
		panelOwlToAce.setBorder(ComponentFactory.createTitledBorder("OWL\u2192ACE service"));
		panelOwlToAce.add(comboboxOwlToAce);


		// Options configuration panel
		Box boxParaphrase = new Box(BoxLayout.X_AXIS);
		boxParaphrase.add(checkboxParaphrase1Enabled);
		boxParaphrase.add(new JLabel("Generate paraphrase (in Core ACE)"));

		Box boxParseWithUndefinedTokens = new Box(BoxLayout.X_AXIS);
		boxParseWithUndefinedTokens.add(checkboxParseWithUndefinedTokens);
		boxParseWithUndefinedTokens.add(new JLabel("Parse sentences that contain undefined wordforms")); 

		Box boxUseMos = new Box(BoxLayout.X_AXIS);
		boxUseMos.add(checkboxUseMos);
		boxUseMos.add(new JLabel("Support Manchester OWL Syntax as an alternative input language"));

		Box boxUpdateAnswersOnClassify = new Box(BoxLayout.X_AXIS);
		boxUpdateAnswersOnClassify.add(checkboxUpdateAnswersOnClassify);
		boxUpdateAnswersOnClassify.add(new JLabel("Automatically update answers after classifying"));

		Box boxUseLexicon = new Box(BoxLayout.X_AXIS);
		boxUseLexicon.add(checkboxUseLexicon);
		boxUseLexicon.add(new JLabel("Use the lexicon of morphological annotations"));

		JPanel panelOptions = new JPanel(new GridLayout(5, 1));
		panelOptions.setBorder(ComponentFactory.createTitledBorder("Options"));
		panelOptions.add(boxParaphrase);
		panelOptions.add(boxParseWithUndefinedTokens);
		panelOptions.add(boxUseMos);
		panelOptions.add(boxUpdateAnswersOnClassify);
		panelOptions.add(boxUseLexicon);


		Box holderBox = new Box(BoxLayout.Y_AXIS);
		holderBox.add(panelAceToOwl);
		holderBox.add(panelOwlToAce);
		holderBox.add(panelOptions);

		setLayout(new BorderLayout());
		add(holderBox, BorderLayout.NORTH);
	}


	private List<String> getComboBoxItemsAsStrings(JComboBox cb) {
		List<String> strings = Lists.newArrayList();
		for (int i = 0; i < cb.getItemCount(); i++) {
			strings.add(cb.getItemAt(i).toString());
		}
		return strings;
	}


	/**
	 * <p>One could also use
	 * <code>getOWLEditorKit().getOWLWorkspace()</code>
	 * in the first argument to <code>UIUtil.openFile</code> but gets
	 * a differently behaving (and less useful) file chooser then, at least on Mac OS tiger.</p>
	 * 
	 * @return File path to the APE executable chosen my the user
	 */
	private File chooseApeExecutable() {
		JFrame frame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, getParent());
		return UIUtil.openFile(frame, "Specify the location of APE", "APE executable", Sets.newHashSet("exe"));
	}


	/**
	 * @deprecatd
	 * 
	 * This is the old way of opening files, which uses a Protege method
	 * that is now deprecated.
	 * 
	 * @param title
	 * @return
	 */
	private File chooseFile(String title) {
		Set<String> extensions = Sets.newHashSet();
		//extensions.add("exe");
		JFrame frame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, getParent());
		return UIUtil.openFile(frame, title, extensions);
	}
}