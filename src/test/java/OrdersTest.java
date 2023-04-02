import com.ocadotask.data.Order;
import com.ocadotask.data.Orders;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

public class OrdersTest {

    @Test
    public void parse_ValidJSONString_ValidList() {
        String json = """
                [
                  {
                    "orderId": "order-1",
                    "orderValue": "1.00",
                    "pickingTime": "PT15M",
                    "completeBy": "09:15"
                  },
                  {
                    "orderId": "order-2",
                    "orderValue": "2.00",
                    "pickingTime": "PT30M",
                    "completeBy": "09:30"
                  },
                  {
                    "orderId": "order-3",
                    "orderValue": "3.00",
                    "pickingTime": "PT15M",
                    "completeBy": "10:00"
                  },
                  {
                    "orderId": "order-4",
                    "orderValue": "4.00",
                    "pickingTime": "PT15M",
                    "completeBy": "10:00"
                  },
                  {
                    "orderId": "order-5",
                    "orderValue": "5.00",
                    "pickingTime": "PT60M",
                    "completeBy": "10:15"
                  },
                  {
                    "orderId": "order-6",
                    "orderValue": "6.00",
                    "pickingTime": "PT30M",
                    "completeBy": "10:45"
                  },
                  {
                    "orderId": "order-7",
                    "orderValue": "7.00",
                    "pickingTime": "PT45M",
                    "completeBy": "11:00"
                  }
                ]""";

        List<Order> orders = Orders.parse(json);
        List<Order> expectedOrders = List.of(
                new Order("order-1", new BigDecimal("1.00"), Duration.ofMinutes(15), LocalTime.parse("09:15")),
                new Order("order-2", new BigDecimal("2.00"), Duration.ofMinutes(30), LocalTime.parse("09:30")),
                new Order("order-3", new BigDecimal("3.00"), Duration.ofMinutes(15), LocalTime.parse("10:00")),
                new Order("order-4", new BigDecimal("4.00"), Duration.ofMinutes(15), LocalTime.parse("10:00")),
                new Order("order-5", new BigDecimal("5.00"), Duration.ofMinutes(60), LocalTime.parse("10:15")),
                new Order("order-6", new BigDecimal("6.00"), Duration.ofMinutes(30), LocalTime.parse("10:45")),
                new Order("order-7", new BigDecimal("7.00"), Duration.ofMinutes(45), LocalTime.parse("11:00"))
        );

        assert orders.equals(expectedOrders);
    }
}
