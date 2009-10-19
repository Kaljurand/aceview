package ch.uzh.ifi.attempto.aceview.ui.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import ch.uzh.ifi.attempto.aceview.ui.Colors;

public class ComponentFactory {

	public static JTextArea makeTextArea() {
		JTextArea textarea = new JTextArea(25, 80);
		textarea.setFont(new Font("Monaco", Font.PLAIN, 11));
		textarea.setEnabled(true);
		textarea.setEditable(false);
		textarea.setLineWrap(false);
		textarea.setWrapStyleWord(false);
		textarea.setForeground(Colors.TEXT_COLOR);
		textarea.setBackground(Color.WHITE);
		return textarea;
	}


	public static Border makeBorder() {
		return BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.LIGHT_GRAY),
				BorderFactory.createEmptyBorder(4, 4, 4, 4)
		);
	}


	public static Border makeBorder(String title) {
		return BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), title),
				BorderFactory.createEmptyBorder(4, 4, 4, 4)
		);
	}

	public static JLabel makeInitialLetter(char ch, Font font) {
		JLabel initial = new JLabel(ch + " ");
		initial.setFont(font);
		return initial;
	}


	public static JLabel makeItalicLabel(String str, Font font) {
		JLabel label = new JLabel(str);
		label.setFont(font);
		return label;
	}


	public static JLabel makeInitialLetter(char ch) {
		return makeInitialLetter(ch, new Font("Monaco", Font.BOLD, 14));
	}


	public static JLabel makeItalicLabel(String str) {
		return makeItalicLabel(str, new Font("Monaco", Font.ITALIC, 12));
	}


	public static JButton makeButton(String str) {
		JButton button = new JButton(str);
		button.setFont(button.getFont().deriveFont(10.0f));
		return button;
	}


	public static JTextArea makeSmallTextArea() {
		JTextArea ta = new JTextArea(5, 30);
		ta.setEnabled(true);
		ta.setEditable(false);
		ta.setTabSize(2);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		ta.setBackground(Colors.BG_COLOR);
		return ta;
	}


	public static JPanel makeSnippetDialogPanel(String message, String content) {
		JTextArea ta = makeSmallTextArea();
		ta.setText(content);
		JLabel label = new JLabel("<html><i>" + message + "</i></html>");
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(label, BorderLayout.NORTH);
		panel.add(new JScrollPane(ta), BorderLayout.CENTER);
		return panel;
	}
}