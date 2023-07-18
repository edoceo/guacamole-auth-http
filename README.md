# Guacamole Auth HTTP

This modules will proxy your HTTP Auth Request to a provider of your choice

## Building

Get the Simple JSON for Java and then add it to the Guacamole CLASSPATH
You can copy, symlink or adjust Tomcat{6,7,8} configurations

	apt-get install libjson-simple-java
	emerge dev-java/json-simple

You will need also  org.apache.commons classes (to be documented)

From the guacamole-client root clone this project into extensions/

	cd extensions/
	git clone THIS
	cd guacamole-auth-http
	mvn package
	cp target/guacamole-auth-http-2014.38.jar /var/lib/tomcat7/webapps/guacamole/WEB-INF/lib/
	cp /usr/share/java/json-simple-1.1.1.jar /var/lib/tomcat7/webapps/guacamole/WEB-INF/lib/

	/etc/init.d/tomcat7 restart

## Configuration

Add the following to your guacamole.properties (/etc/guacamole or /usr/share/tomcat7/.guacamole/guacamole.properties)

	auth-provider com.edoceo.guacamole.auth.HttpAuthenticationProvider
	auth-http-page: http://sso.example.com/external/guacamole
	auth-http-head-auth: Whatever Here is added as Authorization header and is required

## HTTP Auth Server

The HTTP Server will be sent

	POST /external/guacamole HTTP/1.1
	Accept: application/json
	Authorization:  Whatever Here is added as Authorization header and is required
	Content-Length: 47
	Content-Type: application/json

	{
		"username":"something",
		"password":"something"
	}


The HTTP Server should respond with proper HTTP codes: 200, 403.
The responding JSON looks like for a VNC:

	{
		"protocol": "vnc",
		"name": "Connection Name",
		"hostname": "vnc.example.com",
		"port": 5900
	}

For RDP you can use (NOTE: host vs hostname parameter changed with last commit):

	{
		"protocol": "rdp",
		"name": "Connection Name",
		"hostname": "vnc.example.com",
		"port": 3389,
		"username": "user@domain.local",
		"password": "AlocaP4ww0rd",
		"server-layout": "fr-fr-azerty"
	}

Notice that ALL field are required for RDP / VNC, the auth module can throw exception if some is missing !
