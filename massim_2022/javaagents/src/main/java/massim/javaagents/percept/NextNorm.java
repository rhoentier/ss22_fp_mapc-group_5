package massim.javaagents.percept;

import java.util.HashSet;

/**
 *
 * @author Alexander Lorenz
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

    /**
     *
     * @param id
     * @param start
     * @param end
     * @param requirement
     * @param fine
     */
    public NextNorm(String id, int start, int end, HashSet<NextNormRequirement> requirement, int fine) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.requirement = requirement;
        this.fine = fine;
    }

    /**
     *
     * @return
     */
    public String GetId() {
        return id;
    }

    /**
     *
     * @param id
     */
    public void SetId(String id) {
        this.id = id;
    }

    /**
     *
     * @return
     */
    public int GetStart() {
        return start;
    }

    /**
     *
     * @param start
     */
    public void SetStart(int start) {
        this.start = start;
    }

    /**
     *
     * @return
     */
    public int GetEnd() {
        return end;
    }

    /**
     *
     * @param end
     */
    public void SetEnd(int end) {
        this.end = end;
    }

    /**
     *
     * @return
     */
    public HashSet<NextNormRequirement> GetRequirement() {
        return requirement;
    }

    /**
     *
     * @param requirement
     */
    public void SetRequirement(HashSet<NextNormRequirement> requirement) {
        this.requirement = requirement;
    }

    /**
     *
     * @return
     */
    public int GetFine() {
        return fine;
    }

    /**
     *
     * @param fine
     */
    public void SetFine(int fine) {
        this.fine = fine;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return "NextNorm{" + "id=" + id + ", start=" + start + ", end=" + end + ", requirement=" + requirement + ", fine=" + fine + '}';
    }

}
