package com.example.myapplication.ui.courses;

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
import com.example.myapplication.ui.dashboard.DashboardActivity;
import com.example.myapplication.ui.terms.TermDetailsActivity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity for adding a course to a selected term.
 */
public class AddCourseActivity extends AppCompatActivity {

    private static final String TAG = "AddCourseActivity";

    private Button startDateButton;
    private Button endDateButton;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // Use the same date format as in your term activities, e.g. "MMM d yyyy"
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        startDateButton = findViewById(R.id.datePickerButton);
        endDateButton = findViewById(R.id.datePickerButton2);

        // Initialize buttons with today's date in formatted style
        String today = LocalDate.now().format(dateFormatter);

        setupDatePicker(startDateButton, today);
        setupDatePicker(endDateButton, today);

        setupSpinner();
        setupListeners();
    }

    private void setupSpinner() {
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.course_status, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
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

    private void setupListeners() {
        Button addCourseButton = findViewById(R.id.addCourseButton);
        addCourseButton.setOnClickListener(this::handleAddCourse);
    }

    private void handleAddCourse(View view) {
        EditText courseTitleInput = findViewById(R.id.courseTitleText);
        EditText instructorNameInput = findViewById(R.id.courseInstructorName);
        EditText instructorPhoneInput = findViewById(R.id.courseInstructorPhone);
        EditText instructorEmailInput = findViewById(R.id.courseInstructorEmail);
        EditText optionalNoteInput = findViewById(R.id.courseOptionalNoteText);
        Spinner statusSpinner = findViewById(R.id.spinner);

        String courseTitle = courseTitleInput.getText().toString().trim();
        String startDate = startDateButton.getText().toString();
        String endDate = endDateButton.getText().toString();
        String status = statusSpinner.getSelectedItem().toString();
        String instructorName = instructorNameInput.getText().toString().trim();
        String instructorPhone = instructorPhoneInput.getText().toString().trim();
        String instructorEmail = instructorEmailInput.getText().toString().trim();
        String note = optionalNoteInput.getText().toString().trim();
        int termId = DashboardActivity.term_id;

        executorService.execute(() -> {
            try (StudentDatabase db = new StudentDatabase(getApplicationContext())) {
                db.addCourse(courseTitle, startDate, endDate, status, instructorName, instructorPhone, instructorEmail, termId, note);

                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Course Added!", Toast.LENGTH_SHORT).show();
                    navigateToTermDetails();
                });

                Log.i(TAG, "handleAddCourse: Course added to database. Term ID: " + termId);
            } catch (Exception e) {
                Log.e(TAG, "Error adding course", e);
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error adding course", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void navigateToTermDetails() {
        Intent incoming = getIntent();
        Intent intent = new Intent(this, TermDetailsActivity.class);

        String[] keys = {
                "monthValue", "termName", "termStart", "termEnd", "termNameAndDate", "termId"
        };

        for (String key : keys) {
            intent.putExtra(key, incoming.getStringExtra(key));
        }

        startActivity(intent);
        finish();
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
