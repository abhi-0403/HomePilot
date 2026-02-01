package com.diamond.homepilot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.graphics.Color;
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
    TextView txtFire, txtGas;
    TextView txtLastMotion, txtLastGas, txtLastFire;

    // Firebase Reference
    DatabaseReference homeRef;

    // Device States
    int lightState = 0;
    int fanState = 0;
    int buzzerState = 0;
    int securityState = 0;

    // Gas values
    int gasStatus = 0;
    int gasLevel = 0;

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
        txtFire = findViewById(R.id.txtFire);
        txtGas = findViewById(R.id.txtGas);

        txtLastMotion = findViewById(R.id.txtLastMotion);
        txtLastGas = findViewById(R.id.txtLastGas);
        txtLastFire = findViewById(R.id.txtLastFire);

        // Firebase Reference
        homeRef = FirebaseDatabase.getInstance().getReference("HomePilot");

        // Button listeners
        btnLight.setOnClickListener(v -> toggleLight());
        btnFan.setOnClickListener(v -> toggleFan());
        btnBuzzer.setOnClickListener(v -> toggleBuzzer());

        // Secure Home switch
        switchSecurity.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isUserToggle) return;
            homeRef.child("security").setValue(isChecked ? 1 : 0);
        });

        readSensorData();
    }

    private void toggleLight() {
        lightState = (lightState == 0) ? 1 : 0;
        homeRef.child("light").setValue(lightState);
    }

    private void toggleFan() {
        fanState = (fanState == 0) ? 1 : 0;
        homeRef.child("fan").setValue(fanState);
    }

    private void toggleBuzzer() {
        buzzerState = (buzzerState == 0) ? 1 : 0;
        homeRef.child("buzzer").setValue(buzzerState);
    }

    private void readSensorData() {

        homeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Temperature
                if (snapshot.child("temperature").exists()) {
                    txtTemp.setText(snapshot.child("temperature").getValue() + " °C");
                }

                // Humidity
                if (snapshot.child("humidity").exists()) {
                    txtHumidity.setText(snapshot.child("humidity").getValue() + " %");
                }

                // Motion
                if (snapshot.child("motion").exists()) {
                    int motion = snapshot.child("motion").getValue(Integer.class);
                    txtMotion.setText(motion == 1 ? "Motion Detected" : "No Motion");
                }

                // Fire
                if (snapshot.child("fire").exists()) {
                    int fire = snapshot.child("fire").getValue(Integer.class);
                    txtFire.setText(fire == 1 ? "🔥 FIRE DETECTED" : "Safe");
                    txtFire.setTextColor(fire == 1 ? Color.RED : Color.parseColor("#388E3C"));
                }

                // -------- GAS STATUS (UPDATED) --------
                if (snapshot.child("gasStatus").exists()) {
                    gasStatus = snapshot.child("gasStatus").getValue(Integer.class);
                }

                if (snapshot.child("gas_Level").exists()) {
                    gasLevel = snapshot.child("gas_Level").getValue(Integer.class);
                }

                if (gasStatus == 1 || gasLevel > 700) {
                    txtGas.setText("⛽ GAS LEAK!\nLevel: " + gasLevel);
                    txtGas.setTextColor(Color.RED);
                } else {
                    txtGas.setText("Normal\nLevel: " + gasLevel);
                    txtGas.setTextColor(Color.parseColor("#2E7D32"));
                }
                // -------------------------------------

                // Last detected logs
                if (snapshot.child("last_motion").exists()) {
                    txtLastMotion.setText("Motion: " + snapshot.child("last_motion").getValue());
                }

                if (snapshot.child("last_gas").exists()) {
                    txtLastGas.setText("Gas: " + snapshot.child("last_gas").getValue());
                }

                if (snapshot.child("last_fire").exists()) {
                    txtLastFire.setText("Fire: " + snapshot.child("last_fire").getValue());
                }

                // Sync control states
                if (snapshot.child("light").exists()) {
                    lightState = snapshot.child("light").getValue(Integer.class);
                    btnLight.setText(lightState == 1 ? "💡 Light ON" : "💡 Light OFF");
                }

                if (snapshot.child("fan").exists()) {
                    fanState = snapshot.child("fan").getValue(Integer.class);
                    btnFan.setText(fanState == 1 ? "🌀 Fan ON" : "🌀 Fan OFF");
                }

                if (snapshot.child("buzzer").exists()) {
                    buzzerState = snapshot.child("buzzer").getValue(Integer.class);
                    btnBuzzer.setText(buzzerState == 1 ? "🚨 Panic Alarm ON" : "🚨 Panic Alarm OFF");
                }

                if (snapshot.child("security").exists()) {
                    securityState = snapshot.child("security").getValue(Integer.class);
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
