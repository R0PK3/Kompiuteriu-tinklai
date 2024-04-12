import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;


class Main {
    private static final int ROUTER_MAX = 100;
    private static final int INFINITE_COST = 999999;
    private static int sourceRouter = -1;
    private static int destinationRouter = -1;
    private static String topologyFileName;
    private static int totalRouter = 0;

    private static final Router[] network = new Router[ROUTER_MAX];
    private static final Router emptyRouter = new Router();

    private static final int[][] originalNetwork = new int[ROUTER_MAX][ROUTER_MAX];

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n\n\n\t (1) Load a Network Topology");
            System.out.println("\t (2) Init Source Router and Display Connection Table");
            System.out.println("\t (3) Shortest Path to Destination Router");
            System.out.println("\t (4) Modify the Network Topology");
            System.out.println("\t (5) Exit");
            System.out.print("\n\t Command: ");
            char menuSelected = scanner.nextLine().charAt(0);
            switch (menuSelected) {
                case '1' -> {
                    System.out.print("\n\t  Input original network topology matrix data file: ");
                    topologyFileName = scanner.nextLine();
                    if (getOriginalTopology()) {
                        initNetwork();
                        printTopologyMatrix();
                        for (int nodeIndex = 0; nodeIndex < totalRouter; nodeIndex++) {
                            printConnectionTable(nodeIndex);
                        }
                    } else {
                        System.out.println("\n\t File " + topologyFileName + " could not be loaded. Please check location and try again");
                    }
                }
                case '2' -> {
                    System.out.print("\n\t Please select a router: ");
                    sourceRouter = Integer.parseInt(scanner.nextLine());
                    if (sourceRouter >= 1 && sourceRouter <= totalRouter) {
                        printConnectionTable(sourceRouter - 1);
                    } else {
                        System.out.println("\n\t Wrong router index entered. Try again");
                    }
                }
                case '3' -> {
                    System.out.print("\n\t Please select a destination: ");
                    String destination = scanner.nextLine();
                    int source = sourceRouter;
                    int dest = Integer.parseInt(destination);
                    if (source >= 1 && source <= totalRouter && dest >= 1 && dest <= totalRouter) {
                        printShortestPath(source - 1, dest - 1);
                    } else {
                        System.out.println("\n\t Wrong destination key entered. Try again");
                    }
                }
                case '4' -> {
                    System.out.print("\n\t Select a router to be removed: ");
                    int deletedRouter = Integer.parseInt(scanner.nextLine());
                    updateTopology(sourceRouter - 1, destinationRouter - 1, deletedRouter - 1);

                }
                case '5' -> {
                    System.out.println("\n\t Exiting program...");
                    scanner.close();
                    return;
                }
                default -> System.out.println("\t Wrong menu key entered. Try again");
            }
        }
    }

    private static boolean getOriginalTopology() {
        File file = new File(topologyFileName);
        try {
            Scanner scanner = new Scanner(file);
            initializeTopologyMatrix();
            if (!readTopologySize(scanner)) {
                return false;
            }
            populateTopologyMatrix(scanner);
            scanner.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void initializeTopologyMatrix() {
        sourceRouter = -1;
        destinationRouter = -1;
        totalRouter = 0;

        for (int i = 0; i < ROUTER_MAX; i++) {
            for (int j = 0; j < ROUTER_MAX; j++) {
                originalNetwork[i][j] = 0;
            }
        }
    }

    private static boolean readTopologySize(Scanner scanner) {
        if (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] tokens = line.split("\\s+");
            totalRouter = tokens.length;
            for (int i = 0; i < totalRouter; i++) {
                originalNetwork[0][i] = Integer.parseInt(tokens[i]);
            }
            return true;
        }
        return false;
    }

    private static void populateTopologyMatrix(Scanner scanner) {
        for (int i = 1; i < totalRouter; i++) {
            if (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] tokens = line.split("\\s+");
                for (int j = 0; j < totalRouter; j++) {
                    originalNetwork[i][j] = Integer.parseInt(tokens[j]);
                }
            }
        }
    }

    private static void initNetwork() {
        setRouterValuesFromMatrix();
        calculateShortestPaths();
    }

    private static void setRouterValuesFromMatrix() {
        for (int i = 0; i < totalRouter; i++) {
            network[i] = new Router();
            network[i].setRouterNum(i);
            for (int j = 0; j < totalRouter; j++) {
                network[i].setDirectLink(i, j, originalNetwork[i][j]);
            }
        }
    }

    private static void calculateShortestPaths() {
        for (int i = 0; i < totalRouter; i++) {
            Router curRouter = network[i];
            for (int j = 0; j < totalRouter; j++) {
                if (curRouter.getDirectLink(i, j) > 0) {
                    int source = curRouter.getRouterNum();
                    int[] pathArray = curRouter.getDirectLinkFromSource(source);
                    shortestPathUpd(source, source, j, pathArray);
                }
            }
        }
    }


    private static void shortestPathUpd(int senderIndex, int source, int destination, int[] sourceLinks) {
        Router dstRouter = network[destination];
        if (dstRouter.getSequenceNum(source) >= 1)
            return;
        dstRouter.setSequenceNum(source, 1);

        updateDirectLinksFromSource(dstRouter, source, sourceLinks);
        updatePathsForConnectedRouters(dstRouter, senderIndex, source, destination, sourceLinks);
    }

    private static void updateDirectLinksFromSource(Router dstRouter, int source, int[] sourceLinks) {
        for (int i = 0; i < totalRouter; i++) {
            dstRouter.setDirectLink(source, i, sourceLinks[i]);
        }
    }

    private static void updatePathsForConnectedRouters(Router dstRouter, int senderIndex, int source, int destination, int[] sourceLinks) {
        for (int i = 0; i < totalRouter; i++) {
            if (dstRouter.getDirectLink(destination, i) > 0) {
                if (i != source && i != senderIndex) {
                    shortestPathUpd(destination, source, i, sourceLinks);
                }
            }
        }
    }

    private static void printTopologyMatrix() {
        System.out.println("\n\n\t Review original topology matrix:");
        for (int i = 0; i < totalRouter; i++) {
            for (int j = 0; j < totalRouter; j++) {
                if (originalNetwork[i][j] == INFINITE_COST) {
                    System.out.print("\033[34;1m" + -1 + "\t");
                } else {
                    System.out.print("\033[34;1m" + originalNetwork[i][j] + "\t");
                }
            }
            System.out.println("\033[34;1m\n\t\033[39;49m");
        }
    }

    private static void printConnectionTable(int routerNum) {
        Router curRouter = network[routerNum];
        initializePathsForRouters(curRouter, routerNum);

        int counter = 0;
        while (counter < totalRouter + 1) {
            int routerMin = findMinCostRouter(curRouter);
            if (routerMin != -1) {
                curRouter.getPath(routerMin).setCostFound(1);
                updatePathCosts(curRouter, routerMin);
            }
            counter++;
        }

        printConnectionTableHeader(routerNum);

        for (int j = 0; j < totalRouter; j++) {
            printPathInfo(curRouter, j, routerNum);
        }
    }

    private static void initializePathsForRouters(Router curRouter, int routerNum) {
        for (int i = 0; i < totalRouter; i++) {
            curRouter.setPath(i, new ShortestPath());
        }
        initShortestPath(curRouter.getPath(routerNum), 0, -1);

        for (int nextRouter = 0; nextRouter < totalRouter; nextRouter++) {
            int cost = curRouter.getDirectLink(routerNum, nextRouter);
            if (cost > 0 && nextRouter != routerNum) {
                initShortestPath(curRouter.getPath(nextRouter), cost, routerNum);
            } else if (cost < 0 && nextRouter != routerNum) {
                initShortestPath(curRouter.getPath(nextRouter), INFINITE_COST, -1);
            }
        }
    }

    private static int findMinCostRouter(Router curRouter) {
        int minimumCost = INFINITE_COST;
        int routerMin = -1;
        for (int i = 0; i < totalRouter; i++) {
            int totalCost = curRouter.getPath(i).getTotalCost();
            if (curRouter.getPath(i).getCostFound() == 0 && totalCost <= minimumCost) {
                minimumCost = totalCost;
                routerMin = i;
            }
        }
        return routerMin;
    }

    private static void updatePathCosts(Router curRouter, int routerMin) {
        int[] nextDirectLink = curRouter.getDirectLinkFromSource(routerMin);
        for (int i = 0; i < totalRouter; i++) {
            int pathCost = nextDirectLink[i];
            if (pathCost > 0 && curRouter.getPath(i).getCostFound() == 0) {
                int oldCost = curRouter.getPath(i).getTotalCost();
                int newCost = curRouter.getPath(routerMin).getTotalCost() + pathCost;
                if (newCost < oldCost) {
                    curRouter.getPath(i).setPreviousRouter(routerMin);
                    curRouter.getPath(i).setTotalCost(newCost);
                }
            }
        }
    }

    private static void printConnectionTableHeader(int routerNum) {
        System.out.println("\n\n\t\033[34;1m Router " + (routerNum + 1) + " Connection Table");
        System.out.println("\t Destination\t\t Interface");
        System.out.println("\t ===================================");
    }

    private static void printPathInfo(Router curRouter, int j, int routerNum) {
        if (curRouter.getPath(j).getPreviousRouter() == -1 || curRouter.getPath(j).getPreviousRouter() == routerNum) {
            if (j == routerNum) {
                System.out.println("\t\t" + (j + 1) + "\t\t --");
            } else {
                if (curRouter.getPath(j).getTotalCost() >= INFINITE_COST) {
                    System.out.println("\t\t" + (j + 1) + "\t\t --");
                } else {
                    System.out.println("\t\t" + (j + 1) + "\t\t " + (j + 1));
                }
            }
        } else {
            int nextRouter = findNextRouter(curRouter, j, routerNum);
            System.out.println("\t\t" + (j + 1) + "\t\t " + (nextRouter + 1));
        }
    }

    private static int findNextRouter(Router curRouter, int j, int routerNum) {
        int nextRouter;
        int cnt = 0;
        nextRouter = curRouter.getPath(j).getPreviousRouter();
        while (curRouter.getPath(nextRouter).getPreviousRouter() != routerNum && cnt < totalRouter) {
            nextRouter = curRouter.getPath(nextRouter).getPreviousRouter();
            cnt++;
        }
        return nextRouter;
    }


    private static void initShortestPath(ShortestPath path, int totalCost, int previousRouter) {
        path.setTotalCost(totalCost);
        path.setPreviousRouter(previousRouter);
        path.setCostFound(0);
    }

    private static void printShortestPath(int source, int destination) {
        Router srcRouter = network[source];

        if (!(srcRouter.getPath(source).getPreviousRouter() != -1 && srcRouter.getPath(source).getCostFound() == 1)) {
            printConnectionTable(source);
        }

        int cost = srcRouter.getPath(destination).getTotalCost();

        if (cost >= INFINITE_COST) {
            int srcValue = originalNetwork[source][source];
            int destValue = originalNetwork[destination][destination];
            if (srcValue == INFINITE_COST && destValue == INFINITE_COST) {
                System.out.println("\n\n\tSource and destination routers are down. Please enter another source and destination routers.");
            } else {
                if (srcValue == INFINITE_COST) {
                    System.out.println("\n\n\tSource router is down. Please enter another source router.");
                }
                if (destValue == INFINITE_COST) {
                    System.out.println("\n\n\tDestination router is down. Please enter another destination router.");
                }
            }
        } else {
            System.out.print("\n\n\tThe shortest path from " + (source + 1) + " to " + (destination + 1) + " is: ");

            printPath(srcRouter, destination, totalRouter - 1);

            System.out.println(" - " + (destination + 1) + ", the total cost (value): " + srcRouter.getPath(destination).getTotalCost());
        }
    }

    private static void printPath(Router srcRouter, int pnodeIndex, int pathLength) {
        int prevIndex = srcRouter.getPath(pnodeIndex).getPreviousRouter();
        if (prevIndex != srcRouter.getRouterNum() && pathLength > 0) {
            printPath(srcRouter, prevIndex, pathLength - 1);
        }
        if (pathLength != totalRouter - 1) {
            System.out.print("\033[34;1m" + (prevIndex + 1) + " - ");
        } else {
            System.out.print("\033[34;1m" + (prevIndex + 1));
        }
    }

    private static void removeRouter(int deletedRouter) {
        for (int routerNum = 0; routerNum < totalRouter; routerNum++) {
            originalNetwork[deletedRouter][routerNum] = INFINITE_COST;
            originalNetwork[routerNum][deletedRouter] = INFINITE_COST;
        }
    }

    private static void resetNetwork() {
        for (int i = 0; i < ROUTER_MAX; i++) {
            network[i] = emptyRouter;
        }
    }

    private static void handlePrintActions(int source, int destination) {
        if (source == -2 || destination == -2) {
            for (int routerNum = 0; routerNum < totalRouter; routerNum++) {
                printConnectionTable(routerNum);
            }
        } else {
            printShortestPath(source, destination);
        }
    }


    private static void updateTopology(int source, int destination, int deletedRouter) {
        removeRouter(deletedRouter);
        resetNetwork();
        initNetwork();
        printTopologyMatrix();
        handlePrintActions(source, destination);
    }

}
