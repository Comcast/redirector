![Redirector Logo](redirector-logo-light.png)

# Redirector

The Redirector project is a collection of tools that facilitate redirection of incoming connections to a set of trusted backend servers based on a set of rules that operate on parameters in the incoming request.  Redirector can assist with load-balancing across the backend servers and traffic-shaping to two versions of an application.  Redirector is typically used to create "sticky" sessions to the backend servers, however, it could be called for each request to a service.

The current version of the project supports HTTP redirects, or the ability to specify the target backend in the body of the response.  The frameworks are designed to be flexible/extensible and support any non-HTTP protocol (such as WebSockets) as well.  The core premise of the tool is that there are stacks or groups of backend servers running a particular version of software and are validated and ready to take traffic.  Groups of backend servers may be whitelisted to be able to handle any incoming request in a round-robin fashion.  The tools are designed to be able to function during any systemic outages when the tools themselves may not be in a stable state.

The core components of the Redirector framework are **Redirector Gateway** and **Redirector Web Service**. The Redirector Gateway receives requests from clients and returns the server to which the client should connect.  The Redirector Web Service is used to manage the stacks of backend servers and the rules that direct traffic to the stacks.

## Redirector Gateway

Redirector Gateway (Redirector GW) receives requests and returns the IP addresses of backend servers according to the model.  Redirector GW uses a **distributed key/value store (Zookeeper)** as the **source of truth** for backend servers and **Redirector WS** for the rules.  If either Zookeeper or Redirector WS is temporarily down, Redirector GW uses a local file system backup of the **Last Known Good** service discovery and rule engine data.

### Redirector Model

The Redirector model contains various information about an application that allows Redirector GW to choose the correct backend server.  There is a separate model for each application.  Redirector WS notifies Redirector GW of a model update through Zookeeper.  Redirector GW will obtain the latest model and save it in a local filesystem backup.  The local backup is a Last Known Good that can be used if the connection to Zookeeper is down when Redirector GW is restarted.

The Redirector model consists of the following items.
- Default Server - This specifies the default version of the application.  If other rules do not apply, this is where clients are sent.
- Traffic Distribution Rules - This rule specifies the percentage of traffic that will be redirected to a particular version of application.  This is useful for traffic-shaping to a new version.
- Flavor (Version) Rules - This rule specifies conditions where a client would be redirected to a specific version or a final URL.  The conditions are input parameters passed as http headers or query parameters.
- URL Rules - This rule defines concrete values for a template, such as protocol, port, url path, or IP protocol version.  URL rules work with Flavor rules, which define a template of the URL.
- Whitelisted Stacks - Redirector GW can only send traffic to backend servers that are whitelisted.
- NamespacedList - This is a value list (e.g. list of mac addresses, ip addresses, or service account ids) that is used by Flavor and URL rules.

### Redirector Service Discovery

Redirector GW uses service discovery to find the IP addresses of the backend servers in each stack.  There are two approaches to service discovery--static and dynamic.  With static service discovery, Redirector GW takes a snapshot of all service discovery information from Zookeeper when a Model Update is initiated by the user.  Redirector GW keeps the service discovery information in memory and uses until the next Model Update.  With dynamic service discovery, Redirector GW learns of a new server once it registers with Zookeeper.  This server is then available to take traffic.

Application service hosts or stacks register to zookeeper exhibiting their availability. They can publish their attributes such as IPv4 andIPv6 support. During the registration service hosts or stack can publish individual weight from 1 to 100 number to distribute more or less traffic from one another. Redirector GW sets default weight to 10 to balance traffic across all hosts.

```
// Sample payload for registration:

{"name”:"app1","id”:” cldapp-p5-kn1jnv.example.app.com:0","address":"cldapp-p5-kn1jnv.example.app.com","port":0,"sslPort":null,"payload":{"@class":"com.org.example.cloud.MetaData","workerId":"73ffc9a8-c392-4252-bfb6-62b1f6c8515b","listenAddress":"cldapp-p5-kn1jnv.example.app.com","listenPort":0,"serviceName":"app1","parameters":{"ipv6Address":"cldapp-p5-kn1jnv.example.app.com","ipv4Address":"cldapp-p5-kn1jnv.example.app.com","weight":"10.0"}},"registrationTimeUTC":1496432312046,"serviceType":"DYNAMIC","uriSpec":null}
```

## Redirector Web Service

Redirector Web Service (Redirector WS) is used to manage the stacks and rules through an **Admin UX**.  Stacks can be whitelisted in Redirector WS so that they can take traffic.  Rules can be created to send traffic to stacks based on input parameters or distribution percentages.  Redirector WS saves the model in Zookeeper so that Redirector GW can access it. 

### Model approval process in Redirector Web Service

Any changes to the model are saved in a pending changes state.  This allows one person to make the change and another person to approve the change.  Once the change is approved, the new model is saved in Zookeeper, and the Redirector GW servers are notified.

### Redirector Admin UX Offline mode

In order to let administrators modify the model when there is no connection to the Zookeeper cluster, the Admin UX works in an Offline Mode and keeps all information in a browser index DB.  Administrators can download the Redirector Core backup and scp it to Redirector GW servers.  After the Redirector GW servers are restarted, the model produced by Offline Mode will be applied as the Last Known Good and will be used until the Zookeeper cluster is restored.
