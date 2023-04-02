package com.ocadotask.data;

import org.json.JSONObject;

import java.time.LocalTime;
import java.util.List;

public record Store(List<Picker> pickers, LocalTime pickingStartTime, LocalTime pickingEndTime) {

    public static Store parse(String json) {
        JSONObject jsonObject = new JSONObject(json);
        return new Store(
                Pickers.parse(jsonObject.getJSONArray("pickers")),
                LocalTime.parse(jsonObject.getString("pickingStartTime")),
                LocalTime.parse(jsonObject.getString("pickingEndTime"))
        );
    }
}
