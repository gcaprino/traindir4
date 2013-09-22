package com.traindirector.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import jcifs.util.Base64;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;

import com.traindirector.Activator;

public class HTTPClient {

    private static final int TIMEOUT = 30000;

    /**
     * Accepts the SSL certificate from the Server and creates the HTTP client
     *
     * @return
     * @throws Exception
     */
    public static DefaultHttpClient getDefaultHTTPClient() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("SSL");

        // set up a TrustManager that trusts everything
        sslContext.init(null, new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        } }, new SecureRandom());

        SSLSocketFactory sf = new SSLSocketFactory(sslContext);
        sf.setHostnameVerifier(new X509HostnameVerifier() {

            @Override
            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }

            @Override
            public void verify(String arg0, String[] arg1, String[] arg2) throws SSLException {

            }

            @Override
            public void verify(String arg0, X509Certificate arg1) throws SSLException {

            }

            @Override
            public void verify(String arg0, SSLSocket arg1) throws IOException {

            }
        });
        Scheme httpsScheme = new Scheme("https", sf, 443);
        Scheme httpScheme = new Scheme("http", PlainSocketFactory.getSocketFactory(), 80);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(httpsScheme);
        schemeRegistry.register(httpScheme);

        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
        ClientConnectionManager cm = new SingleClientConnManager(params, schemeRegistry);
        DefaultHttpClient httpClient = new DefaultHttpClient(cm, params);

        return httpClient;

    }

    public static void checkForProxy(DefaultHttpClient httpClient, String hostName, String proxyType)
            throws Exception {
        IProxyService proxy = Activator.getDefault().getProxyService();
        if (proxy.isProxiesEnabled()) {
            IProxyData pData = proxy.getProxyDataForHost(hostName, proxyType);
            if (pData == null) {
                return;
            }
            String proxyHost = pData.getHost();
            int proxyPort = pData.getPort();
            if ((proxyHost != null) && (proxyPort != -1)) {
                HttpHost httpProxy = new HttpHost(proxyHost, proxyPort);
                httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, httpProxy);
            }
            String proxyUser = pData.getUserId();
            String proxyPassword = pData.getPassword();
            String proxyDomain = null;
            if ((proxyUser != null) && (proxyPassword != null)) {
                Credentials credentials = null;
                if (proxyUser.contains("\\")) {
                    String[] parts = proxyUser.split("\\\\");
                    if (parts.length < 2) {
                        throw new Exception("Incorrect format - Expected domain\\username");
                    }
                    proxyDomain = parts[0];
                    proxyUser = parts[1];
                }
                if (proxyDomain != null) {
                    httpClient.getAuthSchemes().register("ntlm", new NTLMSchemeFactory());
                    credentials = new NTCredentials(proxyUser, proxyPassword, "", proxyDomain);
                } else {
                    credentials = new UsernamePasswordCredentials(proxyUser, proxyPassword);
                }
                AuthScope authScope = new AuthScope(proxyHost, proxyPort);
                httpClient.getCredentialsProvider().setCredentials(authScope, credentials);
            }
        }
    }

    public static void addAuthenticationHeader(HttpRequestBase request, String authKey, String identifier)
            throws UnsupportedEncodingException {
    	/*
        String passcode = "Workbench-" + System.currentTimeMillis();

        if (authKey != null) {
            passcode = passcode + "-" + authKey;
        }
        String encodedString = java.net.URLEncoder.encode(Base64.encode(passcode.getBytes("UTF-8")), "UTF-8");
        String identifierString = java.net.URLEncoder.encode(Base64.encode(identifier.getBytes("UTF-8")), "UTF-8");
        request.addHeader("Authorization", "key=" + encodedString);
        request.addHeader("Identifier", "id=" + identifierString);
*/
    }

    public String get(String url) {
        try {
            DefaultHttpClient httpClient = getDefaultHTTPClient();
            checkForProxy(httpClient, "www.vmware.com", "http");
            HttpGet request = new HttpGet(url);
            HttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String content = response.toString();
                HttpEntity entity = response.getEntity();
                InputStream stream = entity.getContent();
                content = "";
                int i;
                while((i = stream.read()) != -1) {
                	content += (char)i;
                }
                return content;
            }
        } catch (Exception e) {

        }
    	return null;
    }

}
