public class ShortestPath {
    private int totalCost;
    private int costFound;
    private int previousRouter;

    public int getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(int totalCost) {
        this.totalCost = totalCost;
    }

    public int getCostFound() {
        return costFound;
    }

    public void setCostFound(int costFound) {
        this.costFound = costFound;
    }

    public int getPreviousRouter() {
        return previousRouter;
    }

    public void setPreviousRouter(int previousRouter) {
        this.previousRouter = previousRouter;
    }
}
