public interface RoutingProtocolManagementServiceUserInterface {
    void distanceTableIndication(short id, int[][] table);
    void linkCostIndication(short nodeA, short nodeB, int cost);
}
