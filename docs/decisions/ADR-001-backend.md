# ADR-001: Backend a databáze (Supabase vs Firebase)

Stav: **rozhodnuto: Supabase (free plán)**, 2026-11-08, Erika

## Kontext

Původní rozhodnutí (Supabase, free plán) vycházelo z toho, že se aplikace bude po diplomce reálně používat.
Zadání se upřesnilo: **aplikace se po obhajobě provozovat nebude**. Cílem je pilot s několika klienty (zhruba tři)
a demonstrace při obhajobě. Placený plán se platit nebude. Firebase byl znovu zvážen jako cesta nejmenšího odporu
a nakonec se rozhodlo pro Supabase.

Požadavky: přihlášení, role trenér a klient, více trenérů (oprávnění v databázi, ne v aplikaci), klienti zapisují
tréninky i offline, web se v budoucnu připojí na stejnou databázi.

## Porovnání pro tento rozsah

| | Firebase (plán Spark) | Supabase (free plán) |
|---|---|---|
| Cena | zdarma, bez platební karty | zdarma |
| Omezení provozu | denní kvóty na čtení a zápisy (50 000 čtení, 20 000 zápisů), po vyčerpání se služba zastaví | **žádné denní kvóty na počet dotazů**, limity jsou velikost databáze (500 MB) a přenos dat (5 GB) |
| Nečinnost | o pozastavování neaktivních projektů jsem v dokumentaci plánů nic nenašla (neověřeno) | po 7 dnech nízké aktivity se pozastaví, týden předem přijde e-mail, obnoví se tlačítkem *Resume project*, data zůstanou, do 1 roku |
| Offline | **vestavěný** (cache je na Androidu zapnutá, zápisy se po připojení odešlou, vyhrává poslední zápis) | žádný, vlastní vrstva (ADR-002) |
| Datový model | dokumenty a kolekce, omezené dotazy, agregace v aplikaci | relační SQL (Postgres), agregace v databázi, odpovídá stávajícímu schématu Room |
| Oprávnění | Security Rules | Row Level Security (SQL politiky) |
| Serverová logika zdarma | ne (Cloud Functions vyžadují placený plán s kartou) | ano: SQL funkce v databázi, 500 000 volání Edge Functions měsíčně |
| Zálohy | vyžadují placený plán | jen v placeném plánu |
| Práce | menší, odpadá offline vrstva, ale přepisuje se datová vrstva | větší kvůli offline vrstvě, ale Room a model zůstávají |

## Rozhodnutí

**Supabase (free plán).** Důvody:
- žádné denní kvóty na počet dotazů, takže chyba v kódu nezastaví službu,
- relační model, RLS a SQL odpovídají stávajícímu schématu a hodí se pro analytiku (součet objemu, rekordy),
- Room v aplikaci zůstává jako místní kopie, datová vrstva se nepřepisuje od základu,
- serverovou logiku (např. pravidla přístupu) lze psát v databázi zdarma,
- rozhodování mezi dvěma variantami je samo o sobě materiál pro kapitolu o návrhu práce.

Přijímané nevýhody: vlastní offline vrstva (zúžená, viz ADR-002), pozastavení projektu při nízké aktivitě, žádné zálohy.

## Důsledky

- Dva projekty (`dev` a `prod`) v regionu EU, oba na free plánu. Veřejný klíč a adresa přes konfiguraci buildu, klíč
  `service_role` nikdy do aplikace ani do repozitáře.
- Přihlášení a propojení klienta s profilem viz ADR-003.
- Offline vrstva viz ADR-002 (čtení plánu z Room, odesílání nových výsledků po připojení).
- Zálohy: seed skript pro demo data a pravidelný výpis databáze přes nástroje Supabase mimo repozitář.
- Před obhajobou ověřit, že projekt není pozastavený. Zvážit pravidelné drobné volání API proti pozastavení.
- Po obhajobě se data pilotu smažou.

## Co ověřit

- Skutečné chování pozastavení při lehkém používání (zda e-mailové upozornění přijde včas).
- Přihlášení Google v aplikaci (nastavení OAuth klienta, předání tokenu Supabase).
- Lokální běh Supabase přes CLI pro testy RLS (vyžaduje Docker).
- Příkaz pro výpis databáze a postup obnovy na free plánu.

## Zdroje

- [Supabase: ceník](https://supabase.com/pricing)
- [Supabase: pozastavování projektů ve free plánu](https://supabase.com/docs/guides/platform/free-project-pausing)
- [Supabase: limity přihlašování](https://supabase.com/docs/guides/auth/rate-limits)
- [Firebase: plány Spark a Blaze](https://firebase.google.com/docs/projects/billing/firebase-pricing-plans)
- [Firestore: offline data](https://firebase.google.com/docs/firestore/manage-data/enable-offline)
- [Firestore: zálohy](https://firebase.google.com/docs/firestore/backups)
