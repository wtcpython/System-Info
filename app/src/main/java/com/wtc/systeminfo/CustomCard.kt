package com.wtc.systeminfo

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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