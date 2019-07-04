package net.rdrei.android.buildtimetracker.reporters

import java.text.DateFormat
import java.text.SimpleDateFormat;

public class TrueTimeProvider {

    public long getCurrentTime() {
        return System.currentTimeMillis();
    }

    String getCurrentDate() {
        long timestamp = getCurrentTime()
        TimeZone timeZone = TimeZone.getTimeZone("UTC")
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS'Z'")
        dateFormat.setTimeZone(timeZone)
        return dateFormat.format(new Date(timestamp))
    }
}