# Doctane Feed Server

Core interfaces and support code for for the Doctane Feed Server, built on top of the following modules:

* [rest-server-core](https://projects.softwareplumbers.com/document-management/rest-server-core), 
  which provides authentication services.
* [authz-interface-core](https://projects.softwareplumbers.com/document-management/authz-interface-core), 
  which provides common classes and interfaces for interacting with external authorization services.
* [feed-interface-core](https://projects.softwareplumbers.com/document-management/feed-interface-core), 
  which provides common classes and interfaces for interacting with messaging and chat systems

## Architecture

Provides core interfaces and support code for a RESTFul feed server written in Java. The feed server is a standalone java executable, 
implemented using Spring Boot, Jersey, and Jax-RS. The actual back end is a pluggable spring module, for which a default implementation
is provided in the [feed-service-sql](https://projects.softwareplumbers.com/document-management/feed-service-sql) project.

Build support is provided using gradle, and CI via Gitlab's native gitlab-ci.yml

## Feed Services

The REST server for feeds provides feed services under the <tenant>/feed path. Authentication services from rest-server-core are 
found under <auth_tenant>/auth. A feed service enables clients to send a packet of JSON and binary information to a
feed resource. Other clients may listen for a defined period of time to the same resource, during which time they will
immediately receive any information packets sent to that resource.

Although a default feed service is provided, the intention of the rest-server-feeds module is to provide a standard
authenticated and authorized interface to third party real-time messaging services. Software Plumbers is committed to
providing such integrations as separately licensable modules.

## Configuration

The main configuration file for a Doctane feed server is a file services.xml. This file contains several spring bean definitions, 
as described below.

## Feed Service Factory

The Doctane feed server uses one of a number of plug-in modules to connect to a messaging service. The repository module must
be defined as a spring bean and implement the FeedService interface. The FeedServiceFactory maps these
feed service beans to tenants. The below configuration would map a tenant 'tmp' to the bean 'feed.service.tmp'.
Multiple mappings can be specified by adding additional 'prop' elements.

```xml
    <bean id="FeedServiceFactory"
            class="org.springframework.beans.factory.config.ServiceLocatorFactoryBean">
        <property name="serviceLocatorInterface" value="com.softwareplumbers.feed.rest.server.FeedServiceFactory"/>
        <property name="serviceMappings">
            <props>
                <prop key="tmp">feed.service.tmp</prop>
            </props>
        </property>
    </bean>
```

The rest-server-feed package contains only a single implementation of FeedService, SQLRepositoryService, which uses
an embedded H2 database for metadata and local file-based storage for documents. The project repository for this service
is located [here](https://projects.softwareplumbers.com/document-management/feed-service-sql). SQLFeedService is a reference 
implementation against which all standard unit tests are run; it is not strictly intended for production use - although
it should work well enough for small groups. Details for configuring SQLFeedService are included later in this file.

Various implementations of FeedService will be made available by Software Plumbers as separately licensable modules.

## Authorization Components

### Authorization Service Map

The authorization service map maps a set of authorization services to each tenant. Each set of authentication services
must be defined as a spring bean and implement the AuthorizationService interface. The configuration below maps the tenant 
'test' to a set of authorization services implemented by the spring bean 'authz.public'.

```xml
    <bean id="AuthorizationServiceFactory"
        class="org.springframework.beans.factory.config.ServiceLocatorFactoryBean">
    	<property name="serviceLocatorInterface" value="com.softwareplumbers.dms.rest.server.core.AuthorizationServiceFactory"/>
        <property name="serviceMappings">
            <props>
                <prop key="tmp">authz.public</prop>
            </props>
        </property>
    </bean>
```

### Authorization services

The following bean defines a public authorization service which grants access to all authenticated users. For more information
on configuring authorization services, see [authz-interface-core](https://projects.softwareplumbers.com/document-management/authz-interface-core)

```xml
    <bean id="authz.public" class="com.softwareplumbers.dms.rest.server.model.PublicAuthorizationService" scope="singleton"/>
```



## SQLFeedService

The H2 SQLFeedService is the reference implementation of a Doctane FeedService. Sample
configuration is included below. Firstly, we must import the database scripts needed to create and
the database schema and the SQL statements necessary to implement common Doctane operations on the
database.

```xml    
    <import resource="classpath:com/softwareplumbers/feed/service/sql/h2db.xml" />
    <import resource="classpath:com/softwareplumbers/feed/service/sql/entities.xml" />
```  

The standard h2db.xml file should be reasonably compatible with most SQL servers and
can be modified in order to support any SQL dialect. As well as the templated operations
included in the xml configuration above, the SQL service module also generates certain
statements and clauses programatically. This is done in the DocumentDatabase class, which
is configured below:

```xml   
    <bean id="database" class="com.softwareplumbers.feed.service.sql.MessageDatabase">
        <property name="createOption" value="RECREATE"/>
        <property name="operations" ref="feed.operations"/>
        <property name="templates" ref="feed.templates"/>
    </bean>
```

The createOption property above is optional and determines what SQL scripts will be run on 
service startup. Possible values are CREATE, UPDATE, and RECREATE. CREATE will attempt to
create the database schema. UPDATE will attempt to update the schema (although this is not
always possible). RECREATE will drop any existing database objects and recreate the schema
from scratch. If the option is not included, no attempt will be made to modify the schema.

Next we have some standard boilerplate for configuring the database connection:

```xml
	<bean id="datasource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
		<property name="driverClassName" value="org.h2.Driver" />
		<property name="url" value="jdbc:h2:file:/var/tmp/doctane/db" />
		<property name="username" value="sa" />
		<property name="password" value="" />
	</bean> 
```    

Then finally we can create the SQLFeedService bean itself:

```xml 
    <bean id="tmp" class="com.softwareplumbers.feed.service.sql.SQLFeedService" scope="singleton">
        <constructor-arg index="0" ref="database"/>
        <constructor-arg index="1" value="1000000"/>
        <constructor-arg index="2" value="10000"/>
    </bean>
```

The first parameter references the message database bean configured above. The second parameter
defines the combined maximum size in bytes of the feed server's message buffers. The third parameter
defines the initial size of an individual message buffer. This should be greater than the maximum
expected size of any message.
            
         

