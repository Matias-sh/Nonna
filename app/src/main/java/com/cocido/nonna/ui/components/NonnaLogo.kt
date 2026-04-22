package com.cocido.nonna.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.cocido.nonna.R

@Composable
fun NonnaLogo(
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    Image(
        painter = painterResource(id = R.drawable.nonna_logo),
        contentDescription = stringResource(R.string.welcome_logo_cd),
        modifier = modifier.clip(RoundedCornerShape(20.dp)),
        contentScale = contentScale
    )
}
