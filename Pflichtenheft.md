# Pflichtenheft
# Projekt-Name: MusicColab

## 1. Einleitung

MusicColab ist ein System, welches Musikbegeisterten es ermöglicht, gemeinsam zu musizieren, auch ohne dabei im gleichen Raum zu sitzen.
Nicht einmal reale Instrumente sind notwendig - es reicht das eigene Android-Smartphone.

Und MusicColab ist simpel: einloggen, Instrument wählen und sobald alle bereit sind, kann es auch sofort losgehen! Abhängig vom gewählten Instrument wird auf unterschiedlichste Art und Weise mithilfe der im Smartphone integrierten Sensoren Musik gemacht. Die gespielten Töne der einzelnen Benutzer werden an einen Server gesendet, der alle eingehenden Tonspuren zu einem Stück zusammenfügt und dieses an allen anderen teilnehmenden Musiker zurücksendet. Somit wird ein gemeinsames Lied erschaffen, das bei allen Benutzern in Echtzeit abgespielt werden kann.
- - -
## 2. Features:
### 2.1. Erfoderliche Features:
#### Client:
1. Client kann sich mithilfe seines Android-Smartphones mit einem Server verbinden.
2. Instrument wählen:
	- Mögliche Sensoren/Instrumente:
		- Touchdisplay (Buttons ersetzen Tasten): bspw. Klavier, Gitarre, Bass etc.
			- Es gibt verschiedene Layouts (je nach Instrument). Drücken auf bestimmten Bereich vom Display definiert die Art des Tones. 
		- Bewegungssensor/Beschleunigungssensor (Bewegung bestimmt Ton): bspw. Schlagzeug, Percussions etc.
			- Smartphone wird wie ein Drummstick bewegt. Der Ton wird vorhher eingestellt.
		- Mikrofon (Durch pusten ins Mikrofon wird Ton erzeugt): bpws. Trompete etc.
			- Client unterscheidet nicht in Tonart des Pustens, sondern reagiert nur auf reines Geräuschsignal aus dem Mikro	
3. Client kann sich bei Server autenthifizieren
	- Login mit Benutzername und Passwort
	- Registrierung: Benutzername + Passwort werden gewählt
4. Client wandelt die Rohdaten aus den Sensoren in eine Datenstruktur und
	- Daten werden ggf. komprimiert um schnelle Verbindung zu ermöglichen
5. Client sendet die Daten an den Server
6. Ein Client kann ein Admin sein: 
	- Als Admin kann man ein Musikstück von vorne starten lassen
7. Client kann sich abmelden ohne, dass das Musizieren beendet wird (wenn mehr als 1 Client spielt)
8. Client empfängt die zusammengelegten Tonspuren und wertet die Daten aus um die Musik abzuspielen


#### Server: 
1. Server wertet empfangende Daten von alles Clients aus und fügt die einzelnen Tonspuren in eine Tonspur zusammen
2. Server sendet die zusammengelegte Tonspur an **alle** Clients
3. Server verwaltet die Verbindung mit Clients
	- Authentifizierung via Login-Daten -> Datenbank mit registrierten Clients
	- prüft ob Authentifizierung vorliegt
- - -
### 2.2 Optionale Features (Hauptprogramm):
1. Server fungiert als Observer -> Man kann über den Server mithören ohne mitzuspielen
2. Server bietet Verwaltung von Lobby
	- Einladen/Hinzufügen von Teilnehmern (Clients)
	- Entfernen von Teilnehmer (Clients)
	- Ändern der Teilnehmer (Clients)
3. Admin kann Lobby erstellen/verwalten
	- kann einzelne Tonspuren löschen/ausblenden 
4. Admin kann Lied wählen
5. Lieder aufnehmen und speichern/exportieren/teilen
6. Server schickt eine Notenspur an die Clients als "Notenblatt"
7. Optionaler Sensor: Lichtsensor:
	- Misst Abstand von Hand zu Kamera, was den Ton bestimmt
8. Verbindung wird verschlüsselt
9. Admin erstellt ServerLobby mit Namen
10. Handling wenn User PW vergessen hat (Registrierung daher mit E-Mail)
11. mehrere Server laufen gleichzeitig auf dem Backend


### 2.2.1 Option: Liedvorgabe via festgelegte Noten
1. Server gibt allen Clients eine bestimmte Ton-Folge und den Takt vor
	- Ton-Folge ist für unterschiedliche Instrumente definiert
	- Server sendet abhängig vom gewählten Instrument Ton-Folge an jeweiligen Client
