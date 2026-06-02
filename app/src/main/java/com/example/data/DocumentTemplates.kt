package com.example.data

object DocumentTemplates {

    val BLANK_TEMPLATE = listOf(
        ParagraphBlock(
            text = "Untitled Document",
            type = ParagraphType.H1,
            alignment = BlockAlignment.LEFT,
            fontSize = 24,
            isBold = true
        ),
        ParagraphBlock(
            text = "Start writing your document here...",
            type = ParagraphType.BODY,
            alignment = BlockAlignment.LEFT,
            fontSize = 16
        )
    )

    val BIODATA_TEMPLATE = listOf(
        ParagraphBlock(
            text = "BIODATA",
            type = ParagraphType.H1,
            alignment = BlockAlignment.CENTER,
            fontSize = 28,
            colorHex = "#2196F3", // Blue theme
            isBold = true
        ),
        ParagraphBlock(
            text = "══════════════════════════════════",
            type = ParagraphType.BODY,
            alignment = BlockAlignment.CENTER,
            fontSize = 14,
            colorHex = "#757575"
        ),
        ParagraphBlock(
            text = "PERSONAL INFORMATION",
            type = ParagraphType.H2,
            alignment = BlockAlignment.LEFT,
            fontSize = 20,
            colorHex = "#0D47A1",
            isBold = true,
            isUnderline = true
        ),
        ParagraphBlock(
            text = "• Full Name: Abhijit Imm",
            type = ParagraphType.BULLET,
            alignment = BlockAlignment.LEFT,
            fontSize = 16,
            isBold = true
        ),
        ParagraphBlock(
            text = "• Date of Birth: 15th April 1996",
            type = ParagraphType.BULLET,
            alignment = BlockAlignment.LEFT,
            fontSize = 16
        ),
        ParagraphBlock(
            text = "• Height: 5 ft 10 inches",
            type = ParagraphType.BULLET,
            alignment = BlockAlignment.LEFT,
            fontSize = 16
        ),
        ParagraphBlock(
            text = "• Marital Status: Single",
            type = ParagraphType.BULLET,
            alignment = BlockAlignment.LEFT,
            fontSize = 16
        ),
        ParagraphBlock(
            text = "• Religion / Caste: Hindu / General",
            type = ParagraphType.BULLET,
            alignment = BlockAlignment.LEFT,
            fontSize = 16
        ),
        ParagraphBlock(
            text = "ACADEMIC & PROFESSIONAL DETAILS",
            type = ParagraphType.H2,
            alignment = BlockAlignment.LEFT,
            fontSize = 20,
            colorHex = "#0D47A1",
            isBold = true,
            isUnderline = true
        ),
        ParagraphBlock(
            text = "• Education: Master of Computer Applications (MCA)",
            type = ParagraphType.BULLET,
            alignment = BlockAlignment.LEFT,
            fontSize = 16
        ),
        ParagraphBlock(
            text = "• Occupation: Lead Software Architect",
            type = ParagraphType.BULLET,
            alignment = BlockAlignment.LEFT,
            fontSize = 16,
            isBold = true
        ),
        ParagraphBlock(
            text = "• Monthly Income: $12,500 / month",
            type = ParagraphType.BULLET,
            alignment = BlockAlignment.LEFT,
            fontSize = 16
        ),
        ParagraphBlock(
            text = "FAMILY BACKGROUND",
            type = ParagraphType.H2,
            alignment = BlockAlignment.LEFT,
            fontSize = 20,
            colorHex = "#0D47A1",
            isBold = true,
            isUnderline = true
        ),
        ParagraphBlock(
            text = "• Father's Name: Dr. Rajesh Imm (Retired Professor)",
            type = ParagraphType.BULLET,
            alignment = BlockAlignment.LEFT,
            fontSize = 16
        ),
        ParagraphBlock(
            text = "• Mother's Name: Smt. Sunita Imm (Homemaker)",
            type = ParagraphType.BULLET,
            alignment = BlockAlignment.LEFT,
            fontSize = 16
        ),
        ParagraphBlock(
            text = "• Siblings: 1 Elder Sister (Married, Pediatrician)",
            type = ParagraphType.BULLET,
            alignment = BlockAlignment.LEFT,
            fontSize = 16
        ),
        ParagraphBlock(
            text = "CONTACT DETAILS",
            type = ParagraphType.H2,
            alignment = BlockAlignment.LEFT,
            fontSize = 20,
            colorHex = "#0D47A1",
            isBold = true,
            isUnderline = true
        ),
        ParagraphBlock(
            text = "• Permanent Address: 21B Oak Avenue, Tech City, 85001",
            type = ParagraphType.BULLET,
            alignment = BlockAlignment.LEFT,
            fontSize = 16
        ),
        ParagraphBlock(
            text = "• Email Address: imm.abhijit@gmail.com",
            type = ParagraphType.BULLET,
            alignment = BlockAlignment.LEFT,
            fontSize = 16
        ),
        ParagraphBlock(
            text = "• Cell Number: +1 (555) 0192-3847",
            type = ParagraphType.BULLET,
            alignment = BlockAlignment.LEFT,
            fontSize = 16
        )
    )

