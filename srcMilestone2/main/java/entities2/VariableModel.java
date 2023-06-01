package entities2;

public class VariableModel {

    private static String balancing;
    private static String featureSelection;
    private static String sensitivity;

    private static float majorityClassPercentage;

    private static int[] buggyTrainingSet;
    private static int[] buggyTestingSet;
    private static int[] buggyTrainingSetToWrite;


    public static String getBalancing() {
        return balancing;
    }

    public static void setBalancing(String balancing) {
        VariableModel.balancing = balancing;
    }

    public static String getFeatureSelection() {
        return featureSelection;
    }

    public static void setFeatureSelection(String featureSelection) {
        VariableModel.featureSelection = featureSelection;
    }

    public static String getSensitivity() {
        return sensitivity;
    }

    public static void setSensitivity(String sensitivity) {
        VariableModel.sensitivity = sensitivity;
    }

    public static float getMajorityClassPercentage() {
        return majorityClassPercentage;
    }

    public static void setMajorityClassPercentage(float majorityClassPercentage) {
        VariableModel.majorityClassPercentage = majorityClassPercentage;
    }

    public static int[] getBuggyTrainingSet() {
        return buggyTrainingSet;
    }

    public static void setBuggyTrainingSet(int[] buggyTrainingSet) {
        VariableModel.buggyTrainingSet = buggyTrainingSet;
    }

    public static int[] getBuggyTestingSet() {
        return buggyTestingSet;
    }

    public static void setBuggyTestingSet(int[] buggyTestingSet) {
        VariableModel.buggyTestingSet = buggyTestingSet;
    }

    public static int[] getBuggyTrainingSetToWrite() {
        return buggyTrainingSetToWrite;
    }

    public static void setBuggyTrainingSetToWrite(int[] buggyTrainingSetToWrite) {
        VariableModel.buggyTrainingSetToWrite = buggyTrainingSetToWrite;
    }
}
