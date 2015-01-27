
package pl.mateusz;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author Karol
 * class Testing opening file ADLS
 */
@RunWith(value = Parameterized.class)
public class IOManagerTest {
    
    private String fileName;
    public IOManagerTest(String fileName) {
       this.fileName=fileName;
    }
    /**
     * Parametrizer function
     * @return  new Object to inicjalize
     */
    
       @Parameterized.Parameters()
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"ADLS"}});
    }

    /**
     * Test of LoadFiles method, of class IOManager.
     */
    @Test
    public void testLoadFiles() {
        IOManager instance = new IOManager();
        
        instance.loadFiles(fileName);        
        ArrayList<File> result = instance.getFiles();
        assertNotNull(result.size());
      
    }
    
}
 
