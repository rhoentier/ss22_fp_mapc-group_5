package massim.javaagents.percept;

/**
 *
 * @author Alexander Lorenz
 */
public class NextNormRequirement {

    /*        
    requirement:
            type : the subject of the norm
            name : the precise name the subject refers to, e.g., the role constructor
            quantity : the maximum quantity that can be carried/adopted
            details : possibly additional details
    Examples:
    Created norm(name=n3,announcedat=231,start=250,until=389,level=team,req=[subject(role, default, 2, ),],pun=19)
    Created norm(name=n2,announcedat=66,start=82,until=230,level=individual,req=[subject(block, any, 1, ),],pun=13)
    Created norm(name=n1,announcedat=10,start=20,until=62,level=team,req=[subject(role, explorer, 0, ),],pun=10)
     */

    private String type;
    private String name;
    private int quantity;
    private String details;

    public NextNormRequirement(String type, String name, int quantity, String details) {
        this.type = type;
        this.name = name;
        this.quantity = quantity;
        this.details = details;
    }

    public NextNormRequirement(String type, String name, int quantity) {
        this(type, name, quantity, "");
    }

    public String GetType() {
        return type;
    }

    public void SetType(String type) {
        this.type = type;
    }

    public String GetName() {
        return name;
    }

    public void SetName(String name) {
        this.name = name;
    }

    public int GetQuantity() {
        return quantity;
    }

    public void SetQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String GetDetails() {
        return details;
    }

    public void SetDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "NextNormRequirement{" + "type=" + type + ", name=" + name + ", quantity=" + quantity + ", details=" + details + '}';
    }

}
