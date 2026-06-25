package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.ui.theme.MineCardBackground
import com.example.ui.theme.MineDarkBackground
import com.example.ui.theme.MineGold
import com.example.ui.theme.MineGreenAccent
import com.example.ui.theme.MineGreenPrimary
import com.example.ui.theme.MineGrey
import com.example.ui.theme.MineOrange
import com.example.ui.theme.MineWhite
import com.example.viewmodel.MineModViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditScreen(
    viewModel: MineModViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Register modern Gallery Picker Contract
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onTextureSelected(uri)
    }

    Scaffold(
        containerColor = MineDarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (viewModel.editingModId == null) "CRÉER UN ITEM" else "MODIFIER L'ITEM",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MineWhite
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = MineWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MineCardBackground,
                    titleContentColor = MineWhite
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Informations Générales
            Text(
                text = "INFORMATIONS DE L'OBJET",
                color = MineGreenAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            // Input: Nom de l'objet
            OutlinedTextField(
                value = viewModel.formName,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("Nom de l'objet (ex: Épée Légendaire)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_mod_name"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MineGreenPrimary,
                    focusedLabelColor = MineGreenPrimary,
                    unfocusedBorderColor = MineGrey.copy(alpha = 0.5f),
                    unfocusedLabelColor = MineGrey,
                    focusedTextColor = MineWhite,
                    unfocusedTextColor = MineWhite
                )
            )

            // Input: Identifiant unique (ex: custom:sword)
            OutlinedTextField(
                value = viewModel.formIdentifier,
                onValueChange = { viewModel.onIdentifierChange(it) },
                label = { Text("ID Unique / Identifier") },
                placeholder = { Text("mymod:legendary_sword") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_mod_identifier"),
                singleLine = true,
                supportingText = {
                    Text(
                        text = "Format: namespace:nom_de_l_objet (ex: mymod:legendary_sword)",
                        fontSize = 10.sp,
                        color = MineGrey
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MineGreenPrimary,
                    focusedLabelColor = MineGreenPrimary,
                    unfocusedBorderColor = MineGrey.copy(alpha = 0.5f),
                    unfocusedLabelColor = MineGrey,
                    focusedTextColor = MineWhite,
                    unfocusedTextColor = MineWhite
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Section 2: Attributs & Caractéristiques (Slider Damage)
            Text(
                text = "ATTRIBUTS DE COMBAT & RÉSISTANCE",
                color = MineGreenAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MineCardBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = MineOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Dégâts d'attaque",
                                color = MineWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = "${viewModel.formDamage} ⚔️",
                            color = MineOrange,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 16.sp
                        )
                    }

                    Slider(
                        value = viewModel.formDamage.toFloat(),
                        onValueChange = { viewModel.onDamageChange(it.toInt()) },
                        valueRange = 1f..30f,
                        steps = 29,
                        colors = SliderDefaults.colors(
                            thumbColor = MineGreenPrimary,
                            activeTrackColor = MineGreenPrimary,
                            inactiveTrackColor = MineGrey.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("slider_mod_damage")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MineGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Durabilité / Solidité",
                                color = MineWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = if (viewModel.formDurability == 0) "" else viewModel.formDurability.toString(),
                        onValueChange = {
                            val filtered = it.filter { char -> char.isDigit() }
                            val valInt = if (filtered.isEmpty()) 0 else filtered.toIntOrNull() ?: 250
                            viewModel.onDurabilityChange(valInt)
                        },
                        placeholder = { Text("250") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_mod_durability"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MineGreenPrimary,
                            focusedLabelColor = MineGreenPrimary,
                            unfocusedBorderColor = MineGrey.copy(alpha = 0.3f),
                            focusedTextColor = MineWhite,
                            unfocusedTextColor = MineWhite
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Material presets
                    Text(
                        text = "Remplissage rapide (Matériaux) :",
                        color = MineGrey,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PresetButton(label = "Or (32)", value = 32, onClick = { viewModel.onDurabilityChange(32) })
                        PresetButton(label = "Fer (250)", value = 250, onClick = { viewModel.onDurabilityChange(250) })
                        PresetButton(label = "Diamant (1561)", value = 1561, onClick = { viewModel.onDurabilityChange(1561) })
                        PresetButton(label = "Netherite (2031)", value = 2031, onClick = { viewModel.onDurabilityChange(2031) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Section 3: Texture
            Text(
                text = "TEXTURE DU BLOC / ITEM (PNG)",
                color = MineGreenAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MineCardBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .border(1.dp, MineWhite.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            .clickable { galleryLauncher.launch("image/png") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (viewModel.formTextureUri != null) {
                            // User selected fresh URI
                            Image(
                                painter = rememberAsyncImagePainter(viewModel.formTextureUri),
                                contentDescription = "Texture sélectionnée",
                                modifier = Modifier.fillMaxSize().padding(10.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else if (viewModel.formTexturePath != null) {
                            // Local safe copy path exists
                            val file = File(viewModel.formTexturePath!!)
                            if (file.exists()) {
                                Image(
                                    painter = rememberAsyncImagePainter(file),
                                    contentDescription = "Texture enregistrée",
                                    modifier = Modifier.fillMaxSize().padding(10.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                EmptyTexturePlaceholder()
                            }
                        } else {
                            EmptyTexturePlaceholder()
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { galleryLauncher.launch("image/png") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MineGreenPrimary.copy(alpha = 0.12f),
                            contentColor = MineGreenPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("select_texture_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sélectionner un fichier PNG",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Note: Si aucun fichier n'est choisi, une texture d'épée en pixel-art par défaut sera générée.",
                        fontSize = 10.sp,
                        color = MineGrey,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save and generate CTA Button
            Button(
                onClick = {
                    viewModel.saveMod(context) {
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_mod_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MineGreenPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = MineDarkBackground,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "GÉNÉRER L'ITEM & SAUVEGARDER",
                    color = MineDarkBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun PresetButton(label: String, value: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(MineDarkBackground, RoundedCornerShape(6.dp))
            .border(1.dp, MineWhite.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = MineGreenAccent,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun EmptyTexturePlaceholder() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.PhotoLibrary,
            contentDescription = null,
            tint = MineGreenAccent.copy(alpha = 0.3f),
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "GALERIE",
            color = MineGrey.copy(alpha = 0.6f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
