# Pacchetto per Vibe Code — App Manuale Incidenti Stradali

Questo documento contiene **6 file** da creare nel tuo progetto (cartella `docs/`). Copia ogni sezione in un file separato con il nome indicato, oppure passa tutto intero a Vibe Code chiedendogli di salvarlo come file separati.

**Contenuto del pacchetto:**

| File | Ruolo |
|---|---|
| `PROJECT.md` | Istruzioni per Vibe Code (requisiti app, struttura dati) |
| `KNOWLEDGE.md` | Indice generale del manuale (7 parti, concetti chiave) |
| `estratti-estesi.md` | Estratti fedeli: gestione incidente, gas, maxi tamponamenti |
| `airbag-rischi.md` | Airbag, cinture/pretensionatori, rischi/DPI, varchi |
| `presentazione-struttura.md` | Mappa 175 slide: la fonte principale per le schede |
| `progetto-app.md` | Requisiti app + riferimenti file Drive |

---

## FILE 1/6 — `PROJECT.md`

# Istruzioni per Vibe Code â App "Manuale Incidenti Stradali"

Questo pacchetto contiene i contenuti estratti dal **Manuale Operativo Incidenti Stradali 2.0/2024 (CNVVF)** per generare un'app di consultazione. Proprietario del progetto: Luciano Di Lorenzo (VVF, estensore del nuovo manuale; autore della presentazione "Guida al manuale 2.0").

## Obiettivo dell'app

App Android (**APK installabile localmente**, nessun backend) per la consultazione rapida del manuale, divisa per **argomenti e tag di ricerca**, con richiamo ai **video delle manovre** (link dai QR code del manuale).

## Requisiti obbligatori

1. **Splash screen**: all'avvio, durante il caricamento, mostrare la **copertina del manuale** (vedi `progetto-app.md` per i riferimenti al PDF sorgente; estrarre la copertina dal PDF o chiederla a Luciano come immagine).
2. **Palette colori**: stessa del manuale e della presentazione. Ogni PARTE (1-7) ha il suo colore di sezione: usare quei colori come identificatori visivi coerenti in tutta l'app (header, card, tag, barra di navigazione). I colori esatti vanno estratti dalle slide della presentazione (PDF su Google Drive, file_id `12VYZ9ZoeADYWNbZN3sKusdxjHCM5-1G5`) o dal PDF del manuale â se non disponibili, chiedere a Luciano una palette di riferimento prima di inventarne una.
3. **Struttura contenuti**: basarsi su `presentazione-struttura.md` come mappa degli argomenti (175 slide â schede). Ogni scheda/manovra ha: titolo, PARTE di riferimento (con colore), paragrafo, **pagine del manuale**, testo di sintesi, eventuali **ALERT** (distinguere rischio generico vs rischio elettrocuzione con icone/sfondo diversi), e link al video dove presente.
4. **Tag di ricerca**: indicizzare contenuti per argomento, tipologia veicolo (autovettura, furgone, mezzo pesante, autobus, gas, EV/ibrida, idrogeno), posizione veicolo (ruote, fianco, capovolto), tipo manovra (stabilizzazione, taglio, divaricazione, accesso, cruscotto, sedili, vetriâ¦). Search istantanea.
5. **Video**: ogni manovra con QR nel manuale deve avere un pulsante/link che apre il video (URL di destinazione dei QR). Se i link video non sono ancora disponibili, predisporre il campo URL nel modello dati e un placeholder "video non disponibile".
6. **Offline-first**: tutti i contenuti in locale nell'APK; i video sono link esterni (richiedono connessione solo per quelli).
7. **Lingua**: italiano.

## Contenuti del pacchetto

- `KNOWLEDGE.md` â indice generale: struttura del manuale (7 parti), concetti chiave
- `estratti-estesi.md` â estratti fedeli: sicurezza/materiali, gestione incidente, posizionamento mezzi, veicoli a gas (valori PRV/PRD), maxi tamponamenti
- `airbag-rischi.md` â airbag (generatori di gas, dual-stage), piantone/cinture/pretensionatori, analisi rischi e DPI, tipi di varco
- `presentazione-struttura.md` â mappa delle 175 slide della presentazione con pagine, alert e note operative per ogni argomento/manovra. **Fonte principale per le schede dell'app.**
- `progetto-app.md` â requisiti e riferimenti file (Drive, PDF)

## Come usare i contenuti

- I testi delle schede vanno redatti a partire dagli estratti + struttura presentazione, mantenendo i **valori tecnici esatti** (pressioni, temperature, tempi, normative) riportati nei file.
- Le avvertenze "ATTENZIONE!" del manuale diventano alert in-app: mai ometterle o attenuarle.
- Citare sempre la pagina del manuale in ogni scheda (campo "Manuale p. XX-XX").
- **Coerenza con il manuale**: i contenuti sono di proprietÃ  CNVVF; uso riservato al progetto istituzionale di Luciano.

## Struttura dati suggerita

```json
{
  "id": "apertura-grande",
  "titolo": "Apertura grande",
  "parte": 7,
  "coloreParte": "#...",
  "paragrafo": "Autoveicolo sulle ruote - Tecniche di accesso dalle portiere",
  "pagineManuale": "154/158",
  "tags": ["accesso", "portiere", "su-ruote", "montante-b"],
  "testo": "...sintesi della manovra...",
  "alert": [{"tipo": "generico", "testo": "..."}],
  "videoUrl": null,
  "passaggi": ["1. ...", "2. ..."]
}
```

