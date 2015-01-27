package pl.mateusz;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import org.openehr.am.archetype.Archetype;
import org.openehr.am.archetype.constraintmodel.ArchetypeInternalRef;
import org.openehr.am.archetype.constraintmodel.ArchetypeSlot;
import org.openehr.am.archetype.constraintmodel.CAttribute;
import org.openehr.am.archetype.constraintmodel.CComplexObject;
import org.openehr.am.archetype.constraintmodel.CDomainType;
import org.openehr.am.archetype.constraintmodel.CObject;
import org.openehr.am.archetype.constraintmodel.CPrimitiveObject;
import org.openehr.am.archetype.constraintmodel.ConstraintRef;
import org.openehr.am.openehrprofile.datatypes.quantity.CDvOrdinal;
import org.openehr.am.openehrprofile.datatypes.quantity.CDvQuantity;

public class Elasticsearch 
{
    /**
     * Fields
     */
    private XContentBuilder document;
    private XContentBuilder mapping;
    private final ArchetypeManager archetypeManager = new ArchetypeManager();
    
    /**
     * Method creating a new Client object.
     * @param ip IPv4 Address
     * @param port Port
     * @return Client Object
     */              
    public Client createClient(String ip, int port) 
    {
        ImmutableSettings.Builder settings = ImmutableSettings.settingsBuilder();
        TransportClient client = new TransportClient(settings);
        client = client.addTransportAddress(new InetSocketTransportAddress(ip, port));
        return client;
    }
    
    /**
     * Method is creating mapping for specific archetype
     * @param documentType Document type
     * @param archetype An Archetype object
     * @return Mapping object to  XContentBuilder
     * @throws IOException 
     */
    public XContentBuilder createMapping(String documentType, Archetype archetype) throws IOException
    {
        this.document = jsonBuilder().startObject().prettyPrint();
        
        this.mapping = jsonBuilder().startObject().startObject(documentType).startObject("properties");
        
        int count = this.archetypeManager.getNumberOfFields(archetype);
        
        for(int i=0; i<count; ++i)
        {
            this.printCObject(this.archetypeManager.getCObject(archetype, i));
        }
        
        this.mapping.endObject().endObject().endObject();
        
        return this.mapping;
    }
    
    /**
     * Method creating an new index
     * @param client Client object
     * @param name Name for new index
     * @return New index
     */
    public CreateIndexRequestBuilder createIndex(Client client, String name)
    {
        IndicesExistsResponse exist = client.admin().indices().prepareExists(name).execute().actionGet();
   
        //if index already exist index is deleting
        if (exist.isExists())
        {
            DeleteIndexRequestBuilder deleteIndex = client.admin().indices().prepareDelete(name);
            deleteIndex.execute().actionGet();
        }
        
        CreateIndexRequestBuilder index = client.admin().indices().prepareCreate(name);
        
        return index;
    }    
        
    /**
     * Methods is adding new field to mapping
     * @param path Path to field
     * @param type Field type
     * @throws IOException 
     */     
    private void addFieldToMapping(String path, String type) throws IOException
    {
        //drugim razem tutaj jest null!!! dlatego 
        String fieldName = path;
        
        if(fieldName != null && !type.equals("ELEMENT"))
        {
            this.mapping.startObject(fieldName)
                    .field("type", "multi_field")
                    .startObject("fields")
                    .startObject(fieldName)
                    .field("type", type)
                    .endObject()
                    .startObject("original")
                    .field("type", type)
                    .field("index", "not_analyzed")
                    .endObject()
                    .endObject()
                    .endObject();  
        
            //for document

            if("double".equals(type))
            {
                this.document.field(fieldName, 0.001);
            }

            if("string".equals(type))
            {
                this.document.field(fieldName, "Mateusz Łysień");
            }

            if("integer".equals(type))
            {
                this.document.field(fieldName, 24);
            }        

            if("date".equals(type))
            {
                this.document.field(fieldName, "2014/01/10");
            } 

            if("boolean".equals(type))
            {
                this.document.field(fieldName, true);
            }
        }    
    }

    
    public XContentBuilder getDocumentContent() throws IOException
    {
        this.document.endObject();
        return this.document;
    }
    
