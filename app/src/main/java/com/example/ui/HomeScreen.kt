package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.MineMod
import com.example.ui.theme.MineCardBackground
import com.example.ui.theme.MineDarkBackground
import com.example.ui.theme.MineGold
import com.example.ui.theme.MineGreenAccent
import com.example.ui.theme.MineGreenPrimary
import com.example.ui.theme.MineGrey
import com.example.ui.theme.MineOrange
import com.example.ui.theme.MineRed
import com.example.ui.theme.MineWhite
import com.example.viewmodel.MineModViewModel
import java.io.File

@Composable
fun HomeScreen(
    viewModel: MineModViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (Int) -> Unit
) {
    val mods by viewModel.mods.collectAsState()
    val context = LocalContext.current
    var deleteConfirmModId by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MineDarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.resetForm()
                    onNavigateToCreate()
                },
                containerColor = MineGreenPrimary,
                contentColor = MineDarkBackground,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(8.dp)
                    .testTag("create_mod_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Créer un nouvel Item",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Gaming Custom Banner
            HeaderSection()

            Spacer(modifier = Modifier.height(12.dp))

            if (mods.isEmpty()) {
                EmptyStateSection(onNavigateToCreate = {
                    viewModel.resetForm()
                    onNavigateToCreate()
                })
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "VOS ITEMS PERSONNALISÉS",
                            color = MineGreenAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    items(mods, key = { it.id }) { mod ->
                        ModListItem(
                            mod = mod,
                            onShare = { viewModel.shareMod(context, mod) },
                            onEdit = { onNavigateToEdit(mod.id) },
                            onDelete = { deleteConfirmModId = mod.id }
                        )
                    }

                    // Bottom space for scroll safety
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        if (deleteConfirmModId != null) {
            AlertDialog(
                onDismissRequest = { deleteConfirmModId = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            deleteConfirmModId?.let { id ->
                                viewModel.deleteMod(id, context)
                            }
                            deleteConfirmModId = null
                        }
                    ) {
                        Text("Supprimer", color = MineRed, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteConfirmModId = null }) {
                        Text("Annuler", color = MineWhite)
                    }
                },
                containerColor = MineCardBackground,
                title = { Text("Supprimer l'item ?", color = MineWhite) },
                text = { Text("Voulez-vous vraiment supprimer définitivement cet item de votre catalogue ?", color = MineGrey) }
            )
        }
    }
}

@Composable
fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(MineGreenPrimary.copy(alpha = 0.25f), Color.Transparent)
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pixel-art hammer style icon
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = null,
                    tint = MineGreenPrimary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "MineMod Studio",
                    color = MineWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Créez et exportez vos Add-ons pour Minecraft Bedrock",
                color = MineGrey,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EmptyStateSection(onNavigateToCreate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MineCardBackground),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(MineGreenPrimary.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MineGreenPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Aucun Add-on créé",
                    color = MineWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Appuyez sur le bouton '+' pour fabriquer votre première arme, outil ou épée légendaire personnalisée !",
                    color = MineGrey,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                TextButton(
                    onClick = onNavigateToCreate,
                    modifier = Modifier
                        .background(MineGreenPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MineGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Commencer la création",
                        color = MineGreenPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ModListItem(
    mod: MineMod,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MineWhite.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MineCardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Texture Preview
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(1.dp, MineWhite.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (mod.texturePath != null) {
                    val file = File(mod.texturePath)
                    if (file.exists()) {
                        Image(
                            painter = rememberAsyncImagePainter(file),
                            contentDescription = "Texture pour ${mod.name}",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        // Fallback custom pixel blade drawing
                        PixelFallbackIcon()
                    }
                } else {
                    PixelFallbackIcon()
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Info Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = mod.name,
                    color = MineWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = mod.identifier,
                    color = MineGreenAccent,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stats row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Attack
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = "Attaque",
                        tint = MineOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${mod.damage} ATK",
                        color = MineOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Durability
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Durabilité",
                        tint = MineGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${mod.durability} DUR",
                        color = MineGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Share / Compile button (The primary Minecraft export action)
                IconButton(
                    onClick = onShare,
                    modifier = Modifier
                        .size(34.dp)
                        .background(MineGreenPrimary.copy(alpha = 0.12f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Partager ou importer dans Minecraft",
                        tint = MineGreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Mini editing actions row
                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Modifier",
                            tint = MineWhite.copy(alpha = 0.6f),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = MineRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PixelFallbackIcon() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // We draw a cute fallback representing a pixel tool
        Icon(
            imageVector = Icons.Default.Gavel,
            contentDescription = "Texture par défaut",
            tint = MineGreenAccent.copy(alpha = 0.4f),
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = "DEFAULT",
            color = MineGreenAccent.copy(alpha = 0.4f),
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
