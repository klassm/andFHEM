import li.klass.fhem.build.*

val preBuildTask = tasks.getByName("preBuild")
val generateResourceIdMapper = tasks.create("generateResourceIdMapper", ResourceIdMapperGeneratorTask::class) {
    inputFile = file("src/main/res/values/strings.xml")
    outputFile = file("src/gen/java/li/klass/fhem/resources/ResourceIdMapper.java")
}
preBuildTask.dependsOn(generateResourceIdMapper)