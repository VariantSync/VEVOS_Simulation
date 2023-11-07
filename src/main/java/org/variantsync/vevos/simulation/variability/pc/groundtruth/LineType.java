package org.variantsync.vevos.simulation.variability.pc.groundtruth;

public enum LineType {
    IF("if"),
    ELSE("else"),
    ELIF("elif"),
    ARTIFACT("artifact"),
    ENDIF("endif"),
    ROOT("ROOT");

    public final String name;

    private LineType(String name) {
        this.name = name;
    }

    public boolean isConditionalAnnotation() {
        return this == IF || this == ELIF;
    }

    public boolean isAnnotation() {
        return this != ARTIFACT;
    }

    public boolean notAMacro() {return this == ARTIFACT || this == ROOT;}

    public static LineType fromName(String name) {
        LineType[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            LineType candidate = var1[var3];
            if (candidate.toString().equalsIgnoreCase(name)) {
                return candidate;
            }
        }

        throw new IllegalArgumentException("Given string \"" + name + "\" is not the name of a LineType.");
    }

    public static int getRequiredBitCount() {
        return 3;
    }
}
