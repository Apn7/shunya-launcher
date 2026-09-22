package dev.apn7.shunya.feature.onboarding.logic

/** The first-run steps, in order (PRD 3.6). Every step can be skipped. */
enum class OnboardingStep { Welcome, Look, Favorites, DefaultLauncher, Permissions, Done }

/** Pure step navigation of onboarding. */
object OnboardingFlow {

    val total: Int get() = OnboardingStep.entries.size

    /** The step after [step], or null after the last one. */
    fun next(step: OnboardingStep): OnboardingStep? = OnboardingStep.entries.getOrNull(step.ordinal + 1)

    /** The step before [step], or null on the first one (Back there leaves onboarding). */
    fun previous(step: OnboardingStep): OnboardingStep? = OnboardingStep.entries.getOrNull(step.ordinal - 1)

    /** 1-based position for "Step 2 of 6". */
    fun position(step: OnboardingStep): Int = step.ordinal + 1
}

/** Pure multi-select rules of the favorites picker. Ids keep the order in which they were picked. */
object FavoriteSelection {

    /** Removes [id] when selected; adds it at the end when fewer than [max] are selected; else unchanged. */
    fun toggled(selected: List<String>, id: String, max: Int): List<String> = when {
        id in selected -> selected - id
        isFull(selected, max) -> selected
        else -> selected + id
    }

    fun isFull(selected: List<String>, max: Int): Boolean = selected.size >= max
}
