package dev.apn7.shunya.feature.home.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.ShunyaButtonStyle
import dev.apn7.shunya.core.designsystem.component.ShunyaDialog
import dev.apn7.shunya.core.designsystem.component.ShunyaTextButton
import dev.apn7.shunya.core.designsystem.component.ShunyaTextField
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme

/** Longest text accepted in one-line fields (intention, app names). */
internal const val ONE_LINE_MAX_CHARS = 80

/**
 * A dialog with one text field that opens with the keyboard up: used for the intention line and
 * for renaming apps. [onSave] gets the text as typed (callers trim it); an optional [neutralText]
 * button (e.g. "Original name") calls [onNeutral].
 */
@Composable
internal fun TextInputDialog(
    title: String,
    initial: String,
    placeholder: String?,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    summary: String? = null,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    neutralText: String? = null,
    onNeutral: (() -> Unit)? = null,
) {
    var text by rememberSaveable { mutableStateOf(initial) }
    val focusRequester = remember { FocusRequester() }
    ShunyaDialog(
        onDismiss = onDismiss,
        title = title,
        confirmText = stringResource(R.string.common_save),
        onConfirm = { onSave(text) },
        dismissText = stringResource(R.string.common_cancel),
    ) {
        if (summary != null) {
            Text(text = summary, style = ShunyaTheme.typography.bodySmall, color = ShunyaTheme.colors.secondary)
        }
        ShunyaTextField(
            value = text,
            onValueChange = { text = it.take(ONE_LINE_MAX_CHARS) },
            placeholder = placeholder,
            keyboardOptions = KeyboardOptions(capitalization = capitalization, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSave(text) }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        )
        if (neutralText != null && onNeutral != null) {
            ShunyaTextButton(text = neutralText, onClick = onNeutral, style = ShunyaButtonStyle.Secondary)
        }
        // Inside the dialog's own composition, so the field is attached when focus is requested.
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
}

/** Shown once when double-tap cannot lock: explains the accessibility service and offers to set it up. */
@Composable
internal fun LockHintDialog(onEnable: () -> Unit, onDismiss: () -> Unit) {
    ShunyaDialog(
        onDismiss = onDismiss,
        title = stringResource(R.string.home_lock_hint_title),
        confirmText = stringResource(R.string.common_enable),
        onConfirm = onEnable,
        dismissText = stringResource(R.string.common_not_now),
    ) {
        Text(
            text = stringResource(R.string.home_lock_hint_message),
            style = ShunyaTheme.typography.body,
            color = ShunyaTheme.colors.secondary,
        )
    }
}
