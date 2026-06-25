package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.example.data.MineMod
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object McAddonGenerator {
    private const val TAG = "McAddonGenerator"

    fun generateMcAddon(context: Context, mod: MineMod): File? {
        try {
            // 1. Create a root temp folder for this compilation
            val workDir = File(context.cacheDir, "addon_build_${mod.id}_${System.currentTimeMillis()}")
            if (workDir.exists()) workDir.deleteRecursively()
            workDir.mkdirs()

            // 2. Generate UUIDs for BP and RP manifest pairing
            val bpUuid = UUID.randomUUID().toString()
            val bpModuleUuid = UUID.randomUUID().toString()
            val rpUuid = UUID.randomUUID().toString()
            val rpModuleUuid = UUID.randomUUID().toString()

            val modName = mod.name
            val identifier = mod.identifier
            val shortName = mod.shortName
            val damage = mod.damage
            val durability = mod.durability

            // Create folders
            val bpDir = File(workDir, "${shortName}_BP")
            val rpDir = File(workDir, "${shortName}_RP")
            bpDir.mkdirs()
            rpDir.mkdirs()

            // --- BEHAVIOR PACK GENERATION ---
            // manifest.json
            val bpManifest = """
                {
                  "format_version": 2,
                  "header": {
                    "description": "Behavior Pack for $modName",
                    "name": "$modName BP",
                    "uuid": "$bpUuid",
                    "version": [1, 0, 0],
                    "min_engine_version": [1, 16, 0]
                  },
                  "modules": [
                    {
                      "description": "Behavior Pack Module",
                      "type": "data",
                      "uuid": "$bpModuleUuid",
                      "version": [1, 0, 0]
                    }
                  ]
                }
            """.trimIndent()
            File(bpDir, "manifest.json").writeText(bpManifest)

            // items/<shortName>.json
            val itemsDir = File(bpDir, "items")
            itemsDir.mkdirs()
            val itemJson = """
                {
                  "format_version": "1.16.100",
                  "minecraft:item": {
                    "description": {
                      "identifier": "$identifier",
                      "category": "Equipment"
                    },
                    "components": {
                      "minecraft:icon": {
                        "texture": "$shortName"
                      },
                      "minecraft:display_name": {
                        "value": "item.$identifier.name"
                      },
                      "minecraft:hand_equipped": true,
                      "minecraft:max_stack_size": 1,
                      "minecraft:damage": $damage,
                      "minecraft:durability": {
                        "max_durability": $durability
                      }
                    }
                  }
                }
            """.trimIndent()
            File(itemsDir, "$shortName.json").writeText(itemJson)

            // --- RESOURCE PACK GENERATION ---
            // manifest.json
            val rpManifest = """
                {
                  "format_version": 2,
                  "header": {
                    "description": "Resource Pack for $modName",
                    "name": "$modName RP",
                    "uuid": "$rpUuid",
                    "version": [1, 0, 0],
                    "min_engine_version": [1, 16, 0]
                  },
                  "modules": [
                    {
                      "description": "Resource Pack Module",
                      "type": "resources",
                      "uuid": "$rpModuleUuid",
                      "version": [1, 0, 0]
                    }
                  ],
                  "dependencies": [
                    {
                      "uuid": "$bpUuid",
                      "version": [1, 0, 0]
                    }
                  ]
                }
            """.trimIndent()
            File(rpDir, "manifest.json").writeText(rpManifest)

            // textures/item_texture.json
            val texturesDir = File(rpDir, "textures")
            texturesDir.mkdirs()
            val itemTextureJson = """
                {
                  "resource_pack_name": "vanilla",
                  "texture_name": "atlas.items",
                  "texture_data": {
                    "$shortName": {
                      "textures": "textures/items/$shortName"
                    }
                  }
                }
            """.trimIndent()
            File(texturesDir, "item_texture.json").writeText(itemTextureJson)

            // textures/items/<shortName>.png
            val rpItemsImgDir = File(texturesDir, "items")
            rpItemsImgDir.mkdirs()
            val destImgFile = File(rpItemsImgDir, "$shortName.png")

            if (mod.texturePath != null) {
                val srcFile = File(mod.texturePath)
                if (srcFile.exists()) {
                    srcFile.copyTo(destImgFile, overwrite = true)
                } else {
                    writeDefaultTexture(destImgFile)
                }
            } else {
                writeDefaultTexture(destImgFile)
            }

            // texts/en_US.lang
            val textsDir = File(rpDir, "texts")
            textsDir.mkdirs()
            val langContent = "item.$identifier.name=$modName"
            File(textsDir, "en_US.lang").writeText(langContent)

            // 3. Zip workDir contents into .mcaddon
            val outputZipFile = File(context.cacheDir, "${shortName}_v1.0.mcaddon")
            if (outputZipFile.exists()) outputZipFile.delete()

            ZipOutputStream(FileOutputStream(outputZipFile)).use { zipOut ->
                // Add BP recursively
                zipDirectory(bpDir, "${shortName}_BP", zipOut)
                // Add RP recursively
                zipDirectory(rpDir, "${shortName}_RP", zipOut)
            }

            // Cleanup workDir
            workDir.deleteRecursively()

            return outputZipFile
        } catch (e: Exception) {
            Log.e(TAG, "Error generating mcaddon", e)
            return null
        }
    }

    private fun zipDirectory(dir: File, baseName: String, zipOut: ZipOutputStream) {
        dir.listFiles()?.forEach { file ->
            val entryName = "$baseName/${file.name}"
            if (file.isDirectory) {
                zipDirectory(file, entryName, zipOut)
            } else {
                zipOut.putNextEntry(ZipEntry(entryName))
                FileInputStream(file).use { input ->
                    input.copyTo(zipOut)
                }
                zipOut.closeEntry()
            }
        }
    }

    private fun writeDefaultTexture(destFile: File) {
        try {
            val bitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint()

            // Draw a dark wood guard and cool pixel sword diagonal in a 16x16 canvas
            for (x in 0 until 16) {
                for (y in 0 until 16) {
                    val invY = 15 - y
                    // Sword diagonal blade
                    if (x == invY || x == invY + 1 || x == invY - 1) {
                        if (x < 4) {
                            // Leather/Wood Handle (brown)
                            paint.color = Color.parseColor("#5C4033")
                        } else if (x == 4 || x == 5) {
                            // Golden Guard (gold)
                            paint.color = Color.parseColor("#FFD700")
                        } else {
                            // Iron Blade (gray/blue highlight)
                            paint.color = if (x % 2 == 0) Color.parseColor("#D3D3D3") else Color.parseColor("#B0C4DE")
                        }
                        canvas.drawPoint(x.toFloat(), y.toFloat(), paint)
                    }
                }
            }

            FileOutputStream(destFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            bitmap.recycle()
        } catch (e: Exception) {
            Log.e(TAG, "Error generating default texture", e)
        }
    }
}
