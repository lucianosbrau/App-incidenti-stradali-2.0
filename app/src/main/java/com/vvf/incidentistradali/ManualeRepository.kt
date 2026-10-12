package com.vvf.incidentistradali

import org.json.JSONObject

object ManualeRepository {
    data class Risultato(val pagStampata: Int, val snippet: String)
    data class ParteManuale(val n: Int, val titolo: String, val pagStampata: Int)

    val parti: List<ParteManuale>
    val pagine: List<String>
    val offsetPdf: Int
    val pdfUrl: String

    init {
        val ctx = checkNotNull(App.instance)
        val txt = ctx.assets.open("data/manual_index.json").bufferedReader().use { it.readText() }
        val o = JSONObject(txt)
        offsetPdf = o.optInt("offsetPdf", 10)
        pdfUrl = o.optString("pdfUrl", "https://www.vigilfuoco.it/allegati/biblioteca/MANUALE_Operativo_ver2_0del2024.pdf")
        val jp = o.optJSONArray("parti") ?: org.json.JSONArray()
        parti = (0 until jp.length()).map { i ->
            val p = jp.getJSONObject(i)
            ParteManuale(p.getInt("n"), p.getString("titolo"), p.getInt("pagStampata"))
        }
        val ja = o.optJSONArray("pagine") ?: org.json.JSONArray()
        pagine = (0 until ja.length()).map { ja.getString(it) }
    }

    fun search(q: String, max: Int = 60): List<Risultato> {
        val query = q.trim().lowercase()
        if (query.length < 3) return emptyList()
        val out = mutableListOf<Risultato>()
        for ((i, txt) in pagine.withIndex()) {
            val idx = txt.lowercase().indexOf(query)
            if (idx >= 0) {
                val printed = i + 1 - offsetPdf
                if (printed >= 1) {
                    val from = maxOf(0, idx - 60)
                    val to = minOf(txt.length, idx + 90)
                    out.add(Risultato(printed, txt.substring(from, to)))
                    if (out.size >= max) break
                }
            }
        }
        return out
    }

    fun pdfUrlFor(pagStampata: Int) = "$pdfUrl#page=${pagStampata + offsetPdf}"
}
