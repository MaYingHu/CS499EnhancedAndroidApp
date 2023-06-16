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

package com.example.cs499enhancedandroidapp.notifications;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ToggleButton;

import com.example.cs499enhancedandroidapp.R;
import com.example.cs499enhancedandroidapp.main.MainActivity;
import com.example.cs499enhancedandroidapp.data.DatabaseManager;

/*
 * =====================================================
 * The Notification Activity
 * =====================================================
 */

public class NotificationActivity extends AppCompatActivity {

    private boolean notificationsEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        ToggleButton tbtnEnableNotifications;
        Button btnGoBack;

        notificationsEnabled = DatabaseManager.getInstance(getApplicationContext()).getNotificationStatus();

        tbtnEnableNotifications = (ToggleButton) findViewById(R.id.tbtnNotifications);
        tbtnEnableNotifications.setChecked(notificationsEnabled);
        tbtnEnableNotifications.setOnClickListener(l -> setNotificationStatus());

        btnGoBack = (Button) findViewById(R.id.btnGoBack);
        btnGoBack.setOnClickListener(l -> goBack());
    }

    private void goBack() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void setNotificationStatus() {

        notificationsEnabled = !notificationsEnabled;
        DatabaseManager.getInstance(getApplicationContext()).setNotificationStatus(notificationsEnabled);
    }
}