package li.klass.fhem.update.backend.xmllist

val XmlListDevice.webCmd get() = getAttribute("webCmd").orNull()
        ?.split(":")
        ?: emptyList()