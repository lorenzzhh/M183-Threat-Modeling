# STRIDE Analyse

## S – Spoofing (Identitätsfälschung)

1. Ein Angreifer verwendet gestohlene Login-Daten eines Benutzers.
2. Ein gefälschtes JWT- oder Session-Token wird genutzt, um sich als anderer Nutzer auszugeben.
3. Ein Angreifer gibt sich gegenüber der API als legitimer Client aus.

## T – Tampering (Manipulation von Daten)

1. Manipulation eines API-Requests, um fremde Secrets zu verändern.
2. Direkte Änderung von Datenbankeinträgen mit gespeicherten Zugangsdaten.
3. Veränderung von verschlüsselten Tresor-Daten während der Übertragung.

## R – Repudiation (Abstreiten von Aktionen)

1. Ein Benutzer löscht einen Tresor-Eintrag und bestreitet dies später.
2. Ein Nutzer behauptet, nie ein Secret erstellt oder bearbeitet zu haben.
3. Fehlende Audit-Logs verhindern die Nachvollziehbarkeit von Änderungen.

## I – Information Disclosure (Informationspreisgabe)

1. Ein Benutzer kann aufgrund fehlerhafter Autorisierung Secrets anderer Nutzer abrufen.
2. Passwörter oder sensible Daten werden unverschlüsselt übertragen.
3. Fehlermeldungen geben interne Systeminformationen oder Datenbankdetails preis.

## D – Denial of Service (Dienstverweigerung)

1. Massenhafte Login-Anfragen überlasten den Authentifizierungsdienst.
2. Sehr große Requests führen zu Speicher- oder Ressourcenerschöpfung.
3. Automatisierte API-Aufrufe verhindern die Nutzung des Systems durch legitime Benutzer.

## E – Elevation of Privilege (Rechteausweitung)

1. Ein normaler Benutzer erhält Zugriff auf Administratorfunktionen.
2. Fehlende Zugriffskontrollen ermöglichen den Zugriff auf fremde Tresor-Einträge.
3. Manipulation von Benutzer-IDs in Requests führt zu unberechtigten Berechtigungen.