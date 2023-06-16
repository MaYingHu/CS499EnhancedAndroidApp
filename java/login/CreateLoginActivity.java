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
import android.widget.EditText;
import android.widget.Toast;

import com.example.cs499enhancedandroidapp.R;
import com.example.cs499enhancedandroidapp.data.DatabaseManager;

import java.util.Random;

/*
 * =====================================================
 * The Create Login Activity
 * =====================================================
 */
public class CreateLoginActivity extends AppCompatActivity {

    private EditText etxtUsername;
    private EditText etxtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_login);

        etxtUsername = findViewById(R.id.etxtUsername);
        etxtPassword = findViewById(R.id.etxtPassword);
        Button btnCreateLogin = (Button)findViewById(R.id.btnCreateLogin);
        Button btnGoBack = (Button)findViewById(R.id.btnGoBack);

        etxtUsername.setOnClickListener(l -> {etxtUsername.setText(""); etxtPassword.setText("");});
        etxtPassword.setOnClickListener(l -> etxtPassword.setText(""));
        btnCreateLogin.setOnClickListener(l -> createLogin());
        btnGoBack.setOnClickListener(l -> goBack());
    }

    private void goBack() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    /*
     * method creates 'salt' (i.e. a random string of 32 alphanumeric characters)
     * @return String salt: random string of 32 alphanumeric characters
     */
    public String createSalt() {

        String salt = "";

        Random random = new Random();

        int upperbound = 62;

        for (int i = 0; i < 32; ++i) {
            int next_char = random.nextInt(upperbound);
            if (next_char > 9) {
                if (next_char > 35) {
                    next_char += 13;
                } else
                    next_char += 7;
            }
            next_char += 48;
            salt += (char) next_char;
        }

        return salt;
    }

    private void createLogin() {

        String username;
        String password;

        username = etxtUsername.getText().toString();
        password = etxtPassword.getText().toString();
        String salt = createSalt();
        String saltedPassword = password + salt;
        String hashedSaltedPassword = DatabaseManager.hash(saltedPassword);
        DatabaseManager.getInstance(getApplicationContext()).addUser(username, salt, hashedSaltedPassword);

        etxtUsername.setText(R.string.userAddedNotification);
        etxtPassword.setText("");
        Toast.makeText(getApplicationContext(), "New user added.",
                Toast.LENGTH_LONG).show();
    }
}