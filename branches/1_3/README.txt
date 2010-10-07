= ACE View for developers =

Author: Kaarel Kaljurand
Version: 2010-10-07

== License ==

Copyright 2008-2010, Attempto Group, University of Zurich (see http://attempto.ifi.uzh.ch).

ACE View is free software licensed under the GNU Lesser General Public
License (see LICENSE.txt and http://www.gnu.org/licenses/lgpl.html).

See <http://attempto.ifi.uzh.ch/aceview/> for the documentation and
information about the used third-party libraries.


== Introduction ==

In the following, "Protege" means "Protege 4.1".

== Files ==

* .classpath: sample classpath configuration for ACE View (used by Eclipse)
* .project: Eclipse project file
* .settings/: Eclipse settings files
* README.txt: this file
* RELEASE_NOTES.html: release notes
* build.xml: Ant build-file for ACE View
* classes/: directory into which the Java class files will be generated (both by Eclipse and Ant)
* lib/: external jar-files used by ACE View
* licenses/: licenses of the external jar-files
* manifest.txt: Manifest file for the ACE View jar-file
* plugin.xml: Protege plug-in file for ACE View
* src/: ACE View source files (in Java)
* test/: ACE View unit tests
* viewconfig-aceviewtab.xml: Protege viewconfig-file for ACE View (specifies the layout of the ACE View views)

Note: the files do not include Protege and OWL API jars. You have to install
Protege separately and set up the paths (as described below) to access the Protege jar-files.


== Getting the source code of Protege, building it and running the result ==

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

* Edit the file .classpath (in the root directory of the ACE View source)

Change the paths to the Protege trunk to the ones that point to your local copy of Protege.

* Launch Eclipse and create a new Java project

File -> New -> Project ...

Project name: ACE View
Create project from existing source
Directory: /the root directory of the ACE View source/


== Building ACE View and integrating it with Protege ==

* Edit the property "protege" in ACE View's build.xml so that the
location points to your local copy of Protege source.

* Build the ACE View jar-file and copy it to the Protege plugins-directory
(whenever you have made changes to the source):

ant (in ACE View root directory)

* Restart Protege

As a result, Protege is started with the latest changes that you have made
to ACE View integrated.


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
	ACEText: representation of an ACE text (= set of snippets)
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

* manifest.txt
* plugin.xml
* RELEASE_NOTES.html
* update.properties

== SVN properties ==

RELEASE_NOTES.html must be served as text/html by Google.
To check if it has the right svn property:

$ svn propget svn:mime-type RELEASE_NOTES.html
text/html

== Protege and ACE View preferences ==

Are stored in ~/Library/Preferences/ (on Mac OS X).
