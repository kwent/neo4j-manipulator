Neo4j Manipulator
==================

Manipulate your graph db data directly in java.

1. Build it:

        mvn clean compile assembly:single

2. Stop the database

3. Run the following command in the root folder:

        java -cp "target/neo4j-manipulator-1.0-SNAPSHOT-jar-with-dependencies.jar:*" org.neo4j.Manipulator <path to your neo4j/data/graph.db>

For example:

        java -cp "target/neo4j-manipulator-1.0-SNAPSHOT-jar-with-dependencies.jar:*" org.neo4j.Manipulator /Users/kwent/Projects/neography/neo4j/data/graph.db
