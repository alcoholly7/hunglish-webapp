CLASSPATH="output/hunglishdict-1.0.jar"


for jar in lib/*.jar 
do
	CLASSPATH=$CLASSPATH:$jar
done
echo $CLASSPATH
export CLASSPATH
java -Xmx512m  -classpath $CLASSPATH mokk.nlp.irutil.IndexerConsole $*