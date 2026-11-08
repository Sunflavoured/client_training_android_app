# ADR-001: Backend a databáze (Firebase vs Supabase)

Stav: **návrh, k potvrzení Erikou**
Datum: 2026-11-08 (znovu otevřeno po změně zadání)

## Kontext

Původní rozhodnutí (Supabase, free plán) vycházelo z toho, že se aplikace bude po diplomce reálně používat.
Zadání se upřesnilo: **aplikace se po obhajobě provozovat nebude**. Cílem je pilot s několika klienty
a demonstrace při obhajobě. Placený plán se platit nebude a nechceme riziko, že se projekt zastaví.

Požadavky, které zůstávají: přihlášení, role trenér a klient, více trenérů (oprávnění v databázi, ne v aplikaci),
klienti zapisují tréninky i offline, web se v budoucnu připojí na stejnou databázi.

## Co se změnilo ve váze argumentů

- Slábne: přenositelnost, vlastní hosting, předvídatelné ceny při provozu, relační model kvůli dlouhodobé analytice.
- Sílí: **hotová offline synchronizace** (největší technické riziko celé práce) a minimum vlastní infrastruktury.
- Vyrovnané: cena (obě služby jsou zdarma) a riziko pozastavení. Pozastavený projekt Supabase se obnoví jedním
  tlačítkem a data zůstanou, takže to není důvod proti Supabase. Zálohy nejsou ani u jedné z bezplatných variant.

## Porovnání pro tento rozsah

| | Firebase (plán Spark) | Supabase (free plán) |
|---|---|---|
| Cena | zdarma, bez platební karty | zdarma |
| Riziko zastavení | po vyčerpání denní kvóty se služba zastaví do dalšího období; o pozastavování neaktivních projektů jsem v oficiální dokumentaci plánů nic nenašla (neověřeno) | po 7 dnech nízké aktivity se projekt pozastaví, ale **obnoví se jedním tlačítkem (Resume project), data zůstanou** a jde to do 1 roku; týden předem přijde e-mail (ověřeno v oficiální dokumentaci) |
| Offline | **vestavěný**: na Androidu je offline cache zapnutá, zápisy se po připojení samy odešlou, při souběžných změnách vyhrává poslední zápis | žádný, vlastní synchronizační vrstva nebo PowerSync (viz ADR-002) |
| Datový model | dokumenty a kolekce, omezené dotazy, agregace se počítají v aplikaci | relační SQL, agregace v databázi |
| Oprávnění | Security Rules (testovatelné lokálně přes Emulator Suite, ověřit) | Row Level Security |
| Zálohy | vyžadují placený plán Blaze | jen v placeném plánu |
| Serverový kód | Cloud Functions vyžadují Blaze, nepoužijeme | neověřeno |
| Práce navíc | menší, odpadá vlastní synchronizace | větší, nutná synchronizace (ADR-002) |

Poznámka k analytice: objem dat pilotu je malý (jednotky klientů), grafy a metriky se spočítají v aplikaci.

## Návrh

**Firebase (plán Spark)** kvůli hotové offline synchronizaci. Po ověření obnovy pozastaveného projektu je to
**těsná volba**: Supabase free je pro pilot a obhajobu také použitelný. Rozhoduje, co je důležitější:
nejmenší riziko a nejméně práce (Firebase), nebo relační model, SQL analytika a návaznost na stávající Room
kód (Supabase s vlastní synchronizací, ADR-002).

## Důsledky volby Firebase

- v2 používá Firestore jako hlavní úložiště, repozitáře se píší nad ním (posluchače změn místo Room DAO).
  v1 zůstává na Room. Většina obrazovek se dá převzít, mění se datová vrstva.
- ADR-002 (synchronizace) odpadá jako samostatné rozhodnutí. Zbývá ověřit chování v offline scénářích.
- Model dat se navrhuje podle čtení (co se zobrazuje najednou), ne podle normalizace. Je potřeba počítat s denními
  kvótami (50 000 čtení a 20 000 zápisů denně) a nezbytně nezatěžovat aplikaci posluchači na velké kolekce.
- Role (trenér, klient) a pozvánky se bez Cloud Functions řeší dokumenty a pravidly přístupu. Je to proveditelné,
  ale pravidla se musí pečlivě testovat.
- Zálohy nejsou. Místo nich seed skript pro demo data a krátký export klíčových dat před obhajobou.
- Region EU při zakládání projektu (nejde později změnit). Po obhajobě se data pilotu smažou.
- `google-services.json` je vázaný na `applicationId`: nejdřív se rozhodne ID v2 (plán 1.9), pak se aplikace
  zaregistruje ve Firebase.

## Co ověřit před potvrzením

- Chování offline cache při dvou zařízeních a souběžné úpravě (prototyp na jednom dokumentu).
- Zda se Spark projekty při dlouhé neaktivitě nějak omezují (oficiální dokumentace, podmínky).
- Rozložení dotazů v modelu v2 vůči denním kvótám.

## Zdroje

- [Firebase: plány Spark a Blaze](https://firebase.google.com/docs/projects/billing/firebase-pricing-plans)
- [Firestore: offline data](https://firebase.google.com/docs/firestore/manage-data/enable-offline)
- [Firestore: zálohy](https://firebase.google.com/docs/firestore/backups)
- [Supabase: ceník](https://supabase.com/pricing)
- [Supabase: pozastavování projektů ve free plánu](https://supabase.com/docs/guides/platform/free-project-pausing)
