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

package com.example.cs499enhancedandroidapp.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ToggleButton;

import java.util.List;
import java.util.Arrays;

import com.example.cs499enhancedandroidapp.R;
import com.example.cs499enhancedandroidapp.data.DatabaseManager;
import com.example.cs499enhancedandroidapp.notifications.NotificationActivity;

/*
 * =====================================================
 * The Main Activity with the Inventory View
 * =====================================================
 */
public class MainActivity extends AppCompatActivity {

    private final String CHANNEL_ID_INVENTORY_EMPTY = "channel_inventory_empty";
    private final int NOTIFICATION_ID = 0;

    private EditText qn, dn;
    private List<TextView> quantities;
    private List<TextView> descriptions;

    private int[] viewableItems;
    private int databaseStartIndex;
    private int highestId;
    private long itemCount;
    private static boolean notificationsEnabled;

    private Button btnMoveUpList;
    private Button btnMoveDownList;
    private TextView txtEnableNotifications;
    private ToggleButton tbtnNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView q1, q2, q3, q4, q5, q6, q7, q8, q9, q10, d1, d2, d3, d4, d5, d6, d7, d8, d9, d10;

        q1 = findViewById(R.id.txtQuantity1);
        q2 = findViewById(R.id.txtQuantity2);
        q3 = findViewById(R.id.txtQuantity3);
        q4 = findViewById(R.id.txtQuantity4);
        q5 = findViewById(R.id.txtQuantity5);
        q6 = findViewById(R.id.txtQuantity6);
        q7 = findViewById(R.id.txtQuantity7);
        q8 = findViewById(R.id.txtQuantity8);
        q9 = findViewById(R.id.txtQuantity9);
        q10 = findViewById(R.id.txtQuantity10);
        qn = findViewById(R.id.newItemQuantity);

        d1 = findViewById(R.id.txtDescription1);
        d2 = findViewById(R.id.txtDescription2);
        d3 = findViewById(R.id.txtDescription3);
        d4 = findViewById(R.id.txtDescription4);
        d5 = findViewById(R.id.txtDescription5);
        d6 = findViewById(R.id.txtDescription6);
        d7 = findViewById(R.id.txtDescription7);
        d8 = findViewById(R.id.txtDescription8);
        d9 = findViewById(R.id.txtDescription9);
        d10 = findViewById(R.id.txtDescription10);
        dn = findViewById(R.id.newItemDescription);

        quantities = Arrays.asList(q1, q2, q3, q4, q5, q6, q7, q8, q9, q10);
        descriptions = Arrays.asList(d1, d2, d3, d4, d5, d6, d7, d8, d9, d10);
        viewableItems = new int[10];

        itemCount = DatabaseManager.getInstance(getApplicationContext()).countEntries();
        databaseStartIndex = 1;

        btnMoveUpList = (Button) findViewById(R.id.btnMoveUpList);
        btnMoveDownList = (Button) findViewById(R.id.btnMoveDownList);
        txtEnableNotifications = findViewById(R.id.enableNotifications);

        btnMoveUpList.setOnClickListener(l -> moveUpList());
        btnMoveDownList.setOnClickListener(l -> moveDownList());
        txtEnableNotifications.setText(setNotificationsText());
        txtEnableNotifications.setOnClickListener(l -> toNotifications());

        View inflater = getLayoutInflater().inflate(R.layout.activity_notification, null, true);
        tbtnNotifications = inflater.findViewById(R.id.tbtnNotifications);
        notificationsEnabled = tbtnNotifications.isChecked();

