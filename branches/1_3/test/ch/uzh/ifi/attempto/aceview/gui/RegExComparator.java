package ch.uzh.ifi.attempto.aceview.gui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.netbeans.jemmy.operators.Operator.StringComparator;

/**
 * @author Jean-Marc Vanel jeanmarc.vanel@gmail.com
 *
 */
public class RegExComparator implements StringComparator {

	public boolean equals(String caption, String match) {
        Pattern p = Pattern.compile( match, Pattern.DOTALL );
        Matcher m = p.matcher( caption );
        return m.matches();
	}

}
