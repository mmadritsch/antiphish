# AntiPhish

AntiPhish is a client-server system for detecting phishing websites. The architecture consists of a client-side 
browser extension for Mozilla Firefox and a server-side RESTful web service implemented using Dropwizard.

When a website is called by the browser, the extension sends the requested URL to the server and triggers the 
analysis process. It currently consists of a total of four superordinate analysis steps and evaluates the website 
belonging to the transmitted URL on the basis of certain heuristics. The calculated overall result is assigned to 
a green, yellow or red traffic light symbol, which is then visually displayed to the user in the browser as a 
measure of the outgoing phishing threat. The results of the evaluation of the system showed an accuracy of 84.17%, 
with a sensitivity of 78.13% and a specificity of 90.22%.

### Installation Instructions

In order to run the system, both the client and the server application must be started.

## Client - Mozilla Firefox browser extension

The browser plugin can be loaded as a temporary add-on. Therefore open "about:debugging" in Firefox, click 
"Load Temporary Add-on" and select any file in the extension's [directory](client).

## Server - Dropwizard RESTful web service

The Dropwizard service is built as a “fat” .jar file (contains all of the .class files required to run the service) 
using Maven. This file can be found in the folder [target](server/antiphish/target) and can be built and started as follows:

1. Run `mvn clean install` to build the application
1. Start application with `java -jar target/antiphish-0.0.1-SNAPSHOT.jar server config.yml`
1. To check that the application is running enter url `http://localhost:8080`

```
java -jar target/antiphish-0.0.1-SNAPSHOT.jar server config.yml
``` 

### Documentation

AntiPhish was implemented in the course of a master thesis at the Alpen-Adria University Klagenfurt. The [thesis](docs/2018-01-26_Masterarbeit_Madritsch.pdf) can 
be found in the folder [docs](docs) (chapter 4-7, German only).

### Contributions

* **Marco Madritsch** - *Initial work*
* **[Marcus Hassler](https://hassler.world/)** - *Supervision*
* **Peter Schartner** - *Supervision*
