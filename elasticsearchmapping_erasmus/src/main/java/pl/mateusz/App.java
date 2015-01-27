package pl.mateusz;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.node.Node;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import org.openehr.am.archetype.Archetype;


/**
 * @author Erasmus Students
 */
public class App {

    public static void main(String[] args)
    {
        //objects initialization
        ArchetypeManager archetypeManager = new ArchetypeManager();
        IOManager IOManager = new IOManager();
        Elasticsearch elasticsearch = new Elasticsearch();
        
        //getting files from directory
        IOManager.loadFiles("adls");
        ArrayList<File> files = IOManager.getFiles();
        
        //parsing files from directory to an Archetype's table
        Archetype[] archetypes = archetypeManager.parseFiles(files);

        //creating local node
        Node node = nodeBuilder().local(true).node();
        
        //creating a Client object from localhost address and port - 9300
        Client client = elasticsearch.createClient("127.0.0.1", 9300);

        //number of loaded archetypes
        int count = archetypes.length;
        
         
        try 
        {
            for(int i=0; i < count; ++i)
            {
                archetypeManager.getNumberOfFields(archetypes[i]);
                String indexName = archetypeManager.getArchetypeName(archetypes[i]);
                String documentType = "archetype";
                String documentId = "1";
                
                //creating index
                CreateIndexRequestBuilder index = elasticsearch.createIndex(client, indexName);

                //creating mapping
                XContentBuilder mappingBuilder = elasticsearch.createMapping(documentType, archetypes[i]);
                index.addMapping(documentType, mappingBuilder);
                index.execute().actionGet();

                //adding a document to index
                IndexRequestBuilder indexRequestBuilder = client.prepareIndex(indexName, documentType, documentId);
                XContentBuilder content = elasticsearch.getDocumentContent();
                indexRequestBuilder.setSource(content);
                indexRequestBuilder.execute().actionGet();                
            }
        
        } catch (IOException ex) {
            System.out.print("Error message: "+ex.getMessage());
        }

        //closing node
        node.close();
        
        //closing client
        client.close();
    }
}
