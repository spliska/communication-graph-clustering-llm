package pliska.communicationgraphclusteringbackend.clustering.hierarchy.meta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pliska.communicationgraphclusteringbackend.clustering.hierarchy.HierarchyCalculator;
import pliska.communicationgraphclusteringbackend.clustering.hierarchy.features.*;
import pliska.communicationgraphclusteringbackend.db.graph.EdgeDto;
import pliska.communicationgraphclusteringbackend.db.graph.NodeDto;

import java.util.List;
@Service
public class HierarchyCalculatorMeta implements HierarchyCalculator {

    private double rawCliqueScoreWeight=11.12;
    private double numberOfEmailsWeight=11.11;
    private double degreeCentralityWeight=11.00;
    private double weightedCliqueScoreWeight=10.85;
    private double hubsAndAuthoritiesWeight=10.24;
    private double averageDistanceWeight=8.25;
    private double clusteringCoefficientWeight=7.55;
    private double averageResonseTimeWeight=0.26;
    private double numberOfEmailsWithMoreThanOneReceiverWeight=10.00;

    @Autowired
    private BasicEmailInsightsCalculator basicEmailInsightsCalculator;
    @Autowired
    private AverageResponseTimeCalculator averageResponseTimeCalculator;


    public HierarchyCalculatorMeta(BasicEmailInsightsCalculator basicEmailInsightsCalculator) {
        this.basicEmailInsightsCalculator = basicEmailInsightsCalculator;
    }

    public Double calculateGlobalHierarchy(Long graphId, NodeDto node, List<NodeDto> nodes, List<EdgeDto> edges) {
        BasicGraphInsightsCalculator graphInsights = new BasicGraphInsightsCalculator(nodes, edges);
        CliquesCalculator cliquesCalculator = new CliquesCalculator(nodes, edges);
        ClusteringCoefficientCalculator clusteringCoefficientCalculator = new ClusteringCoefficientCalculator(nodes, edges);
        HubsAndAuthoritiesCalculator hubsAndAuthoritiesCalculator = new HubsAndAuthoritiesCalculator(nodes, edges);

        String email = node.getEmail();

        double numberOfEmails = basicEmailInsightsCalculator.numberOfEmailsSent(email)
                + basicEmailInsightsCalculator.numberOfEmailsReceived(email);

        double numberOfEmailsWithMoreThanOneReceiver=basicEmailInsightsCalculator.ratioOfMultiRecipientEmails(email);

        double degreeCentrality = graphInsights.getGlobalNodeCentrality(graphId, email);

        double averageDistance = graphInsights.getAverageDistanceToConnectedNodes(email);

        double rawCliqueScore = cliquesCalculator.getNumberOfCliquesForNode(node);
        double weightedCliqueScore = cliquesCalculator.getCliquesMembersAmountForNode(node.getNodeId().toString())
                .stream().mapToDouble(Double::valueOf).sum();

        double hubsAndAuthorities = hubsAndAuthoritiesCalculator.getCombinedHitsScore(node.getNodeId().toString());
        double clusteringCoefficient = clusteringCoefficientCalculator.calculateConnectionsOfNodeToOtherClusters(node.getNodeId().toString());

        long averageResponseTime = averageResponseTimeCalculator.calculateAverageResponseTime(email, email);

        double hierarchyScore =
                (numberOfEmails * numberOfEmailsWeight)
                        + (degreeCentrality * degreeCentralityWeight)
                        + (averageDistance * averageDistanceWeight)
                        + (rawCliqueScore * rawCliqueScoreWeight)
                        + (weightedCliqueScore * weightedCliqueScoreWeight)
                        + (clusteringCoefficient * clusteringCoefficientWeight)
                        + (hubsAndAuthorities * hubsAndAuthoritiesWeight)
                        + (averageResponseTime * averageResonseTimeWeight)
                        + (numberOfEmailsWithMoreThanOneReceiver * numberOfEmailsWithMoreThanOneReceiverWeight);
        System.out.println("HierarchyScore: " + hierarchyScore);
        return hierarchyScore;
    }
}
