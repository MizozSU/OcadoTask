import com.ocadotask.algorithm.CombinedScheduler;
import com.ocadotask.algorithm.Scheduler;
import com.ocadotask.algorithm.SchedulerOptimizationCriteria;
import com.ocadotask.data.Order;
import com.ocadotask.data.Orders;
import com.ocadotask.data.ScheduleEntry;
import com.ocadotask.data.Store;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CombinedSchedulerTest {
    @Test
    public void calculateSchedule_ProblemMaximizeCount_ReturnsSchedule() {
        Scheduler scheduler = new CombinedScheduler(SchedulerOptimizationCriteria.MAXIMIZE_COUNT);
        List<Order> orders = Orders.parse(
                """
                                [
                                  {
                                    "orderId": "order-1",
                                    "orderValue": "0.00",
                                    "pickingTime": "PT45M",
                                    "completeBy": "10:00"
                                  },
                                  {
                                    "orderId": "order-2",
                                    "orderValue": "0.00",
                                    "pickingTime": "PT30M",
                                    "completeBy": "09:30"
                                  }
                                ]
                        """);
        Store store = Store.parse(
                """
                                {
                                  "pickers": [
                                    "P1"
                                  ],
                                  "pickingStartTime": "09:00",
                                  "pickingEndTime": "10:00"
                                }
                        """
        );
        List<ScheduleEntry> scheduleEntries = scheduler.calculateSchedule(orders, store, 10);
        assertTrue(TestUtils.isValidSchedule(scheduleEntries, store));
        assertEquals(1, TestUtils.getScheduleOrderFitness(scheduleEntries, store), 0.01);
    }

    @Test
    public void calculateSchedule_ProblemMaximizeValue_ReturnsSchedule() {
        Scheduler scheduler = new CombinedScheduler(SchedulerOptimizationCriteria.MAXIMIZE_VALUE);
        List<Order> orders = Orders.parse(
                """
                                [
                                  {
                                    "orderId": "order-1",
                                    "orderValue": "5.00",
                                    "pickingTime": "PT15M",
                                    "completeBy": "09:15"
                                  },
                                  {
                                    "orderId": "order-2",
                                    "orderValue": "5.00",
                                    "pickingTime": "PT30M",
                                    "completeBy": "10:00"
                                  },
                                  {
                                    "orderId": "order-3",
                                    "orderValue": "10.00",
                                    "pickingTime": "PT45M",
                                    "completeBy": "10:00"
                                  },
                                  {
                                    "orderId": "order-4",
                                    "orderValue": "20.00",
                                    "pickingTime": "PT45M",
                                    "completeBy": "09:45"
                                  },
                                  {
                                    "orderId": "order-5",
                                    "orderValue": "50.00",
                                    "pickingTime": "PT0S",
                                    "completeBy": "10:00"
                                  }
                                ]
                        """);
        Store store = Store.parse(
                """
                                {
                                  "pickers": [
                                    "P1",
                                    "P2"
                                  ],
                                  "pickingStartTime": "09:00",
                                  "pickingEndTime": "10:00"
                                }
                        """
        );
        List<ScheduleEntry> scheduleEntries = scheduler.calculateSchedule(orders, store, 10);
        assertTrue(TestUtils.isValidSchedule(scheduleEntries, store));
        assertEquals(85, TestUtils.getScheduleValueFitness(scheduleEntries, store), 0.01);
    }

    @Test
    public void calculateSchedule_ProblemMaximizeCount_ReturnsEmptySchedule() {
        Scheduler scheduler = new CombinedScheduler(SchedulerOptimizationCriteria.MAXIMIZE_COUNT);
        List<Order> orders = Orders.parse(
                """
                                [
                                  {
                                    "orderId": "order-1",
                                    "orderValue": "0.00",
                                    "pickingTime": "PT15M",
                                    "completeBy": "09:15"
                                  },
                                ]
                        """);
        Store store = Store.parse(
                """
                                {
                                  "pickers": [
                                    "P1",
                                    "P2"
                                  ],
                                  "pickingStartTime": "15:00",
                                  "pickingEndTime": "16:00"
                                }
                        """
        );
        List<ScheduleEntry> scheduleEntries = scheduler.calculateSchedule(orders, store, 10);
        assertTrue(scheduleEntries.isEmpty());
    }
}
