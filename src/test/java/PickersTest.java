import com.ocadotask.data.Picker;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;
import com.ocadotask.data.Pickers;

import java.util.List;

public class PickersTest {

    @Test
    void parse_ValidJSONArray_ReturnsListOfPickers() {
        JSONArray jsonArray = new JSONArray(
                """
                        [
                            "P1",
                            "P2",
                            "P3"
                        ]
                        """);

        List<Picker> pickers = Pickers.parse(jsonArray);
        List<Picker> expected = List.of(
                new Picker("P1"),
                new Picker("P2"),
                new Picker("P3")
        );

        assert pickers.equals(expected);
    }
}
