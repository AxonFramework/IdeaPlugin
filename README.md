Axon Framework IntelliJ Plugin
===================================

This plugin will help you navigate and visualize the [Axon Framework](http://www.axonframework.org/) annotations.

If you have any issues using the plugin, please let us know by [filing a new issue](http://issues.axonframework.org/), send us an [e-mail](mailto:intellijplugin@axonframework.org) or even sending a pull request.


### Build from source

#### Requirements
The plugin is build by maven. At the moment there is no remote repository available. Make sure you have [maven](https://maven.apache.org/) and the [Intellij plugin development with Maven](http://plugins.jetbrains.com/plugin/7127?pr=) installed. 

#### Build
Before you build you should run the setup.sh script once to install the intellij dependencies into your local maven repository.

From the command line: mvn clean install

#### Release
mvn clean release:prepare release:perform -Dmaven.javadoc.skip -Dgoals=install

### Usage
Download and install the plugin manually from the [IntelliJ Plugin Website](http://plugins.jetbrains.com/plugin/7506) or use the Plugin Repository Browser in your IDE.

After installation the plugin will automatically annotate the [Axon Framework](http://www.axonframework.org/) annotations it recognizes. You can click on the icons on the left of the sourcecode to navigate to and from the annotations.

![Axon Trader Application EventHandler Screenshot With Annotations](http://plugins.jetbrains.com/files/7506/screenshot_14623.png)
