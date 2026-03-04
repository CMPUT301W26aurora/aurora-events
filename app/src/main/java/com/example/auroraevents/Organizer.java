package com.example.auroraevents;
import android.os.Build;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Organizer extends User {
    String eventTitle;
    String eventDate;
    LocalDate eventTime;
    LocalDateTime eventRegisterStartTime;
    LocalDateTime eventRegisterEndTime;
    String eventDescription;

    public Organizer() {
        super();
        setRole(User.ROLE_ORGANIZER);
    }
    private void CreateEvent(String title, String date, String time, String startTime, String endTime, String description) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.eventTitle = title;
            this.eventDate = date;
            this.eventTime = LocalDate.from(LocalDateTime.parse(time));
            this.eventRegisterStartTime = LocalDate.from(LocalDateTime.parse(startTime)).atStartOfDay();
            this.eventRegisterEndTime = LocalDate.from(LocalDateTime.parse(endTime)).atStartOfDay();
            this.eventDescription = description;
        }
    }
}
