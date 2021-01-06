package li.klass.fhem.update.backend

import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.core.RoomDeviceList
import java.io.*
import java.nio.charset.Charset

fun mapDevice(device: FhemDevice): List<String> = device.xmlListDevice.let {
    val def = it.internals["DEF"] ?: return emptyList()

    val definitionLine = "define ${device.name} ${it.type} ${def.value}"
    val attributes = device.xmlListDevice.attributes.map { attr ->
        "attr ${device.name} ${attr.key} ${attr.value.value}"
    }

    return listOf(definitionLine) + attributes
}

fun main() {
    val input = File("<<input>>")
    val deviceList = CustomInputStream(BufferedInputStream(FileInputStream(input))).use {
        it.readObject() as RoomDeviceList
    }
    val lines = deviceList.allDevices.flatMap {
        mapDevice(it)
    }
    val result = lines.joinToString(separator = "\n")

    val userHome = System.getProperty("user.home")
    File("${userHome}/Downloads/out.txt").apply {
        delete()
        createNewFile()
        writeText(result, Charset.forName("UTF-8"))
    }

}

// copies from https://sanjitmohanty.in/2011/11/23/making-jvm-to-ignore-serialversionuids-mismatch/
class CustomInputStream(input: InputStream?) : ObjectInputStream(input) {
    @Throws(IOException::class, ClassNotFoundException::class)
    override fun readClassDescriptor(): ObjectStreamClass {
        var resultClassDescriptor: ObjectStreamClass = super.readClassDescriptor() // initially streams descriptor
        val localClass: Class<*> // the class in the local JVM that this descriptor represents.
        localClass = try {
            Class.forName(resultClassDescriptor.getName())
        } catch (e: ClassNotFoundException) {
            return resultClassDescriptor
        }
        val localClassDescriptor: ObjectStreamClass = ObjectStreamClass.lookup(localClass)
        // only if class implements serializable
        val localSUID: Long = localClassDescriptor.serialVersionUID
        val streamSUID: Long = resultClassDescriptor.serialVersionUID
        if (streamSUID != localSUID) { // check for serialVersionUID mismatch.
            val s = StringBuffer("Overriding serialized class version mismatch: ")
            s.append("local serialVersionUID = ").append(localSUID)
            s.append(" stream serialVersionUID = ").append(streamSUID)
            resultClassDescriptor = localClassDescriptor // Use local class descriptor for deserialization
        }
        return resultClassDescriptor
    }
}