    val LETTER_TEMPLATE = listOf(
        ParagraphBlock(
            text = "SCRIBEFLOW SOLUTIONS LTD",
            type = ParagraphType.H1,
            alignment = BlockAlignment.CENTER,
            fontSize = 26,
            colorHex = "#E91E63", // Pink accent
            isBold = true
        ),
        ParagraphBlock(
            text = "Suite 505, Innovator District, San Jose, California",
            type = ParagraphType.BODY,
            alignment = BlockAlignment.CENTER,
            fontSize = 12,
            colorHex = "#757575",
            isItalic = true
        ),
        ParagraphBlock(
            text = "________________________________________________________",
            type = ParagraphType.BODY,
            alignment = BlockAlignment.CENTER,
            fontSize = 14,
            colorHex = "#BDBDBD"
        ),
        ParagraphBlock(
            text = "Date: June 2, 2026",
            type = ParagraphType.BODY,
            alignment = BlockAlignment.LEFT,
            fontSize = 15
        ),
        ParagraphBlock(
            text = "To,\nMr. David Miller\nManaging Director, AppScale Inc.\n100 Enterprise Way, Floor 7\nBoston, MA 02110",
            type = ParagraphType.BODY,
            alignment = BlockAlignment.LEFT,
            fontSize = 15
        ),
        ParagraphBlock(
            text = "Subject: Proposal for Real-Time Collaborative System Upgrade",
            type = ParagraphType.BODY,
            alignment = BlockAlignment.LEFT,
            fontSize = 15,
            isBold = true,
            isUnderline = true
        ),
        ParagraphBlock(
            text = "Dear Mr. Miller,",
            type = ParagraphType.BODY,
            alignment = BlockAlignment.LEFT,
            fontSize = 16
        ),
        ParagraphBlock(
            text = "Following our recent dialogue regarding Document workflow modernization, I am delighted to formally present our software engineering upgrade proposal.",
            type = ParagraphType.BODY,
            alignment = BlockAlignment.LEFT,
            fontSize = 16
        ),
        ParagraphBlock(
            text = "Our proposed client-side Word Processor engine features military-grade local persistence via Room, zero-latency collaborative simulators, automatic voice transcription, and robust multi-format export to PDF and high-resolution JPEG, operating in fully offline-resilient sandboxes.",
            type = ParagraphType.BODY,
            alignment = BlockAlignment.LEFT,
            fontSize = 16
        ),
        ParagraphBlock(
            text = "We are prepared to commence integration work by next Monday. If this structure meets your administrative approval, kindly sign and return a copy of this correspondence.",
            type = ParagraphType.BODY,
            alignment = BlockAlignment.LEFT,
            fontSize = 16
        ),
        ParagraphBlock(
            text = "Thank you for your partnership and mutual confidence.",
            type = ParagraphType.BODY,
            alignment = BlockAlignment.LEFT,
            fontSize = 16
        ),
        ParagraphBlock(
            text = "Warm regards,\n\n\nAbhijit Imm\nCEO & Foundational Engineer\nScribeFlow Inc.",
            type = ParagraphType.BODY,
            alignment = BlockAlignment.LEFT,
            fontSize = 16
        )
    )

    val APPLICATION_TEMPLATE = listOf(
        ParagraphBlock(
            text = "FORMAL APPLICATION",
            type = ParagraphType.H1,
            alignment = BlockAlignment.CENTER,
            fontSize = 24,
            colorHex = "#3F51B5", // Indigo Core
            isBold = true
        ),
        ParagraphBlock(
            text = "Date: June 2, 2026",
            type = ParagraphType.BODY,
            alignment = BlockAlignment.RIGHT,
            fontSize = 14
        ),
        ParagraphBlock(
            text = "From:\nAbhijit Imm\nSenior Software Architect\nSunnyvale, CA, 94085",
            type = ParagraphType.BODY,
            alignment = BlockAlignment.LEFT,
            fontSize = 15
        ),
        ParagraphBlock(
            text = "To,\nOffice of Academic Registrations\nContinental Institute of Computing\nSeattle, WA, 98101",
            type = ParagraphType.BODY,
            alignment = BlockAlignment.LEFT,
            fontSize = 15
        ),
        ParagraphBlock(
            text = "Subject: Application for Professional Fellowship in Human-Computer Interfaces",
            type = ParagraphType.BODY,
            alignment = BlockAlignment.LEFT,
            fontSize = 15,
            isBold = true,
            isUnderline = true
        ),
        ParagraphBlock(
            text = "Dear members of the Fellowship Committee,",
            type = ParagraphType.BODY,
            alignment = BlockAlignment.LEFT,
            fontSize = 16
        ),
        ParagraphBlock(
            text = "I am writing to formally submit my candidature for the 2026-2027 Professional Fellowship in Human-Computer Interfaces at Continental Institute.",
            type = ParagraphType.BODY,
            alignment = BlockAlignment.LEFT,
            fontSize = 16
        ),
        ParagraphBlock(
            text = "As an advocate of high-fidelity mobile interactions, my recent work on high-performance Rich Text engines has redefined how content designers format structured data. I seek the Fellowship to carry out advanced research on collaborative visual layouts, with focus on low-latency Canvas rendering and dynamic layout engines.",
            type = ParagraphType.BODY,
            alignment = BlockAlignment.LEFT,
            fontSize = 16
        ),
        ParagraphBlock(
            text = "Please find attached my research synopsis, professional credentials, and a highly optimized sample platform for document structure modeling.",
            type = ParagraphType.BODY,
            alignment = BlockAlignment.LEFT,
            fontSize = 16
        ),
        ParagraphBlock(
            text = "Thank you for reviewing my formal bid. I remain at your complete disposal for interviews.",
            type = ParagraphType.BODY,
            alignment = BlockAlignment.LEFT,
            fontSize = 16
        ),
        ParagraphBlock(
            text = "Respectfully and sincerely submitted,",
            type = ParagraphType.BODY,
            alignment = BlockAlignment.LEFT,
            fontSize = 16
        ),
        ParagraphBlock(
            text = "Abhijit Imm\nSenior Android Specialist",
            type = ParagraphType.BODY,
            alignment = BlockAlignment.LEFT,
            fontSize = 16,
            isBold = true
        )
    )
}
