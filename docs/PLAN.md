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
- **Cíl diplomky: milník M7 (ostrý provoz a pilot) bez webu.** Web je návrh rozšíření a nebude výstupem diplomky. Architektura s ním ale počítá: web se později napojí na stejnou databázi, proto musí být oprávnění vyřešená v databázi (RLS), ne v aplikaci.
- **Backend: Supabase, free plán** (rozhodnuto 2026-11-08). Dva zdarma dostupné projekty využijeme jako `dev` a `prod`, stejně jako dnešní DEV a PROD build.
- Má vzniknout **platforma pro více trenérů**. Navrhujeme `trainerId` v schématu a oprávnění přes RLS hned od začátku, uživatelské rozhraní zatím pro jednoho trenéra.
- **Klienti budou zapisovat tréninky offline** (špatný signál v posilovně). Z toho plyne klientský režim v aplikaci (viz 1.2).
- Termín: několik měsíců (přesné datum doplnit).
- První uživatelé: Erika a její klienti.
- Workflow: Claude Code + Android Studio (USB, kontrola diffu), GitHub issues přes `gh`, změny na větvích, nic se nepushuje bez souhlasu, commity jsou podepsané.
- Text diplomky je v **samostatném repozitáři** jako Markdown, později převod do LaTeXu. Zdroje ověřuje Erika. Pravidla školy k AI řeší Erika sama v textu práce.
- Dnešní verze: DEV (debug, přípona `.dev`, nahrává se přes USB) a PROD (release, aktualizace podepsaná klíčem). To zůstává.
- **Strategie v1 a v2 (dohodnuto 2026-11-08):** v1 (`com.example.client_training_app`) se stabilizuje a běží dál v praxi, repozitář zůstává **veřejný** (kontrola aktualizací a stahování APK z Releases). v2 vznikne jako **nová aplikace** v **novém privátním repozitáři** s novým `applicationId`, názvem a keystorem. Data se přenesou importem z exportu v1.
- Větší přestavba rozložení aplikace (spodní lišta, boční panel s přepínáním klientů) patří do v2, viz sekce *Návrh rozložení aplikace v2*. U v1 se dělají jen drobná vizuální vylepšení.

## 2. Otevřené otázky

- Přesné datum odevzdání. Podle něj se určí, kdy nejpozději musí začít pilot (doporučení: s rezervou několika týdnů na sběr dat a vyhodnocení).
- **Synchronizace: vlastní řešení, nebo PowerSync?** Backend je rozhodnutý, tuto otázku je potřeba uzavřít jako první technickou (viz 1.5).
- Klientský režim: má klient vlastní účet a zapisuje sám, a zapisuje trenér také za klienta při osobním tréninku? (viz 1.2)
- Registrace trenérů: volná, nebo jen na pozvání?
- Může mít klient více trenérů, nebo vždy jednoho?
- Konkrétní `applicationId` a název v2 (viz 1.9). Po publikaci v Google Play už ID nejde změnit.- Kde je uložený keystore pro podepisování PROD verze? (viz 0.4)

## 3. Milníky

| Milník | Obsah | Fáze |
|---|---|---|
| M1 | Stabilní základ, nástroje fungují, repo uklizené | 0, 2 |
| M2 | Analýza a rozhodnutí hotová | 1 |
| M3 | Nový datový model v aplikaci | 3 |
| M4 | Cloud, přihlášení, synchronizace | 4, 5 |
| M5 | Plánování programů a fází **v aplikaci**, klientský režim | 6 |
| M6 | Analytika, média, progresivní přetížení | 7, 8 |
| M7 | Ostrý provoz a pilot (cíl diplomky) | 9 |
| – | Web a další rozšíření (mimo rozsah) | W |

Pořadí priorit při tlaku na termín: M1 až M5 a M7 jsou jádro. Z M6 vzít nejdřív základní grafy, progresivní přetížení a média jsou rezerva.

---

## Fáze 0 – Prostředí a repozitář (blokery)

