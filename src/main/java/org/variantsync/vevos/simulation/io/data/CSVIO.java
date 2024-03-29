package org.variantsync.vevos.simulation.io.data;

import org.tinylog.Logger;
import org.variantsync.functjonal.Result;
import org.variantsync.functjonal.Unit;
import org.variantsync.vevos.simulation.io.ResourceLoader;
import org.variantsync.vevos.simulation.io.ResourceWriter;
import org.variantsync.vevos.simulation.io.TextIO;
import org.variantsync.vevos.simulation.util.io.PathUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class to load CSV files.
 */
public class CSVIO implements ResourceLoader<CSV>, ResourceWriter<CSV> {
    public final static String DefaultSeparator = ";";
    private String separator;
    private String separatorWithWhiteSpace;

    public CSVIO() {
        this(DefaultSeparator);
    }

    /**
     * Create a CSVLoader with the given separator.
     * @param separator A string that will be interpreted as separator between elements in a row in the csv file.
     *                  The default value is ";".
     */
    public CSVIO(final String separator) {
        setSeparator(separator);
    }

    public void setSeparator(final String separator) {
        this.separator = separator;
        this.separatorWithWhiteSpace = "\\s*" + separator + "\\s*";
    }

    @Override
    public boolean canLoad(final Path p) {
        return PathUtils.hasExtension(p, ".csv");
    }

    @Override
    public boolean canWrite(final Path p) {
        return canLoad(p);
    }

    @Override
    public Result<CSV, Exception> load(final Path p) {
        try (final BufferedReader reader = new BufferedReader(new FileReader(p.toFile()))) {
            final List<String[]> rows =
                    reader.lines().map(line -> line.trim().split(separatorWithWhiteSpace)).collect(Collectors.toList());
            return Result.Success(new CSV(rows));
        } catch (final Exception e) {
            Logger.error(e);
            return Result.Failure(e);
        }
    }

    @Override
    public Result<Unit, ? extends Exception> write(final CSV object, final Path p) {
        return Result.Try(() -> TextIO.write(p, object.toString(separator)));
    }
}
