plugins {
    id("pl.allegro.tech.build.axion-release") version "1.14.3"
    id("java")
    id("java-gradle-plugin")

    alias(libs.plugins.shadow) apply true
    alias(libs.plugins.runVelocity)
}

group = "com.mattmx.reconnect"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven{    
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly(libs.velocity.api)
    annotationProcessor(libs.velocity.api)

    compileOnly(libs.litebans.api)
    compileOnly(libs.luckperms.api)

    implementation(libs.storage.yaml)
    implementation(libs.storage.mysql)
    implementation(libs.storage.maria)
    implementation(libs.storage.sqlite)
    implementation(libs.storage.postgresql)
    implementation(libs.storage.hikari)
}

sourceSets["main"].resources.srcDir("src/resources/")

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

scmVersion {
    tag {
        prefix = "v"
    }
}

// Directory where the processed template will go
val generatedSrcDir = file("$buildDir/generated/sources/reconnect")

val processVersionTemplate by tasks.registering(Copy::class) {
    val version = scmVersion.version
    from("src/main/java/com/mattmx/reconnect/ReconnectVersion.java.template")
    into(generatedSrcDir.resolve("com/mattmx/reconnect"))
    expand("version" to version)
    rename { fileName -> fileName.replace(".template", "") }
}

sourceSets["main"].java.srcDir("$generatedSrcDir")
tasks.named<JavaCompile>("compileJava") {
    dependsOn(processVersionTemplate)
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mergeServiceFiles()
    }
    runVelocity {
        velocityVersion(libs.versions.velocity.get())
    }
}