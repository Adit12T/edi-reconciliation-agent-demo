package io.github.adit12t.edirecon;

import io.github.adit12t.edirecon.Model.ClientResult;
import io.github.adit12t.edirecon.Model.Finding;
import io.github.adit12t.edirecon.Model.Report;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Entry point: compares a line of business across two environments and writes a report. */
public class EdiReconciler {

    private static final Set<String> OPTIONS = Set.of("reference", "candidate", "lob", "out");
    private static final String USAGE =
            "Usage: EdiReconciler [--reference DIR] [--candidate DIR] [--lob LINE_OF_BUSINESS] [--out DIR]";

    public static void main(String[] args) {
        try {
            run(parse(args));
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println(USAGE);
            System.exit(2);
        } catch (NoSuchFileException e) {
            System.err.println("File or folder not found: " + e.getFile());
            System.exit(2);
        } catch (IOException e) {
            System.err.println("Could not read or write files: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void run(Map<String, String> options) throws IOException {
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

    /** Reads "--name value" pairs; rejects unknown options and options without a value. */
    static Map<String, String> parse(String[] args) {
        Map<String, String> options = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            String name = args[i].startsWith("--") ? args[i].substring(2) : null;
            if (name == null || !OPTIONS.contains(name)) {
                throw new IllegalArgumentException("Unknown option: " + args[i]);
            }
            if (i + 1 >= args.length) {
                throw new IllegalArgumentException("Option " + args[i] + " needs a value");
            }
            options.put(name, args[i + 1]);
        }
        return options;
    }
}
