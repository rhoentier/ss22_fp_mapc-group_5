package massim.javaagents.agents;

public class TimeMonitor {
    private long simulationStartTime;
    private long roundStartTime;
    private long roundDeadline;

    // Ein Offset, damit am Ende noch Zeit für den Versand über bleibt
    private long secureOffset = 100;

    public TimeMonitor(long simulationStartTime) {
        this.simulationStartTime = simulationStartTime;
    }

    public void SetRoundTime(long roundStartTime, long roundDeadline){
        this.roundStartTime = roundStartTime;
        this.roundDeadline = roundDeadline;
    }

    public void SetSecureOffset(long secureOffset) {
        this.secureOffset = secureOffset;
    }

    // Gibt zurück, ob noch Zeit für eine Berechnung bleibt
    public boolean IsTimeRemaining(){
        // TODO: Prüfen, ob noch Zeit ist um Berechnung auszufüren
        return True;
    }

    // Gibt die restliche Zeit bis zum Rundenende an
    public long GetRemainingTime(){
        // TODO: Restliche Zeit berechnen
        return 0;
    }
}