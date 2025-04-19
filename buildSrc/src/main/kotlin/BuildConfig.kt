object BuildConfig {
    val minecraftVersion: String = "1.21.1"
    val minecraftVersionRange: String = ">=1.21"
    val yarnMappings: String = minecraftVersion + "+build.3"
    val loaderVersion: String = "0.16.13"

    val modVersion: String = "1.0.0"
    val mavenGroup: String = "com.macuguita.tagstack"
    val modId: String = "tag_stack"
    val modName: String = "Tag Stack"
    val description: String = "Mod that allows you to change item stack sizes with tags"
    val license: String = "MIT"

    val fabricVersion: String = "0.115.4+" + minecraftVersion
    val architecturyVersion: String = "9.2.14+fabric"
    val clothConfigVersion: String = "11.1.136+fabric"
    val meadowVersion: String = "no" //there's no 1.21.1 version
    val moonlightVersion: String = "fabric_1.21-2.18.8"
    val supplementariesVersion: String = "fabric_1.21-3.1.7" //1.20-3.1.26
}