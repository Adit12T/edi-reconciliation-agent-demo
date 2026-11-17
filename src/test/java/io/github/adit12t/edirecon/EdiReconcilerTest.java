package io.github.adit12t.edirecon;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EdiReconcilerTest {

    @Test
    void readsOptionPairs() {
        assertEquals(Map.of("lob", "dental", "out", "build/report"),
                EdiReconciler.parse(new String[] {"--lob", "dental", "--out", "build/report"}));
        assertTrue(EdiReconciler.parse(new String[0]).isEmpty());
    }

    @Test
    void rejectsUnknownOptionsAndMissingValues() {
        assertEquals("Unknown option: --bogus", assertThrows(IllegalArgumentException.class,
                () -> EdiReconciler.parse(new String[] {"--bogus", "x"})).getMessage());
        assertEquals("Unknown option: dental", assertThrows(IllegalArgumentException.class,
                () -> EdiReconciler.parse(new String[] {"dental"})).getMessage());
        assertEquals("Option --lob needs a value", assertThrows(IllegalArgumentException.class,
                () -> EdiReconciler.parse(new String[] {"--lob"})).getMessage());
    }
}
