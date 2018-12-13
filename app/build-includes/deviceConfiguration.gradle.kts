import li.klass.fhem.build.*

val preBuildTask = tasks.getByName("preBuild")

val buildDeviceConfigurationJson by tasks.registering(DeviceConfigurationTask::class) {
    inputDir = file("src/main/resources/deviceConfiguration")
    outputFile = file("src/gen/resources/deviceConfiguration.json")
}
preBuildTask.dependsOn(buildDeviceConfigurationJson)