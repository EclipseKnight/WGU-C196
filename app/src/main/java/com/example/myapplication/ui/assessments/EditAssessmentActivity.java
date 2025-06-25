package com.example.myapplication.ui.assessments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.StudentDatabase;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditAssessmentActivity extends AppCompatActivity {

    private static final String TAG = "EditAssessmentActivity";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d yyyy", Locale.US);

    private Button dateButton;
    private Button dateButton2;
    private EditText editTitle;
    private Spinner typeSpinner;
    private StudentDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_assessment);

        db = new StudentDatabase(this);

        dateButton = findViewById(R.id.datePickerButton);
        dateButton2 = findViewById(R.id.datePickerButton2);
        editTitle = findViewById(R.id.assessmentTitleText);
        typeSpinner = findViewById(R.id.spinner2);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Set initial values
        String startDate = getIntent().getStringExtra("assessmentStart");
        String endDate = getIntent().getStringExtra("assessmentEnd");

        editTitle.setText(getIntent().getStringExtra("assessmentTitle"));
        setupDatePicker(dateButton, startDate);
        setupDatePicker(dateButton2, endDate);

        setupSpinner();

        findViewById(R.id.editAssessmentButton).setOnClickListener(view -> updateAssessment());
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.assessment_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);

        String type = getIntent().getStringExtra("assessmentType");
        int position = Arrays.asList(getResources().getStringArray(R.array.assessment_type)).indexOf(type);
        typeSpinner.setSelection(position);
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

    private void updateAssessment() {
        int id = getIntent().getIntExtra("assessmentId", 0);
        String title = editTitle.getText().toString();
        String start = dateButton.getText().toString();
        String end = dateButton2.getText().toString();
        String type = typeSpinner.getSelectedItem().toString();

        executor.execute(() -> {
            db.updateAssessment(String.valueOf(id), title, start, end, type);
            runOnUiThread(() -> {
                Toast.makeText(this, "Assessment Updated!", Toast.LENGTH_SHORT).show();
                launchViewAssessmentActivity();
            });
        });
    }

    private void launchViewAssessmentActivity() {
        Intent intent = new Intent(this, AssessmentDetailsActivity.class);
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
        Log.w(TAG, "Unhandled menu item");
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
