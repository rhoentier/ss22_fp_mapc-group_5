/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package massim.javaagents.agents;

import java.util.HashSet;

/**
 *
 * @author AVL
 */
public class NextNorm {
    /*        
    
    norm(id, start, end, [requirement(type, name, quantity, details), ...], fine)

    id : Identifier - ID of the norm
    start : Numeral - first step the norm holds
    end : Numeral - last step the norm holds
        requirement:
            type : the subject of the norm
            name : the precise name the subject refers to, e.g., the role constructor
            quantity : the maximum quantity that can be carried/adopted
            details : possibly additional details
    fine : Numeral - the energy cost of violating the norm (per step)

    */
    
    private String id;
    private int start;
    private int end;
    private HashSet<NextNormRequirement> requirement;
    private int fine;

    public NextNorm(String id, int start, int end, HashSet<NextNormRequirement> requirement, int fine) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.requirement = requirement;
        this.fine = fine;
    }

    public String GetId() {
        return id;
    }

    public void SetId(String id) {
        this.id = id;
    }

    public int GetStart() {
        return start;
    }

    public void SetStart(int start) {
        this.start = start;
    }

    public int GetEnd() {
        return end;
    }

    public void SetEnd(int end) {
        this.end = end;
    }

    public HashSet<NextNormRequirement> GetRequirement() {
        return requirement;
    }

    public void SetRequirement(HashSet<NextNormRequirement> requirement) {
        this.requirement = requirement;
    }

    public int GetFine() {
        return fine;
    }

    public void SetFine(int fine) {
        this.fine = fine;
    }

    @Override
    public String toString() {
        return "NextNorm{" + "id=" + id + ", start=" + start + ", end=" + end + ", requirement=" + requirement + ", fine=" + fine + '}';
    }
    
    
}
