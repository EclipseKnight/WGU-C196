package com.example.myapplication.ui.assessments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.StudentDatabase;
import com.example.myapplication.ui.courses.CourseDetailsActivity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddAssessmentActivity extends AppCompatActivity {

    private static final String TAG = "AddAssessmentActivity";

    private Button dateButtonStart;
    private Button dateButtonEnd;
    private Spinner typeSpinner;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d yyyy", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_assessment);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        dateButtonStart = findViewById(R.id.datePickerButton);
        dateButtonEnd = findViewById(R.id.datePickerButton2);
        typeSpinner = findViewById(R.id.spinner2);

        String today = LocalDate.now().format(dateFormatter);
        setupDatePicker(dateButtonStart, today);
        setupDatePicker(dateButtonEnd, today);

        setupSpinner();
        setupListeners();
    }

    private void setupDatePicker(Button button, String initialDate) {
        LocalDate date = LocalDate.parse(initialDate, dateFormatter);
        button.setText(initialDate);

        button.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());

            DatePickerDialog picker = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        LocalDate selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                        button.setText(selectedDate.format(dateFormatter));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            picker.show();
        });
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.assessment_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
    }

    private void setupListeners() {
        findViewById(R.id.addAssessmentButton).setOnClickListener(this::handleAddAssessment);
    }

    private void handleAddAssessment(View view) {
        EditText titleInput = findViewById(R.id.assessmentTitleText);
        String title = titleInput.getText().toString().trim();
        String startDate = dateButtonStart.getText().toString();
        String endDate = dateButtonEnd.getText().toString();
        String type = typeSpinner.getSelectedItem().toString();

        int courseId = getIntent().getIntExtra("course_id", -1);
        if (courseId == -1) {
            Toast.makeText(this, "Error: Invalid Course ID", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            try (StudentDatabase db = new StudentDatabase(getApplicationContext())) {
                db.addAssessment(title, startDate, endDate, type, courseId);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Assessment Added!", Toast.LENGTH_SHORT).show();
                    navigateToCourseDetails();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error adding assessment", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to add assessment", Toast.LENGTH_LONG).show()
                );
            }
        });

        Log.i(TAG, "Added assessment for course ID " + courseId);
    }

    private void navigateToCourseDetails() {
        Intent incoming = getIntent();
        Intent intent = new Intent(this, CourseDetailsActivity.class);

        String[] keys = {
                "termMonthValue", "termName", "termStart", "termEnd",
                "termNameAndDate", "termId",
                "courseMonthValue", "courseNameAndDate", "courseId", "courseOptionalNotes"
        };

        for (String key : keys) {
            intent.putExtra(key, incoming.getStringExtra(key));
        }

        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
