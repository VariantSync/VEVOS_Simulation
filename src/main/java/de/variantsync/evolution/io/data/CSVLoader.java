package de.variantsync.evolution.io.data;

import de.variantsync.evolution.io.ResourceLoader;
import de.variantsync.evolution.io.ResourceWriter;
import de.variantsync.evolution.io.TextIO;
import de.variantsync.evolution.util.PathUtils;
import de.variantsync.evolution.util.functional.Result;
import de.variantsync.evolution.util.functional.Unit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class to load CSV files.
 */
public class CSVLoader implements ResourceLoader<CSV>, ResourceWriter<CSV> {
    public final static String DefaultSeparator = ";";
    private String separator;
    private String separatorWithWhiteSpace;

    public CSVLoader() {
        this(DefaultSeparator);
    }

    /**
     * Create a CSVLoader with the given separator.
     * @param separator A string that will be interpreted as separater between elements in a row in the csv file.
     *                  The default value is ";".
     */
    public CSVLoader(String separator) {
        setSeparator(separator);
    }

    public void setSeparator(String separator) {
        this.separator = separator;
        this.separatorWithWhiteSpace = "\\s*" + separator + "\\s*";
    }

    @Override
    public boolean canLoad(Path p) {
        return PathUtils.hasExtension(p, ".csv");
    }

    @Override
    public boolean canWrite(Path p) {
        return canLoad(p);
    }

    @Override
    public Result<CSV, Exception> load(Path p) {
        try (BufferedReader reader = new BufferedReader(new FileReader(p.toFile()))) {
            final List<String[]> rows =
                    reader.lines().map(line -> line.trim().split(separatorWithWhiteSpace)).collect(Collectors.toList());
            return Result.Success(new CSV(rows));
        } catch (IOException e) {
            return Result.Failure(e);
        }
    }

    @Override
    public Result<Unit, ? extends Exception> write(CSV object, Path p) {
        return Result.Try(() -> TextIO.write(p, object.toString(separator)));
    }
}
