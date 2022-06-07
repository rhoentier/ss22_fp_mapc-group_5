# Treffen 7 Protokoll

## Top 1: Aktueller Stand | Was haben wir gemacht

### Sebastian
- Dokumentation
  - Technische Sachen sind nicht gut über die Protokolle nachvollziehbar
  - Organisatorische Sachen sind gut über die Protokolle nachvollziehbar
  - Arbeiten über unser #history in Discord
- Map wird beim Percept lesen fortlaufend implementiert
  - eigene attachedBlocks werden nicht angezeigt
- Flag "IsWalkable" dem MapTile hinzugefügt, um eine Prüfung zu ermöglichen, ob ein Feld begehbar ist



### Alexander
- Surveyed Objekte wurden im AgentStatus eingebunden
- Rolle kann aus gegebener Situation ausgewählt und geändert werden
- A* - Algorithmus implementiert
- Prüfung, ob ein Agent in einer Zone ist


### Miri
- Server eingerichtet und Server Config erstellt
- Erkundungsalgorithmus (Spirale) implementiert
  - Evtl. kann der noch etwas klüger implementiert werden, damit der Agent nicht ständig gegen die gleichen Blöcke läuft


### Steffen
- Prüfung, ob eine Aufgabe erfüllt werden kann
- NextMap erweitert, um zu prüfen, ob relevante Dispenser gefunden wurden


## Top 2: Aufgaben für nächste Woche
- Offene Tickets
  - A*
  - Agenten laufen zu einem bestimmten Ziel
  - Zieleinlauf
- Neue Tickets
  - Vector2D umbauen: double soll in int geändert werden
  - Mit manchen Rollen können mehrere Schritte ausgeführt werden - > Funktionalitäten der Rollen müssen wir berücksichtigen
  - Nicht gesehene NextMapTiles sollten nicht null sein - Alexander schickt einen Vorschlag
  - Klären, wir Nord/Süd implementiert ist


## Top 3: Sonstiges und weitere Fragen
- Namenskonventionen
  - Wir bleiben bei unserer Konvention und versuchen den Code richtig zu schreiben
- Tickets
  - Wenn wir Tickets schreiben und die Sachen zugeteilt sind, dann sollten wir die Person das auch zu Ende arbeiten lassen  
  - Überschriften sollten detaillierter formuliert werden
  - Tickets sollten einen klaren Plan haben, damit alle wissen, wie man von A nach B kommt