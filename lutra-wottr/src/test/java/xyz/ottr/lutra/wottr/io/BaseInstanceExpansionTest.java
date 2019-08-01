package xyz.ottr.lutra.wottr.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import xyz.ottr.lutra.wottr.util.ModelIO;

@RunWith(Parameterized.class)
public class BaseInstanceExpansionTest {

    @Parameterized.Parameters(name = "{index}: {0}, {1}")
    public static List<String[]> data() throws IOException {

        List<String[]> data = new ArrayList<>();

        Path root = Paths.get("src",  "test", "resources", "baseinstances");
        Path inFolder = root.resolve("in");
        Path outFolder = root.resolve("out");

        data.add(new String[]{ inFolder.resolve("test1.ttl").toString(), outFolder.resolve("test1.ttl").toString() });
        return data;
    }

    private String input;
    private String output;

    public BaseInstanceExpansionTest(String input, String output) {
        this.input = input;
        this.output = output;
    }

    @Test
    public void shouldBeIsomorphic() {

        ModelUtils.testIsomorphicModels(
            ModelUtils.getOTTRParsedRDFModel(this.input),
            ModelIO.readModel(this.output));
    }
}
