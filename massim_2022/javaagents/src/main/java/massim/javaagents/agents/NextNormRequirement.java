/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package massim.javaagents.agents;

/**
 *
 * @author AVL
 */
class NextNormRequirement {
    /*        
    requirement:
            type : the subject of the norm
            name : the precise name the subject refers to, e.g., the role constructor
            quantity : the maximum quantity that can be carried/adopted
            details : possibly additional details
    Created norm(name=n3,announcedat=231,start=250,until=389,level=team,req=[subject(role, default, 2, ),],pun=19)
    Created norm(name=n2,announcedat=66,start=82,until=230,level=individual,req=[subject(block, any, 1, ),],pun=13)
    Created norm(name=n1,announcedat=10,start=20,until=62,level=team,req=[subject(role, explorer, 0, ),],pun=10)
    Created norm(name=n4,announcedat=321,start=332,until=512,level=individual,req=[subject(block, any, 1, ),],pun=20)
    Created norm(name=n3,announcedat=285,start=297,until=319,level=team,req=[subject(role, default, 0, ),],pun=9)
    Created norm(name=n2,announcedat=74,start=84,until=281,level=team,req=[subject(role, digger, 3, ),],pun=10)
    Created norm(name=n1,announcedat=18,start=32,until=72,level=team,req=[subject(role, worker, 0, ),],pun=6)
    
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
