package li.klass.fhem.build

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

open class DeviceConfigurationTask : DefaultTask() {
    @InputDirectory
    lateinit var inputDir: File

    @OutputFile
    lateinit var outputFile: File

    @TaskAction
    fun generate() {
        val text = inputDir.listFiles()
                .map {
                    val type = it.name.replace(".json", "")
                    val text = it.readText(Charsets.UTF_8)
                    "\"$type\":$text"
                }
                .joinToString(",")
        val content = "{\"configurations\":{$text}}"
        outputFile.writeText(content, Charsets.UTF_8)
    }
}