Notes Oct 2016
==============

Notes written in October 2016 will trying to build/use ACE View with Protege versions after 4.1.

Compatibility with Protege
--------------------------

v5.1
~~~~

Installation and launching succeed.

ACE View DOES NOT compile against Protege 5.1. Several required jars have been rearranged.

v4.3
~~~~

(Should be almost identical to Protege 4.2.)

Installation:

- http://protege.stanford.edu/download/protege/4.3/installanywhere/Web_Installers/
- Linux 64bit
- with Java VM (v1.6)

ACE View compiles without errors.

Starting ./Protege produces::

    Unable to locate the application's 'main' class. The class 'org.protege.osgi.framework.Launcher' must be public and have a 'public static void main(String[])' method. (LAX)
    Unable to Launch Java Application: Unable to locate the application's 'main' class. The class 'org.protege.osgi.framework.Launcher' must be public and have a 'public static void main(String[])' method. (LAX)

v4.2
~~~~

Installation:

- http://protege.stanford.edu/download/protege/old-releases/Protege%204.x/4.2/installanywhere/Web_Installers/
- Linux 64bit
- with Java VM (v1.6)

ACE View compiles without errors.

Starting ./Protege succeeds.

ACE View runs without errors.

Install with Ant
----------------

- export PROTEGE_HOME=${HOME}/Protege_4.2/
- ant clean install


Developing with IntelliJ
------------------------

- install IntelliJ IDEA (https://www.jetbrains.com/idea/)
- load ACE View sources
- load Protege libs as external libs:

  - bundles/org.protege.editor.core.application.jar
  - plugins/org.protege.editor.owl.jar
  - plugins/org.semanticweb.owl.owlapi.jar
  - bin/felix.jar
  - bin/ProtegeLauncher.jar
