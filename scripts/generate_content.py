#!/usr/bin/env python3
"""Genera i contenuti JSON dell'app dal PDF della presentazione (175 slide)."""
import pymupdf, json, re, sys

pres = pymupdf.open("Presentazione manuale incidenti stradali 2.0")

# Colori ufficiali delle 7 parti estratti dalle slide
PARTI = {
    1: {"titolo": "L'autoveicolo", "colore": "#202060"},
    2: {"titolo": "Analisi di una collisione", "colore": "#00A0C0"},
    3: {"titolo": "Informazioni indispensabili al soccorso", "colore": "#C04080"},
    4: {"titolo": "Attrezzature per il soccorso", "colore": "#60C060"},
    5: {"titolo": "La gestione dell'incidente", "colore": "#00652E"},
    6: {"titolo": "Tecniche d'intervento", "colore": "#E57525"},
    7: {"titolo": "Strategia d'intervento", "colore": "#ED2323"},
}

# Mappa slide -> parte (dagli estremi della presentazione-struttura.md)
def parte_della_slide(n):
    if 5 <= n <= 18: return 1
    if 19 <= n <= 25: return 2
    if 26 <= n <= 32: return 3
    if 33 <= n <= 50: return 4
    if 51 <= n <= 62: return 5
    if 63 <= n <= 88: return 6
    if 89 <= n <= 175: return 7
    return None

def parse_slide(page, num):
    d = page.get_text("dict")
    spans = []
    for b in d["blocks"]:
        if b.get("type") != 0: continue
        for l in b["lines"]:
            for s in l["spans"]:
                t = s["text"].strip()
                if t:
                    spans.append({"text": t, "size": round(s["size"],1), "color": s["color"]})
    return spans

def build_scheda(num):
    page = pres[num-1]
    spans = parse_slide(page, num)
    if not spans: return None
    parte = parte_della_slide(num)
    # skip slide intro/obiettivi/copertina
    if parte is None: return None
    if any(s["size"] >= 55 and "Obiettivo" in s["text"] for s in spans): return None
    # titolo: span grigio grande (#797979) o il più grande dopo l'header
    titolo = None
    paragrafo = None
    corpo = []
    alerts = []
    pagine = None
    passi = []
    in_alert = False
    for s in spans:
        t, sz, col = s["text"], s["size"], s["color"]
        if t.startswith("tutor:"): continue
        if re.fullmatch(r"\d+(\.\d+)?", t) and sz < 25: continue  # numero slide
        if sz == 22 and t == "!": continue
        if col == 0x797979 and sz >= 50 and titolo is None:
            titolo = t; continue
        if col in (0xED2323, 0xE57525, 0x00652E, 0x042EEE, 0x00A0C0, 0xC04080, 0x60C060, 0x202060) and sz in (30.0,) and paragrafo is None and t != "!":
            if not t.startswith("ATTENZIONE") and "d'intervento" not in t and "d\u2019intervento" not in t and len(t) > 3 and col != 0xED2324:
                paragrafo = t; continue
        if t == "ATTENZIONE!" or t == "ATTENZIONE":
            in_alert = True
            alerts.append({"tipo": "generico", "testo": ""})
            continue
        if in_alert and sz >= 35 and col == 0x000000:
            alerts[-1]["testo"] += " " + t
            continue
        if in_alert and col == 0xED2324:
            continue
        if re.fullmatch(r"\d{1,3}/\d{1,3}", t) and sz < 30:
            pagine = t; continue
        if col in (0x0432FF, 0x042EEE) and "Video" in t:
            continue
        if t in ("\u2022", "!"): continue
        if col == 0xFFFFFF and re.fullmatch(r"\d+", t): continue
        if sz >= 35 and col == 0x000000:
            corpo.append(t)
    if titolo is None:
        # fallback: span più grande grigio o nero
        bigs = [s for s in spans if s["size"] >= 45 and s["color"] != 0xFFFFFF]
        if bigs:
            titolo = max(bigs, key=lambda s: s["size"])["text"]
    if titolo is None: return None
    testo = " ".join(corpo).strip()
    for a in alerts:
        a["testo"] = a["testo"].strip()
    return {
        "id": f"slide-{num}",
        "slide": num,
        "titolo": titolo.strip(),
        "parte": parte,
        "paragrafo": (paragrafo or "").strip(),
        "pagineManuale": pagine or "",
        "testo": testo,
        "alert": [a for a in alerts if a["testo"]],
        "passi": passi,
        "tags": auto_tags(titolo, paragrafo or "", testo, parte),
        "videoUrl": LINKS.get(num),
    }

def auto_tags(titolo, paragrafo, testo, parte):
    t = (titolo + " " + paragrafo + " " + testo).lower()
    tags = set()
    if "ibrid" in t or "elettric" in t or " ev " in t: tags.add("ev-ibride")
    if "idrogeno" in t: tags.add("idrogeno")
    if "gas" in t or "gpl" in t or "cng" in t or "lng" in t or "metano" in t: tags.add("gas")
    if "autobus" in t or "bus " in t: tags.add("autobus")
    if "furgon" in t: tags.add("autofurgone")
    if "mezzi pesanti" in t or "camion" in t: tags.add("mezzi-pesanti")
    if "stabilizz" in t: tags.add("stabilizzazione")
    if "tagli" in t or "cesoi" in t: tags.add("taglio")
    if "divaricat" in t or "apertura" in t: tags.add("divaricazione")
    if "portiere" in t or "portiera" in t: tags.add("portiere")
    if "vetri" in t or "parabrezza" in t or "lunotto" in t: tags.add("vetri")
    if "airbag" in t: tags.add("airbag")
    if "cintur" in t: tags.add("cinture")
    if "cruscotto" in t: tags.add("cruscotto")
    if "sedili" in t: tags.add("sedili")
    if "tetto" in t or "padiglione" in t: tags.add("tetto")
    if "montante" in t: tags.add("montanti")
    if "batteria" in t: tags.add("batteria")
    if "service" in t: tags.add("service-plug")
    if "elicotter" in t: tags.add("elicottero")
    if "tamponament" in t: tags.add("maxi-tamponamenti")
    if "sala operativa" in t: tags.add("sala-operativa")
    if "varco" in t: tags.add("varchi")
    if "dpi" in t: tags.add("dpi")
    if "incendio" in t or "fiamm" in t: tags.add("incendio")
    if "idrogeno" in t: tags.add("idrogeno")
    if "sommersion" in t or "acqua" in t: tags.add("sommersione")
    if "capovol" in t: tags.add("capovolto")
    if "fianco" in t and "su fianco" in t: tags.add("su-fianco")
    if "ruote" in t: tags.add("su-ruote")
    return sorted(tags)

# link dai QR
LINKS = {}
for i in range(len(pres)):
    for l in pres[i].get_links():
        uri = l.get("uri")
        if uri and "vigilfuoco.tv" in uri:
            LINKS[i+1] = uri.replace("%0D", "")

schede = []
for n in range(1, len(pres)+1):
    s = build_scheda(n)
    if s and s["titolo"] and len(s["titolo"]) > 2:
        schede.append(s)

out = {"parti": {str(k): v for k, v in PARTI.items()}, "schede": schede}
with open("app/src/main/assets/data/content.json", "w", encoding="utf-8") as f:
    json.dump(out, f, ensure_ascii=False, indent=1)
print(f"Generate {len(schede)} schede")
for s in schede[:8]:
    print(f"  P{s['parte']} slide {s['slide']}: {s['titolo'][:50]} | pag={s['pagineManuale']} | tags={s['tags'][:4]}")
