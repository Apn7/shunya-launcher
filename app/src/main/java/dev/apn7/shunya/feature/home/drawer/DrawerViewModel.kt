package dev.apn7.shunya.feature.home.drawer

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.apn7.shunya.core.contract.UsageRepository
import dev.apn7.shunya.core.data.AppsRepository
import dev.apn7.shunya.core.data.SettingsRepository
import dev.apn7.shunya.core.model.AppKey
import dev.apn7.shunya.core.model.DrawerPrefs
import dev.apn7.shunya.core.model.DrawerSort
import dev.apn7.shunya.core.model.LauncherApp
import dev.apn7.shunya.feature.home.drawer.logic.DrawerLayout
import dev.apn7.shunya.feature.home.drawer.logic.DrawerRow
import dev.apn7.shunya.feature.home.drawer.logic.SectionIndex
import dev.apn7.shunya.feature.home.drawer.logic.UsageOrder
import dev.apn7.shunya.feature.home.search.logic.Calculator
import dev.apn7.shunya.feature.home.search.logic.FuzzyMatcher
import dev.apn7.shunya.feature.home.search.logic.SearchKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** What the drawer shows. [results] and [calculation] belong to [query]; both are empty for a blank query. */
@Immutable
data class DrawerUiState(
    val isLoaded: Boolean = false,
    /** Every visible app in drawer order. */
    val apps: List<LauncherApp> = emptyList(),
    /** Rows (apps and optional letter headers) and fast-scroller sections over [apps]. */
    val layout: DrawerLayout = DrawerLayout.Empty,
    /** The alphabet fast scroller is shown (A–Z order with more than one letter). */
    val showScroller: Boolean = false,
    val query: String = "",
    val results: List<LauncherApp> = emptyList(),
    /** Calculator result for [query] ("84"), or null when it is not a calculation. */
    val calculation: String? = null,
)

/**
 * The app drawer and search. The sorted list, its letter index and every label's search keys
 * are prepared off the main thread whenever the apps or drawer settings change; each keystroke
 * then only ranks the prepared keys.
 */
class DrawerViewModel(
    appsRepository: AppsRepository,
    private val settingsRepository: SettingsRepository,
    private val usageRepository: UsageRepository,
) : ViewModel() {

    /** The search text, read synchronously by the text field (no lost keystrokes or cursor jumps). */
    var query by mutableStateOf("")
        private set

    private val queryFlow = MutableStateFlow("")

    /** Packages, most used first; empty unless "Most used" is chosen and usage access is granted. */
    private val ranking = MutableStateFlow<List<String>>(emptyList())

    private val catalog: StateFlow<Catalog> = combine(
        appsRepository.visibleApps,
        settingsRepository.settings.map { it.drawer }.distinctUntilChanged(),
        ranking,
    ) { apps, prefs, ranked -> Catalog.build(apps, prefs, ranked) }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, Catalog.Empty)

    val state: StateFlow<DrawerUiState> = combine(catalog, queryFlow, appsRepository.isLoaded) { catalog, text, loaded ->
        val searching = text.isNotBlank()
        DrawerUiState(
            isLoaded = loaded,
            apps = catalog.apps,
            layout = catalog.layout,
            showScroller = catalog.alphabetical && catalog.layout.sections.size > 1,
            query = text,
            results = if (searching) FuzzyMatcher.rank(text, catalog.apps) { catalog.keysOf(it) } else emptyList(),
            calculation = if (searching) Calculator.calculate(text) else null,
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, DrawerUiState())

    init {
        onOpened()
    }

    fun onQueryChange(text: String) {
        query = text
        queryFlow.value = text
    }

    fun clearQuery() = onQueryChange("")

    /** The drawer became visible (and once at start): refresh the "Most used" order when it is the chosen sort. */
    fun onOpened() {
        if (settingsRepository.settings.value.drawer.sort != DrawerSort.MostUsed) return
        viewModelScope.launch {
            ranking.value = if (usageRepository.hasAccess()) {
                usageRepository.ranking(RANKING_DAYS).map { it.packageName }
            } else {
                emptyList()
            }
        }
    }

    /** Sorted apps with their layout and search keys (custom and original label). */
    private class Catalog(
        val apps: List<LauncherApp>,
        val layout: DrawerLayout,
        val alphabetical: Boolean,
        private val keys: Map<AppKey, List<SearchKey>>,
    ) {
        fun keysOf(app: LauncherApp): List<SearchKey> = keys[app.key] ?: listOf(SearchKey(app.displayLabel))

        companion object {
            val Empty = Catalog(emptyList(), DrawerLayout.Empty, alphabetical = true, keys = emptyMap())

            fun build(apps: List<LauncherApp>, prefs: DrawerPrefs, ranking: List<String>): Catalog {
                val byUsage = prefs.sort == DrawerSort.MostUsed && ranking.isNotEmpty()
                val sorted = if (byUsage) UsageOrder.sort(apps, ranking) { it.packageName } else apps
                val layout = if (byUsage) {
                    // Sections make no sense out of alphabetical order: plain rows, no index.
                    DrawerLayout(sorted.indices.map { DrawerRow.App(it) }, emptyList())
                } else {
                    SectionIndex.layout(sorted.map { it.displayLabel }, prefs.showSectionLetters)
                }
                val keys = HashMap<AppKey, List<SearchKey>>(sorted.size * 2)
                for (app in sorted) {
                    val custom = app.customLabel
                    keys[app.key] = if (custom == null) {
                        listOf(SearchKey(app.label))
                    } else {
                        listOf(SearchKey(custom), SearchKey(app.label))
                    }
                }
                return Catalog(sorted, layout, alphabetical = !byUsage, keys = keys)
            }
        }
    }

    private companion object {
        /** "Most used" looks at the last week. */
        const val RANKING_DAYS = 7
    }
}
