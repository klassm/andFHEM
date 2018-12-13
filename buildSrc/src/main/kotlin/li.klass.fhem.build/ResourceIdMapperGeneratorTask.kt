package li.klass.fhem.build

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.slf4j.LoggerFactory
import java.io.File

open class ResourceIdMapperGeneratorTask : DefaultTask() {
    @InputFile
    lateinit var inputFile: File

    @OutputFile
    lateinit var outputFile: File

    @TaskAction
    fun generateResource() {
        if (!project.buildDir.exists()) {
            project.buildDir.mkdir();
        }

        val content = inputFile.readText(Charsets.UTF_8)

        val resourceIdMapperContent = pattern.findAll(content)
                .map { it.groupValues[1] }
                .joinToString(separator = ",") { "$it (R.string.$it)" }


        val toWrite = """
            package li.klass.fhem.resources;

            import li.klass.fhem.R;

            public enum ResourceIdMapper {
                $resourceIdMapperContent,none(-1);

                private int id;

                private ResourceIdMapper(int id) {
                    this.id = id;
                }

                public int getId() {
                    return id;
                }
        }""".trimIndent()

        LoggerFactory.getLogger(ResourceIdMapperGeneratorTask::class.java)
                .info("writing ResourceIdMapper.java to ${outputFile.absolutePath}")
        println("writing ResourceIdMapper.java to " + outputFile.absolutePath)

        outputFile.writeText(toWrite, Charsets.UTF_8)
    }

    companion object {
        val pattern = "<string name=\"([^\"]+)".toRegex()
    }
}
