/*
 * Copyright (C) 2021 Manuel Di Donna
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  he Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.manueldidonna.jetpackcomposetemplate

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets

@Composable
fun JetpackComposeTemplateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) DarkColorPalette else LightColorPalette,
        shapes = Shapes,
        content = content
    )
}

@Composable
fun EdgeToEdgeContent(content: @Composable () -> Unit) {
    val view = LocalView.current
    val window = (LocalContext.current as Activity).window
    // window.statusBarColor = android.graphics.Color.TRANSPARENT
    // window.navigationBarColor = android.graphics.Color.TRANSPARENT
    val insetsController = remember(view, window) {
        WindowCompat.getInsetsController(window, view)
    }
    val isLightTheme = MaterialTheme.colors.isLight
    insetsController?.run {
        isAppearanceLightNavigationBars = isLightTheme
        isAppearanceLightStatusBars = isLightTheme
    }
    ProvideWindowInsets(content = content)
}

private val DarkColorPalette = darkColors(
    primary = Color(0xff9bbed3),
    primaryVariant = Color(0xff9bbed3),
    secondary = Color(0xff86e6a9),
    surface = Color(0xff9bbed3).copy(alpha = 0.08f).compositeOver(Color(0xff121212)),
    background = Color(0xff9bbed3).copy(alpha = 0.08f).compositeOver(Color(0xff121212))
)

private val LightColorPalette = lightColors(
    primary = Color(0xff073042),
    primaryVariant = Color(0xff073042),
    secondary = Color(0xff3ddc84)
)

private val Shapes = Shapes(
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(16.dp)
)
