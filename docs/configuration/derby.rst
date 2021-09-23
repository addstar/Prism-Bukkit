.. _derby:

==========================
Apache Derby Database
==========================

Prism now supports the on disk database known as Apache Derby.  In fact it is the default configuration.

Apache Derby, an Apache DB subproject, is an open source relational database implemented entirely in Java and available under the Apache License, Version 2.0. Some key advantages include:

1. Derby has a small footprint -- about 3.5 megabytes for the base engine and embedded JDBC driver.
2. Derby is based on the Java, JDBC, and SQL standards.
3. Derby provides an embedded JDBC driver that lets you embed Derby in any Java-based solution.
4. Derby also supports the more familiar client/server mode with the Derby Network Client JDBC driver and Derby Network Server.
5. Derby is easy to install, deploy, and use.

Prism currently embeds Apache Derby 10.14.2

.. code-block::yaml

datasource:
  type: derby
  properties:
    database-name: derby
    user-name: PRISM
    password: PRISM
    prefix: prism_

This is the default derby configuration found in config.yml in the plugin directory.  The derby database will be located in a directory called 'derby' in the plugin directory.  You can tweak the `hikari.properties` to adjust the way the database is accessed.
