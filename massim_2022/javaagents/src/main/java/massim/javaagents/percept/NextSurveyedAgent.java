/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package massim.javaagents.percept;

/**
 * Container class for describing surveyed Agents.  
 * @author AVL
 */
public class NextSurveyedAgent {
    
    private String name;
    private String role;
    private int energy;

    public NextSurveyedAgent(String name, String role, int energy) {
        this.name = name;
        this.role = role;
        this.energy = energy;
    }

    public String GetName() {
        return name;
    }

    public void SetName(String name) {
        this.name = name;
    }

    public String GetRole() {
        return role;
    }

    public void SetRole(String role) {
        this.role = role;
    }

    public int GetEnergy() {
        return energy;
    }

    public void SetEnergy(int energy) {
        this.energy = energy;
    }
    
    
    
    
}
