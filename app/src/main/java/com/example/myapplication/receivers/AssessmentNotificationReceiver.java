package com.example.myapplication.receivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.myapplication.R;
import com.example.myapplication.ui.assessments.AssessmentDetailsActivity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

public class AssessmentNotificationReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID_ASSESSMENTS = "assessment_notification_channel";
    public static final String EXTRA_ASSESSMENT_TITLE = "assessmentTitleCopy";
    public static final String EXTRA_ASSESSMENT_START = "assessmentStartCopy";
    public static final String EXTRA_ASSESSMENT_END = "assessmentEndCopy";

    public static final String EXTRA_TERM_MONTH_VALUE = "termMonthValue";
    public static final String EXTRA_TERM_NAME = "termName";
    public static final String EXTRA_TERM_START = "termStart";
    public static final String EXTRA_TERM_END = "termEnd";
    public static final String EXTRA_TERM_NAME_AND_DATE = "termNameAndDate";
    public static final String EXTRA_TERM_ID = "termId";

    public static final String EXTRA_COURSE_MONTH_VALUE = "courseMonthValue";
    public static final String EXTRA_COURSE_NAME_AND_DATE = "courseNameAndDate";
    public static final String EXTRA_COURSE_ID = "courseId";
    public static final String EXTRA_COURSE_OPTIONAL_NOTES = "courseOptionalNotes";

    public static final String EXTRA_ASSESSMENT_MONTH_VALUE = "assessmentMonthValue";
    public static final String EXTRA_ASSESSMENT_ID = "assessmentId";
    public static final String EXTRA_ASSESSMENT_ORIGINAL_TITLE = "assessmentTitle";
    public static final String EXTRA_ASSESSMENT_ORIGINAL_START = "assessmentStart";
    public static final String EXTRA_ASSESSMENT_ORIGINAL_END = "assessmentEnd";
    public static final String EXTRA_ASSESSMENT_TYPE = "assessmentType";

    private static final String TAG = "AssessmentNotificationReceiver";

    private static int nextNotificationId = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        createNotificationChannel(context);

        String title = intent.getStringExtra(EXTRA_ASSESSMENT_TITLE);
        String start = intent.getStringExtra(EXTRA_ASSESSMENT_START);
        String end = intent.getStringExtra(EXTRA_ASSESSMENT_END);

        if (title == null || start == null || end == null) {
            Log.e(TAG, "Missing assessment details in intent extras. Cannot process notification.");
            return;
        }

        Log.i(TAG, "Assessment title: " + title);
        Log.i(TAG, "Assessment start: " + start);
        Log.i(TAG, "Assessment end: " + end);

        DateTimeFormatter inputFormatter = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("MMM d yyyy")
                .toFormatter(Locale.US);

        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = LocalDate.parse(start, inputFormatter);
            endDate = LocalDate.parse(end, inputFormatter);
        } catch (java.time.format.DateTimeParseException e) {
            Log.e(TAG, "Error parsing assessment dates: " + e.getMessage(), e);
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate sevenDaysBeforeStart = startDate.minusDays(7);
        LocalDate sevenDaysBeforeEnd = endDate.minusDays(7);

        boolean startSoon = today.isAfter(sevenDaysBeforeStart) && (today.isBefore(startDate) || today.isEqual(startDate));
        boolean endSoon = today.isAfter(sevenDaysBeforeEnd) && (today.isBefore(endDate) || today.isEqual(endDate));

        if (startSoon) {
            String body = title + " is starting within the next 7 days.";
            Intent openIntent = createAssessmentDetailsIntent(context, intent);
            showAssessmentNotification(context, title, body, "Upcoming Assessment", openIntent, generateNotificationId(intent, "start"));
        }

        if (endSoon) {
            String body = title + " is ending within the next 7 days.";
            Intent openIntent = createAssessmentDetailsIntent(context, intent);
            showAssessmentNotification(context, title, body, "Assessment Ending Soon", openIntent, generateNotificationId(intent, "end"));
        }
    }

    private Intent createAssessmentDetailsIntent(Context context, Intent sourceIntent) {
        Intent openIntent = new Intent(context, AssessmentDetailsActivity.class);

        openIntent.putExtra(EXTRA_TERM_MONTH_VALUE, sourceIntent.getStringExtra(EXTRA_TERM_MONTH_VALUE));
        openIntent.putExtra(EXTRA_TERM_NAME, sourceIntent.getStringExtra(EXTRA_TERM_NAME));
        openIntent.putExtra(EXTRA_TERM_START, sourceIntent.getStringExtra(EXTRA_TERM_START));
        openIntent.putExtra(EXTRA_TERM_END, sourceIntent.getStringExtra(EXTRA_TERM_END));
        openIntent.putExtra(EXTRA_TERM_NAME_AND_DATE, sourceIntent.getStringExtra(EXTRA_TERM_NAME_AND_DATE));
        openIntent.putExtra(EXTRA_TERM_ID, sourceIntent.getStringExtra(EXTRA_TERM_ID));

        openIntent.putExtra(EXTRA_COURSE_MONTH_VALUE, sourceIntent.getStringExtra(EXTRA_COURSE_MONTH_VALUE));
        openIntent.putExtra(EXTRA_COURSE_NAME_AND_DATE, sourceIntent.getStringExtra(EXTRA_COURSE_NAME_AND_DATE));
        openIntent.putExtra(EXTRA_COURSE_ID, sourceIntent.getStringExtra(EXTRA_COURSE_ID));
        openIntent.putExtra(EXTRA_COURSE_OPTIONAL_NOTES, sourceIntent.getStringExtra(EXTRA_COURSE_OPTIONAL_NOTES));

        openIntent.putExtra(EXTRA_ASSESSMENT_MONTH_VALUE, sourceIntent.getStringExtra(EXTRA_ASSESSMENT_MONTH_VALUE));
        openIntent.putExtra(EXTRA_ASSESSMENT_ID, sourceIntent.getIntExtra(EXTRA_ASSESSMENT_ID, 0));
        openIntent.putExtra(EXTRA_ASSESSMENT_ORIGINAL_TITLE, sourceIntent.getStringExtra(EXTRA_ASSESSMENT_ORIGINAL_TITLE));
        openIntent.putExtra(EXTRA_ASSESSMENT_ORIGINAL_START, sourceIntent.getStringExtra(EXTRA_ASSESSMENT_ORIGINAL_START));
        openIntent.putExtra(EXTRA_ASSESSMENT_ORIGINAL_END, sourceIntent.getStringExtra(EXTRA_ASSESSMENT_ORIGINAL_END));
        openIntent.putExtra(EXTRA_ASSESSMENT_TYPE, sourceIntent.getStringExtra(EXTRA_ASSESSMENT_TYPE));

        return openIntent;
    }

    private void showAssessmentNotification(Context context, String title, String body, String notificationContentTitle, Intent openIntent, int notificationId) {
        int requestCode = notificationId + (notificationContentTitle.hashCode() % 1000);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode, openIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID_ASSESSMENTS)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setContentTitle(notificationContentTitle)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setContentText(body)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setCategory(Notification.CATEGORY_REMINDER);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(notificationId, notificationBuilder.build());
        }
    }

    private int generateNotificationId(Intent intent, String type) {
        try {
            int assessmentId = intent.getIntExtra(EXTRA_ASSESSMENT_ID, 0);
            if (assessmentId != 0) { // If a valid ID is passed
                return assessmentId * 10 + (type.equals("start") ? 1 : 2);
            } else {
                Log.w(TAG, "Assessment ID not found or invalid. Using generic notification ID.");
                return getNextNotificationId();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error generating notification ID for assessment: " + e.getMessage(), e);
            return getNextNotificationId();
        }
    }

    private static synchronized int getNextNotificationId() {
        return nextNotificationId++;
    }

    private void createNotificationChannel(Context context) {
        CharSequence name = context.getResources().getString(R.string.channel_name_assessment);
        String description = context.getString(R.string.channel_description_assessment);
        int importance = NotificationManager.IMPORTANCE_HIGH; // Changed to HIGH for consistency with Course receiver
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID_ASSESSMENTS, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }
}