extension {
    name = "extensions/extension.mpe"
}

android {
    namespace = "app.template.extension"
    compileSdk = 37
    defaultConfig {
        minSdk = 32
    }
}

dependencies {
    implementation("org.lsposed.hiddenapibypass:hiddenapibypass:6.2")
}
