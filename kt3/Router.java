public class Router {

    private int ROUTER_MAX = 100;
    private int routerNum;
    private int[][] directLink = new int[ROUTER_MAX][ROUTER_MAX];
    private int[] sequenceNum = new int[ROUTER_MAX];
    private ShortestPath[] path = new ShortestPath[ROUTER_MAX];

    public Router(){}

    public int getRouterNum() {
        return routerNum;
    }

    public void setRouterNum(int routerNum) {
        this.routerNum = routerNum;
    }

    public int[][] getDirectLink() {
        return directLink;
    }

    public void setDirectLink(int[][] directLink) {
        this.directLink = directLink;
    }

    public int[] getSequenceNum() {
        return sequenceNum;
    }

    public void setSequenceNum(int[] sequenceNum) {
        this.sequenceNum = sequenceNum;
    }

    public ShortestPath[] getPath() {
        return path;
    }

    public void setPath(ShortestPath[] path) {
        this.path = path;
    }

    public int getDirectLink(int i, int j) {
        return directLink[i][j];
    }
    public int[] getDirectLinkFromSource(int source) {
        return directLink[source];
    }
    public void setDirectLink(int i, int j, int value) {
        this.directLink[i][j] = value;
    }

    public int getSequenceNum(int i) {
        return sequenceNum[i];
    }

    public void setSequenceNum(int i, int value) {
        this.sequenceNum[i] = value;
    }

    public ShortestPath getPath(int i) {
        return path[i];
    }

    public void setPath(int i, ShortestPath shortestPath) {
        this.path[i] = shortestPath;
    }
}
