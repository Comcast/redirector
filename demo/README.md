# Redirector Docker demo

In order to quickly start Redirector infrastructure and play with it use this Docker demo. It creates following containers: 

 - key/value store (Zookeeper is the main key/value store and Consul support is work-in-progress),
 - Redirector Gateway (core service which performs redirects)
 - Redirector Web Service (Redirector WS) with Admin UX - used for managing rules and monitoring stats
 
## Prerequisites
 
 - Docker
 - JDK 1.8
 - maven 3
 
## Installation

1) from root module do `mvn clean install` 

2) go to 'demo' module

3) make sure docker is up and running

4) run `mvn docker:build` for Windows 7
  
   If you're using Mac OS run: 
  
         `mvn docker:build -P Docker-MacOS`
     
   for Mac OS (in macOS 10.10 Yosemite and higher)
          
         `mvn docker:build -P Docker-MacOS-HyperV`
           
   If you're using Linux run: 
  
         `mvn docker:build -P Docker-Linux`
         
   If you're using Windows 10 with Hyper-V run:
  
         `mvn docker:build -P Docker-HyperV`
         
5) run `mvn docker:run` for Windows 7
  
   for Mac OS run:
  
         `mvn docker:run -P Docker-MacOS`
     
   for Mac OS (in macOS 10.10 Yosemite and higher)
     
         `mvn docker:run -P Docker-MacOS-HyperV`
         
   for Linux run:
  
         `mvn docker:run -P Docker-Linux`
         
   If you're using Windows 10 with Hyper-V run:
    
         `mvn docker:run -P Docker-HyperV`
         
6) you can use `docker ps` command in your docker client to see that 3 containers are created
   example for Mac OS (in macOS 10.10 Yosemite and higher) OR Linux:
        `docker ps`
        
    | CONTAINER ID | IMAGE | COMMAND | CREATED | STATUS | PORTS |
    |--------------|-------|---------|---------|--------|-------|                     
    | 30154af5f551 | appds/redirector-gateway   | "/bin/sh -c 'java ..." | 16 minutes ago | Up 16 minutes | 0.0.0.0:10601->10601/tcp |
    | 93a4a6ee5892 | appds/redirector-admin-ux  | "/docker-entrypoin..." | 16 minutes ago | Up 16 minutes | 0.0.0.0:10540->8080/tcp |
    | 029665cbc84e | appds/redirector-zookeeper | "/opt/zookeeper/bi..." | 16 minutes ago | Up 16 minutes | 0.0.0.0:2181->2181/tcp |
    
7) go to testing/tools/host-registration/target folder and run command `
java -cp lib/*;host-registration-<REDIRECTOR VERSION HERE>.jar com.comcast.xre.common.redirector.v2.utils.HostRegistration  -connection 192.168.99.100:2181 -stack /BR/BRC2 -flavor 1.67 -app xreGuide -hosts 5 -weight 2 -ip 192.168.201.97`

   for Mac OS (in macOS 10.10 Yosemite and higher) OR Linux
    
        `cd testing/tools/host-registration/target`
        
        `java -cp lib/*:host-registration-<REDIRECTOR VERSION HERE>.jar com.comcast.xre.common.redirector.v2.utils.HostRegistration  -connection localhost:2181 -stack /BR/BRC2 -flavor 1.67 -app xreGuide -hosts 5 -weight 2 -ip 192.168.201.97`

   for Windows 10
   
        `cd testing/tools/host-registration/target`
                
        `java -cp lib/*;host-registration-<REDIRECTOR VERSION HERE>.jar com.comcast.xre.common.redirector.v2.utils.HostRegistration  -connection localhost:2181 -stack /BR/BRC2 -flavor 1.67 -app xreGuide -hosts 5 -weight 2 -ip 192.168.201.97`


8) once hosts are registered open Admin UX in browser (Chrome and Firefox are supported), e.g. http://192.168.99.100:10540/redirectorWebService/admin .

   for Mac OS (in macOS 10.10 Yosemite and higher) OR Linux OR Windows 10
    
        GET http://localhost:10540/redirectorWebService/admin
 
   You should be brought to 'Deployed Applications' page and you should find application you registered in step 6. Once you press 'Start Redirection' and confirm Redirector Gateway is ready to process redirect requests
    
9) perform 'GET http://192.168.99.100:10601/serviceDirect/{Your App Name}' and view json obtained

   for Mac OS (in macOS 10.10 Yosemite and higher) OR Linux OR Windows 10
    
        GET http://localhost:10601/serviceDirect/{Your App Name}

10) To stop containers on Windows 7 use `mvn docker:stop`

   for Mac OS run:
  
         `mvn docker:stop -P Docker-MacOS`
     
   for Mac OS (in macOS 10.10 Yosemite and higher)
     
         `mvn docker:stop -P Docker-MacOS-HyperV`
         
   for Linux run:
  
         `mvn docker:stop -P Docker-Linux`
         
   for Windows 10 run:
      
         `mvn docker:stop -P Docker-HyperV`
