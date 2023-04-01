import java.nio.charset.StandardCharsets
import java.util.*

plugins {
	id("java")
	id("fabric-loom") version "1.1-SNAPSHOT"
	id("maven-publish")
}

group = property("maven_group")!!
version = property("mod_version")!!

repositories {
	mavenCentral()
	maven(url = "https://maven.terraformersmc.com/releases")
	maven(url = "https://api.modrinth.com/maven") { name = "Modrinth" }
	maven(url = "https://repo.papermc.io/repository/maven-public") { name = "papermc" }
}

dependencies {
	minecraft("com.mojang:minecraft:${property("minecraft_version")}")
	mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
	modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

	modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
//	modImplementation("com.terraformersmc:modmenu:${property("mod_menu_version")}")

	modRuntimeOnly("maven.modrinth:lazydfu:0.1.3")
	modRuntimeOnly("maven.modrinth:smoothboot-fabric:1.19.4-1.7.0")
	modRuntimeOnly("maven.modrinth:mixintrace:1.1.1+1.17")
	modRuntimeOnly("maven.modrinth:notenoughcrashes:4.4.0+1.19.3-fabric")
}

loom {
	sourceSets["main"].resources.files
			.find { file -> file.name.endsWith(".accesswidener") }
			.let(accessWidenerPath::set)

	@Suppress("UnstableApiUsage")
	mixin {
		defaultRefmapName.set("fabroxy.refmap.json")
	}

	runs {
		val commonVmArgs = arrayOf(
				"-Dfabric.systemLibraries=${System.getProperty("java.home")}/lib/hotswap/hotswap-agent.jar",
				"-XX:+AllowEnhancedClassRedefinition",
				"-XX:HotswapAgent=fatjar",
				"-javaagent:\"${
					Scanner(file(".gradle/loom-cache/remapClasspath.txt"), StandardCharsets.UTF_8).let {
						it.useDelimiter(File.pathSeparator)
						while (it.hasNext()) {
							val next = it.next()
							if (next.contains("net.fabricmc${File.separator}sponge-mixin")) {
								it.close()
								return@let next
							}
						}
					}
				}\""
		)

		getByName("client") {
			configName = "Minecraft Client"
			runDir = "run/client"
			vmArgs(*commonVmArgs)
			client()
		}

		getByName("server") {
			configName = "Minecraft Server"
			runDir = "run/server"
			vmArgs(*commonVmArgs)
			server()
		}
	}
}

val targetJavaVersion = JavaVersion.toVersion(property("target_java_version")!!)
java {
	targetCompatibility = targetJavaVersion
	sourceCompatibility = targetJavaVersion
	if (JavaVersion.current() < targetJavaVersion)
		toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion.majorVersion.toInt()))
}

tasks {
	compileJava {
		if (targetJavaVersion.majorVersion.toInt() >= 10 || JavaVersion.current().isJava10Compatible)
			options.release.set(targetJavaVersion.majorVersion.toInt())
		this.options.encoding = "UTF-8"
	}

	processResources {
		inputs.property("version", project.version)

		filesMatching("fabric.mod.json") {
			expand("version" to project.version)
		}
	}

	jar {
		from("LICENSE")
		archiveBaseName.set("${project.property("mod_name")}")
		archiveAppendix.set("fabric-${project.property("minecraft_version")}")
	}

	remapJar {
		archiveBaseName.set("${project.property("mod_name")}")
		archiveAppendix.set("fabric-${project.property("minecraft_version")}")
	}
}

// configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
	}
}
