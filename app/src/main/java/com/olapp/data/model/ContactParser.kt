package com.olapp.data.model

enum class ContactPlatform(val key: String, val label: String) {
    INSTAGRAM("ig", "Instagram"),
    WHATSAPP("wa", "WhatsApp"),
    TELEGRAM("tg", "Telegram"),
    TWITTER("tw", "Twitter / X"),
    PHONE("ph", "Phone"),
    EMAIL("em", "Email"),
    OTHER("ot", "Other")
}

data class ContactEntry(val platform: ContactPlatform, val value: String)

object ContactParser {

    fun parse(raw: String): List<ContactEntry> {
        if (raw.isBlank()) return emptyList()
        // Structured format: "ig:@user|wa:+351123|tg:@user"
        if (raw.contains("|") || Regex("^[a-z]{2}:.+").containsMatchIn(raw)) {
            return raw.split("|").mapNotNull { part ->
                val idx = part.indexOf(':')
                if (idx < 1) return@mapNotNull null
                val key = part.substring(0, idx).trim()
                val value = part.substring(idx + 1).trim()
                if (value.isBlank()) return@mapNotNull null
                val platform = ContactPlatform.entries.firstOrNull { it.key == key } ?: ContactPlatform.OTHER
                ContactEntry(platform, value)
            }
        }
        // Legacy plain string — guess platform
        return listOf(ContactEntry(guessPlatform(raw), raw))
    }

    fun format(entries: List<ContactEntry>): String =
        entries.filter { it.value.isNotBlank() }
            .joinToString("|") { "${it.platform.key}:${it.value}" }

    fun intentUri(entry: ContactEntry): String? = when (entry.platform) {
        ContactPlatform.INSTAGRAM -> {
            val handle = entry.value.trimStart('@')
            "https://instagram.com/$handle"
        }
        ContactPlatform.WHATSAPP -> {
            val num = entry.value.replace(Regex("[^0-9+]"), "")
            "https://wa.me/$num"
        }
        ContactPlatform.TELEGRAM -> {
            val handle = entry.value.trimStart('@')
            "https://t.me/$handle"
        }
        ContactPlatform.TWITTER -> {
            val handle = entry.value.trimStart('@')
            "https://x.com/$handle"
        }
        ContactPlatform.PHONE -> "tel:${entry.value.replace(" ", "")}"
        ContactPlatform.EMAIL -> "mailto:${entry.value}"
        ContactPlatform.OTHER -> null
    }

    private fun guessPlatform(value: String): ContactPlatform {
        val lower = value.lowercase().trim()
        return when {
            lower.contains("instagram") || lower.contains("ig.me") -> ContactPlatform.INSTAGRAM
            lower.contains("whatsapp") || lower.contains("wa.me") -> ContactPlatform.WHATSAPP
            lower.contains("telegram") || lower.contains("t.me") -> ContactPlatform.TELEGRAM
            lower.contains("twitter") || lower.contains("x.com") -> ContactPlatform.TWITTER
            lower.contains("@") && lower.contains(".") -> ContactPlatform.EMAIL
            lower.startsWith("+") || lower.all { it.isDigit() || it in " -()+" } -> ContactPlatform.PHONE
            lower.startsWith("@") -> ContactPlatform.INSTAGRAM
            else -> ContactPlatform.OTHER
        }
    }
}
