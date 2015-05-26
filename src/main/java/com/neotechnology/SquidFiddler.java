package com.neotechnology;


import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.collection.IteratorUtil;


class SquidFiddler {

    private static final String NEO_DIR = "/Users/jsimpson/Documents/workspace/neo-technology/science/neo4j";
    private static final String CONF_PATH = NEO_DIR + "/conf/";
    private static final String DB_PATH = NEO_DIR + "/data/graph.db";
    private final GraphDatabaseService graphDb;


    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }

    private SquidFiddler() {
        graphDb = new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder(DB_PATH)
                .loadPropertiesFromFile(CONF_PATH + "neo4j.properties")
                .newGraphDatabase();

        registerShutdownHook(graphDb);

    }

    private ResourceIterator<Node> getPings() {
        System.out.print("Getting all the pings ...");

        Label label = DynamicLabel.label("Ping");
        ResourceIterator<Node> allPings;

        try (Transaction tx = graphDb.beginTx()) {
            allPings = graphDb.findNodes(label);
            tx.success();
        }
        System.out.println("... done.");
        return allPings;
    }

    private void applyFix(String propName, Node ping, Object property) {
        try (Transaction tx = graphDb.beginTx()) {
            Long newValue = new Long(property.toString());
            ping.setProperty(propName, newValue);
            tx.success();
        }
    }

    private boolean hasProperty(Node ping, String prop) {
        boolean hasProperty;
        try (Transaction tx = graphDb.beginTx()) {
            hasProperty = ping.hasProperty(prop);
            tx.success();
        }
        return hasProperty;
    }

    private Object getProperty(Node ping, String prop) {
        Object pingProperty;
        try (Transaction tx = graphDb.beginTx()) {
            pingProperty = ping.getProperty(prop);
            tx.success();
        }
        return pingProperty;
    }

    private void fixProperties() {
        String[] propertyNames = {"rels", "nodes", "labels", "props", "heapsize"};
        ResourceIterator<Node> pings = getPings();

        for (Node ping : IteratorUtil.asIterable(pings)) {

            for (String prop : propertyNames) {

                if (hasProperty(ping, prop)) {
                    Object property = getProperty(ping, prop);
                    if (property.getClass() == String.class) {
                        applyFix(prop, ping, property);
                    }
                }
            }

            System.out.print(".");

        }
        System.out.println("done");
    }


    public static void main(String[] args) {
        new SquidFiddler().fixProperties();
    }

}