## Note operative

- Se manca un contenuto (es. testo dettagliato di una manovra non coperto dagli estratti), non inventarlo: predisporre la scheda con titolo/pagine/tag e testo segnaposto da completare, e segnalarlo a Luciano.
- I link esatti dei video vanno estratti dai QR code del PDF del manuale (indice video pp. 284-291) â Luciano puÃ² fornirli.


---

## FILE 2/6 — `KNOWLEDGE.md`

---
name: incidenti-stradali
description: "Riferimenti e contenuti chiave del Manuale Operativo Incidenti Stradali vers 2.0/2024 del CNVVF: struttura del manuale, concetti e procedure operative (decarcerazione, estricazione, veicoli elettrici/ibridi, maxi tamponamenti)"
display_name: "Incidenti stradali"
icon: "ð"
---

# Incidenti stradali â Manuale Operativo CNVVF vers 2.0/2024

## Fonte

Manuale Operativo "Incidenti stradali", vers 2.0, marzo 2024 â Ministero dell'Interno, Corpo Nazionale dei Vigili del Fuoco (Direzione Centrale per la Formazione e DC Emergenza, Soccorso Tecnico e Antincendio Boschivo). Manuale proprietÃ  del CNVVF; Luciano, VVF incaricato della stesura del nuovo manuale, ne ha caricato la copia: qui conservati indice strutturato, sintesi dei concetti ed estratti estesi fedeli al testo per lavoro redazionale e consultazione personale.

## Struttura del manuale (7 parti)

1. **L'autoveicolo** â concetto di sicurezza (primaria/secondaria/terziaria), materiali automotive (acciai ad alta resistenza, leghe leggere, acciai al boro nascosti nei montanti), elementi costituenti, serbatoi, piantone sterzo, cinture e pretensionatori, airbag, vetrate, climatizzazione; strutture specifiche: autofurgone, veicoli Polizia di Stato, autobus, mezzi pesanti.
2. **Analisi di una collisione** â dinamica degli urti e comportamento della scocca.
3. **Informazioni indispensabili al soccorso** â banca dati MIT, Rescue Sheets (SDS), ERG - Emergency Response Guide, sistema E-call e sistemi di alert (chiamate ricevute dal NUE).
4. **Attrezzature per il soccorso** â stabilizzazione, taglio e divaricazione, attrezzi vari, termocamera, attrezzature specifiche per veicoli ibridi/elettrici.
5. **La gestione dell'incidente** â gestione a livello di Sala Operativa, approccio alla zona, posizionamento mezzi, analisi rischi e DPI, tipi di varco, decarcerazione ed estricazione.
6. **Tecniche di intervento** â manovre preliminari, stabilizzazione (vettura su ruote / su fianco / capovolta), gestione chiave e batteria 12V, marking, gestione airbag, cinture e pretensionatori, vetri, tagli di scarico/indebolimento, abbattimento sedili, spostamento cruscotto; aspetti di polizia giudiziaria post crash.
7. **Strategia di intervento** â economia dell'intervento; tecniche di accesso (portiere, posteriore, tetto) per vettura su ruote / fianco / capovolta / urto laterale; cross-ramming (ripristino volumetrico); interventi su autofurgone, mezzi pesanti, autobus, veicoli LPG/LNG/CNG, ibridi ed elettrici, veicoli a idrogeno, macchine operatrici/agricole elettriche, motociclette elettriche; interventi congiunti con elicottero; maxi tamponamenti (incl. gestione in autostrada con nebbia).

Seguito da Glossario e Allegati (indice multimediale delle manovre, moduli ritiro/affidamento veicolo, indice video).

## Concetti chiave

- **Sicurezza terziaria / "golden hour"**: portare a termine tutte le fasi del soccorso (allertamento, arrivo, decarcerazione, estricazione, stabilizzazione sanitaria) entro 1 ora dall'incidente aumenta le probabilitÃ  di sopravvivenza.
- **Scocca moderna**: zone d'urto a deformazione programmata per dissipare energia + cellula centrale indeformabile; acciai ad altissima resistenza nascosti nei montanti rendono il taglio impegnativo.
- **NovitÃ  versione 2.0**: approfondimenti su veicoli elettrici/idrogeno, nuove attrezzature, nuovi DPI, manovre aggiuntive, strategia su ibride/elettriche in fiamme, vetture Polizia di Stato, maxi tamponamenti.
- **Maxi tamponamenti in autostrada**: verifiche incrociate con gestore del tratto, Sala Operativa 118 e Polizia Stradale; solo la Polizia Stradale puÃ² autorizzare l'accesso contromano in autostrada.

## File del topic

