# Redirector

The Redirector project is a collection of tools that facilitate redirection of incoming connections to a set of trusted backend servers based on a set of rules that operate on parameters in the incoming request. The current version of the project supports HTTP redirects, or the ability to specify the target backend in the body of the response. The frameworks are designed to be flexible/extensible and support any non-HTTP protocol (such as WebSockets) as well. The core premise of the tool is that there are stacks or groups of backend servers running a particular version of software and are validated and ready to take traffic. Groups are backend servers may be whitelisted to be able to handle any incoming request in a round-robin fashion. The tools are designed to be able to function during any systemic outages when the tools themselves may not be in a stable state.

# Redirector Framework

The Redirector framework is a dynamic content-based gateway tool. The Redirector uses service-discovery in combination with a rules engine to define the particular version of a given application that the client needs to be redirected to and returns an IP address of particular application.

## Redirector Gateway

The core component of Redirector system is **Redirector Gateway**. It receives requests from clients and returns information about a particular host that the client should connect to. Redirector Gateway uses **distributed key/value store (Zookeeper)** as **source of truth** for service discovery and **Redirector WS** for rule engine facilities. In situations when the source of thruth is down Redirector Gateway uses its local file system copy of **Last Known Good** service discovery and rule engine data.

### Redirector Model

The Redirector Gateway processes rules based on the following model:

- Default Server (specifies default version of application which we redirect to)
- Traffic Distribution Rules (specifies how many %% of traffic will be redirected to particular version of application)
- Flavor (Version) Rules. Match particular context (http headers or other information coming from client device) to version of application or 
 stack with version deployed or to final URL.
- URL Rules. Flavor Rules return template of url and then URL Rules define concrete values for template: protocol, port, url path, IP protocol version (4/6)
- Whitelist. Gateway discovers application instances only from stacks included into Whitelist
- NamespacedLists - value lists, e.g. list of mac addresses or ip addresses or service account ids which are used by Flavor and URL rules

The Redirector Gateway considers all these constituents as a single aggregate called Redirector Model. Redirector Gateway uses separate model for each particular application.
The Redirector Gateway updates the models on notification sent from Redirector Web Service through Zookeeper. When the Redirector Gateway receives a notification it pulls the Model and uses it until the next notification. 
Every time the model is updated, it is also persisted in the local filesystem backup. Even if the Redirector Gateway is restarted, the persistant Last Known Good model facilitates the functioning of the Redirector Gateway with reasonable expectations even when the sources of truth may not be available to load the most recent models.

### Redirector Service Discovery

The Redirector Gateway is designed to return IP address for any particular version of the application based on the rules using service discovery pattern. There are 2 modes currently supported for service discovery. One is static mode, and the other is dynamic mode.


In Static service discovery more, during model update the Redirector Gateway takes a snapshot of all service discovery data from zookeeper, keeps it in-memory and then uses it until next model update.
In Dynamic service discovery mode, as long as the host is registered in zookeeper it is available to be returned back by the Redirector Gateway.If the host is not registered in zookeeper, it will not be returned back as a potential target host.

## Redirector Web Service

The Rules are  managed by **Redirector Web Service** (Redirector WS) through an **Admin UX**. 
Redirector WS saves rules in Zookeeper and then Redirector Gateway loads new rules from there. 

### Model approval process in Redirector Web Service

When a rule or whitelist is added or modified it is kept in pending changes. This allows an Administrator to verify the changes and then  either approve the changes or reject any of them. Upon approval, the new Model is saved in Zookeeper and Redirector Gateway nodes are notified about it.

### Redirector Admin UX Offline mode

In order to support administrators to be able to modify model when there is loss in connectivity to Zookeeper cluster, the Admin UX works in Offline Mode and keeps all information in browser index DB. The Administrator can download Redirector Core backup and push it to Redirector Gateway servers (typically via scripts) and then restart Redirector Gateway. The model produced by offline mode will be applied as Last Known Good and will be used until connectivity to the Zookeeper Cluster is restored.
