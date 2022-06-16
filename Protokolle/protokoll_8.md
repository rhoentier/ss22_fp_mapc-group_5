# Treffen 8 Protokoll, 06.06.2022

## Top 1: Aktueller Stand vor Turnier diskutiert

- Karte wird für A* verwendet und dabei zentriert (A)
- Kann noch Probleme bereiten (wenn die Kartengröße noch nicht bekannt ist) (A/S)
- A überlegt sich was wie man das ändern kann, damit es auch bei (noch) unbekannter Kartengröße funktioniert
- Problem des Vector2D als double nochmal aufgebracht (A). Vector2D wird in int geändert (übernimmt S)

## Top 2: Miri zeigt aktuelle Umsetzung

- Agents finden nahe Ziele, wenn diese innerhalb der Vision liegen und können einzelne Tasks erfüllen
- Agent nutzt aktuell nur die Informationen aus dem Percept (nur Vision und nicht die NextMap)
- Verbesserung für die Zukunft: Wegfindung über die NextMap (bzw. auf dieser Datenbasis)
- Sebastian gibt Miri Bescheid wie am besten vorzugehen ist. Es wird eine Methode getThingType(dispensers, ... endzone) geben

## Top 3: ToDos

- NextMap eine Ebene nach oben (also Entity von NextAgent und nicht NextAgentStatus) (S)
- NextMap später eventuell auf Gruppenebene (noch nicht final festgelegt) (S)
- Methode zum Zugriff auf MapTiles einbauen (S)
- Datenbasis von NextMap noch für morgiges Turnier berücksichtigen, falls es zeitlich klappt (M)