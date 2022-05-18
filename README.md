### Database

<code>jdbc:postgresql://localhost:5432/quarkus-lra</code>
o commentare <code>quarkus.datasource.jdbc.url</code> 
per l'avvio come dev-service di postgres


### Avvio coordinatore
<code>java -jar lra-coordinator-runner.jar</code>
(porta 8080)

### Avvio ordini 
<code>mvn compile quarkus:dev</code>
(porta 8081)

### Avvio pagamenti
<code>mvn compile quarkus:dev</code>
(porta 8082)

### _TODO_

Provare retry, timeout etc su chiamate ai pagamenti

