Command used to install vboxws.jar version 4.1.8 to local repository:

mvn deploy:deploy-file
    -DgroupId=org.virtualbox
    -DartifactId=vboxws-41
    -Dversion=4.1.8
    -Dpackaging=jar
    -Dfile=/PATH_TO_DOWNLOADED_JAR/vboxjws-4.1.8.jar
    -Durl=file:/PATH_TO_PLUGIN_SOURCES/virtualbox-plugin/plugin/../lib
    -DrepositoryId=virtualbox-libs


Command used to install vboxws.jar version 4.2.0 to local repository:

mvn deploy:deploy-file
    -DgroupId=org.virtualbox
    -DartifactId=vboxws-42
    -Dversion=4.2.0
    -Dpackaging=jar
    -Dfile=/PATH_TO_DOWNLOADED_JAR/vboxjws-4.2.0.jar
    -Durl=file:/PATH_TO_PLUGIN_SOURCES/virtualbox-plugin/plugin/../lib
    -DrepositoryId=virtualbox-libs

Command used to install vboxws.jar version 4.3.0 to local repository:

mvn deploy:deploy-file
    -DgroupId=org.virtualbox
    -DartifactId=vboxws-43
    -Dversion=4.3.0
    -Dpackaging=jar
    -Dfile=/PATH_TO_DOWNLOADED_JAR/vboxjws-4.3.0.jar
    -Durl=file:/PATH_TO_PLUGIN_SOURCES/virtualbox-plugin/plugin/../lib
    -DrepositoryId=virtualbox-libs


Command used to install vboxws.jar version 5.0.4 to local repository:

mvn deploy:deploy-file
    -DgroupId=org.virtualbox
    -DartifactId=vboxws-50
    -Dversion=5.0.4
    -Dpackaging=jar
    -Dfile=/PATH_TO_VBOX_SDK/bindings/webservice/java/jax-ws/vboxjws.jar
    -Durl=file:/PATH_TO_PLUGIN_SOURCES/virtualbox-plugin/plugin/../lib
    -DrepositoryId=virtualbox-libs
