# Integrations Plugin for Graylog

[![Build Status](https://travis-ci.org/Graylog2/graylog-plugin-estreamer.svg?branch=master)](https://travis-ci.org/Graylog2/graylog-plugin-estreamer)

**This repository should be kept private for now**

This plugin is the central collection point for integrations-related content. Integrations will be implemented in this
plugin to allow release of them independent of the Graylog Server project. The initial idea is to implement all 
integrations content in a single plugin repository, which should keep things simple and allow for easier reworking 
and maintenance of existing integrations.

This Readme should be updated to describe new plugins as they are implemented.

While this repository is in the early stages, all commits will be made to master. Once real implementation of 
integrations begins, commits should be made to new branches with a PR.

**Required Graylog version:** 2.0 and later

Installation
------------

[Download the plugin](https://github.com/Graylog2/graylog-plugin-estreamer/releases)
and place the `.jar` file in your Graylog plugin directory. The plugin directory
is the `plugins/` folder relative from your `graylog-server` directory by default
and can be configured in your `graylog.conf` file.

Restart `graylog-server` and you are done.

Development
-----------

You can improve your development experience for the web interface part of your plugin
dramatically by making use of hot reloading. To do this, do the following:

* `git clone https://github.com/Graylog2/graylog2-server.git`
* `cd graylog2-server/graylog2-web-interface`
* `ln -s $YOURPLUGIN plugin/`
* `npm install && npm start`

Usage
-----

__TODO: Add more details as individual integrations are implemented.__


Getting started
---------------

This project is using Maven 3 and requires Java 8 or higher.

* Clone this repository.
* Run `mvn package` to build a JAR file.
* Optional: Run `mvn jdeb:jdeb` and `mvn rpm:rpm` to create a DEB and RPM package respectively.
* Copy generated JAR file in target directory to your Graylog plugin directory.
* Restart the Graylog.

Plugin Release
--------------

We are using the maven release plugin:

```
$ mvn release:prepare
[...]
$ mvn release:perform
```

This sets the version numbers, creates a tag and pushes to GitHub. Travis CI will build the release artifacts and upload to GitHub automatically.
