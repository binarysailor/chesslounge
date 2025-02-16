plugins {
    kotlin("jvm") version "1.9.0"
}

group = "net.binarysailor.chesslounge"
version = "1.1-SNAPSHOT"

repositories {
    mavenCentral()
}

sourceSets {
    create("intTest") {
        val mainOutput = sourceSets.main.get().output
        compileClasspath += mainOutput
        runtimeClasspath += mainOutput
    }
}

val intTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

dependencies {
    implementation(project(":engine"))
    implementation("com.sparkjava:spark-core:2.9.4")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("ch.qos.logback:logback-core:1.5.16")
    implementation("ch.qos.logback:logback-classic:1.5.16")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.assertj:assertj-core:3.26.3")
    testImplementation(kotlin("test"))
    intTestImplementation(platform("org.junit:junit-bom:5.9.1"))
    intTestImplementation("org.junit.jupiter:junit-jupiter")
}


tasks.test {
    useJUnitPlatform()
}

val integrationTest = task<Test>("intTest") {
    description = "Runs integration tests"

    val intTestSourceSet = sourceSets["intTest"]
    testClassesDirs = intTestSourceSet.output.classesDirs
    classpath = intTestSourceSet.runtimeClasspath //+ sourceSets.main.get().output

    shouldRunAfter("test")
    useJUnitPlatform()
}

tasks.check {
    dependsOn(integrationTest)
}
