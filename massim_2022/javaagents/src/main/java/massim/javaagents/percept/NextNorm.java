package massim.javaagents.percept;

import java.util.HashSet;

/**
 * Compilation of norm informations
 * 
 * Data recieved from Server:
 *  
 * norm(id, start, end, [requirement(type, name, quantity, details), ...], fine)
 * id : Identifier - ID of the norm
 * start : Numeral - first step the norm holds
 * end : Numeral - last step the norm holds
 * 
 * requirement:
 *         type : the subject of the norm
 *         name : the precise name the subject refers to, e.g., the role constructor
 *         quantity : the maximum quantity that can be carried/adopted
 *         details : possibly additional details
 * fine : Numeral - the energy cost of violating the norm (per step)
 * 
 * Examples:
 *   name=n3,start=250,until=389,level=team,req=[subject(role, default, 2, ),],pun=19
 *   name=n2,start=82,until=230,level=individual,req=[subject(block, any, 1, ),],pun=13
 *   name=n1,start=20,until=62,level=team,req=[subject(role, explorer, 0, ),],pun=10
 *    
 * 
 * @author Alexander Lorenz
 */
public class NextNorm {

    /*
     * ########## region fields
     */

    private String id;                                      // ID of the norm
    private int start;                                      // start of validity
    private int end;                                        // end of validity
    private HashSet<NextNormRequirement> requirement;       // collection of rsules
    private int fine;                                       // cost of violating
 
    /*
     * ##################### endregion fields
     */

    /**
     * ########## region constructor.
     * 
     * @param id String describing the identification
     * @param start int startstep of validity
     * @param end int endstep of validity
     * @param requirement NextNormRequirement HashSet describing rules
     * @param fine int energycost if violated
     */
    public NextNorm(String id, int start, int end, HashSet<NextNormRequirement> requirement, int fine) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.requirement = requirement;
        this.fine = fine;
    }
    
    /*
     * ##################### endregion constructor
     */
    
    /*
     * ########## region public methods
     */

    /**
     * Retrieves the Norm ID
     * 
     * @return String describing the identification
     */
    public String GetId() {
        return id;
    }

    /**
     * Retrieves the first step the norm is valid
     * 
     * @return int step of the simulation
     */
    public int GetStart() {
        return start;
    }

    /**
     * Retrieves the last step the norm is valid
     * 
     * @return int step of the simulation
     */
    public int GetEnd() {
        return end;
    }

    /**
     * Retrieves the collection of requirements, to not get punished by server
     * 
     * @return NextNormRequirement HashSet of rules and regulations
     */
    public HashSet<NextNormRequirement> GetRequirement() {
        return requirement;
    }

    /**
     * Retrieves the penalty for violating the norm
     * 
     * @return int amount of energy lost per round
     */
    public int GetFine() {
        return fine;
    }

    /**
     * This implementation returns a small part of the stored values as a string
     * 
     * @return String formatted for representation
     */
    @Override
    public String toString() {
        return "NextNorm{" + "id=" + id + ", start=" + start + ", end=" + end + ", requirement=" + requirement + ", fine=" + fine + '}';
    }
    
    /*
     * ##################### endregion public methods
     */
}
