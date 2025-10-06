import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

// build.gradle.kts: IntelliJプラットフォーム向けプラグインのビルドと検証設定を集約する。
val platformTypeNameProvider = providers.gradleProperty("platformType")
    .orElse("IntellijIdeaCommunity")
val platformVersionProvider = providers.gradleProperty("platformVersion")
    .orElse("2024.3")
val platformType = platformTypeNameProvider.map(IntelliJPlatformType::valueOf).get()
val platformVersion = platformVersionProvider.get()

group = "io.github.poruru210.methodinsight"

plugins {
    kotlin("jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.9.0"
    id("com.palantir.git-version") version "4.0.0"
}

val gitVersion: groovy.lang.Closure<String> by extra
version = runCatching { gitVersion() }.getOrElse { "0.0.0-local" }

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    intellijPlatform {
        create(platformType, platformVersion)
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.kotlin")
    }
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    pluginVerification {
        ides {
            // verifyPlugin実行時にCIと同じIDEビルドで検証する。
            create(platformType, platformVersion)
        }
    }
}

tasks {
    patchPluginXml {
        // plugin.xmlにプロジェクト版数を統一的に反映する。
        version = project.version.toString()
        sinceBuild.set("243")
        pluginDescription.set(
            """
            Generate a single Markdown sequence report that combines a Mermaid call graph and the tests referencing the selected method. Invoke the action from the editor context menu to inspect calls, coverage, and source in one place.
            """.trimIndent()
        )
    }
    test {
        useJUnitPlatform()
    }
    instrumentCode {
        enabled = false
    }
    instrumentTestCode {
        enabled = false
    }
}
