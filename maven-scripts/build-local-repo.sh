mvn install:install-file \
-DlocalRepositoryPath=../repo \
-Dfile=../src/main/resources/lib/functjonal-1.0-SNAPSHOT.jar \
-DgroupId=org.variantsync -DartifactId=functjonal-1.0-SNAPSHOT \
-Dversion=1.0-SNAPSHOT \
-Dpackaging=jar \
-DgeneratePom=true
rm -rf ~/.m2/repository/org/variantsync/
