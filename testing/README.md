# Redirector Testing Framework

 - end-to-end module - End-To-End testing framework, assumes Redirector WS and Redirector Gateway are deployed and End2End tool creates rules through Redirector WS and then 
performs requests to Redirector Gateway to retrieve redirects. Used as smoke test of new deployment
 - tools - contains helpful utilities such as host-registration tools for simulating of registration of deployed apps
 - ux-automation - Automated acceptance tests of Redirector WS Admin UX. Tests are run on Desktop/VM with GUI. Require Firefox with Selenium support