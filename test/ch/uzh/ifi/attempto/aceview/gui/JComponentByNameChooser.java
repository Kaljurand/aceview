/**
 * 
 */
package ch.uzh.ifi.attempto.aceview.gui;

import org.netbeans.jemmy.ComponentChooser;

import javax.swing.*;
import java.awt.*;

final class JComponentByNameChooser implements
		ComponentChooser {
	private String name;
	boolean usingRegularExpression;
	
	public JComponentByNameChooser(String name) {
		this.name = name;
	}
	public JComponentByNameChooser(String name, boolean usingRegularExpression) {
		this(name);
		setUsingRegularExpression(usingRegularExpression);
	}
	
	public boolean checkComponent(Component c ) {
		if (c instanceof JComponent ) {
			JComponent b = (JComponent) c;
			String a = b.getName();
			if( a != null ) {
				if( usingRegularExpression ) {
					if( a.matches(name) ) {
						return true;
					}
				} else if( name.equals( a ) ) {
					return true;
				}
			}
		}
		return false;
	}

	public String getDescription() {
		return "NAME Component Chooser \"" + name + "\"" +
				"\n";
	}
	
	public void setUsingRegularExpression(boolean usingRegularExpression) {
		this.usingRegularExpression = usingRegularExpression;
	}

}
