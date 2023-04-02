package com.ocadotask.data;

import org.json.JSONArray;

import java.util.List;
import java.util.stream.IntStream;

public class Orders {
    private Orders() {
    }

    // The returned List is unmodifiable; calls to any mutator method
    // will always cause UnsupportedOperationException to be thrown.
    public static List<Order> parse(String json) {
        JSONArray jsonArray = new JSONArray(json);
        return IntStream.range(0, jsonArray.length())
                .mapToObj(jsonArray::getJSONObject)
                .map(Order::parse)
                .toList();
    }
}