# pam-registeroppslag

Appen tilbyr et API for å søke etter en bedrift i ulike registere.
API-et støtter å søke etter både hovedenheter (juridiske enheter) og underenheter (avdelinger) for en bedrift.
Registerene som tilbys er:
- Bemanningsforetaksregisteret fra Arbeidstilsynet

Registerene lastes ned en gang i døgnet og lagres i en valkey-instans.

## Lokal kjøring

1. Bytt til korrekt JDK-versjon, ved å bruke kommandoen `sdk env`.
2. Bygg appen med gradle: `./gradlew build`.
3. Appen krever en lokal auth-server, det kan startes med `./start-docker-compose.sh`.
4. Kjør `LocalApplication.kt` for å starte applikasjonen.
5. Hent ut et test-access-token med følgende kommando:
```
curl --location 'http://host.docker.internal:8237/tokenx/token?grant_type=client_credentials&client_id=local-token-x-client-id&client_secret=hemmelig' \
    --header 'Content-Type: application/x-www-form-urlencoded' \
    --data-urlencode 'grant_type=client_credentials' \
    --data-urlencode 'client_id=local-token-x-client-id' \
    --data-urlencode 'client_secret=hemmelig' | jq -r '.access_token'
```
