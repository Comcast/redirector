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

## Installation

1. from root module do `mvn clean install` 
2. go to 'demo' module
3. run `mvn docker:build` for Windows
  1. If you're using Mac OS run: 
  
         `mvn docker:build -P Docker-MacOS`
           
  2. If you're using Linux run: 
  
         `mvn docker:build -P Docker-Linux`
         
4. run `mvn docker:run` for Windows
  1. for Mac OS run:
  
         `mvn docker:run -P Docker-MacOS`
         
  1. for Linux run:
  
         `mvn docker:run -P Docker-Linux`
         
5. you can use `docker ps` command in your docker client to see that 3 containers are created
6. use [Discovery Client Registration utility](https://github.com/Comcast/discovery/blob/develop/discovery-client/src/main/java/com/comcast/tvx/cloud/RegistrationMain.java) 
or [Redirector Host Registration Utility](https://github.comcast.com/appds-services/redirector-2.0/blob/next-sprint/testing/tools/host-registration/src/main/java/com/comcast/xre/common/redirector/v2/utils/HostRegistration.java) to register the hosts. 
Use Docker Machine IP address (192.168.99.100 on windows by default) instead of localhost in ZooKeeper connection string. In order to use Client Registration you need to modify discovery client project to build jar or tar.gz with dependencies. We made that work for you in *testing/tools/host-registration* module. So simply go to testing/tools/host-registration/target folder and run command `
java -cp lib/*;host-registration-<REDIRECTOR VERSION HERE>.jar com.comcast.xre.common.redirector.v2.utils.HostRegistration  -connection 192.168.99.100:2181 -stack /BR/BRC2 -flavor 1.67 -app xreGuide -hosts 5 -weight 2 -ip 192.168.201.97`
7. once hosts are registered open Admin UX in browser (Chrome and Firefox are supported), e.g. http://192.168.99.100:10540/redirectorWebService/admin . 
You should be brought to 'Deployed Applications' page and you should find application you registered in step 6. Once you press 'Start Redirection' and confirm Redirector Gateway is ready to process redirect requests
8. perform 'GET http://192.168.99.100:10601/serviceDirect/{Your App Name}' and view json obtained
9. To stop containers use `mvn docker:stop`
