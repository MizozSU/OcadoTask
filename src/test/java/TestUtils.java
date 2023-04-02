import com.ocadotask.data.Order;
import com.ocadotask.data.Picker;
import com.ocadotask.data.ScheduleEntry;
import com.ocadotask.data.Store;

import java.time.LocalTime;
import java.util.*;

public class TestUtils {
    private TestUtils() {
    }

    protected static boolean isValidSchedule(List<ScheduleEntry> schedule, Store store) {
        Set<Order> seenOrders = new HashSet<>();
        for (ScheduleEntry entry : schedule) {
            if (seenOrders.contains(entry.order())) {
                return false;
            }
            seenOrders.add(entry.order());
        }

        Map<Picker, List<ScheduleEntry>> pickerToOrders = new HashMap<>();
        for (ScheduleEntry entry : schedule) {
            pickerToOrders.computeIfAbsent(entry.picker(), k -> new ArrayList<>()).add(entry);
        }

        for (Map.Entry<Picker, List<ScheduleEntry>> entry : pickerToOrders.entrySet()) {
            LocalTime currentTime = store.pickingStartTime();
            List<ScheduleEntry> pickerSchedule = entry.getValue();
            pickerSchedule.sort(Comparator.comparing(ScheduleEntry::startTime).thenComparing(o -> o.order().pickingTime()));
            for (ScheduleEntry scheduleEntry : pickerSchedule) {
                if (scheduleEntry.startTime().isBefore(currentTime)) {
                    return false;
                }
                currentTime = currentTime.plus(scheduleEntry.order().pickingTime());
            }
            if (currentTime.isAfter(store.pickingEndTime())) {
                return false;
            }
        }

        return true;
    }

    protected static double getScheduleOrderFitness(List<ScheduleEntry> schedule, Store store) {
        double fitness = 0;
        for (ScheduleEntry entry : schedule) {
            fitness += 1;
        }
        return fitness;
    }

    protected static double getScheduleValueFitness(List<ScheduleEntry> schedule, Store store) {
        double fitness = 0;
        for (ScheduleEntry entry : schedule) {
            fitness += entry.order().value().doubleValue();
        }
        return fitness;
    }
}
