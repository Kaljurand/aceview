#!/bin/sh

# This is the Protege 4 run.sh file customized for ACE View.
# This file must be used instead of the official run.sh when
# using APELocal for the ACE->OWL/SWRL translation.
# The only difference with respect to the official Protege run-file
# is the addition of 'java.library.path' and an additional classpath
# entry that points to 'jpl.jar'.

# On some systems, the SWI Prolog command is "pl" instead of "swipl":
eval `swipl -dump-runtime-variables`
#eval `pl -dump-runtime-variables`

# Under Linux, the environment variable LD_PRELOAD has to refer to the SWI
# Prolog library. Under some circumstances, also LD_LIBRARY_PATH has to be set.

#echo $PLBASE/lib/$PLARCH
#echo $PLBASE/lib/jpl.jar
#export LD_PRELOAD=$PLBASE/lib/$PLARCH/libjpl.so

cd `dirname $0`

java -Xmx500M -Xms250M \
     -Dlog4j.configuration=file:log4j.xml \
     -DentityExpansionLimit=100000000 \
     -Dfile.encoding=UTF-8 \
     -Djava.library.path=$PLBASE/lib/$PLARCH \
     -classpath bin/felix.jar:bin/ProtegeLauncher.jar:$PLBASE/lib/jpl.jar \
     org.protege.osgi.framework.Launcher
