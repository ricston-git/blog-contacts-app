blog-contacts-app
=================

## To Jar hell and back

In this blog post we'll be using [JHades](http://jhades.org/) to troubleshoot problems in our classpath. JHades is a powerful tool that can give us useful information when trying to resolve a [certain set of problems](http://blog.jhades.org/classnotfoundexception-jhades-jar-hell-made-easy/) in Java development.

The demo behind this blog post builds on the following: [RAML code generation](http://ricston.com/blog/raml-code-generation/)

It has nothing to do with RAML per se. I'll just be using and continuing from the [demo](https://github.com/ricston-git/blog-contacts-app) introduced in that blog post (note that each post's "resulting" project state is in its own branch).

Ok, so before we can get to the meat of things, we need a problem to troubleshoot.

Go ahead and `git clone https://github.com/ricston-git/blog-contacts-app && cd blog-contacts-app && git checkout raml-code-generation` (this is the demo behind the previous post). Now, cd into contacts-app and `mvn install`. Cd into contacts-app-impl and `mvn exec:java -Dexec.mainClass="com.ricston.contacts.Main"`. If your JAVA_HOME env variable is pointing to a Java 6 installation you'll get: Unsupported major.minor version 51.0. This is because I upgraded Jersey to 2.13 - so you'll want to point it to your Java 7 home directory. You should then see `Server listening on port 4433` - indicating that our server is running and we can hit it with requests as defined in our RAML file.

So... no trouble - app works fine. That's because you're using the fixed version :)

What you'll want to do to follow along is to open up contacts-app-api's pom.xml and remove the scope of provided on the raml-jaxrs-codegen-core dependency. That (and a couple of other unnecessary things) was the state of the project when I started working on the demo code behind the blog post which I was going to write originally (then I ended up writing this one). What actually happened was that I was able to continue working without facing this problem for quite a while because I was running the program from within my IDE. I had set up another Maven module with war packaging, transferred the configuration from the Main.java over to a web.xml deployment descriptor and fired things up with no problem in my IDE. It was only when I tried running the project from Maven using the Tomcat plugin - `mvn clean tomcat7:run` that I ran into the following exception. This exception is the same one you'll get at this point if you mvn install everything again and run as you just did (only this time you should have the default scope of compile on the raml-jaxrs-codegen-core dependency):

```
java.lang.reflect.InvocationTargetException
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:606)
	at org.codehaus.mojo.exec.ExecJavaMojo$1.run(ExecJavaMojo.java:297)
	at java.lang.Thread.run(Thread.java:745)
Caused by: java.lang.NoSuchMethodError: javax.ws.rs.core.Application.getProperties()Ljava/util/Map;
	at org.glassfish.jersey.server.ApplicationHandler.<init>(ApplicationHandler.java:304)
	at org.glassfish.jersey.server.ApplicationHandler.<init>(ApplicationHandler.java:285)
	at org.glassfish.jersey.servlet.WebComponent.<init>(WebComponent.java:311)
	at org.glassfish.jersey.servlet.ServletContainer.init(ServletContainer.java:170)
	at org.glassfish.jersey.servlet.ServletContainer.init(ServletContainer.java:358)
	at javax.servlet.GenericServlet.init(GenericServlet.java:244)
	at org.eclipse.jetty.servlet.ServletHolder.initServlet(ServletHolder.java:532)
	at org.eclipse.jetty.servlet.ServletHolder.doStart(ServletHolder.java:344)
	at org.eclipse.jetty.util.component.AbstractLifeCycle.start(AbstractLifeCycle.java:64)
	at org.eclipse.jetty.servlet.ServletHandler.initialize(ServletHandler.java:791)
	at org.eclipse.jetty.servlet.ServletContextHandler.startContext(ServletContextHandler.java:265)
	at org.eclipse.jetty.server.handler.ContextHandler.doStart(ContextHandler.java:717)
	at org.eclipse.jetty.util.component.AbstractLifeCycle.start(AbstractLifeCycle.java:64)
	at org.eclipse.jetty.server.handler.HandlerWrapper.doStart(HandlerWrapper.java:95)
	at org.eclipse.jetty.server.Server.doStart(Server.java:282)
	at org.eclipse.jetty.util.component.AbstractLifeCycle.start(AbstractLifeCycle.java:64)
	at com.ricston.contacts.Main.main(Main.java:35)
	... 6 more
```

