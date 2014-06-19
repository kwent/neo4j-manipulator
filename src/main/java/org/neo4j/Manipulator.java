package org.neo4j;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;

public class Manipulator{

    static GraphDatabaseService graphDb = null;

    /**
     * Connection to the graph db store. Make sure the database is stopped when running this.
     *
     * @param pathToDatabase path to the database.
     */
     
    public static void main(final String[] args) {
        
        graphDb = buildGraphDb(args[0]);
        manipulator();
        
    }
    
    /**
     * Proceed any operation you want there.
     */
     
    private static void manipulator() {
        
        Iterable<Node> nodes = GlobalGraphOperations.at(graphDb).getAllNodes();
            
        for(Node n : nodes) {
            System.out.println(n.getId());
        }    
    }
    
    /**
     * Build a GraphDatabaseService object
     *
     * @param pathToDatabase path to the database.
     */
     
    private static GraphDatabaseService buildGraphDb(String pathToDatabase) {
        
        return new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder( pathToDatabase )
                .newGraphDatabase();
    }

}