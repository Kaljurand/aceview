package ch.uzh.ifi.attempto.aceview.ui;

import org.apache.log4j.Logger;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponent;
import org.protege.editor.owl.ui.renderer.OWLRendererPreferences;
import org.semanticweb.owl.model.*;

import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.ace.ACEToken;
import ch.uzh.ifi.attempto.ace.ACETokenizer;
import ch.uzh.ifi.attempto.aceview.ACETextManager;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ACECellRenderer implements TableCellRenderer, TreeCellRenderer, ListCellRenderer {

	private static final Logger logger = Logger.getLogger(ACECellRenderer.class);

	private OWLEditorKit owlEditorKit;

	private boolean renderIcon;

	private boolean renderExpression;

	private OWLOntology ontology;

	private LinkedObjectComponent linkedObjectComponent;

	private Font plainFont;

	public static final Color SELECTION_BACKGROUND = UIManager.getDefaults().getColor("List.selectionBackground");

	public static final Color SELECTION_FOREGROUND = UIManager.getDefaults().getColor("List.selectionForeground");

	public static final Color FOREGROUND = UIManager.getDefaults().getColor("List.foreground");

	private boolean gettingCellBounds;

	// The object that determines which icon should be displayed.
	private OWLObject iconObject;

	private int rightMargin = 40;

	private JComponent componentBeingRendered;

	private JPanel renderingComponent;

	private JLabel iconLabel;

	private JTextPane textPane;

	private int preferredWidth;

	private int minTextHeight;

	private OWLEntity focusedEntity;

	private boolean commentedOut;

	private boolean inferred;

	private boolean highlightKeywords;

	private boolean wrap = true;

	private Set<OWLEntity> crossedOutEntities;

	private Set<String> boxedNames;

	private int plainFontHeight;

	private boolean opaque = false;

	private boolean linkRendered = false;


	public ACECellRenderer(OWLEditorKit owlEditorKit) {
		this(owlEditorKit, true, true);
	}


	public ACECellRenderer(OWLEditorKit owlEditorKit, boolean renderExpression, boolean renderIcon) {
		this.owlEditorKit = owlEditorKit;
		this.renderExpression = renderExpression;
		this.renderIcon = renderIcon;

		iconLabel = new JLabel("");
		iconLabel.setOpaque(false);
		iconLabel.setVerticalAlignment(SwingConstants.CENTER);

		textPane = new JTextPane();
		textPane.setOpaque(false);

		renderingComponent = new JPanel(new OWLCellRendererLayoutManager());
		renderingComponent.add(iconLabel);
		renderingComponent.add(textPane);

		crossedOutEntities = Sets.newHashSet();
		boxedNames = Sets.newHashSet();
		prepareStyles();
		setupFont();
	}


	public void setOpaque(boolean opaque){
		this.opaque = opaque;
	}


	public void setHighlightKeywords(boolean hightlighKeywords) {
		this.highlightKeywords = hightlighKeywords;
	}


	public void setIconObject(OWLObject object) {
		iconObject = object;
	}

	public void setCrossedOutEntities(Set<OWLEntity> entities) {
		crossedOutEntities.addAll(entities);
	}

	public void addBoxedName(String name) {
		boxedNames.add(name);
	}

	public boolean isBoxedName(String name) {
		return boxedNames.contains(name);
	}

	public void reset() {
		iconObject = null;
		rightMargin = 0;
		ontology = null;
		focusedEntity = null;
		commentedOut = false;
		inferred = false;
		crossedOutEntities.clear();
		boxedNames.clear();
	}


	public void setFocusedEntity(OWLEntity entity) {
		focusedEntity = entity;
	}


	public int getPreferredWidth() {
		return preferredWidth;
	}


	public void setPreferredWidth(int preferredWidth) {
		this.preferredWidth = preferredWidth;
	}


	public int getRightMargin() {
		return rightMargin;
	}


	public void setRightMargin(int rightMargin) {
		this.rightMargin = rightMargin;
	}


	private void setupFont() {
		plainFont = OWLRendererPreferences.getInstance().getFont();
		plainFontHeight = iconLabel.getFontMetrics(plainFont).getHeight();
		// boldFont = plainFont.deriveFont(Font.BOLD);
		textPane.setFont(plainFont);
	}

	protected int getFontSize() {
		return OWLRendererPreferences.getInstance().getFontSize();
	}


	public boolean isRenderExpression() {
		return renderExpression;
	}


	public boolean isRenderIcon() {
		return renderIcon;
	}


	public void setCommentedOut(boolean commentedOut) {
		this.commentedOut = commentedOut;
	}


	public boolean isWrap() {
		return wrap;
	}


	public void setWrap(boolean wrap) {
		this.wrap = wrap;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation of renderer interfaces
	//
	////////////////////////////////////////////////////////////////////////////////////////

	private boolean renderLinks;


	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		setupLinkedObjectComponent(table, table.getCellRect(row, column, true));
		preferredWidth = table.getParent().getWidth();
		componentBeingRendered = table;
		// Set the size of the table cell
		//		setPreferredWidth(table.getColumnModel().getColumn(column).getWidth());
		return prepareRenderer(value, isSelected, hasFocus);

		//		// This is a bit messy - the row height doesn't get reset if it is larger than the
		//		// desired row height.
		//		// Reset the row height if the text has been wrapped
		//		int desiredRowHeight = getPrefSize(table, table.getGraphics(), c.getText()).height;
		//		if (desiredRowHeight < table.getRowHeight()) {
		//		desiredRowHeight = table.getRowHeight();
		//		}
		//		else if (desiredRowHeight > table.getRowHeight(row)) {
		//		// Add a bit of a margin, because wrapped lines
		//		// tend to merge with adjacent lines too much
		//		desiredRowHeight += 4;
		//		}
		//		if (table.getEditingRow() != row) {
		//		if (table.getRowHeight(row) < desiredRowHeight) {
		//		table.setRowHeight(row, desiredRowHeight);
		//		}
		//		}
		//		reset();
		//		return c;
	}


	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		componentBeingRendered = tree;
		Rectangle cellBounds = new Rectangle();
		if (!gettingCellBounds) {
			gettingCellBounds = true;
			cellBounds = tree.getRowBounds(row);
			gettingCellBounds = false;
		}
		setupLinkedObjectComponent(tree, cellBounds);
		preferredWidth = -1;
		minTextHeight = 12;
		//		textPane.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2 + rightMargin));
		tree.setToolTipText(value != null ? value.toString() : "");
		Component c = prepareRenderer(value, selected, hasFocus);
		reset();
		return c;
	}


	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		componentBeingRendered = list;
		Rectangle cellBounds = new Rectangle();
		// We need to prevent infinite recursion here!
		if (!gettingCellBounds) {
			gettingCellBounds = true;
			cellBounds = list.getCellBounds(index, index);
			gettingCellBounds = false;
		}
		minTextHeight = 12;
		if (list.getParent() != null) {
			preferredWidth = list.getParent().getWidth();
		}
		//		preferredWidth = -1;
		//		textPane.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2 + rightMargin));
		setupLinkedObjectComponent(list, cellBounds);
		Component c = prepareRenderer(value, isSelected, cellHasFocus);
		reset();
		return c;
	}


	private void setupLinkedObjectComponent(JComponent component, Rectangle cellRect) {
		renderLinks = false;
		linkedObjectComponent = null;
		if (cellRect == null) {
			return;
		}
		if (component instanceof LinkedObjectComponent && OWLRendererPreferences.getInstance().isRenderHyperlinks()) {
			linkedObjectComponent = (LinkedObjectComponent) component;
			Point mouseLoc = component.getMousePosition(true);
			if (mouseLoc == null) {
				linkedObjectComponent.setLinkedObject(null);
				return;
			}
			renderLinks = cellRect.contains(mouseLoc);
		}
	}


	private Component prepareRenderer(Object value, boolean isSelected, boolean hasFocus) {
		renderingComponent.setOpaque(isSelected || opaque);

		prepareTextPane(getRendering(value), isSelected);

		if (isSelected) {
			renderingComponent.setBackground(SELECTION_BACKGROUND);
			textPane.setForeground(SELECTION_FOREGROUND);
		}
		else {
			renderingComponent.setBackground(componentBeingRendered.getBackground());
			textPane.setForeground(componentBeingRendered.getForeground());
		}

		final Icon icon = getIcon(value);
		iconLabel.setIcon(icon);
		if (icon != null){
			iconLabel.setPreferredSize(new Dimension(icon.getIconWidth(), plainFontHeight));
		}
		renderingComponent.revalidate();
		return renderingComponent;
	}


	protected String getRendering(Object object) {
		if (object instanceof OWLObject) {
			String rendering = getOWLModelManager().getRendering(((OWLObject) object));
			return rendering;
		}
		if (object != null) {
			return object.toString();
		}
		return "";
	}


	protected Icon getIcon(Object object) {
		if(!renderIcon) {
			return null;
		}
		if (iconObject != null) {
			return owlEditorKit.getWorkspace().getOWLIconProvider().getIcon(iconObject);
		}
		if (object instanceof OWLObject) {
			return owlEditorKit.getWorkspace().getOWLIconProvider().getIcon((OWLObject) object);
		}
		return null;
	}


	private OWLModelManager getOWLModelManager() {
		return owlEditorKit.getModelManager();
	}


	private Style plainStyle;

	private Style boldStyle;

	private Style nonBoldStyle;

	private Style selectionForeground;

	private Style foreground;

	private Style linkStyle;

	private Style inconsistentClassStyle;

	private Style focusedEntityStyle;

	//	private Style linespacingStyle;

	private Style annotationURIStyle;

	private Style ontologyURIStyle;

	private Style commentedOutStyle;

	private Style strikeOutStyle;

	private Style fontSizeStyle;

	private void prepareStyles() {
		StyledDocument doc = textPane.getStyledDocument();
		Map<String, Color> keyWordColorMap = owlEditorKit.getWorkspace().getKeyWordColorMap();
		for (String keyWord : keyWordColorMap.keySet()) {
			Style s = doc.addStyle(keyWord, null);
			Color color = keyWordColorMap.get(keyWord);
			StyleConstants.setForeground(s, color);
			StyleConstants.setBold(s, true);
		}
		plainStyle = doc.addStyle("PLAIN_STYLE", null);
		//		StyleConstants.setForeground(plainStyle, Color.BLACK);
		StyleConstants.setItalic(plainStyle, false);
		StyleConstants.setSpaceAbove(plainStyle, 0);
		//		StyleConstants.setFontFamily(plainStyle, textPane.getFont().getFamily());

		boldStyle = doc.addStyle("BOLD_STYLE", null);
		StyleConstants.setBold(boldStyle, true);


		nonBoldStyle = doc.addStyle("NON_BOLD_STYLE", null);
		StyleConstants.setBold(nonBoldStyle, false);

		selectionForeground = doc.addStyle("SEL_FG_STYPE", null);
		StyleConstants.setForeground(selectionForeground, SELECTION_FOREGROUND);

		foreground = doc.addStyle("FG_STYLE", null);
		StyleConstants.setForeground(foreground, FOREGROUND);

		linkStyle = doc.addStyle("LINK_STYLE", null);
		StyleConstants.setForeground(linkStyle, Color.BLUE);
		StyleConstants.setUnderline(linkStyle, true);

		inconsistentClassStyle = doc.addStyle("INCONSISTENT_CLASS_STYLE", null);
		StyleConstants.setForeground(inconsistentClassStyle, Color.RED);

		focusedEntityStyle = doc.addStyle("FOCUSED_ENTITY_STYLE", null);
		StyleConstants.setForeground(focusedEntityStyle, Color.BLACK);
		StyleConstants.setBackground(focusedEntityStyle, new Color(220, 220, 250));

		//		linespacingStyle = doc.addStyle("LINE_SPACING_STYLE", null);
		//		StyleConstants.setLineSpacing(linespacingStyle, 0.0f);

		annotationURIStyle = doc.addStyle("ANNOTATION_URI_STYLE", null);
		StyleConstants.setForeground(annotationURIStyle, Color.BLUE);
		StyleConstants.setItalic(annotationURIStyle, true);

		ontologyURIStyle = doc.addStyle("ONTOLOGY_URI_STYLE", null);
		StyleConstants.setForeground(ontologyURIStyle, Color.GRAY);

		commentedOutStyle = doc.addStyle("COMMENTED_OUT_STYLE", null);
		StyleConstants.setForeground(commentedOutStyle, Color.GRAY);
		StyleConstants.setItalic(commentedOutStyle, true);

		strikeOutStyle = doc.addStyle("STRIKE_OUT", null);
		StyleConstants.setStrikeThrough(strikeOutStyle, true);
		StyleConstants.setBold(strikeOutStyle, false);

		fontSizeStyle = doc.addStyle("FONT_SIZE", null);
		StyleConstants.setFontSize(fontSizeStyle, 40);
	}


	private void prepareTextPane(Object value, boolean selected) {

		textPane.setBorder(null);
		String theVal = value.toString();
		if (!wrap) {
			theVal = theVal.replace('\n', ' ');
			theVal = theVal.replaceAll(" [ ]+", " ");
		}
		textPane.setText(theVal);
		if (commentedOut) {
			textPane.setText("// " + textPane.getText());
		}
		//		textPane.setSize(textPane.getPreferredSize());
		StyledDocument doc = textPane.getStyledDocument();
		//		doc.setParagraphAttributes(0, doc.getLength(), linespacingStyle, false);
		resetStyles(doc);

		if (selected) {
			doc.setParagraphAttributes(0, doc.getLength(), selectionForeground, false);
		}
		else {
			doc.setParagraphAttributes(0, doc.getLength(), foreground, false);
		}

		if (commentedOut) {
			doc.setParagraphAttributes(0, doc.getLength(), commentedOutStyle, false);
			return;
		}
		else if (inferred) {

		}


		if (ontology != null) {
			if (OWLRendererPreferences.getInstance().isHighlightActiveOntologyStatements() && getOWLModelManager().getActiveOntology().equals(
					ontology)) {
				doc.setParagraphAttributes(0, doc.getLength(), boldStyle, false);
			}
			else {
				doc.setParagraphAttributes(0, doc.getLength(), nonBoldStyle, false);
			}
		}
		else {
			textPane.setFont(plainFont);
		}

		highlightText(doc);
	}


	private void highlightText(StyledDocument doc) {
		List<ACEToken> tokens = ACETokenizer.tokenize(textPane.getText());
		linkRendered = false;
		int tokenStartIndex = 0;

		for (ACEToken token : tokens) {
			String curToken = token.toString();
			renderToken(curToken, tokenStartIndex, doc);
			tokenStartIndex += curToken.length() + 1;
		}
		if (renderLinks && !linkRendered) {
			linkedObjectComponent.setLinkedObject(null);
		}
	}


	/**
	 * TODO: Mark if the wordform is ambiguous.
	 * 
	 * @param curToken
	 * @param tokenStartIndex
	 * @param doc
	 */
	protected void renderToken(final String curToken, final int tokenStartIndex, final StyledDocument doc) {

		OWLRendererPreferences prefs = OWLRendererPreferences.getInstance();

		int tokenLength = curToken.length();
		Color c = owlEditorKit.getWorkspace().getKeyWordColorMap().get(curToken);
		if (c != null && prefs.isHighlightKeyWords() && highlightKeywords) {
			Style s = doc.getStyle(curToken);
			doc.setCharacterAttributes(tokenStartIndex, tokenLength, s, true);
		}
		else {
			Set<OWLEntity> curEntities = ACETextManager.getActiveACELexicon().getWordformEntities(curToken);
			if (curEntities != null && ! curEntities.isEmpty()) {
				OWLEntity curEntity = curEntities.iterator().next();

				if (curEntities.size() > 1) {
					logger.info("Ambiguous entity: " + curEntity);
				}
				if (focusedEntity != null) {
					if (curEntity.equals(focusedEntity)) {
						doc.setCharacterAttributes(tokenStartIndex, tokenLength, focusedEntityStyle, true);
					}
				}
				strikeoutEntityIfCrossedOut(curEntity, doc, tokenStartIndex, tokenLength);

				if (renderLinks) {
					renderHyperlink(curEntity, tokenStartIndex, tokenLength, doc);
				}
			}
			else {
				// If token does not correspond to an OWL entity
			}
		}
	}


	private void renderHyperlink(OWLEntity curEntity, int tokenStartIndex, int tokenLength, StyledDocument doc) {
		try {
			Rectangle startRect = textPane.modelToView(tokenStartIndex);
			Rectangle endRect = textPane.modelToView(tokenStartIndex + tokenLength);
			if (startRect != null && endRect != null) {
				int width = endRect.x - startRect.x;
				int heght = startRect.height;

				Rectangle tokenRect = new Rectangle(startRect.x, startRect.y, width, heght);
				tokenRect.grow(0, -2);
				if (linkedObjectComponent.getMouseCellLocation() != null) {
					Point mouseCellLocation = linkedObjectComponent.getMouseCellLocation();
					if (mouseCellLocation != null) {
						mouseCellLocation = SwingUtilities.convertPoint(renderingComponent,
								mouseCellLocation,
								textPane);
						if (tokenRect.contains(mouseCellLocation)) {
							doc.setCharacterAttributes(tokenStartIndex, tokenLength, linkStyle, false);
							linkedObjectComponent.setLinkedObject(curEntity);
							linkRendered = true;
						}
					}
				}
			}
		}
		catch (BadLocationException e) {
			e.printStackTrace();
		}
	}


	private void strikeoutEntityIfCrossedOut(OWLEntity entity, StyledDocument doc, int tokenStartIndex,
			int tokenLength) {
		if(crossedOutEntities.contains(entity)) {
			doc.setCharacterAttributes(tokenStartIndex, tokenLength, strikeOutStyle, false);
		}
	}


	private void resetStyles(StyledDocument doc) {
		doc.setParagraphAttributes(0, doc.getLength(), plainStyle, true);
		StyleConstants.setFontSize(fontSizeStyle, getFontSize());
		Font f = OWLRendererPreferences.getInstance().getFont();
		StyleConstants.setFontFamily(fontSizeStyle, f.getFamily());
		doc.setParagraphAttributes(0, doc.getLength(), fontSizeStyle, false);
		setupFont();
	}


	private class OWLCellRendererLayoutManager implements LayoutManager2 {


		/**
		 * Adds the specified component to the layout, using the specified
		 * constraint object.
		 * @param comp        the component to be added
		 * @param constraints where/how the component is added to the layout.
		 */
		public void addLayoutComponent(Component comp, Object constraints) {
			// We only have two components the label that holds the icon
			// and the text area
		}


		/**
		 * Calculates the maximum size dimensions for the specified container,
		 * given the components it contains.
		 * @see java.awt.Component#getMaximumSize
		 * @see java.awt.LayoutManager
		 */
		public Dimension maximumLayoutSize(Container target) {
			return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
		}


		/**
		 * Returns the alignment along the x axis.  This specifies how
		 * the component would like to be aligned relative to other
		 * components.  The value should be a number between 0 and 1
		 * where 0 represents alignment along the origin, 1 is aligned
		 * the furthest away from the origin, 0.5 is centered, etc.
		 */
		public float getLayoutAlignmentX(Container target) {
			return 0;
		}


		/**
		 * Returns the alignment along the y axis.  This specifies how
		 * the component would like to be aligned relative to other
		 * components.  The value should be a number between 0 and 1
		 * where 0 represents alignment along the origin, 1 is aligned
		 * the furthest away from the origin, 0.5 is centered, etc.
		 */
		public float getLayoutAlignmentY(Container target) {
			return 0;
		}


		/**
		 * Invalidates the layout, indicating that if the layout manager
		 * has cached information it should be discarded.
		 */
		public void invalidateLayout(Container target) {
		}


		/**
		 * If the layout manager uses a per-component string,
		 * adds the component <code>comp</code> to the layout,
		 * associating it
		 * with the string specified by <code>name</code>.
		 * @param name the string to be associated with the component
		 * @param comp the component to be added
		 */
		public void addLayoutComponent(String name, Component comp) {
		}


		/**
		 * Removes the specified component from the layout.
		 * @param comp the component to be removed
		 */
		public void removeLayoutComponent(Component comp) {
		}


		/**
		 * Calculates the preferred size dimensions for the specified
		 * container, given the components it contains.
		 * @param parent the container to be laid out
		 * @see #minimumLayoutSize
		 */
		public Dimension preferredLayoutSize(Container parent) {
			if (componentBeingRendered instanceof JList) {
				JList list = (JList) componentBeingRendered;
				if (list.getFixedCellHeight() != -1) {
					return new Dimension(list.getWidth(), list.getHeight());
				}
			}
			int iconWidth;
			int iconHeight;
			int textWidth;
			int textHeight;
			int width;
			int height;
			iconWidth = iconLabel.getPreferredSize().width;
			iconHeight = iconLabel.getPreferredSize().height;
			Insets insets = parent.getInsets();
			Insets rcInsets = renderingComponent.getInsets();

			if (preferredWidth != -1) {
				textWidth = preferredWidth - iconWidth - rcInsets.left - rcInsets.right;
				View v = textPane.getUI().getRootView(textPane);
				v.setSize(textWidth, Integer.MAX_VALUE);
				textHeight = (int) v.getMinimumSpan(View.Y_AXIS);
				width = preferredWidth;
			}
			else {
				textWidth = textPane.getPreferredSize().width;
				textHeight = textPane.getPreferredSize().height;
				width = textWidth + iconWidth;
			}
			if (textHeight < iconHeight) {
				height = iconHeight;
			}
			else {
				height = textHeight;
			}
			int minHeight = minTextHeight;
			if (height < minHeight) {
				height = minHeight;
			}
			int totalWidth = width + rcInsets.left + rcInsets.right;
			int totalHeight = height + rcInsets.top + rcInsets.bottom;
			return new Dimension(totalWidth, totalHeight);
		}

		/**
		 * Lays out the specified container.
		 * @param parent the container to be laid out
		 */
		public void layoutContainer(Container parent) {
			int iconWidth;
			int iconHeight;
			int textWidth;
			int textHeight;
			Insets rcInsets = renderingComponent.getInsets();

			iconWidth = iconLabel.getPreferredSize().width;
			iconHeight = iconLabel.getPreferredSize().height;
			if (preferredWidth != -1) {
				textWidth = preferredWidth - iconWidth - rcInsets.left - rcInsets.right;
				View v = textPane.getUI().getRootView(textPane);
				v.setSize(textWidth, Integer.MAX_VALUE);
				textHeight = (int) v.getMinimumSpan(View.Y_AXIS);
			}
			else {
				textWidth = textPane.getPreferredSize().width;
				textHeight = textPane.getPreferredSize().height;
				if (textHeight < minTextHeight) {
					textHeight = minTextHeight;
				}
			}
			int leftOffset = rcInsets.left;
			int topOffset = rcInsets.top;
			iconLabel.setBounds(leftOffset, topOffset, iconWidth, iconHeight);
			textPane.setBounds(leftOffset + iconWidth, topOffset, textWidth, textHeight);
		}

		/**
		 * Calculates the minimum size dimensions for the specified
		 * container, given the components it contains.
		 * @param parent the component to be laid out
		 * @see #preferredLayoutSize
		 */
		public Dimension minimumLayoutSize(Container parent) {
			return new Dimension(0, 0);
		}
	}
}