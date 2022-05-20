# Treffen 6 Protokoll

## Top 1: Aktueller Stand | Was haben wir gemacht

### Sebastian
- Sebastian hat die Map erstellt
  - inklusive ein paar Funktionen, u.a.: MapTiles ausgeben, Karten mergen, Percepts einfügen
  - Vector2D hat einige Methode implementiert, damit alle Standardoperationen bereits vorliegen
    - Point in Vector2D erst ändern, wenn es Probleme macht
    - Wir arbeiten ab jetzt mit Vector2D und nicht mit der Klasse Point


### Alexander
- BugFixes und PerceptReader fertig gemacht (überagabe an AgentStatus und SimStatus)


### Miri
- Config erstellt, damit wir die Definitionen des Agents auslagern können
  - Bisher in NextScheduler implementiert, dies soll alleredings geändert werden (siehe unten)
- Einfachen Erkundungsalgorithmus implementiert


### Steffen
- Klasse erstellt, die Aktionen erstellt
- Prüfung innerhalb der Map, ob eine Rotation möglich ist


## Top 2: Aufgaben für nächste Woche
- Offene Tickets
  - Klasse, damit zusätzliche config eingelesen werden kann
    - Wird im NextAgent aufgerufen, der zu Begin die json-config einliest
  - Map erweitern, damit wir wissen, welche Dispenser, welche RoleZones, etc. es gibt
  - Turnier vorbereitungen treffen
- Entscheidungsprozess am Anfang
  - Agenten erkunden solange die Karte, bis eine der Aufgaben erfüllt werden kann
  - Agent errechnet den kürzesten Weg zum Ziel (Dispensern/Zielzone) und teilt diesen an alle mit (Ziel und Wegekosten)
  - Agenten mit minimalen Kosten laufen zum Ziel
  - Treffen sich an Ziel - bei Ankunft wird dem Zielzonen-Agenten mitgeteilt, wo die Übergabe stattfindet
  - Zielzonen-Agent nimmt die Blöcke an der richtigen Stelle auf und gibt sie an der Zielzone ab
  - Alle Agenten ohne Aufgabe erkunden weiter die Karte
    - Wenn die Karte bereits vollständig ist, dann gehen die Agenten nahe zu einem Dispenser


## Top 3: Sonstiges und weitere Fragen
- Dokumentation des Codes und unserer Historie
  - Sebastian schaut sich an, ob sich eine Historie aus den Protokollen an und versucht es nochmal nachzuvollziehen
    - Ansonsten können wir eine eigene Historie schreiben (beim nächsten Mal besprechen)
  - mit dem Hashtag #historie schreiben wir auf Discord Nachrichten, damit wir sehen, was passiert ist
    - starten das erstmal als Versuch
- Git
  - Tickets bitte so erstellen, damit möglichst alle Aufgaben klar erkennbar sind. Wenn man für sich selber Tickets erstellt, dann sollte man sich diese direkt selbst zuordnen
  - develop sollte immer vor dem PullRequest gemerged werden
- Surveyed Objekte
  - 2 neue Felder bei NextAgentStatus machen und dafür dann zwei neue Klassen
- Turniere
  - nächste Woche ist ein Turnier
  - das nächste Treffen fällt daher aus