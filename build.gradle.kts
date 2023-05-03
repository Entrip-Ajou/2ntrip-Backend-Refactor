import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val snippetsDir by extra { file("build/generated-snippets") }
val asciidoctorExt: Configuration by configurations.creating

plugins {
	id("org.springframework.boot") version "2.7.1"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	id("org.asciidoctor.jvm.convert") version "3.3.2"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
	kotlin("plugin.jpa") version "1.6.21"
}

tasks.named("jar") {
	enabled = false
}

group = "com.entrip"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	compileOnly("org.projectlombok:lombok")
	runtimeOnly("com.h2database:h2")
	runtimeOnly("mysql:mysql-connector-java")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	implementation("io.awspring.cloud:spring-cloud-starter-aws:2.4.2")
	implementation("org.json:json:20220320")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.security:spring-security-test")
	implementation("io.jsonwebtoken:jjwt:0.9.1")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("io.springfox:springfox-swagger2:2.9.2")
	implementation("io.springfox:springfox-swagger-ui:2.9.2")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.apache.commons:commons-exec:1.3")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

	testImplementation("io.kotest:kotest-runner-junit5:4.4.3")
	testImplementation("io.kotest:kotest-assertions-core:4.4.3")
	implementation("io.kotest:kotest-extensions-spring:4.4.3")

	testImplementation("io.mockk:mockk:1.12.0")
	testImplementation("com.ninja-squad:springmockk:3.0.0")

	// Spring REST Docs dependency
	testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc:2.0.6.RELEASE")
	testImplementation("org.springframework.restdocs:spring-restdocs-asciidoctor:2.0.6.RELEASE")
	asciidoctorExt("org.springframework.restdocs:spring-restdocs-asciidoctor")

	// Test Dependencies for Kotlin
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.mockk:mockk:1.12.0")
	testImplementation("com.ninja-squad:springmockk:3.0.0")
	testImplementation("io.kotest:kotest-runner-junit5:4.4.3")
	testImplementation("io.kotest:kotest-assertions-core:4.4.3")
	implementation("io.kotest:kotest-extensions-spring:4.4.3")

	implementation(group="it.ozimov", name="embedded-redis", version = "0.7.2")
}


tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}


tasks {

	test {
		outputs.dir(snippetsDir)
	}

	asciidoctor {
		inputs.dir(snippetsDir)
		configurations(asciidoctorExt.name)
		dependsOn(test)
		doLast {
			copy {
				from("build/docs/asciidoc")
				into("src/main/resources/static/docs")
			}
		}
	}

	build {
		dependsOn(asciidoctor)
	}
}
