package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.example.myapplication.data.StudentDatabase;
import com.example.myapplication.ui.dashboard.DashboardActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static int numAlert = 0; // Initialize explicitly

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_home);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(view -> {
            // Initialize DB properly and close it to avoid leaks
            try (StudentDatabase sdb = new StudentDatabase(this)) {
                sdb.getWritableDatabase();
            } catch (Exception e) {
                Log.e(TAG, "Database initialization failed", e);
            }

            // Start DashboardActivity
            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}
