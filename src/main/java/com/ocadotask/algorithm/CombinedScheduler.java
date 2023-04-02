package com.ocadotask.algorithm;

import com.ocadotask.data.Order;
import com.ocadotask.data.ScheduleEntry;
import com.ocadotask.data.Store;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CombinedScheduler implements Scheduler {

    private final List<Scheduler> schedulers;
    private final SchedulerOptimizationCriteria criteria;

    public CombinedScheduler(SchedulerOptimizationCriteria criteria) {
        this.schedulers = new LinkedList<>();
        this.criteria = criteria;

        try {
            Scheduler cpSatScheduler = new CpSatScheduler(criteria);
            schedulers.add(cpSatScheduler);
        } catch (RuntimeException e) {
            // We can proceed without or-tools (no native library for this platform)
        }

        schedulers.add(new GeneticScheduler(criteria));
    }

    @Override
    public List<ScheduleEntry> calculateSchedule(List<Order> orders, Store store, int timeLimitInSeconds) {
        // Since we are using a time limit, we have to create a thread for each scheduler
        ExecutorService executorService = Executors.newCachedThreadPool();
        List<Future<List<ScheduleEntry>>> futures = new LinkedList<>();
        for (Scheduler scheduler : schedulers) {
            // orders and store are immutable, so we can safely pass them to the scheduler
            futures.add(executorService.submit(() -> scheduler.calculateSchedule(orders, store, timeLimitInSeconds)));
        }

        List<ScheduleEntry> bestSchedule = List.of();
        double bestFitness = Double.NEGATIVE_INFINITY;
        for (Future<List<ScheduleEntry>> future : futures) {
            try {
                List<ScheduleEntry> schedule = future.get();
                double fitness = calculateCommonFitness(schedule);
                if (fitness > bestFitness) {
                    bestFitness = fitness;
                    bestSchedule = schedule;
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException("Error while executing scheduler", e);
            }
        }

        executorService.shutdown();

        return bestSchedule;
    }

    private double calculateCommonFitness(List<ScheduleEntry> schedule) {
        double fitness = 0;
        for (ScheduleEntry entry : schedule) {
            fitness += switch (criteria) {
                case MAXIMIZE_COUNT -> 1;
                case MAXIMIZE_VALUE -> entry.order().value().doubleValue();
            };
        }
        return fitness;
    }

}
