.. _usage:


Hooking the API
===============

This piece of code can be used to retrieve and store the api for later use.  Typically you place the this call inside a Wrapper and check the plugin is present before loading the wrapper - this ensures you dont need to shade in Prism's api and cause dramas.

.. code-block:: java

    static void hookPrismApi() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Prism");
        if (plugin != null & plugin.isEnabled()) {
            PrismApi prismApi = (PrismApi) plugin;
        }
    }

Usage of the API
================

.. code-block:: java

        CommandSender sender;
        PrismParameters parameters = this.createParameters();
        parameters.addActionType("block-place");
        parameters.addPlayerName("Narimm");
        final Future<Result> result = this.performLookup(parameters, sender);
        Bukkit.getScheduler().runTaskAsynchronously(instance, new Runnable() {
            @Override
            public void run() {
                try {
                    Result done = result.get(); //Blocks until complete
                    for (me.botsko.prism.api.actions.Handler handler : done.getActionResults()) {
                        ///do something with the handler here. Remember you are Async.
                    }
                } catch (InterruptedException | ExecutionException e) {
                        //handle the exceptions
                }
            }
        });

Importing Prism into your project
-------------------------------------

First, add the repository:

.. tabs::

   .. group-tab:: Maven

      .. code:: xml

         <repositories>
             <!-- ... -->
                <repository>
                    <id>maven.addstar.com.au-snapshots</id>
                    <name>addstar-maven-snapshots</name>
                    <url>https://maven.addstar.com.au/artifactory/all-snapshot</url>
                    <snapshots>
                        <enabled>true</enabled>
                    </snapshots>
                </repository>
                <repository>
                    <id>maven.addstar.com.au</id>
                    <name>addstar-maven-releases</name>
                    <url>https://maven.addstar.com.au/artifactory/all-release</url>
                    <releases>
                        <enabled>true</enabled>
                    </releases>
                </repository>
             <!-- ... -->
         </repositories>

   .. group-tab:: Gradle (Groovy)

      .. code:: groovy

         repositories {
            // for development builds
            maven {
                name = "maven-addstar-snapshot"
                url = "https://maven.addstar.com.au/artifactory/all-snapshot"
            }
            // for releases
            maven {
                name = "maven-addstar-release"
                url = "https://maven.addstar.com.au/artifactory/all-releases"
            }
         }

   Declaring the dependency:

.. tabs::

   .. group-tab:: Maven

      .. code:: xml

        <dependency>
            <groupId>me.botsko</groupId>
            <artifactId>Prism-Api</artifactId>
            <version>2.1.8-SNAPSHOT</version>
            <scope>provided</scope>
        </dependency>

   .. group-tab:: Gradle (Groovy)

      .. code:: groovy

         dependencies {
            provided "me.botsko:Prism-Api:2.1.8-SNAPSHOT"
         }