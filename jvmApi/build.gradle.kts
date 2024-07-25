plugins {
	java
	war
	id("org.springframework.boot") version "2.7.10-SNAPSHOT"
	id("io.spring.dependency-management") version "1.0.15.RELEASE"
}

group = "io.telereso"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenLocal()
	mavenCentral()
	maven { url = uri("https://s01.oss.sonatype.org/content/groups/staging") }
	maven { url = uri("https://repo.spring.io/milestone") }
	maven { url = uri("https://repo.spring.io/snapshot") }
	maven(url = "https://pkgs.dev.azure.com/burnoo/maven/_packaging/public/maven/v1") {
		content {
			includeVersionByRegex(".*", ".*", ".*-beap[0-9]+")
		}
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")
	testImplementation("org.springframework.boot:spring-boot-starter-test")


	implementation(project(":annotations-models"))

}

tasks.withType<Test> {
	useJUnitPlatform()
}
