import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

val kotlinVersion = "1.5.20"
val ktorVersion = "1.6.2"

plugins {
    application
    kotlin("multiplatform") version "1.5.20"
    kotlin("plugin.serialization") version "1.5.20"
}

group = "ch.rbarton"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
    maven { url = uri("https://dl.bintray.com/kotlin/kotlin-js-wrappers") }
    maven { url = uri("https://dl.bintray.com/kotlin/kotlinx") }
    maven { url = uri("https://dl.bintray.com/kotlin/ktor") }
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
    }
    js(LEGACY) {
        browser {
            binaries.executable()
            webpackTask {
                cssSupport.enabled = true
            }
            runTask {
                cssSupport.enabled = true
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
                implementation("io.ktor:ktor-client-websockets:$ktorVersion")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-netty:$ktorVersion")
                implementation("io.ktor:ktor-html-builder:$ktorVersion")
                implementation("io.ktor:ktor-websockets:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.3")
                implementation("ch.qos.logback:logback-classic:1.2.3")

                // CLI tool
                implementation("io.ktor:ktor-client-websockets:$ktorVersion")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:$ktorVersion") //includes http&websockets
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.231-kotlin-1.5.21")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:17.0.2-pre.231-kotlin-1.5.21")

                implementation(npm("react", "17.0.2"))
                implementation(npm("react-dom", "17.0.2"))
                implementation(npm("@types/react", "17.0.2"))
                implementation(npm("react-is", "17.0.2"))
                implementation(npm("react-markdown", "7.0.0"))

                implementation("org.jetbrains.kotlin-wrappers:kotlin-styled:5.3.0-pre.231-kotlin-1.5.21")
                implementation(npm("styled-components", "~5.3.0"))
                implementation(npm("inline-style-prefixer", "~6.0.0"))

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1-native-mt")
            }
        }
    }
}

application {
    applicationName = "server"
    mainClassName = "server.ServerKt"
}

val cliTask = tasks.register("cli", CreateStartScripts::class){
    applicationName = "cli"
    mainClassName = "cli.CliKt"
    dependsOn(tasks.getByName<Jar>("jvmJar"))
    outputDir = file("build/scripts")
}


tasks.getByName<KotlinWebpack>("jsBrowserDevelopmentWebpack") {
    outputFileName = "client.js"
}

tasks.getByName<Jar>("jvmJar") {
    dependsOn(tasks.getByName("jsBrowserDevelopmentWebpack"))
    val jsBrowserProductionWebpack = tasks.getByName<KotlinWebpack>("jsBrowserDevelopmentWebpack")
    from(File(jsBrowserProductionWebpack.destinationDirectory, jsBrowserProductionWebpack.outputFileName))
}

tasks.getByName<JavaExec>("run") {
    dependsOn(tasks.getByName<Jar>("jvmJar"))
    classpath(tasks.getByName<Jar>("jvmJar"))
}