# App Manuale Incidenti Stradali 2.0 — CNVVF

App Android (APK) per la consultazione rapida del **Manuale Operativo Incidenti Stradali vers. 2.0/2024** del Corpo Nazionale dei Vigili del Fuoco, basata sulla presentazione "Guida al manuale operativo incidenti stradali 2.0" (tutor: VE Di Lorenzo Luciano).

## Funzionalità

- **Splash screen** con la copertina del manuale all'avvio
- **7 sezioni** (PARTI 1-7) con i colori ufficiali della presentazione:
  | Parte | Titolo | Colore |
  |---|---|---|
  | 1 | L'autoveicolo | `#202060` |
  | 2 | Analisi di una collisione | `#00A0C0` |
  | 3 | Informazioni indispensabili al soccorso | `#C04080` |
  | 4 | Attrezzature per il soccorso | `#60C060` |
  | 5 | La gestione dell'incidente | `#00652E` |
  | 6 | Tecniche d'intervento | `#E57525` |
  | 7 | Strategia d'intervento | `#ED2323` |
- **156 schede** estratte dalle 175 slide della presentazione, con titolo, paragrafo, pagine del manuale, testo, ALERT (rischio generico / elettrocuzione) e slide di riferimento
- **Ricerca istantanea** su tutto il manuale + **tag** (ev-ibride, gas, idrogeno, stabilizzazione, taglio, vetri, airbag, maxi-tamponamenti, …)
- **Video delle manovre** (link dai QR code della presentazione, vigilfuoco.tv) — 8 schede con video
- **Offline-first**: tutti i contenuti sono incorporati nell'APK; serve connessione solo per i video

## Installazione

Scaricare `apk/IncidentiStradali2.0.apk` e installarlo sul telefono (abilitare "origini sconosciute"). minSdk 24 (Android 7.0+).

## Struttura progetto

- `app/` — progetto Android (Kotlin + Jetpack Compose)
- `scripts/generate_content.py` — rigenera `app/src/main/assets/data/content.json` e le immagini slide dai PDF sorgente
- `apk/IncidentiStradali2.0.apk` — APK firmato pronto per l'installazione

## Build

```bash
export ANDROID_HOME=<percorso SDK>
gradle assembleRelease
```

## Fonti

- Manuale: https://www.vigilfuoco.it/allegati/biblioteca/MANUALE_Operativo_ver2_0del2024.pdf
- Contenuti proprietà CNVVF — uso riservato al progetto istituzionale.
