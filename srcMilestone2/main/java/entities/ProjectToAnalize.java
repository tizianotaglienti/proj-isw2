package entities;

public class ProjectToAnalize {

    private static String csvFile = "";
    private static String arffFile = "";
    private static String path = "";
    private static String projectName = "";
    private static String firstRelease = "";



    public static String getCsvFile() {
        return csvFile;
    }

    public static void setCsvFile(String csvFile) {
        ProjectToAnalize.csvFile = csvFile;
    }

    public static String getArffFile() {
        return arffFile;
    }

    public static void setArffFile(String arffFile) {
        ProjectToAnalize.arffFile = arffFile;
    }

    public static String getPath() {
        return path;
    }

    public static void setPath(String path) {
        ProjectToAnalize.path = path;
    }

    public static String getProjectName() {
        return projectName;
    }

    public static void setProjectName(String projectName) {
        ProjectToAnalize.projectName = projectName;
    }

    public static String getFirstRelease() {
        return firstRelease;
    }

    public static void setFirstRelease(String firstRelease) {
        ProjectToAnalize.firstRelease = firstRelease;
    }
}
