package com.development.taxiappproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginScreen extends AppCompatActivity {
    EditText loginEdt;
    Button btnLogin;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        sharedPreferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
        String userToken = sharedPreferences.getString("userToken", "defaultValue");

        assert userToken != null;
        if (!userToken.equalsIgnoreCase("defaultValue")) {
            // User is signed in (getCurrentUser() will be null if not signed in)
            Intent intent = new Intent(this, HomeScreen.class);
            startActivity(intent);
            finish();
        }

        loginEdt = findViewById(R.id.phone_number);
        btnLogin = findViewById(R.id.login_btn);
        btnLogin.setOnClickListener(view -> {
            if (validate()) {
                try {
                    String email = loginEdt.getText().toString();
                    Intent intent = new Intent(getBaseContext(), OTPScreen.class);
                    intent.putExtra("type", "signIn");
                    intent.putExtra("phone_number", email);
                    startActivity(intent);
                } catch (Exception ex) {
                    Log.e("Mahdi: Login error: 1 ", String.valueOf(ex));
                }
            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signup:
                startActivity(new Intent(getApplicationContext(), SignUpScreen.class));
                break;

            case R.id.forgot_password:
                startActivity(new Intent(getApplicationContext(), ForgotPassword.class));
                break;
        }
    }

    public boolean validate() {
        boolean valid = true;

        String login = loginEdt.getText().toString();

        if (login.isEmpty()) {
            loginEdt.setError("enter a valid email address");
            valid = false;
        } else {
            loginEdt.setError(null);
        }
        return valid;
    }
}

//class RetrieveFeedTask extends AsyncTask<String, Void, Void> {
//    private Exception exception;
//    private String phone;
//    private String password;
//
//    RetrieveFeedTask(String phone, String password) {
//        this.phone = phone;
//        this.password = password;
//    }
//
//    protected Void doInBackground(String... urls) {
//        try {
//            loginMethod();
//        } catch (UnsupportedEncodingException e) {
//            Log.e("Mahdi: Login: ", "doInBackground: " + e);
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//
//    public void loginMethod() throws UnsupportedEncodingException {
//        String data = URLEncoder.encode("phone", "UTF-8")
//                + "=" + URLEncoder.encode(phone, "UTF-8");
//
//
//        String text = "";
//        BufferedReader reader = null;
//
//        try {
//            // Defined URL  where to send data
//            URL url = new URL("https://taxiappapi.webfumeprojects.online/driver/login");
//
//            // Send POST data request
//
//            URLConnection conn = url.openConnection();
//            conn.setDoOutput(true);
//            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
//            wr.write(data);
//            wr.flush();
//
//            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            StringBuilder sb = new StringBuilder();
//            String line = null;
//
//            Log.i("Mahdi: Login: Server", sb.toString());
//
//            while ((line = reader.readLine()) != null) {
//                // Append server response in string
//                sb.append(line).append("\n");
//            }
//
//            text = sb.toString();
//
//        } catch (IOException e) {
//            Log.e("Mahdi: Login error: 2 ", String.valueOf(e));
//        } finally {
//            try {
//                assert reader != null;
//                if (reader != null)
//                    reader.close();
//            } catch (Exception ex) {
//                Log.e("Mahdi: Login error: 3 ", String.valueOf(ex));
//
//            }
//        }
//    }
//}
