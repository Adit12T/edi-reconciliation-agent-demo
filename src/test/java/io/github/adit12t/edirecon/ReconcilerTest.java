package io.github.adit12t.edirecon;

import io.github.adit12t.edirecon.Model.ClientResult;
import io.github.adit12t.edirecon.Model.Finding;
import io.github.adit12t.edirecon.Model.FindingType;
import io.github.adit12t.edirecon.Model.Report;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Runs the reconciliation on the sample data shipped with the repository. */
class ReconcilerTest {

    private Report reconcile(String lineOfBusiness) throws IOException {
        return new Reconciler().reconcile(Path.of("sample-data/env-a"), Path.of("sample-data/env-b"), lineOfBusiness);
    }

    @Test
    void comparesOnlyClientsEnabledInBothEnvironments() throws IOException {
        Report report = reconcile("medical");

        assertEquals(List.of("ALPHA", "BRAVO", "DELTA"),
                report.results().stream().map(result -> result.client().id()).toList());
        assertEquals(List.of("CHARLIE", "ECHO"),
                report.skipped().stream().map(skipped -> skipped.clientId()).toList());
    }

    @Test
    void reportsMissingExtraDuplicateAndChangedMembers() throws IOException {
        ClientResult alpha = reconcile("medical").results().get(0);

        assertEquals(5, alpha.referenceCount());
        assertEquals(6, alpha.candidateCount());
        assertEquals(List.of("DUPLICATE M1003", "CHANGED M1002", "MISSING M1004", "CHANGED M1005", "EXTRA M1099"),
                alpha.findings().stream().map(finding -> finding.type() + " " + finding.memberId()).toList());

        Finding dateChange = alpha.findings().get(1);
        assertTrue(dateChange.detail().contains("DTP*356*D8*20260101"));
        assertTrue(dateChange.detail().contains("DTP*356*D8*20260102"));
        assertTrue(dateChange.likelyCause().startsWith("Only dates differ"));
        assertTrue(alpha.findings().get(3).likelyCause().startsWith("Segment HD differs"));
    }

    @Test
    void flagsAClientWhoseFilesNeverArrived() throws IOException {
        ClientResult bravo = reconcile("medical").results().get(1);

        assertEquals(1, bravo.findings().size());
        assertEquals(FindingType.NO_FILES, bravo.findings().get(0).type());
    }

    @Test
    void reportsACleanMatch() throws IOException {
        Report medical = reconcile("medical");
        assertTrue(medical.results().get(2).clean());

        Report dental = reconcile("dental");
        assertEquals(2, dental.results().size());
        assertEquals(0, dental.findingCount());
    }
}
