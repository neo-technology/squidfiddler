package com.neotechnology;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.collection.IteratorUtil;


class SquidFiddler {
    private static final String NODES = "nodes";
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

        Label label = DynamicLabel.label("Ping");
        ResourceIterator<Node> allPings;

        try (Transaction tx = graphDb.beginTx()) {
            allPings = graphDb.findNodes(label);
            tx.success();
        }
        return allPings;
    }

    private void fix_labels() {

        ResourceIterator<Node> allPings = getPings();
        try (Transaction tx = graphDb.beginTx()) {
            for (Node ping : IteratorUtil.asIterable(allPings)) {

                if (ping.hasProperty(NODES)) {

                    Object property = ping.getProperty(NODES);
                    if (property.getClass() == String.class) {
                        System.out.println("setting " + property + " back to long");
                        ping.setProperty(NODES, new Long(property.toString()));
                    }


                }


            }
            tx.success();
        }

    }

    public static void main(String[] args) {
        new SquidFiddler().fix_labels();
    }

}


