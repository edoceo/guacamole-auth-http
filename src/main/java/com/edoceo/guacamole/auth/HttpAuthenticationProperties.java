/**
 * Properties used by the HTTP Authentication plugin.
 * @author Edoceo, Inc
 */

package com.edoceo.guacamole.auth;

import org.glyptodon.guacamole.properties.BooleanGuacamoleProperty;
import org.glyptodon.guacamole.properties.IntegerGuacamoleProperty;
import org.glyptodon.guacamole.properties.StringGuacamoleProperty;

public class HttpAuthenticationProperties {

    /**
     * This class should not be instantiated.
     */
    private HttpAuthenticationProperties() {}

    /**
     * The URL of the MySQL server hosting the guacamole authentication tables.
     */
    public static final StringGuacamoleProperty AUTH_PAGE = new StringGuacamoleProperty() {

        @Override
        public String getName() { return "auth-http-page"; }

    };

    /**
     * The port of the MySQL server hosting the guacamole authentication tables.
     */
    public static final StringGuacamoleProperty HEAD_AUTH = new StringGuacamoleProperty() {

        @Override
        public String getName() { return "auth-http-head-auth"; }

    };

}