- [ ] 0.1 Spouštět Android Studio z `C:\Program Files\Android\Android Studio1\bin\studio64.exe` (viz poznámky na konci), opravit zástupce.
- [x] 0.2 Přihlásit `gh` (hotovo, účet Sunflavoured).
- [ ] 0.3 **Zálohovat** celou složku `client_training_app` mimo Git a **exportovat databázi z telefonu s PROD verzí** (jsou v ní reálná data klientů). Udělat před jakýmkoli instalováním nové verze.
- [ ] 0.4 Keystore PROD je v `C:\Users\erika\OneDrive\Training_App_klicenka\moje_klicenka` (složka byla přejmenována, Android Studio ukazuje ještě na starou cestu `...\Training_App\...`, při příštím podepisování ji přenastavit). Ověřit, že odpovídá PROD APK (otisk SHA-256 podpisu `7accf4cb…42d5`, příkaz `keytool -list -v -keystore <soubor>`, heslo zadáš jen ty). Uložit soubor jako přílohu do 1Passwordu a heslo k němu. Bez klíče nelze vydat aktualizaci přes existující instalaci.
- [ ] 0.5 Ověřit, že DEV build (`.dev`) se instaluje vedle PROD a data nesdílí (dosud fungovalo).
- [ ] 0.6 Pushnout `chore/repo-cleanup` a otevřít PR do `master`, potom PR pro `04-dev-features`. Po sloučení vytvořit nový release.
- [ ] 0.7 Commitnout tento soubor a odkaz na něj v `CLAUDE.md`.
- [ ] 0.8 Založit repozitář diplomky (Markdown): kapitoly, `sources.md` (zdroj, odkaz, stav ověření a datum), složka `decisions/`, složka `figures/`.
- [ ] 0.9 Projít stávající issues (#1 až #13). Většina odpovídá už hotovým funkcím (profily, cviky, kalendář, jednotky, historie, sdílení), zavřít ty hotové a zbytek přeřadit. Nové issues podle tohoto plánu s milníky M1 až M7 a štítky `bug`, `feature`, `backend`, `data-model`, `sync`, `thesis`.
- [ ] 0.10 Smazat staré sloučené větve na GitHubu (01, 02, 03). Volitelné, až po sloučení.
- [ ] 0.11 Vytvořit **privátní** repozitář pro v2 (až bude rozhodnuté `applicationId` a název, viz 1.9). V1 repozitář zůstává veřejný.

## Fáze 1 – Analýza a rozhodnutí (před psaním kódu)

- [ ] 1.1 Jeden seznam požadavků (funkční a nefunkční), roztříděný podle MoSCoW. Sem patří všechny dosavadní nápady. Web a iOS označit jako mimo rozsah.
- [ ] 1.2 Role a případy užití. **Trenérský režim** (správa klientů, plánování, přehledy) a **klientský režim** (vlastní plán, zápis tréninku offline, vlastní historie). Rozhodnout, zda trenér zapisuje i za klienta a kdo je autorem záznamu.
- [ ] 1.3 Rešerše podobných řešení (co umí, co chybí). Zdroje ověřit a zapsat do `sources.md`.
- [x] 1.4 Backend: Supabase free plán (rozhodnuto 2026-11-08). Zapsat jako záznam rozhodnutí včetně alternativy Firebase a důvodů.
- [ ] 1.5 Synchronizace: ověřit PowerSync (ceny, podmínky, podpora Room a Kotlinu) proti vlastnímu řešení nad Room. Zapsat záznam rozhodnutí. Strategie konfliktů, mazání (tombstony), pořadí push/pull, čas serveru vs zařízení.
- [ ] 1.6 Datový model v2: ER diagram a pravidla:
  - UUID ve všech tabulkách (dnes jsou v polovině tabulek autoincrementy),
  - `createdAt`, `updatedAt`, `deletedAt`, `trainerId`/`ownerId`,
  - číselné hodnoty místo textu (váha, opakování od–do, RIR, čas, vzdálenost),
  - plán vs skutečnost, šablona vs naplánovaná instance (snapshot),
  - program → fáze → jednotka,
  - datum plánu jako kalendářní datum, ne epoch půlnoc v časové zóně zařízení,
  - svalové skupiny jako klíče, ne české popisky,
  - jednotky (kg, cm),
  - vztah trenér–klient a účet vs profil klienta (klient může existovat bez účtu).
- [ ] 1.7 Bezpečnost a GDPR: matice oprávnění (kdo co vidí), souhlas klienta, doba uchování, právo na výmaz a export, smlouva se zpracovatelem, region EU, zásady ochrany osobních údajů. Jde o zdravotní údaje. Pozor na role: dokud aplikaci používá jen Erika, je správcem údajů ona. Pokud ji budou používat další trenéři, stává se provozovatelem platformy (zpracovatelem) a trenéři správci. To je téma pro návrh a diplomku, právní dokumenty pro cizí trenéry teď neřešit.
- [ ] 1.8 Uživatelské toky a drátěné modely aplikace: přihlášení, pozvání klienta, plánování fází, zápis tréninku, grafy.
- [ ] 1.9 Rozhodnout `applicationId` a název aplikace. **Návrh:** v2 jako nová aplikace (nové ID, nový název, nový keystore uložený od začátku v 1Passwordu). V1 (`com.example.client_training_app`) běží dál pro praxi a dostává jen opravy z fáze 2. Po publikaci v Google Play už ID nelze změnit, proto ho vybrat s rozmyslem. Přenos dat: import exportu z v1 (viz 2.0 a 5.7), ne migrace na místě.
- [ ] 1.10 Kritéria úspěchu a způsob testování (offline scénáře, uživatelský test, případně dotazník použitelnosti). Podklad pro kapitolu o testování.
- [ ] 1.11 Multi-tenance: model trenér–klient, kdo může založit účet trenéra, globální cviky vs vlastní cviky trenéra, sdílení programů v rámci jednoho trenéra (mezi trenéry až později).

## Fáze 2 – Stabilizace stávající aplikace

Opravy v1 (stávající aplikace, stejné `applicationId` a klíč), které se vydají jako aktualizace PROD. Neřešit zde nic, co se mění ve fázi 3. Pracovat na samostatné větvi, testovat přes DEV build (má vlastní databázi, data PROD se nedotkne).

- [ ] 2.0 **Záloha a export všech dat** (klienti, jednotky, kalendář, historie, měření) do jednoho souboru přes sdílení a import/obnova z něj. Dnes se ze stávající PROD databáze nedá nic vytáhnout (aplikace není ladicí, `adb backup` na Android 12+ nefunguje, export umí jen jednu tréninkovou jednotku). Po instalaci aktualizace udělat **jako první věc** zálohu. Soubor je zároveň podklad pro přenos do v2.
- [ ] 2.1 Ukládat `rest` při dokončení tréninku (dnes se do `WorkoutSetResultEntity` nepřenáší).
- [ ] 2.2 Kalendář: výběr tréninku sbírá Flow bez ukončení, bottom sheet se může otevírat opakovaně (ověřit a opravit).
- [ ] 2.3 Kalendář: označit splněné tréninky (kód je zakomentovaný).
- [ ] 2.4 Měření těla: zprovoznit vstup z detailu klienta (tlačítko je zatím placeholder, obrazovka existuje).
- [ ] 2.5 Hodnocení a poznámka k tréninku (`rating` a `note` se nikdy nevyplní).
- [ ] 2.6 Výchozí cviky se přepisují z JSON při každém otevření databáze. Verzovat seed, ať se uživatelské úpravy nepřepíšou.
- [ ] 2.7 Základní testy (DAO, migrace Room) jako záchranná síť před refaktorem.
- [ ] 2.8 Zvážit zapnutí minifikace u release (ověřit Gson a Room).
- [x] 2.9 (hotovo na větvi `fix/stabilizace`, pokryto testy `TemplateEditIntegrityTest`, ověřeno na emulátoru) **Úprava šablony nesmí měnit nic jiného.** `insertOrUpdateUnit` používá REPLACE, který řádek smaže a znovu vloží. Na kopii databáze ověřeno: odcvičeným tréninkům se přitom smaže odkaz na šablonu (`SET NULL`) a smažou se její cviky (`CASCADE`). Nahradit skutečným UPDATE/UPSERT. Důsledek, který Erika popsala: trénink v kalendáři zůstane, ale po otevření je prázdný. Pravděpodobná příčina: `ActiveWorkoutViewModel.loadFromHistory` bere ID šablony ze session (po REPLACE je null) místo z parametru z kalendáře. Opravit i toto (použít ID z kalendáře, při chybějící šabloně zobrazit uložené série), čímž se napraví i již poškozená data. Ověřit testem.
- [x] 2.10 (hotovo na větvi `fix/stabilizace`: varování s počtem naplánovaných a odcvičených, plán se smaže spolu se šablonou, historie zůstane; ověřeno testy, dialog zatím vizuálně neověřen) Smazání šablony, která je naplánovaná v kalendáři, narazí na cizí klíč (na kopii databáze ověřeno: `FOREIGN KEY constraint failed`, v aplikaci pravděpodobně pád). Ošetřit: varování a rozhodnutí, co se plánem stane.
- [ ] 2.11 Přidání **nového cviku** přímo z výběru cviků (během tréninku i při tvorbě jednotky). Dnes výběr cviků jen vybírá, tlačítko pro nový cvik chybí.
- [ ] 2.12 Změna **pořadí cviků** v editoru tréninkové jednotky (dnes jde jen v aktivním tréninku). Táhnutí nebo šipky nahoru/dolů, editor je v rolovacím okně.
- [ ] 2.14 Z kalendáře jde naplánovaný trénink jen přidat, **odebrat ho nejde** (smazání je v kódu, ale chybí v obrazovce). Přidat odebrání, nejlépe s potvrzením.
- [ ] 2.13 Drobná vizuální vylepšení v1: seznam konkrétních výhrad od Eriky (obrazovka, co vadí), pak po jedné obrazovce. Přestavba rozložení je záležitost v2.

## Fáze 3 – Datový model v2 v aplikaci (Room)

- [ ] 3.1 Migrace Room 3→4 se zachováním dat (UUID, `updatedAt`, `deletedAt`, `trainerId`).
- [ ] 3.2 Typované hodnoty a převod stávajících textových dat.
- [ ] 3.3 Program → fáze → jednotka, šablona vs instance.
- [ ] 3.4 Datum plánu jako `LocalDate`.
- [ ] 3.5 Svalové skupiny jako klíče, překlad v UI.
- [ ] 3.6 Verzovaný formát JSON exportu a importu se zpětnou kompatibilitou.
- [ ] 3.7 Test migrace na **anonymizované** kopii reálné databáze (`MigrationTestHelper`).
- [ ] 3.8 Aktualizovat `CLAUDE.md` a `app/schemas/`.

## Fáze 4 – Backend (Supabase free plán)

- [ ] 4.1 Dva projekty v regionu EU: `dev` (pro DEV build) a `prod` (pro PROD build). Adresa a veřejný klíč přes konfiguraci buildu mimo repozitář. Klíč `service_role` nikdy do aplikace ani do repozitáře.
- [ ] 4.2 SQL migrace schématu verzované v repozitáři, shodné s modelem z fáze 3.
- [ ] 4.3 Přihlášení: e-mail a heslo (případně magic link). Role trenér/klient.
- [ ] 4.4 Row Level Security a automatizované testy politik (trenér vidí jen své klienty, klient jen sebe). Ověřit možnost lokálního běhu Supabase přes jeho CLI.
- [ ] 4.5 Pozvánky klientů (kód nebo odkaz) a převzetí existujícího profilu klienta.
- [ ] 4.6 **Zálohy na free plánu nejsou.** Zavést vlastní pravidelný export databáze (např. týdenní dump z Erikina počítače), uložit mimo repozitář a **otestovat obnovu**. Projekt se po týdnu neaktivity pozastaví, hlídat to před dovolenou. Rozhodnout před pilotem, kdy přejít na placený plán (zálohy a žádné pozastavení).
- [ ] 4.7 Média: úložiště pro obrázky a GIFy, nebo jen odkazy na YouTube (viz fáze 8).

## Fáze 5 – Přihlášení a synchronizace v Androidu

- [ ] 5.1 Přihlašovací obrazovky, správa relace, odhlášení, smazání účtu. Ověřit aktuální požadavky Google Play na mazání účtu a formulář o datech.
- [ ] 5.2 Synchronizační vrstva podle rozhodnutí z 1.5 (např. fronta odchozích změn, stahování podle `updatedAt`, tombstony, WorkManager, idempotence).
- [ ] 5.3 Řešení konfliktů podle rozhodnutí z 1.5.
- [ ] 5.4 Stav synchronizace v UI (čeká / synchronizováno / chyba).
- [ ] 5.5 Testy offline scénářů: letecký režim, přerušené spojení, dvě zařízení.
- [ ] 5.6 Jednorázové nahrání stávajících lokálních dat do cloudu (se zálohou před spuštěním).
- [ ] 5.7 Přenos dat z v1 do v2 (v2 má nové `applicationId`): import souboru z exportu v1 (viz 2.0), kontrola počtů záznamů (klienti, jednotky, kalendář, tréninky, série), nahrání do cloudu. V1 ponechat jako zálohu, dokud pilot neprojde. Klienti dostanou rovnou v2. Kontrola nové verze přes GitHub Releases na v2 nenaváže, je potřeba nový mechanismus (viz 9.2).

## Fáze 6 – Plánování a klientský režim v aplikaci

Dřív to mělo být na webu, teď to musí zvládnout aplikace.

- [ ] 6.1 Vytvoření programu a fází pro klienta (trenérský režim).
- [ ] 6.2 Plánování fází do kalendáře v čase.
- [ ] 6.3 Sdílení a kopírování programů mezi klienty (v rámci jednoho trenéra).
- [ ] 6.4 Klientský režim: „Můj plán“, dnešní trénink, zápis offline, vlastní historie.
- [ ] 6.5 Pozvání klienta do aplikace a první přihlášení (propojení s 4.5).
- [ ] 6.6 Přehled výsledků klienta pro trenéra.

## Fáze 7 – Analytika a progresivní přetížení

- [ ] 7.1 Definice metrik (objem, odhad maxima na jedno opakování, rekordy, dodržení plánu).
- [ ] 7.2 Grafy v aplikaci.
- [ ] 7.3 Pravidla progresivního přetížení: návrh další zátěže z předchozích výsledků, trenér ho potvrzuje.
- [ ] 7.4 Vyhodnocení na reálných datech z pilotu.

## Fáze 8 – Média ke cvikům

- [ ] 8.1 Rozhodnout: odkazy na YouTube, nebo vlastní hosting GIFů. Ověřit licence obsahu.
- [ ] 8.2 Zobrazení v aplikaci a doplnění médií k výchozím cvikům (dnes má odkaz 4 z 78).

## Fáze 9 – Provoz a pilot (cíl diplomky)

- [ ] 9.1 Zásady ochrany osobních údajů, souhlas klientů, záznamy o zpracování.
- [ ] 9.2 Distribuce (vnitřní testování v Google Play, nebo APK) a mechanismus aktualizací.
- [ ] 9.3 Hlášení chyb z provozu.
- [ ] 9.4 Pilot: Erika a její klienti, sběr zpětné vazby.
- [ ] 9.5 Rozhodnutí o přechodu Supabase na placený plán a test obnovy ze zálohy.
- [ ] 9.6 Uživatelské testování jako podklad pro diplomku.

## Fáze W – Web a další rozšíření (mimo rozsah diplomky)

Jen ověřit, že návrh nic neblokuje (oprávnění v RLS, žádná logika uzamčená v aplikaci). Samotná implementace patří do kapitoly o návrzích na rozšíření.

- [ ] W.1 Web pro trenéra napojený na stejnou databázi
- [ ] W.2 Plánování v kalendáři na počítači
- [ ] W.3 Více trenérů: správa, fakturace, smluvní dokumenty
- [ ] W.4 iOS

---

## Diplomka – kapitoly a podklady

| Kapitola | Podklady z plánu |
|---|---|
| Úvod, cíle, metodika | 1.1, 1.10 |
| Rešerše a analýza současných řešení | 1.3 |
| Analýza požadavků a případy užití | 1.1, 1.2 |
| Návrh architektury (volba backendu, synchronizace) | 1.4, 1.5 |
| Datový model a multi-tenance | 1.6, 1.11, fáze 3 |
| Bezpečnost a ochrana osobních údajů | 1.7, 4.4, 9.1 |
| Implementace (Android, backend) | fáze 3 až 6 |
| Analytika a progresivní přetížení | fáze 7 |
| Testování a vyhodnocení | 1.10, 5.5, 9.4, 9.6 |
| Návrhy na rozšíření (web, více trenérů, iOS) | fáze W |
| Závěr | celek |

Pravidla pro psaní: každý zdroj do `sources.md` se stavem ověření, citace od AI nepřebírat bez kontroly.

## Rizika

- **Ztráta dat klientů** při migraci nebo přechodu na nové ID: záloha databáze před každou instalací PROD verze, testy jen přes DEV build.
- **Free plán Supabase bez záloh a s pozastavováním:** vlastní týdenní export a test obnovy, hlídat neaktivitu.
- **Vlastní synchronizace je složitá:** začít jednoduše (poslední zápis vyhrává), udělat prototyp, psát testy. Rozhodnutí o PowerSync co nejdřív.
- **Rozsah vs termín:** plánování musí být v aplikaci, protože web odpadá. Držet jádro M1 až M5 a M7.
- **Zdravotní údaje (GDPR):** řešit od návrhu. Při více trenérech se mění role provozovatele.
- **Veřejný repozitář:** nikdy nevkládat klíče, data klientů, exporty databáze ani `service_role` klíč.
- **Ztráta podepisovacího klíče:** záloha mimo repo, heslo v 1Passwordu.

## Poznámky z prostředí (stav 2026-11-08)

- **Android Studio:** složka `C:\Program Files\Android\Android Studio` je poškozený pozůstatek (chybí `product-info.json`). Zástupce ve Start menu míří právě tam. Funkční instalace (build AI-261.26222.65) je v `C:\Program Files\Android\Android Studio1`. Nastavení a projekty jsou v `AppData\...\AndroidStudio2026.1.4`.
- **`gh`:** nainstalované a přihlášené (účet Sunflavoured, oprávnění `repo`, `read:org`, `gist`). Terminály spuštěné před instalací ho nemusí vidět v PATH, pak volat plnou cestou `C:\Program Files\GitHub CLI\gh.exe`.
- **Podepisování commitů:** přes 1Password. Pokud je 1Password zamčený, commit se zasekne a vyprší čas. Před commitem odemknout 1Password a potvrdit dialog.
- **DEV a PROD:** debug build má příponu `.dev`, instaluje se vedle PROD a data nesdílí.
- **Lokální build a testy:** Claude Code umí aplikaci sestavit (`gradlew`) a spustit instrumentované testy na běžícím emulátoru Pixel_6 (Android 16). Testy používají databázi v paměti a data aplikace se nedotýkají. Gradle běží na JDK `C:\Users\erika\.jdks\jbr-21.0.11`. Po testech zůstává na emulátoru nainstalovaná DEV aplikace a testovací APK, PROD balíček se nemění. Na fyzický telefon se bez tvého svolení nic neinstaluje.
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
