package dev.apn7.shunya.feature.focus.schedules

import android.text.format.DateFormat
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.ShunyaDialog

/** Width the Material clock dial needs; narrower dialogs get the compact keyboard input instead. */
private val DialMinWidth = 300.dp

/**
 * Material 3 time picker in a Shunya dialog. [initialMinute] and the confirmed value are minutes
 * after midnight. Follows the phone's 12/24-hour setting.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TimePickerDialog(
    title: String,
    initialMinute: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val state = rememberTimePickerState(
        initialHour = initialMinute / 60,
        initialMinute = initialMinute % 60,
        is24Hour = DateFormat.is24HourFormat(context),
    )
    ShunyaDialog(
        onDismiss = onDismiss,
        title = title,
        confirmText = stringResource(R.string.common_ok),
        onConfirm = { onConfirm(state.hour * 60 + state.minute) },
        dismissText = stringResource(R.string.common_cancel),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (maxWidth >= DialMinWidth) {
                TimePicker(state = state)
            } else {
                TimeInput(state = state)
            }
        }
    }
}