- [Presentazione - struttura](presentazione-struttura.md) â mappa delle 175 slide della "Guida al manuale operativo incidenti stradali 2.0" (tutor: Luciano): convenzioni grafiche (QR video, alert, colori per parte), argomenti con pagine di riferimento. Base per l'app di consultazione.
- [Progetto app](progetto-app.md) â requisiti dell'app Android (APK locale) basata sul manuale: argomenti + tag di ricerca, video via QR, splash con copertina, palette colori per sezione come manuale/presentazione; file Drive correlati.
- [Airbag e rischi](airbag-rischi.md) â estratti estesi su airbag (componenti, generatori di gas, dual-stage, tipologie, ROPS), piantone sterzo, cinture e pretensionatori, analisi dei rischi e DPI minimi, tipi di varco. Luciano Ã¨ un VVF incaricato della stesura del nuovo manuale: estrarre testo fedele con valori tecnici esatti.
- [Estratti estesi](estratti-estesi.md) â passaggi piÃ¹ ampi tratti dal testo del manuale (sicurezza/materiali, gestione incidente e posizionamento mezzi, veicoli a gas, maxi tamponamenti), con numeri di pagina. Luciano ha dichiarato di possedere il manuale; contenuti per consultazione personale. Il file Ã¨ estendibile con altre sezioni su richiesta.

## Note per l'uso

**Preferenza di Luciano (durable):** quando risponde a domande su incidenti stradali, decarcerazione/estricazione e procedure operative, non basarsi solo sul manuale CNVVF: integrare sempre anche conoscenze e riferimenti internazionali affidabili, cercandoli sul web quando serve (fonti istituzionali e di settore, es. ISO 17840 / rescue sheets europei, NFPA, CTIF, WRO, documentazione di corpi e associazioni di soccorso tecnico internazionali (es. IAFC, AFAC), DG ECHO, produttori di attrezzature con white paper tecnici). Citare le fonti e, dove rilevante, segnalare divergenze tra il manuale CNVVF e le pratiche internazionali â utile a Luciano per la stesura del nuovo manuale.

Quando Luciano chiede della procedura per un intervento stradale, attingere a questo riassunto per l'inquadramento; per dettagli puntuali (manovra specifica, pagina del manuale) chiedere di ricaricare il PDF "MANUALE Operativo Incidenti Stradali vers 2_0 edizione 2024" nella conversazione, perchÃ© il testo completo OCR (300 pagine) non Ã¨ conservato permanentemente.


---

## FILE 3/6 — `estratti-estesi.md`

# Incidenti stradali â Estratti estesi dal Manuale Operativo CNVVF vers 2.0/2024

