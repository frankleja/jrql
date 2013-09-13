jrql
====

jRQL is a Java API for OpenText Website Management Server, formerly known as RedDot CMS. It encapsulate RQL request and response handling and offer high level functions from SmartEdit, SmartTree and ServerManager area, see http://jrql.wordpress.com/


Developing
----------

This is a Maven-based source tree. There is an Eclipse project called *jrql* in this git repository. What you need to work with it:

* A Java 1.6 environment called "JavaSE-1.6" as an "Installed JRE"
* m2e - Maven Integration for Eclipse

Building
--------

On the command line with Maven and Java properly set up:

* mvn compile
* mvn package
* mvn javadoc:javadoc
* mvn clean

