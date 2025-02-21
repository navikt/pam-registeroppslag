# Opprette ny app

1. Opprett nytt repo på Github, og velg `pam-registeroppslag` som template.
1. Klon det nye repo-et lokalt.
1. Søk etter `pam_registeroppslag` og erstatt dette med navnet på den nye pakken, merk bruken av understrek `_`.
1. Søk etter `pam-registeroppslag` og erstatt dette med navnet på den nye appen, merk bruken av bindestrek `-`.
1. Verifiser at appen kjører lokalt.
1. Fjern koden i pakka `person`, da dette kun var med som et eksempel.
1. Oppdater denne readme-filen med informasjon om den nye appen.
1. Commit og push endringene til Github.

---

# pam-registeroppslag

Mal for å opprette nye applikasjoner som kjører med Javalin.

## Lokal kjøring

1. Bytt til korrekt Java SDK, ved å bruke kommandoen `sdk env`.
2. Bygg appen med gralde: `./gradlew build`.
3. Appen krever en lokal auth-server, det kan startes med `./start-docker-compose.sh`.
4. Kjør `LocalApplication.kt` for å starte applikasjonen.
5. Hent ut et test-access-token, og lagr det i en miljøvariabel:

```
TEST_TOKEN=$(curl --location 'http://host.docker.internal:8237/tokenx/token?grant_type=client_credentials&client_id=local-token-x-client-id&client_secret=hemmelig' \
    --header 'Content-Type: application/x-www-form-urlencoded' \
    --data-urlencode 'grant_type=client_credentials' \
    --data-urlencode 'client_id=local-token-x-client-id' \
    --data-urlencode 'client_secret=hemmelig' | jq -r '.access_token')
```

4. Gjør et curl-kall til appen med token-et fra steg 3:

```
curl 'http://localhost:8080/pam-registeroppslag/personer' \
    --header "Authorization: Bearer $TEST_TOKEN"
```
