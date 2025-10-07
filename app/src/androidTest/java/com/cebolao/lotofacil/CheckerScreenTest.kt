package com.cebolao.lotofacil

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cebolao.lotofacil.data.LotofacilConstants
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class CheckerScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        // Navega para a tela do Conferidor antes de cada teste
        composeTestRule.onNodeWithText("Conferidor").performClick()
    }

    @Test
    fun checkerScreen_when15NumbersAreSelected_checkButtonIsEnabled() {
        // Verifica o estado inicial do botão
        composeTestRule.onNodeWithText("Conferir").assertIsNotEnabled()

        // Seleciona 15 números
        for (i in 1..LotofacilConstants.GAME_SIZE) {
            composeTestRule.onNodeWithText("%02d".format(i)).performClick()
        }

        // Verifica se o progresso foi atualizado
        composeTestRule.onNodeWithText("15 de 15 números").assertIsDisplayed()

        // Verifica se o botão "Conferir" agora está habilitado
        composeTestRule.onNodeWithText("Conferir").assertIsEnabled()
    }

    @Test
    fun checkerScreen_whenCheckButtonIsClicked_resultIsShown() {
        // Seleciona 15 números
        for (i in 1..LotofacilConstants.GAME_SIZE) {
            composeTestRule.onNodeWithText("%02d".format(i)).performClick()
        }

        // Clica para conferir
        composeTestRule.onNodeWithText("Conferir").performClick()

        // Aguarda e verifica se o resultado (ou o cabeçalho do resultado) é exibido.
        // O texto exato pode variar se o jogo foi premiado ou não.
        // "Análise de Desempenho" é um bom indicador de que a análise ocorreu.
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Análise de Desempenho").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Análise de Desempenho").assertIsDisplayed()
        composeTestRule.onNodeWithText("Estatísticas do Jogo").assertIsDisplayed()
    }

    @Test
    fun checkerScreen_clearButtonResetsSelection() {
        // Seleciona alguns números
        composeTestRule.onNodeWithText("01").performClick()
        composeTestRule.onNodeWithText("02").performClick()
        composeTestRule.onNodeWithText("03").performClick()

        // Verifica se o botão de limpar está habilitado
        composeTestRule.onNodeWithContentDescription("Limpar seleção").assertIsEnabled()

        // Clica no botão de limpar
        composeTestRule.onNodeWithContentDescription("Limpar seleção").performClick()

        // Verifica se o progresso foi resetado
        composeTestRule.onNodeWithText("0 de 15 números").assertIsDisplayed()

        // Verifica se o botão de conferir está desabilitado
        composeTestRule.onNodeWithText("Conferir").assertIsNotEnabled()
    }
}