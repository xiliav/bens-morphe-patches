package app.template.patches.thetransit

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.template.patches.shared.Constants.TRANSIT_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val transitUnlockRoyalePatch = bytecodePatch(
    name = "Unlock Royale",
    description = "Enables Transit's local Royale membership checks.",
    default = true
) {
    compatibleWith(TRANSIT_COMPATIBILITY)
    extendWith("extensions/extension.mpe")

    val mapsApiKey by stringOption(
        key = "mapsApiKey",
        default = "",
        title = "Google Maps API key (optional)",
        description = "Leave blank to keep Transit's original Maps key. Supply your own compatible key only if needed.",
        validator = { it.isNullOrBlank() || it.matches(Regex("AIza[A-Za-z0-9_-]{35}")) }
    )

    execute {
        RoyaleIsActiveFingerprint.method.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        FetchSubscriptionStatusFingerprint.let { fp ->
            // Match the literal, native call, and its result together. Never assume v2.
            forceRoyaleFeatureFlag(fp.method, fp.instructionMatches.last().index)
        }

        // Install the process-local certificate compatibility hook before SDK startup.
        // The rest of onCreate, including Application.onCreate(), remains intact.
        initializeTransitHelper(TransitAppOnCreateFingerprint.method, mapsApiKey.orEmpty().trim())
    }
}

internal fun forceRoyaleFeatureFlag(method: MutableMethod, resultIndex: Int) {
    val result = method.getInstruction(resultIndex)
    if (result.opcode != Opcode.MOVE_RESULT) {
        throw PatchException("Transit Royale feature check does not return a primitive result.")
    }
    val register = (result as OneRegisterInstruction).registerA
    // const/16 encodes any 8-bit result register, including v16 and above.
    method.replaceInstruction(resultIndex, "const/16 v$register, 0x1")
}

internal fun initializeTransitHelper(method: MutableMethod, mapsApiKey: String) {
    val implementation = method.implementation
        ?: throw PatchException("Transit application initializer has no implementation.")
    if (implementation.registerCount < 2) {
        throw PatchException("Transit application initializer needs a local register.")
    }
    val key = mapsApiKey.replace("\\", "\\\\").replace("\"", "\\\"")
        .replace("\n", "\\n").replace("\r", "\\r")
    method.addInstructions(
        0,
        "const-string v0, \"$key\"\n" +
            "invoke-static {v0}, $TRANSIT_HELPER_CLASS->init(Ljava/lang/String;)V"
    )
}
