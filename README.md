Dynamic Extensions for Alfresco
===============================

Rapid development of Alfresco repository extensions in Java. Deploy your code in seconds, not minutes. Life is too short for endless server restarts.

Example extension
-----------------

TODO.

Getting started
---------------

TODO.

Officially supported Alfresco versions
--------------------------------------

Dynamic Extensions is developed and tested against

* Alfresco Community 3.4
* Alfresco Community 4.0

In practice Dynamic Extensions works just fine on Alfresco Enterprise 4.0 and it will probably work on the recently released Community 4.2 as well.

Supports Alfresco repository only
---------------------------------

Dynamic Extensions is intended for developing extensions to the Alfresco repository only. The repository is Java-based and uses Spring as a core framework.  Extending Alfresco Share is outside the scope of this project, as Share is built on an entirely different framework.

OSGi
----

Dynamic Extensions adds an OSGi container to Alfresco, enabling live deployment of Java code packaged as OSGi bundles. This is where the magic happens. Alfresco itself is not "OSGi"-fied in any way: the OSGi container runs completely separate from the core Alfresco platform.

Basically the OSGi container is responsible for:

* Loading and managing the lifecycle of OSGi bundles.
* Injecting dependencies on Alfresco repository services. I.e. NodeService, CategoryService, etc.
* Providing support for Alfresco-oriented Java annotations. These annotations improve developer productivity by reducing boilerplate code for Web Scripts, Behaviours and Actions.

OSGi usage is kept completely under the hood. Your Alfresco extensions have no OSGi API dependencies.