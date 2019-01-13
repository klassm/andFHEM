package li.klass.fhem.update.backend.xmllist

val XmlListDevice.webCmd
    get() = getAttribute("webCmd")
            ?.split(":")
            ?.filter { it.isNotBlank() }
            ?: emptyList()