import com.ocadotask.data.Picker;
import com.ocadotask.data.Store;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

public class StoreTest {

    @Test
    public void parse_ValidJSONString_ValidStore() {
        String json = """
                {
                  "pickers": [
                    "P1",
                    "P2"
                  ],
                  "pickingStartTime": "09:00",
                  "pickingEndTime": "11:00"
                }""";

        Store store = Store.parse(json);

        Store expectedStore = new Store(
                List.of(new Picker("P1"), new Picker("P2")),
                LocalTime.parse("09:00"),
                LocalTime.parse("11:00")
        );

        assert store.equals(expectedStore);
    }
}