2. Admin kann Song wählen
3. Auf Client Gui ist zu sehen, welche Note als nächstes kommt. Mit Taktanzeige für das Timing
4. Server stellt min. 1 Song zur Auswahl 
	- Ton-Folgen für mögliche Instrumente vordefiniert

### 2.2.2 Option: Game like GuitarHero
1. Client bekommt Ton-Folge vom Server und prüft ob jewiliger Ton zum richtigen Zeitpunkt getroffen wurde und sendet die Info ob getroffen oder nicht an den Server
2. Server zählt getroffene und nicht getroffene Töne aus -> Highscore wird gespeichert

- - -

## 3. Technische-Features:
1. Robustheit: 
	- Bei Ausfall von Clients wird das Lied nicht unterbrochen sondern läuft für den Rest ohne die Tonspur des ausgefallenden weiter
	- Was ist wenn der Admin Verbindung verliert (=>nächster beitretender User in Server wird Admin : Server muss immer einen Admin haben)
	- Wenn Server Abstürzt startet der neu und die Lobby löst sich auf/Lied endet
	- Wenn Verbindung zum Server verloren wird stürzt Client nicht ab sondern kommt nach einer Meldung zurück in den Hauptbildschirm
2. Benutzerfreundlichkeit: 
	- Einfaches Login (Benutzername, Passwort) -> ID, die vom Server überprüft wird
	- nach dem Login -> Hauptmenü (Lobby erstellen, beitreten)
		- Lobby erstellen: (OPTIONAL: Lied wählen, Teilnehmer (Clients) einladen), Lobby starten
		- Beitreten: (OPTIONAL: Einladung annehmen) oder offener Lobby beitreten
	- nach dem Start -> Instrument wählen und bereitschaft anzeigen. (OPTIONAL: Wenn alle Bereit -> Lied starten)
3. Sicherheit:
	- Nur authentifizierte Clients dürfen sich mit dem Server verbinden.
	- Es gibt eine Datenbank auf dem Server in der jeder Client sich erst registrieren muss.
	- Server checkt bei Login-Versuch ob Client authentifiziert ist-> gibt ggf. Fehlermeldung bzw. Aufforderung zur Authentifizierung wieder
4. Skalierbarkeit:
	- min. bis zu 7 Clients sollten mitspielen können (inkl. Verbinden etc.)
5. Mindestanforderung:
	- für Smartphones mit Android OS


- - -
## 4. Entwicklungsmethode
### 4.1 Organisation in Teams
- **Team Client**: Vincent Happersberger, Marc-Fabio Pascal Niemella, Dimitrios Pigkas, Christoph Jacob Hermann
	- Kümmert sich um die Entwicklung des Client und erstellt Funktionalität auf Seiten des Client zum Verbindungsaufbau mit Server
	- Entwickelt Tests um den Client zu testen
- **Team Server**: Zead Alshukairi, Nils Neurath, Benedikt Simon Kunz, Leon Hecht, Alexandros Laskos
	- Kümmert sich um die Entwicklung des Servers und erstellt Funktionalität auf Seiten des Servers zum Verbindungsaufbau mit Client
	- Entwickelt Tests um den Server zu testen

- **Projektleiter**: Marc-Fabio Pascal Niemella
- **Dokumentationsbeauftragter**: Nils Neurath
- **Qualitätsbeauftragter**: Christoph Jacob Hermann

- - -


## 5. Qualitätssicherung
1. Angemessene Dokumentation in Englisch 
2. Es wird auf sauberen Code geachtet (Clean-Code)
3. UnitTests min. 80% Testabdeckung sowohl auf dem Server als auch auf den Clients
4. Metriken: 
	- Method Lines of Code: max. 30
	- Nestes Block Depth: max. 5
	- Anzahl Parameter pro Funktion: max. 5 (mit Vorbehalt inbezug auf genormte Funktion (Protokolle))
	- Zyklomatische Komplexität nach McCabe: max. 10
5. Meilensteine:
	1. 17.05.21 PoC: Kommunikation zwischen Server und Client, Sensordaten auf Clientseite ausgelesen
	2. 07.06.21 MVP: Client kann "gespielte" Töne eines anderen Clients wiedergeben, Login-System fertig
	3. 21.06.21 alle Pflicht-Features fertiggestellt
	4. Final: 05.07.21 (geplant, mit Vorbehalt) Feinschliff, fertige Implementierung optionaler Features


## 6. Mögliche App-Visualisierung:

<img src="https://git.tu-berlin.de/nilsn/PCPS-2021/-/raw/main/photos/android_gui_1.0.png"
     alt="App GUI"
	 width="1000"
/>
