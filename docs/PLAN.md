# Plán a checklist – Training App (diplomová práce)

Poslední aktualizace: 2026-11-08

**Jak s tímto souborem pracovat**
- Položku odškrtni až ve chvíli, kdy je hotová a ověřená.
- Nové nápady piš do sekce *Backlog nápadů* dole, ne přímo do fází. Do fází se dostanou až po zhodnocení.
- Každé významné rozhodnutí zapiš jako krátký záznam (kontext, možnosti, rozhodnutí, důsledky). Je to podklad pro text diplomky.
- Před další prací se na tento soubor podívej, ať se dodrží pořadí.
- Repozitář je **veřejný**: do tohoto souboru ani nikam jinam nepatří klíče, hesla, data klientů ani exporty databáze.

---

## 1. Dohodnuté

- Zůstáváme u **nativního Android Kotlinu**. Cílem je **sdílená cloudová databáze** s přihlášením.
- **Cíl diplomky: milník M7 bez webu, tedy pilot s několika klienty (zhruba tři) a demonstrace při obhajobě** (upřesněno 2026-11-08). Aplikace se po obhajobě reálně provozovat nebude. Web je návrh rozšíření a nebude výstupem diplomky. Architektura s ním ale počítá: web se později napojí na stejnou databázi, proto musí být oprávnění vyřešená v databázi (RLS), ne v aplikaci.
- **Backend: Supabase, free plán** (rozhodnuto 2026-11-08, viz `docs/decisions/ADR-001-backend.md`). Firebase byl zvážen a porovnán, rozhodnutí a srovnání jsou podklad pro kapitolu o návrhu. Placený plán se nebude platit. Dva zdarma dostupné projekty slouží jako `dev` a `prod`.
- Má vzniknout **platforma pro více trenérů**. Navrhujeme `trainerId` v datech a oprávnění v databázi hned od začátku, uživatelské rozhraní zatím pro jednoho trenéra.
- **Přihlášení (rozhodnuto 2026-11-08, viz ADR-003):** klienti se přihlašují, aby viděli jen svá data. Hlavní způsob je **Google Sign-In**, případně e-mail a heslo pro testovací a demo účty. Bez automatiky: účty trenérů a role se zakládají ručně, klient se propojí s profilem podle **ověřeného e-mailu** zadaného trenérem. **Automatické pozvánky a samoobslužná registrace trenérů nejsou součástí diplomky**, patří do návrhů na rozšíření (fáze W).
- **Klienti budou zapisovat tréninky offline** (špatný signál v posilovně). Řeší to zúžená offline vrstva (viz ADR-002): čtení plánu z místní kopie a odesílání nových výsledků po připojení.
- Termín: několik měsíců (přesné datum doplnit).
- Pilot: Erika a několik jejích klientů.
- Workflow: Claude Code + Android Studio (USB, kontrola diffu), GitHub issues přes `gh`, změny na větvích, nic se nepushuje bez souhlasu, commity jsou podepsané.
- Text diplomky je v **samostatném repozitáři** jako Markdown, později převod do LaTeXu. Zdroje ověřuje Erika. Pravidla školy k AI řeší Erika sama v textu práce.
- Dnešní verze: DEV (debug, přípona `.dev`, nahrává se přes USB) a PROD (release, aktualizace podepsaná klíčem). To zůstává.
- **Strategie v1 a v2 (dohodnuto 2026-11-08):** v1 (`com.example.client_training_app`) se stabilizuje, vydá jako verze 0.2.0 a pak se zmrazí (jen kritické opravy). Je to Erikin současný pracovní nástroj, repozitář zůstává **veřejný** (kontrola aktualizací a stahování APK z Releases). v2 je diplomová platforma: **nová aplikace** v **novém privátním repozitáři** s novým `applicationId`, názvem a keystorem. v2 začíná bez dat z v1 (demo data ze seed skriptu), přenos dat z v1 je volitelný (viz 5.7).
- Větší přestavba rozložení aplikace (spodní lišta, boční panel s přepínáním klientů) patří do v2, viz sekce *Návrh rozložení aplikace v2*. U v1 se dělají jen drobná vizuální vylepšení.

