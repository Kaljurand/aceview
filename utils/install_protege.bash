
protege_home=${HOME}/TEST/Protege/

mkdir ${protege_home}
export PROTEGE_HOME=${protege_home}

echo "PROTEGE_HOME=${PROTEGE_HOME}"

for i in protege-base org.protege.common org.protege.editor.core.application org.semanticweb.owl.owlapi org.protege.editor.owl org.protege.jaxb
do
	cd $i
	ant install
	cd ..
done
