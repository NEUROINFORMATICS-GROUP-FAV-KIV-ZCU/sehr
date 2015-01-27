package pl.mateusz;
import java.io.File;
import java.util.ArrayList;
import org.openehr.am.archetype.Archetype;
import org.openehr.am.archetype.constraintmodel.CObject;
import se.acode.openehr.parser.ADLParser;

/**
 * @author Erasmus students
 */

public class ArchetypeManager 
{
    /***
     * Method is getting number of fields from an Archetype object.
     * @param archetype An Archetype object.
     * @return Number of fields in an Archetype.
     */
    public int getNumberOfFields(Archetype archetype)
    {
        //fields are getting from default language
        return archetype.getDefinition().getAttributes().get(0).getChildren().size();
    }
  
    /***
     * Method is getting archetype name
     * @param archetype An Archetype object
     * @return Archetype name
     */
    public String getArchetypeName(Archetype archetype)
    {
        return archetype.getArchetypeId().conceptName();
    }        
    
    /**
     * Method getting field name from path included in archetype
     * @param path String object included in an archetype object
     * @return Field name
     */
    public String getNameFromPath(String path)
    {
        if(path.indexOf('[') != -1)
        {
            int startIndex = path.indexOf('[') + 1;
            int endIndex = path.indexOf(']');
            
            return (String) path.subSequence(startIndex, endIndex);
        } 
        return null;   
    } 
    
    /**
     * Method getting CObject from an archetype
     * @param archetype An archetype object
     * @param fieldIndex Index of children
     * @return CObject from an archetype
     */
    public CObject getCObject(Archetype archetype, int fieldIndex)
    {
        return archetype.getDefinition().getAttributes().get(0).getChildren().get(fieldIndex);
    }

    /**
     * Method is parsing a list of Files to an Archetype object
     * @param files List of files with archetypes
     * @return Table of Archetypes
     */
    public Archetype[] parseFiles(ArrayList<File> files)
    {
        Archetype[] archetypes = new Archetype[files.size()];
        
        for(int i=0; i<files.size(); ++i)
        {
           try 
           {
               archetypes[i] = new ADLParser(files.get(i)).archetype();
           } 
           catch (Exception ex) 
           {
               System.out.print("Error message: "+ex.getMessage());
           }
        } 
        return archetypes;
    }
    
}
