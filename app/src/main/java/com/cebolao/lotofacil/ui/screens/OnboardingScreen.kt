package com.cebolao.lotofacil.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.components.AnimateOnEntry
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val imageResId: Int,
    val title: String,
    val description: String
)

private val ONBOARDING_PAGES = listOf(
    OnboardingPage(R.drawable.img_onboarding_step_1, "Boas-vindas ao Cebolão!", "Explore as estatísticas da Lotofácil e use filtros inteligentes para criar seus jogos."),
    OnboardingPage(R.drawable.img_onboarding_step_2, "Análise e Estatísticas", "Veja os números mais sorteados, os mais atrasados e a distribuição de padrões em gráficos interativos."),
    OnboardingPage(R.drawable.img_onboarding_step_3, "Gerador Inteligente", "Aplique filtros como 'Soma', 'Pares' e 'Moldura' para gerar combinações com maior probabilidade estatística."),
    OnboardingPage(R.drawable.img_onboarding_step_4, "Confira e Gerencie", "Use o conferidor para testar seus jogos contra todo o histórico e salve suas melhores combinações.")
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onOnboardingComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { ONBOARDING_PAGES.size })
    val scope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(vertical = Dimen.CardPadding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                AnimateOnEntry {
                    OnboardingPageContent(page = ONBOARDING_PAGES[page])
                }
            }
            OnboardingControls(
                pageCount = ONBOARDING_PAGES.size,
                currentPage = pagerState.currentPage,
                onNextClick = {
                    scope.launch {
                        if (pagerState.currentPage < ONBOARDING_PAGES.size - 1) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        } else {
                            onOnboardingComplete()
                        }
                    }
                },
                onSkipClick = onOnboardingComplete
            )
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimen.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = page.imageResId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(AppConfig.UI.OnboardingImageFraction),
            contentScale = ContentScale.Fit
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)
        ) {
            Text(page.title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun OnboardingControls(
    pageCount: Int,
    currentPage: Int,
    onNextClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = Dimen.ScreenPadding, vertical = Dimen.LargePadding)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val isLastPage = currentPage == pageCount - 1

        Box(modifier = Modifier.weight(1f)) {
            if (!isLastPage) {
                TextButton(onClick = onSkipClick) {
                    Text(stringResource(R.string.onboarding_skip))
                }
            }
        }

        PagerIndicator(
            pageCount = pageCount,
            currentPage = currentPage,
            modifier = Modifier.weight(1f)
        )

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            Button(onClick = onNextClick) {
                Text(if (isLastPage) stringResource(R.string.onboarding_start) else stringResource(R.string.onboarding_next))
            }
        }
    }
}

@Composable
private fun PagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { iteration ->
            val isSelected = currentPage == iteration
            val width by animateDpAsState(
                targetValue = if (isSelected) Dimen.ActiveIndicatorWidth else Dimen.IndicatorHeight,
                label = "indicatorWidth"
            )
            val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant

            Box(
                modifier = Modifier
                    .padding(horizontal = Dimen.IndicatorSpacing)
                    .height(Dimen.IndicatorHeight)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}