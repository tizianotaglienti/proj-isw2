package entitiestwo;

public class ProjectToAnalyze {

    private String csvFile = "";
    private String arffFile = "";
    private String path = "";
    private String projectName = "";
    private String firstRelease = "";

    public String getCsvFile() {
        return csvFile;
    }

    public void setCsvFile(String csvFile) {
        this.csvFile = csvFile;
    }

    public String getArffFile() {
        return arffFile;
    }

    public void setArffFile(String arffFile) {
        this.arffFile = arffFile;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getFirstRelease() {
        return firstRelease;
    }

    public void setFirstRelease(String firstRelease) {
        this.firstRelease = firstRelease;
    }
}
