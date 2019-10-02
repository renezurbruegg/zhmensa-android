package com.mensa.zhmensa.services;


import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.mensa.zhmensa.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import cz.msebera.android.httpclient.conn.scheme.HostNameResolver;
import cz.msebera.android.httpclient.conn.scheme.PlainSocketFactory;
import cz.msebera.android.httpclient.conn.scheme.Scheme;
import cz.msebera.android.httpclient.conn.scheme.SchemeRegistry;
import cz.msebera.android.httpclient.conn.ssl.SSLSocketFactory;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.protocol.HTTP;

@SuppressWarnings("HardCodedStringLiteral")
public class HttpUtils {
    //  private static final String BASE_URL = "http://api.twitter.com/1/";

    @Nullable
    private static AsyncHttpClient client;

    public static void getByUrl(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {

        if (client == null) {
            Log.e("HttpUtils", "Client was null. please init client at beginning of application!");
            client = new AsyncHttpClient();
        }

        Log.d("HTTPUtils.getByUrk", "Found API VERSION " + Build.VERSION.SDK_INT);

        client.setEnableRedirects(true);
        client.setConnectTimeout(40000);
        client.get(url, params, responseHandler);

    }


    public static void post(String url, JsonObject payload, Context context, ResponseHandlerInterface responseHandler) throws UnsupportedEncodingException {

        if (client == null) {
            Log.e("HttpUtils", "Client was null. please init client at beginning of application!");
            client = new AsyncHttpClient();
        }

        StringEntity entity = new StringEntity(payload.toString(), HTTP.UTF_8);
        Log.d("HttpUtils.post", "JSON BODY: " + entity);

        client.post(context, url, entity, "application/json",
                responseHandler);
    }

    public static int postByUrl(String urlString, String jsonPayload) throws IOException, JSONException {
        String result = "";

        URL url = new URL(urlString);

        // 1. create HttpURLConnection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");


        // 3. add JSON content to POST request body
        setPostRequestContent(conn, jsonPayload);
        //Log.e("cont", conn.getContent() +" ");
        // 4. make POST request to the given URL
        conn.connect();

        // get response
        InputStream responseStream = new BufferedInputStream(conn.getInputStream());
        BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
        String line = "";
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = responseStreamReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        responseStreamReader.close();

        String response = stringBuilder.toString();

        JSONObject jsonResponse = new JSONObject(response);
        return jsonResponse.getInt("id");
        //return response;
        // 5. return response message
  //      return conn.getResponseMessage() + " "  + conn.getResponseCode() + " " + conn.getOutputStream();

    }

    private static void setPostRequestContent(HttpURLConnection conn,
                                       String payload) throws IOException {

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write("{\"title\": \"Question #1234567\", \"options\": " +payload +", \"multi\":false}");
        //writer.write(jsonObject.toString());
        writer.flush();
        writer.close();
        os.close();
    }

    public static void setupClient(Context ctx) {

        if (Build.VERSION.SDK_INT < 23) {
            // Fix certificate problems with old SDK, since Certificate is not in root we need to verifiy with a custom one
            final SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            AdditionalKeyStoresSSLSocketFactory sf = createAdditionalCertsSSLSocketFactory(ctx);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            schemeRegistry.register(new Scheme("https", sf, 443));
            client = new AsyncHttpClient(schemeRegistry);
        } else {
            // All good. Certificate should be in root repo
            client = new AsyncHttpClient();
        }
    }


    /*
     STUFF TO FIX SSL PROBLEM WITH OLD DEVICES
    */

    private static AdditionalKeyStoresSSLSocketFactory createAdditionalCertsSSLSocketFactory(Context context) {
        try {
            final KeyStore ks = KeyStore.getInstance("BKS");

            // the bks file we generated above
            final InputStream in = context.getResources().openRawResource(R.raw.mystore);
            try {
                // don't forget to put the password used above in strings.xml/mystore_password
                ks.load(in, context.getString(R.string.mystore_password).toCharArray());
            } finally {
                in.close();
            }

            return new AdditionalKeyStoresSSLSocketFactory(ks);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Allows you to trust certificates from additional KeyStores in addition to
     * the default KeyStore
     */
    public static class AdditionalKeyStoresSSLSocketFactory extends SSLSocketFactory {
        protected SSLContext sslContext = SSLContext.getInstance("TLS");

        public AdditionalKeyStoresSSLSocketFactory(KeyStore keyStore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(null, null, null, null, null, (HostNameResolver) null);
            sslContext.init(null, new TrustManager[]{new AdditionalKeyStoresTrustManager(keyStore)}, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }



        /**
         * Based on http://download.oracle.com/javase/1.5.0/docs/guide/security/jsse/JSSERefGuide.html#X509TrustManager
         */
        public static class AdditionalKeyStoresTrustManager implements X509TrustManager {

            protected ArrayList<X509TrustManager> x509TrustManagers = new ArrayList<X509TrustManager>();


            protected AdditionalKeyStoresTrustManager(KeyStore... additionalkeyStores) {
                final ArrayList<TrustManagerFactory> factories = new ArrayList<TrustManagerFactory>();

                try {
                    // The default Trustmanager with default keystore
                    final TrustManagerFactory original = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    original.init((KeyStore) null);
                    factories.add(original);

                    for( KeyStore keyStore : additionalkeyStores ) {
                        final TrustManagerFactory additionalCerts = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                        additionalCerts.init(keyStore);
                        factories.add(additionalCerts);
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }



                /*
                 * Iterate over the returned trustmanagers, and hold on
                 * to any that are X509TrustManagers
                 */
                for (TrustManagerFactory tmf : factories)
                    for( TrustManager tm : tmf.getTrustManagers() )
                        if (tm instanceof X509TrustManager)
                            x509TrustManagers.add( (X509TrustManager)tm );


                if( x509TrustManagers.size()==0 )
                    throw new RuntimeException("Couldn't find any X509TrustManagers");

            }

            /*
             * Delegate to the default trust manager.
             */
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                final X509TrustManager defaultX509TrustManager = x509TrustManagers.get(0);
                defaultX509TrustManager.checkClientTrusted(chain, authType);
            }

            /*
             * Loop over the trustmanagers until we find one that accepts our server
             */
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                for( X509TrustManager tm : x509TrustManagers ) {
                    try {
                        tm.checkServerTrusted(chain,authType);
                        return;
                    } catch( CertificateException e ) {
                        // ignore
                    }
                }
                throw new CertificateException();
            }

            public X509Certificate[] getAcceptedIssuers() {
                final ArrayList<X509Certificate> list = new ArrayList<X509Certificate>();
                for( X509TrustManager tm : x509TrustManagers )
                    list.addAll(Arrays.asList(tm.getAcceptedIssuers()));
                return list.toArray(new X509Certificate[list.size()]);
            }
        }

    }
}
