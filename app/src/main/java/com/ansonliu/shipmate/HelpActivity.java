package com.ansonliu.shipmate;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        String helpText = "Request a pickup: Tap the pickup button at the bottom of the screen.\n" +
                "Phone call is automatically initiated to the SHIPMATE duty phone so we can verify you are a real person.\n\n" +
                "Cancel a pickup: Tap the pickup button again.\n\n" +
                "Connection Error indicates that another device may be using SHIPMATE with your phone number. It may also mean that you have no internet.\n\n" +
                "Call SHIPMATE duty phone directly by pressing the green call button.\n\n" +
                "For feedback and additional help, please email shipmatevan@gmail.com.";

        ((TextView) findViewById(R.id.helpTextView)).setText(helpText);
    }
}
