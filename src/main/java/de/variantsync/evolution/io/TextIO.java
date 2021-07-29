package de.variantsync.evolution.io;

import de.variantsync.evolution.util.CaseSensitivePath;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.variability.pc.LineBasedAnnotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO: Implement readLinesAs(Path p, Function<> f) with which one can load a file into a desired format

public class TextIO {
    public static String[] readLinesAsArray(File file) throws IOException {
        LinkedList<String> lines = new LinkedList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.lines().forEach(lines::add);
        } catch (IOException e) {
            Logger.error("Failed to read lines from file: ", e);
            throw e;
        }
        return lines.toArray(new String[0]);
    }

    public static String readLastLine(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = "";
            while (reader.ready()) {
                line = reader.readLine();
            }
            return line;
        } catch (IOException e) {
            Logger.error("Failed to read lines from file: ", e);
            throw e;
        }
    }

    /**
     * Read the lines in the file under the given path, trim whitespace at the start and end of each line, and remove empty lines
     *
     * @param p Path to the file that should be read
     * @return The lines that were read
     */
    public static Result<List<String>, IOException> readLinesTrimmed(Path p) {
        return Result.Try(() -> Files.readAllLines(p).stream().map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
    }

    /**
     * Extract all lines in [lineFrom, lineTo] = i from sourceFile and put them into targetFile for each interval i in linesToTake.
     *
     * @param sourceFile  File from which to read lines. Won't be altered.
     * @param targetFile  File to write the desired lines to.
     * @param linesToTake Intervals of lines to copy.
     * @throws IOException May occur upon writing or creating files.
     */
    public static void copyTextLines(Path sourceFile, Path targetFile, List<Integer> linesToTake) throws IOException {
        /// Do not use Files.readAllLines(sourceFile) as it assumes the files to be in UTF-8 and crashes otherwise.
        try (Stream<String> linesStream = new BufferedReader(new FileReader(sourceFile.toFile())).lines()) {
            final List<String> read_lines = linesStream.collect(Collectors.toList());
            final StringBuilder linesToWrite = new StringBuilder();

            for (final Integer lineNo : linesToTake) {
                // skip lines that exceed the content
                if (lineNo - 1 >= read_lines.size()) {
                    Logger.warning("Skipped copying line "
                            + lineNo
                            + " from \""
                            + sourceFile
                            + "\" to \""
                            + targetFile
                            + "\" as it is out of bounds [1, "
                            + (read_lines.size() - 1)
                            + "]!"
                    );
                    continue;
                }

                // The list read_lines is 0-based.
                // Given lines are 1-based because line numbers are typically given 1-based.
                // Thus, we have to -1 here because line numbers are 1-indexed
                // but we want to look them up in read_lines, which is 0-based.
                linesToWrite.append(read_lines.get(lineNo - 1)).append(System.lineSeparator());
            }

            Files.write(
                    targetFile,
                    linesToWrite.toString().getBytes(),
                    StandardOpenOption.APPEND);
        }
    }

    /**
     * Writes the given text to the given file.
     * Creates a new file and assumes there exists no file yet at the given path.
     * @param p File to create and fill with text.
     * @param text Text to write to file.
     * @throws IOException if an I/O error occurs writing to or creating the file, or the text cannot be encoded using the specified charset.
     *                     Also throws if the given file already exists.
     */
    public static void write(Path p, String text) throws IOException {
        Files.writeString(p, text, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
    }

    public static String readAsString(final Path p) throws IOException {
        try (final BufferedReader reader = new BufferedReader(new FileReader(p.toFile()))) {
            return reader.lines().collect(Collectors.joining());
        } catch (final IOException e) {
            Logger.error("Failed to read lines from file: ", e);
            throw e;
        }
    }
}
