# Redirector Framework

Redirector framework is a dynamic content-based gateway tool. Redirector is using service-discovery facility to find concrete IP address of particular application.
Redirector is using rule engine in order to define which version of given application it should redirect to. 

## Redirector Gateway

Core component of Redirector system is **Redirector Gateway**. It receives requests from clients and return information 
about concrete host client should connect to. Redirector Gateway uses **distributed key/value store (Zookeeper)** as **source of truth** for 
service discovery and **Redirector WS** for rule engine facilities. In case when the source of thruth is down Redirector Gateway is using local file system backup of 
**Last Known Good** service discovery and rule engine data.

### Redirector Model

In order to let Redirector Gateway make right decisions it needs following constituents:

- Default Server (specifies default version of application which we redirect to)
- Traffic Distribution Rules (specifies how many %% of traffic will be redirected to particular version of application)
- Flavor (Version) Rules. Match particular context (http headers or other information coming from client device) to version of application or 
 stack with version deployed or to final URL.
- URL Rules. Flavor Rules return template of url and then URL Rules define concrete values for template: protocol, port, url path, IP protocol version (4/6)
- Whitelist. Gateway discovers application instances only from stacks included into Whitelist
- NamespacedLists - value lists, e.g. list of mac addresses or ip addresses or service account ids which are used by Flavor and URL rules

Redirector Gateway considers all these constituents as a single aggregate named Redirector Model. Redirector Gateway has separate model for each particular application.
Redirector Gateway updates model on notification sent from Redirector Web Service through Zookeeper. Once Redirector Gateway receives notification it pulls the Model and uses it until next notification comes. 
On each update model is being saved in local filesystem backup so it could be used as Last Known Good once Redirector Gateway is restarted.

### Redirector Service Discovery

In order to make Redirector Gateway find IP address for version of application returned by Flavor Rules service discovery pattern is used. 
Redirector Core uses 2 approaches of service discovery: static and dynamic.


Static service discovery means that on Model update Redirector takes snapshot of all service discovery data from zookeeper, keeps it in-memory and uses until next model update.
Dynamic service discovery means that once host is registered in zookeeper it becomes available to Redirector Gateway.

## Redirector Web Service

Rules are being managed by **Redirector Web Service** (Redirector WS) mainly through **Admin UX**. 
Redirector WS is saving rules in Zookeeper and then Redirector Gateway pulls new rules from there. 

### Model approval process in Redirector Web Service

Once rule or whitelist is added or modified it's put into pending changes. After that Admin should approve the change and on approval new Model is saved in Zookeeper and Redirector Gateway nodes are notified about it.

### Redirector Admin UX Offline mode

In order to let admin modify model when there is no connection to Zookeeper cluster Admin UX works in Offline Mode and keeps all information in
browser index DB. Admin can download Redirector Core backup and scp it to Redirector Gateway servers and then restart Redirector Gateway, model produced by offline mode will be applied as Last Known Good and will be used until
Zookeeper Cluster is restored.
