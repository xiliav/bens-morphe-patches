# Citizen

[Back to the collection](../../README.md)

Package: `sp0n.citizen`  
Patch: **Unlock Pro**

## Supported versions

| Citizen version | Version code | Status |
| --- | --- | --- |
| 0.1318.0 | 1151 | Updated fingerprints and regression tests; reported working on a user device |
| 0.1307.0 | 1139 | Legacy implementation retained; not revalidated against the old APK |

Use the original APKM bundle, including its splits, when patching in Morphe Manager. You do not need to extract DEX files.

## Behavior and limitations

The patch overrides local Plus/Protect checks and suppresses paywalls. It does not create a subscription on Citizen's servers or establish server-side eligibility for paid services. Successful patching does not guarantee that every server-backed feature is available.

The 0.1318.0 implementation uses 34 required targets. It preserves invitation and challenge event handlers, changes only the family collector's entitlement flag, and calls superclass initialization before dismissing paywall activities. Its five paywall Activity fingerprints match the exact `public final` access flags required by Morphe.

Old Superwall and obfuscated collector hooks execute only for the legacy version.

## Attribution

Adapted from the [Citizen patch in rushiranpise/morphe-patches](https://github.com/rushiranpise/morphe-patches/tree/main/patches/src/main/kotlin/app/template/patches/citizen), commit `92c0fca0ae103a419c7fc61f9c5b8b38b7d6b82a`, with compatibility updates for 0.1318.0.

See the repository's [license](../../LICENSE) and [notices](../../NOTICE).
