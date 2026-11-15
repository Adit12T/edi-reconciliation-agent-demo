package io.github.adit12t.edirecon;

import io.github.adit12t.edirecon.Model.ClientResult;
import io.github.adit12t.edirecon.Model.Finding;
import io.github.adit12t.edirecon.Model.Report;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Entry point: compares a line of business across two environments and writes a report. */
public class EdiReconciler {

    public static void main(String[] args) throws IOException {
        Map<String, String> options = parse(args);
        Path reference = Path.of(options.getOrDefault("reference", "sample-data/env-a"));
        Path candidate = Path.of(options.getOrDefault("candidate", "sample-data/env-b"));
        String lineOfBusiness = options.getOrDefault("lob", "medical");
        Path outDir = Path.of(options.getOrDefault("out", "out"));

        Report report = new Reconciler().reconcile(reference, candidate, lineOfBusiness);
        new ReportWriter().write(report, outDir);

        for (ClientResult result : report.results()) {
            System.out.printf("%-8s %s%n", result.client().id(),
                    result.clean() ? "match" : result.findings().size() + " difference(s)");
            for (Finding finding : result.findings()) {
                System.out.printf("  %-9s %-6s %s%n", finding.type(), finding.memberId(), finding.detail());
            }
        }
        report.skipped().forEach(skipped ->
                System.out.printf("%-8s skipped: %s%n", skipped.clientId(), skipped.reason()));
        System.out.println("Report: " + outDir.resolve("report.md"));
    }

    private static Map<String, String> parse(String[] args) {
        Map<String, String> options = new HashMap<>();
        for (int i = 0; i + 1 < args.length; i += 2) {
            if (!args[i].startsWith("--")) {
                throw new IllegalArgumentException("Expected an option like --lob, got: " + args[i]);
            }
            options.put(args[i].substring(2), args[i + 1]);
        }
        return options;
    }
}
