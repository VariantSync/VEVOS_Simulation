package simulation.util.names;

@FunctionalInterface
public interface NameGenerator {
    String getNameAtIndex(int i);
}