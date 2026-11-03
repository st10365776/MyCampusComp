package com.example.mycampuscomp

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

data class LanguageItem(
    val tag: String,
    val nativeName: String,
    val englishName: String
)

object LanguageManager {
    val languages = listOf(
        LanguageItem("en", "English", "English"),
        LanguageItem("af", "Afrikaans", "Afrikaans"),
        LanguageItem("zu", "isiZulu", "Zulu"),
        LanguageItem("xh", "isiXhosa", "Xhosa"),
        LanguageItem("st", "Sesotho", "Sotho"),
        LanguageItem("tn", "Setswana", "Tswana"),
        LanguageItem("nso", "Sepedi", "Northern Sotho"),
        LanguageItem("ss", "siSwati", "Swati"),
        LanguageItem("ts", "Xitsonga", "Tsonga"),
        LanguageItem("ve", "Tshivenda", "Venda"),
        LanguageItem("nr", "isiNdebele", "Ndebele")
    )

    fun currentTag(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (!locales.isEmpty) locales.get(0)?.language ?: "" else ""
    }

    fun displayName(tag: String, defaultName: String): String {
        if (tag.isEmpty()) return defaultName
        val item = languages.find { it.tag == tag }
        return if (item != null) "${item.nativeName} (${item.englishName})" else defaultName
    }

    fun apply(tag: String) {
        val localeList = if (tag.isEmpty()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(tag)
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}
