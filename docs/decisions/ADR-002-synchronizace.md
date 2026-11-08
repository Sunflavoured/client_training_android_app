# ADR-002: Synchronizace Room ↔ Supabase

Stav: **rozhodnuto** (2026-11-08): varianta A v zúžené podobě, PowerSync zůstává záložní variantou (viz ADR-001, Supabase).
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

## Rozhodnutí

Varianta **A** v zúžené podobě, přiměřené rozsahu diplomky (tři pilotní klienti):
- **čtení offline:** plán, cviky a data klienta se stahují do Room, aplikace s nimi pracuje bez připojení,
- **zápis offline:** odcvičený trénink se uloží lokálně jako nový záznam s UUID a po připojení se odešle (fronta
  odchozích změn, WorkManager, idempotentní zápis). Nové záznamy nemají s čím kolidovat,
- **bez obecné obousměrné synchronizace:** plán upravuje jen trenér online, výsledky zapisuje jen klient.
  Úpravy stejného záznamu z více míst by byly rozšíření.

Pokud by ani tato zúžená vrstva nevycházela časově, odloží se obousměrná část do návrhů na rozšíření a výsledky
se budou odesílat jen při dostupném spojení. **B** (PowerSync) zůstává záložní možností.

Datový model v2 (UUID, `updatedAt`, `deletedAt`, vlastník záznamu) je pro obě varianty stejný.

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
