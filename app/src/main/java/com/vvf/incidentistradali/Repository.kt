package com.vvf.incidentistradali

import org.json.JSONObject
import org.json.JSONArray

data class Alert(val tipo: String, val testo: String)

data class Scheda(
    val id: String,
    val slide: Int,
    val titolo: String,
    val parte: Int,
    val paragrafo: String,
    val pagineManuale: String,
    val testo: String,
    val alert: List<Alert>,
    val tags: List<String>,
    val videoUrl: String?
)

data class Parte(val numero: Int, val titolo: String, val coloreHex: String)

object Repository {
    val parti: List<Parte>
    val schede: List<Scheda>
    val tuttiTag: List<String>

    init {
        val ctx = App.instance
        val partsJson = JSONObject(ctx.assets.open("data/content.json").bufferedReader().use { it.readText() })
        val partiMap = partsJson.getJSONObject("parti")
        parti = (1..7).map { n ->
            val p = partiMap.getJSONObject(n.toString())
            Parte(n, p.getString("titolo"), p.getString("colore"))
        }
        val arr = partsJson.getJSONArray("schede")
        val list = mutableListOf<Scheda>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val alerts = mutableListOf<Alert>()
            val ja = o.optJSONArray("alert") ?: JSONArray()
            for (j in 0 until ja.length()) {
                val a = ja.getJSONObject(j)
                alerts.add(Alert(a.getString("tipo"), a.getString("testo")))
            }
            val jt = o.optJSONArray("tags") ?: JSONArray()
            val tags = (0 until jt.length()).map { jt.getString(it) }
            list.add(
                Scheda(
                    id = o.getString("id"),
                    slide = o.getInt("slide"),
                    titolo = o.getString("titolo"),
                    parte = o.getInt("parte"),
                    paragrafo = o.optString("paragrafo", ""),
                    pagineManuale = o.optString("pagineManuale", ""),
                    testo = o.optString("testo", ""),
                    alert = alerts,
                    tags = tags,
                    videoUrl = if (o.isNull("videoUrl") || o.optString("videoUrl").isBlank()) null else o.getString("videoUrl")
                )
            )
        }
        schede = list
        tuttiTag = list.flatMap { it.tags }.distinct().sorted()
    }

    fun schedeDiParte(p: Int) = schede.filter { it.parte == p }

    fun coloreParte(p: Int) = parti.first { it.numero == p }.coloreHex

    fun search(q: String): List<Scheda> {
        val query = q.trim().lowercase()
        if (query.length < 2) return emptyList()
        return schede.filter { s ->
            s.titolo.lowercase().contains(query) ||
            s.paragrafo.lowercase().contains(query) ||
            s.testo.lowercase().contains(query) ||
            s.tags.any { it.lowercase().contains(query) }
        }
    }

    fun searchByTag(tag: String): List<Scheda> =
        schede.filter { it.tags.contains(tag) }
}
