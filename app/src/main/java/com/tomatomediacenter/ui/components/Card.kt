package com.tomatomediacenter.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tomatomediacenter.ui.theme.TomatoMediaCenterTheme

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    TomatoMediaCenterTheme {
        Card(
            modifier = modifier,
            content = content
        )
    }
}