    /**
     * Method is using to recognize archetype field type
     * @param cobj CObject 
     * @throws IOException 
     */
    private void printCObject(CObject cobj) throws IOException 
    {
        if (cobj instanceof CDomainType) 
        {
            printCDomainType((CDomainType) cobj);
        } 
        else if (cobj instanceof CPrimitiveObject) 
        {
            printCPrimitiveObject((CPrimitiveObject) cobj);
        } 
        else if (cobj instanceof CComplexObject) 
        {
            printCComplexObject((CComplexObject) cobj);
        } 
        else if (cobj instanceof ArchetypeInternalRef) 
        {
            printArchetypeInternalRef((ArchetypeInternalRef) cobj);
        } 
        else if (cobj instanceof ArchetypeSlot) 
        {
            printArchetypeSlot((ArchetypeSlot) cobj);
        }
        else if (cobj instanceof ConstraintRef) 
        {
            printConstraintRef((ConstraintRef) cobj);
        }
    }    
    
    /**
     * Method is using to add CComplexObject to mapping
     * @param ccobj CComplexObject
     * @throws IOException 
     */
    private void printCComplexObject(CComplexObject ccobj) throws IOException 
    {
        String type = ccobj.getRmTypeName();
        String path = ccobj.path();
        this.recognizeField(type, path);

        
        if (!ccobj.isAnyAllowed()) 
        {
            for (CAttribute cattribute : ccobj.getAttributes()) 
            {
                printCAttribute(cattribute);
            }
        } 
    }

    /**
     * Method is using to add CAttribute to mapping
     * @param cattribute CAttribute
     * @throws IOException 
     */    
    private void printCAttribute(CAttribute cattribute) throws IOException 
    {
        List<CObject> children = cattribute.getChildren();

        if (children.size() != 1 || !(children.get(0) instanceof CPrimitiveObject)) 
        {
            for (CObject cobject : cattribute.getChildren()) 
            {
                printCObject(cobject);
            }
        } 
    }
    
    /**
     * Method is using to add ArchetypeInternalRef to mapping
     * @param ref ArchetypeInternalRef object
     * @throws IOException 
     */
    private void printArchetypeInternalRef(ArchetypeInternalRef ref) throws IOException
    {
        String type = ref.getRmTypeName();
        String path = ref.path();
        this.recognizeField(type, path);
    }

    /**
     * Method is using to add ArchetypeSlot object to mapping
     * @param slot ArchetypeSlot object
     * @throws IOException 
     */
    private void printArchetypeSlot(ArchetypeSlot slot) throws IOException 
    {
        String type = slot.getRmTypeName();
        String path = slot.path();
        this.recognizeField(type, path);      
    }

    /**
     * Method is using to add ConstraintRef object to mapping
     * @param ref ConstraintRef object
     * @throws IOException 
     */
    private void printConstraintRef(ConstraintRef ref) throws IOException 
    {
        String type = ref.getRmTypeName();
        String path = ref.path();
        this.recognizeField(type, path);         
    }

    /**
     * Method is using to add CDvOrdinal object to mapping
     * @param cordinal CDvOrdinal object
     * @throws IOException 
     */
    private void printCDvOrdinal(CDvOrdinal cordinal) throws IOException 
    {
        String type = cordinal.getRmTypeName();
        String path = cordinal.path();
        this.recognizeField(type, path);    
    }

    /**
     * Method is using to add CDvQuantity object to mapping
     * @param cquantity CDvQuantity object
     * @throws IOException 
     */
    private void printCDvQuantity(CDvQuantity cquantity) throws IOException 
    {
        String type = cquantity.getRmTypeName();
        String path = cquantity.path();
        this.recognizeField(type, path);
    }
	
    /**
     * Method is using to add CPrimitiveObject object to mapping
     * @param cpo CPrimitiveObject object
     * @throws IOException 
     */
    private void printCPrimitiveObject(CPrimitiveObject cpo) throws IOException 
    {
        String type = cpo.getRmTypeName();
        String path = cpo.path();
        this.recognizeField(type, path); 
    }
    
