Axon Framework IntelliJ Plugin
===================================

This plugin will help you navigate and visualize the [Axon Framework](http://www.axonframework.org/) annotations. You
can click through to handlers of messages such as commands, queries and events.

If you have any issues using the plugin, please let us know
by [filing a new issue](https://github.com/AxonFramework/IdeaPlugin/issues) or even sending a pull request.

## Usage

Download and install the plugin manually from the [IntelliJ Plugin Website](http://plugins.jetbrains.com/plugin/7506) or
use the Plugin Repository Browser in your IDE.

After installation the plugin will automatically annotate the [Axon Framework](http://www.axonframework.org/)
annotations it recognizes. You can click on the icons on the left of the sourcecode to navigate to and from the
annotations.

![Axon Hotel Demo Screenshot With Annotations](.github/screenshot.png)

## Building

You can run an IntelliJ instance with the plugin locally with `./gradlew runIde`. This will bootstrap a clean instance
and load the plugin.

While the instance is running, you can run `./gradlew buildPlugin` for a hot reload of the plugin. 
