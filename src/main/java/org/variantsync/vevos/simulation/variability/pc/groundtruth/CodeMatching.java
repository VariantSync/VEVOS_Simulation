package org.variantsync.vevos.simulation.variability.pc.groundtruth;

import org.variantsync.functjonal.Lazy;
import org.variantsync.functjonal.list.ListHeadTailView;
import org.variantsync.vevos.simulation.io.data.CSV;

import java.nio.file.Path;
import java.util.*;

public record CodeMatching(HashMap<Path, FileMatching> fileMatchingBefore, HashMap<Path, FileMatching> fileMatchingAfter) {

    public Optional<Integer> beforeCommitMatch(Path pathToFile, int lineNumber) {
        return Optional.of(fileMatchingBefore.get(pathToFile)).map(m -> m.matching.get(lineNumber));
    }

    public Optional<Integer> afterCommitMatch(Path pathToFile, int lineNumber) {
        return Optional.of(fileMatchingAfter.get(pathToFile)).map(m -> m.matching.get(lineNumber));
    }

    public static Lazy<Optional<CodeMatching>> lazyFromCSVs(Lazy<Optional<CSV>> matchingBefore, Lazy<Optional<CSV>> matchingAfter) {
        return Lazy.of(() -> fromCSVs(matchingBefore, matchingAfter));
    }

    public static Optional<CodeMatching> fromCSVs(Lazy<Optional<CSV>> matchingBefore, Lazy<Optional<CSV>> matchingAfter) {
        Optional<CSV> mBefore = matchingBefore.run();
        Optional<CSV> mAfter = matchingAfter.run();
        if (mBefore.isEmpty() || mAfter.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(fromCSVs(mBefore.get(), mAfter.get()));
    }

    public static CodeMatching fromCSVs(CSV matchingBefore, CSV matchingAfter) {
        HashMap<Path, FileMatching> fileMatchingBefore = parseCSV(matchingBefore);
        HashMap<Path, FileMatching> fileMatchingAfter = parseCSV(matchingAfter);
        // Make sure that the matchings agree
        checkAgreement(fileMatchingBefore, fileMatchingAfter);
        return new CodeMatching(fileMatchingBefore, fileMatchingAfter);
     }

    private static HashMap<Path, FileMatching> parseCSV(CSV csv) {
        // skip first entry as it is the csv header
        final ListHeadTailView<String[]> rows = new ListHeadTailView<>(csv.rows()).tail();
        final HashMap<Path, FileMatching> matching = new HashMap<>();
        for (final String[] row : rows) {
            Path filePath = Path.of(row[0]);
            int currentLine = Integer.parseInt(row[1]);
            int matchedLine = Integer.parseInt(row[2]);

            // Retrieve or initialize the file matching
            FileMatching fm = matching.computeIfAbsent(filePath, FileMatching::new);
            fm.add(currentLine, matchedLine);
        }
        return matching;
    }

    private static final class FileMatching {
        private final Path filePath;
        private final ArrayList<Integer> matching;

        public FileMatching(Path filePath) {
            super();
            this.filePath = filePath;
            this.matching = new ArrayList<>();
        }

        public void add(int currentLine, int matchedLine) {
            if (this.matching.size() != currentLine) {
                throw new IllegalStateException();
            }
            this.matching.add(matchedLine);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (FileMatching) obj;
            return Objects.equals(this.filePath, that.filePath) &&
                    Objects.equals(this.matching, that.matching);
        }

        @Override
        public int hashCode() {
            return Objects.hash(filePath, matching);
        }

        @Override
        public String toString() {
            return "FileMatching[" +
                    "filePath=" + filePath + ", " +
                    "matching=" + matching + ']';
        }

    }

    private static void checkAgreement(HashMap<Path, FileMatching> before, HashMap<Path, FileMatching> after) {
        final IllegalArgumentException agreementMismatch = new IllegalArgumentException("The matchings do not agree.");
        if (before.keySet().size() != after.keySet().size()) {
            throw agreementMismatch;
        }
        for (Path path : before.keySet()) {
            FileMatching beforeMatching = before.get(path);
            FileMatching afterMatching = after.get(path);
            if (afterMatching == null) {
                throw agreementMismatch;
            }
            if (arraysDisagree(beforeMatching.matching, afterMatching.matching)
                    || arraysDisagree(afterMatching.matching, beforeMatching.matching)) {
                throw agreementMismatch;
            }
        }
    }

    private static boolean arraysDisagree(ArrayList<Integer> first, ArrayList<Integer> second) {
        for (int i = 0; i < first.size(); i++)  {
            if (first.get(i) == -1) {
                continue;
            }
            if (second.get(first.get(i)) != i) {
                return true;
            }
        }
        return false;
    }
}
