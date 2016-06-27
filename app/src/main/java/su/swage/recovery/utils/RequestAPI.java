package su.swage.recovery.utils;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class RequestAPI extends AsyncTask<Hashtable<String, String>, Void, JSONObject> {
    public static long lastPing = 0;
    private SSLContext socketFact;
    private InterfaceAPI initclass;
    private String method;

    public RequestAPI(Context mContext, InterfaceAPI initclass) {
        this.initclass = initclass;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream cert = new BufferedInputStream(mContext.getAssets().open("https.crt"));
            Certificate ca = cf.generateCertificate(cert);

            // creating a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // creating a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // creating an SSLSocketFactory that uses our TrustManager
            socketFact = SSLContext.getInstance("TLS");
            socketFact.init(null, tmf.getTrustManagers(), null);
        } catch (CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    protected JSONObject doInBackground(Hashtable<String, String>... params) {
        try {
            URL url = new URL("https://recovery.swage.su:24242/api" + method);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setSSLSocketFactory(socketFact.getSocketFactory());
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("USER-AGENT", "SwageRecovery/1.0");
            urlConnection.setDoOutput(true);
            DataOutputStream dStream = new DataOutputStream(urlConnection.getOutputStream());
            StringBuilder buf = new StringBuilder();
            Enumeration<String> keys = params[0].keys();
            while (keys.hasMoreElements()) {
                buf.append(buf.length() == 0 ? "" : "&");
                String key = keys.nextElement();
                buf.append(key).append("=").append(params[0].get(key));
            }
            String bufStream = new String(buf.toString().getBytes(), "UTF-8");
            long start = System.currentTimeMillis();
            dStream.writeBytes(bufStream);
            dStream.flush();
            dStream.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            StringBuilder responseOutput = new StringBuilder();
            while ((line = br.readLine()) != null) {
                responseOutput.append(line);
            }
            br.close();
            lastPing = System.currentTimeMillis() - start;
            return new JSONObject(new String(responseOutput.toString().getBytes(), "UTF-8"));
        } catch (IOException e) {
            try {
                return new JSONObject("{\"status\": \"error\", \"system\": \"IOException\"}");
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        } catch (JSONException e) {
            try {
                return new JSONObject("{\"status\": \"error\", \"system\": \"JSONException\"}");
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }

    protected void onPostExecute(JSONObject result) {
        if (result != null) {
            try {
                if (result.getString("status").contains("success")) {
                    initclass.onSuccessResponseAPI(result);
                } else {
                    try {
                        if (result.getString("type") != null)
                            initclass.onFailureResponseAPI(false, result);
                    } catch (JSONException e) {
                        initclass.onFailureResponseAPI(true, result);
                    }
                }
            } catch (JSONException e) {
                initclass.onFailureResponseAPI(true, null);
            }
        }
        if (result == null) {
            initclass.onFailureResponseAPI(true, null);
        }
    }
}
