package com.example.myapplication.receivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.myapplication.R;
import com.example.myapplication.ui.courses.CourseDetailsActivity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

public class CourseNotificationReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID_COURSES = "course_notification_channel";
    public static final String EXTRA_COURSE_TITLE = "courseTitleCopy";
    public static final String EXTRA_COURSE_START = "courseStartCopy";
    public static final String EXTRA_COURSE_END = "courseEndCopy";

    private static final String TAG = "CourseNotificationReceiver";

    private static int nextNotificationId = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        createNotificationChannel(context);

        String title = intent.getStringExtra(EXTRA_COURSE_TITLE);
        String start = intent.getStringExtra(EXTRA_COURSE_START);
        String end = intent.getStringExtra(EXTRA_COURSE_END);

        if (title == null || start == null || end == null) {
            Log.e(TAG, "Missing course details in intent extras. Cannot process notification.");
            return;
        }

        Log.i(TAG, "Course title: " + title);
        Log.i(TAG, "Course start: " + start);
        Log.i(TAG, "Course end: " + end);

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
            Log.e(TAG, "Error parsing course dates: " + e.getMessage(), e);
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate sevenDaysBeforeStart = startDate.minusDays(7);
        LocalDate sevenDaysBeforeEnd = endDate.minusDays(7);

        boolean startSoon = today.isAfter(sevenDaysBeforeStart) && (today.isBefore(startDate) || today.isEqual(startDate));
        boolean endSoon = today.isAfter(sevenDaysBeforeEnd) && (today.isBefore(endDate) || today.isEqual(endDate));

        if (startSoon) {
            String body = title + " is starting within the next 7 days.";
            Intent openIntent = createCourseDetailsIntent(context, intent);
            showCourseNotification(context, title, body, "Upcoming Course", openIntent, generateNotificationId(intent, "start"));
        }

        if (endSoon) {
            String body = title + " is ending within the next 7 days.";
            Intent openIntent = createCourseDetailsIntent(context, intent);
            showCourseNotification(context, title, body, "Course Ending Soon", openIntent, generateNotificationId(intent, "end"));
        }
    }

    private Intent createCourseDetailsIntent(Context context, Intent sourceIntent) {
        Intent openIntent = new Intent(context, CourseDetailsActivity.class);

        openIntent.putExtra("termMonthValue", sourceIntent.getStringExtra("termMonthValue"));
        openIntent.putExtra("termName", sourceIntent.getStringExtra("termName"));
        openIntent.putExtra("termStart", sourceIntent.getStringExtra("termStart"));
        openIntent.putExtra("termEnd", sourceIntent.getStringExtra("termEnd"));
        openIntent.putExtra("termNameAndDate", sourceIntent.getStringExtra("termNameAndDate"));
        openIntent.putExtra("termId", sourceIntent.getStringExtra("termId"));

        openIntent.putExtra("courseMonthValue", sourceIntent.getStringExtra("courseMonthValue"));
        openIntent.putExtra("courseNameAndDate", sourceIntent.getStringExtra("courseNameAndDate"));
        openIntent.putExtra("courseId", sourceIntent.getStringExtra("courseId"));
        openIntent.putExtra("courseOptionalNotes", sourceIntent.getStringExtra("courseOptionalNotes"));

        return openIntent;
    }

    private void showCourseNotification(Context context, String title, String body, String notificationContentTitle, Intent openIntent, int notificationId) {
        int requestCode = notificationId + (notificationContentTitle.hashCode() % 1000);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode, openIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID_COURSES)
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
            int courseId = Integer.parseInt(intent.getStringExtra("courseId"));
            return courseId * 10 + (type.equals("start") ? 1 : 2);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid course ID for notification ID generation: " + intent.getStringExtra("courseId"));
            return getNextNotificationId();
        }
    }

    private static synchronized int getNextNotificationId() {
        return nextNotificationId++;
    }

    private void createNotificationChannel(Context context) {
        CharSequence name = context.getResources().getString(R.string.channel_name_course);
        String description = context.getString(R.string.channel_description_course);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID_COURSES, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }
}