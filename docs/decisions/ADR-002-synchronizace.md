# ADR-002: Synchronizace Room ↔ Supabase

Stav: **návrh**, relevantní jen při volbě Supabase (viz ADR-001). Při volbě Firebase synchronizaci zajišťuje Firestore a tento záznam odpadá.
Datum: 2026-11-08

## Kontext

Aplikace má lokální databázi Room. Klienti zapisují tréninky i bez signálu, data se mají sdílet přes
Supabase (Postgres, free plán, oprávnění přes RLS). Je potřeba synchronizace v obou směrech.
Data jsou malého objemu a většinu záznamů mění jen jedna strana (trenér plány, klient výsledky),
takže skutečné konflikty budou vzácné.

## Možnosti

### A) Vlastní synchronizace nad Room
- Room zůstává zdrojem pravdy, přibývá fronta odchozích změn, stahování podle `updated_at`, tombstony
  (`deletedAt`), WorkManager, konflikty „poslední zápis vyhrává“.
- Plus: žádná další služba ani cena, jeden model oprávnění (RLS), beze změny stávající vrstvy Room,
  dobré téma do diplomky.
- Mínus: vše píšeme a ladíme sami (fronta, opakování, idempotence, obnova tokenu). Chyba = ztráta nebo
  duplicita dat.

### B) PowerSync (hostovaná služba nebo vlastní hosting)
- Hotová synchronizace pro SQLite s oficiálním návodem pro Supabase a Kotlin SDK.
- Zjištěno (oficiální ceník, 2026-11-08): free plán 2 GB synchronizovaných dat měsíčně, 500 MB uložených
  dat, 50 souběžných klientů, 2 instance. Placený plán od 49 USD měsíčně. Je i bezplatná verze pro vlastní
  hosting (Open Edition). Free projekty se po týdnu neaktivity deaktivují (ceník je v tomto bodě
  nejednotný, uvádí i dva týdny).
- Podpora Room je podle dokumentace v betě. Dokumentace zároveň píše, že od verze Kotlin SDK 1.6.0 jsou
  Room a SQLDelight podporované, nejasná je tedy zralost.
- Z dokumentace (ověřit při prototypu): oprávnění pro čtení se definují zvlášť (sync rules), zápisy jdou
  přes vlastní konektor do Supabase. To znamená dvě místa s pravidly oprávnění a další služba, která se
  na free plánu pozastavuje.

## Návrh

Začít s **A** (vlastní jednoduchá synchronizace), pokud malý prototyp po datovém modelu v2 ukáže, že je
zvládnutelná. **B** ponechat jako záložní řešení.

Datový model v2 (UUID, `updatedAt`, `deletedAt`, vlastník záznamu) je potřeba pro obě varianty,
takže o synchronizaci lze rozhodnout až po fázi 3 bez ztráty práce.

## Co ověřit před rozhodnutím

- Prototyp A: dvě zařízení, offline zápis, obnovení spojení, smazání na jednom a úprava na druhém.
- Prototyp B: zda funguje se stávajícími entitami Room a jak složité je zavést pravidla oprávnění.
- Skutečné chování pozastavení free plánu u PowerSync i Supabase.

## Zdroje

- [PowerSync ceník](https://www.powersync.com/pricing)
- [PowerSync: Kotlin SDK a Room](https://docs.powersync.com/client-sdks/orms/kotlin/room)
- [PowerSync: průvodce integrací se Supabase](https://docs.powersync.com/integrations/supabase/guide)
- [Supabase ceník](https://supabase.com/pricing)
