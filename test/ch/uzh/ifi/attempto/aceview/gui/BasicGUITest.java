package ch.uzh.ifi.attempto.aceview.gui;

import java.lang.reflect.InvocationTargetException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.FrameOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JComponentOperator.JComponentByTipFinder;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;

/**
 * @author Jean-Marc Vanel jeanmarc.vanel@gmail.com
 * 
 */
public class BasicGUITest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/* Scenario:
	 * 
	 * start Protégé
	 * wait for a new window
	 * click on "Create new OWL Ontology"
	 * wait for a new window entitled "Create ontology wizard"
	 * click on button "Continue"
	 * again
	 * click on button "Finish"
	 * click on tab "ACE View"
	 * paste in "Ace snippet editor" :
	 * 	Toulouse belongs-to France .
	 * If there is a woman then she does not like a snake.
	 * push on button "Add as new"
	 * the same sentence should appear in panel "Paraphrases"
	 * the panel's title should be "Paraphrases: 1"
	 * this should appear in panel "Corresponding logical axioms"
	 * 	Toulouse belong-to France
	 * the panel's title should be "Corresponding logical axioms: OWL: 1 SWRL: 0"
	 */
	@Test
	public void test() throws InvocationTargetException, NoSuchMethodException,
	ClassNotFoundException {
		{
			new ClassReference("org.protege.osgi.framework.Launcher")
			.startApplication();

			FrameOperator firstFrame = new FrameOperator();
			ComponentChooser chooser = new JComponentByNameChooser( "Create new .*", true );
			final JComponentOperator pseudoButton = new JComponentOperator( firstFrame, chooser );
			pseudoButton.clickMouse();
		}
		{
			//  "Create ontology wizard"
			JDialogOperator dialog = new JDialogOperator();
			new JButtonOperator( dialog, "Continue" ).push();
			new JButtonOperator( dialog, "Continue" ).push();
			new JButtonOperator( dialog, "Finish" ).push();
		}

		FrameOperator protegeFrame = new FrameOperator();

		// 	 * click on tab "ACE View"
		JTabbedPaneOperator tabbedPane = new JTabbedPaneOperator( protegeFrame, "ACE View" );
//		Component aceViewComponent = 
		tabbedPane.selectPage( "ACE View" );

		{
			// paste in "Ace snippet editor" :
			JComponentByTipFinder f = new JComponentByTipFinder( "Selected ACE snippet.");
			JTextComponentOperator jtextInput = new JTextComponentOperator( tabbedPane, f );
			jtextInput.setText( "If there is a woman then she does not like a snake." );
		}
		{
			// 	 push on button "Add as new"
			new JButtonOperator( tabbedPane, "Add as new" ).push();

			/* 
			 * this sentence should appear in panel "Paraphrases"
			 * If there is a woman X1
			then it is false
	    	that the woman X1 likes a snake . */
			ComponentChooser finder
			= new JTextComponentOperator.JTextComponentByTextFinder(
					"If there is a woman X1.*", new RegExComparator() );
			new JTextComponentOperator( tabbedPane, finder );
			/* this works too:
			 * new JTextComponentOperator( protegeFrame, finder ); */
		}

		/* TODO
		 * the panel's title should be "Paraphrases: 1"
		 * this should appear in panel "Corresponding logical axioms"
		 * 	Toulouse belong-to France
		 * the panel's title should be "Corresponding logical axioms: OWL: 1 SWRL: 0"
		 */
	}

}
