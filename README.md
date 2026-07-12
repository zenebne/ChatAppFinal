# ChatApp
## Projektbeschreibung
ChatApp ist eine webbasierte Echtzeit-Chat-Anwendung.
Benutzer können sich registrieren und anmelden, Direktnachrichten senden, Gruppen erstellen und in Echtzeit miteinander kommunizieren. Außerdem können Benutzer ihren Status und ihre persönliche Nachricht bearbeiten, ein Profilbild hochladen und frühere Chatverläufe anzeigen.

## Funktionen
- Registrierung und Login
- Einzelchat
- Gruppenchat
- Erstellung und Löschung von Gruppen
- Echtzeit-Nachrichten über WebSocket
- Speicherung und Anzeige des Chatverlaufs
- Anzeige von Benutzern und Gruppen
- Aktualisierung des Status
- Speicherung einer persönlichen Nachricht
- Upload eines Profilbildes
- Logout

## Team
**Gruppe 9**
- Nilüfer Civelek
- Zeynep Ünver

## Voraussetzungen
Für die Installation und Ausführung werden benötigt:
- Java 17
- ein moderner Webbrowser
Die installierte Java-Version kann mit folgendem Befehl überprüft werden:
```powershell
java -version
```
Durch den enthaltenen Maven Wrapper ist keine separate Maven-Installation erforderlich.

## Installationsanleitung
1. Die ZIP-Datei entpacken.
2. Ein Terminal im Projektordner öffnen. Der richtige Projektordner ist der Ordner, in dem sich die Datei `pom.xml` befindet.
3. Das Projekt bauen und die benötigten Abhängigkeiten installieren:
```powershell
.\mvnw.cmd clean install
```
4. Die Anwendung starten:
```powershell
.\mvnw.cmd spring-boot:run
```
5. Nach dem erfolgreichen Start die Anwendung im Browser öffnen:
```text
http://localhost:8080
```
6. Die Anwendung kann im Terminal mit folgender Tastenkombination beendet werden:
```text
Ctrl + C
```
Unter macOS oder Linux können stattdessen folgende Befehle verwendet werden:
```bash
./mvnw clean install
./mvnw spring-boot:run
```
## Verwendetes Software Pattern
Das Projekt verwendet das Repository Pattern. Die Repository-Klassen trennen den Datenzugriff von der übrigen Anwendungslogik.
Verwendet werden unter anderem:
- `UserRepository`
- `MessageRepository`
- `ChatGroupRepository`
## Code-Konventionen
- Java-Klassen werden in PascalCase benannt.
- Methoden und Variablen werden in camelCase benannt.
- Konstanten werden, sofern vorhanden, in UPPER_SNAKE_CASE benannt.
- Jede Quell- und Testdatei enthält eine Angabe zum Verantwortungsbereich und Autor.
- Controller, Repository, Modell und Konfiguration werden nach ihren jeweiligen Aufgaben getrennt organisiert.
- Testmethoden werden so benannt, dass die getestete Situation und das erwartete Verhalten erkennbar sind.

## Tests
Die automatisierten Tests können unter Windows mit folgendem Befehl ausgeführt werden:
```powershell
.\mvnw.cmd clean test
```
## Unter macOS oder Linux:

```bash
chmod +x mvnw
./mvnw clean install
./mvnw spring-boot:run
```

Beim letzten Testlauf wurden alle Tests erfolgreich ausgeführt:
- 19 Tests
- 0 fehlgeschlagene Tests
- 0 Fehler
- 0 übersprungene Tests

## Datenbank
Für die lokale Ausführung verwendet die Anwendung standardmäßig eine H2-In-Memory-Datenbank. Daher ist keine zusätzliche Datenbankinstallation erforderlich.
Optional kann die Anwendung auch mit einer PostgreSQL-Datenbank verwendet werden.

## Nutzung von KI
Die Nutzung von KI-gestützten Werkzeugen ist im Dokument [KI-Verzeichnis.md](KI-Verzeichnis.md) beschrieben.