So what does this stack trace tell us? Apparently, javax.ws.rs.core.Application is being loaded from some jar but it doesn't have the `getProperties()` method which returns a java.util.Map.

When you get this sort of behaviour; when a program works fine when executed from your IDE (for instance), but gives this sort of exception when executed in another environment (from Maven in this case), you have good reason to believe you've got a Jar hell issue to deal with. By "this sort of exception" I mean an exception which hints at the fact that you could have more than one jar in your classpath which has the same class name in the same package but with a different implementation. Class cast exceptions and no such method errors are common indicators of Jar hell. Again, by themselves they're not, but when you're getting this sort of thing when running in a particular environment but not in another, then that's a pretty good sign you've got Jar hell issues.

Ok, so now we're in trouble. Obviously, we can't just rely on chance, hoping the right class will be loaded in the environment we will be eventually deploying to. Technically, if I had `mvn package` on the Maven module I mentioned in passing above, I would get my war file and I could deploy it to my local installation of Tomcat, in which - as it so happens - the right class gets loaded and this exception doesn't rear its ugly head. But that's not solving the problem.

What we really need here is more information. It would be great if the NoSuchMethodError would also have given us the location of the jar from which the javax.ws.rs.core.Application class was being loaded when things crashed. We don't get that but we can use a tool called [JHades](http://jhades.org/) to let us know if we have duplicate classes in the jars in our classpath. That information will practically solve this issue.

Go ahead and add jhades (not jhades-standalone-report) to contacts-app-impl's pom - use latest version from [here](http://jhades.org/downloads.html):

```
<dependency>
    <groupId>org.jhades</groupId>
    <artifactId>jhades</artifactId>
    <version>1.0.4</version>
</dependency>
```

Now put the following in contacts-app-impl's Main.java's main method (at the beginning):

```
new JHades().overlappingJarsReport();
```

When you install and run as before, you should get a listing of overlapping jars in the console output along with the previous error message. The important part is the following:

```
file:/Users/justin/.m2/repository/javax/ws/rs/jsr311-api/1.1.1/jsr311-api-1.1.1.jar overlaps with
file:/Users/justin/.m2/repository/javax/ws/rs/javax.ws.rs-api/2.0/javax.ws.rs-api-2.0.jar - total overlapping classes: 55 - same classloader ! This is an ERROR!
```

We know from the error message that the problematic class is javax.ws.rs.core.Application, so it makes sense to focus on the clash involving jsr311-api-1.1.1.jar and javax.ws.rs.api-2.0.jar. Sure enough, if we open up the javax.ws.rs.core.Application class in both of these jars, we can see that the one in jsr311-api-1.1.1.jar does *not* have the getProperties() method which our code is trying to use. The one in javax.ws.rs.api-2.0.jar does. Note, if you don't have the sources in your local repo, you should be able to get them by running `mvn dependency:sources` in the contacts-app parent module's directory.

Finally, we're almost at the root of the problem here. The final piece of the puzzle is figuring out what's pulling in jsr311-api-1.1.1.jar and removing it. Since we actually started with the solution, you already know that contacts-app-api's dependency on raml-jaxrs-codegen-core is pulling in jsr311-api-1.1.1.jar - but assuming you didn't, how would you go about it?

