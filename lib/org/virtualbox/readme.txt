Command used to install vboxws.jar version 5.2.32 to local repository:

mvn deploy:deploy-file
    -DgroupId=org.virtualbox
    -DartifactId=vboxws-52
    -Dversion=5.2.32
    -Dpackaging=jar
    -Dfile=/PATH_TO_DOWNLOADED_JAR/vboxjws-5.2.32.jar
    -Durl=file:/PATH_TO_PLUGIN_SOURCES/virtualbox-plugin/plugin/../lib
    -DrepositoryId=virtualbox-libs


Command used to install vboxws.jar version 6.0.12 to local repository:

mvn deploy:deploy-file
    -DgroupId=org.virtualbox
    -DartifactId=vboxws-60
    -Dversion=6.0.12
    -Dpackaging=jar
    -Dfile=/PATH_TO_DOWNLOADED_JAR/vboxjws-6.0.10.jar
    -Durl=file:/PATH_TO_PLUGIN_SOURCES/virtualbox-plugin/plugin/../lib
    -DrepositoryId=virtualbox-libs


