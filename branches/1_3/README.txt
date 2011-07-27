= ACE View for developers =

Author: Kaarel Kaljurand
Version: 2011-03-18

== License ==

Copyright 2008-2011, Attempto Group, University of Zurich (see http://attempto.ifi.uzh.ch).

ACE View is free software licensed under the GNU Lesser General Public
License (see LICENSE.txt and http://www.gnu.org/licenses/lgpl.html).

See <http://attempto.ifi.uzh.ch/aceview/> for the documentation and
information about the used third-party libraries.


== Introduction ==

In the following, "Protege" means "Protege 4.1".

== Read first ==

http://protegewiki.stanford.edu/wiki/Protege4DevDocs

== Files ==

Note: the files do not include Protege and OWL API jars. You have to install
Protege separately and set up the paths (as described below) to access the Protege jar-files.

* README.txt: this file
* RELEASE_NOTES.html: release notes
* build.xml: Ant build-file for ACE View
* lib/: external jar-files used by ACE View
* licenses/: licenses of the external jar-files
* META-INF/MANIFEST.MF: Manifest file for the ACE View jar-file
* plugin.xml: Protege plug-in file for ACE View
* src/: ACE View source files (in Java)
* test/: ACE View unit tests
* viewconfig-aceviewtab.xml: Protege viewconfig-file for ACE View (specifies the layout of the ACE View views)

=== Eclipse files ==

Only needed if you intend to use Eclipse to make changes to ACE View.

* .classpath: Eclipse classpath configuration
* .project: Eclipse project file
* .settings/: Eclipse settings files

=== Automatically generated files ==

* classes/: directory into which the Java class files will be generated (by Eclipse)
* build/: directory into which the Java class files will be generated (by Ant)
* javadoc/: directory into which the Java class files will be generated (by Ant)


== Getting the source code of Protege, building it and running the result ==

In order to build ACE View from the sources you don't need the sources
of Protege or OWL-API, just download and install the latest released version
of Protege and set the environment variable $PROTEGE_HOME to the root
folder of this new installation. Note however that we develop (at least at the moment)
against the latest svn version of Protege which means that ACE View is less likely
to compile against the latest released version (depending on the time difference
the latest Protege release has compared to its latest svn version).
So it's a better idea to fetch the Protege sources from the svn, as described next.

1. Read:

http://protegewiki.stanford.edu/wiki/Protege4DevDocs#Building_Protege4.1_from_scratch_using_ant

2. Set $PROTEGE_HOME (e.g. "export PROTEGE_HOME=${HOME}/TEST/Protege/")

3. Check out and install Protege
(see the Bash-script utils/checkout_and_install_protege.bash)

4. cd $PROTEGE_HOME

5. sh run.sh

[BUG: rewrite. You could also specify the revision of Protege to be checked out
so that the Protege source matches
the source that ACE View was tested against. (See the ACE View RELEASE_NOTES.txt for
information on which Protege revision was used.)]

[BUG: rewrite. Do it if you need to make sure that your local copy of Protege contains
the last revision of Protege. Note that updating to the latest revision might break
some of the ACE View code, so it is useful to have two revisions checked out,
one that seems stable and the other that is the latest. You can develop against the
stable revision and occasionally test against that latest one.]


== Setting up the ACE View Eclipse project ==

We have tested the following with
* Eclipse 3.5.2 on OS X 10.4 using Java 1.5
* Eclipse 3.6.1 on Ubuntu 10.04 using Java 1.6.

* Launch Eclipse and create a new Java project

1. Choose: File -> New -> Project ...
2. Set project name: ACE View 1.3
3. Create project from existing source (in Eclipse 3.6 it is called a "nondefault location").
4. Choose the folder: the root directory of the ACE View source (must contain the .classpath file)
5. Configure the Eclipse variable PROTEGE41 (see the instructions below)
6. The project is now going to be rebuilt resulting in no errors and some warnings.
(There will be more warnings if you use Java 1.6, but don't worry about them.)

The paths to the external jars are configured automatically based on the .classpath file.
But you have to additionally set the Eclipse variable PROTEGE41 to point to $PROTEGE_HOME
so that the Protege and OWL-API jars can be accessed.
(You can also set PROTEGE41_SRC to point to the the Protege sources.)
To do that follow this path of menus (in Eclipse 3.6.1):
Project -> Properties -> Java Build Path -> Libraries -> Edit... -> Variable... -> New...
Now type in the new variable PROTEGE41 and set the folder to whereever you have
installed Protege.

=== More notes ===

* For spellchecking use the dictionary (i.e. list of exceptional words) utils/eclipse_dictionary.txt


== Building ACE View and integrating it with Protege ==

* Set $PROTEGE_HOME (put this into .bashrc or a similar startup script)

* Go into the ACE View directory (must contain build.xml)

* ant install: builds ACE View and installs it into $PROTEGE_HOME plugins directory

* ant run: same as "ant install" but additionally starts Protege



= Code =

== Layout ==

./ch/uzh/ifi/attempto/ace:
	This package contains some general ACE utilities:
	tokenizer, sentence splitter.
	These will distributed in the Attempto Java Packages at some point, i.e.
	they will not stay in the ACE View source tree.

./ch/uzh/ifi/attempto/aceview:
	Most notable classes are:
	ACESnippet: representation of a snippet
	ACEText: representation of an ACE text (= ordered set of snippets)
	ACETextManager: lots of important static methods, many generate events
	in case a change in the ACE text occurs.
	ACEPreferences: model for the ACE View preferences
	ACEPreferencesPanel: UI for the ACE View preferences
	ACEViewTab: implementation of the ACE View tab. Listens to Protege events and
	translates them to ACE View events.

./ch/uzh/ifi/attempto/aceview/lexicon:
	This package contains classes for managing the ACE lexicon:
	ACE lexicon entry types (noun, verb, proper name),
	ACE lexicon field types (sg, pl, vbg),
	auto-completer.

./ch/uzh/ifi/attempto/aceview/model:
	This package contains table models. Most of the UI in ACE View is based on tables.
	These tables get their data from the table models in this package.

./ch/uzh/ifi/attempto/aceview/model/event:
	Events, event type enums, event listener interfaces

./ch/uzh/ifi/attempto/aceview/predicate:
	This package contains various implementations of the SwingX's HighlightPredicate interface.
	Used for highlighting entity selections, error messages, etc.

./ch/uzh/ifi/attempto/aceview/ui:
	This package contains:
	ACESnippetEditor: used by ACE Snippet Editor (extended version of JTextArea)
	ACETable: used by all ACE View tables (extended version of JXTable)
	SnippetAutocompleter: pop-up that shows auto-completer results (customized version of a similar
		auto-completer in Protege)

./ch/uzh/ifi/attempto/aceview/ui/action:
	This package contains the implementation of two "actions" currently provided by ACE View

./ch/uzh/ifi/attempto/aceview/ui/util:
	This package contains some utilities, e.g. for an easier creation of the ACE View UI.

./ch/uzh/ifi/attempto/aceview/ui/view:
	Implementations of ACE View view components.

./ch/uzh/ifi/attempto/aceview/util:
	Some utilities.


= Other =

== Version numbers ==

Before every release,
modify these files to update the ACE View version number:

* build.xml
* plugin.xml
* RELEASE_NOTES.html
* update.properties (updated automatically by "ant dist")

== SVN properties ==

RELEASE_NOTES.html must be served as text/html by Google.
To check if it has the right svn property:

$ svn propget svn:mime-type RELEASE_NOTES.html
text/html

if not then:

$ svn propset svn:mime-type "text/html" RELEASE_NOTES.html

== Protege and ACE View preferences ==

See: http://protegewiki.stanford.edu/wiki/ClearingP4Preferences

Linux: ~/.java/.userPrefs
Mac OS X: ~/Library/Preferences/com.apple.java.util.prefs.plist
Windows: Windows Registry at HKEY_CURRENT_USER/Software/JavaSoft/Prefs

== Other ==

What is the simplest way of replacing a file in a zip archive?
Something like:

zip -in ch.uzh.ifi.attempto.aceview.ui.view.jar -replace lib/jpl.jar -with /usr/lib/blah/jpl.jar
