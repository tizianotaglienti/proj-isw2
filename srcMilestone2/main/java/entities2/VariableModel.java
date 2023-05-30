package entities2;

public class VariableModel {

    private static String balancing;
    private static String featureSelection;
    private static String sensitivity;

    private static float majorityClassPercent;

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

    public static float getMajorityClassPercent() {
        return majorityClassPercent;
    }

    public static void setMajorityClassPercent(float majorityClassPercent) {
        VariableModel.majorityClassPercent = majorityClassPercent;
    }

}
