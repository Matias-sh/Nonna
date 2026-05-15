// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.navigation.safe.args) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
}

import org.gradle.api.GradleException

tasks.register("checkUiHardcodedStrings") {
    group = "verification"
    description = "Fails when user-facing literals bypass string resources."

    doLast {
        val scanRoots = listOf(
            rootDir.resolve("app/src/main/java/com/cocido/nonna/ui"),
            rootDir.resolve("app/src/main/java/com/cocido/nonna/util")
        ).filter { it.exists() }

        if (scanRoots.isEmpty()) return@doLast

        val ignoredFiles = setOf(
            "AppShell.kt",
            "NonnaButton.kt",
            "NonnaMotion.kt",
            "FamilyTreeScreen.kt",
            "CofresListScreen.kt",
            "UserMessages.kt"
        )

        val assignmentRegex = Regex("""\b(text|placeholder|contentDescription|title)\s*=\s*"([^"]+)"""")
        val directTextRegex = Regex("""\bText\(\s*"([^"]+)"""")
        val throwableRegex = Regex("""\bthrow\s+\w+\s*\(\s*"([^"]+)"""")
        val previewBlockRegex = Regex("""@Preview[\s\S]*?(?=@Preview|$)""")

        val findings = mutableListOf<String>()

        scanRoots.forEach { scanRoot ->
            scanRoot.walkTopDown()
                .filter { it.isFile && it.extension == "kt" && it.name !in ignoredFiles }
                .forEach { file ->
                    val original = file.readText()
                    val withoutPreviewBlocks = original.replace(previewBlockRegex, "")
                    val lines = withoutPreviewBlocks.lines()

                    lines.forEachIndexed { index, line ->
                        if (line.contains("i18n-ignore")) return@forEachIndexed
                        val isUiLiteral = assignmentRegex.containsMatchIn(line) || directTextRegex.containsMatchIn(line)
                        val isThrowableLiteral = throwableRegex.containsMatchIn(line)
                        if (isUiLiteral || isThrowableLiteral) {
                            findings += "${file.relativeTo(rootDir).path}:${index + 1}: ${line.trim()}"
                        }
                    }
                }
        }

        if (findings.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("Hardcoded user-facing strings detected. Use string resources.")
                    appendLine("If an exception is intentional, add // i18n-ignore in that line.")
                    appendLine()
                    findings.forEach { appendLine("- $it") }
                }
            )
        }
    }
}