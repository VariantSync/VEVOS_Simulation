package org.variantsync.vevos.simulation.io;

import org.variantsync.functjonal.Result;
import org.tinylog.Logger;

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
    public final static String LINEBREAK = System.lineSeparator();

    public static String[] readLinesAsArray(final File file) throws IOException {
        final LinkedList<String> lines = new LinkedList<>();
        try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.lines().forEach(lines::add);
        } catch (final IOException e) {
            Logger.error("Failed to read lines from file: ", e);
            throw e;
        }
        return lines.toArray(new String[0]);
    }

    public static String readLastLine(final File file) throws IOException {
        try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = "";
            while (reader.ready()) {
                line = reader.readLine();
            }
            return line;
        } catch (final IOException e) {
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
    public static Result<List<String>, IOException> readLinesTrimmed(final Path p) {
        return readLines(p).map(lines ->
                lines.stream()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList()));
    }

    public static Result<List<String>, IOException> readLines(final Path p) {
        try (final BufferedReader br = new BufferedReader(new FileReader(p.toFile()));
             final Stream<String> linesStream = br.lines()) {
            return Result.Success(linesStream.toList());
        } catch (final IOException e) {
            return Result.Failure(e);
        }
    }

    /**
     * Writes the given text to the given file.
     * Creates a new file and assumes there exists no file yet at the given path.
     *
     * @param p    File to create and fill with text.
     * @param text Text to write to file.
     * @throws IOException if an I/O error occurs writing to or creating the file, or the text cannot be encoded using the specified charset.
     *                     Also throws if the given file already exists.
     */
    public static void write(final Path p, final String text) throws IOException {
        Files.writeString(p, text, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
    }

    /**
     * Append the given text to the given file.
     * Assumes that the given file already exists.
     *
     * @param p    Existing file to append text to.
     * @param text Text to write to file.
     * @throws IOException if an I/O error occurs while writing to the file, or the text cannot be encoded using the specified charset.
     */
    public static void append(final Path p, final String text) throws IOException {
        Files.writeString(p, text, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
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
