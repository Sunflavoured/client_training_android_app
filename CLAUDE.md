# CLAUDE.md – Training App (client_training_app)

Android aplikace pro trenéra: správa klientů, knihovna cviků, tréninkové jednotky, kalendář a zápis
odcvičených sérií. Je to praktická část diplomové práce *Mobilní platforma pro podporu a rozvoj
tréninkových a rehabilitačních služeb* a zároveň se má používat v praxi ve fitku.
Komunikace i commity jsou česky.

## Stack (stav k 2026-11-08)
- Nativní Kotlin 2.0.21, AGP 8.13.2, Gradle 8.13, minSdk 26, target/compileSdk 34
- UI: XML layouty + ViewBinding, jedna `MainActivity`, Navigation Component (Fragmenty, Safe Args)
- Data: Room 2.6.1 (schéma verze 4, `exportSchema = true`), KSP, Coroutines/Flow, LiveData, Gson
- Knihovny: kizitonwose CalendarView, android-youtube-player (zatím nevyužito)
- Žádná dependency injection, repozitáře se vytvářejí ve Fragmentech/ViewModelech z `Context`
- Distribuce: APK přes GitHub Releases, aplikace při startu kontroluje `releases/latest`

## Struktura (`app/src/main/java/com/example/client_training_app`)
- `data/entity`, `data/dao`, `data/repository`, `data/database` – Room (entity, DAO, `AppDatabase`, `DatabaseInstance`)
- `model` – aplikační modely a převody entita ↔ model
- `ui/<feature>` – Fragmenty, ViewModely a adaptéry (profile, exercise, training_unit, training_calendar, active_workout, exercise_history, home)
- `utils/TransferManager.kt` – export/import tréninkové jednotky jako JSON
- `res/raw/default_exercises.json` – výchozí cviky, při každém otevření DB se upsertují
- `app/schemas/` – exportovaná schémata Room (necommitovat ručně upravená)

## Příkazy
Aplikaci spouští a ladí uživatelka v Android Studiu (USB zařízení). Z terminálu:
- `.\gradlew.bat assembleDebug` – debug build (`applicationId` má příponu `.dev`, verze `-DEV`)
- `.\gradlew.bat testDebugUnitTest` – unit testy (zatím jen šablona)
- `.\gradlew.bat connectedDebugAndroidTest "-Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true"` – instrumentované testy
  na emulátoru (bez přepínače by je Gradle po testech odinstaloval i s daty DEV aplikace). **Vždy nastavit `ANDROID_SERIAL=emulator-5554`**,
  jinak Gradle pustí testy na všech připojených zařízeních včetně telefonu. Emulátor `Pixel_6` se spouští jako dlouhý proces
  (`emulator.exe -avd Pixel_6`), z krátkého příkazu se po jeho skončení vypne. Jeden test:
  `"-Pandroid.testInstrumentationRunnerArguments.class=com.example.client_training_app.data.TemplateEditIntegrityTest"`
- Gradle musí běžet na JDK 21: `JAVA_HOME=C:\Users\erika\.jdks\jbr-21.0.11` (JDK v Android Studiu je 25, Gradle 8.13 ho oficiálně nepodporuje; projekt používá 21),
  `ANDROID_HOME=C:\Users\erika\AppData\Local\Android\Sdk`
- Release APK vzniká v Android Studiu (Build → Generate Signed Bundle / APK) do `app/release/`;
  složka je ignorovaná gitem, APK se nahrává do GitHub Releases. Keystore si zálohuj mimo repo,
  bez něj nelze vydat aktualizaci přes existující instalaci.

## Pravidla práce
- Neupravovat repozitář bez domluvy a nepushovat bez výslovného souhlasu.
- Změny dělat na samostatné větvi, nikdy přímo na `master`. Commity jsou podepisované (1Password).
- Při změně Room entit vždy zvýšit verzi DB, napsat `Migration` a nechat vygenerovat nové schéma v `app/schemas/`.
- Nikdy necommitovat export databáze, APK, keystore ani žádná data klientů (jde o osobní údaje, částečně zdravotní).
  Pro vývoj a ukázky používat anonymizovaná data.
- Commit zprávy: česky, s číslem issue ve formátu `#N` (jako dosud).

## Plán a checklist
Postup, pořadí kroků a rozhodnutí jsou v `docs/PLAN.md`. Před další prací ho přečíst, držet pořadí fází
a po splnění položky ji odškrtnout. Nové nápady patří do sekce *Backlog nápadů*, ne rovnou do fází.

## Domluvený směr (diplomka)
- Zůstat u nativního Android Kotlinu, webová aplikace pro trenéra vznikne zvlášť (React + TypeScript).
- Cílem je sdílená cloudová databáze s přihlášením (trenér spravuje tréninky klientů), práce offline
  s lokálním Room jako zdrojem pravdy a následnou synchronizací.
- Backend: kandidáti Supabase vs Firebase, zatím nerozhodnuto.
- Plánovaný refaktor datového modelu před synchronizací: UUID ve všech tabulkách, `updatedAt` a `deletedAt`,
  číselné hodnoty místo textu (váha, opakování, RIR), program → fáze → jednotka.
- Text diplomky je v samostatném repozitáři v Markdownu (později LaTeX); zdroje ověřuje uživatelka.