Contenuti tratti dal manuale caricato da Luciano (che ne dichiara il possesso/titolaritÃ  d'uso), conservati per consultazione personale. Riferimento pagine secondo la numerazione del PDF.

## Parte 1 â L'autoveicolo: sicurezza e materiali (pp. 2-4)

**Sicurezza primaria (prima dell'incidente):** sicurezza fornita da sorveglianza continua in fase di guida: ABS, sistemi antipattinamento, correzione dello sbandamento, mantenimento della corsia, dispositivi legati alla guida autonoma (frenata assistita radar a protezione pedoni). Supporto attivo che entra in funzione senza intervento manuale e torna quiescente appena cessato il pericolo.

**Sicurezza secondaria (nell'incidente):** si attiva durante l'incidente, rimane quiescente per quasi tutta la vita del veicolo, una volta attivata Ã¨ irreversibile e di uso unico: airbag, cinture con pretensionatori (esclusi quelli elettrici), barre di rinforzo, strutture in acciaio a deformazione programmata, vetri di sicurezza.

**Sicurezza terziaria (post crash):** vede protagonisti VVF e sanitari dal momento dell'urto all'estricazione delle vittime (la "golden hour" anglosassone): portare a termine tutte le fasi del soccorso in 1 ora â dall'incidente all'allertamento, arrivo sul posto, decarcerazione, estricazione e stabilizzazione sanitaria â dÃ  buone probabilitÃ  di sopravvivenza.

**Materiali â acciai:** gli acciai nelle zone d'urto lavorano in deformazione plastica permanente per dissipare la massima energia. Obiettivo: zona anteriore che assorbe forti deformazioni + "cellula resistente" centrale indeformabile. La tenacitÃ  (area sottesa al diagramma tensioni-deformazioni) di un acciaio ad elevata deformabilitÃ  Ã¨ enormemente maggiore di quella di un acciaio ad alta resistenza. Metalli ad altissima resistenza (acciai al boro, a tempra pressata, a doppia fase, acciaio martensite) sono collocati dentro le aree vuote dei componenti strutturali (montanti, trasversi del tetto): il rinforzo interno Ã¨ inscatolato da strati esterni in acciaio dolce â insidia non visibile che rende impegnativo il taglio.

## Parte 5 â Gestione dell'incidente (pp. 81-83)

**Fase 4 â Antincendio e logistica:** collocazione immediata degli estintori a supporto rapido della squadra di estricazione; stendimento di linea antincendio e altri sistemi di protezione (ventilatori ecc.) se indicato dal ROS. Importante la logistica, in primis l'illuminazione del luogo quando lo scenario Ã¨ poco illuminato.

**Fase 5 â Decarcerazione ed estricazione:** pianificate e decise dal ROS dopo confronto con il personale sanitario del 118; l'accesso tecnico dei VV.F. non sempre corrisponde all'accesso sanitario pensato dagli operatori sanitari.

**Posizionamento dei mezzi â principi generali:**
- Mezzi (APS, AG) disposti a protezione degli operatori su scenario ancora dinamico (sopraggiungono altri veicoli da entrambe le corsie);
- Distanza dai veicoli incidentati tale da creare area di lavoro agevole per attrezzature e arrivo di altri mezzi (autogru, carri faro) con turn-over;
- In caso di incendio l'APS non deve sorpassare il mezzo in fiamme;
- In presenza di possibili dispersioni di gas: distanza minima non inferiore a **m 50**, area preventivamente monitorata con esplosimetro per escludere miscele esplosive.

**ATTENZIONE (testo originale):** quando Ã¨ accertato o anche solo ipotizzabile il coinvolgimento di autoveicoli a gas, prima di avvicinare i mezzi occorre una valutazione strumentale mirata a escludere perdite, ripetendo periodicamente il monitoraggio dell'area.

**Area operativa â tre zone:** zona di ammassamento mezzi (rapido dispiegamento e ripartenza), zona di lavoro (entro ~2 m di raggio dai veicoli non depositare attrezzatura che intralcia), zona di sicurezza. Divieto di accesso a mezzi e personale non strettamente connesso al soccorso.

**Sicurezza nell'area:** uscire dal veicolo dal lato opposto al traffico; pettorina alta visibilitÃ  classe 3 ISO EN 20471 per chi interdice il traffico; delimitazione anticipata prima di curve o dossi; APS eventualmente parcheggiato a 30Â° per aumentare l'area di protezione; lampeggianti accesi su tutti i mezzi.

## Parte 7 â Veicoli alimentati a gas: valvole e dispositivi (pp. 234-235)

- **Valvola di eccesso flusso:** blocca il flusso del gas in caso di rottura della tubazione; posta dentro la MV (multivalvola), non visibile dall'esterno.
- **Valvola manuale di chiusura:** isola il prodotto nel serbatoio/bombola. Su impianti CNG (ECE-ONU R110) la manopola puÃ² essere sostituita da vite esagonale o brugola; su impianti LPG (ECE-ONU 67/01) non Ã¨ sempre presente.
- **Valvola termofusibile PRD:** tappo in lega basso-fondente che scarica il gas a **120-130 Â°C** evitando lo scoppio; una volta aperta lascia fuoriuscire tutto il gas.
- **Valvola di sovrapressione PRV:** scarica il gas al raggiungimento del valore di taratura e si ripristina al rientro (soglie: **LPG 27 bar, CNG 280 bar, LNG 16 e 24 bar**).
- **Dispositivo galleggiante di massimo riempimento:** solo su LPG, riempimento massimo all'80% della capacitÃ .
- **Sensore di pressione (bus CNG):** allarme se la pressione supera 250 bar in ricarica; impedisce l'avviamento sotto i 40 bar.
- **Manometro serbatoio LNG:** valuta la pressione interna e prevede in anticipo l'attivazione delle PRV.

**Scenari incidentali per ogni gas:** A) incidente senza perdita di gas; B) incidente con perdita di gas; C) incendio dell'autoveicolo a gas.

## Parte 7 â Maxi tamponamenti (p. 277)

In caso di maxi tamponamento (es. in autostrada con nebbia), la Sala Operativa combina tutte le informazioni disponibili (chiamanti, sistemi e-call, immagini videocamere) per il quadro piÃ¹ completo: autostrada, direzione, esatta chilometrica, quantitÃ  di veicoli, numero vittime, presenza di sostanze e merci pericolose. Poi: 1) verifica col gestore del tratto la corretta localizzazione (possibile accesso a immagini di videosorveglianza); 2) verifica con la Sala Operativa 118 i mezzi attivati (ambulanze, auto mediche, elisoccorso); 3) verifica con la Polizia Stradale il miglior accesso, valutando la chiusura di entrambe le carreggiate; 4) identifica il tragitto piÃ¹ breve in funzione dell'orario (esodo/controesodo). **Esclusivamente la Polizia Stradale Ã¨ autorizzata a consentire l'accesso contromano in autostrada.**

---

*Nota: file estendibile. Altre sezioni (es. airbag pp. 18-23, decarcerazione/estricazione pp. 91-98, veicoli elettrici/ibridi pp. 243-266, idrogeno pp. 261-266, tecniche di accesso Parte 7) possono essere aggiunte con estratti estesi su richiesta, ricaricando il PDF in conversazione.*


---

## FILE 4/6 — `airbag-rischi.md`

# Airbag e rischi dell'intervento â Estratti dal Manuale Operativo CNVVF vers 2.0/2024

Estratti fedeli tratti dal manuale caricato da Luciano (VVF, incaricato della stesura del nuovo manuale), per consultazione e lavoro redazionale.

## Airbag (pp. 18-23)

