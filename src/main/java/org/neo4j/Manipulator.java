package org.neo4j;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.*;
import org.neo4j.index.lucene.LuceneTimeline;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.WildcardQuery;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;
import org.neo4j.graphdb.Transaction;
import java.nio.charset.Charset;

public class Manipulator{

    static GraphDatabaseService graphDb = null;
    static String arg1;
    private static final RelationshipType LIVES_IN = DynamicRelationshipType.withName("lives_in");

    /**
     * Connection to the graph db store. Make sure the database is stopped when running this.
     *
     * @param pathToDatabase path to the database.
     */
     
    public static void main(final String[] args) {
        
        graphDb = buildGraphDb(args[0]);
        arg1 = args[1];
        
        for (String s: args) {
            System.out.println(s);
        }
        manipulator();
        
    }
    
    /**
     * Proceed any operation you want there.
     */
     
    private static void manipulator() {
        //fix_orientation_lives_in();
        //cleanup_users_nodes();
        try
        {
            byte[] bytes = arg1.getBytes("UTF-8");
            search(new String(bytes, "UTF-8"));
        }
        catch (java.io.UnsupportedEncodingException e) {}

    }
    
    private static void search(String pattern) {
        
        // Example for Céline carron
        // java -cp "target/neo4j-manipulator-1.0-SNAPSHOT-jar-with-dependencies.jar:*" org.neo4j.Manipulator ../down-neo4j/neo4j/data/graph.db Cé\*
        
        Index<Node> index = graphDb.index().forNodes("search_idx");
        IndexHits<Node> index_hits = index.query( new WildcardQuery( new Term( "name", pattern ) ) );
        System.out.println( "SEARCH RESULTS FOUND : " + index_hits.size());
        
        while (index_hits.hasNext())
        {
            Node node = index_hits.next(); 
            System.out.println("ID :" + node.getId());
            System.out.println("NAME :" + node.getProperty("name").toString());
        }
        
    }
    
    // SOME LIVES_IN RELATIONSHIP ARE THE WRONG WAY NODE2 - NODE1
    // THIS FUNCTION REVERSE LIVES_IN RELATION WAY REVERSE WAY NODE1 - NODE2
    private static void fix_orientation_lives_in() {
           
      Index<Relationship> index = graphDb.index().forRelationships("lives_in_idx");
      IndexHits<Relationship> index_hits = index.query( new WildcardQuery( new Term( "rel", "*" ) ) );
      System.out.println( "LIVES_IN_RELATIONSHIPS FOUND : " + index_hits.size());
    
      while (index_hits.hasNext())
      {
        Relationship rel = index_hits.next(); 
        Node start_node = rel.getStartNode();
        Node end_node = rel.getEndNode();
        Node user_node = null;
        Node location_node = null;
        
        if (end_node.hasProperty("fb_id"))
        {
            user_node = end_node;
            location_node = start_node;
        }
        else
        {
            System.out.println("NOTHING TO FIX");
        }
        
        if (user_node != null && location_node != null)
        {
            Transaction tx = graphDb.beginTx();
            
            try
            {
                IndexHits<Relationship> already_existing = index.query( new WildcardQuery( new Term( "rel", user_node.getProperty("fb_id").toString() + "-" + location_node.getProperty("id").toString() ) ) );
                
                if(already_existing.size() == 1)
                {
                    System.out.println("FIXING");
                    Relationship new_rel = user_node.createRelationshipTo(location_node, LIVES_IN);
                    index.add(new_rel, "rel", user_node.getProperty("fb_id").toString() + "-" + location_node.getProperty("id").toString() );
                }
                
                rel.delete();
                tx.success();
            }
            finally
            {
                tx.finish();
            }
        }
        
      }
          
    }
    
    // SOME USER NODES CONTAINES USELESS PROPERTY AND ARE WITHOUT FB_ID
    // THIS FUNCTION CLEANUP ALL NODE AND DELETE THEM IF NECESSARY (IF NO FB_ID SO FAR)
    private static void cleanup_users_nodes()
    {    
        Iterable<Node> nodes = GlobalGraphOperations.at(graphDb).getAllNodes();
    
        // IndexHits<Node> nodes = graphDb.index().forNodes("users").get("fb_id", 755883446);
        // System.out.println("TOTAL NODES : " + nodes.size());
        
        for(Node n : nodes)
        {    
            if(n.hasProperty("fb_id") || n.hasProperty("relationship_status")) //We know that it's a user
            {
                System.out.println("USER NODE ID : " + n.getId());
                
                if (n.hasProperty("pics")) n.removeProperty("pics");
                if (n.hasProperty("pic_small")) n.removeProperty("pic_small");
                if (n.hasProperty("pic_big")) n.removeProperty("pic_big");
                if (!n.hasProperty("fb_id"))
                {
                    Transaction tx = graphDb.beginTx();
                    
                    try
                    {
                        System.out.println("DELETE NODE ID : " + n.getId());
                        n.delete();
                        //AUTOREMOVED FROM INDEX WHEN CALLING THAT
                        tx.success();
                    }
                    finally
                    {
                        tx.finish();
                    }
                }
            }
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