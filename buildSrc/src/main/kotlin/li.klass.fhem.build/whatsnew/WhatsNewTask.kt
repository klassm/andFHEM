package li.klass.fhem.build.whatsnew

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.TaskAction
import java.io.File

open class WhatsNewTask : DefaultTask() {
    @InputFile
    lateinit var whatsnewFileEn: File
    @InputFile
    lateinit var whatsnewFileDe: File

    @OutputDirectories
    lateinit var toUpdate: List<File>

    @TaskAction
    fun updateWhatsNew() {
        allOutputDirectoriesExist()
    }

    private fun allOutputDirectoriesExist() {
        val notExisting = toUpdate.filterNot { it.exists() }
        val notExistingPaths = notExisting.map {
            it.getAbsolutePath()
        }
        if (!notExisting.isEmpty()) {
            throw IllegalArgumentException("output directories $notExistingPaths must exist")
        }

        writeContentTo("de-DE", whatsnewFileDe)
        writeContentTo("en-US", whatsnewFileEn)
    }

    private fun writeContentTo(locale: String, changedFile: File) {
        val content = changedFile.readText(Charsets.UTF_8)
        if (content.isEmpty() || content.contains("TODO")) {
            throw IllegalArgumentException("changelog must be filled, changedFile=" + changedFile.getAbsolutePath())
        }
        val shortText = if (content.length > 400) {
            content.substring(0, 400) + "...\r\nhttp://andfhem.klass.li/changelog/"
        } else content

        toUpdate.forEach {
            val targetFile = File(it.getAbsolutePath() + "/$locale/whatsnew")
            targetFile.delete()
            targetFile.writeText(shortText, Charsets.UTF_8)
        }
    }
}