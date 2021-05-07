package de.variantsync.evolution.io.data;

import de.variantsync.evolution.io.ResourceLoader;
import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.PathUtils;
import de.variantsync.evolution.util.functional.Result;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CSVLoader implements ResourceLoader<CSV> {
    private final String separator = ";";

    @Override
    public boolean canLoad(Path p) {
        return PathUtils.hasExtension(p, ".csv");
    }

    @Override
    public Result<CSV, Exception> load(Path p) {
        Scanner scanner;
        try {
            scanner = new Scanner(p.toFile());
        } catch (FileNotFoundException e) {
            return Result.Failure(e);
        }
        scanner.useDelimiter("\n");

        List<String[]> rows = new ArrayList<>();
        while(scanner.hasNext()) {
            String line = scanner.next();
            String[] entries = line.split(separator);

            for (int i = 0; i < entries.length; ++i) {
                entries[i] = entries[i].trim();
            }

            rows.add(entries);
        }
        scanner.close();

        return Result.Success(new CSV(rows));
    }
}