## 2. Otevřené otázky

- Přesné datum odevzdání. Podle něj se určí, kdy nejpozději musí začít pilot (doporučení: s rezervou několika týdnů na sběr dat a vyhodnocení).
- Které opravy v1 ještě zařadit do vydání 0.2.0 (viz 2.16)?
- Klientský režim: zapisuje trenér i za klienta při osobním tréninku? (viz 1.2)
- Může mít klient více trenérů, nebo vždy jednoho?
- Konkrétní `applicationId` a název v2 (viz 1.9). Přihlášení Google je vázané na `applicationId` a otisk podpisového klíče, takže se nastavuje až po jejich určení.
- Ověřit na malém prototypu Supabase: přihlášení Google v Androidu, RLS podle e-mailu, offline zápis a odeslání po připojení, region EU, příkaz pro výpis databáze (viz ADR-001).

## 3. Milníky

| Milník | Obsah | Fáze |
|---|---|---|
| M1 | Stabilní základ, nástroje fungují, repo uklizené, v1 vydaná jako 0.2.0 | 0, 2 |
| M2 | Analýza a rozhodnutí hotová | 1 |
| M3 | Nový datový model | 3 |
| M4 | Cloud, přihlášení, offline vrstva | 4, 5 |
| M5 | Plánování programů a fází **v aplikaci**, klientský režim | 6 |
| M6 | Analytika, média, progresivní přetížení | 7, 8 |
| M7 | Pilot s několika klienty a demonstrace při obhajobě (cíl diplomky) | 9 |
| – | Web a další rozšíření (mimo rozsah) | W |

Pořadí priorit při tlaku na termín: M1 až M5 a M7 jsou jádro. Z M6 vzít nejdřív základní grafy, progresivní přetížení a média jsou rezerva.

---

## Fáze 0 – Prostředí a repozitář (blokery)

