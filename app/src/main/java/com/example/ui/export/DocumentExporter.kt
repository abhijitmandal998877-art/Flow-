package com.example.ui.export

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.BlockAlignment
import com.example.data.ParagraphBlock
import com.example.data.ParagraphType
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object DocumentExporter {

    private const val PAGE_WIDTH = 595 // A4 standard width (points)
    private const val PAGE_HEIGHT = 842 // A4 standard height (points)
    private const val MARGIN_LEFT = 50f
    private const val MARGIN_RIGHT = 50f
    private const val MARGIN_TOP = 60f
    private const val MARGIN_BOTTOM = 60f
    private const val USABLE_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT

    // Model for storing drawn text layout to support multiline text wrapped properly
    private data class WrappedLine(
        val text: String,
        val textPaint: TextPaint,
        val x: Float,
        val alignment: BlockAlignment
    )

    // Layout the blocks and return list of pages, where each page contains list of lines to draw
    private fun layoutDocument(blocks: List<ParagraphBlock>): List<List<Pair<WrappedLine, Float>>> {
        val pages = mutableListOf<MutableList<Pair<WrappedLine, Float>>>()
        var currentPage = mutableListOf<Pair<WrappedLine, Float>>()
        var yPosition = MARGIN_TOP

        for (block in blocks) {
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = (block.fontSize * 0.9f).coerceAtLeast(10f) // Scale font relative to PDF DPI
                color = try {
                    Color.parseColor(block.colorHex)
                } catch (e: Exception) {
                    Color.BLACK
                }
                
                // Style typeface
                var style = Typeface.NORMAL
                if (block.isBold && block.isItalic) style = Typeface.BOLD_ITALIC
                else if (block.isBold) style = Typeface.BOLD
                else if (block.isItalic) style = Typeface.ITALIC

                typeface = Typeface.create(
                    when (block.type) {
                        ParagraphType.H1, ParagraphType.H2, ParagraphType.H3 -> Typeface.DEFAULT_BOLD
                        ParagraphType.QUOTE -> Typeface.SERIF
                        else -> Typeface.DEFAULT
                    },
                    style
                )
                
                isUnderlineText = block.isUnderline
            }

            // Paragraph spacing
            val topSpacing = when (block.type) {
                ParagraphType.H1 -> 24f
                ParagraphType.H2 -> 18f
                ParagraphType.H3 -> 14f
                else -> 8f
            }
            yPosition += topSpacing

            // Add bullets/numbers if needed
            val bulletPrefix = when (block.type) {
                ParagraphType.BULLET -> "• "
                ParagraphType.NUMBER -> "1. " // Simplification
                else -> ""
            }

            val textToWrap = bulletPrefix + block.text
            val wrappedLines = wrapText(textToWrap, paint, USABLE_WIDTH)

            for (lineText in wrappedLines) {
                // If we exceed usable height, create a new page
                if (yPosition + paint.fontSpacing > PAGE_HEIGHT - MARGIN_BOTTOM) {
                    pages.add(currentPage)
                    currentPage = mutableListOf()
                    yPosition = MARGIN_TOP
                }

                // Horizontal Alignment position
                val lineLength = paint.measureText(lineText)
                val xPosition = when (block.alignment) {
                    BlockAlignment.LEFT -> MARGIN_LEFT
                    BlockAlignment.CENTER -> MARGIN_LEFT + (USABLE_WIDTH - lineLength) / 2
                    BlockAlignment.RIGHT -> PAGE_WIDTH - MARGIN_RIGHT - lineLength
                    BlockAlignment.JUSTIFY -> MARGIN_LEFT
                }

                val wrappedLine = WrappedLine(lineText, paint, xPosition, block.alignment)
                currentPage.add(Pair(wrappedLine, yPosition))
                yPosition += paint.fontSpacing + 2f // line spacing
            }
            yPosition += 4f // paragraph split spacing
        }

        if (currentPage.isNotEmpty()) {
            pages.add(currentPage)
        }
        return pages
    }

    private fun wrapText(text: String, paint: TextPaint, maxWidth: Float): List<String> {
        if (text.isEmpty()) return listOf("")
        
        // Split by newlines first
        val paragraphs = text.split("\n")
        val allLines = mutableListOf<String>()

        for (para in paragraphs) {
            val words = para.split(" ")
            var currentLine = ""

            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                val testWidth = paint.measureText(testLine)
                if (testWidth <= maxWidth) {
                    currentLine = testLine
                } else {
                    if (currentLine.isNotEmpty()) {
                        allLines.add(currentLine)
                    }
                    currentLine = word
                }
            }
            if (currentLine.isNotEmpty()) {
                allLines.add(currentLine)
            }
        }
        return allLines
    }

    // Export styled blocks to PDF, return exported File
    fun exportToPdf(context: Context, docTitle: String, blocks: List<ParagraphBlock>): File? {
        try {
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "ScribeFlow_${docTitle.lowercase().replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
            )

            val pdfDocument = PdfDocument()
            val layedOutPages = layoutDocument(blocks)

            for ((pageIdx, elements) in layedOutPages.withIndex()) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIdx + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                // Background
                canvas.drawColor(Color.WHITE)

                // Margin guides (very clean design, subtle layout lines)
                val marginPaint = Paint().apply {
                    color = Color.parseColor("#E0E0E0")
                    strokeWidth = 0.5f
                    style = Paint.Style.STROKE
                }
                // Light bounding box for aesthetics representing paper edge
                canvas.drawRect(
                    MARGIN_LEFT - 10f,
                    MARGIN_TOP - 15f,
                    PAGE_WIDTH - MARGIN_RIGHT + 10f,
                    PAGE_HEIGHT - MARGIN_BOTTOM + 15f,
                    marginPaint
                )

                // Draw Elements
                for ((line, yPos) in elements) {
                    canvas.drawText(line.text, line.x, yPos, line.textPaint)
                }

                // Page number footer
                val footerPaint = Paint().apply {
                    color = Color.GRAY
                    textSize = 9f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                }
                val footerText = "Page ${pageIdx + 1} of ${layedOutPages.size} | Powered by ScribeFlow"
                val textWidth = footerPaint.measureText(footerText)
                canvas.drawText(
                    footerText,
                    (PAGE_WIDTH - textWidth) / 2,
                    PAGE_HEIGHT - MARGIN_BOTTOM + 35f,
                    footerPaint
                )

                pdfDocument.finishPage(page)
            }

            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Export PDF Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
        return null
    }

    // Export styled blocks to high-resolution JPEG, return File
    fun exportToJpg(context: Context, docTitle: String, blocks: List<ParagraphBlock>): File? {
        try {
            // Generate single composite page image representing the entire Document top-half, or first page.
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "ScribeFlow_${docTitle.lowercase().replace(" ", "_")}_${System.currentTimeMillis()}.jpg"
            )

            val layedOutPages = layoutDocument(blocks)
            if (layedOutPages.isEmpty()) return null
            
            // We'll export the first page or create a vertical scroll canvas. Let's export the first page at high-res (2x multiplier)
            val scale = 2.0f
            val width = (PAGE_WIDTH * scale).toInt()
            val height = (PAGE_HEIGHT * scale).toInt()

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.scale(scale, scale)

            // Canvas drawing background
            canvas.drawColor(Color.WHITE)

            // Draw border
            val borderPaint = Paint().apply {
                color = Color.parseColor("#CCCCCC")
                strokeWidth = 1f
                style = Paint.Style.STROKE
            }
            canvas.drawRect(
                MARGIN_LEFT - 10f,
                MARGIN_TOP - 15f,
                PAGE_WIDTH - MARGIN_RIGHT + 10f,
                PAGE_HEIGHT - MARGIN_BOTTOM + 15f,
                borderPaint
            )

            // Draw elements on Page 1
            val elements = layedOutPages[0]
            for ((line, yPos) in elements) {
                canvas.drawText(line.text, line.x, yPos, line.textPaint)
            }

            // Draw Footer
            val footerPaint = Paint().apply {
                color = Color.GRAY
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            }
            val footerText = "Page 1 of ${layedOutPages.size} (JPEG Export) | ScribeFlow Word Processor"
            val textWidth = footerPaint.measureText(footerText)
            canvas.drawText(
                footerText,
                (PAGE_WIDTH - textWidth) / 2,
                PAGE_HEIGHT - MARGIN_BOTTOM + 35f,
                footerPaint
            )

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Export JPG Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
        return null
    }

    // Share File Helper using FileProvider
    fun shareExportedFile(context: Context, file: File, mimeType: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Exported ScribeFlow Document: ${file.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Document via:"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error sharing file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
