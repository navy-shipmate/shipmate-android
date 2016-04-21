package com.ansonliu.shipmate;

import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.jar.Attributes;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        String versionName = "Unknown";
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = e.getLocalizedMessage();
        }

        String helpText = "SHIPMATE v"+versionName+"\n\n" +
                "Developed by\n"+
                "Anson Liu (github.com/ansonl)\n" +
                "Jeremy Jones (github.com/jjmiddy)\n" +
                "Nate Haynes (github.com/natejh)\n" +
                "Dan Iskandar (github.com/diskandan)\n" +
                "\nSource code available on GitHub (github.com/navy-shipmate)\n\n" +
                "Please send feedback to shipmatevan@gmail.com";

        ((TextView) findViewById(R.id.aboutTextView)).setText(helpText);
    }
}
