package de.variantsync.evolution.io;

import de.variantsync.evolution.util.Logger;

import java.io.*;
import java.util.LinkedList;
import java.util.stream.Stream;

public class TextIO {

    public static String[] readLinesAsArray(File file) throws IOException {
        LinkedList<String> lines = new LinkedList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.lines().forEach(lines::add);
        } catch (IOException e) {
            Logger.exception("Failed to read lines from file: ", e);
            throw e;
        }
        return lines.toArray(new String[0]);
    }

    public static Stream<String> readLinesAsStream(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines();
        } catch (IOException e) {
            Logger.exception("Failed to read lines from file: ", e);
            throw e;
        }
    }

    public static String readLastLine(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = "";
            while(reader.ready()) {
                line = reader.readLine();
            }
            return line;
        } catch (IOException e) {
            Logger.exception("Failed to read lines from file: ", e);
            throw e;
        }
    }
}
