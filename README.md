# Integrations Plugin for Graylog

**Required Graylog version:** 2.5 and later

Content
-------

This plugin includes the following features:
 * [Palo Alto input](http://docs.graylog.org/en/2.5/pages/integrations.html#palo-alto-networks-input-tcp)

Installation
------------

See the [Releases](https://github.com/Graylog2/graylog-plugin-integrations/releases) page to access the latest versions 
of the plugin. A typical plugin JAR and Debian/RPM packages are available for download. Public URLs are also included 
for each for remote installs. Please make sure to not publicly distribute the download links. 

The installation process is the same as any other plugin: Place the `.jar` file in your Graylog plugin directory. 
See the [docs](http://docs.graylog.org/en/2.5/pages/plugins.html#installing-and-loading-plugins) for more info.  

Releases
--------

This plugin will be released approximately once per quarter (and timed with `graylog-server` when possible). Each build of the 
plugin will target a specific (and usually current) `graylog-server` version. 

Versioning
----------

The Integrations plugin version includes both the `graylog-server` version and the `integrations`
release number in the following format:

`<graylog-server-version>+<integrations-release-number>`

eg. `2.5.0+0.jar` for the 2.5.0 server release and the initial Integrations release. The 
Integrations release number is an `integer` that starts at `0` and increments until the `graylog-server` 
major version is incremented. 

The plugin `.jar` file follows the same format: 

`graylog-plugin-integrations-<graylog-server-version>+<integrations-release-number>.jar`  

eg. `graylog-plugin-integrations-2.5.0+0.jar` for the 2.5.0 `graylog-server` version and the initial plugin release.
  
**Why a + symbol between the server and integrations versions?** Due to the versioning 
library that we're using in Java, the version number needs to follow a designated format: `major`.`minor`.`patch`+`build-metadata`. 
The `build-metadata` field is the only place where we could stuff another version.
 
The format of this version scheme is perhaps not ideal, but it should allow us to easily identify which version of the 
plugin works with which `graylog-server` version. 