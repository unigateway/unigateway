import com.github.gradle.node.npm.task.NpmTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  groovy
  kotlin("jvm") version libs.versions.kotlin
  kotlin("plugin.allopen") version libs.versions.kotlin
  kotlin("plugin.serialization") version libs.versions.kotlin
  id("com.google.devtools.ksp") version "1.9.25-1.0.20"
  id("com.gradleup.shadow") version "8.3.6"
  id("io.micronaut.minimal.application") version "4.5.3"
  id("org.jlleitschuh.gradle.ktlint") version "12.3.0"
  id("pl.allegro.tech.build.axion-release") version "1.18.18"
  id("com.github.node-gradle.node") version "7.1.0"
}

repositories {
  mavenCentral()
  maven { url = uri("https://jitpack.io") }
}

dependencies {
  // Micronaut
  ksp("io.micronaut:micronaut-http-validation")
  ksp("io.micronaut.serde:micronaut-serde-processor")
  implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
  implementation("io.micronaut.serde:micronaut-serde-jackson")
  annotationProcessor("io.micronaut.validation:micronaut-validation-processor")
  implementation("io.micronaut.validation:micronaut-validation")
  implementation("io.micronaut:micronaut-websocket")
  implementation("io.micronaut:micronaut-http-client")
  implementation("io.micronaut:micronaut-management")
  implementation("io.micronaut.micrometer:micronaut-micrometer-core")
  implementation("io.micronaut.micrometer:micronaut-micrometer-registry-prometheus")
  implementation("io.micronaut.cache:micronaut-cache-caffeine")

  // Kotlin
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.6.3") // need to stay on 1.6.x until kotlin 2.x

  // YAML parsing
  implementation("com.networknt:json-schema-validator:1.5.7")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

  // Logging
  implementation("io.github.oshai:kotlin-logging-jvm:6.0.9") // need to stay on 6.x until kotlin 2.x
  implementation("ch.qos.logback:logback-classic")
  implementation("net.logstash.logback:logstash-logback-encoder:8.1")

  // DioZero
  implementation("com.diozero:diozero-core:1.4.1")

  // Oshi - Library for SystemInfoProvider
  implementation("com.github.oshi:oshi-core-java11:6.8.2")

  // Serial library for my sensors
  implementation("com.fazecast:jSerialComm:[2.0.0,3.0.0)")

  // MQTT
  implementation("com.hivemq:hivemq-mqtt-client:1.3.7")
  compileOnly("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")

  // Avahi / ZeroConf / Multicast DNS
  implementation("org.jmdns:jmdns:3.6.1")

  // Tests
  testImplementation("org.apache.groovy:groovy-json")
  testImplementation("org.apache.groovy:groovy-yaml")
  testImplementation("org.objenesis:objenesis:3.4")
  testImplementation("net.bytebuddy:byte-buddy:1.17.5")
  testImplementation(platform("org.testcontainers:testcontainers-bom:1.21.1"))
  testImplementation("org.testcontainers:spock")
  testImplementation("io.micronaut:micronaut-http-client")
}

application {
  mainClass = "com.mqgateway.ApplicationKt"
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
  sourceCompatibility = JavaVersion.VERSION_21
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

tasks.shadowJar {
  dependsOn("copyWebApp")
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
  download = true
  version = "22.16.0"
  workDir = project.layout.buildDirectory.dir("nodejs").get().asFile
  npmWorkDir = project.layout.buildDirectory.dir("npm").get().asFile
  nodeProjectDir = project.layout.projectDirectory.dir("ui").asFile
  npmInstallCommand = "ci"
}

tasks.register<NpmTask>("npmBuild") {
  dependsOn("npmInstall")
  args.set(listOf("run", "build"))
  environment.put("NODE_OPTIONS", "--openssl-legacy-provider")
}

tasks.register<Copy>("copyWebApp") {
  dependsOn("npmBuild")
  from(project.layout.projectDirectory.dir("ui").dir("build"))
  into(project.layout.buildDirectory.get().dir("resources").dir("main").dir("webapp"))
}

tasks.jar {
  dependsOn("copyWebApp")
}

tasks.compileTestGroovy {
  dependsOn("copyWebApp")
}
