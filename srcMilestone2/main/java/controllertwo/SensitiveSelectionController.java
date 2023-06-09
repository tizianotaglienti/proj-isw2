package controllertwo;

import entitiestwo.VariableModel;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.core.Instances;

/**
 * TECNICHE DI SENSITIVE SELECTION:
 *  No cost sensitive / sensitive threshold / sensitive learning (CFN = 10*CFP)
 **/

public class SensitiveSelectionController {
    private Classifier classifier;
    private Instances trainingSet;
    private Instances testingSet;

    /**
     * Avvia il processo di sensitive selection in base all'opzione selezionata.
     *
     * @param trainingSet                   Dati di addestramento.
     * @param testingSet                    Dati di test.
     * @param indexForSensitiveSelection    Indice per l'opzione di sensitive selection scelta.
     * @param classifier                    Classificatore utilizzato per la sensitive selection.
     * @return L'oggetto Evaluation contenente i risultati dell'analisi.
     * @throws Exception in caso di errori durante la sensitive selection.
     */

    public Evaluation startSensitiveSelection(Instances trainingSet, Instances testingSet, int indexForSensitiveSelection, Classifier classifier) throws Exception {
        this.classifier = classifier;
        this.testingSet = testingSet;
        this.trainingSet = trainingSet;

        if(indexForSensitiveSelection == 0) return noCostSensitive();
        if(indexForSensitiveSelection == 1) return sensitiveThreshold();
        if(indexForSensitiveSelection == 2) return sensitiveLearning();

        return null;
    }

    /**
     * No selection: costruisce il classificatore utilizzando il trainingSet senza alcuna considerazione sui costi.
     * Valuta il classificatore sull'insieme di test.
     *
     * @return L'oggetto Evaluation contenente i risultati dell'analisi.
     * @throws Exception in caso di errori durante la valutazione.
     */

    private Evaluation noCostSensitive() throws Exception {
        VariableModel.setSensitivity("No cost sensitive");

        try{
            classifier.buildClassifier(trainingSet);
        } catch (Exception e2) {
            e2.printStackTrace();
        }

        Evaluation evaluation = new Evaluation(testingSet);

        try{
            evaluation.evaluateModel(classifier, testingSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return evaluation;
    }

    /**
     * Applica la sensitive selection utilizzando la tecnica del "sensitive threshold".
     * Calcola la matrice dei costi e crea un classificatore sensibile ai costi.
     * Imposta la minimizzazione del costo atteso a true.
     * Valuta il classificatore sull'insieme di test.
     *
     * @return L'oggetto Evaluation contenente i risultati dell'analisi.
     * @throws Exception in caso di errori durante la valutazione.
     */

    private Evaluation sensitiveThreshold() throws Exception {
        VariableModel.setSensitivity("Sensitive threshold");

        CostMatrix costMatrix = this.calculateCostMatrix();
        CostSensitiveClassifier costSensitiveClassifier = createCostSensitiveClassifier(costMatrix);
        costSensitiveClassifier.setMinimizeExpectedCost(true);

        return applyEvaluation(costSensitiveClassifier, costMatrix);
    }

    /**
     * Applica la sensitive selection utilizzando la tecnica del "sensitive learning".
     * Calcola la matrice dei costi e crea un classificatore sensibile ai costi.
     * Imposta la minimizzazione del costo atteso a false.
     * Valuta il classificatore sull'insieme di test.
     *
     * @return L'oggetto Evaluation contenente i risultati dell'analisi.
     * @throws Exception in caso di errori durante la valutazione.
     */

    private Evaluation sensitiveLearning() throws Exception {
        VariableModel.setSensitivity("Sensitive learning");

        CostMatrix costMatrix = this.calculateCostMatrix();
        CostSensitiveClassifier costSensitiveClassifier = createCostSensitiveClassifier(costMatrix);
        costSensitiveClassifier.setMinimizeExpectedCost(false);

        return applyEvaluation(costSensitiveClassifier, costMatrix);
    }

    /**
     * Calcola la matrice dei costi con i valori appropriati.
     *
     * @return La matrice dei costi.
     */

    private CostMatrix calculateCostMatrix() {
        CostMatrix costMatrix = new CostMatrix(2);

        double falsePositiveCost = 1;
        double falseNegativeCost = 10 * falsePositiveCost;

        costMatrix.setCell(0, 0, 0.0);
        costMatrix.setCell(0, 1, falseNegativeCost);
        costMatrix.setCell(1, 0, falsePositiveCost);
        costMatrix.setCell(1, 1, 0.0);

        return costMatrix;
    }

    /**
     * Applica la valutazione del classificatore sensibile ai costi sull'insieme di test.
     *
     * @param costSensitiveClassifier   Il classificatore sensibile ai costi.
     * @param costMatrix                La matrice dei costi.
     * @return L'oggetto Evaluation contenente i risultati dell'analisi.
     * @throws Exception in caso di errori durante la valutazione.
     */

    private Evaluation applyEvaluation(CostSensitiveClassifier costSensitiveClassifier, CostMatrix costMatrix) throws Exception {
        try{
            costSensitiveClassifier.buildClassifier(trainingSet);
        } catch (Exception e2) {
            e2.printStackTrace();
        }

        Evaluation evaluation = new Evaluation(testingSet, costMatrix);

        try{
            evaluation.evaluateModel(classifier, testingSet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return evaluation;
    }

    /**
     * Crea un classificatore sensibile ai costi utilizzando il classificatore fornito e la matrice dei costi.
     *
     * @param costMatrix    La matrice dei costi.
     * @return Il classificatore sensibile ai costi.
     */

    private CostSensitiveClassifier createCostSensitiveClassifier(CostMatrix costMatrix) {
        CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier();
        costSensitiveClassifier.setClassifier(classifier);
        costSensitiveClassifier.setCostMatrix(costMatrix);

        return costSensitiveClassifier;
    }
}
