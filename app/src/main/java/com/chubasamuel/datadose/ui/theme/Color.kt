package com.chubasamuel.datadose.ui.theme

import androidx.compose.material.ButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)

val statusBarColor=Color(0xFF225522)
val buttonColor=AppBtnColors(Color(0xFF97DD97),Color(0xFF225522))

class AppBtnColors(private val background: Color, val content: Color): ButtonColors {
    @Composable
    override fun backgroundColor(enabled: Boolean): State<Color> {
        return if(enabled) mutableStateOf(background)
        else mutableStateOf(background)
    }
    @Composable
    override fun contentColor(enabled: Boolean): State<Color> {
        return if(enabled) mutableStateOf(content)
        else mutableStateOf(content)
    }
}