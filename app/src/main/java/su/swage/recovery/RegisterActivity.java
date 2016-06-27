package su.swage.recovery;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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

import su.swage.recovery.utils.InterfaceAPI;
import su.swage.recovery.utils.RequestAPI;
import su.swage.recovery.utils.SessionManager;

public class RegisterActivity extends Activity implements InterfaceAPI {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnRegister;
    private Button btnLinkToLogin;
    private EditText inputFullName;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputFullName = (EditText) findViewById(R.id.name);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        if (SessionManager.isLogged) {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String name = inputFullName.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                    if (isAlphaNumeric(name) && isAlphaNumeric(email) && isAlphaNumeric(password))
                        registerUser(name, email, password);
                    else Toast.makeText(getApplicationContext(),
                            R.string.toast_login_credentials2, Toast.LENGTH_LONG)
                            .show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            R.string.toast_login_credentials, Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    public boolean isAlphaNumeric(String s) {
        String pattern = "^[a-zA-Z0-9@.]*$";
        return s.matches(pattern);
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

    private void registerUser(final String name, final String email, final String password) {
        pDialog.setMessage(getString(R.string.progress_registering));
        if (!pDialog.isShowing()) pDialog.show();

        Hashtable<String, String> hashtable = new Hashtable<>();
        hashtable.put("email", email);
        hashtable.put("password", get_SHA512(password));
        hashtable.put("name", name);

        RequestAPI rAPI = new RequestAPI(this, this);
        rAPI.setMethod("/users/register");
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
        System.out.println(result.toString());
        return null;
    }
}