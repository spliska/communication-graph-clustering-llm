package pliska.communicationgraphclusteringbackend.db.graph;

import java.util.HashMap;

import static java.lang.Math.log;

public class WeightCalculator {

    public static Double logXmaxWeight(int interactions, int maxInteractions) {
        return 1 - log(1 + interactions) / log(1 + maxInteractions);
    }

    public static Double logWeight(int interactions) {
        return 1.0 / (1.0 + Math.log(1.0 + interactions));
    }


    public static Double logXmaxWeightLLM(HashMap<String, Double> llmWeights, HashMap<String, Double> llmValues, int interactions, int maxInteractions) {
        return 1 / (log(1 + interactions) / log(1 + maxInteractions))
                + log(llmWeights.get("personal") * llmValues.get("personal"))
                + log(llmWeights.get("topic") * llmValues.get("topic"))
                + log(llmWeights.get("sentiment") * llmValues.get("sentiment"));
    }


    public static Double logWeightLlm(HashMap<String, Double> llmWeights, HashMap<String, Double> llmValues, int interactions) {
        return 1 / log(llmWeights.get("personal") * llmValues.get("personal")) +
                log(llmWeights.get("topic") * llmValues.get("topic")) +
                log(llmWeights.get("sentiment") * llmValues.get("sentiment")) +
                log(llmWeights.get("interactions") * interactions);
    }
}
