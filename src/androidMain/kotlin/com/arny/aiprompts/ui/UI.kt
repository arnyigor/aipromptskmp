package com.arny.aiprompts.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Icon
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import com.arny.aipromptskmp.R

@Composable
fun TabNavigationExample() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Home", "List", "Settings")

    Column {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                )
            }
        }

//        when (selectedTab) {
//            0 -> HomeScreen()
//            1 -> ListScreen()
//            2 -> SettingsScreen()
//        }
    }
}
