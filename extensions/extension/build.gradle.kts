extension {
    name = "extensions/extension.mpe"
}

android {
    namespace = "app.template.extension"
    defaultConfig {
        minSdk = 32
    }
}

dependencies {
    implementation("org.lsposed.hiddenapibypass:hiddenapibypass:6.2")
}
