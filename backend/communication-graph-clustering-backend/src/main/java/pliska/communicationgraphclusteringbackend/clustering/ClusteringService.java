package pliska.communicationgraphclusteringbackend.clustering;

import java.util.Map;

public interface ClusteringService {
    Map<String, Object> clusterGraph(Long graphId, Map<String, Object> parameters);
}
