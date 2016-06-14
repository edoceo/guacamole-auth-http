/**
 * Leverage an Up-Stream HTTP(S) Server which provides the authentication and configuration details. Recent modifications made
 * to assume that the upstream config provider is going to provide us a valid config. Assuming that protocol is acceptable, configuration bits
 * should be passed directly to guacamole, etc.
 *
 * Example `guacamole.properties`:
 *
 *  auth-provider: net.sourceforge.guacamole.net.auth.http.HttpAuthenticationProvider
 *
 * @author David Busby / Edoceo, Inc.
 * @author last update by David Gibbons david.c.gibbons@gmail.com
 * @see http://stackoverflow.com/questions/2793150/how-to-use-java-net-urlconnection-to-fire-and-handle-http-requests
 * @see http://alvinalexander.com/blog/post/java/how-open-url-read-contents-httpurl-connection-java
 * @see http://www.java2blog.com/2013/11/jsonsimple-example-read-and-write-json.html
 */


package com.edoceo.guacamole.auth;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

// libraries that we need to do the advanced XML processing
import java.util.Iterator;
import java.util.Set;
import java.lang.String;

// JSON Parser
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.GuacamoleServerException;
import org.glyptodon.guacamole.net.auth.simple.SimpleAuthenticationProvider;
import org.glyptodon.guacamole.net.auth.AuthenticationProvider;
import org.glyptodon.guacamole.net.auth.Credentials;
import org.glyptodon.guacamole.properties.FileGuacamoleProperty;
import org.glyptodon.guacamole.properties.GuacamoleProperties;
import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;

public class HttpAuthenticationProvider extends SimpleAuthenticationProvider {

	/**
	 * Logger for this class.
	 */
	private Logger logger = LoggerFactory.getLogger(HttpAuthenticationProvider.class);

	/**
	 * Map of all known configurations, indexed by identifier.
	 */
	private Map<String, GuacamoleConfiguration> configs;

	/*
	 * What protocols are available
	 */
	private enum GProto
	{
		VNC, RDP;
	}

	public synchronized void init() throws GuacamoleException {
		// Nothing
	}

	/*@Override
	public String getIdentifier() {
		return "guac-auth-http";
	}*/

	@Override
	public Map<String, GuacamoleConfiguration> getAuthorizedConfigurations(Credentials credentials) throws GuacamoleException {

		// Verify Password Inputs
		if (null == credentials.getUsername()) {
			logger.info("Invalid username");
			return null;
		}

		if (null == credentials.getPassword() || 0 == credentials.getPassword().length()) {
			logger.info("Invalid password");
			return null;
		}

		Map<String, GuacamoleConfiguration> configs = new TreeMap<String, GuacamoleConfiguration>();

		try {
			String auth_page = GuacamoleProperties.getRequiredProperty( HttpAuthenticationProperties.AUTH_PAGE );
			String head_auth = GuacamoleProperties.getRequiredProperty( HttpAuthenticationProperties.HEAD_AUTH );

			logger.info("url:" + auth_page);
			URL u = new URL(auth_page);

			HttpURLConnection uc = (HttpURLConnection) u.openConnection();
			uc.setDoOutput(true);
			uc.setDoInput(true);

			if (head_auth.length() > 0) {
				uc.setRequestProperty("Authorization", head_auth);
			}
			uc.setRequestProperty("Content-Type", "application/json; charset=uf-8;");

			// Add a timeout of 3 seconds
			uc.setReadTimeout(3*1000);
			uc.connect();

			// Send
			// @todo Build with JSON
			JSONObject sendJSON = new JSONObject();
			sendJSON.put("username", credentials.getUsername());
			sendJSON.put("password", credentials.getPassword());

			DataOutputStream os = new DataOutputStream(uc.getOutputStream());
			os.writeBytes(sendJSON.toJSONString());
			os.flush();
			os.close();

			// Read Response Status Code?
			if(uc.getResponseCode() != 200){
			    logger.info("HTTP Response code was " + uc.getResponseCode());
			    return null;
			}else{
				BufferedReader rd = new BufferedReader(new InputStreamReader(uc.getInputStream()));
				//logger.info("InputStream ok");
				String responsetoparse = org.apache.commons.io.IOUtils.toString(rd);

				//logger.info("Response : "+responsetoparse);
				
				JSONObject json = (JSONObject)JSONValue.parseWithException(responsetoparse);

				logger.info("Got the JSON" + json);

				GuacamoleConfiguration config = new GuacamoleConfiguration();

                // set the protocol in our config. assume that json is passing in a valid config.
                config.setProtocol(json.get("protocol").toString().toLowerCase());

				// add all of the parameters that are in the json object to the configuration
                Set keys = json.keySet();
                Iterator a = keys.iterator();
                while(a.hasNext()){
                    String paramName = (String)a.next();
                    config.setParameter(paramName, (String)json.get(paramName));
                }

                // put the connection name into the config
                configs.put(json.get("name").toString(), config);

				return configs;
			}
		} catch (Exception e) {
			logger.info("Exception: " + Arrays.toString(e.getStackTrace()));
			logger.info("Message: " + e.toString());
			// throw e;
		}

		return null;
	}
}
