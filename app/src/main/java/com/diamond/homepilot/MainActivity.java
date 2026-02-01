package com.diamond.homepilot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    // UI Components
    Button btnLight, btnFan, btnBuzzer;
    SwitchCompat switchSecurity;
    TextView txtTemp, txtHumidity, txtMotion;

    // Firebase Reference
    DatabaseReference homeRef;

    // Device States
    int lightState = 0;
    int fanState = 0;
    int buzzerState = 0;
    int securityState = 0;

    boolean isUserToggle = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI
        btnLight = findViewById(R.id.btnLight);
        btnFan = findViewById(R.id.btnFan);
        btnBuzzer = findViewById(R.id.btnBuzzer);
        switchSecurity = findViewById(R.id.switchSecurity);

        txtTemp = findViewById(R.id.txtTemp);
        txtHumidity = findViewById(R.id.txtHumidity);
        txtMotion = findViewById(R.id.txtMotion);

        // Firebase Reference
        homeRef = FirebaseDatabase.getInstance().getReference("HomePilot");

        // Button Listeners
        btnLight.setOnClickListener(v -> toggleLight());
        btnFan.setOnClickListener(v -> toggleFan());
        btnBuzzer.setOnClickListener(v -> toggleBuzzer());

        // Secure Home Switch Listener
        switchSecurity.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isUserToggle) return;
            homeRef.child("security").setValue(isChecked ? 1 : 0);
        });

        // Listen for sensor + state updates
        readSensorData();
    }

    // Toggle Light
    private void toggleLight() {
        lightState = (lightState == 0) ? 1 : 0;
        homeRef.child("light").setValue(lightState);
    }

    // Toggle Fan
    private void toggleFan() {
        fanState = (fanState == 0) ? 1 : 0;
        homeRef.child("fan").setValue(fanState);
    }

    // Toggle Buzzer
    private void toggleBuzzer() {
        buzzerState = (buzzerState == 0) ? 1 : 0;
        homeRef.child("buzzer").setValue(buzzerState);
    }

    // Read Sensor & State Data from Firebase
    private void readSensorData() {

        homeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Temperature
                if (snapshot.child("temperature").exists()) {
                    String temp = snapshot.child("temperature").getValue().toString();
                    txtTemp.setText(temp + " °C");
                }

                // Humidity
                if (snapshot.child("humidity").exists()) {
                    String humidity = snapshot.child("humidity").getValue().toString();
                    txtHumidity.setText(humidity + " %");
                }

                // Motion
                if (snapshot.child("motion").exists()) {
                    String motion = snapshot.child("motion").getValue().toString();
                    txtMotion.setText(motion.equals("1") ? "Motion Detected" : "No Motion");
                }

                // Light state sync
                if (snapshot.child("light").exists()) {
                    lightState = Integer.parseInt(snapshot.child("light").getValue().toString());
                    btnLight.setText(lightState == 1 ? "💡 Light ON" : "💡 Light OFF");
                }

                // Fan state sync
                if (snapshot.child("fan").exists()) {
                    fanState = Integer.parseInt(snapshot.child("fan").getValue().toString());
                    btnFan.setText(fanState == 1 ? "🌀 Fan ON" : "🌀 Fan OFF");
                }

                // Buzzer state sync
                if (snapshot.child("buzzer").exists()) {
                    buzzerState = Integer.parseInt(snapshot.child("buzzer").getValue().toString());
                    btnBuzzer.setText(buzzerState == 1 ? "🚨 Panic Alarm ON" : "🚨 Panic Alarm OFF");
                }

                // Security state sync
                if (snapshot.child("security").exists()) {
                    securityState = Integer.parseInt(snapshot.child("security").getValue().toString());

                    isUserToggle = false;
                    switchSecurity.setChecked(securityState == 1);
                    isUserToggle = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this,
                        "Firebase Error: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
