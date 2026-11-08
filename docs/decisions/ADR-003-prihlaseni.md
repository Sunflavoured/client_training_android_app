# ADR-003: Přihlášení, role a propojení klienta s profilem

Stav: **rozhodnuto** (2026-11-08, Erika)

## Kontext

Klienti mají vidět jen svá data (jde o zdravotní údaje), trenér vidí své klienty. Backend je Supabase na free plánu
(ADR-001). Diplomka má pilot s několika klienty, automatizace správy účtů není její cíl.

## Rozhodnutí

- **Přihlášení:** Google Sign-In jako hlavní způsob. E-mail a heslo jen pro testovací a demo účty (bez Google účtu,
  např. pro obhajobu nebo emulátor).
- **Účty trenérů** zakládá Erika ručně (dashboard nebo jednorázový skript). Role trenéra je záznam odpovídající účtu.
- **Propojení klienta s profilem:** trenér vyplní e-mail v profilu klienta. Politika RLS pustí přihlášeného uživatele
  k datům profilu, jehož e-mail se shoduje s ověřeným e-mailem z přihlášení. Žádné pozvánky.
- **Mimo rozsah diplomky** (návrh na rozšíření, fáze W): automatické pozvánky klientů, samoobslužná registrace
  trenérů, správa rolí přes serverový kód.

## Důsledky

- Google přihlášení vyžaduje OAuth klienta v Google Cloud Console (vázaný na `applicationId` a otisk SHA podpisového
  klíče) a nastavení poskytovatele Google v Supabase. U v2 s novým `applicationId` a keystorem se to dělá až po
  jejich vytvoření. Supabase free plán obsahuje sociální přihlášení.
- Klient potřebuje Google účet. U pilotních klientů s Androidem to nevadí.
- Bez hesel nejsou potřeba e-maily pro reset a odpadá omezení vestavěného rozesílání e-mailů
  (2 e-maily za hodinu na projekt).
- Kontrola e-mailu v RLS je citlivá na velikost písmen a změnu e-mailu: ukládat normalizovaně (malá písmena).
- Bezpečnost stojí na RLS, proto musí mít automatizované testy.

## Co ověřit

- Postup přihlášení Google v Androidu a předání tokenu Supabase (knihovna pro Kotlin, nastavení klienta).
- Ze kterého pole tokenu RLS čte e-mail a že je ověřený.
- Postup pro přidání klienta s jiným než Google účtem (výjimka, ručně).

## Alternativy

- E-mail a heslo pro všechny: vyžaduje reset hesel a e-maily, více práce kolem účtů.
- Automatické pozvánky přes serverový kód: odloženo do rozšíření.
