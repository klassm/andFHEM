import li.klass.fhem.build.whatsnew.*

val generateWhatsNew by tasks.registering(WhatsNewTask::class) {
    whatsnewFileDe = file("whatsnew-de.md")
    whatsnewFileEn = file("whatsnew-en.md")
    toUpdate = listOf(file("src/inapp/play"), file("src/premium/play"))
}

val resetWhatsNew by tasks.registering(ResetWhatsNewTask::class) {
    whatsnewFileDe = file("whatsnew-de.md")
    whatsnewFileEn = file("whatsnew-en.md")
    toUpdate = listOf(file("src/inapp/play"), file("src/premium/play"))
}
