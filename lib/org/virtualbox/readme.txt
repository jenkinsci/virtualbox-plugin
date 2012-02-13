Command used to install vboxws.jar version 4.1.8 to local repository:

mvn deploy:deploy-file
    -DgroupId=org.virtualbox
    -DartifactId=vboxws-41
    -Dversion=4.1.8
    -Dpackaging=jar
    -Dfile=/PATH_TO_DOWNLOADED_JAR/vboxjws-4.1.8.jar
    -Durl=file:/PATH_TO_PLUGIN_SOURCES/virtualbox-plugin/plugin/../lib
    -DrepositoryId=virtualbox-libs
