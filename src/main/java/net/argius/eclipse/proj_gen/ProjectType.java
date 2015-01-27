package net.argius.eclipse.proj_gen;

public enum ProjectType {

    Plain, Java, Maven2, MavenAndServlet30, MavenAndJavaFX, Unknown;

    public static ProjectType fromString(String s) {
        switch (s) {
            case "Plain":
                return Plain;
            case "Java":
                return Java;
            case "Maven2":
                return Maven2;
        }
        return Unknown;
    }

}
