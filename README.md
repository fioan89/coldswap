coldswap
========

A java agent for JVM that will reload a Java class file immediately after it is changed.
Currently **coldswap** will reload any change like:

* method body redefinition
* new public static attribute

You don't need to run you java application in debug mode. You can run it normally with or without the
aid of a powerful IDE like Eclipse or IntelliJ IDEA.

How to install **coldswap**
----------------

* Linux
  1. Run in a terminal:

     <code>sh install-notify.sh</code>  
     This command installs libjnotify on your box.
  2. In a terminal:  
     <code>mvn package</code>


How to use **coldswap**
-----------------------

Add this option to your JVM options when you start the application:
        <code>-javaagent:coldswap-{version}-jar-with-dependencies.jar=cp={path to folder you wish to watch for  
        changes},recursive={true if you want to watch recursively, false otherwise} </code>


Licence
-------
Please take a look at **LICENCE** file

Contacts
--------

For any problems, questions, advices regarding coldswap please contact me at fioan89@gmail.com  
Bug reporting goes to https://github.com/fioan89/coldswap/issues