        createInventoryEmptyNotificationChannel();
        refresh();
    }

    private void getViewableItems(int startIndex) {

        Arrays.fill(viewableItems, 0);

        int i = 0;
        int j = startIndex;

        while (j <= highestId && i < 10) {
            if (DatabaseManager.getInstance(getApplicationContext()).getDescriptionById(Integer.toString(j)).length() > 0) {
                viewableItems[i] = j;
                ++i;
            }
            ++j;
        }
    }

    private void moveUpList() {

        int newValue = databaseStartIndex;
        int shift = -5;

        while (newValue > 0 && shift < 0) {
            --newValue;
            if (DatabaseManager.getInstance(getApplicationContext()).getDescriptionById(Integer.toString(newValue)).length() > 0) {
                ++shift;
            }
        }

        databaseStartIndex = newValue;

        refresh();
    }

    private void moveDownList() {

        int newValue = databaseStartIndex;
        int shift = 5;
        int highestId = DatabaseManager.getInstance(getApplicationContext()).getHighestId();

        while (highestId - newValue >= 10 && shift > 0) {
            ++newValue;
            if (DatabaseManager.getInstance(getApplicationContext()).getDescriptionById(Integer.toString(newValue)).length() > 0) {
                --shift;
            }
        }

        databaseStartIndex = newValue;

        refresh();
    }

    private void refresh() {

        int i = 0;
        int c;

        itemCount = DatabaseManager.getInstance(getApplicationContext()).countEntries();
        highestId = DatabaseManager.getInstance(getApplicationContext()).getHighestId();
        getViewableItems(databaseStartIndex);

        for (c = 0; c < 10; ++c) {
            quantities.get(c).setText("");
            descriptions.get(c).setText("");
        }

        c = 1;

        while (c <= 10 && i <= itemCount && i < viewableItems.length) {

            int id = viewableItems[i];

            String qText = DatabaseManager.getInstance(getApplicationContext()).getQuantityById(Integer.toString(id));
            String dText = DatabaseManager.getInstance(getApplicationContext()).getDescriptionById(Integer.toString(id));

            if (dText.length() > 0) {
                if (qText.length() > 18) {
                    qText = qText.substring(0,18);
                }
                if (dText.length() > 18) {
                    dText = dText.substring(0,18);
                }
                quantities.get(c-1).setText(qText);
                descriptions.get(c-1).setText(dText);
                c++;
            }
            ++i;
        }
    }

    /*
     * Method adds an item to the database
     * @param view - the View
     */
    public void addItem(View view) {
        String description = dn.getText().toString();
        String quantity = qn.getText().toString();

        if (DatabaseManager.getInstance(getApplicationContext()).itemExists(description)) {
            DatabaseManager.getInstance(getApplicationContext()).updateInventoryItem(description, quantity);
        }
        else {
            DatabaseManager.getInstance(getApplicationContext()).addInventoryItem(description, quantity);
        }

        refresh();
        if (quantity.equals(String.valueOf(0))) {
            createInventoryEmptyNotification(description);
        }

        dn.setText("ADD NEW ITEM");
        qn.setText("");
    }

    /*
     * Method deletes an item from the database
     * @param view - the View
     */
    public void deleteItem(View view) {

        int id = 0;
        String description = "";

        switch (view.getId()) {
            case R.id.btnDelete1:
                id = viewableItems[0];
                description = descriptions.get(0).getText().toString();
                break;
            case R.id.btnDelete2:
                id = viewableItems[1];
                description = descriptions.get(1).getText().toString();
                break;
            case R.id.btnDelete3:
                id = viewableItems[2];
                description = descriptions.get(2).getText().toString();
                break;
            case R.id.btnDelete4:
                id = viewableItems[3];
                description = descriptions.get(3).getText().toString();
                break;
            case R.id.btnDelete5:
                id = viewableItems[4];
                description = descriptions.get(4).getText().toString();
                break;
            case R.id.btnDelete6:
                id = viewableItems[5];
                description = descriptions.get(5).getText().toString();
                break;
            case R.id.btnDelete7:
                id = viewableItems[6];
                description = descriptions.get(6).getText().toString();
                break;
            case R.id.btnDelete8:
                id = viewableItems[7];
                description = descriptions.get(7).getText().toString();
                break;
            case R.id.btnDelete9:
                id = viewableItems[8];
                description = descriptions.get(8).getText().toString();
                break;
            case R.id.btnDelete10:
                id = viewableItems[9];
                description = descriptions.get(9).getText().toString();
                break;
        }
        DatabaseManager.getInstance(getApplicationContext()).deleteInventoryItem(id);
        createInventoryEmptyNotification(description);
        refresh();
    }

    private void toNotifications() {
        Intent intent = new Intent(this, NotificationActivity.class);
        startActivity(intent);
    }

    private String setNotificationsText() {
        if (DatabaseManager.getInstance(getApplicationContext()).getNotificationStatus()) {
            return getString(R.string.txtEnableNotifications);
        }
        else {
            return getString(R.string.txtDisableNotifications);
        }
    }

    private void createInventoryEmptyNotificationChannel() {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        CharSequence name = "inventoryEmptyNotificationChannel";
        String description = "Notify user that stock of an item has reached zero.";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID_INVENTORY_EMPTY, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void createInventoryEmptyNotification(String text) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_INVENTORY_EMPTY)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(getString(R.string.notificationHeader))
                .setContentText(getString(R.string.notificationMessage1) + text + getString(R.string.notificationMessage2))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}