- [ ] 0.1 Spouštět Android Studio z `C:\Program Files\Android\Android Studio1\bin\studio64.exe` (viz poznámky na konci), opravit zástupce.
- [x] 0.2 Přihlásit `gh` (hotovo, účet Sunflavoured).
- [ ] 0.3 **Zálohovat** celou složku `client_training_app` mimo Git. Data z telefonu s PROD verzí dnes vyexportovat nejde, jde to až po instalaci verze 0.2.0 (viz 2.0 a 2.16).
- [ ] 0.4 Keystore PROD je v `C:\Users\erika\OneDrive\Training_App_klicenka\moje_klicenka` (složka byla přejmenována, Android Studio ukazuje ještě na starou cestu `...\Training_App\...`, při příštím podepisování ji přenastavit). Ověřit, že odpovídá PROD APK (otisk SHA-256 podpisu `7accf4cb…42d5`, příkaz `keytool -list -v -keystore <soubor>`, heslo zadáš jen ty). Uložit soubor jako přílohu do 1Passwordu a heslo k němu. Bez klíče nelze vydat aktualizaci přes existující instalaci.
- [ ] 0.5 Ověřit, že DEV build (`.dev`) se instaluje vedle PROD a data nesdílí (dosud fungovalo).
- [ ] 0.6 Pushnout `chore/repo-cleanup` a `fix/stabilizace`, otevřít PR do `master`, potom PR pro `04-dev-features`. Po sloučení vytvořit release 0.2.0.
- [x] 0.7 Commitnout tento soubor a odkaz na něj v `CLAUDE.md`.
- [ ] 0.8 Založit repozitář diplomky (Markdown): kapitoly, `sources.md` (zdroj, odkaz, stav ověření a datum), složka `decisions/`, složka `figures/`.
- [ ] 0.9 Projít stávající issues (#1 až #13). Většina odpovídá už hotovým funkcím (profily, cviky, kalendář, jednotky, historie, sdílení), zavřít ty hotové a zbytek přeřadit. Nové issues podle tohoto plánu s milníky M1 až M7 a štítky `bug`, `feature`, `backend`, `data-model`, `sync`, `thesis`.
- [ ] 0.10 Smazat staré sloučené větve na GitHubu (01, 02, 03). Volitelné, až po sloučení.
- [ ] 0.11 Vytvořit **privátní** repozitář pro v2 (až bude rozhodnuté `applicationId` a název, viz 1.9). V1 repozitář zůstává veřejný.

## Fáze 1 – Analýza a rozhodnutí (před psaním kódu v2)

- [ ] 1.1 Jeden seznam požadavků (funkční a nefunkční), roztříděný podle MoSCoW. Sem patří všechny dosavadní nápady. Web a iOS označit jako mimo rozsah.
- [ ] 1.2 Role a případy užití. **Trenérský režim** (správa klientů, plánování, přehledy) a **klientský režim** (vlastní plán, zápis tréninku offline, vlastní historie). Rozhodnout, zda trenér zapisuje i za klienta a kdo je autorem záznamu.
- [ ] 1.3 Rešerše podobných řešení (co umí, co chybí). Zdroje ověřit a zapsat do `sources.md`.
- [x] 1.4 Backend: Supabase (free plán), rozhodnuto 2026-11-08, záznam v `docs/decisions/ADR-001-backend.md` včetně srovnání s Firebase.
- [x] 1.5 Offline: zúžená vlastní vrstva nad Room (viz `docs/decisions/ADR-002-synchronizace.md`): plán se čte z místní kopie, odcvičené tréninky se ukládají lokálně a po připojení odesílají. Obousměrná synchronizace je rozšíření, PowerSync záložní varianta.
- [ ] 1.6 Datový model v2: ER diagram a pravidla:
  - UUID ve všech záznamech (dnes jsou v polovině tabulek autoincrementy),
  - `createdAt`, `updatedAt`, `deletedAt`, `trainerId`/`ownerId`,
  - číselné hodnoty místo textu (váha, opakování od–do, RIR, čas, vzdálenost),
  - plán vs skutečnost, šablona vs naplánovaná instance (snapshot),
  - program → fáze → jednotka,
  - datum plánu jako kalendářní datum, ne epoch půlnoc v časové zóně zařízení,
  - svalové skupiny jako klíče, ne české popisky,
  - jednotky (kg, cm),
  - vztah trenér–klient a účet vs profil klienta (klient může existovat bez účtu).
- [ ] 1.7 Bezpečnost a GDPR: matice oprávnění (kdo co vidí), souhlas klienta, doba uchování, právo na výmaz a export, region EU, zásady ochrany osobních údajů. Jde o zdravotní údaje. Pilot má jen několik klientů a Erika je správcem údajů, po obhajobě se data smažou. Při více trenérech by se měnila role provozovatele, to je téma pro návrh a diplomku, právní dokumenty pro cizí trenéry neřešit.
- [ ] 1.8 Uživatelské toky a drátěné modely aplikace: přihlášení, plánování fází, zápis tréninku, grafy.
- [ ] 1.9 Rozhodnout `applicationId` a název aplikace. **Návrh:** v2 jako nová aplikace (nové ID, nový název, nový keystore uložený od začátku v 1Passwordu). V1 (`com.example.client_training_app`) běží dál a po vydání 0.2.0 dostává jen kritické opravy. v2 začíná bez dat z v1 (viz 5.7).
- [ ] 1.10 Kritéria úspěchu a způsob testování (offline scénáře, uživatelský test, případně dotazník použitelnosti). Podklad pro kapitolu o testování.
- [ ] 1.11 Multi-tenance: model trenér–klient (účty trenérů se zakládají ručně), globální cviky vs vlastní cviky trenéra, sdílení programů v rámci jednoho trenéra (mezi trenéry až později).

## Fáze 2 – Stabilizace stávající aplikace (v1)

Opravy v1 (stejné `applicationId` a klíč), které se vydají jako aktualizace PROD. Neřešit zde nic, co patří do v2. Pracovat na samostatné větvi, testovat přes DEV build (má vlastní databázi, data PROD se nedotkne).

- [x] 2.0 (hotovo na větvi `fix/stabilizace`: v menu úvodní obrazovky „Zálohovat data“ a „Obnovit ze zálohy“ pracují se souborem `.db`; ověřeno testy `BackupManagerTest` a ručně na emulátoru včetně restartu aplikace po obnově) **Záloha dat v1 (pojistka, netýká se v2).** Dnes se ze stávající PROD databáze nedá nic vytáhnout (aplikace není ladicí, `adb backup` na Android 12 a novějším nefunguje, export umí jen jednu tréninkovou jednotku). Po instalaci aktualizace udělat zálohu jako první věc.
- [ ] 2.1 Ukládat `rest` při dokončení tréninku (dnes se do `WorkoutSetResultEntity` nepřenáší).
- [ ] 2.2 Kalendář: výběr tréninku sbírá Flow bez ukončení, bottom sheet se může otevírat opakovaně (ověřit a opravit).
- [ ] 2.3 Kalendář: označit splněné tréninky (kód je zakomentovaný).
- [ ] 2.4 Měření těla: zprovoznit vstup z detailu klienta (tlačítko je zatím placeholder, obrazovka existuje).
- [ ] 2.5 Hodnocení a poznámka k tréninku (`rating` a `note` se nikdy nevyplní).
- [ ] 2.6 Výchozí cviky se přepisují z JSON při každém otevření databáze. Verzovat seed, ať se uživatelské úpravy nepřepíšou.
- [ ] 2.7 Základní testy (DAO, migrace Room) jako záchranná síť. Částečně hotovo: `TemplateEditIntegrityTest`, `MigrationTest`, `BackupManagerTest`, `TemplateEditorReorderTest`.
- [ ] 2.8 Zvážit zapnutí minifikace u release (ověřit Gson a Room).
- [x] 2.9 (hotovo na větvi `fix/stabilizace`, pokryto testy `TemplateEditIntegrityTest`, ověřeno na emulátoru) **Úprava šablony nesmí měnit nic jiného.** `insertOrUpdateUnit` používal REPLACE, který řádek smaže a znovu vloží. Na kopii databáze ověřeno: odcvičeným tréninkům se přitom smazal odkaz na šablonu (`SET NULL`) a smazaly se její cviky (`CASCADE`). Nahrazeno UPSERT. Důsledek, který Erika popsala: trénink v kalendáři zůstal, ale po otevření byl prázdný. Opraveno i čtení ID šablony z kalendáře, takže se napraví i již poškozená data.
- [x] 2.10 (hotovo na větvi `fix/stabilizace`: smazaná šablona s historií se archivuje, naplánované tréninky se z kalendáře odeberou, odcvičené zůstanou v kalendáři i v historii; migrace databáze 3→4; ověřeno testy, dialog zatím vizuálně neověřen) Smazání šablony, která je naplánovaná v kalendáři, narazilo na cizí klíč (na kopii databáze ověřeno: `FOREIGN KEY constraint failed`, v aplikaci pravděpodobně pád).
- [x] 2.11 (hotovo na větvi `fix/stabilizace`, ověřeno ručně na emulátoru: tlačítko „Nový cvik“ ve výběru, po uložení se cvik rovnou vybere) Přidání **nového cviku** přímo z výběru cviků (během tréninku i při tvorbě jednotky).
- [x] 2.12 (hotovo na větvi `fix/stabilizace`: šipky nahoru a dolů u každého cviku, ověřeno testy `TemplateEditorReorderTest` a ručně na emulátoru; přetahování prstem případně později) Změna **pořadí cviků** v editoru tréninkové jednotky.
- [ ] 2.13 Drobná vizuální vylepšení v1: seznam konkrétních výhrad od Eriky (obrazovka, co vadí), pak po jedné obrazovce. Přestavba rozložení je záležitost v2.
- [ ] 2.14 Z kalendáře jde naplánovaný trénink jen přidat, **odebrat ho nejde** (smazání je v kódu, ale chybí v obrazovce). Přidat odebrání, nejlépe s potvrzením.
- [x] 2.15 (hotovo na větvi `fix/stabilizace`, ověřeno testy) **Odcvičený trénink je záznam, nesleduje šablonu.** Zobrazuje jen uložené série v pořadí, v jakém byly zapsány. Naplánovaný (neodcvičený) trénink šablonu dál sleduje. Známá omezení do v2 (fáze 3, snapshot plánu): cvik bez jediné zapsané série se v odcvičeném tréninku nezobrazí a název šablony v kalendáři je pořád aktuální název šablony.
- [ ] 2.16 **Vydání v1 jako 0.2.0.** Postup:
  - zvýšit `versionCode` a `versionName` (dnes 1 a 0.1.0), ať aktualizace jde nainstalovat přes PROD a sedí s tagem na GitHubu,
  - vybrat, které z 2.1 až 2.6 a 2.14 jde do vydání (zbytek do v2),
  - otestovat na DEV buildu (kontrolní seznam: úprava šablony, odcvičený a naplánovaný trénink, smazání šablony, záloha a obnova, nový cvik, pořadí cviků),
  - sestavit podepsaný APK a ověřit otisk podpisu (musí sedět s `7accf4cb…42d5`), jinak ho Android nepřijme jako aktualizaci,
  - nainstalovat přes PROD, hned udělat zálohu a zkontrolovat počty dat,
  - vytvořit release a tag `v0.2.0` na GitHubu,
  - **od té chvíle je v1 zmrazená** (jen kritické opravy), práce pokračuje ve v2. Pozor: na nižší verzi se po instalaci vrátit nejde bez odinstalace, tedy se ztrátou dat.

## Fáze 3 – Datový model v2

Relační model v Postgresu (SQL, Supabase) s místní kopií v Room.

- [ ] 3.1 Schéma v2 (UUID, `updatedAt`, `deletedAt`, `trainerId`). v2 je nová aplikace a startuje bez dat z v1, takže nepotřebuje migraci databáze z v1.
- [ ] 3.2 Typované hodnoty (váha, opakování, RIR, čas, vzdálenost) místo textu.
- [ ] 3.3 Program → fáze → jednotka, šablona vs naplánovaná instance (snapshot plánu).
- [ ] 3.4 Datum plánu jako kalendářní datum.
- [ ] 3.5 Svalové skupiny jako klíče, překlad v UI.
- [ ] 3.6 Verzovaný formát exportu a importu dat se zpětnou kompatibilitou.
- [ ] 3.7 **Volitelné:** test importu dat z v1 na **anonymizované** kopii (viz 5.7).
- [ ] 3.8 Aktualizovat `CLAUDE.md` a dokumentaci modelu.

## Fáze 4 – Backend (Supabase, free plán)

- [ ] 4.1 Dva projekty (`dev` pro DEV build, `prod` pro PROD build) v regionu EU, oba na free plánu. Adresa projektu a veřejný klíč přes konfiguraci buildu mimo repozitář. Klíč `service_role` nikdy do aplikace ani do repozitáře.
- [ ] 4.2 SQL migrace schématu verzované v repozitáři (v2 je privátní), podle modelu z fáze 3.
- [ ] 4.3 Přihlášení Google (poskytovatel Google v Supabase Auth, OAuth klient v Google Cloud Console s otiskem SHA podpisového klíče, přihlášení v aplikaci přes výběr účtu a předání tokenu Supabase), případně e-mail a heslo pro testovací účty. Role trenér/klient v tabulkách, účty trenérů zakládá Erika ručně (viz ADR-003).
- [ ] 4.4 Row Level Security a její automatizované testy: trenér vidí jen své klienty, klient jen sebe. Ověřit lokální běh Supabase přes CLI (vyžaduje Docker).
- [ ] 4.5 Propojení klienta s profilem podle ověřeného e-mailu: politika RLS porovná e-mail z přihlášení s e-mailem v profilu klienta. Bez pozvánek a bez serverového kódu. Ukládat e-maily malými písmeny, hlídat ověřený e-mail.
- [ ] 4.6 **Zálohy na free plánu nejsou.** Místo nich: seed skript pro demo data (viz 5.6) a pravidelný výpis databáze přes nástroje Supabase mimo repozitář (ověřit příkaz a obnovu). Projekt se po 7 dnech nízké aktivity pozastaví (týden předem přijde e-mail, obnoví se tlačítkem *Resume project*, data zůstanou, do 1 roku). Zvážit pravidelné drobné volání API proti pozastavení a před obhajobou ověřit, že projekt běží. **Rozhodnuto: žádný placený plán.**
- [ ] 4.7 Média: odkazy na YouTube, nebo Supabase Storage (1 GB zdarma), viz fáze 8.

## Fáze 5 – Přihlášení a offline vrstva v Androidu

- [ ] 5.1 Přihlášení Google účtem, správa relace (obnova tokenu), odhlášení, smazání účtu (výmaz dat na žádost, viz 1.7).
- [ ] 5.2 Offline vrstva podle ADR-002: plán a cviky se stahují do Room pro čtení bez připojení, odcvičené tréninky se ukládají lokálně jako nové záznamy s UUID a po připojení se odešlou (fronta odchozích změn, WorkManager, idempotentní zápis). Obecná obousměrná synchronizace se nevyvíjí.
- [ ] 5.3 Konflikty se řeší návrhem, ne kódem: výsledky zapisuje jen klient (nové záznamy), plán jen trenér. Stahování podle `updatedAt` a příznaku smazání (`deletedAt`).
- [ ] 5.4 Stav odeslání v UI (čeká / odesláno / chyba).
- [ ] 5.5 Testy offline scénářů: letecký režim, ukončení aplikace během offline zápisu, obnovení spojení, dvě zařízení.
- [ ] 5.6 Seed skript s demo daty (klienti, programy, historie) pro vývoj, testování a obhajobu. Reprodukovatelný, bez reálných osobních údajů.
- [ ] 5.7 **Volitelné:** přenos dat z v1 do v2 (v2 má nové `applicationId`). Jen pokud se Erika rozhodne, že data z v1 chce ve v2 mít, jinak v2 startuje s demo daty. Využije zálohu z v1 (viz 2.0).

## Fáze 6 – Plánování a klientský režim v aplikaci

Dřív to mělo být na webu, teď to musí zvládnout aplikace.

- [ ] 6.1 Vytvoření programu a fází pro klienta (trenérský režim).
- [ ] 6.2 Plánování fází do kalendáře v čase.
- [ ] 6.3 Sdílení a kopírování programů mezi klienty (v rámci jednoho trenéra).
- [ ] 6.4 Klientský režim: „Můj plán“, dnešní trénink, zápis offline, vlastní historie.
- [ ] 6.5 První přihlášení klienta Google účtem a propojení s jeho profilem (viz 4.5), bez pozvánek.
- [ ] 6.6 Přehled výsledků klienta pro trenéra.

## Fáze 7 – Analytika a progresivní přetížení

- [ ] 7.1 Definice metrik (objem, odhad maxima na jedno opakování, rekordy, dodržení plánu).
- [ ] 7.2 Grafy v aplikaci.
- [ ] 7.3 Pravidla progresivního přetížení: návrh další zátěže z předchozích výsledků, trenér ho potvrzuje.
- [ ] 7.4 Vyhodnocení na datech z pilotu.

## Fáze 8 – Média ke cvikům

- [ ] 8.1 Rozhodnout: odkazy na YouTube, nebo Supabase Storage. Ověřit licence obsahu.
- [ ] 8.2 Zobrazení v aplikaci a doplnění médií k výchozím cvikům (dnes má odkaz 4 z 78).

## Fáze 9 – Pilot a obhajoba (cíl diplomky)

- [ ] 9.1 Souhlas pilotních klientů se zpracováním údajů, minimální množství osobních údajů, záznamy o zpracování.
- [ ] 9.2 Distribuce pilotním klientům jako APK z privátního repozitáře nebo odkazem (Google Play se nepoužívá), aktualizace ručně. Nový mechanismus aktualizací se nevyvíjí.
- [ ] 9.3 Hlášení chyb z pilotu (např. jednoduché sbírání logů nebo formulář).
- [ ] 9.4 Pilot s několika klienty, sběr zpětné vazby.
- [ ] 9.5 Příprava obhajoby: ověřit, že Supabase projekt běží (není pozastavený), obnovit demo data seed skriptem, zkouška offline scénáře na dvou zařízeních.
- [ ] 9.6 Uživatelské testování jako podklad pro diplomku.
- [ ] 9.7 Po obhajobě: smazat data pilotu v cloudu a projekt (výmaz), informovat klienty.

## Fáze W – Web a další rozšíření (mimo rozsah diplomky)

Jen ověřit, že návrh nic neblokuje (oprávnění v RLS, žádná logika uzamčená v aplikaci). Samotná implementace patří do kapitoly o návrzích na rozšíření.

- [ ] W.1 Web pro trenéra napojený na stejnou databázi
- [ ] W.2 Plánování v kalendáři na počítači
- [ ] W.3 Více trenérů: správa, fakturace, smluvní dokumenty
- [ ] W.4 iOS
- [ ] W.5 Automatické pozvánky klientů, samoobslužná registrace trenérů a správa rolí (vyžaduje serverový kód, např. Edge Functions nebo funkce v databázi)
- [ ] W.6 Obecná obousměrná synchronizace s řešením konfliktů (nebo PowerSync)

---

## Diplomka – kapitoly a podklady

| Kapitola | Podklady z plánu |
|---|---|
| Úvod, cíle, metodika | 1.1, 1.10 |
| Rešerše a analýza současných řešení | 1.3 |
| Analýza požadavků a případy užití | 1.1, 1.2 |
| Návrh architektury (volba backendu, offline, přihlášení) | 1.4, 1.5 (ADR-001 porovnání Supabase a Firebase, ADR-002, ADR-003) |
| Datový model a multi-tenance | 1.6, 1.11, fáze 3 |
| Bezpečnost a ochrana osobních údajů | 1.7, 4.4, 9.1 |
| Implementace (Android, backend) | fáze 3 až 6 |
| Analytika a progresivní přetížení | fáze 7 |
| Testování a vyhodnocení | 1.10, 5.5, 9.4, 9.6 |
| Návrhy na rozšíření (web, více trenérů, iOS, pozvánky, obousměrná synchronizace, provoz po obhajobě) | fáze W |
| Závěr | celek |

Pravidla pro psaní: každý zdroj do `sources.md` se stavem ověření, citace od AI nepřebírat bez kontroly. Změna zadání (provoz po obhajobě) a z ní plynoucí zvažování obou backendů patří do kapitoly o návrhu, zdůvodnění je v ADR-001.

## Rizika

- **Ztráta dat klientů ve v1** při aktualizaci PROD (migrace databáze 3→4): migrace je jedna změna tabulky a je otestovaná na kopii reálných dat. Záloha před migrací není možná (PROD zatím žádný export nemá), proto záloha ihned po instalaci. Na starší verzi se nelze vrátit bez odinstalace.
- **Free plán Supabase:** bez záloh, pozastavení po 7 dnech nízké aktivity (obnova tlačítkem, data zůstanou). Zmírnění: seed skript na obnovu demo dat, pravidelný výpis databáze, kontrola před obhajobou.
- **Offline vrstva je největší technické riziko:** držet ji zúženou (čtení plánu, odesílání nových výsledků), psát testy. Pokud by se nestihla, odložit obousměrnou část do rozšíření, záložní možnost PowerSync.
- **Rozsah vs termín:** plánování musí být v aplikaci, protože web odpadá. Držet jádro M1 až M5 a M7.
- **Zdravotní údaje (GDPR):** řešit od návrhu, souhlas pilotních klientů, výmaz po obhajobě.
- **Veřejný repozitář (v1):** nikdy nevkládat klíče, data klientů ani exporty databáze. v2 vzniká v privátním repozitáři.
- **Ztráta podepisovacího klíče v1:** záloha mimo repo, heslo v 1Passwordu.

## Poznámky z prostředí (stav 2026-11-08)

- **Android Studio:** složka `C:\Program Files\Android\Android Studio` je poškozený pozůstatek (chybí `product-info.json`). Zástupce ve Start menu míří právě tam. Funkční instalace (build AI-261.26222.65) je v `C:\Program Files\Android\Android Studio1`. Nastavení a projekty jsou v `AppData\...\AndroidStudio2026.1.4`.
- **`gh`:** nainstalované a přihlášené (účet Sunflavoured, oprávnění `repo`, `read:org`, `gist`). Terminály spuštěné před instalací ho nemusí vidět v PATH, pak volat plnou cestou `C:\Program Files\GitHub CLI\gh.exe`.
- **Podepisování commitů:** přes 1Password. Pokud je 1Password zamčený, commit se zasekne a vyprší čas. Před commitem odemknout 1Password a potvrdit dialog.
- **DEV a PROD:** debug build má příponu `.dev`, instaluje se vedle PROD a data nesdílí.
- **Lokální build a testy:** Claude Code umí aplikaci sestavit (`gradlew`) a spustit instrumentované testy na běžícím emulátoru Pixel_6 (Android 16). Testy používají databázi v paměti a data aplikace se nedotýkají. Gradle běží na JDK `C:\Users\erika\.jdks\jbr-21.0.11`. Po testech zůstává na emulátoru nainstalovaná DEV aplikace a testovací APK, PROD balíček se nemění. Na fyzický telefon se bez tvého svolení nic neinstaluje. Gradle ale pouští testy na všech připojených zařízeních, proto vždy s `ANDROID_SERIAL=emulator-5554`. Jednou se testy omylem spustily na připojeném telefonu (aktualizovala se DEV aplikace a nainstaloval testovací balíček `.dev.test`, PROD ani žádná data se nezměnily).
- **Issues na GitHubu:** #1 až #13, většina je dnes už funkčně hotová, viz 0.9.

## Návrh rozložení aplikace v2 (k rozpracování v 1.8)

Nápad od Eriky, určeno pro v2. Dnešní rozcestník na hlavní stránce se nahradí běžnou navigací.

- **Spodní lišta** přepíná hlavní sekce: *Přehled*, *Kalendář tréninků*, *Měření*, *Knihovna* (databáze tréninků a cviků).
- **Vysouvací levý panel** s přepínáním osoby: výchozí je uživatel sám („já“), pod ním jeho klienti. Po přepnutí ukazují všechny sekce data vybraného klienta.
- **Klientský režim** má stejné rozložení bez přepínání osob, vidí jen vlastní data.
- Princip: *kdo* (osoba v levém panelu) je oddělené od *co* (sekce ve spodní liště).
- K rozhodnutí: co přesně je *Přehled* (dnešní trénink, nejbližší plán, poslední výsledky, rychlé akce), kam patří správa klientů a nastavení účtu, zda má *Knihovna* záložky *Tréninky* a *Cviky*.
- Navazuje na model oprávnění z 1.7 (kdo smí přepnout na koho).

## Backlog nápadů

Sem patří všechno nové, než se rozhodne, do které fáze to spadá. Návrhy k posouzení:

- [ ] Připomínky tréninku (notifikace)
- [ ] Export programu nebo tréninku do PDF pro klienta
- [ ] Fotky pokroku u měření těla
- [ ] Tmavý režim (dnes je vynucený světlý)
- [ ] Šablony programů pro rehabilitaci
- [ ] Přetahování cviků prstem v editoru jednotky (dnes šipky)
