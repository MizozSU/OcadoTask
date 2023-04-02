package com.ocadotask.algorithm;

import com.ocadotask.data.Order;
import com.ocadotask.data.ScheduleEntry;
import com.ocadotask.data.Store;
import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Function;

import static io.jenetics.engine.Limits.byExecutionTime;
import static io.jenetics.engine.Limits.bySteadyFitness;

// Unfortunately that's the only way to implement a genotype with two chromosomes of different types
// See: https://github.com/jenetics/jenetics/blob/master/jenetics.example/src/main/java/io/jenetics/example/MixedGenotype.java
@SuppressWarnings({"rawtypes", "unchecked"})
public class GeneticScheduler implements Scheduler {

    private record OrderEntry(Order order, long pickingDurationInSeconds, long startDeadlineInSeconds) {
    }

    Function<Order, Double> orderScoreFunction;

    public GeneticScheduler(SchedulerOptimizationCriteria criteria) {
        switch (criteria) {
            case MAXIMIZE_COUNT -> {
                orderScoreFunction = x -> 1.0;
            }
            case MAXIMIZE_VALUE -> {
                orderScoreFunction = x -> x.value().doubleValue();
            }
            default -> {
                throw new IllegalArgumentException("Unknown criteria: " + criteria);
            }
        }
    }

    @Override
    public List<ScheduleEntry> calculateSchedule(List<Order> orders, Store store, int timeLimitInSeconds) {
        List<OrderEntry> orderEntries = new ArrayList<>();

        // Precompute picking duration and start deadline for each order to avoid recomputing it in fitness function
        orders.forEach(order -> {
            long startDeadlineInSeconds = Math.min(order.completeBy().toSecondOfDay() - order.pickingTime().getSeconds(), store.pickingEndTime().toSecondOfDay());
            orderEntries.add(new OrderEntry(order, order.pickingTime().getSeconds(), startDeadlineInSeconds));
        });

        // Genotype consists of two chromosomes:
        // 1. Picker mapping chromosome - i-th position is an index of the picker assigned to the order with index i in orderMappingChromosome
        // 2. Order mapping chromosome - i-th position is an index of the order in orders list. This chromosome is a permutation of integers from 0
        // to orders.size() - 1 it describes the sequence in which orders should be picked by a picker.
        Genotype Encoding = Genotype.of(
                (Chromosome) IntegerChromosome.of(0, store.pickers().size(), orders.size()),
                (Chromosome) PermutationChromosome.ofInteger(0, orders.size())
        );
        Function<Genotype, Double> fitnessFunction = getFitnessFunction(orderEntries, store.pickingStartTime().toSecondOfDay());

        Engine engine = Engine.builder(fitnessFunction, Encoding)
                .optimize(Optimize.MAXIMUM)
                .populationSize(100)
                .alterers(
                        new Mutator<>(0.15),
                        new SinglePointCrossover<>(0.2))
                .survivorsSelector(new TournamentSelector<>(5))
                .offspringSelector(new RouletteWheelSelector())
                .build();

        Phenotype best = (Phenotype) engine.stream()
                .limit(byExecutionTime(Duration.ofSeconds(timeLimitInSeconds)))
                .limit(bySteadyFitness(300))
                .collect(EvolutionResult.toBestPhenotype());

        return createScheduleFromPhenotype(orderEntries, store, best);
    }

    // We use double for fitness and long for time because of 4x speedup over BigDecimal/LocaleTime
    private Function<Genotype, Double> getFitnessFunction(List<OrderEntry> orderEntries, long pickingStartTimeInSeconds) {
        return gt -> {
            double fitness = 0;

            IntegerChromosome pickerMappingChromosome = (IntegerChromosome) gt.get(0);
            PermutationChromosome<Integer> orderMappingChromosome = (PermutationChromosome<Integer>) gt.get(1);

            for (int pickerMappingIndex = 0; pickerMappingIndex < pickerMappingChromosome.length(); pickerMappingIndex++) {
                long currentTime = pickingStartTimeInSeconds;
                for (int orderMappingIndex = 0; orderMappingIndex < orderMappingChromosome.length(); orderMappingIndex++) {
                    if (pickerMappingChromosome.get(orderMappingIndex).intValue() != pickerMappingIndex) {
                        continue;
                    }

                    int orderIndex = orderMappingChromosome.get(orderMappingIndex).allele();
                    OrderEntry orderEntry = orderEntries.get(orderIndex);
                    if (currentTime <= orderEntry.startDeadlineInSeconds) {
                        currentTime += orderEntry.pickingDurationInSeconds;
                        fitness += orderScoreFunction.apply(orderEntry.order);
                    }
                }
            }
            return fitness;
        };
    }

    private List<ScheduleEntry> createScheduleFromPhenotype(List<OrderEntry> orderEntries, Store store, Phenotype phenotype) {
        IntegerChromosome pickerMappingChromosome = (IntegerChromosome) phenotype.genotype().get(0);
        PermutationChromosome<Integer> orderMappingChromosome = (PermutationChromosome) phenotype.genotype().get(1);

        List<ScheduleEntry> scheduleEntries = new ArrayList<>();
        for (int pickerMappingIndex = 0; pickerMappingIndex < pickerMappingChromosome.length(); pickerMappingIndex++) {
            long currentTime = store.pickingStartTime().toSecondOfDay();
            for (int orderMappingIndex = 0; orderMappingIndex < orderMappingChromosome.length(); orderMappingIndex++) {
                if (pickerMappingChromosome.get(orderMappingIndex).intValue() != pickerMappingIndex) {
                    continue;
                }

                int orderIndex = orderMappingChromosome.get(orderMappingIndex).allele();
                OrderEntry orderEntry = orderEntries.get(orderIndex);
                if (currentTime <= orderEntry.startDeadlineInSeconds) {
                    scheduleEntries.add(new ScheduleEntry(store.pickers().get(pickerMappingIndex), orderEntry.order, LocalTime.ofSecondOfDay(currentTime)));
                    currentTime += orderEntry.pickingDurationInSeconds;
                }
            }
        }

        return scheduleEntries;
    }
}
