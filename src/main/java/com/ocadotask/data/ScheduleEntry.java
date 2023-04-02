package com.ocadotask.data;

import java.time.LocalTime;

public record ScheduleEntry(Picker picker, Order order, LocalTime startTime) {
    @Override
    public String toString() {
        return String.format("%s %s %s", picker.id(), order.id(), startTime);
    }
}
