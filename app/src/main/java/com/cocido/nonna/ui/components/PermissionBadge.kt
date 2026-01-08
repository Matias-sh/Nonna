package com.cocido.nonna.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.RoleAbueloBackground
import com.cocido.nonna.ui.theme.RoleAbueloText
import com.cocido.nonna.ui.theme.RoleColaboradorBackground
import com.cocido.nonna.ui.theme.RoleColaboradorText
import com.cocido.nonna.ui.theme.RoleCreadorBackground
import com.cocido.nonna.ui.theme.RoleCreadorText
import com.cocido.nonna.ui.theme.RoleInvitadoBackground
import com.cocido.nonna.ui.theme.RoleInvitadoText

enum class CofreRole(
    val displayName: String,
    val backgroundColor: Color,
    val textColor: Color
) {
    Creador("Creador", RoleCreadorBackground, RoleCreadorText),
    Colaborador("Colaborador", RoleColaboradorBackground, RoleColaboradorText),
    Invitado("Invitado", RoleInvitadoBackground, RoleInvitadoText),
    Abuelo("Legado en vida", RoleAbueloBackground, RoleAbueloText)
}

@Composable
fun PermissionBadge(
    role: CofreRole,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = NonnaCorners.Full,
        color = role.backgroundColor
    ) {
        Text(
            text = role.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = role.textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
