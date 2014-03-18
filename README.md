coldswap
========

A java agent for JVM that will reload a Java class file immediately after it is changed.
Currently **coldswap** will reload any change like:

* method body redefinition
* new public static attribute
* new private static attribute
* new protected static attribute
* new public {ret_type} {method_name}(Object[] {arg_name})

    <i>Support for adding new methods that have this format: 
    <code>public {ret_type} {method_name}(Object[] {arg_name})</code>. The number of methods that have this format 
    and that could be replaced varies from 0 to 25, depending on the agent option with max 10 
    being the default if user hasn't specified anything.</i>
* new public {ret_type} {method_name}(int {arg_name})

    <i>Support for adding new methods that have this format:   
    <code>public {ret_type} {method_name}(int {arg_name})</code>. The number of methods that have this format   
    and that could be replaced varies from 0 to 25, depending on the agent option with max 10   
    being the default if user hasn't specified anything.</i>  
* new public {ret_type} {method_name}(float {arg_name})  

    <i>Support for adding new methods that have this format:     
    <code>public {any_ret_type} {any_method_name}(float {any arg name})</code>. The number of methods that have this format     
    and that could be replaced varies from 0 to 25, depending on the agent option with max 10     
    being the default if user hasn't specified anything.</i>  
* new public {ret_type} {method_name}(String {arg_name})  

    <i>Support for adding new methods that have this format:     
    <code>public {ret_type} {method_name}(String {arg_name})</code>. The number of methods that have this format     
    and that could be replaced varies from 0 to 25, depending on the agent option with max 10     
    being the default if user hasn't specified anything.</i>  
* new public {ret_type} {method_name}(long {arg_name})  

    <i>Support for adding new methods that have this format:     
    <code>public {ret_type} {method_name}(long {arg_name})</code>. The number of methods that have this format     
    and that could be replaced varies from 0 to 25, depending on the agent option with max 10     
    being the default if user hasn't specified anything.</i>                


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

<code> Please notice that for now you need mvn version 3.0.5 or BELOW because the goldin plugin does not support mvn
 version starting with 3.1.0</code>

How to use **coldswap**
-----------------------

Add this option to your JVM options when you start the application:
        <code>-javaagent:coldswap-{version}-jar-with-dependencies.jar=cp={file path},recursive={true/false},maxNumberOfMethods={0< X <= 25}</code>
        
      
  <b>cp</b>                     a folder file path where classes that you want to reload resides.  
  <b>recursive</b>              true if you want to monitor the folder from <b>cp</b> recursively, false otherwise.  
  <b>maxNumberOfMethods</b>     the maximum number of new methods of certain type that could be added per class.  
  This number should be between 0 and 25, 10 being the default one if this parameter is not specified.  


Licence
-------
Please take a look at **LICENCE** file

Contacts
--------

For any problems, questions, advices regarding coldswap please contact me at fioan89@gmail.com  
Bug reporting goes to https://github.com/fioan89/coldswap/issues