What I did was simply dump out the output of `mvn dependency:tree` into a temporary file: `mvn dependency:tree >> dependency-tree` and search for jsr311 (or maybe grep for it with some context e.g. `mvn dependency:tree | grep -C 20 jsr311` if you don't want to stay creating a file). Either way, we get the info we need:

```
[INFO] +- com.ricston:contacts-app-api:jar:1.0.0-SNAPSHOT:compile
[INFO] |  \- org.raml:raml-jaxrs-codegen-core:jar:1.0.OA-SNAPSHOT:compile
[INFO] |     +- org.raml:raml-parser:jar:0.8.7:compile
[INFO] |     |  +- org.yaml:snakeyaml:jar:1.13:compile
[INFO] |     |  +- commons-validator:commons-validator:jar:1.3.1:compile
[INFO] |     |  |  +- commons-beanutils:commons-beanutils:jar:1.7.0:compile
[INFO] |     |  |  +- commons-digester:commons-digester:jar:1.6:compile
[INFO] |     |  |  |  +- commons-collections:commons-collections:jar:2.1:compile
[INFO] |     |  |  |  \- xml-apis:xml-apis:jar:1.0.b2:compile
[INFO] |     |  |  \- commons-logging:commons-logging:jar:1.0.4:compile
[INFO] |     |  \- org.kitchen-eel:json-schema-validator:jar:1.2.2:compile
[INFO] |     |     \- org.mozilla:rhino:jar:1.7R4:compile
[INFO] |     +- javax.ws.rs:jsr311-api:jar:1.1.1:compile
```

The code generator is pulling in the jar we want out, and come to think of it, it really makes no sense to include the raml-jaxrs-codegen-core as a dependency which is carried over when contacts-app-api is depended upon in another project. The code generator is only used to generate code - i.e. to build our contacts-app-api. It is not something we need at runtime. Therefore, using the scope of provided on the code generator will have the effect we're after, to include the dependency while building contacts-app-api but not when depending on it from another module.

What I'd like to quickly demo now is how to use JHades in other scenarios. Let's replicate the issue we are having here in the context of a war Maven module, where we don't have the luxury of simply dropping in some code to print out the overlapping jars.

So lets go ahead and add another Maven module - `contacts-app-server`. Now our project structure looks something like this:

```
── contacts-app
│   └── pom.xml
│   ├── contacts-app-server
│       ├── pom.xml
│       ├── src
├── contacts-app-api
│   ├── pom.xml
│   └── src
└── contacts-app-impl
    └── src
```

We'll `mkdir -p src/main/webapp/WEB-INF` in contacts-app-server and add web.xml in WEB-INF:

```
<?xml version="1.0" encoding="ISO-8859-1" standalone="no"?>
<web-app version="2.5" xmlns="http://java.sun.com/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd">

    <display-name>contacts-app-server</display-name>

<!--     REST API servlet: -->
    <servlet>
        <servlet-name>restService</servlet-name>
        <servlet-class>org.glassfish.jersey.servlet.ServletContainer</servlet-class>
        <init-param>
            <param-name>javax.ws.rs.Application</param-name>
            <param-value>com.ricston.contacts.app.ContactsApp</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>
    <servlet-mapping>
        <servlet-name>restService</servlet-name>
        <url-pattern>/api/*</url-pattern>
    </servlet-mapping>

</web-app>
```

Nothing special here. We've practically just moved over from running the app from Main.java to being able to package it up as a war and run it on a servlet container like Tomcat. (Note you can get the pom.xml which is not show here for brevity from the demo repo under the branch with the same name as this blog post - just remember that the code generator does have the scope of provided there).

If you try running contacts-app-server using `mvn clean tomcat7:run` you will get this error:

```
java.lang.ClassCastException: org.glassfish.jersey.servlet.ServletContainer cannot be cast to javax.servlet.Servlet
```

This is actually an error which has nothing to do with what we're concerned with. I am not entirely sure why but the Jetty dependencies we have in contacts-app-impl are causing this. I am able to open up the ServletContainer in my IDE and see that the HttpServlet it's extending is being picked up from a jar with Maven coordinates: org.eclipse.jetty.orbit:javax.servlet:3.0.0.v201112011016 - (looks like one crazy version number).

Anyway, we will not be using Main.java to host our web app so go ahead and delete it along with the Jetty dependencies in contacts-app-impl's pom.xml. Then you should be able to run tomcat from maven again and get the NoSuchMethodError from before.

In order to get the info we need here, we have two options (at least I know of 2 options). We can either drop in the dependency on JHades as we did before and add the following to our web.xml (at the start):

```
<listener>
        <listener-class> org.jhades.JHadesServletListener</listener-class>
</listener>
```

This should again give us the info we need (append the output to a temporary file).

Alternatively, you can get your hands on the jhades-standalone-report jar (either [download](http://jhades.org/downloads.html) it manually or depend on it and let Maven install it in your local repo). This allows us to run the following command on our project's output (the war file):

```
java -jar /Users/justin/.m2/repository/org/jhades/jhades-standalone-report/1.0.4/jhades-standalone-report-1.0.4.jar ./target/contacts-app-server-1.0.0-SNAPSHOT.war
```

Note: using `mvn tomcat7:run` seems to remove the war file from the target dir so you'll have to `mvn package` again.

Either way, you'll be able to get to the same conclusion as before with the output from JHades.

I will eventually come round to writing the blog post I initially had in mind (this one just kind of developed while trying to go for something else). Tune in again next time for yet another module we'll be adding to this project :)

Next time we will be going through setting up a front-end module using [Yeoman](http://yeoman.io/) (using the backbone generator) - build our project with Grunt from Maven and depend on our shiny new front-end module from our contacts-app-server.

Until next time :)
