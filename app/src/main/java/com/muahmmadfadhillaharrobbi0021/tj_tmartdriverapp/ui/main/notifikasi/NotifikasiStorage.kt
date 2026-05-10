package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.notifikasi

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * NotifikasiStorage — menyimpan dan memuat daftar notifikasi ke SharedPreferences
 * agar tidak hilang ketika aplikasi ditutup.
 */
object NotifikasiStorage {

    private const val PREF_NAME = "notifikasi_pref"
    private const val KEY_LIST = "notifikasi_list"

    fun save(context: Context, list: List<NotifikasiItem>) {
        val jsonArray = JSONArray()
        list.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("pesananId", item.pesananId ?: -1)
                put("title", item.title)
                put("message", item.message)
                put("time", item.time)
                put("isRead", item.isRead)
                put("type", item.type.name)
                put("timestamp", item.timestamp)
            }
            jsonArray.put(obj)
        }

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LIST, jsonArray.toString())
            .apply()
    }

    fun load(context: Context): MutableList<NotifikasiItem> {
        val json = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LIST, null) ?: return mutableListOf()

        val result = mutableListOf<NotifikasiItem>()
        try {
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                result.add(
                    NotifikasiItem(
                        id = obj.getInt("id"),
                        pesananId = obj.getInt("pesananId").takeIf { it != -1 },
                        title = obj.getString("title"),
                        message = obj.getString("message"),
                        time = obj.getString("time"),
                        isRead = obj.getBoolean("isRead"),
                        type = NotifType.valueOf(obj.getString("type")),
                        timestamp = if (obj.has("timestamp")) obj.getLong("timestamp") else 0L
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().remove(KEY_LIST).apply()
    }
}