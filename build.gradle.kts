import org.gradle.api.attributes.java.TargetJvmVersion

plugins {
    java
    id("com.gradleup.shadow") version "8.3.6"
}

group = property("group") as String
version = property("version") as String

// Paper 26.3 dependencies are Java 25 bytecode; the plugin remains Java 21.
val paperApiVersion = providers.gradleProperty("paperApiVersion")
    .orElse("26.3.build.157-beta")
    .get()

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    // Request Java 25 dependency variants while JavaCompile emits release 21.
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    withSourcesJar()
}

// Select Paper's Java 25 variant even though JavaCompile emits release 21.
configurations.configureEach {
    if (isCanBeResolved) {
        attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
    compileOnly("com.ticxo.modelengine:ModelEngine:R4.1.0") {
        isTransitive = false
    }
    compileOnly("io.lumine:Mythic-Dist:5.13.0-SNAPSHOT") {
        isTransitive = false
    }

    implementation("gg.moonflower:molang-compiler:3.1.1.19")
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("io.papermc.paper:paper-api:$paperApiVersion")
    testRuntimeOnly("com.ticxo.modelengine:ModelEngine:R4.1.0") {
        isTransitive = false
    }
    testCompileOnly("io.papermc.paper:paper-api:$paperApiVersion")
    testCompileOnly("com.ticxo.modelengine:ModelEngine:R4.1.0") {
        isTransitive = false
    }
    testCompileOnly("io.lumine:Mythic-Dist:5.13.0-SNAPSHOT") {
        isTransitive = false
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("YsmModelEngineMolang-${project.version}.jar")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
