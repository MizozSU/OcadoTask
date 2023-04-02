package com.ocadotask.algorithm;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import com.ocadotask.data.Order;
import com.ocadotask.data.ScheduleEntry;
import com.ocadotask.data.Store;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CpSatScheduler implements Scheduler {

    private final SchedulerOptimizationCriteria criteria;
    private static final int MAX_VALUE_DECIMALS_MULTIPLIER = 100;
    private static final int PROBLEM_SIZE_THRESHOLD = 1_000;

    public CpSatScheduler(SchedulerOptimizationCriteria criteria) {
        switch (criteria) {
            case MAXIMIZE_COUNT, MAXIMIZE_VALUE -> {
                this.criteria = criteria;
            }
            default -> {
                throw new IllegalArgumentException("Unknown criteria: " + criteria);
            }
        }

        try {
            Loader.loadNativeLibraries();
        } catch (UnsatisfiedLinkError | RuntimeException e) {
            throw new RuntimeException("Failed to load native libraries", e);
        }
    }

    @Override
    public List<ScheduleEntry> calculateSchedule(List<Order> orders, Store store, int timeLimitInSeconds) {
        // The problem is too large to be solved in a reasonable amount of time using Constraint Programming
        // We have a time limit, but we might not be able to create a model in time
        // This limit could be increased, but I have no idea on what hardware the code will be run
        if (orders.size() > PROBLEM_SIZE_THRESHOLD) {
            return List.of();
        }

        CpModel model = new CpModel();

        int numOrders = orders.size();
        int numPickers = store.pickers().size();

        BoolVar[][] isAssigned = new BoolVar[numOrders][numPickers];

        // Each order can only be assigned to one picker
        for (int orderIndex = 0; orderIndex < numOrders; orderIndex++) {
            List<Literal> orderVars = new ArrayList<>();
            for (int pickerIndex = 0; pickerIndex < numPickers; pickerIndex++) {
                isAssigned[orderIndex][pickerIndex] = model.newBoolVar("order_" + orderIndex + "_picker_" + pickerIndex + "_is_assigned");
                orderVars.add(isAssigned[orderIndex][pickerIndex]);
            }
            model.addAtMostOne(orderVars);
        }

        long pickingStartTime = store.pickingStartTime().toSecondOfDay();
        long pickingEndTime = store.pickingEndTime().toSecondOfDay();

        Map<String, IntervalVar> intervalMap = new HashMap<>();

        // Each picker can only pick one order at a time (no overlap)
        for (int pickerIndex = 0; pickerIndex < numPickers; pickerIndex++) {
            List<IntervalVar> intervals = new ArrayList<>();

            for (int orderIndex = 0; orderIndex < numOrders; orderIndex++) {
                Order order = orders.get(orderIndex);
                long orderCompleteBy = order.completeBy().toSecondOfDay();
                long orderPickingTime = order.pickingTime().getSeconds();

                IntVar start = model.newIntVar(pickingStartTime, orderCompleteBy - orderPickingTime,
                        "order_" + orderIndex + "_picker_" + pickerIndex + "_start");
                IntVar end = model.newIntVar(pickingStartTime + orderPickingTime, Math.min(orderCompleteBy, pickingEndTime),
                        "order_" + orderIndex + "_picker_" + pickerIndex + "_end");
                IntervalVar interval = model.newOptionalIntervalVar(start, model.newConstant(orderPickingTime), end, isAssigned[orderIndex][pickerIndex],
                        "order_" + orderIndex + "_picker_" + pickerIndex + "_interval");
                intervals.add(interval);
                intervalMap.put("order_" + orderIndex + "_picker_" + pickerIndex + "_interval", interval);
            }
            model.addNoOverlap(intervals);
        }

        // Objective function
        LinearExprBuilder objectiveBuilder = LinearExpr.newBuilder();
        for (int orderIndex = 0; orderIndex < numOrders; orderIndex++) {
            for (int pickerIndex = 0; pickerIndex < numPickers; pickerIndex++) {
                if (this.criteria == SchedulerOptimizationCriteria.MAXIMIZE_COUNT)
                    objectiveBuilder.addTerm(isAssigned[orderIndex][pickerIndex], 1);
                else if (this.criteria == SchedulerOptimizationCriteria.MAXIMIZE_VALUE) {
                    objectiveBuilder.addTerm(isAssigned[orderIndex][pickerIndex],
                            (int) (orders.get(orderIndex).value().doubleValue() * MAX_VALUE_DECIMALS_MULTIPLIER));
                }
            }
        }

        model.maximize(objectiveBuilder.build());

        CpSolver solver = new CpSolver();
        solver.getParameters().setMaxTimeInSeconds(timeLimitInSeconds);
        CpSolverStatus status = solver.solve(model);

        // Only if the problem is feasible or optimal, we can extract the solution
        if (status == CpSolverStatus.FEASIBLE || status == CpSolverStatus.OPTIMAL) {
            List<ScheduleEntry> schedule = new ArrayList<>();
            for (int pickerIndex = 0; pickerIndex < numPickers; pickerIndex++) {
                for (int orderIndex = 0; orderIndex < numOrders; orderIndex++) {
                    if (solver.value(isAssigned[orderIndex][pickerIndex]) == 1) {
                        long value = solver.value(intervalMap.get("order_" + orderIndex + "_picker_" + pickerIndex + "_interval").getStartExpr());
                        LocalTime startTime = LocalTime.ofSecondOfDay(value);
                        schedule.add(new ScheduleEntry(store.pickers().get(pickerIndex), orders.get(orderIndex), startTime));
                    }
                }
            }
            return schedule;
        }

        return List.of();
    }
}
