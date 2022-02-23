package com.kobting.stsdependencies

import groovy.lang.Closure
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.plugins.ExtraPropertiesExtension
import java.io.File
import java.io.InputStream
import java.lang.IllegalArgumentException


private const val DEPENDENCY_TASK = "stsDependencies"

class STSDependenciesPlugin: Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            project.tasks.create(DEPENDENCY_TASK) {
                val configFile = project.file("${project.rootProject.rootDir}/stsdependencies.config")
                val steamPath = if(configFile.exists()) {
                    configFile.readLines()[0]
                } else {
                    val defaultSteamPath = "C:/Program Files/Steam"
                    configFile.createNewFile()
                    configFile.writeText(defaultSteamPath)
                    defaultSteamPath
                }
                val workshopPath = "$steamPath/steamapps/workshop/content/646570"
                val dependencyJsonString = this@STSDependenciesPlugin::class.java.classLoader.getResourceAsStream("dependency_map.json")!!.asString()
                val dependencies = Json.decodeFromString<Dependencies>(dependencyJsonString)
                dependencies.dependencies.forEach {
                    val dependencyPath = "${workshopPath}/${it.id}/${it.jar}"
                    project.ext + (it.name to project.files(dependencyPath))
                }
                project.ext + ("SlayTheSpire" to project.files("$steamPath/steamapps/common/SlayTheSpire/desktop-1.0.jar"))
            }
        }
    }

}

val Project.ext get() = properties["ext"] as ExtraPropertiesExtension

operator fun ExtraPropertiesExtension.plus(ext: Pair<String, Any>) {
    println("Adding ext: ${ext.first}, ${ext.second}")
    set(ext.first, ext.second)
}

@Serializable
data class Dependencies(val dependencies: List<Dependency>)
@Serializable
data class Dependency(var name: String, var id: String, var jar: String)

fun InputStream.asString(): String {
    return bufferedReader().use { it.readLines() }.joinToString("")
}