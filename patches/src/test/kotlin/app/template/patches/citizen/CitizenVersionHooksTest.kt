package app.template.patches.citizen

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction10x
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction11n
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction11x
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction22c
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c
import com.android.tools.smali.dexlib2.immutable.ImmutableClassDef
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableFieldReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import org.junit.Assert.*
import org.junit.Test

/** Minimal instruction fixtures for the signatures observed in the supplied DEX versions. */
class CitizenVersionHooksTest {
    private data class Layout(
        val purchaseClass: String,
        val protectFabClass: String,
        val analyticsType: String,
        val contextType: String,
        val continuationType: String
    )

    private val layouts = linkedMapOf(
        "0.1318.0" to Layout("Lbfd;", "Lafd;", "Lzn;", "Lk27;", "Lyx3;"),
        "0.1319.0" to Layout("Lhcd;", "Lgcd;", "Lbo;", "Lf17;", "Lxx3;"),
        "0.1320.0" to Layout("Ljfd;", "Lifd;", "Lao;", "Ld17;", "Lhx3;")
    )

    private fun classDef(method: ImmutableMethod) = ImmutableClassDef(
        method.definingClass, AccessFlags.PUBLIC.value, "Ljava/lang/Object;",
        null, null, null, null, listOf(method)
    )

    private fun helper(version: String, purchase: Boolean, withPaywallCall: Boolean = true): ImmutableMethod {
        val layout = layouts.getValue(version)
        val type = if (purchase) layout.purchaseClass else layout.protectFabClass
        val parameters = if (purchase) listOf(
            "Landroid/content/Context;", "Z",
            "Lsp0n/citizen/data/variablesettings/VariableSettingsRepository;",
            "Lsp0n/citizen/analytics/premium/purchase/PurchaseAnalyticsApi\$ProtectPaywallOrigin;"
        ) else listOf(
            "Lsp0n/citizen/data/variablesettings/VariableSettingsRepository;",
            "Lsp0n/citizen/data/store/PreferenceStorage;",
            layout.analyticsType, "Z", layout.contextType,
            "Lsp0n/citizen/premium/purchase/ui/ProtectFabOrigin;"
        )
        return ImmutableMethod(
            type, "a", parameters.map { ImmutableMethodParameter(it, null, null) },
            if (purchase) "Landroid/content/Intent;" else "V",
            AccessFlags.PUBLIC.value or AccessFlags.STATIC.value, null, null,
            MutableMethodImplementation(if (purchase) 6 else 7).apply {
                if (withPaywallCall) addInstruction(BuilderInstruction35c(
                    Opcode.INVOKE_STATIC, 4, 0, 1, 2, 3, 0,
                    ImmutableMethodReference(
                        "Lsp0n/citizen/paywall/superwall/MultimonthPaywallActivity\$a;", "a",
                        listOf("Landroid/content/Context;", "Ljava/lang/String;",
                            "Lsp0n/citizen/paywall/superwall/PlusFeature;", "I"),
                        "Landroid/content/Intent;"
                    )
                ))
                if (purchase) {
                    addInstruction(BuilderInstruction11n(Opcode.CONST_4, 0, 0))
                    addInstruction(BuilderInstruction11x(Opcode.RETURN_OBJECT, 0))
                } else {
                    addInstruction(BuilderInstruction10x(Opcode.RETURN_VOID))
                }
            }
        )
    }

    private fun assertMatch(fingerprint: Fingerprint, method: ImmutableMethod, expected: Boolean) {
        fingerprint.clearMatch()
        try {
            val match = fingerprint.matchOrNull(method, classDef(method))
            if (expected) assertNotNull(method.toString(), match) else assertNull(method.toString(), match)
        } finally {
            fingerprint.clearMatch()
        }
    }

    @Test
    fun `each version selects helper signatures from its own DEX layout`() {
        for (version in layouts.keys) {
            val selected = citizenVersionHooks(version)!!
            for (purchase in listOf(true, false)) {
                val method = helper(version, purchase)
                assertMatch(if (purchase) selected.purchasePremiumHelper else selected.protectFabHelper, method, true)
                for (otherVersion in layouts.keys - version) {
                    val other = citizenVersionHooks(otherVersion)!!
                    assertMatch(if (purchase) other.purchasePremiumHelper else other.protectFabHelper, method, false)
                }
            }
        }
    }

    @Test
    fun `reused obfuscated helper classes require the paywall method call anchor`() {
        for (version in layouts.keys) {
            val selected = citizenVersionHooks(version)!!
            assertMatch(selected.purchasePremiumHelper, helper(version, true, false), false)
            assertMatch(selected.protectFabHelper, helper(version, false, false), false)
        }
    }

    @Test
    fun `family collectors match the version continuation and select only the entitlement read`() {
        for ((version, layout) in layouts) {
            val method = ImmutableMethod(
                "Lsp0n/citizen/social/safetynetwork/FamilyPlanBenefitActivity\$a\$a\$a;", "emit",
                listOf("Ljava/lang/Object;", layout.continuationType).map { ImmutableMethodParameter(it, null, null) },
                "Ljava/lang/Object;", AccessFlags.PUBLIC.value or AccessFlags.FINAL.value, null, null,
                MutableMethodImplementation(4).apply {
                    for (field in listOf("a", "b", "c")) addInstruction(BuilderInstruction22c(
                        Opcode.IGET_BOOLEAN, 0, 2,
                        ImmutableFieldReference("Lsp0n/citizen/social/safetynetwork/e\$b\$a;", field, "Z")
                    ))
                }
            )
            val selected = citizenVersionHooks(version)!!.familyPlanBenefitCollector
            selected.clearMatch()
            try {
                val match = selected.matchOrNull(method, classDef(method))
                assertNotNull(match)
                assertEquals(1, match!!.instructionMatches.single().index)
            } finally {
                selected.clearMatch()
            }
            for (otherVersion in layouts.keys - version) {
                val other = citizenVersionHooks(otherVersion)!!
                assertMatch(other.familyPlanBenefitCollector, method, false)
            }
        }
    }
}
