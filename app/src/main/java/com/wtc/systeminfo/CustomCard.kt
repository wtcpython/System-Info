package com.wtc.systeminfo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@Composable
fun CustomCard(title: String, description: String, onClick: (() -> Unit) = {  }) {
    Card(modifier = Modifier
        .padding(top = 10.dp, bottom = 10.dp)
        .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(),
        onClick = onClick)
    {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, modifier = Modifier.weight(1f))
            Text(description, modifier = Modifier.weight(1f), textAlign = TextAlign.Right)
        }
    }
}
@Composable
fun <T> IntervalSelectorCard(
    currentValue: T,
    options: List<T>,
    onIntervalChange: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(top = 10.dp, bottom = 10.dp)
            .fillMaxWidth()
            .clickable { expanded = true },
        elevation = CardDefaults.cardElevation()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "刷新频率",
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentSize(Alignment.CenterEnd)
            ) {
                Text(
                    "$currentValue 秒",
                    modifier = Modifier
                        .clickable { expanded = true }
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(text = "$option 秒") },
                            onClick = {
                                onIntervalChange(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}