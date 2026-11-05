package com.example.mycampuscomp

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/** The 11 official South African languages. An empty tag means "follow the system". */
object LanguageManager {

    data class Language(val tag: String, val nativeName: String, val englishName: String)

    val languages = listOf(
        Language("en", "English", "English"),
        Language("af", "Afrikaans", "Afrikaans"),
        Language("zu", "isiZulu", "Zulu"),
        Language("xh", "isiXhosa", "Xhosa"),
        Language("st", "Sesotho", "Southern Sotho"),
        Language("tn", "Setswana", "Tswana"),
        Language("nso", "Sepedi", "Northern Sotho"),
        Language("ss", "siSwati", "Swati"),
        Language("ve", "Tshivenda", "Venda"),
        Language("ts", "Xitsonga", "Tsonga"),
        Language("nr", "isiNdebele", "Southern Ndebele")
    )

    /** Tag of the language currently applied, or "" when following the system. */
    fun currentTag(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) return ""
        val locale = locales[0] ?: return ""
        return locale.language
    }

    fun apply(tag: String) {
        AppCompatDelegate.setApplicationLocales(
            if (tag.isEmpty()) LocaleListCompat.getEmptyLocaleList()
            else LocaleListCompat.forLanguageTags(tag)
        )
    }

    fun displayName(tag: String, systemDefaultLabel: String): String =
        languages.firstOrNull { it.tag == tag }?.nativeName ?: systemDefaultLabel
}