### Elementi costitutivi
- **Sensori di collisione (o accelerazione):** montati nella centralina o come satelliti nella parte frontale/laterale del veicolo; rilevano la decelerazione da impatto e trasmettono dati alla centralina. Le centraline di ultima generazione integrano il **sensore di safing**, che evita attivazioni involontarie degli airbag.
- **Centralina elettronica:** riceve ed elabora il segnale dai sensori; posto solitamente nella zona del cruscotto sul tunnel centrale; al riconoscimento dell'incidente surriscalda un filamento che innesca la carica del detonatore, attivando il gonfiaggio del sacco.
- **Sistema di attivazione dell'espansione del gas** (detonatore o capsule a gas).
- **Sacco airbag:** cuscino in tessuto sintetico cosparso di talco (fuoriesce come nuvola bianca all'attivazione); nastri di trattenimento mantengono la forma; fori di efflusso posteriori per la fuoriuscita del gas.

### Tipologie di generatore di gas
- **A. Propellente solido (pirotecnico):** la corrente innesca la carica del detonatore â reazione di azoturo di sodio e nitrato di potassio â enorme quantitÃ  di azoto gassoso che gonfia il modulo a **oltre 300 km/h in circa 30 millisecondi**. Generalmente su airbag frontali. Tra capsula esplosiva e sacco Ã¨ presente una **rete metallica a maglie fini** che impedisce l'accesso al sacco alle particelle solide e raffredda i gas (assente negli airbag a gas, dove il gas inerte Ã¨ piÃ¹ freddo dei fumi dell'esplosione).
- **B. Propellente ibrido:** combinazione di argon, elio o azoto. Usato su airbag frontale passeggero in alternativa al precedente e sui laterali (window bag, sidebag). La pastiglia innescata genera fuoriuscita di gas inerto compresso a **200-600 bar** da una cartuccia; aumenta la velocitÃ  di gonfiaggio.
- **C. Pirotecnico combinato:** sfrutta le tecnologie precedenti; piÃ¹ lento, abbinato a un generatore ibrido o inserito nei pretensionatori. Cartuccia con ulteriori pastiglie di azoturo di sodio, filtro metallico e due ossidanti (nitrato di potassio e diossido di silicio).

### Doppio stadio ("Dual-stage")
Due detonatori di gonfiaggio: a seconda della gravitÃ  dell'impatto la centralina attiva un solo detonatore o entrambi. **Il primo generatore riempie il sacco al 70%, il secondo per il restante 30%.** Con impatto a bassa energia o occupante leggero (es. bambino) si attiva solo la carica di potenza inferiore.
> **ATTENZIONE (testo originale):** un airbag a doppio stadio non Ã¨ mai neutralizzato completamente e puÃ² riattivarsi; dall'esterno Ã¨ impossibile riconoscere se si Ã¨ attivato uno o entrambi i detonatori. **L'unico sistema per gestirlo in sicurezza Ã¨ collocare sopra al punto di uscita del sacco la protezione di sicurezza copri airbag, anche quando l'airbag appare giÃ  sviluppato.**

### Funzionamento operativo
L'airbag lavora in sincronia con i pretensionatori: il gonfiaggio avviene mentre testa e torace si spostano in avanti; il contatto col sacco consente una decelerazione piÃ¹ dolce; lo sgonfiamento immediato attraverso il foro sottostante attutisce l'impatto contro sterzo/cruscotto senza rischio di soffocamento.

