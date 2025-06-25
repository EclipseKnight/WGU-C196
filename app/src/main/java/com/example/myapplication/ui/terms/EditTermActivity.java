package com.example.myapplication.ui.terms;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.StudentDatabase;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditTermActivity extends AppCompatActivity {

    private static final String TAG = "EditTermActivity";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d yyyy", Locale.US);

    private Button dateButton;
    private Button dateButton2;
    private StudentDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_term);

        db = new StudentDatabase(getApplicationContext());

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        EditText titleEdit = findViewById(R.id.termTitleTextEdit);
        titleEdit.setText(getIntent().getStringExtra("termName"));

        dateButton = findViewById(R.id.datePickerButton);
        dateButton2 = findViewById(R.id.datePickerButton2);

        setupDatePicker(dateButton, getIntent().getStringExtra("termStart"));
        setupDatePicker(dateButton2, getIntent().getStringExtra("termEnd"));

        Button saveButton = findViewById(R.id.editTermButton);
        saveButton.setOnClickListener(v -> {
            String title = titleEdit.getText().toString();
            String start = dateButton.getText().toString();
            String end = dateButton2.getText().toString();
            String termId = getIntent().getStringExtra("termId");

            executor.execute(() -> {
                db.updateTerm(termId, title, start, end);
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Term Updated!", Toast.LENGTH_SHORT).show();
                    launchTermDetailsActivity();
                });
            });
        });
    }

    private void setupDatePicker(Button button, String initialDate) {
        try {
            LocalDate date = LocalDate.parse(initialDate, dateFormatter);

            button.setText(initialDate);

            button.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {
                            LocalDate selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                            String formatted = selectedDate.format(dateFormatter);
                            button.setText(formatted);
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );

                datePickerDialog.show();
            });

        } catch (DateTimeParseException e) {
            Log.e(TAG, "Invalid date format: " + initialDate, e);
        }
    }



    private void launchTermDetailsActivity() {
        Intent intent = new Intent(this, TermDetailsActivity.class);

        // Re-pass all extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            intent.putExtras(extras);
        }

        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        Log.d(TAG, "onOptionsItemSelected: unknown menu item");
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
