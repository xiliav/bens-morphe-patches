package app.template.patches.thetransit

import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction10x
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction11x
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.immutable.ImmutableClassDef
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableStringReference
import org.junit.Assert.*
import org.junit.Test

class TransitPatchTest {
    private fun subscriptionMethod(resultRegister: Int, flag: String = "activate_royale_subscription") =
        ImmutableMethod(
            TRANSIT_LIB_CLASS, "fetchSubscriptionStatus",
            listOf(ImmutableMethodParameter("Z", null, null), ImmutableMethodParameter("J", null, null)),
            "V", AccessFlags.PUBLIC.value or AccessFlags.STATIC.value, null, null,
            MutableMethodImplementation(24).apply {
                addInstruction(BuilderInstruction21c(Opcode.CONST_STRING, 2, ImmutableStringReference(flag)))
                addInstruction(BuilderInstruction35c(
                    Opcode.INVOKE_STATIC, 1, 2, 0, 0, 0, 0,
                    ImmutableMethodReference(TRANSIT_LIB_CLASS, "isFeatureActivated", listOf("Ljava/lang/String;"), "Z")
                ))
                addInstruction(BuilderInstruction11x(Opcode.MOVE_RESULT, resultRegister))
                addInstruction(BuilderInstruction10x(Opcode.RETURN_VOID))
            }
        )

    private fun classDef(method: ImmutableMethod) = ImmutableClassDef(
        method.definingClass, AccessFlags.PUBLIC.value, "Ljava/lang/Object;",
        null, null, null, null, listOf(method)
    )

    @Test
    fun `real matcher finds the Royale check and transformation uses its actual result register`() {
        for (register in listOf(2, 19)) {
            val method = subscriptionMethod(register)
            val fp = FetchSubscriptionStatusFingerprint
            fp.clearMatch()
            try {
                val match = fp.matchOrNull(method, classDef(method))
                assertNotNull(match)
                val resultIndex = match!!.instructionMatches.last().index
                val mutable = method.toMutable()
                forceRoyaleFeatureFlag(mutable, resultIndex)
                val result = mutable.getInstruction(resultIndex)
                assertEquals(Opcode.CONST_16, result.opcode)
                assertEquals(register, (result as OneRegisterInstruction).registerA)
                assertEquals(1, (result as NarrowLiteralInstruction).narrowLiteral)
                assertEquals(Opcode.INVOKE_STATIC, mutable.getInstruction(resultIndex - 1).opcode)
                assertEquals(4, mutable.instructions.size)
            } finally {
                fp.clearMatch()
            }
        }
    }

    @Test
    fun `subscription fingerprint rejects a different feature flag`() {
        val method = subscriptionMethod(2, "some_other_feature")
        FetchSubscriptionStatusFingerprint.clearMatch()
        try {
            assertNull(FetchSubscriptionStatusFingerprint.matchOrNull(method, classDef(method)))
        } finally {
            FetchSubscriptionStatusFingerprint.clearMatch()
        }
    }

    @Test
    fun `Royale getter and application initializer match the exact observed signatures`() {
        for ((fp, method) in listOf(
            RoyaleIsActiveFingerprint to ImmutableMethod(
                ROYALE_STATUS_CLASS, "isActive", emptyList(), "Z",
                AccessFlags.PUBLIC.value or AccessFlags.FINAL.value, null, null, null
            ),
            TransitAppOnCreateFingerprint to ImmutableMethod(
                TRANSIT_APP_CLASS, "onCreate", emptyList(), "V",
                AccessFlags.PUBLIC.value or AccessFlags.FINAL.value, null, null, null
            )
        )) {
            fp.clearMatch()
            try {
                assertNotNull(fp.matchOrNull(method, classDef(method)))
            } finally {
                fp.clearMatch()
            }
        }
    }

    @Test
    fun `helper injection preserves superclass initialization and keeps the Maps override blank by default`() {
        val method = ImmutableMethod(
            TRANSIT_APP_CLASS, "onCreate", emptyList(), "V",
            AccessFlags.PUBLIC.value or AccessFlags.FINAL.value, null, null,
            MutableMethodImplementation(15).apply {
                addInstruction(BuilderInstruction35c(
                    Opcode.INVOKE_SUPER, 1, 14, 0, 0, 0, 0,
                    ImmutableMethodReference("Landroid/app/Application;", "onCreate", emptyList(), "V")
                ))
                addInstruction(BuilderInstruction10x(Opcode.RETURN_VOID))
            }
        ).toMutable()
        initializeTransitHelper(method, "")
        val key = (method.getInstruction<ReferenceInstruction>(0).reference as StringReference).string
        assertEquals("", key)
        val initializer = method.getInstruction<ReferenceInstruction>(1).reference as MethodReference
        assertEquals(TRANSIT_HELPER_CLASS, initializer.definingClass)
        assertEquals("init", initializer.name)
        assertEquals(listOf("Ljava/lang/String;"), initializer.parameterTypes.map { it.toString() })
        assertEquals("V", initializer.returnType)
        assertEquals(Opcode.INVOKE_SUPER, method.getInstruction(2).opcode)
        assertEquals(Opcode.RETURN_VOID, method.getInstruction(3).opcode)
        assertEquals(15, method.implementation!!.registerCount)
    }

    @Test
    fun `object results are rejected rather than overwritten with a boolean`() {
        val method = ImmutableMethod(
            TRANSIT_LIB_CLASS, "example", emptyList(), "V", AccessFlags.PUBLIC.value,
            null, null, MutableMethodImplementation(2).apply {
                addInstruction(BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, 0))
                addInstruction(BuilderInstruction10x(Opcode.RETURN_VOID))
            }
        ).toMutable()
        assertThrows(PatchException::class.java) { forceRoyaleFeatureFlag(method, 0) }
    }

    @Test
    fun `startup injection refuses to overwrite this when no local register exists`() {
        val method = ImmutableMethod(
            TRANSIT_APP_CLASS, "onCreate", emptyList(), "V", AccessFlags.PUBLIC.value,
            null, null, MutableMethodImplementation(1).apply {
                addInstruction(BuilderInstruction10x(Opcode.RETURN_VOID))
            }
        ).toMutable()
        assertThrows(PatchException::class.java) { initializeTransitHelper(method, "") }
    }
}
