
package pl.mateusz;

import java.util.Arrays;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author Karol
 * Class Testing Create index and create client
 */
@RunWith(value = Parameterized.class)
public class ElasticsearchTest {
   private String ip ;
   private int port ;
        
    public ElasticsearchTest(String ip, int port) {
    this.ip=ip;
    this.port=port;
    }
 
          @Parameterized.Parameters()
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"127.0.0.1",9306},
            {"128.0.0.1",9300},
            {"138.0.12.1",9200},
            {"128.0.0.1",0},
            {"128.0.0.1",9101}
            });
    }

   /**
    * Testing method tests throw exception NoNodeAvailableException
    */
   @Test(expected = NoNodeAvailableException.class)
    public void testCreateIndex() {
        Elasticsearch instance = new Elasticsearch();
        Client client = instance.createClient(ip, port);
 
        String name = "indeks0"; 
        CreateIndexRequestBuilder result = instance.createIndex(client, name);
    }
    
    /**
     * Testing time create Client.
     * Testing methods tests how long method create client, if more than 5 second test is fail
     */
    @Test(timeout = 5000)  
     public void testCreateIndexTime() {
        Elasticsearch instance = new Elasticsearch();
        Client client = instance.createClient("127.0.0.1", 9300);
        String name = "indeks0"; 
        CreateIndexRequestBuilder result = instance.createIndex(client, name);
    }
    
}
