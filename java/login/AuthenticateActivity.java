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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cs499enhancedandroidapp.R;
import com.example.cs499enhancedandroidapp.data.DatabaseManager;
import com.example.cs499enhancedandroidapp.main.MainActivity;


/*
 * =====================================================
 * the Authenticate Login Activity
 * =====================================================
 */

public class AuthenticateActivity extends AppCompatActivity {

    private EditText etxtAuthenticationCode;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    String phoneNo;
    String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticate);

        etxtAuthenticationCode = findViewById(R.id.etxtAuthenticationCode);
        Button btnAuthenticate = (Button) findViewById(R.id.btnAuthenticate);
        Button btnGoBack = (Button) findViewById(R.id.btnGoBack);
        Button btnSendCode = (Button) findViewById(R.id.btnSendCode);

        etxtAuthenticationCode.setOnClickListener(l -> etxtAuthenticationCode.setText(""));
        btnAuthenticate.setOnClickListener(l -> handleAuthentication());
        btnGoBack.setOnClickListener(l -> goBack());
        btnSendCode.setOnClickListener(l -> sendSMSMessage());

        DatabaseManager.getInstance(getApplicationContext()).setPasscode();

        sendSMSMessage();
    }

    private void handleAuthentication() {
        String passcode = etxtAuthenticationCode.getText().toString();

        if (DatabaseManager.getInstance(getApplicationContext()).authenticateSMS(passcode)) {
            DatabaseManager.getInstance(getApplicationContext()).setPasscode();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            etxtAuthenticationCode.setText(R.string.incorrectPasscodeError);
        }
    }

    private void goBack() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    /*
     * method sends an SMS message to the user with a temporary six-digit passcode
     */
    protected void sendSMSMessage() {
        phoneNo = "6505551212"; //getPhoneNumber;
        message = String.valueOf(DatabaseManager.getInstance(getApplicationContext()).getPasscode());

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNo, null, message, null, null);
                    Toast.makeText(getApplicationContext(), "SMS sent.",
                            Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "SMS failed, please try again.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
    }
}