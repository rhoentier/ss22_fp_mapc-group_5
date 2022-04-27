# Treffen 3 Protokoll

## Top 1: Infos von den Leads
### Turnierplanung
Alle 4 Wochen, gegen Ende des Praktikums alle 3 Wochen.
Es gibt keine Verpflichtung zur Teilnahme.
Vorläufige Termine (Doodle gibts immer 1 1/2 Wochen vorher)
16.05. - 20.05.
06.06. - 10.06.
04.07. - 08.07.
25.07. - 29.07.
15.08. - 19.08.
05.09. - 09.09.
Gerne können sich die Teams auch untereinander verabreden und spielen.

### Dokumentation
- Eine Dokumentation für alle, mit einer einheitliche Gliederung für jede Gruppe
- Leads legen die Gliederung fest

### Fragen an den Prof.
- Doku engl/deutsch
- Zugriff auf die Submodule einzelner Teams
- Server bereitstellen
- Ausführbarkeit der Agenten auf dem Server (Erfahrung aus dem letzten Jahr)

### Nächstes Treffen:
- 09.05 - 13.05 (Doodle folgt)

### Themen nächstes Treffen:
- Gliederung der Doku
- Spielumgebung (Rollen, Mapgröße, Server)
- Github public/private


## Top 2: Github
 - Wir lassen unser Github für alle offen, damit wir gegenseitig voneinander lernen können
 - Wir versuchen Submodules zu verwenden und versuchen es trotzdem öffentlich zu halten
 - Steffen kümmert sich drum


## Top 3: Regelmäßige Termine
 - Wir arbeiten erstmal zu 4. weiter, bis dahin schreibt Miriam ihn mal an und fragt
 - Wir starten daher erstmal etwas früher: **Dienstag 18:00 Uhr**


## Top 4: Strategien
 - Kartierung
    - Gruppenbildung: alle Gruppen starten in einer Gruppe und diese Gruppen fusionieren mit der Zeit
        - Eine lokale Karte sollte bei jedem Agent weiterhin gespeichert sein
        - Synchronisation der Karten vermutlich am kompliziertesten
    - Zeilicher Verlauf der Erkundung wird mit gefasst
    - Erkundung sollte relativ einfach zu implementieren sein
        - Einfache Heuristik (z.B. immer geradeaus), die sich immer wieder ändert (durch Zufall oder Ereignis)
    - Identifikation muss auch implementiert werden (vermutlich). Ideen:
        - Zeitliche Verschiebung einbauen, um sicher zu sein, dass Identifikation eindeutig ist
        - Gewisser Move um Identifikation zu garantieren
 - Suchalgorithmen
    - Wir beginnen mit A*, da dieser gut Dokumentiert ist und auch gut implementiert werden kann
    - Danach schauen wir uns andere Algorithmen an, z.B. D*
 - Kommunikation der Agenten, Verteilung der Aufgaben und Suchalgorithmen besprechen wir später


## Top 5: Rollen und Maps
 - Wir können festlegen, welche Rollen wir haben wollen und geben das an die Leads weiter
 - Die Leads entscheiden, welche Rollen dann letztendlich zugelassen werden
    - In den ersten Turnieren werden wir erstmal nur eine Rolle festlegen (DEFAULT)
    - Im Laufe des Praktikums werden weitere Rollen freigeschaltet
 - Ebenso wollen wir bei der Map starten
    - wir starten mit einer festen Mapgröße (64x64), ohne Random-Events und einer kleinen Anzahl (5) an Agenten
    - Im Laufe des Praktikums können die Werte erhöht werden


## Top 6: Sonstiges und weitere Fragen
 - Mehrere Agenten an einem Block
    - Die Schritte der Agenten summieren sich
 - Wissen zwei sich treffende Agenten, wer sie sind oder kann es nur durch ausschluss entschieden werden?
    - Alexander schaut nach