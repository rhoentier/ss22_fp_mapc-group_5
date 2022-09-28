package massim.javaagents.percept;

/**
 * Description of a norm provided by the server.
 * 
 *  Examples:
 *   subject(role, default, 2, )
 *   subject(block, any, 1, )
 *   subject(role, explorer, 0, )
 * 
 * @author Alexander Lorenz
 */
public class NextNormRequirement {


    /*
     * ########## region fields
     */

    private String type;        // the subject of the norm
    private String name;        // the precise name the subject refers to, e.g., the role constructor
    private int quantity;       // the maximum quantity that can be carried/adopted
    private String details;     // possible additional details

    /*
     * ##################### endregion fields
     */

    /**
     * ########## region constructor.
     *
     * Data is provided by the server
     * 
     * @param type String type of requirement, e.g. role, block
     * @param name String precise specification of subject
     * @param quantity int allowed quantity
     * @param details String possible details
     */
    public NextNormRequirement(String type, String name, int quantity, String details) {
        this.type = type;
        this.name = name;
        this.quantity = quantity;
        this.details = details;
    }

    /**
     * Simplified constructor
     * 
     * Data is provided by the server
     * 
     * @param type String type of requirement, e.g. role, block
     * @param name String precise specification of subject
     * @param quantity int allowed quantity
     */
    public NextNormRequirement(String type, String name, int quantity) {
        this(type, name, quantity, "");
    }

    /*
     * ##################### endregion constructor
     */
    
    /*
     * ########## region public methods
     */
    
    /**
     * Retrieves the type of the requirement
     * 
     * @return String type of requirement, e.g. role, block
     */
    public String GetType() {
        return type;
    }

    /**
     * Retrieves the the precise name the subject of the norm
     * refers to e.g., the role constructor
     * 
     * @return String precise specification of subject
     */
    public String GetName() {
        return name;
    }
    /**
     * Retrieves the allowed amount of specified subject
     *
     * @return int permitted quantity
     */
    public int GetQuantity() {
        return quantity;
    }

    /**
     * Retrieves additional possible details
     * 
     * @return String possible further details
     */
    public String GetDetails() {
        return details;
    }

    /**
     * This implementation rformats the stored values to a string
     * 
     * @return String formatted for representation
     */
    
    @Override
    public String toString() {
        return "NextNormRequirement{" + "type=" + type + ", name=" + name + ", quantity=" + quantity + ", details=" + details + '}';
    }
    
    /*
     * ##################### endregion public methods
     */
}