### Tipologie di airbag
Frontali; laterali (sul fianco sedile o portiere); a tendina frontale (scende dal soffitto prospiciente al parabrezza); tetto (antiurto col soffitto, per tettucci apribili); a tendina laterale (coprono le superfici vetrate della fiancata; **su alcune volanti della Polizia di Stato potrebbero non essere presenti** per via dello schermo in plexiglass tra zona anteriore e posteriore); per le ginocchia (parte bassa del cruscotto sotto lo sterzo); centrale a tendina (protegge il conducente da urti col passeggero anteriore, utile in urti laterali e ribaltamenti); frontali per sedili posteriori (integrati negli schienali anteriori); airbag cinture; per tamponamento (poggiatesta o cappelliera); antisprofondamento (sulla seduta); combinato airbag pedone + sollevamento cofano (frenata automatica con radar/sensori a infrarossi, airbag tra cofano e parabrezza, sollevamento di alcuni cm del cofano per dare spazio di deformazione); airbag per incidenti multipli (in fase di studio; dopo un primo impatto senza attivazione, la centralina **ricalibra il sistema** abbassando il valore di decelerazione di soglia e verificando la posizione dei passeggeri); airbag esterno (radar, si gonfia prima dell'urto laterale).

- Pittogrammi sui vetri comunicano solo presenza e tipologia, **non posizione nÃ© quantitÃ **: resta fondamentale la consultazione delle schede di soccorso.
- **ROPS**: struttura di protezione metallica per cabrio ad attivazione pirotecnica in caso di ribaltamento. (p. 23)

## Piantone dello sterzo e cinture (estratti collegati, pp. 13-15)

- Piantoni alleggeriti/talvolta cavi con servosterzo elettrico; sterzi collassabili; verificare **sempre tramite la scheda di soccorso** la possibilitÃ  di tagliare il piantone sotto il volante.
- Cinture: obbligatorie dal 1988 (posti anteriori), 2006 (ogni posto occupato), 2010 (anche autobus non urbani e camion). Tipologie: tre punti, due punti subaddominale, diagonali, a bretella. Elementi: cinghie, riavvolgitore, regolatore, ancoraggi (bulloni Î¦ 17 o 19 mm â **attenzione: difficoltÃ  concreta nelle fasi di cesoiamento/schiacciamento, rischio reale di danno alle attrezzature**), anello oscillante, fibbia.
- **Pretensionatori** (meccanico a molla; pirotecnico con carica esplosiva nell'arrotolatore alla base del montante B, attivato dalla centralina airbag o dal sensore di ribaltamento, **soglia di attivazione piÃ¹ bassa dell'airbag** â puÃ² attivarsi anche in piccoli urti; a gas con cartuccia pressurizzata vicino alla fibbia). Una volta azionato il modulo va sostituito completamente (eccetto quello elettrico).

## Analisi dei rischi e DPI (pp. 88-89)

- **Rischio rumore:** centralina idraulica a scoppio fino a **90 dB** in prossimitÃ  del motore.
- **Sostanze tossiche e nocive:** COâ e CO dai motori a scoppio (rilevanti in galleria/ambienti chiusi; valutare posizione e vento anche in area aperta); fluidi idraulici, refrigeranti dei condizionatori e liquidi dei veicoli incidentati **corrosivi**.
- **Perdita idraulica in pressione:** olio a **700 bar** nel kit idraulico; rottura o taglio accidentale della tubazione puÃ² ferire gravemente l'operatore.
- **Esplosione:** motori a scoppio sulla scena (gruppi elettrogeni, centraline idrauliche) potenziale fonte di innesco per vapori di benzina o gas da perdite. Cesoie e divaricatori riducono le scintille, ma lo scintillio puÃ² generarsi dal movimento reciproco delle lamiere tagliate/piegate. Particolare attenzione con veicoli a gas o benzina visibile/avvertibile all'olfatto sul manto.
- **Taglio e schiacciamento:** parti taglienti, proiezione di pezzi che si staccano, schiacciamento per brusco cedimento durante sollevamento/spostamento.
- **Polveri:** taglio di vetri stratificati o parti in fibra (kevlar, carbonio) produce polveri estremamente pericolose inalabili da soccorritori e vittime â **mascherine FFP2** nelle fasi di taglio.
- **Biologico:** ferite esposte, liquidi biologici, persone infette â rischi contaminazione aerea, ematica, enterica. Buona pratica: guanti in lattice sotto i guanti da intervento, abbinati alla mascherina.
- **Scarsa visibilitÃ :** il personale VVF Ã¨ "personale esposto al traffico di veicoli nello svolgimento dell'attivitÃ  lavorativa". Chi segnala ai conducenti in arrivo (fino all'arrivo di Polizia locale/FF.OO./gestore) Ã¨ maggiormente esposto a investimento â **indumento alta visibilitÃ  classe 3 norma EN 20471** (gilet sopra la divisa). Il restante personale: completo antifiamma con bande fluorescenti (classe 1 di visibilitÃ  sufficiente, essendo la presenza giÃ  segnalata).

### DPI irrinunciabili (tutte le fasi di soccorso)
elmo EOM o EVO Â· occhiali protettivi o visiera di protezione Â· completo antifiamma Â· guanti antitaglio Â· calzature di sicurezza. Scelta dell'elmo secondo nota DCSTAB **n. 26045 del 18/08/2022**.

## Tipi di varco (p. 90)

- **Varco comunicativo:** per raggiungere/valutare il ferito (rottura finestrino, foro su parabrezza/lunotto, taglio minimo di carrozzeria); rapido, diretto, sempre salvaguardando la vittima.
- **Varco sanitario:** apertura per il raggiungimento del ferito da parte dei sanitari; consente valutazione dello stato di salute, somministrazione farmaci urgenti e definizione della strategia di decarcerazione. PuÃ² coincidere col varco di ispezione e di estricazione.
- **Varco di ispezione:** apertura (anche accidentale) per l'ingresso di un VVF e ispezione tecnico/sanitaria; quando possibile prima stabilizzazione del rachide cervicale; sempre dopo adeguata stabilizzazione.
- **Varco di estricazione:** varco fondamentale per l'uscita dei feriti; decisione del ROS sul percorso di decarcerazione ed estricazione piÃ¹ rapido ed efficace, anche dal punto di vista sanitario.
- La strategia di decarcerazione ed estricazione Ã¨ **totalmente a carico del ROS**, che rispetta il triage sanitario gestendo perÃ² autonomamente le scelte operative.
- **ATTENZIONE (testo originale):** rimuovendo/aprendo il portellone posteriore e abbattendo i sedili posteriori si ottiene un varco molto ampio idoneo all'uscita dei feriti; rispetta la **regola dell'angolo zero** (allineamento testa/collo/tronco) ed Ã¨ pertanto la via piÃ¹ idonea per l'estricazione.


---

## FILE 5/6 — `presentazione-struttura.md`

# Guida al manuale operativo incidenti stradali 2.0 â Presentazione (struttura)

Fonte: "Presentazione manuale incidenti stradali 2.0" (PDF ~19 MB, Google Drive, cartella "Formazione incidenti stradali", file_id `12VYZ9ZoeADYWNbZN3sKusdxjHCM5-1G5`) â autore/tutor: VE Di Lorenzo Luciano. 175 slide che sintetizzano il manuale vers 2.0/2024.

## Convenzioni grafiche delle slide (rilevanti per l'app)

- **Colore e numero del capitolo** + titolo capitolo/paragrafo di riferimento + pagine del manuale in calce (es. "148/150")
- **QR Code** che, inquadrato, rimanda a un breve filmato esplicativo della manovra
- **Disegno indicativo della manovra** (immagine interattiva per accedere alla descrizione)
- **ALERT** con simboli distinti: rischio generico e rischio da elettrocuzione
- Testo "tutor: VE Di Lorenzo Luciano" a piÃ¨ di slide
- Manuale 2.0 disponibile a: https://www.vigilfuoco.it/allegati/biblioteca/MANUALE_Operativo_ver2_0del2024.pdf

## Struttura (175 slide)

- **1-3**: copertina, obiettivi della guida, struttura del manuale (7 parti + allegati)
- **4**: legenda elementi delle schede manovra
- **5-18 â PARTE 1 L'autoveicolo**: sicurezza primaria/secondaria/terziaria; vocabolario (scocca, telaio, pianale, carrozzeria); materiali (ATTENZIONE: fibre di carbonio conduttive â elettrocuzione; batteria HV possibile nel tunnel centrale); piantoni alleggeriti/cavi e sterzi collassabili; cinture (ancoraggi Î¦17/19 â rischio per attrezzature); airbag; superfici vetrate (parabrezza stratificato, lunotto temperato come punto d'ingresso preferito); impianto condizionamento (freon R-134A/R-134YF â ustioni da freddo, acido fluoridrico e fluorofosgene se innescato); specchietti intelligenti; autofurgoni/autobus/trattori (ATTENZIONE: abbassamento involontario sedile trattore â cunei in legno)
- **19-25 â PARTE 2 Analisi collisione**: urto frontale (50-55% dei sinistri; fasi compressione/restituzione), laterale (contatto alto vs basso), posteriore (ATTENZIONE: danni serbatoio/bombole), ribaltamento; peculiaritÃ  ibride/elettriche (pacco batteria HV 170-600 kg, baricentro piÃ¹ basso)
- **26-32 â PARTE 3 Informazioni al soccorso**: banca dati MIT (da targa â alimentazione, modello, rescue sheet); SDS/rescue sheet (ISO 17840-1 dal 2022 obbligo produttori; ATTENZIONE: rimozione tappezzeria indispensabile per valutazioni); ERG ed E-call (chiamata automatica NUE 112)
- **33-50 â PARTE 4 Attrezzature**: stabilizzazione (cunei, puntelli, cinghie cricchetto); divaricatore (17-25 kg, apertura 60-82 cm, sommergibilitÃ  su taluni modelli); ATTENZIONE torsioni cesoia ("sforbiciata" e rotazione su montanti B rinforzati); pistoni (30/60-99/181 cm, 15-22 kg, 2-3 stadi); basi per cilindri/crossramming; gruppi combinati; attrezzi a batteria (pregi: pronti subito, nessun limite distanza); tagliapedali; attrezzi vari (tagliacinture, seghe vetro stratificato, Halligan ~8 kg); accessori protezione (teli antitaglio, copri airbag); teli antifiamma/contenitivi per quarantena EV; termocamera, multigas, spina di emergenza
- **51-62 â PARTE 5 Gestione incidente**: sala operativa (istruzioni salvavita, pre-arrivo, intervista COSA-DOVE-CHI); approccio zona; posizionamento mezzi (50 m + esplosimetro se gas); tipi di varco; decarcerazione vs estricazione; traumi frequenti (frontale, tamponamento, laterale, ribaltamento)
- **63-88 â PARTE 6 Tecniche di intervento**: manovre preliminari (sblocco portiere, spegnimento, chiave, batteria 12V, service plug); stabilizzazione (su ruote: ATTENZIONE calzaruota â¥10 cm per ibride/elettriche; su fianco: verifica costante stabilizzatori; capovolta: ATTENZIONE vietata variazione stabilizzazione dopo recisione montanti; ibride/EV: teste stabilizzatori mai su batteria/cablaggi AT, baricentro diverso); gestione chiave (di prossimitÃ : da allontanare); batteria 12V (verificarne numero via SDS); bonifica parti interne; marking (pennarelli a cera); airbag (identificare, disalimentare; ATTENZIONE cartucce gas compresso: esplodono se schiacciate/recise â danni elmo documentati); cinture (prediligere sgancio, memorizzare chi era cinturato); vetri (temperati: griglia nastro adesivo, colpire angolo basso, manovra annunciata a voce; stratificati: punte widia, gattuccio, tagli lineare/obliquo/a U); tagli di indebolimento/scarico; abbattimento sedili; spostamento/rotazione cruscotto (rotazione con pistone su base montante B, sollevamento con divaricatore)
- **89-134 â PARTE 7 Strategie (vetture)**: economia dell'intervento, "effetto tunnel"; accesso portiere (schiacciamento/torsione cornice; IN/OUT; pinzatura â sempre provare la maniglia prima); apertura grande (5+1 passi, video didattico; non possibile con porte scorrevoli); apertura grande ribaltata (per EV con batteria sul pianale); accesso fianco laterale posteriore; portellone (manovra A gruppo ottico/divaricatore, manovra B rimozione lunotto+taglio cornice; ATTENZIONE portelloni in materiale composito); berlina 3 volumi (accesso dal bagagliaio, ATTENZIONE serbatoi gas fissi); tetto (A rotazione anteriore, B scrigno, C rimozione totale, D rotazione posteriore â non allinea testa-collo-tronco); su fianco (5 manovre A-E, incl. semiabbattimento laterale per cinturati sospesi e taglio del tetto in caso di ostacolo); capovolto (A portiera, B1/B2 portiereÂ±montante B, C terzo varco su 3 porte, portellone); urto laterale e cross ramming (ripristino volumetrico, punti di spinta: montanti, basi montanti, angolo montante B); autofurgoni (stabilizzazione multipunto, batterie supplementari, porte composite, gattuccio su fiancate)
- **135-153 â Mezzi pesanti e gas**: mezzi pesanti (stab. cabina articolata, lavoro in quota, ribaltamento cruscotto con 2 pistoni); autobus (struttura vulnerabile, pulsanti emergenza meccanici, boccaporti, ATTENZIONE controllo bagagliere/vani letto/toilette); gas (GPL/GNC/GNL/H2; rischi dardi lunghi, esplosione serbatoi, ustioni, asfissia, fiamme invisibili H2; multivalvole ECE/ONU R110 e R67/01; 3 scenari A/B/C; distanza 50 m; acqua nebulizzata/elettroventilatori Atex; incendio: 2 linee da 250 lt/min, mai superare il mezzo incendiato â PRV/PRD aprono senza preavviso; LNG -130Â°C, 8-10 bar, NON odorizzato)
- **154-167 â Ibride/EV/idrogeno**: tipologie in circolazione; identificazione (pittogrammi, cavi arancioni, portale MIT da targa); elementi impianto; supercondensatore (fino a 1200 Farad, sotto passaruota anteriore lato guida, Start&Stop); service plug (rimozione con guanti dielettrici); sezionatori bassa tensione; thermal runaway (cause: urti/perforazione, irraggiamento, problemi ricarica); 3 operazioni fondamentali (spegnere quadro, service plug, polo negativo 12V â ordine da rispettare); incendio EV (autosostenuto, riattivazioni ripetute, quantitativi d'acqua ingenti); sommersione (nessun rischio elettrocuzione, disattivare AT solo dopo estrazione dall'acqua); idrogeno (infiammabilitÃ  4-74,5%, fiamma azzurrognola/trasparente 1430Â°C); macchine operatrici/agricole e moto elettriche (procedure analoghe alle autovetture)
- **168-174 â Elicottero e maxi tamponamenti**: elicottero (area flusso rotore a rischio, zona atterraggio lontana da cavi, decisione finale del pilota); maxi tamponamenti (triage tecnico vs sanitario, zonizzazione, chiavi lasciate dagli illesi, turn-over mezzi, verifiche SO con gestore/118/Polizia Stradale)
- **175**: chiusura

## Uso per il progetto app

Questa struttura Ã¨ la mappa argomenti/tag per l'app di Luciano basata sul manuale: ogni slide-manovra ha giÃ  capitolo, paragrafo, pagine di riferimento, alert (generico/elettrocuzione) e rimando video (QR). Per i link video esatti servono i QR code del PDF originale o l'indice dei video del manuale (pp. 284-291).


---

## FILE 6/6 — `progetto-app.md`

# Progetto: app di consultazione del Manuale Incidenti Stradali

## Requisiti (dichiarati da Luciano, ott 2026)

- **Formato**: app Android in **APK**, da aprire in locale sul telefono (non un Canvas in chat). Luciano usa "Code" per questo.
- **Contenuto**: basata sul Manuale Operativo Incidenti Stradali 2.0/2024, divisa per **argomenti e tag di ricerca**, con richiamo ai **video allegati** (QR code con link del manuale) per consultazione veloce dell'argomento o della manovra.
- **Stile/palette**: stessa palette colori del manuale e della presentazione. **Splash screen**: all'avvio, durante il caricamento, pagina con la copertina del manuale.
- **Sezioni**: le diverse sezioni dell'app devono avere gli stessi colori che dividono le sezioni nel manuale e nella presentazione (ogni PARTE ha il suo colore).

## Struttura dati disponibile

- Mappa argomenti: file "presentazione-struttura.md" (175 slide, con capitolo, pagine, alert per ogni manovra)
- Estratti contenutistici: "estratti-estesi.md" e "airbag-rischi.md"
- Per i link video: servono i QR code dal PDF del manuale (indice dei video pp. 284-291) o dalla presentazione originale (file Drive `12VYZ9ZoeADYWNbZN3sKusdxjHCM5-1G5`)
- Manuale 2.0 pubblico: https://www.vigilfuoco.it/allegati/biblioteca/MANUALE_Operativo_ver2_0del2024.pdf

## Fonti correlate nel Drive (cartella "Formazione incidenti stradali")

- Presentazione manuale 2.0 (PDF, 19 MB) â file_id `12VYZ9ZoeADYWNbZN3sKusdxjHCM5-1G5`
- MANUALE Operativo vers 2.0 consegna (PDF) â file_id `1QwekXwtmhuSje8GezmPCE-W2hIV_YSzx`
- QR Code link presentazione (JPG) â file_id `1yIGnm0Rfso7WqGGcAlTbTevBTjOSIL-G`