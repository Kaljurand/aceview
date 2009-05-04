cd `dirname $0`
CMD_OPTIONS="-Djava.library.path=/opt/local/lib/swipl-5.6.61/lib/powerpc-darwin8.11.0/ -Dapple.laf.useScreenMenuBar=true -Xdock:name=Protege -classpath /opt/local/lib/swipl-5.6.61/lib/jpl.jar" sh run.sh
