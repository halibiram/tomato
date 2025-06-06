package com.tomatomediacenter.ui.components

import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tomatomediacenter.ui.theme.TomatoMediaCenterTheme

@Composable
fun AppTextField(
    modifier: Modifier = Modifier,
    label: String,
    initialValue: String = ""
) {
    TomatoMediaCenterTheme {
        var text by remember { mutableStateOf(initialValue) }
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(label) },
            modifier = modifier
        )
    }
}
