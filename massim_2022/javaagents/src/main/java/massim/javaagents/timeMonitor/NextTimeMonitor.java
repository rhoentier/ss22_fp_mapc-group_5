package massim.javaagents.timeMonitor;

import java.time.Instant;

public class NextTimeMonitor {
    private long simulationStartTime;
    private long roundStartTime;
    private long roundDeadline;
    /**
     * Offeset, damit genügend Zeit zur Kommunikation mit dem Server bleibt
     */
    private long secureOffset = 100;

    public NextTimeMonitor(long simulationStartTime) {
        this.simulationStartTime = simulationStartTime;
    }

    /**
     * Setzt zu beginn die Start und Deadline-Werte. Ermittelt außerdem ein SecureOffset,
     * das sich aus der Zeit der Kommunikation ermittelt
    */
    public void SetRoundTime(long roundStartTime, long roundDeadline){
        long currentTime = Instant.now().toEpochMilli();
        this.roundStartTime = roundStartTime;
        this.roundDeadline = roundDeadline;
        this.secureOffset = roundStartTime - currentTime;
    }

    /**
     * SecureOffset kann bei Kommunikationsproblemen individuell gesetzt werden
     * @param secureOffset
     */
    public void SetSecureOffset(long secureOffset) {
        this.secureOffset = secureOffset;
    }

    /**
     * Gibt zurück, ob noch Zeit bis zur Deadline verfügbar ist
     * @return
     */
    public boolean IsTimeRemaining(){
        long currentTime = Instant.now().toEpochMilli();
        if ((currentTime + secureOffset) < roundDeadline) {
            return true;
        }
        return false;
    }

    /**
     * Gibt die restliche Zeit, abzüglich des SecureOffset an
     * @return restliche Zeit im Millisekunden
     */
    public long GetRemainingTime(){
        if(!IsTimeRemaining()) return 0;
        long currentTime = Instant.now().toEpochMilli();
        return roundDeadline - (currentTime + secureOffset);
    }
}