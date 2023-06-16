/*
 * =====================================================
 * ANDROID INVENTORY MANAGEMENT APP
 * Ed Morrow
 * Southern New Hampshire University
 * CS499: Computer Science Capstone
 * Milestone 3: Databases Code Enhancement
 * Prof Brooke Goggin
 * 8 June 2023
 * =====================================================
 */

package com.example.cs499enhancedandroidapp.login;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;

import com.example.cs499enhancedandroidapp.R;
import com.example.cs499enhancedandroidapp.data.DatabaseManager;

/*
 * =====================================================
 * The Login Activity
 * =====================================================
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etxtUsername;
    private EditText etxtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etxtUsername = findViewById(R.id.etxtUsername);
        etxtPassword = findViewById(R.id.etxtPassword);
        Button btnLogin = (Button)findViewById(R.id.btnLogin);
        TextView txtCreateLogin = findViewById(R.id.txtCreateLogin);

        etxtUsername.setOnClickListener(l -> etxtUsername.setText(""));
        etxtPassword.setOnClickListener(l -> etxtPassword.setText(""));
        btnLogin.setOnClickListener(l -> handleLogin());
        txtCreateLogin.setOnClickListener(l -> createLogin());
    }

    private void handleLogin() {
        String username = etxtUsername.getText().toString();
        String password = etxtPassword.getText().toString();

        if (DatabaseManager.getInstance(getApplicationContext()).authenticateLogin(username, password)) {
            DatabaseManager.getInstance(getApplicationContext()).setPasscode();

            Intent intent = new Intent(this, AuthenticateActivity.class);
            startActivity(intent);
        }
        else {
            etxtPassword.setText("");
            if (DatabaseManager.getInstance(getApplicationContext()).verifyUsername(username)) {
                etxtUsername.setText(getString(R.string.incorrectPasswordError));
            }
            else {
                etxtUsername.setText(R.string.incorrectUsernameError);
            }
        }
    }

    private void createLogin() {
        Intent intent = new Intent(this, CreateLoginActivity.class);
        startActivity(intent);
    }
}