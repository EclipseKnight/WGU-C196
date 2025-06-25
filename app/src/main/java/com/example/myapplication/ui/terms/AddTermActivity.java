package com.example.myapplication.ui.terms;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.data.StudentDatabase;
import com.example.myapplication.ui.dashboard.DashboardActivity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddTermActivity extends AppCompatActivity {

    private static final String TAG = "AddTermActivity";

    private Button startDateButton;
    private Button endDateButton;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d yyyy", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_term);

        startDateButton = findViewById(R.id.datePickerButton);
        endDateButton = findViewById(R.id.datePickerButton2);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        String today = LocalDate.now().format(formatter);
        setupDatePicker(startDateButton, today);
        setupDatePicker(endDateButton, today);

        findViewById(R.id.addTermButton).setOnClickListener(v -> addTerm());
    }

    private void setupDatePicker(Button button, String initialDate) {
        LocalDate date = LocalDate.parse(initialDate, formatter);
        button.setText(initialDate);

        button.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());

            DatePickerDialog picker = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        LocalDate selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                        button.setText(selectedDate.format(formatter));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            picker.show();
        });
    }

    private void addTerm() {
        EditText termTitleEditText = findViewById(R.id.termTitleText);
        String title = termTitleEditText.getText().toString().trim();
        String startDate = startDateButton.getText().toString();
        String endDate = endDateButton.getText().toString();

        executorService.execute(() -> {
            try (StudentDatabase db = new StudentDatabase(getApplicationContext())) {
                db.addTerm(title, startDate, endDate);

                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Term Added!", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Term added to database");
                    startActivity(new Intent(this, DashboardActivity.class));
                });
            } catch (Exception e) {
                Log.e(TAG, "Error adding term", e);
                runOnUiThread(() ->
                        Toast.makeText(getApplicationContext(), "Error adding term", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
