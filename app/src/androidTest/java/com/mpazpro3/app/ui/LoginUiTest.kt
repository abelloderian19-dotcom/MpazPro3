package com.mpazpro3.app.ui

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mpazpro3.app.navigation.AppNavigation
import com.mpazpro3.app.ui.theme.MpazPro3Theme
import org.junit.Rule
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith

/**
 * Prueba de interfaz (Compose UI Test) del ingreso. Se ejecuta en un emulador o dispositivo:
 * en Android Studio, clic derecho sobre la clase > Run, o `./gradlew connectedDebugAndroidTest`.
 *
 * Pantalla bajo prueba: LoginScreen dentro de AppNavigation (campos "Usuario o correo" y "Clave", botón "Ingresar").
 *
 * Casos (3):
 *  1. Campos vacíos: tocar "Ingresar" sin escribir nada muestra "Ingresa tu usuario" y "Ingresa tu clave" y sigue en el login.
 *  2. Clave incorrecta: utp@mpazpro3.cl con "mala" muestra "Usuario o clave incorrectos" y NO navega.
 *  3. Ingreso correcto: est001 / 1234 llega a la pantalla "Mis actividades" (barra superior) y no aparece el botón "Ingresar".
 *
 * Pistas: composeRule.onNodeWithText("Usuario o correo").performTextInput("est001");
 * composeRule.onNodeWithText("Ingresar").performClick(); composeRule.waitUntil(5_000) { ... } para esperar al ViewModel;
 * composeRule.onNodeWithText("Mis actividades").assertIsDisplayed(). Si la app está en modo servidor (USAR_API = true o interruptor apagado), el servidor debe estar encendido.
 */
@RunWith(AndroidJUnit4::class)
class LoginUiTest {

    private val preferencias by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext
            .getSharedPreferences("mpazpro3_modo", Context.MODE_PRIVATE)
    }
    private var habiaPreferenciaModo = false
    private var modoAnterior = false

    @get:Rule
    val composeRule = createComposeRule()

    private fun abrirApp() {
        composeRule.setContent { MpazPro3Theme { AppNavigation() } }
    }

    @Before
    fun activarModoLocalParaLaPrueba() {
        habiaPreferenciaModo = preferencias.contains("modoLocal")
        modoAnterior = preferencias.getBoolean("modoLocal", true)
        preferencias.edit().putBoolean("modoLocal", true).commit()
    }

    @After
    fun restaurarModoAnterior() {
        preferencias.edit().apply {
            if (habiaPreferenciaModo) putBoolean("modoLocal", modoAnterior) else remove("modoLocal")
        }.commit()
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
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Usuario o clave incorrectos").fetchSemanticsNodes().isNotEmpty()
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
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Mis actividades").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Mis actividades").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithText("Ingresar").fetchSemanticsNodes().isEmpty())
    }
}
