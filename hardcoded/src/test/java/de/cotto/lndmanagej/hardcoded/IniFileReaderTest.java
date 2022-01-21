package de.cotto.lndmanagej.hardcoded;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class IniFileReaderTest {

    private static final String SECTION = "section";
    private static final String SECTION_2 = "another-section";

    @Test
    void file_does_not_exist() throws IOException {
        File file = createTempFile();
        boolean deleted = file.delete();
        IniFileReader iniFileReader = new IniFileReader(file.getPath());
        assertThat(iniFileReader.getValues(SECTION)).isEmpty();
        assertThat(deleted).isTrue();
    }

    @Test
    void path_does_not_exist() {
        IniFileReader iniFileReader = new IniFileReader("/blabla/this/does/not/exist/foo.conf");
        assertThat(iniFileReader.getValues(SECTION)).isEmpty();
    }

    @Test
    void empty_file() throws IOException {
        File file = createTempFile();
        IniFileReader iniFileReader = new IniFileReader(file.getPath());
        assertThat(iniFileReader.getValues(SECTION)).isEmpty();
    }

    @Test
    void section_without_values() throws IOException {
        File file = createTempFile();
        addLineToFile(file, "[" + SECTION + "]");
        IniFileReader iniFileReader = new IniFileReader(file.getPath());
        assertThat(iniFileReader.getValues(SECTION)).isEmpty();
    }

    @Test
    void section_with_value() throws IOException {
        File file = createTempFile();
        addLineToFile(file, "[" + SECTION + "]", "x=y");
        IniFileReader iniFileReader = new IniFileReader(file.getPath());
        assertThat(iniFileReader.getValues(SECTION)).isEqualTo(Map.of("x", Set.of("y")));
    }

    @Test
    void section_with_two_values() throws IOException {
        File file = createTempFile();
        addLineToFile(file, "[" + SECTION + "]", "x=y", "a=b");
        IniFileReader iniFileReader = new IniFileReader(file.getPath());
        assertThat(iniFileReader.getValues(SECTION)).isEqualTo(Map.of("x", Set.of("y"), "a", Set.of("b")));
    }

    @Test
    void two_sections_with_two_values() throws IOException {
        File file = createTempFile();
        addLineToFile(file, "[" + SECTION + "]", "x=y", "a=b", "[" + SECTION_2 + "]", "x=1", "a=2");
        IniFileReader iniFileReader = new IniFileReader(file.getPath());
        assertThat(iniFileReader.getValues(SECTION)).isEqualTo(Map.of("x", Set.of("y"), "a", Set.of("b")));
        assertThat(iniFileReader.getValues(SECTION_2)).isEqualTo(Map.of("x", Set.of("1"), "a", Set.of("2")));
    }

    @Test
    void section_with_two_values_for_key() throws IOException {
        File file = createTempFile();
        addLineToFile(file, "[" + SECTION + "]", "a=y", "a=b");
        IniFileReader iniFileReader = new IniFileReader(file.getPath());
        assertThat(iniFileReader.getValues(SECTION)).isEqualTo(Map.of("a", Set.of("y", "b")));
    }

    private void addLineToFile(File file, String... lines) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
        }
    }

    private File createTempFile() throws IOException {
        return File.createTempFile("hardcoded", "temp");
    }
}