    /**
     * Method is using to add CDomainType object to mapping
     * @param cdomain CDomainType object
     * @throws IOException 
     */
    private void printCDomainType(CDomainType cdomain) throws IOException 
    {
        if (cdomain instanceof CDvOrdinal) 
        {
            printCDvOrdinal((CDvOrdinal) cdomain);
        } 
        else if (cdomain instanceof CDvQuantity) 
        {
            printCDvQuantity((CDvQuantity) cdomain);
        }
    }
    
    /**
     * Method is using to recognize field type
     * @param type Field type 
     * @param path Field path
     * @throws IOException 
     */
    private void recognizeField(String type, String path) throws IOException 
    {
        path = this.archetypeManager.getNameFromPath(path);
       
        if("DATA_VALUE".equals(type)) this.addFieldToMapping(path, "double");
        
        if("DV_BOOLEAN".equals(type)) this.addFieldToMapping(path, "boolean");
        if("DV_STATE".equals(type)) this.addFieldToMapping(path, "boolean");
        if("DV_IDENTIFIER".equals(type)) this.addFieldToMapping(path, "string");
        if("DV_PARAGRAPH".equals(type)) this.addFieldToMapping(path, "string");
        if("DV_ORDERED".equals(type)) this.addFieldToMapping(path, "double");
        if("DV_INTERVAL".equals(type)) this.addFieldToMapping(path, "double");
        if("REFERENCE_RANGE".equals(type)) this.addFieldToMapping(path, "double");
        if("DV_ORDINAL".equals(type)) this.addFieldToMapping(path, "double");
        if("DV_QUANTIFIED".equals(type)) this.addFieldToMapping(path, "double");
        if("DV_MEASURABLE".equals(type)) this.addFieldToMapping(path, "double");
        if("DV_QUANTITY".equals(type)) this.addFieldToMapping(path, "double");
        if("DV_COUNT".equals(type)) this.addFieldToMapping(path, "integer");
        if("DV_PROPERTION".equals(type)) this.addFieldToMapping(path, "double");
        if("PROPORTION_KIND".equals(type)) this.addFieldToMapping(path, "double");
        if("DV_CUSTOMARY_QUANTITY".equals(type)) this.addFieldToMapping(path, "double");
        
        if("DV_DATE".equals(type)) this.addFieldToMapping(path, "date");
        if("DV_TIME".equals(type)) this.addFieldToMapping(path, "double");
        if("DV_DATE_TIME".equals(type)) this.addFieldToMapping(path, "date");
        if("DV_DURATION".equals(type)) this.addFieldToMapping(path, "double");
        if("DV_TIME_SPECIFICATION".equals(type)) this.addFieldToMapping(path, "double");
        if("DV_PERIODIC_TIME_SPECIFICATION".equals(type)) this.addFieldToMapping(path, "double");
        if("DV_GENERAL_TIME_SPECIFICATION".equals(type)) this.addFieldToMapping(path, "double");
        
        if("DV_URI".equals(type)) this.addFieldToMapping(path, "string");
        if("DV_EHR_URI".equals(type)) this.addFieldToMapping(path, "string");
        if("DV_PARSABLE".equals(type)) this.addFieldToMapping(path, "string");
        if("DV_ENCAPSULATED".equals(type)) this.addFieldToMapping(path, "string");
        if("DV_URI".equals(type)) this.addFieldToMapping(path, "double");
        if("DV_URI".equals(type)) this.addFieldToMapping(path, "double");
        
        
        
        if("DV_PROPORTION".equals(type)) this.addFieldToMapping(path, "double");
        if("DV_TEXT".equals(type)) this.addFieldToMapping(path, "string");
        if("DV_MULTIMEDIA".equals(type)) this.addFieldToMapping(path, "string");              
        if("DvOrdinal".equals(type)) this.addFieldToMapping(path, "double");
        if("DvQuantity".equals(type)) this.addFieldToMapping(path, "double");
    }
       
}