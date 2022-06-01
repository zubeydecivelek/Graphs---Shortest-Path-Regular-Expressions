import java.io.Serializable;
import java.util.*;

public class RoutingTable implements Serializable {

    static final long serialVersionUID = 99L;
    private final Router router;
    private final Network network;
    private List<RoutingTableEntry> entryList;
    private final List<Link> links;
    private ArrayList<ArrayList<Link>> allRoutersPath;
    private ArrayList<Link> remainingLinks = new ArrayList<>();
    private ArrayList<String> routerArray = new ArrayList<>();
    private ArrayList<Double> costArray = new ArrayList<>();

    public RoutingTable(Router router) {
        this.router = router;
        this.network = router.getNetwork();
        this.entryList = new ArrayList<>();
        links = network.getLinks();

    }

    /**
     * updateTable() should calculate routing information and then instantiate RoutingTableEntry objects, and finally add
     * them to RoutingTable object’s entryList.
     */
    public void updateTable() {
        // TODO: YOUR CODE HERE
        int size = network.getRouters().size()-1, index = 0;

        routerArray = new ArrayList<>();
        costArray = new ArrayList<>();
        remainingLinks = new ArrayList<>();

        for (Link link:links){
            remainingLinks.add(link);
            remainingLinks.add(new Link(link.getIpAddress2(),link.getIpAddress1(),link.getBandwidthInMbps()));
        }
        allRoutersPath = new ArrayList<>();
        entryList = new ArrayList<>();

        for (Router router:network.getRouters()){
            if (router.isDown()){
                Link found = findLink(router.getIpAddress());
                while (found != null){
                    remainingLinks.remove(found);
                    found = findLink(router.getIpAddress());
                }
            }
        }


        String routerID = router.getIpAddress();
        Link found = findLink(routerID);
        while (found != null){
            String dest = found.getOtherIpAddress(routerID);
            routerArray.add(dest);
            //costArray.set(routerArray.indexOf(dest),found.getCost());
            costArray.add(found.getCost());
            ArrayList<Link> linkStack = new ArrayList<>();
            linkStack.add(found);
            allRoutersPath.add(linkStack);
            remainingLinks.remove(found);
            found = findLink(routerID);
        }
        while (index <routerArray.size() && !remainingLinks.isEmpty()){
            routerID = routerArray.get(index);
            found = findLink(routerID);
            while (found!=null){
                String dest = found.getOtherIpAddress(routerID);
                if (!routerArray.contains(dest)){
                    routerArray.add(dest);
                    //costArray.set(routerArray.indexOf(dest),costArray.get(index)+found.getCost());
                    costArray.add(costArray.get(index)+found.getCost());
                    ArrayList<Link> linkStack = new ArrayList<>(allRoutersPath.get(index));
                    linkStack.add(found);
                    allRoutersPath.add(linkStack);

                }
                else {
                    int oldIndex =routerArray.indexOf(dest);
                    if(costArray.get(oldIndex) > costArray.get(index)+found.getCost()){
                        costArray.set(oldIndex,costArray.get(index)+found.getCost());
                        ArrayList<Link> linkStack = new ArrayList<>(allRoutersPath.get(index));
                        linkStack.add(found);
                        allRoutersPath.set(oldIndex,linkStack);
                    }
                }
                remainingLinks.remove(found);
                found = findLink(routerID);

            }
            index++;
        }
        for (Router controlRouter:network.getRouters()){
            if (!router.equals(controlRouter) && !routerArray.contains(controlRouter.getIpAddress())){
                routerArray.add(controlRouter.getIpAddress());
                costArray.add(Double.POSITIVE_INFINITY);
                ArrayList<Link> links = new ArrayList<>();
                allRoutersPath.add(links);
            }
        }

        for (int i=0;i<size;i++){
            RoutingTableEntry tableEntry = new RoutingTableEntry(router.getIpAddress(),routerArray.get(i),pathTo(routerArray.get(i)));
            entryList.add(tableEntry);
        }


    }

    /**
     * pathTo(Router destination) should return a Stack<Link> object which contains a stack of Link objects,
     * which represents a valid path from the owner Router to the destination Router.
     *
     * @param destination Destination router
     * @return Stack of links on the path to the destination router
     */
    public Stack<Link> pathTo(String destination) {
        Stack<Link> linkStack = new Stack<>();
        int index = routerArray.indexOf(destination);

        for (int i =allRoutersPath.get(index).size()-1;i>=0;i--){
            linkStack.push(allRoutersPath.get(index).get(i));
        }
        // TODO: YOUR CODE
        return linkStack;
    }


    private Link findLink(String routerID){
        for (Link link:remainingLinks){
            if (link.getIpAddress1().equals(routerID) && !link.getIpAddress2().equals(router.getIpAddress()))
                return link;
        }
        return null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoutingTable that = (RoutingTable) o;
        return router.equals(that.router) && entryList.equals(that.entryList);
    }

    public List<RoutingTableEntry> getEntryList() {
        return entryList;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Router getRouter() {
        return router;
    }

    public Network getNetwork() {
        return network;
    }

    public List<Link> getLinks() {
        return links;
    }
}
