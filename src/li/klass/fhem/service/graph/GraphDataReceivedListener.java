package li.klass.fhem.service.graph;

import java.util.List;
import java.util.Map;

public interface GraphDataReceivedListener {
    void graphDataReceived(Map<String, List<GraphEntry>> graphData);
}
