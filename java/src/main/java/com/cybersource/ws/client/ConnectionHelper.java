/*
* Copyright 2003-2014 CyberSource Corporation
*
* THE SOFTWARE AND THE DOCUMENTATION ARE PROVIDED ON AN "AS IS" AND "AS
* AVAILABLE" BASIS WITH NO WARRANTY.  YOU AGREE THAT YOUR USE OF THE SOFTWARE AND THE
* DOCUMENTATION IS AT YOUR SOLE RISK AND YOU ARE SOLELY RESPONSIBLE FOR ANY DAMAGE TO YOUR
* COMPUTER SYSTEM OR OTHER DEVICE OR LOSS OF DATA THAT RESULTS FROM SUCH USE. TO THE FULLEST
* EXTENT PERMISSIBLE UNDER APPLICABLE LAW, CYBERSOURCE AND ITS AFFILIATES EXPRESSLY DISCLAIM ALL
* WARRANTIES OF ANY KIND, EXPRESS OR IMPLIED, WITH RESPECT TO THE SOFTWARE AND THE
* DOCUMENTATION, INCLUDING ALL WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE,
* SATISFACTORY QUALITY, ACCURACY, TITLE AND NON-INFRINGEMENT, AND ANY WARRANTIES THAT MAY ARISE
* OUT OF COURSE OF PERFORMANCE, COURSE OF DEALING OR USAGE OF TRADE.  NEITHER CYBERSOURCE NOR
* ITS AFFILIATES WARRANT THAT THE FUNCTIONS OR INFORMATION CONTAINED IN THE SOFTWARE OR THE
* DOCUMENTATION WILL MEET ANY REQUIREMENTS OR NEEDS YOU MAY HAVE, OR THAT THE SOFTWARE OR
* DOCUMENTATION WILL OPERATE ERROR FREE, OR THAT THE SOFTWARE OR DOCUMENTATION IS COMPATIBLE
* WITH ANY PARTICULAR OPERATING SYSTEM.
*/

package com.cybersource.ws.client;

import org.bouncycastle.util.encoders.Base64;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Helps in creating the Proxy and adding Proxy credentials to HttpURLConnection.
 */
class ConnectionHelper {
	
	
    /**
     * Default HTTP Client 
     * @return
     */
    public static boolean getDefaultUseHttpClient() {
        return false;
    }

    /**
     * Sets the timeout for HTTP Request
     * @param con
     * @param timeout
     */
    public static void setTimeout(HttpURLConnection con, int timeout) {
        int timeoutInMS = timeout * 1000;
        con.setConnectTimeout(timeoutInMS);
        con.setReadTimeout(timeoutInMS);
    }

    /**
     * Open the HTTPURLConnection for given credentials.
     * @param url - URL strinf
     * @param mc - Merchant Config
     * @return returns intance of the connection to server.
     * @throws IOException
     */
    public static HttpURLConnection openConnection(
            URL url, MerchantConfig mc)
            throws IOException {
        Proxy proxy = createProxy(mc);
        HttpURLConnection con = (HttpURLConnection)
                url.openConnection(proxy);

        if (proxy != Proxy.NO_PROXY) {
            addProxyCredentials(con, mc);
        }

        return con;
    }

    /**
     * Creates a HTTP Proxy request
     * @param mc
     * @return
     */
    private static Proxy createProxy(MerchantConfig mc) {
        if (mc.getProxyHost() == null) {
            return Proxy.NO_PROXY;
        }

        InetSocketAddress addr
                = new InetSocketAddress(
                mc.getProxyHost(), mc.getProxyPort());

        return new Proxy(Proxy.Type.HTTP, addr);
    }

    /**
     * Add proxy credentails for creating a connection
     * @param con
     * @param mc
     */
    private static void addProxyCredentials(
            HttpURLConnection con, MerchantConfig mc)

    {
        if (mc.getProxyUser() != null) {
            con.setRequestProperty(
                    "Proxy-Authorization",
                    "Basic " +
                            Base64.encode((mc.getProxyUser() + ":" + mc.getProxyPassword()).getBytes(Charset.forName("UTF-8"))));

        }
    }
}
