package com.vvf.incidentistradali

import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File

object ManualeRepository {
    data class Risultato(val pagStampata: Int, val snippet: String, val parte: Int, val titoloParte: String)
    data class ParteManuale(val n: Int, val titolo: String, val pagStampata: Int, val coloreHex: String)

    val parti: List<ParteManuale>
    val pagine: List<String>
    val offsetPdf: Int
    val pdfNome: String

    init {
        val ctx = checkNotNull(App.instance)
        val txt = ctx.assets.open("data/manual_index.json").bufferedReader().use { it.readText() }
        val o = JSONObject(txt)
        offsetPdf = o.optInt("offsetPdf", 10)
        pdfNome = "manuale.pdf"
        val jp = o.optJSONArray("parti") ?: org.json.JSONArray()
        parti = (0 until jp.length()).map { i ->
            val p = jp.getJSONObject(i)
            val n = p.getInt("n")
            ParteManuale(n, p.getString("titolo"), p.getInt("pagStampata"), Repository.coloreParte(if (n in 1..7) n else 1))
        }
        val ja = o.optJSONArray("pagine") ?: org.json.JSONArray()
        pagine = (0 until ja.length()).map { ja.getString(it) }
    }

    private val rangesParti = listOf(
        1 to 1..34, 2 to 35..40, 3 to 41..52, 4 to 53..72, 5 to 73..98, 6 to 99..134, 7 to 135..269
    )

    fun parteDellaPagina(p: Int): Pair<Int, String> {
        val found = rangesParti.firstOrNull { p in it.second }
        return if (found != null) found.first to (parti.firstOrNull { it.n == found.first }?.titolo ?: "")
        else 0 to "Glossario e allegati"
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
                    val (pn, pt) = parteDellaPagina(printed)
                    out.add(Risultato(printed, txt.substring(from, to), pn, pt))
                    if (out.size >= max) break
                }
            }
        }
        return out
    }

    /**
     * Apre il PDF locale (copiandolo in cache se necessario) alla pagina desiderata.
     * Usa FileProvider per condividere il file con il viewer PDF esterno.
     */
    fun apriPdf(ctx: android.content.Context, pagStampata: Int) {
        val cacheFile = File(ctx.cacheDir, pdfNome)
        if (!cacheFile.exists() || cacheFile.length() == 0L) {
            ctx.assets.open("data/$pdfNome").use { input ->
                cacheFile.outputStream().use { output -> input.copyTo(output) }
            }
        }
        val uri: Uri = FileProvider.getUriForFile(ctx, "com.vvf.incidentistradali.fileprovider", cacheFile)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            ctx.startActivity(intent)
        } catch (e: Exception) {
            val fallback = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try { ctx.startActivity(fallback) } catch (e2: Exception) { }
        }
    }
}
