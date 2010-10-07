
protege_home=${HOME}/TEST/Protege/
protege_repo="http://smi-protege.stanford.edu/repos/protege/protege4/"

mkdir ${protege_home}
export PROTEGE_HOME=${protege_home}

echo "PROTEGE_HOME=${PROTEGE_HOME}"

echo "svn checkout ${protege_repo}/protege-base/trunk protege-base"
svn checkout ${protege_repo}/protege-base/trunk protege-base
cd protege-base
ant install
cd ..

for i in org.protege.common org.protege.editor.core.application org.semanticweb.owl.owlapi org.protege.editor.owl org.protege.jaxb
do
	echo "svn checkout ${protege_repo}/plugins/$i/trunk $i"
	svn checkout ${protege_repo}/plugins/$i/trunk $i
	cd $i
	ant install
	cd ..
done
