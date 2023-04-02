package com.ocadotask.data;

import org.json.JSONArray;

import java.util.List;
import java.util.stream.IntStream;

public class Pickers {

    private Pickers() {
    }

    // The returned List is unmodifiable; calls to any mutator method
    // will always cause UnsupportedOperationException to be thrown.
    public static List<Picker> parse(JSONArray jsonArray) {
        return IntStream.range(0, jsonArray.length())
                .mapToObj(jsonArray::getString)
                .map(Picker::new)
                .toList();
    }
}
