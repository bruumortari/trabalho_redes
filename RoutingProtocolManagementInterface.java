public interface RoutingProtocolManagementInterface{
    boolean getDistanceTable(short id);
    boolean getLinkCost(short node1, short node2);
    boolean setLinkCost(short node1, short node2, int cost);
}
