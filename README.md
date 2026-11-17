# Ben's Morphe Patches

An independent Citizen patch source for use with [Morphe Manager](https://morphe.software), adapted from [rushiranpise/morphe-patches](https://github.com/rushiranpise/morphe-patches/tree/main/patches/src/main/kotlin/app/template/patches/citizen).

[**Add this source to Morphe**](https://morphe.software/add-source?github=xiliav/bens-morphe-patches&app=sp0n.citizen)

Repository address: `https://github.com/xiliav/bens-morphe-patches`

## Install on your phone

1. Install or update [Morphe Manager](https://morphe.software).
2. Open the **Add this source to Morphe** link above and confirm, or open **Sources → + → Remote** and paste this repository's address.
3. Wait for the source to load. On the home screen, choose **Citizen** and this source. In Expert mode, enable **Unlock Pro**.
4. Select the original **Citizen 0.1318.0 (version code 1151) APKM** from your phone's storage. Use the complete APKM bundle, including its splits. You do not need to extract DEX files to patch on your phone.
5. Patch, then install the result using Morphe's prompts. If Android reports a signing conflict with stock Citizen, installing the patched app may require removing the stock app first; removing it can delete its local data.

The source only becomes usable after the first successful release has published its `.mpp` bundle and `patches-bundle.json`. Check [Releases](https://github.com/xiliav/bens-morphe-patches/releases) if Morphe cannot load it yet.

For a manual import, download `patches-<version>.mpp` from a release and add it with **Sources → + → Local**. A remote source can update automatically; a local import must be updated manually.

## Supported versions and limits

| Citizen version | Version code | Implementation |
| --- | --- | --- |
| 0.1318.0 | 1151 | Updated against supplied DEX files; 34 required targets checked statically |
| 0.1307.0 | 1139 | Original implementation retained; not revalidated against the old APK |

The patch overrides local Plus/Protect checks and suppresses paywalls. It does not create a subscription on Citizen's servers or establish server-side eligibility for paid services. Static checks and a successful build do not establish that every feature works on a phone; device validation is still required.

The 0.1318.0 implementation keeps invitation and challenge event handlers intact, changes only the family collector's entitlement flag, and preserves superclass initialization when dismissing paywall activities. Old Superwall and obfuscated collector hooks execute only for the legacy version.

## Available patches

<!-- PATCHES_START EXPANDED -->
> **[v1.0.0](https://github.com/xiliav/bens-morphe-patches/releases/tag/v1.0.0)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;1 patches total
<details open>
<summary>📦 Citizen&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**Package name:** `sp0n.citizen`

**🎯 Supported versions:**

| 0.1318.0 | 0.1307.0 |
| :---: | :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Pro](#unlock-pro) | Unlocks local Citizen Plus/Protect features and hides paywalls. |  |

</details>

<!-- PATCHES_END -->

## Build and release

This project uses the official [Morphe patches template](https://github.com/MorpheApp/morphe-patches-template) and its existing semantic-release workflow. It publishes the Android `.mpp` bundle, patch list, changelog, and Manager metadata together.

- Make changes on `dev` with `fix:` or `feat:` commit messages to create a prerelease. Enable **Pre-release patches** on this source in Manager to receive those builds.
- Merge `dev` into `main` without squashing to create a stable release. The **Release** workflow also supports manual dispatch.
- Do not edit generated `patches-list.json`, `patches-bundle.json`, or `CHANGELOG.md` by hand.
- For local builds, use Java 21 and run `./gradlew :patches:buildAndroid :patches:generatePatchesList`. The bundle appears under `patches/build/libs/`.
- GitHub Packages authentication may be required locally. Follow [Morphe's setup documentation](https://github.com/MorpheApp/morphe-documentation/blob/main/docs/morphe-development/1_setup.md); never commit a token. Actions uses its built-in `GITHUB_TOKEN`.
- Release signing is optional and is not configured initially. Follow [Morphe's signing guide](https://github.com/MorpheApp/morphe-documentation/blob/main/docs/morphe-development/2_signing.md) to add a persistent signing key. The private key is ignored by git.

Original APKs, DEX files, account information, and credentials are not part of this repository or its release assets.

## Attribution and license

Citizen patch source: [rushiranpise/morphe-patches](https://github.com/rushiranpise/morphe-patches), commit `92c0fca0ae103a419c7fc61f9c5b8b38b7d6b82a`, with compatibility updates for 0.1318.0. Build and release infrastructure: [MorpheApp/morphe-patches-template](https://github.com/MorpheApp/morphe-patches-template).

Licensed under [GPL-3.0](LICENSE), with the notices in [NOTICE](NOTICE). This source is independently maintained and is not affiliated with Citizen or the Morphe project.
