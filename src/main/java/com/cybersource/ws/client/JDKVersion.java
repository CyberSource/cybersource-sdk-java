package com.cybersource.ws.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import com.com.com.cybersource.b;
import com.com.com.cybersource.c;
import com.com.com.Base64.a;

/**
 * Helps in creating the Proxy and adding Proxy credentials to HttpURLConnection.
 * This is aimed for JDK versions 1.5+
 * @author sunagara
 *
 */
class JDKVersion
{
	public static boolean getDefaultUseHttpClient()
	{
		return false;
	}	

	public static void setTimeout( HttpURLConnection con, int timeout ) 
	{
		int timeoutInMS = timeout * 1000;
		con.setConnectTimeout( timeoutInMS );
		con.setReadTimeout( timeoutInMS );
        }

	public static HttpURLConnection openConnection(
		URL url, MerchantConfig mc )
	  throws IOException, b,
		 c
	{
		Proxy proxy = createProxy( mc );
        	HttpURLConnection con = (HttpURLConnection)
			url.openConnection( proxy );

		if (proxy != Proxy.NO_PROXY) {
			addProxyCredentials( con, mc );
		}

		return con;
	}

	private static Proxy createProxy( MerchantConfig mc ) 
	{
		if (mc.getProxyHost() == null) {
			return Proxy.NO_PROXY;
		}

		InetSocketAddress addr
			= new InetSocketAddress(
				mc.getProxyHost(), mc.getProxyPort() );

		return new Proxy( Proxy.Type.HTTP, addr );
        }

	private static void addProxyCredentials(
		HttpURLConnection con, MerchantConfig mc )
	  throws b, c
	{
                if (mc.getProxyUser() != null) {
                   con.setRequestProperty(
                        "Proxy-Authorization",
                        "Basic " +
                        a.a(
                          mc.getProxyUser() + ":" + mc.getProxyPassword()));
		}
	}
}
