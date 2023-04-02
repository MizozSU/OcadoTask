package com.ocadotask;

import com.ocadotask.algorithm.*;
import com.ocadotask.data.Order;
import com.ocadotask.data.Orders;
import com.ocadotask.data.ScheduleEntry;
import com.ocadotask.data.Store;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    // The time limit is set to 11 seconds for the sake of the demo. Keep in mind that this limit is an approximate value.
    public static final int TIME_LIMIT_IN_SECONDS = 11;
    // The optimization criteria is hard-coded because of the run specifications.
    //public static final SchedulerOptimizationCriteria OPTIMIZATION_CRITERIA = SchedulerOptimizationCriteria.MAXIMIZE_COUNT;
    public static final SchedulerOptimizationCriteria OPTIMIZATION_CRITERIA = SchedulerOptimizationCriteria.MAXIMIZE_VALUE;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar <jar_file_path> <store_file_path> <orders_file_path>");
            return;
        }

        String storeFilePath = args[0];
        String ordersFilePath = args[1];

        try {
            List<Order> orders = Orders.parse(Files.readString(Path.of(ordersFilePath)));
            Store store = Store.parse(Files.readString(Path.of(storeFilePath)));
            Scheduler combinedScheduler = new CombinedScheduler(OPTIMIZATION_CRITERIA);
            List<ScheduleEntry> schedule = combinedScheduler.calculateSchedule(orders, store, TIME_LIMIT_IN_SECONDS);
            schedule.forEach(System.out::println);
        } catch (IOException e) {
            System.err.println("Error reading input files: " + e.getMessage());
        }
    }
}
