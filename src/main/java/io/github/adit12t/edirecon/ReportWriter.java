package io.github.adit12t.edirecon;

import io.github.adit12t.edirecon.Model.ClientResult;
import io.github.adit12t.edirecon.Model.Finding;
import io.github.adit12t.edirecon.Model.Report;
import io.github.adit12t.edirecon.Model.SkippedClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Writes the reconciliation report as Markdown. */
public class ReportWriter {

    public void write(Report report, Path outDir) throws IOException {
        Files.createDirectories(outDir);
        StringBuilder md = new StringBuilder("# EDI reconciliation report\n\n");
        md.append("- **Reference environment:** ").append(report.referenceName()).append('\n');
        md.append("- **Candidate environment:** ").append(report.candidateName()).append('\n');
        md.append("- **Line of business:** ").append(report.lineOfBusiness()).append('\n');
        md.append("- **Clients compared:** ").append(report.results().size()).append('\n');
        md.append("- **Differences found:** ").append(report.findingCount()).append("\n\n");

        md.append("| Client | Members in ").append(report.referenceName()).append(" | Members in ")
                .append(report.candidateName()).append(" | Result |\n|---|---|---|---|\n");
        for (ClientResult result : report.results()) {
            md.append("| ").append(result.client().id()).append(" | ").append(result.referenceCount()).append(" | ")
                    .append(result.candidateCount()).append(" | ")
                    .append(result.clean() ? "match" : result.findings().size() + " difference(s)").append(" |\n");
        }

        for (ClientResult result : report.results()) {
            if (result.clean()) {
                continue;
            }
            md.append("\n## ").append(result.client().id()).append(": ").append(result.client().name()).append("\n\n");
            md.append("| Type | Member | Detail | Likely cause |\n|---|---|---|---|\n");
            for (Finding finding : result.findings()) {
                md.append("| ").append(finding.type()).append(" | ").append(finding.memberId()).append(" | ")
                        .append(cell(finding.detail())).append(" | ").append(cell(finding.likelyCause())).append(" |\n");
            }
        }

        if (!report.skipped().isEmpty()) {
            md.append("\n## Skipped clients\n\n");
            for (SkippedClient skipped : report.skipped()) {
                md.append("- ").append(skipped.clientId()).append(": ").append(skipped.reason()).append('\n');
            }
        }
        md.append("\n_Likely causes are rule-based hints for a developer to confirm._\n");
        Files.writeString(outDir.resolve("report.md"), md.toString());
    }

    private static String cell(String text) {
        return text.replace("|", "\\|");
    }
}
