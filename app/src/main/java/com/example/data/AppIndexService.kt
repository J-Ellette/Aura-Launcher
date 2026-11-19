package com.example.data

import com.example.model.AppCategory
import com.example.model.LauncherApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.math.min

data class IndexedAppEntry(
    val app: LauncherApp,
    val normalizedLabel: String,
    val tokens: List<String>,
    val acronym: String,
    val packageTokens: List<String>,
    val categoryKeywords: List<String>
)

data class AppSearchHit(
    val app: LauncherApp,
    val score: Int,
    val matchReason: String
)

data class AppIndexSnapshot(
    val entries: List<IndexedAppEntry> = emptyList(),
    val indexedCount: Int = 0,
    val lastIndexedEpochMs: Long = 0L,
    val isIndexing: Boolean = false
)

/**
 * Background indexing service that catalogs installed and curated apps into a token,
 * acronym, keyword, and fuzzy-searchable Spotlight index.
 */
class AppIndexService {

    private val _snapshot = MutableStateFlow(AppIndexSnapshot())
    val snapshot: StateFlow<AppIndexSnapshot> = _snapshot.asStateFlow()

    suspend fun rebuildIndex(apps: List<LauncherApp>): AppIndexSnapshot {
        _snapshot.value = _snapshot.value.copy(isIndexing = true)
        val built = withContext(Dispatchers.Default) {
            apps.map { app ->
                val cleanLabel = app.label.trim()
                val normalized = cleanLabel.lowercase()
                val tokens = normalized.split(Regex("[^a-z0-9]+")).filter { it.isNotBlank() }
                val acronym = tokens.mapNotNull { it.firstOrNull() }.joinToString("")
                val pkgTokens = (app.packageName ?: "")
                    .lowercase()
                    .split('.', '_', '-')
                    .filter { it.length >= 2 && it !in COMMON_PKG_NOISE }

                val catKeywords = keywordsForCategory(app.category) + keywordsForIconKey(app.iconKey)

                IndexedAppEntry(
                    app = app,
                    normalizedLabel = normalized,
                    tokens = tokens,
                    acronym = acronym,
                    packageTokens = pkgTokens,
                    categoryKeywords = catKeywords.distinct()
                )
            }
        }
        val next = AppIndexSnapshot(
            entries = built,
            indexedCount = built.size,
            lastIndexedEpochMs = System.currentTimeMillis(),
            isIndexing = false
        )
        _snapshot.value = next
        return next
    }

    fun search(
        query: String,
        launchCounts: Map<String, Int> = emptyMap(),
        limit: Int = 20
    ): List<AppSearchHit> {
        val entries = _snapshot.value.entries
        val q = query.trim().lowercase()

        if (q.isBlank()) {
            return entries
                .sortedByDescending { launchCounts[it.app.id] ?: 0 }
                .take(limit)
                .map {
                    val count = launchCounts[it.app.id] ?: 0
                    AppSearchHit(
                        app = it.app,
                        score = count,
                        matchReason = if (count > 0) "Frequently used" else it.app.category.title
                    )
                }
        }

        val results = mutableListOf<AppSearchHit>()
        for (entry in entries) {
            val recencyBoost = (launchCounts[entry.app.id] ?: 0) * 6
            val match = scoreEntry(entry, q)
            if (match != null) {
                results += AppSearchHit(
                    app = entry.app,
                    score = match.first + recencyBoost,
                    matchReason = match.second
                )
            }
        }

        return results
            .sortedWith(compareByDescending<AppSearchHit> { it.score }.thenBy { it.app.label.lowercase() })
            .take(limit)
    }

    private fun scoreEntry(entry: IndexedAppEntry, q: String): Pair<Int, String>? {
        return when {
            entry.normalizedLabel == q -> 1000 to "Exact match"
            entry.normalizedLabel.startsWith(q) -> 850 to "Name match"
            entry.tokens.any { it.startsWith(q) } -> 740 to "Word match"
            entry.acronym.length >= 2 && entry.acronym.startsWith(q) -> 660 to "Acronym (${entry.acronym.uppercase()})"
            entry.normalizedLabel.contains(q) -> 560 to "Contains \"$q\""
            entry.packageTokens.any { it.startsWith(q) } -> 450 to "Package match"
            entry.categoryKeywords.any { it.startsWith(q) || it.contains(q) } -> {
                410 to "${entry.app.category.title} keyword"
            }
            q.length >= 3 -> {
                val maxDist = if (q.length <= 4) 1 else 2
                val bestTokenDist = entry.tokens.minOfOrNull { token ->
                    levenshtein(q, token.take(q.length + 1))
                } ?: Int.MAX_VALUE
                if (bestTokenDist <= maxDist) {
                    (320 - bestTokenDist * 50) to "Fuzzy match"
                } else {
                    null
                }
            }
            else -> null
        }
    }

    private fun keywordsForCategory(category: AppCategory): List<String> = when (category) {
        AppCategory.SUGGESTIONS -> listOf("suggested", "top", "web", "browse", "internet")
        AppCategory.SOCIAL -> listOf("chat", "sms", "call", "dial", "friends", "social", "contact")
        AppCategory.PRODUCTIVITY -> listOf("work", "schedule", "tasks", "notes", "office", "calendar", "focus")
        AppCategory.MEDIA -> listOf("music", "video", "audio", "play", "stream", "entertainment")
        AppCategory.UTILITIES -> listOf("tools", "system", "calc", "control", "dnd", "focus", "dock", "lock")
        AppCategory.CREATIVITY -> listOf("photo", "camera", "wallpaper", "theme", "font", "design", "style")
    }

    private fun keywordsForIconKey(key: String): List<String> = when (key) {
        "phone" -> listOf("dialer", "voice", "call")
        "messages" -> listOf("sms", "text", "chat")
        "safari" -> listOf("web", "internet", "browser")
        "camera" -> listOf("snapshot", "photo", "video")
        "calendar" -> listOf("events", "agenda", "date")
        "calculator" -> listOf("math", "numbers")
        "wallpaper" -> listOf("background", "dynamic", "dawn", "sunset", "night")
        "lock_style" -> listOf("font", "clock", "typography", "lockscreen")
        "controls" -> listOf("control center", "wifi", "bluetooth", "brightness", "dnd", "flashlight")
        "widgets" -> listOf("focus", "weather", "battery", "reminders")
        "home" -> listOf("default", "launcher", "home screen", "nova", "replace")
        else -> emptyList()
    }

    private fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        val prev = IntArray(b.length + 1) { it }
        val curr = IntArray(b.length + 1)
        for (i in a.indices) {
            curr[0] = i + 1
            for (j in b.indices) {
                val cost = if (a[i] == b[j]) 0 else 1
                curr[j + 1] = min(min(curr[j] + 1, prev[j + 1] + 1), prev[j] + cost)
            }
            curr.copyInto(prev)
        }
        return prev[b.length]
    }

    private companion object {
        val COMMON_PKG_NOISE = setOf("com", "org", "net", "android", "google", "app", "example")
    }
}
