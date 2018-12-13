package li.klass.fhem.build.whatsnew

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.TaskAction
import java.io.File

open class ResetWhatsNewTask : DefaultTask() {
    @InputFile
    lateinit var whatsnewFileEn: File
    @InputFile
    lateinit var whatsnewFileDe: File

    @OutputDirectories
    lateinit var toUpdate: List<File>


    @TaskAction
    fun reset() {
        clear(whatsnewFileDe)
        clear(whatsnewFileEn)
    }

    private fun clear(file: File) {
        file.delete()
        file.createNewFile()
        file.writeText("TODO", Charsets.UTF_8)

        delete("de-DE")
        delete("en-US")
    }

    private fun delete(locale: String) {
        toUpdate.forEach {
            val targetFile = File(it.getAbsolutePath() + "/$locale/whatsnew")
            targetFile.delete()
        }
    }
}