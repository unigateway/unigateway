import com.github.gradle.node.npm.task.NpmTask
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  groovy
  kotlin("jvm") version libs.versions.kotlin
  kotlin("kapt") version libs.versions.kotlin
  kotlin("plugin.allopen") version libs.versions.kotlin
  kotlin("plugin.serialization") version libs.versions.kotlin
  id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
  id("com.github.johnrengelman.shadow") version "7.1.2"
  id("pl.allegro.tech.build.axion-release") version "1.14.2"
  id("io.micronaut.application") version "3.2.1"
  id("com.github.node-gradle.node") version "7.1.0"
}

repositories {
  mavenCentral()
  maven { url = uri("https://jitpack.io") }
}

dependencies {
  // Micronaut
  implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
  implementation("io.micronaut:micronaut-runtime")
  implementation("io.micronaut:micronaut-http-server-netty")
  implementation("io.micronaut:micronaut-http-client")
  implementation("io.micronaut:micronaut-management")
  implementation("io.micronaut.micrometer:micronaut-micrometer-core")
  implementation("io.micronaut.micrometer:micronaut-micrometer-registry-prometheus")
  implementation("io.micronaut.kotlin:micronaut-kotlin-extension-functions")
  implementation("io.micronaut.cache:micronaut-cache-caffeine")

  // Kotlin
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.3.2")

  // YAML parsing
  implementation("com.networknt:json-schema-validator:1.0.65")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

  // Logging
  implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
  implementation("ch.qos.logback:logback-classic")
  implementation("net.logstash.logback:logstash-logback-encoder:7.2")

  // DioZero
  implementation("com.diozero:diozero-core:1.4.1")

  // Oshi - Library for SystemInfoProvider
  implementation("com.github.oshi:oshi-core-java11:6.1.4")

  // Serial library for my sensors
  implementation("com.fazecast:jSerialComm:[2.0.0,3.0.0)")

  // MQTT
  implementation("com.hivemq:hivemq-mqtt-client:1.3.0")
  compileOnly("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.2")

  // Avahi / ZeroConf / Multicast DNS
  implementation("org.jmdns:jmdns:3.5.8")

  // Tests
  testImplementation(enforcedPlatform(libs.micronaut.bom))
  testImplementation("io.micronaut:micronaut-inject-groovy")
  testImplementation("org.codehaus.groovy:groovy:3.0.25")
  testImplementation("org.codehaus.groovy:groovy-json:3.0.25")
  testImplementation("org.codehaus.groovy:groovy-yaml:3.0.25")
  testImplementation(platform("org.spockframework:spock-bom:2.0-M5-groovy-3.0"))
  testImplementation("org.spockframework:spock-core") {
    exclude(group = "org.codehaus.groovy", module = "groovy-all")
  }
  testImplementation("io.micronaut.test:micronaut-test-spock")
  testImplementation("net.bytebuddy:byte-buddy:1.12.8")
  testImplementation(platform("org.testcontainers:testcontainers-bom:1.15.3"))
  testImplementation("org.testcontainers:spock")
}

application {
  mainClass.set("com.mqgateway.ApplicationKt")
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    showStandardStreams = true
    exceptionFormat = TestExceptionFormat.FULL
  }
  jvmArgs("--add-opens", "java.base/java.util=ALL-UNNAMED")
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
}

tasks {
  compileKotlin {
    kotlinOptions {
      jvmTarget = "11"
    }
  }
  compileTestKotlin {
    kotlinOptions {
      jvmTarget = "11"
    }
  }
}

micronaut {
  version(libs.versions.micronaut.get())
  runtime("netty")
  testRuntime("spock2")
  processing {
    incremental(true)
    annotations("com.mqgateway.*")
  }
}

tasks.withType<ShadowJar> {
  mergeServiceFiles()
}

scmVersion {
  versionIncrementer("incrementMinor")
  versionCreator { version, position ->
    "$version-${position.shortRevision}"
  }
}

group = "com.mqgateway"
version = scmVersion.version

tasks.processResources {
  duplicatesStrategy = DuplicatesStrategy.INCLUDE
  val props = mapOf("version" to version)
  from("src/main/resources") {
    include("application.yml")
    expand(props)
  }
  outputs.upToDateWhen { false }
}

node {
  download.set(true)
  version.set("16.13.1")
  workDir.set(project.layout.buildDirectory.dir("nodejs").get().asFile)
  npmWorkDir.set(project.layout.buildDirectory.dir("npm").get().asFile)
  nodeProjectDir.set(project.layout.projectDirectory.dir("ui").asFile)
  npmInstallCommand.set("ci")
}

tasks.register<NpmTask>("npmBuild") {
  args.set(listOf("run", "build"))
}

tasks.register<Copy>("copyWebApp") {
  from(project.layout.projectDirectory.dir("ui").dir("build"))
  into(project.layout.buildDirectory.get().dir("resources").dir("main").dir("webapp"))
}

tasks.whenTaskAdded {
  if (name == "kaptTestKotlin") {
    dependsOn("copyWebApp")
  }
}

tasks.jar {
  dependsOn("copyWebApp")
}

tasks.named("buildLayers") {
  dependsOn("copyWebApp")
}

tasks.compileTestGroovy {
  dependsOn("copyWebApp")
}

tasks.named("npmBuild") {
  dependsOn("npmInstall")
}

tasks.named("copyWebApp") {
  dependsOn("npmBuild")
}

tasks.shadowJar {
  dependsOn("copyWebApp")
}
