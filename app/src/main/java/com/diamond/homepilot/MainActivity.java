package com.diamond.homepilot;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
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
    TextView txtTemp, txtHumidity, txtMotion;

    // Firebase Reference
    DatabaseReference homeRef;

    // Device States
    int lightState = 0;
    int fanState = 0;
    int buzzerState = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI
        btnLight = findViewById(R.id.btnLight);
        btnFan = findViewById(R.id.btnFan);
        btnBuzzer = findViewById(R.id.btnBuzzer);

        txtTemp = findViewById(R.id.txtTemp);
        txtHumidity = findViewById(R.id.txtHumidity);
        txtMotion = findViewById(R.id.txtMotion);

        // Firebase Reference
        homeRef = FirebaseDatabase.getInstance().getReference("HomePilot");

        // Button Listeners
        btnLight.setOnClickListener(v -> toggleLight());
        btnFan.setOnClickListener(v -> toggleFan());
        btnBuzzer.setOnClickListener(v -> toggleBuzzer());

        // Listen for sensor updates
        readSensorData();
    }

    // Toggle Light
    private void toggleLight() {
        lightState = (lightState == 0) ? 1 : 0;
        homeRef.child("light").setValue(lightState);

        if (lightState == 1) {
            btnLight.setText("💡 Light ON");
        } else {
            btnLight.setText("💡 Light OFF");
        }
    }

    // Toggle Fan
    private void toggleFan() {
        fanState = (fanState == 0) ? 1 : 0;
        homeRef.child("fan").setValue(fanState);

        if (fanState == 1) {
            btnFan.setText("🌀 Fan ON");
        } else {
            btnFan.setText("🌀 Fan OFF");
        }
    }

    // Toggle Buzzer
    private void toggleBuzzer() {
        buzzerState = (buzzerState == 0) ? 1 : 0;
        homeRef.child("buzzer").setValue(buzzerState);

        if (buzzerState == 1) {
            btnBuzzer.setText("🚨 Panic Alarm ON");
        } else {
            btnBuzzer.setText("🚨 Panic Alarm OFF");
        }
    }

    // Read Sensor Data from Firebase
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
                    if (motion.equals("1")) {
                        txtMotion.setText("Motion Detected");
                    } else {
                        txtMotion.setText("No Motion");
                    }
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
