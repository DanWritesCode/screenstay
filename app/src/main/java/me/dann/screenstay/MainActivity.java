package me.dann.screenstay;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputFilter;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    MainActivity that = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handleSystemSettingsPermission();

        EditText hoursBox = findViewById(R.id.hours);
        EditText minutesBox = findViewById(R.id.minutes);
        hoursBox.setFilters(new InputFilter[]{new InputFilterMinMax("0", "24")});
        minutesBox.setFilters(new InputFilter[]{new InputFilterMinMax("0", "59")});

        Button button = findViewById(R.id.btn_apply);
        button.setOnClickListener(v -> {
            hideKeyboard(v);
            try {
                int timeout = parseInt(hoursBox.getText().toString()) * 60 * 60 + parseInt(minutesBox.getText().toString()) * 60;

                android.provider.Settings.System.putString(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, String.valueOf(timeout * 1000));
                updateCurrentTimeout();
            } catch (SecurityException se) {
                Toast.makeText(this, "Permissions error when setting screen timeout", Toast.LENGTH_SHORT).show();
                handleSystemSettingsPermission();
            } catch (Exception e) {
                Toast.makeText(this, "Other error setting screen timeout", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            // Do something in response to button click
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateCurrentTimeout();
    }

    private int parseInt(String str) {
        try {
            if (str.trim().isEmpty())
                return 0;
            else
                return Integer.parseInt(str);
        } catch (Exception e) {
            return 0;
        }
    }

    public void hideKeyboard(View view) {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch(Exception ignored) {
        }
    }

    private void updateCurrentTimeout() {
        TextView tv = findViewById(R.id.currentTimeoutStr);
        int ms = 0;
        try {
            ms = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (Settings.SettingNotFoundException e) {
            tv.setText("Unable to find screen timeout. Check your permissions.");
        }
        tv.setText("The current screen timeout is set to " + millisecondsToHoursAndMinutes(ms));
    }

    private String millisecondsToHoursAndMinutes(long ms) {
        int mins = (int) Math.floor(ms / 1000f / 60f);
        int hours = (int) Math.floor(mins / 60f);
        int minsLeft = mins % 60;
        return hours + " hours and " + minsLeft + " minutes";
    }

    private void handleSystemSettingsPermission() {
        if (!Settings.System.canWrite(getApplicationContext())) {
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage(getString(R.string.app_name) + " requires system settings permissions to update screen timeout")
                    .setNeutralButton("OK", (dialogInterface, i) -> askForSystemPermissions())
                    .show();
        }
    }

    private void askForSystemPermissions() {
        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + that.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}