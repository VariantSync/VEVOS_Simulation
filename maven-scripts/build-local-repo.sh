mvn install:install-file \
   -DlocalRepositoryPath=../repo \
   -Dfile=../src/main/resources/lib/de.ovgu.featureide.lib.fm-v3.8.1.jar \
   -DgroupId=de.ovgu \
   -DartifactId=featureide.lib.fm \
   -Dversion=3.8.1 \
   -Dpackaging=jar \
   -DgeneratePom=true

mvn install:install-file \
-DlocalRepositoryPath=../repo \
   -Dfile=../src/main/resources/lib/Functjonal-1.0-SNAPSHOT.jar \
   -DgroupId=vevos \
   -DartifactId=functjonal \
   -Dversion=1.0.0 \
   -Dpackaging=jar \
   -DgeneratePom=true

mvn install:install-file \
-DlocalRepositoryPath=../repo \
   -Dfile=../src/main/resources/lib/kernel_haven-1.0.0.jar \
   -DgroupId=net.ssehub \
   -DartifactId=kernel_haven \
   -Dversion=1.0.0 \
   -Dpackaging=jar \
   -DgeneratePom=true

mvn install:install-file \
-DlocalRepositoryPath=../repo \
   -Dfile=../src/main/resources/lib/kconfigreader-1.0.0.jar \
   -DgroupId=net.ssehub.kernelhaven \
   -DartifactId=kconfigreader \
   -Dversion=1.0.0 \
   -Dpackaging=jar \
   -DgeneratePom=true

mvn install:install-file \
-DlocalRepositoryPath=../repo \
   -Dfile=../src/main/resources/lib/org.sat4j.core.jar \
   -DgroupId=org.sat4j \
   -DartifactId=core \
   -Dversion=2.3.5 \
   -Dpackaging=jar \
   -DgeneratePom=true