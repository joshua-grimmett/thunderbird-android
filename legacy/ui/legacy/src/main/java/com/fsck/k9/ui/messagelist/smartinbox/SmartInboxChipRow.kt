/*
 * Throwaway prototype integration: the chip row below sits in the legacy View-based
 * MessageListFragment purely so the Smart Inbox UX can be validated against real mail
 * today. All logic here will be replaced by an equivalent Compose implementation once
 * the ML 0.1 message-list migration has landed (see upstream issues under the
 * [ML 0.1/UI.04] label — the Compose MessageList* renderer is where this belongs
 * long-term). The classifier used here (MessageCategoryClassifier) is deliberately
 * portable — it has no Android or view-state dependency and will travel unchanged.
 *
 * Using the design-system ButtonFilled/ButtonOutlined atoms rather than a Material 3
 * FilterChip because the legacy module does not pull in material3 directly; chips land
 * naturally on the Compose list once ML 0.1 ships.
 */
package com.fsck.k9.ui.messagelist.smartinbox

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.k9mail.core.ui.compose.designsystem.atom.Surface
import app.k9mail.core.ui.compose.designsystem.atom.button.ButtonFilled
import app.k9mail.core.ui.compose.designsystem.atom.button.ButtonOutlined
import net.thunderbird.feature.mail.message.list.smartinbox.MessageCategory

data class SmartInboxChipState(
    val selected: MessageCategory? = null,
    val unreadCounts: Map<MessageCategory, Int> = emptyMap(),
)

@Composable
fun SmartInboxChipRow(
    state: SmartInboxChipState,
    onCategorySelected: (MessageCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CategoryChip(
                label = "All",
                selected = state.selected == null,
                onClick = { onCategorySelected(null) },
            )
            CategoryChip(
                label = "Personal",
                count = state.unreadCounts[MessageCategory.Personal] ?: 0,
                selected = state.selected == MessageCategory.Personal,
                onClick = { onCategorySelected(MessageCategory.Personal) },
            )
            CategoryChip(
                label = "Newsletters",
                count = state.unreadCounts[MessageCategory.Newsletter] ?: 0,
                selected = state.selected == MessageCategory.Newsletter,
                onClick = { onCategorySelected(MessageCategory.Newsletter) },
            )
            CategoryChip(
                label = "Notifications",
                count = state.unreadCounts[MessageCategory.Notification] ?: 0,
                selected = state.selected == MessageCategory.Notification,
                onClick = { onCategorySelected(MessageCategory.Notification) },
            )
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    count: Int? = null,
) {
    val displayLabel = if (count != null && count > 0) "$label  $count" else label
    if (selected) {
        ButtonFilled(text = displayLabel, onClick = onClick)
    } else {
        ButtonOutlined(text = displayLabel, onClick = onClick)
    }
}
