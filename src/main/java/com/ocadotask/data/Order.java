package com.ocadotask.data;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;

public record Order(String id, BigDecimal value, Duration pickingTime, LocalTime completeBy) {

    public static Order parse(JSONObject json) {
        return new Order(
                json.getString("orderId"),
                json.getBigDecimal("orderValue"),
                Duration.parse(json.getString("pickingTime")),
                LocalTime.parse(json.getString("completeBy"))
        );
    }
}
