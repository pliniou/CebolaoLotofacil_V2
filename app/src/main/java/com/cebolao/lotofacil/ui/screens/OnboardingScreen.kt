package com.cebolao.lotofacil.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import kotlinx.coroutines.launch

data class OnboardingPage(
    val imageResId: Int,
    val title: String,
    val description: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onOnboardingComplete: () -> Unit) {
    val pages = remember {
        listOf(
            OnboardingPage(
                imageResId = com.cebolao.lotofacil.R.drawable.img_onboarding_step_1,
                title = "Boas-vindas ao Cebolão!",
                description = "Explore as estatísticas da Lotofácil e use filtros inteligentes para criar seus jogos."
            ),
            OnboardingPage(
                imageResId = com.cebolao.lotofacil.R.drawable.img_onboarding_step_2,
                title = "Análise e Estatísticas",
                description = "Veja os números mais sorteados, os mais atrasados e a distribuição de padrões em gráficos interativos."
            ),
            OnboardingPage(
                imageResId = com.cebolao.lotofacil.R.drawable.img_onboarding_step_3,
                title = "Gerador Inteligente",
                description = "Aplique filtros como 'Soma', 'Pares' e 'Moldura' para gerar combinações com maior probabilidade estatística."
            ),
            OnboardingPage(
                imageResId = com.cebolao.lotofacil.R.drawable.img_onboarding_step_4,
                title = "Confira e Gerencie",
                description = "Use o conferidor para testar seus jogos contra todo o histórico e salve suas melhores combinações."
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(page = pages[page])
            }
            OnboardingControls(
                pageCount = pages.size,
                currentPage = pagerState.currentPage,
                onNextClick = {
                    scope.launch {
                        if (pagerState.currentPage < pages.size - 1) {
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

@SuppressLint("DiscouragedApi")
@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = page.imageResId),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun OnboardingControls(
    pageCount: Int,
    currentPage: Int,
    onNextClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val isLastPage = currentPage == pageCount - 1
        TextButton(
            onClick = onSkipClick,
            enabled = !isLastPage
        ) {
            if (!isLastPage) {
                Text(stringResource(R.string.onboarding_skip))
            }
        }

        PagerIndicator(pageCount = pageCount, currentPage = currentPage)

        Button(onClick = onNextClick) {
            Text(if (isLastPage) stringResource(R.string.onboarding_start) else stringResource(R.string.onboarding_next))
        }
    }
}

@Composable
fun PagerIndicator(pageCount: Int, currentPage: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(pageCount) { iteration ->
            val isSelected = currentPage == iteration
            val width by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 8.dp,
                label = "indicatorWidth"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            )
        }
    }
}