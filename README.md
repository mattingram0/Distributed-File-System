# Distributed File System
A file back-up system that distributes files accross three servers, providing fault tolerance, using Java RMI

## Prerequisites
To run the distributed system locally on your machine, the latest version of **Java** needs to be installed.

## Installation
Click 'Clone or download' above and then 'Download ZIP', or alternatively run the following command from the command line:

```
git clone https://github.com/mattingram0/Distributed-File-System.git
```

## Running
/out/ contains the compiled files. All other files in the root directory hold the source code.

* Sockets use ports 9090 - 9093 (inclusive). From here on, [PATH] is the path to the /Distributed-File-System/ directory. **Please ensure you REPLACE any spaces in the path with %20**: /Example Path/ -> /Example%20Path/
Please then note the following:
  * Programs must be ran from directories specified to ensure relative filepaths work
  * Please run all the programs on the same machine, as socket connections are hardcoded to use 127.0.0.1
  * [registry_host] should likely be 127.0.0.1, [registry_port] any available port

* To run:
  1. To start the registry from any location, run: 
```
rmiregistry -J-Djava.rmi.server.codebase="file:/[PATH]/out/FrontEnd/FrontEnd.jar" [registry_port] &
```
  2. To start the client, navigate to /[PATH]/out/Client/files/, run:
```
java -jar -Djava.security.policy=client.policy -Djava.rmi.server.codebase="file:/[PATH]/out/FrontEnd/FrontEnd.jar" ../Client.jar [registry_host] [registry_port]
```
  3. To start the front end, navigate to /[PATH]/out/FrontEnd/, run:
```
:~ java -jar -Djava.security.policy=server.policy -Djava.rmi.server.codebase="file:/[PATH]/out/FrontEnd/FrontEnd.jar" FrontEnd.jar [registry_host] [registry_port]
```
  4. To start the three servers, navigate to /[PATH]/out/Server[1,2,3]/, run:
```
java -jar -Djava.security.policy=server.policy -Djava.rmi.server.codebase="file:/[PATH]/out/FrontEnd/FrontEnd.jar" Server[1,2,3].jar [registry_host] [registry_port]
```

* I compiled this program using IntelliJ. If you need to recompile, please load the source code into IntelliJ as a project, and rebuild the artefacts.
