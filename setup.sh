#!/bin/sh
export IDEA="/C/Program Files (x86)/JetBrains/IntelliJ IDEA 14.1"
if [ ! -e "$IDEA" ]
then
    export IDEA="/Applications/IntelliJ IDEA 14.app/Contents"
fi
if [ ! -e "$IDEA" ]
then
    export IDEA="/Applications/IntelliJ IDEA CE.app/Contents"
fi
if [ ! -e "$IDEA" ]
then
    echo "IDEA libraries not found!"
    echo "Checked: /C/Program Files (x86)/JetBrains/IntelliJ IDEA 14.1"
    echo "Checked: /Applications/IntelliJ IDEA 14.app/Contents"
    echo "Checked: /Applications/IntelliJ IDEA CE.app/Contents"
    echo ""
    echo "Try installing the IDEA Community Edition"
    exit 1
fi

export IDEA_VERSION=LOCAL
mvn install:install-file -Dfile="$IDEA/lib/annotations.jar" -DgroupId=com.intellij.idea -DartifactId=annotations -Dversion=$IDEA_VERSION -Dpackaging=jar
mvn install:install-file -Dfile="$IDEA/lib/idea.jar" -DgroupId=com.intellij.idea -DartifactId=idea -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true -Dversion=$IDEA_VERSION
mvn install:install-file -Dfile="$IDEA/lib/openapi.jar" -DgroupId=com.intellij.idea -DartifactId=openapi -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true -Dversion=$IDEA_VERSION
mvn install:install-file -Dfile="$IDEA/lib/util.jar" -DgroupId=com.intellij.idea -DartifactId=util -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true -Dversion=$IDEA_VERSION
mvn install:install-file -Dfile="$IDEA/lib/extensions.jar" -DgroupId=com.intellij.idea -DartifactId=extensions -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true -Dversion=$IDEA_VERSION

git update-index --assume-unchanged META-INF/plugin.xml