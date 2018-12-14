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
import org.netbeans.jemmy.operators.JLabelOperator;
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
	 * paste in "ACE Snippet Editor" :
	 * 	Every dog is an animal.
	 * push on button "Add as new"
	 * this sentence should appear in panel "Paraphrases"
	 * If there is a dog X1
               then the dog X1 is an animal .
	 * the panel's title should be "Paraphrases: 1"
	 * this should appear in panel "Corresponding logical axioms"
	 * dog SubClassOf animal
	 * the panel's title should be "Corresponding logical axioms: OWL: 1 SWRL: 0"
	 */
	@Test
	public void test() throws InvocationTargetException, NoSuchMethodException,
	ClassNotFoundException {
		
		JTabbedPaneOperator aceViewtabbedPane = launchACEView();
		enterACEASnippet( aceViewtabbedPane, "Every dog is an animal." );
		
		{
			/* 
			 * this sentence should appear in panel "Paraphrases"
			 * If there is a dog X1
               then the dog X1 is an animal . */
			ComponentChooser finder
			= new JTextComponentOperator.JTextComponentByTextFinder(
					"If there is a dog X1.*", new RegExComparator() );
			new JTextComponentOperator( aceViewtabbedPane, finder );
			/* this works too: new JTextComponentOperator( protegeFrame, finder ); */
		}

		// the panel's title should be "Paraphrases: 1"
		new JLabelOperator( aceViewtabbedPane, "Paraphrases: 1" );

		// the panel's title should be this:
		new JLabelOperator( aceViewtabbedPane, "Corresponding logical axioms: OWL: 1   SWRL: 0" );

		/* this should appear in panel "Corresponding logical axioms" */
		ComponentChooser finder
		= new JTextComponentOperator.JTextComponentByTextFinder(
		"dog SubClassOf animal" );
		new JTextComponentOperator( aceViewtabbedPane, finder );
	}

	/**
	 * @param aceViewtabbedPane
	 */
	public static void enterACEASnippet( JTabbedPaneOperator aceViewtabbedPane, String aceText ) {
			// paste in "Ace snippet editor" :
			JComponentByTipFinder f = new JComponentByTipFinder( "Selected ACE snippet.");
			JTextComponentOperator jtextInput = new JTextComponentOperator( aceViewtabbedPane, f );
			jtextInput.setText( aceText );
			
			// 	 push on button "Add as new"
			new JButtonOperator( aceViewtabbedPane, "Add as new" ).push();
	}

	/**
	 * @return
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 */
	public static JTabbedPaneOperator launchACEView()
			throws InvocationTargetException, NoSuchMethodException,
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
		JTabbedPaneOperator aceViewtabbedPane = new JTabbedPaneOperator( protegeFrame, "ACE View" );
//		Component aceViewComponent = 
		aceViewtabbedPane.selectPage( "ACE View" );
		return aceViewtabbedPane;
	}

}
