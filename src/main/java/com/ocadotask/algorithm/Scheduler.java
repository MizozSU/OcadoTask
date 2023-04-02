package com.ocadotask.algorithm;

import com.ocadotask.data.Order;
import com.ocadotask.data.ScheduleEntry;
import com.ocadotask.data.Store;

import java.util.List;

public interface Scheduler {

    List<ScheduleEntry> calculateSchedule(List<Order> orders, Store store, int timeLimitInSeconds);
}
