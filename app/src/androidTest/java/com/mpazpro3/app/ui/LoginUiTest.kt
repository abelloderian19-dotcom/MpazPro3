package com.mpazpro3.app.ui

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mpazpro3.app.navigation.AppNavigation
import com.mpazpro3.app.ui.theme.MpazPro3Theme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.test.onAllNodesWithText


@RunWith(AndroidJUnit4::class)
class LoginUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun abrirApp() {
        composeRule.setContent { MpazPro3Theme { AppNavigation() } }
    }

    @Test
    fun camposVacios_muestranLosErroresPorCampo() {
        abrirApp()

        composeRule.onNodeWithText("Ingresar").performClick()

        composeRule.onNodeWithText("Ingresa tu usuario").assertIsDisplayed()
        composeRule.onNodeWithText("Ingresa tu clave").assertIsDisplayed()
        composeRule.onNodeWithText("Ingresar").assertIsDisplayed()
    }

    @Test
    fun claveIncorrecta_muestraElErrorGeneral_ySigueEnElLogin() {
        abrirApp()

        composeRule.onNodeWithText("Usuario o correo").performTextInput("utp@mpazpro3.cl")
        composeRule.onNodeWithText("Clave").performTextInput("mala")
        composeRule.onNodeWithText("Ingresar").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Usuario o clave incorrectos")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Usuario o clave incorrectos").assertIsDisplayed()
        composeRule.onNodeWithText("Ingresar").assertIsDisplayed()
    }

    @Test
    fun ingresoDeEstudiante_llegaAMisActividades() {
        abrirApp()

        composeRule.onNodeWithText("Usuario o correo").performTextInput("est001")
        composeRule.onNodeWithText("Clave").performTextInput("1234")
        composeRule.onNodeWithText("Ingresar").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Mis actividades")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Mis actividades").assertIsDisplayed()
        composeRule.onNodeWithText("Ingresar").assertDoesNotExist()
    }
}
