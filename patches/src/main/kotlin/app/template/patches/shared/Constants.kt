package app.template.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val CITIZEN_COMPATIBILITY = Compatibility(
        name = "Citizen",
        packageName = "sp0n.citizen",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x0066FF,
        targets = listOf(
            AppTarget(version = "0.1318.0", versionCode = 1151),
            AppTarget(version = "0.1307.0", versionCode = 1139)
        )
    )
}
