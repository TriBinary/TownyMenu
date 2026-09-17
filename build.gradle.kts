plugins {
    kotlin("jvm") version "2.3.10"
    idea
}

group = "net.trilleo.mc.plugins"
version = providers.gradleProperty("plugin_version").get()

idea {
    module {
        isDownloadSources = true
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://repo.glaremasters.me/repository/towny/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    compileOnly("com.palmergames.bukkit.towny:towny:${providers.gradleProperty("towny_version").get()}")
    testImplementation(kotlin("test"))
    testImplementation("net.kyori:adventure-api:5.2.0")
    testImplementation("net.kyori:adventure-text-minimessage:5.2.0")
    testImplementation("net.kyori:adventure-text-serializer-plain:5.2.0")
    testImplementation("org.yaml:snakeyaml:2.2")
}

kotlin {
    jvmToolchain(25)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    val props = mapOf("projectVersion" to version)
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.jar {
    from(configurations.runtimeClasspath.get().map { zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["paperweight-mappings-namespace"] = "spigot"
    }
}

tasks.register<Copy>("copyPlugin") {
    dependsOn("jar")
    from(tasks.jar.get().archiveFile)
    into(layout.projectDirectory.dir("run/plugins"))
}

tasks.register<JavaExec>("startServer") {
    dependsOn("copyPlugin")
    workingDir(layout.projectDirectory.dir("run"))
    classpath(fileTree(layout.projectDirectory.dir("run")) { include("paper-*.jar") })
}
