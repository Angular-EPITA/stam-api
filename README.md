# stam-api

API Spring Boot (Java 21) avec persistance Postgres et démonstration Kafka (ingestion partenaire asynchrone + DLT).

## Démarrage rapide

### Pré-requis

- Docker + Docker Compose
- Java 21

### Lancer l'infrastructure (Postgres + Kafka + UI)

```bash
docker compose up -d
```

URLs :

- API : http://localhost:8080
- Swagger : http://localhost:8080/swagger-ui/index.html
- Kafka UI : http://localhost:8090

### Lancer l'API

```bash
./mvnw spring-boot:run
```

## Kafka

### Topics

- Ingestion partenaire : `partner.catalog.import`
- topic d'erreur : `partner.catalog.import.dlt`

Note : le topic DLT est généralement créé automatiquement au premier message envoyé en DLT (il peut ne pas être visible immédiatement dans Kafka UI).

### Configuration

### Contrat de message

Les messages Kafka sont des **String JSON**.

Enveloppe `PartnerCatalogImportMessage` :

- `schemaVersion` (int)
- `eventId` (uuid)
- `producedAt` (instant ISO-8601)
- `partnerId` (string) : identifiant stable du partenaire (ex: `acme`)
- `mode` (string) : `HTTP_PARTNER` ou `KAFKA_DIRECT`
- `game` : objet jeu (mêmes champs que `GameRequestDTO`)

Exemple (une seule ligne) :

```json
{"schemaVersion":1,"eventId":"00000000-0000-0000-0000-000000000001","producedAt":"2026-01-01T00:00:00Z","partnerId":"acme","mode":"KAFKA_DIRECT","game":{"title":"Direct Kafka Game","description":"from kafka","releaseDate":"2024-03-03","price":9.99,"imageUrl":"https://example.com/a.png","genreId":1}}
```

### Producer / Consumer

- Mode HTTP partenaire : l'API publie un message par jeu sur `partner.catalog.import`
- Consumer : parse JSON → validation Bean Validation → insertion DB

### DLT (Dead Letter Topic)

Règle : si le consumer échoue (JSON invalide, validation KO, erreur métier), le message est publié dans `<topic>.dlt`.

Un message peut exister à la fois dans `partner.catalog.import` (message d'origine) et dans `partner.catalog.import.dlt` (copie en erreur).

## Démonstration (checklist prof)

Objectif : prouver **2 modes d'alimentation**, la consommation asynchrone et un cas limite avec DLT.

1) Démarrer l'infra :

```bash
docker compose up -d
```

2) Démarrer l'API :

```bash
./mvnw spring-boot:run
```

3) Vérifier les topics dans Kafka UI : http://localhost:8090

### Mode 1 — Partenaire via HTTP → Kafka → Consumer → DB

Endpoint : `POST /api/partners/{partnerId}/catalog/import-async`

Exemple :

```bash
curl -X POST 'http://localhost:8080/api/partners/acme/catalog/import-async' \
	-H 'Content-Type: application/json' \
	-d '[{"title":"ACME Game","description":"from partner","releaseDate":"2024-03-03","price":9.99,"imageUrl":"https://example.com/a.png","genreId":1}]'
```

Vérifier l'insertion :

- `GET /api/games/latest`

### Mode 2 — Kafka direct (sans passer par l'API)

Publier :

Coller ensuite un message JSON (une seule ligne), par exemple l'exemple de la section “Contrat de message”.

Vérifier l'insertion :

- `GET /api/games/latest`

### Cas limite — DLT

Consommer la DLT :

Produire un message invalide :

saisir `not-json`.

Option : JSON valide mais incomplet (validation KO, ex: `title` manquant) :

{"schemaVersion":1,"eventId":"00000000-0000-0000-0000-000000000001","producedAt":"2026-01-01T00:00:00Z","partnerId":"acme","mode":"KAFKA_DIRECT","game":{"title":"Mon jeu depuis Kafka","description":"import direct","releaseDate":"2024-03-03","price":9.99,"imageUrl":"https://example.com/a.png","genreId":1}}

## Tests (preuve automatisée)

```bash
# Lancer les tests + vérification couverture (seuil 70%)
./mvnw verify
```

Le rapport de couverture JaCoCo est généré dans `target/site/jacoco/index.html`.

Scénarios couverts :

- HTTP partenaire → Kafka → consumer → insertion DB
- message invalide → DLT
