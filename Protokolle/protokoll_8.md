# Treffen 8 Protokoll

## Top 1: Aktueller Stand | Was haben wir gemacht

### Sebastian
- Sebastian hat Vector2D von double auf int umgestellt
- Map war im AgentStatus und ist nun direkt im NextAgent
    - Kann bald in die Gruppe ausgelagert werden


### Alexander
- Änderung von A* wegen Anpassung in NextMap und Vector2D
- A* läuft auf Karte, die als Kreis angelegt ist
- Refactoring von step()
- Dokumentation einiger Methoden, zum Beispiel in NextAgent


### Miri
- Wegfinden mit Blöcke kaputt hauen
- Auswahl von einem beliebigen Task mit einem Block
- Weg zum Dispenser/Goalzone (Rolezone is eingebaut aber noch nicht getestet)
- Abgabe der Tasks


### Steffen
- Vorstellung vorbereitet: Task- und Rollenauswahl


## Top 2: Aufgaben für nächste Woche
- Alexander
    - A* optimieren, damit Rolleneigenschaften (clear) mit eingebaut werden sollen (evtl. nächste Woche oder so)
    - Dokumentation weiter machen
- Miri
    - Bugfixing
    - Clevere Auswahl der Tasks
    - Tasks mit mehreren Blöcken
    - Cleveres rotieren
    - Den Block beim Clear so drehen, dass Agent durch die Lücke passe
    - Testen mit mehreren Agenten
    - Pathfinding einbauen 
    - Weg zur Rolezone
- Sebastian
    - Gruppe erstellen
    - NextMap in eine Gruppe einarbeiten
- Steffen und Alexander
    - Möglichkeit Pläne zu erstellen und abzuspeichern
    - Rollenauswahl
- Steffen
    - Prüfung, wann ein Rollentausch sinnvoll ist
 

## Top 3: Sonstiges und weitere Fragen
- Problem mit Gruppen:
    - Statische Liste mit den Gruppen, damit alle Agenten Zugriff drauf haben
- Problem mit Rotation:
    - Rotation wirft bei Miri immer eine Exception
- MR von Steffen
