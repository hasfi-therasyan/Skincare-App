buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
        classpath("com.apollographql.apollo3:apollo-gradle-plugin:3.8.2")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
