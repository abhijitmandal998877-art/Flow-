package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class ParagraphType {
    H1, H2, H3, BODY, BULLET, NUMBER, QUOTE
}

enum class BlockAlignment {
    LEFT, CENTER, RIGHT, JUSTIFY
}

data class ParagraphBlock(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val type: ParagraphType = ParagraphType.BODY,
    val alignment: BlockAlignment = BlockAlignment.LEFT,
    val fontSize: Int = 16,
    val colorHex: String = "#1D1B20", // Default body text color
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false
) {
    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("text", text)
        obj.put("type", type.name)
        obj.put("alignment", alignment.name)
        obj.put("fontSize", fontSize)
        obj.put("colorHex", colorHex)
        obj.put("isBold", isBold)
        obj.put("isItalic", isItalic)
        obj.put("isUnderline", isUnderline)
        return obj
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): ParagraphBlock {
            return ParagraphBlock(
                id = obj.optString("id", UUID.randomUUID().toString()),
                text = obj.optString("text", ""),
                type = ParagraphType.valueOf(obj.optString("type", ParagraphType.BODY.name)),
                alignment = BlockAlignment.valueOf(obj.optString("alignment", BlockAlignment.LEFT.name)),
                fontSize = obj.optInt("fontSize", 16),
                colorHex = obj.optString("colorHex", "#1D1B20"),
                isBold = obj.optBoolean("isBold", false),
                isItalic = obj.optBoolean("isItalic", false),
                isUnderline = obj.optBoolean("isUnderline", false)
            )
        }
    }
}

fun List<ParagraphBlock>.toJsonString(): String {
    val array = JSONArray()
    for (block in this) {
        array.put(block.toJsonObject())
    }
    return array.toString()
}

fun String.toParagraphBlocks(): List<ParagraphBlock> {
    if (this.isEmpty()) return emptyList()
    val list = mutableListOf<ParagraphBlock>()
    try {
        val array = JSONArray(this)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(ParagraphBlock.fromJsonObject(obj))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // "General", "Biodata", "Letter", "Application"
    val templateType: String, // "Blank", "Biodata", "Letter", "Application"
    val blocksJson: String, // Serialized List<ParagraphBlock>
    val plainText: String, // Plain text for search
    val createdAt: Long = System.currentTimeMillis(),
    val lastModifiedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val collaborationEnabled: Boolean = false
)
