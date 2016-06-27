package su.swage.recovery;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;

import su.swage.recovery.utils.AlertDialogs;
import su.swage.recovery.utils.InterfaceAPI;
import su.swage.recovery.utils.RequestAPI;
import su.swage.recovery.utils.SessionManager;

public class LoginActivity extends Activity implements InterfaceAPI {
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPref = getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        if (getIntent().getBooleanExtra("unLogin", false)) unLogin();

        setContentView(R.layout.activity_login);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        Button btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        if (SessionManager.isLogged) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        if (sharedPref.getString("EMail", "") != "") {
            inputEmail.setText(sharedPref.getString("EMail", ""));
        }

        if (sharedPref.getString("Password", "") != "") {
            inputPassword.setText(sharedPref.getString("Password", ""));
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                checkLogin(email, password, true);
            } else {
                Toast.makeText(getApplicationContext(),
                        R.string.toast_login_credentials, Toast.LENGTH_LONG)
                        .show();
            }
        }

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (!email.isEmpty() && !password.isEmpty()) {
                    checkLogin(email, password, false);
                } else {
                    Toast.makeText(getApplicationContext(),
                            R.string.toast_login_credentials, Toast.LENGTH_LONG)
                            .show();
                }
            }

        });

        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });
        editor.apply();
    }

    public String get_SHA512(String passwordToHash) {
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] bytes = md.digest(passwordToHash.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    private void checkLogin(final String email, final String password, boolean auto) {
        pDialog.setMessage(getString(R.string.progress_login));
        if (!pDialog.isShowing()) pDialog.show();

        Hashtable<String, String> hashtable = new Hashtable<>();
        if (auto) {
            hashtable.put("email", email);
            hashtable.put("password", password);
            editor.putString("EMail", email);
            editor.putString("Password", password);
        } else {
            hashtable.put("email", email);
            hashtable.put("password", get_SHA512(password));
            editor.putString("EMail", email);
            editor.putString("Password", get_SHA512(password));
        }
        editor.commit();

        RequestAPI rAPI = new RequestAPI(this, this);
        rAPI.setMethod("/users/login");
        rAPI.execute(hashtable);
    }

    @Override
    public Void onSuccessResponseAPI(JSONObject result) {
        try {
            SessionManager.ApiKey = result.getString("token");
            SessionManager.isLogged = true;
            SessionManager.eMail = result.getString("email");
            SessionManager.UserName = result.getString("name");
            if (pDialog.isShowing()) pDialog.dismiss();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Void onFailureResponseAPI(Boolean isSystem, JSONObject result) {
        SessionManager.isLogged = false;
        SessionManager.ApiKey = "";
        if (pDialog.isShowing()) pDialog.dismiss();

        if (isSystem) new AlertDialogs(this).NetworkError();
        else new AlertDialogs(this).LoginError();
        return null;
    }

    public void unLogin() {
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        editor.putBoolean("HasPINCode", false);
        editor.putString("EMail", "");
        editor.putString("Password", "");
        editor.apply();
        SessionManager.isLogged = false;
    }
}