# SPLVariantEvolution
Generate Clone-and-Own Histories with Ground-Truth in Space and Time from Software Product Lines

## Setup

To initialize to local maven repository for the libraries we use, run the following as maven targets:

- FeatureIDE: `deploy:deploy-file -DgroupId=de.ovgu -DartifactId=featureide.lib.fm -Dversion=3.7.0 -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=src/main/resources/lib/de.ovgu.featureide.lib.fm-v3.7.0.jar`
- Sat4j: `deploy:deploy-file -DgroupId=org.sat4j -DartifactId=core -Dversion=2.3.5 -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=src/main/resources/lib/org.sat4j.core.jar`