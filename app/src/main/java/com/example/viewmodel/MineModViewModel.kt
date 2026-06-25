package com.example.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.MineMod
import com.example.data.MineModRepository
import com.example.utils.McAddonGenerator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MineModViewModel(private val repository: MineModRepository) : ViewModel() {
    private val TAG = "MineModViewModel"

    // Expose mod list reactive flow
    val mods: StateFlow<List<MineMod>> = repository.allMods
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Form fields state
    var formName by mutableStateOf("")
        private set
    var formIdentifier by mutableStateOf("")
        private set
    var formDamage by mutableStateOf(5)
        private set
    var formDurability by mutableStateOf(250)
        private set
    var formTextureUri by mutableStateOf<Uri?>(null)
        private set
    var formTexturePath by mutableStateOf<String?>(null)
        private set

    // Track if we are editing an existing mod (holds its ID, or null for new)
    var editingModId by mutableStateOf<Int?>(null)
        private set

    // Automatically suggest ID flag
    private var autoSuggestId = true

    fun onNameChange(newName: String) {
        formName = newName
        if (autoSuggestId) {
            formIdentifier = suggestIdentifier(newName)
        }
    }

    fun onIdentifierChange(newId: String) {
        formIdentifier = newId
        // If they manually edit the ID, stop auto-suggesting
        autoSuggestId = false
    }

    fun onDamageChange(newDamage: Int) {
        formDamage = newDamage
    }

    fun onDurabilityChange(newDurability: Int) {
        formDurability = newDurability
    }

    fun onTextureSelected(uri: Uri?) {
        formTextureUri = uri
    }

    fun resetForm(mod: MineMod? = null) {
        if (mod == null) {
            formName = ""
            formIdentifier = "mymod:new_item"
            formDamage = 5
            formDurability = 250
            formTextureUri = null
            formTexturePath = null
            editingModId = null
            autoSuggestId = true
        } else {
            formName = mod.name
            formIdentifier = mod.identifier
            formDamage = mod.damage
            formDurability = mod.durability
            formTextureUri = null
            formTexturePath = mod.texturePath
            editingModId = mod.id
            autoSuggestId = false
        }
    }

    fun saveMod(context: Context, onSuccess: () -> Unit) {
        if (formName.isBlank()) {
            Toast.makeText(context, "Veuillez entrer un nom", Toast.LENGTH_SHORT).show()
            return
        }

        val idPattern = Regex("^[a-z0-9_]+:[a-z0-9_]+$")
        var finalId = formIdentifier.trim().lowercase()
        if (!finalId.contains(":")) {
            finalId = "mymod:$finalId"
        }

        if (!idPattern.matches(finalId)) {
            Toast.makeText(context, "ID invalide (format requis: namespace:nom_item)", Toast.LENGTH_LONG).show()
            return
        }

        viewModelScope.launch {
            try {
                var finalTexturePath = formTexturePath

                // If user picked a new gallery image, copy it to app's safe storage
                formTextureUri?.let { uri ->
                    val shortName = finalId.substringAfter(":")
                    finalTexturePath = copyUriToInternalStorage(context, uri, shortName)
                }

                val mod = MineMod(
                    id = editingModId ?: 0,
                    name = formName.trim(),
                    identifier = finalId,
                    damage = formDamage,
                    durability = formDurability,
                    texturePath = finalTexturePath
                )

                repository.insert(mod)
                Toast.makeText(context, "Mod sauvegardé !", Toast.LENGTH_SHORT).show()
                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Error saving mod", e)
                Toast.makeText(context, "Erreur de sauvegarde", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteMod(modId: Int, context: Context) {
        viewModelScope.launch {
            try {
                repository.deleteById(modId)
                Toast.makeText(context, "Mod supprimé", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting mod", e)
                Toast.makeText(context, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun shareMod(context: Context, mod: MineMod) {
        viewModelScope.launch {
            try {
                // Generate the .mcaddon file
                val file = McAddonGenerator.generateMcAddon(context, mod)
                if (file != null && file.exists()) {
                    // Get URI via FileProvider
                    val uri: Uri = FileProvider.getUriForFile(
                        context,
                        "com.aistudio.mineaddon.fileprovider",
                        file
                    )

                    // Intent to Share
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/octet-stream"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "Import Minecraft Add-on: ${mod.name}")
                        putExtra(Intent.EXTRA_TEXT, "Voici votre Add-on Minecraft Bedrock personnalisé: ${mod.name}!")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    // Create chooser
                    val chooser = Intent.createChooser(shareIntent, "Exporter et Installer votre Mod").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(chooser)
                } else {
                    Toast.makeText(context, "Erreur lors de la compilation de l'addon", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sharing mod", e)
                Toast.makeText(context, "Erreur lors de l'exportation", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun copyUriToInternalStorage(context: Context, uri: Uri, nameKey: String): String? {
        return try {
            val texturesDir = File(context.filesDir, "textures")
            if (!texturesDir.exists()) texturesDir.mkdirs()

            val destFile = File(texturesDir, "${nameKey}_${System.currentTimeMillis()}.png")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy texture", e)
            null
        }
    }

    private fun suggestIdentifier(name: String): String {
        val clean = name.lowercase()
            .replace(Regex("[éèêë]"), "e")
            .replace(Regex("[àâä]"), "a")
            .replace(Regex("[ùûü]"), "u")
            .replace(Regex("[ôö]"), "o")
            .replace(Regex("[ç]"), "c")
            .replace(Regex("[^a-z0-9\\s_]"), "")
            .trim()
            .replace(Regex("\\s+"), "_")
        return if (clean.isEmpty()) "mymod:item" else "mymod:$clean"
    }
}

// Simple Factory to build ViewModel with Repository constructor injection
class MineModViewModelFactory(private val repository: MineModRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MineModViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MineModViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
