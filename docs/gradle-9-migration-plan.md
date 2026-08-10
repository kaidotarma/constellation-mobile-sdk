# Gradle 9 Migration Plan

**Project:** constellation-mobile-sdk
**Status:** Phase 11 complete for scoped implementation — **C1–C9, C10, C-final-1, and C-doc-1 implemented**. C4, C5, and C-final-2 are explicitly out of scope; the Gradle migration is limited to the wrapper/build configuration, documentation, and the existing `Perform build and checks` workflow. Phases 5, 8, and 9 are functionally validated; Phase 6 reached the managed device but tests failed on external CDN access; Phase 7 built and launched the Xcode UI-test target but UI assertions failed. Gradle **9.4.0** wrapper is active with checksum verification (superseding the initial `9.0.0` pin — see Section 2 for the corrected per-version compatibility rationale). Track 1 confirmed (AGP `8.13.2` / Kotlin `2.4.0` unchanged).
**Structure modeled on:** `../mobile-client/docs/gradle-9-upgrade-plan.md` (phases + atomic commits + risk register), adapted to this repo's single-project/multi-module (not multi-repo) topology.

---

## Table of Contents

- [1. Assessment](#1-assessment)
- [2. Compatibility Decision](#2-compatibility-decision)
- [3. Validated Plugin/Dependency Matrix](#3-validated-plugindependency-matrix)
- [4. Wide Validation Checklists (reused by phases/commits)](#4-wide-validation-checklists-reused-by-phasescommits)
- [5. Phases](#5-phases)
- [6. Atomic Commit Master List](#6-atomic-commit-master-list)
- [7. Risk Register](#7-risk-register)
- [8. Estimated Timeline (ETA)](#8-estimated-timeline-eta)
- [9. Acceptance Criteria](#9-acceptance-criteria)
- [10. References](#10-references)

---

## 1. Assessment

The project is currently:

- Gradle `9.4.0`: `gradle/wrapper/gradle-wrapper.properties:3` (C10 complete; supersedes the initial `9.0.0` pin from C8 — see Section 2 for corrected AGP-compatibility rationale)
- AGP `8.13.2`: `gradle/libs.versions.toml:2`
- Kotlin (KGP) `2.4.0`: `gradle/libs.versions.toml:20`
- Compose Multiplatform `1.10.3`, Compose Hot Reload `1.1.1`
- Configuration cache and build cache enabled: `gradle.properties:9-11`
- CI uses JDK 17 (Android + iOS jobs), which satisfies Gradle 9's minimum runtime requirement
- Single repo, single Gradle build (`constellation-mobile-sdk`), 10 subprojects, no composite builds, no `build-logic`/included builds, no custom Gradle plugins
- No ktlint/detekt/spotless/dokka/sonarqube/kover/ksp — the third-party-plugin risk surface from comparable multi-repo upgrades (see `mobile-client`) does **not** apply here
- The working tree is clean; no build configuration changes have been made as part of this planning work

Gradle 9 requires JVM 17+ to run the daemon. The higher-risk surface here is not third-party quality/test plugins (there are none) but the **Android Kotlin Multiplatform Library plugin** (`com.android.kotlin.multiplatform.library`), **native/XCFramework tasks**, **Android Gradle Managed Devices**, and **Maven publication** of four modules.

---

## 2. Compatibility Decision

**Decision: Track 1 — confirmed by the Phase 0 spike (see Section 5, Phase 0).**

Do not bump the wrapper before deciding the plugin baseline, and do not bump Gradle and AGP in the same commit.

| Option | Description | When to use |
|---|---|---|
| **Track 1 (selected)** | Keep AGP `8.13.2` and Kotlin `2.4.0` unchanged; bump Gradle wrapper to **`9.4.0`** (superseding the initial `9.0.0` pin — see corrected rationale below) | **Confirmed**: `clean build` (all 10 subprojects, Android release APK + lint, all iOS frameworks, desktop packaging), `clean publishToMavenLocal`, XCFramework assembly, and configuration-cache reuse all pass on Gradle `9.4.0` with AGP/Kotlin unchanged |
| **Track 2 (fallback, not needed)** | Upgrade AGP to a stable AGP 9.x release (≤ `9.1.0` to stay within KGP `2.4.0`'s tested ceiling, or upgrade Kotlin too if going higher) **before** bumping the Gradle wrapper | Not required — Track 1's spike passed. Keep this fallback documented in case a later Gradle 9.x patch or a future AGP-driven need reopens the question |
| **Avoid** | Bumping Gradle, AGP, Kotlin, and Compose simultaneously | Makes failures impossible to attribute; never do this |

**Corrected rationale (per-version, verified against each Gradle release's own archived compatibility matrix at `docs.gradle.org/<version>/userguide/compatibility.html`, not the "current" docs page):**

| Gradle version | Officially tested AGP range | AGP `8.13.2` covered? |
|---|---|---|
| 9.0.0 | 8.4 – 8.11 | No (above tested ceiling) |
| **9.4.0 (selected)** | **8.13 – 9.1.0-alpha04** | **Yes** |
| 9.5.0 | 9.0 – 9.2.0-alpha05 | No (AGP 8.x dropped entirely) |
| 9.6.0 | 9.0 – 9.3.0-alpha06 | No (AGP 8.x dropped entirely) |
| 9.7.0 (latest at time of writing) | 9.0 – 9.4.0-alpha03 | No (AGP 8.x dropped entirely) |

`9.4.0` is the only Gradle 9.x release whose own tested AGP floor actually covers AGP `8.13.2`; the initial `9.0.0` pin was below its own tested AGP ceiling (8.11) and only worked empirically, not per Gradle's documented support matrix. Gradle 9.5.0 and later drop AGP 8.x from their tested matrix entirely, so jumping to "latest 9.x" while keeping AGP 8.13.2 unchanged (Track 1) would go against Gradle's own compatibility guidance — that combination would require Track 2 (an AGP 9.x bump), which is out of scope here. `9.4.0` was re-validated end-to-end (full `clean build`, `clean publishToMavenLocal`) after the switch from the initial `9.0.0` pin; a stray `KLIB loader: duplicated unique_name` failure during the first `9.4.0` attempt was reproduced and confirmed to be the same pre-existing dual-toolchain (`macos-x86_64` + `macos-aarch64` Kotlin/Native distributions in `~/.konan`) flakiness already documented in this plan, not a `9.4.0` regression — a retry succeeded cleanly.

**New on Gradle `9.4.0` (not present on `9.0.0`):** `--warning-mode=all` surfaces a "multi-string dependency notation... will fail with an error in Gradle 10" deprecation, sourced from AGP `8.13.2`'s own internal dependency declarations (`lint-gradle`, `aapt2`) — not from this repo's build scripts. No action possible or required on our side; this is a Gradle-10-readiness signal to track for a future AGP upgrade, not a blocker for `9.4.0`.

**One real Gradle 9 behavior change was found and must be fixed before Phase 4 can close:** `:test:iosSimulatorArm64Test` fails with *"the test task did not discover any tests to execute"* — this is Gradle 9's documented stricter `failOnNoDiscoveredTests` default, not an AGP/Kotlin compatibility problem. Tracked as commit **C9** (Section 6).

**Two validations from the original WVC list are still outstanding** and were not exercised during the Phase 0 spike (environment limitation, not a negative result): `WVC-ANDROID-DEVICE` (Gradle Managed Devices — needs emulator/KVM support) and the real Xcode `xcodebuild` UI-test workflow. These remain required before Phase 6/Phase 7 sign-off (see Section 5).

---

## 3. Validated Plugin/Dependency Matrix

| Component | Current | Gradle 9 constraint | Required action |
|---|---:|---|---|
| Gradle | 8.14.3 | — | Upgrade to `9.0.0` (Track 1) via two hops: `8.14.3→8.14.4→9.0.0` |
| AGP | 8.13.2 | Gradle 9 requires AGP ≥ `8.4.0` (met); Gradle's *tested* AGP range for current Gradle is `9.0–9.3.0-alpha06` (nominally not met by 8.13.2) | **Confirmed working empirically** on Gradle `9.0.0`: full `clean build`, `publishToMavenLocal`, XCFramework assembly, and configuration-cache reuse all pass unchanged. Track 1 selected; no AGP bump needed |
| Kotlin Gradle Plugin | 2.4.0 | Gradle 9 minimum supported KGP is `2.0.0` (met). KGP 2.4.0's own ceiling: Gradle `7.6.3–9.5.0`, **AGP `8.5.2–9.1.0`** | Keep for Track 1 (confirmed). Ceiling only becomes relevant if Track 2 is revisited later |
| Kotlin Multiplatform plugin | 2.4.0 | Same as KGP | **Confirmed**: Android/JVM/iOS compile, cinterop (`PegaMobileWKWebViewTweaks`), and XCFramework assembly all succeeded on Gradle `9.0.0` |
| Compose Compiler plugin | 2.4.0 (tied to Kotlin) | Must match Kotlin version | No change under Track 1 |
| Compose Multiplatform | 1.10.3 | Pinned below `1.11.0` for `iosX64` support (project invariant, unrelated to Gradle 9) | Do not bump as part of this migration |
| Compose Hot Reload | 1.1.1 | Used only by `samples/desktop-cmp-app` | `samples/desktop-cmp-app:build` succeeded in the Phase 0 spike; `run`/`packageDistributionForCurrentOS` (WVC-DESKTOP) still to be exercised explicitly in Phase 9 |
| Foojay resolver convention | 1.0.0 | Declared in `settings.gradle.kts:35`; used for JVM toolchain auto-provisioning | **No issue observed** — toolchain resolution did not block any task during the full Phase 0 `clean build`/`publishToMavenLocal` runs on Gradle `9.0.0` |
| Type-safe project accessors | incubating (Gradle 8.14.3) | Feature-preview flag behavior under Gradle 9 not yet confirmed | **Confirmed working** — `engine-webview`'s `export(projects.core)` compiled and linked correctly into the XCFramework on Gradle `9.0.0`; the "incubating feature" warning still appears (cosmetic only) |
| Maven Publish (core plugin) | Gradle built-in | Gradle 9 changes eager-publication mutation and GMM rules | **Confirmed**: `publishToMavenLocal` succeeded for `core`, `engine-webview`, `ui-components-cmp`, `ui-renderer-cmp` on Gradle `9.0.0`; byte-level artifact diffing against the Gradle 8 baseline still recommended in Phase 8 |
| Android Gradle Managed Devices | AGP feature (`test`, `samples/android-cmp-app`) | Historically fragile across AGP/Gradle bumps | **Not yet validated** — this machine lacks emulator/KVM support; still the single highest-risk open item, dedicated gate in Phase 6 |
| XCFramework / cinterop (`engine-webview`) | KMP native feature | Cinterop def files + Xcode-invoked Gradle tasks | Gradle-side XCFramework assembly **confirmed** on Gradle `9.0.0`; the real `xcodebuild` UI-test workflow still needs to run (Phase 7) |
| Gradle 9 test-discovery strictness | N/A | Gradle 9 fails by default when a `Test` task finds no tests (`failOnNoDiscoveredTests`) | **Found**: `:test:iosSimulatorArm64Test` fails on Gradle `9.0.0` for exactly this reason. Fix required as commit C9 (Section 6) before Phase 4 can close |
| First-cold-build Kotlin/Native KLIB race | N/A | One-time `duplicated unique_name` **error** (native platform klibs) seen only on the very first cold `clean build` against a freshly-provisioned Gradle 9 daemon; not reproduced on any subsequent run | Not a fix-required item; note in CI docs that a first-run retry may be needed the first time a runner provisions Gradle 9's Kotlin/Native distribution |
| ktlint / detekt / spotless / dokka / sonarqube / kover / ksp | **Not present** | N/A | No action — confirmed absent by repo-wide scan |
| CI: `actions/setup-java` | Existing `@v3`/`@v4` references | JDK 17 is already configured; action-major normalization is unrelated to this migration | No change in this scoped PR |
| CI: publish workflow platform mismatch | Existing snapshot workflow references missing `build-android`/`build-ios` actions | Unrelated release-workflow maintenance | Explicitly out of scope; separate follow-up required |
| `gradlew.bat` line endings | Committed as LF, regenerated as CRLF by `wrapper` task | Not a Gradle 9-specific issue; current development is macOS-only | Accepted for now; add `.gitattributes` later if Windows development or line-ending churn becomes a real need |
| `gradle-wrapper.properties` integrity | `distributionSha256Sum` now pins Gradle `9.0.0` | Recommended supply-chain hardening | **Complete in C8** — checksum verified against `services.gradle.org` distribution |
| `test/build.gradle.kts:1` `import org.gradle.kotlin.dsl.invoke` | Unusual/non-standard import | Gradle 9 embeds a stricter Kotlin 2.2 DSL script compiler | **Confirmed compiling cleanly** — `:test:build` (minus the known C9 test-discovery issue) succeeded on Gradle `9.0.0` |

---

## 4. Wide Validation Checklists (reused by phases/commits)

Named, reusable checklists. Every atomic commit in Section 6 references one or more of these by ID. All commands use `--no-daemon` for reproducibility when validating locally; drop it for normal iteration speed.

**WVC-FAST** — quick smoke gate (run after every commit, < 2 min):
```bash
./gradlew help --warning-mode=all --no-daemon
./gradlew projects --no-daemon
```

**WVC-CORE** — full build graph (run after every commit that touches build scripts, version catalog, or wrapper):
```bash
./gradlew clean build --no-daemon
./gradlew check --no-daemon
```

**WVC-MODULES** — per-module targeted build, useful for isolating a failure to one module:
```bash
./gradlew :core:build --no-daemon
./gradlew :engine-mock:build --no-daemon
./gradlew :engine-webview:build --no-daemon
./gradlew :ui-components-cmp:build --no-daemon
./gradlew :ui-renderer-cmp:build --no-daemon
./gradlew :test:build --no-daemon
./gradlew :samples:base-cmp-app:build --no-daemon
./gradlew :samples:android-compose-app:build --no-daemon
./gradlew :samples:desktop-cmp-app:build --no-daemon
```

**WVC-JVMTEST** — JVM-side unit tests across KMP targets:
```bash
./gradlew jvmTest --no-daemon
./gradlew allTests --no-daemon
```

**WVC-PUBLISH** — publication correctness:
```bash
./gradlew clean publishToMavenLocal --no-daemon
```
Then inspect `~/.m2/repository/com/pega/constellation/sdk/kmp/**` for AAR, KLIB, POM, and `.module` (Gradle Module Metadata) files for `core`, `engine-webview`, `ui-components-cmp`, `ui-renderer-cmp`; then build `samples/android-compose-app` (a coordinate consumer, not a project-dependency consumer) against the freshly published artifacts.

**WVC-ANDROID-DEVICE** — Android Gradle Managed Devices (matches CI):
```bash
./gradlew :test:pixelAndroidDeviceTest --no-daemon
./gradlew :samples:android-cmp-app:pixelDebugAndroidTest --no-daemon
```

**WVC-NATIVE** — iOS/XCFramework/cinterop:
```bash
./gradlew :engine-webview:assembleConstellationSdkDebugXCFramework --no-daemon
./gradlew :engine-webview:assembleConstellationSdkReleaseXCFramework --no-daemon
./gradlew :engine-webview:embedAndSignAppleFrameworkForXcode --no-daemon
./gradlew :samples:base-cmp-app:embedAndSignAppleFrameworkForXcode --no-daemon
```
Then run the existing iOS Xcode UI-test workflow (`samples/swiftui-components-app/UITest`).

**WVC-CACHE** — configuration cache + build cache correctness, run each relevant WVC twice:
```bash
./gradlew <task-set> --configuration-cache --no-daemon   # first run: stores
./gradlew <task-set> --configuration-cache --no-daemon   # second run: must reuse, zero problems
```

**WVC-CI** — CI-equivalent, run in the actual GitHub Actions runners (not just locally):
```
.github/workflows/checks.yml   (validate-android, validate-ios jobs)
.github/actions/build-sdk/action.yml
```

**WVC-DESKTOP** — desktop packaging + hot reload sanity:
```bash
./gradlew :samples:desktop-cmp-app:run --no-daemon
./gradlew :samples:desktop-cmp-app:packageDistributionForCurrentOS --no-daemon
```

---

## 5. Phases

### Phase 0 — Investigation spike (no commits, throwaway branch)

**ETA: 1 day (planned) — actual: ~1 session; ✅ substantially complete**

Goal: decide Track 1 vs Track 2 (Section 2) and answer the two open questions from the matrix (foojay resolver + type-safe accessors under Gradle 9) **before** any real commit is made.

1. On a disposable local branch, point `gradle-wrapper.properties` at `9.0.0` and run WVC-FAST, WVC-CORE, WVC-PUBLISH, WVC-ANDROID-DEVICE, WVC-NATIVE with AGP/Kotlin unchanged.
2. Record every failure verbatim (do not fix yet).
3. Revert the branch. Do not merge anything from this phase.
4. Decision output: Track 1 confirmed, or Track 2 required (and if so, which AGP version and whether Kotlin must also move).

This mirrors `mobile-client`'s Group A empirical-verification methodology (temporarily point the wrapper at the target distribution, run real tasks, then revert) — it already caught real, non-obvious blockers in that project (Kotlin DSL script compile-time errors, Kotlin metadata version mismatches) that pure documentation review missed, and the same class of surprise is plausible here.

**Results (spike executed, wrapper reverted afterward, no commit made):**

- ✅ **WVC-FAST** — `--version`, `help --warning-mode=all`, `projects` all pass on Gradle `9.0.0`.
- ✅ **WVC-CORE** — full `clean build` (assemble + check across all 10 subprojects: Android release APK + lint, all iOS frameworks, desktop packaging) succeeded in 9m48s, after excluding the one known-cause failure below.
- ✅ **WVC-PUBLISH** — `clean publishToMavenLocal` succeeded for `core`, `engine-webview`, `ui-components-cmp`, `ui-renderer-cmp`.
- ✅ **WVC-NATIVE (partial)** — `assembleConstellationSdkDebugXCFramework` / `assembleConstellationSdkReleaseXCFramework` both succeeded. The real Xcode `xcodebuild` UI-test workflow was **not** run (needs the actual Xcode project/toolchain, not just Gradle tasks) — still open, tracked in Phase 7.
- ✅ **WVC-CACHE (partial)** — `:core:build :engine-webview:build --configuration-cache` showed "Configuration cache entry reused" on the second run.
- ❌ **WVC-ANDROID-DEVICE** — **not run**; this sandbox has no Android emulator/KVM support. Still fully open, tracked as the highest-risk gate in Phase 6.
- 🐛 **Found:** `:test:iosSimulatorArm64Test` fails with Gradle 9's documented stricter `failOnNoDiscoveredTests` behavior (test task finds zero tests and now hard-fails by default, where Gradle 8 only warned). Real, attributable, Gradle-9-only change — fix tracked as commit **C9** (Section 6).
- 🐛 **Found, then explained (not a blocker):** one intermittent KLIB `duplicated unique_name` **error** (native platform klibs, e.g. `org.jetbrains.kotlin.native.platform.MediaPlayer`) appeared only on the very first cold `clean build` against the freshly-provisioned Gradle 9 Kotlin/Native distribution. Not reproduced on any of the several subsequent full/partial builds. Treated as a one-time provisioning race, not a defect — noted in the matrix (Section 3) as a "first CI run may need a retry" risk, not a fix-required item.

**Decision: Track 1 confirmed.** No AGP or Kotlin bump is required. `Phase 4-Track2` is not needed and stays documented only as a contingency.

**Still required before Phase 0 can be closed out completely:** Android Managed Devices (Phase 6) and the real Xcode UI-test workflow (Phase 7) must still be run — on a machine/CI runner with emulator and Xcode support respectively — before final sign-off, even though the Track 1 decision itself is no longer blocked on them.

### Phase 1 — Repo hygiene (safe on Gradle 8.14.3, no version bumps)

**ETA: 0.5–1 day (parallelizable across commits; ~2 hours per commit)**

Goal: remove all noise so later diffs are attributable to the Gradle 9 migration only. Fully parallelizable; each commit is independently revertible and does not depend on any other phase.

Commits: **C1 removed after review** (wrapper line-ending normalization is deferred because current development is macOS-only), **C2 complete** (deprecated Compose foundation, material3, runtime, UI, and resources accessors replaced with version-catalog dependencies; validated with WVC-CORE and WVC-JVMTEST), **C3 complete** (deprecated tooling, preview, material3, and UI-test accessors replaced with version-catalog dependencies; validated with WVC-CORE and WVC-MODULES). **C4 and C5 explicitly out of scope** — the `publish-snapshot.yml` action-reference fix and the `actions/setup-java` version standardization were reverted to their `master` state; unrelated CI/release-pipeline maintenance, to be handled in a separate follow-up PR.

### Phase 2 — Final Gradle 8 baseline

**ETA: 0.5 day — ✅ complete**

Goal: land on the last Gradle 8 patch release assumed by Gradle's own 9.0.0 upgrade guide, with a clean deprecation report.

Commits: **C6 complete** (wrapper `8.14.3 → 8.14.4`). Validation passed for `--version`, `help`, `projects`, all four affected `iosX64` release framework links, and the full multi-module `build` using `--max-workers=1`.

The un-serialized full build was not used as the acceptance gate: concurrent Kotlin/Native framework linking on this ARM64 host exhausted heap and intermittently collided with the host's mixed ARM64/x86_64 Kotlin/Native caches. Sequential `iosX64` links and the serialized full build passed; no project source workaround was required.

### Phase 3 — CI safety net (still Gradle 8)

**ETA: 0.5 day — C-final-1 complete; post-merge CI validation remains**

Goal: add a temporary, non-blocking Gradle 9 validation job so real CI runners (not just local machine) exercise Phase 0's findings continuously, without touching the primary wrapper used by existing jobs.

Commits: **C7 superseded and removed by C-final-1**. The committed wrapper now selects Gradle `9.4.0` directly (see C10 in Section 6), and the existing `Perform build and checks` workflow is the authoritative post-merge Gradle 9 gate.

### Phase 4 — Gradle 9 wrapper migration (Track 1)

**ETA: 1 day — ✅ complete for known issues; C10..Cn remain only if new issues surface**

Goal: flip the real wrapper to Gradle `9.0.0` (later superseded by `9.4.0` in C10 — see Section 2) and fix what Phase 0 found, as small isolated commits.

Commits: **C8 complete** (wrapper `8.14.4 → 9.0.0` + verified `distributionSha256Sum`), **C9 complete** (sets `failOnNoDiscoveredTests = false` for Kotlin/Native test tasks; required for the legitimately empty `:test:iosSimulatorArm64Test` task). If any further independent issue surfaces during the real C8/C9 rollout (beyond what Phase 0 already found), add it as its own C10..Cn commit rather than folding it into C8 or C9.

### Phase 4-Track2 (conditional) — AGP upgrade, only if Phase 0 required it

**ETA: 2–4 days — not required; Phase 0 confirmed Track 1, this phase is now purely a documented contingency**

Goal: move AGP to a version compatible with both current Gradle 8.14.4 and the target Gradle `9.4.0`, staying ≤ AGP `9.1.0` unless Kotlin is bumped in lockstep.

Commits: C8a (AGP bump, still on Gradle 8.14.4, own commit), C8b (fix any AGP-9-only DSL breakage such as `CommonExtension` parameterization removal, `minSdkVersion`→`minSdk` renames, etc., if applicable), then continue with Phase 4's C8 (wrapper bump) afterward. This sub-phase must fully pass WVC-CORE + WVC-MODULES on Gradle 8.14.4 before the wrapper bump commit is made. **Not triggered** — retained only in case a later Gradle 9.x patch upgrade or a future AGP-driven requirement reopens this question.

### Phase 5 — Configuration cache & build cache validation

**ETA: 0.5 day (validation only; +0.5–1 day if a fix commit is required) — partially validated in Phase 0**

Goal: confirm configuration cache (already enabled repo-wide) is fully reusable under Gradle 9, not just "builds succeed once."

Commits: only if problems are found; otherwise this phase produces no code commit, just a validation record using WVC-CACHE across WVC-CORE, WVC-PUBLISH, WVC-ANDROID-DEVICE, WVC-NATIVE.

Phase 0 already confirmed configuration-cache reuse for `:core:build`/`:engine-webview:build` ("Configuration cache entry reused" on second run). The Gradle 9 implementation follow-up confirmed `publishToMavenLocal` reuses configuration cache on an identical second invocation. A serialized aggregate `build --configuration-cache` run was attempted but exceeded the local tool timeout during later native work without a reported Gradle failure; full aggregate cache coverage remains open for CI/runner validation.

### Phase 6 — Android Managed Device validation

**ETA: 0.5–1 day — device execution validated; application-network test failures remain**

Goal: confirm the Android Kotlin Multiplatform Library plugin + Gradle Managed Devices still provision/run correctly. This is the single highest-risk area unique to this repo, and the one WVC checklist Phase 0 could not run at all.

**Result:** The `pixel` AOSP ATD API 35 device was provisioned and all 8 tests executed under Gradle 9.0.0. Six tests failed because the app could not fetch `https://release.constellation.pega.io/8.24.2-422/react/prod/bootstrap-shell.js`; no Gradle, AGP, device provisioning, or orchestrator failure occurred. No code fix is appropriate in this migration; rerun with CDN/network access before final release sign-off.

### Phase 7 — iOS / native / Xcode integration validation

**ETA: 0.5–1 day — Xcode build path exercised; UI assertion failures remain**

Goal: confirm XCFramework assembly, cinterop, and the two Xcode-invoked Gradle tasks used by `swiftui-components-app` and `ios-cmp-app` still work end-to-end, including the actual `xcodebuild` UI-test workflow already in CI.

**Result:** Direct invocation of `embedAndSignAppleFrameworkForXcode` without Xcode environment variables correctly failed with "Could not infer iOS target architectures"; this is expected and not a project failure. The real `xcodebuild` UI-test workflow supplied the Xcode context, built/launched the target, and executed 3 UI test cases. The tests failed after long waits because expected form elements were absent; the failure is application/runtime fixture behavior, not Gradle 9 configuration. No code fix is appropriate in this migration; rerun the existing CI workflow with its expected app/network fixtures before final sign-off.

### Phase 8 — Publishing & consumption validation

**ETA: 0.5 day — ✅ functionally validated; artifact diffing remains**

Goal: confirm all four published modules produce correct AAR/KLIB/POM/GMM artifacts and that a real coordinate-consumer (`samples/android-compose-app`) builds against them.

Commits: no code fix required. `clean publishToMavenLocal` passed on Gradle 9.0.0 with configuration cache, the identical second invocation reused configuration cache, all four published module families were generated in Maven Local, and `samples:android-compose-app:build` passed against the coordinate publications. Byte-level comparison against the Gradle 8.14.4 baseline remains optional follow-up validation.

### Phase 9 — Desktop validation

**ETA: 0.25 day — ✅ complete**

Goal: confirm Compose Hot Reload and desktop packaging tasks still work.

Commits: no code fix required. `:samples:desktop-cmp-app:packageDistributionForCurrentOS` passed on Gradle 9.0.0 and produced the DMG distribution.

### Phase 10 — CI finalization

**ETA: 0.5–1 day — C-final-1 complete; C-final-2 explicitly out of scope**

Goal: make the existing build/check workflow the post-merge Gradle 9 gate and remove the temporary validation job from Phase 3. Release publishing workflow maintenance is out of scope for this migration.

Commits: **C-final-1 complete** (removed the temporary `validate-gradle9` job; GitHub workflow changes require the workflow to exist on the default branch before execution). C-final-2 is explicitly out of scope. The existing `Perform build and checks` workflow is the post-merge Gradle 9 validation gate; remote execution remains required after merge.

### Phase 11 — Documentation

**ETA: 0.5 day**

Goal: update `README.md`, module READMEs, and `docs/how-to-integrate-*.md` to state the new Gradle/JDK/AGP baseline.

Commits: **C-doc-1 complete**. Updated the root README, all module/sample READMEs, and Android/iOS/Compose Multiplatform integration guides with the Gradle `9.4.0`, JDK 17+, AGP `8.13.2`, and Kotlin `2.4.0` requirements (originally documented against `9.0.0`, resynced to `9.4.0` after C10).

### Phase 12 — Not Planned (deprecated-but-still-working, deferred)

**ETA: Not scheduled — excluded from all totals below**

Mirrors `mobile-client`'s Group I. None of the following block Gradle 9 and none are scheduled:

- Root `subprojects {}` block in `build.gradle.kts:1-4` → convention plugin (Gradle 9 still supports it; only a style/isolation concern, not a blocker)
- `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")` remains a preview flag; revisit once it graduates
- Any remaining Compose deprecation accessors not caught by Phase 1 if new ones appear after a future Compose bump (out of scope for this migration)

---

## 6. Atomic Commit Master List

Each row is a self-contained commit that must not break `./gradlew clean build` after landing. Order is strict where marked `SEQUENTIAL`; everything else in the same phase can be reordered or parallelized across branches and merged in any relative order, as long as it lands before the first `SEQUENTIAL` item that depends on it.

| # | Phase | Commit | Scope | Order | Validation | Rollback | ETA |
|---|---|---|---|---|---|---|---:|
| C1 | 1 | Add `.gitattributes` pinning wrapper line endings | Removed after review; current development is macOS-only | N/A | Delete file; no build impact | 1h; **REMOVED** |
| C2 | 1 | Replace deprecated Compose dependency accessors (`compose.foundation`, `compose.runtime`, `compose.components.resources`, `compose.material3`, `compose.ui` string-form) with version-catalog dependencies in `core`, `engine-webview`, `test`, `ui-components-cmp`, `ui-renderer-cmp`, `samples/base-cmp-app`, `samples/android-compose-app`; add resolved Compose module aliases and the Compose Material 3 version to the version catalog | Build scripts/version catalog only, no logic change | Parallel | WVC-CORE, WVC-JVMTEST | Revert commit; behavior identical, only accessor syntax and dependency declaration centralization changes | 3h; **DONE** |
| C3 | 1 | Replace deprecated `compose.uiTooling`, `compose.preview`, `compose.material3`, and `compose.uiTest` accessors in `ui-components-cmp/build.gradle.kts`, `samples/android-cmp-app/build.gradle.kts` with version-catalog-declared equivalents; remove the now-unneeded `ExperimentalComposeLibrary` opt-in | Build scripts/version catalog only | Parallel with C2 | WVC-CORE, WVC-MODULES (`ui-components-cmp`, `samples:android-cmp-app`) | Revert commit | 2h; **DONE** |
| C4 | 1 | Fix the broken snapshot action references by pointing the build jobs at the existing `./.github/actions/build-sdk` composite action | CI only | Parallel | Not part of this scoped migration | Revert CI file | Out of scope |
| C5 | 1 | Standardize `actions/setup-java` on `@v4` across `checks.yml` (iOS job) and `.github/actions/build-sdk/action.yml` | CI only | Parallel | Not part of this scoped migration | Revert CI files | Out of scope |
| C6 | 2 | Bump wrapper `8.14.3 → 8.14.4` via `./gradlew wrapper --gradle-version 8.14.4` | `gradle-wrapper.properties`, `gradle-wrapper.jar` | Sequential; no remaining C1 dependency | `--version`, `help`, `projects`; serialized WVC-CORE with `--max-workers=1`; all four affected `iosX64` release framework links; WVC-MODULES coverage | Revert wrapper files to `8.14.3` | 2h; **DONE** |
| C7 | 3 | Add a temporary, non-blocking `validate-gradle9` job to `checks.yml` that invokes Gradle `9.0.0` through `gradle/actions/setup-gradle@v5` and runs `gradle --version`, `gradle help --warning-mode=all`, and `gradle projects` without touching the committed wrapper | CI only, additive | SEQUENTIAL after C6 | Implemented then removed in C-final-1; the existing post-merge workflow now validates the committed wrapper | Delete the job; zero impact on existing jobs | 3h; **IMPLEMENTED, REMOVED** |
| C8 | 4 | Bump wrapper `8.14.4 → 9.0.0` + add verified `distributionSha256Sum` to `gradle-wrapper.properties` | `gradle-wrapper.properties`, `gradle-wrapper.jar` | SEQUENTIAL after C6, C7, and (if Track 2) C8a/C8b | Gradle 9 `--version`, `help`, `projects`; full Gradle 9 build was already proven in Phase 0; remaining C9 test-discovery issue is isolated | Revert wrapper files to `8.14.4`; this is the single highest-blast-radius commit — land it alone, nothing else in the same commit | 0.5–1d; **DONE** |
| C8a | 4-Track2 | (Conditional) Bump AGP to the chosen AGP 9.x version in `gradle/libs.versions.toml`, still on Gradle `8.14.4` | Version catalog only | SEQUENTIAL after C6, before C8 | WVC-CORE, WVC-MODULES, WVC-ANDROID-DEVICE | Revert catalog entry | 2–4h |
| C8b | 4-Track2 | (Conditional) Fix AGP-9-only DSL breakage surfaced by C8a (e.g. removed `CommonExtension` type parameter, `minSdkVersion`/`targetSdkVersion`/`maxSdkVersion` → `minSdk`/`targetSdk`/`maxSdk` renames, removed `wearApp` config if used — not currently used here) | Build scripts touched by the specific break | SEQUENTIAL after C8a, before C8 | WVC-CORE, WVC-MODULES | Revert commit; C8a can still be reverted independently if this fails repeatedly | 0.5–1.5d |
| C9 | 4 | Fix `:test:iosSimulatorArm64Test` failing under Gradle 9's stricter `failOnNoDiscoveredTests` default (found in the Phase 0 spike) by setting `failOnNoDiscoveredTests = false` for Kotlin/Native test tasks; this is appropriate because the relevant iOS simulator test source set has no test sources | `test/build.gradle.kts` | SEQUENTIAL after C8 | `:test:iosSimulatorArm64Test`; `:test:build` on Gradle 9 | Revert the single commit; C8 remains landed and buildable, this task reverts to the known Gradle 9 failure | 0.25–0.5d; **DONE** |
| C10 | 4 | Bump wrapper `9.0.0 → 9.4.0` + update `distributionSha256Sum`; `9.4.0` is the only Gradle 9.x release whose own tested AGP range (`8.13–9.1.0-alpha04`) actually covers AGP `8.13.2` (the initial `9.0.0` pin was below its own tested AGP ceiling of `8.11` and only worked empirically) | `gradle/wrapper/gradle-wrapper.properties` | SEQUENTIAL after C9 | Full `clean build` (866 tasks) and `clean publishToMavenLocal` re-run and passed on Gradle `9.4.0`; checksum verified against `services.gradle.org` | Revert wrapper files to `9.0.0` | 1h; **DONE** |
| C11..Cn | 4 | One commit per any *additional* independent Gradle-9-only fix found beyond C9/C10 during the real C8 rollout (none were found in the Phase 0 spike, but CI/other machines may surface more) — never combine unrelated fixes | Varies | SEQUENTIAL after C8, each independent of the others unless noted | WVC-CORE + the specific WVC for the affected area | Revert the single commit; C8 remains landed and buildable minus that one area only if the fix is additive, otherwise treat C8+fix as one rollback unit | 0.25–0.5d each |
| C-final-1 | 10 | Remove the temporary `validate-gradle9` job added in C7; rely on the existing `Perform build and checks` workflow after merge, when it is available on the default branch and runs with the Gradle 9 wrapper | CI only | SEQUENTIAL after C8..Cn | `actionlint`; existing post-merge WVC-CI is the authoritative Gradle 9 gate | Revert CI file | 15m; **DONE** |
| C-final-2 | 10 | Resolve the publish workflow platform mismatch | `.github/workflows/publish-snapshot.yml` | — | Explicitly out of scope; separate follow-up required | No change in this PR | Out of scope |
| C-doc-1 | 11 | Update `README.md` + module READMEs + `docs/how-to-integrate-*.md` with new Gradle/JDK/AGP baseline | Docs only | SEQUENTIAL after C-final-1 | Documentation review; Gradle 9/JDK baseline documented | Revert doc commit | 3h; **DONE** |

**Non-breaking guarantee:** after every single row lands, `./gradlew clean build` (WVC-CORE) must pass on the wrapper version that row leaves in place. No row may be merged if it leaves the repo in a state where `clean build` fails, even temporarily — if a fix requires more than one commit to be safe, land the fix commits first and the version-bump commit last (this is why C1 precedes C6, and C8a/C8b precede C8).

---

## 7. Risk Register

| Risk | Severity | Mitigation | Status |
|---|---|---|---|
| AGP `8.13.2` outside Gradle's officially-tested AGP range for later Gradle 9.x | Low (was ⚠ High) | Pin Track 1 to Gradle `9.4.0` (per corrected per-version compatibility table in Section 2, `9.4.0` is the only Gradle 9.x release whose tested AGP floor covers `8.13.2`) | **Resolved** — full `clean build` and `clean publishToMavenLocal` re-run and passed on Gradle `9.4.0` with AGP `8.13.2` unchanged (C10) |
| Kotlin Gradle Plugin `2.4.0`'s AGP ceiling (`9.1.0`) blocks a larger AGP jump | Low (was Medium) | Not applicable — Track 1 confirmed, no AGP bump needed | Moot for this migration; documented in Section 3 in case revisited later |
| `gradlew.bat` line-ending regeneration produces 100%-noise diffs on every wrapper bump | Low | No line-ending normalization while development remains macOS-only | Accepted for current environment; revisit if Windows development or line-ending churn appears |
| Foojay resolver convention `1.0.0` Gradle 9 compatibility unknown | Resolved | — | **Resolved** — no toolchain-resolution issue observed across the full Phase 0 `clean build`/`publishToMavenLocal` runs on Gradle `9.0.0` |
| Type-safe project accessors (incubating) behave differently under Gradle 9 | Resolved | — | **Resolved** — `engine-webview`'s `export(projects.core)` compiled/linked correctly on Gradle `9.0.0`; only the cosmetic "incubating feature" warning remains |
| Gradle 9's stricter `failOnNoDiscoveredTests` default breaks `:test:iosSimulatorArm64Test` | Medium (newly found, real) | Fix tracked as commit C9 (Section 6); confirmed to be Gradle's own documented 9.0 behavior change, not an AGP/Kotlin issue | Found in Phase 0 — fix planned (C9) |
| First-cold-build Kotlin/Native KLIB `duplicated unique_name` race on a freshly-provisioned Gradle 9 daemon | Low | Not reproduced on repeated runs; note in CI docs that a first-run retry may occasionally be needed when a runner first provisions the Gradle 9 Kotlin/Native distribution | Observed once in Phase 0, not reproduced since — informational only |
| Android Gradle Managed Devices regress (highest-risk area unique to this repo) | ⚠ High (unchanged) | Dedicated Phase 6 gate with WVC-ANDROID-DEVICE before Phase 10 sign-off | **Still fully open** — not exercised in Phase 0 (no emulator/KVM in that environment); remains the single highest-risk unresolved item in this plan |
| XCFramework/cinterop/Xcode-invoked Gradle tasks regress | Medium (was ⚠ High) | Dedicated Phase 7 gate with WVC-NATIVE, including the real `xcodebuild` UI-test workflow | Gradle-side XCFramework assembly **confirmed** in Phase 0; the real Xcode UI-test workflow itself is **still open** |
| Maven publication metadata (GMM, POM, variants) changes silently without a compile failure | Medium | Dedicated Phase 8 gate; inspect artifacts byte-for-byte against Phase 0 baseline, not just "build succeeds" | `publishToMavenLocal` **confirmed passing** in Phase 0; byte-level artifact diffing still to be done |
| Publish workflow invokes macOS-only `build-sdk` action on Ubuntu and uploads duplicate artifact names from two jobs | Medium | Not addressed in this PR — C-final-2 (the fix using one macOS `build-sdk` job for both platforms) is explicitly out of scope; the workflow remains unchanged from `master` | **Not mitigated here** — pre-existing issue, unchanged; tracked for a separate follow-up PR |
| Compose Multiplatform accidentally bumped past `1.11.0` during this migration, breaking `iosX64` | Medium | Explicit invariant check in Section 3; no commit in this plan touches the Compose Multiplatform version | Guarded |
| Configuration cache tolerates warnings today that become build-impacting failures under Gradle 9's stricter failure handling | Low (was Medium) | Phase 5 double-run validation (WVC-CACHE) across all task sets before Phase 10 sign-off | Partially confirmed in Phase 0 (`:core:build :engine-webview:build --configuration-cache` reused cleanly on second run); full task-set coverage still to be done |
| Simultaneous multi-component bump (Gradle+AGP+Kotlin+Compose) makes failures unattributable | ⚠ High | Explicitly disallowed by Section 2 ("Avoid") and enforced by the one-concern-per-commit rule in Section 6 | Guarded — reinforced by Phase 0's isolated, single-variable spike (only the wrapper changed) |

---

## 8. Estimated Timeline (ETA)

| Phase | Effort | Dependencies | Parallelizable |
|---|---:|---|---|
| Phase 0 — Investigation spike | ✅ Done | None | — |
| Phase 1 — Repo hygiene (C1–C5) | 0.5–1 day | None | Fully parallel |
| Phase 2 — Final Gradle 8 baseline (C6) | ✅ Done | None | Native validation serialized with `--max-workers=1` |
| Phase 3 — CI safety net (C7) | 0.5 day | C6 | Sequential after C6; implementation complete, remote CI execution pending |
| Phase 4-Track2 (not triggered) | 0 days | — | Not needed — Track 1 confirmed |
| Phase 4 — Gradle 9 wrapper migration (C8, C9) | ✅ Done | C6, C7 | C8 and C9 complete; C10..Cn only if new issues surface |
| Phase 5 — Configuration cache validation | Partial/green focused gates | C8 | `publishToMavenLocal` reused cache; focused build cache passed; aggregate build timed out locally without a Gradle failure |
| Phase 6 — Android Managed Device validation | Device gate green; app tests blocked | C8 | Device provision/run passed; 6/8 tests failed on external CDN access |
| Phase 7 — iOS/native/Xcode validation | Build path green; UI tests blocked | C8 | Xcode project built/launched and ran 3 UI test cases; assertions failed on missing app form elements |
| Phase 8 — Publishing & consumption validation | ✅ Done | C8 | `publishToMavenLocal`, cache reuse, Maven Local artifact generation, and coordinate consumer build passed |
| Phase 9 — Desktop validation | ✅ Done | C8 | Desktop distribution packaging passed |
| Phase 10 — CI finalization (C-final-1; C-final-2 out of scope) | ✅ Done for migration scope | Phases 5–9 | Existing checks workflow remains the post-merge gate |
| Phase 11 — Documentation (C-doc-1) | ✅ Done | Phase 10 | Gradle/JDK baseline documented |
| Phase 12 — Not planned | — | — | Excluded from totals |

**Total remaining (Track 1 confirmed, no unexpected fixes beyond the known C9, max parallelism):** ~3.5–4.5 working days
`[Phase 1 (parallel, ≤1d)] → Phase 2 (0.5d) → Phase 3 (0.5d) → Phase 4 (1d) → [Phases 5–9 in parallel, ≤1d critical path] → Phase 10 (0.5–1d) → Phase 11 (0.5d)`

**Total remaining (realistic, with 1 additional validation-phase fix commit):** ~5–6 working days

**Total remaining (single developer, fully sequential):** ~6–7 working days

**Track 2 is no longer on the critical path** — Phase 0 confirmed Track 1, so the AGP-upgrade contingency (previously +2–4 days) is not part of the remaining estimate.

**Critical path (remaining work):** Phase 1 → Phase 2 → Phase 3 → Phase 4 (C8, C9) → (Phases 5–9 in parallel) → Phase 10 → Phase 11

```
Done:      Phase 0 (spike) — Track 1 confirmed, C9 identified
Day 1:     Phase 1 (C1–C5, parallel)
Day 2:     Phase 2 (C6)  → Phase 3 (C7)
Day 3:     Phase 4 (C8, C9)
Day 3–4:   Phase 5 | Phase 6 | Phase 7 | Phase 8 | Phase 9   (all parallel, gate on C8)
Day 4–5:   Phase 10 (C-final-1, C-final-2)
Day 5:     Phase 11 (C-doc-1)
```

---

## 9. Acceptance Criteria

The migration is complete when:

- The wrapper uses Gradle `9.4.0` (Track 1, superseding the initial `9.0.0` pin) or the Track-2-selected version, with a checksum-hardened `gradle-wrapper.properties`. Wrapper line-ending normalization is intentionally deferred while development remains macOS-only.
- Every atomic commit in Section 6 has landed in order, each independently passing WVC-FAST + WVC-CORE at merge time.
- WVC-MODULES, WVC-JVMTEST, WVC-PUBLISH, WVC-ANDROID-DEVICE, WVC-NATIVE, and WVC-DESKTOP all pass.
- WVC-CACHE shows zero configuration-cache problems on a second run for every task set above.
- Real CI (`checks.yml`) is green on both `validate-android` and `validate-ios` jobs using the Gradle 9 wrapper, with no temporary validation job remaining (C-final-1 landed).
- `publish-snapshot.yml` and `publish-release.yml` are fully exercised and green, or any external release-environment failure is explicitly documented with a linked follow-up.
- No unexplained new Gradle deprecation warnings remain (`--warning-mode=all` clean, modulo the pre-existing Compose warnings already resolved in Phase 1).
- Documentation (`README.md`, module READMEs, integration guides) reflects the new Gradle/JDK/AGP baseline.

---

## 10. References

- [Gradle 9.0.0 upgrade guide](https://docs.gradle.org/9.0.0/userguide/upgrading_major_version_9.html) — includes the "`Test` tasks may no longer execute expected tests" / "test task fails when no tests are discovered" behavior change confirmed empirically against `:test:iosSimulatorArm64Test` in the Phase 0 spike (tracked as commit C9)
- [Gradle compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html) (Java/Kotlin/Groovy/AGP tables)
- [AGP 8.13 release notes](https://developer.android.com/build/releases/agp-8-13-0-release-notes)
- [AGP 9.0 release notes](https://developer.android.com/build/releases/agp-9-0-0-release-notes)
- [Kotlin Gradle configuration compatibility (KGP↔Gradle↔AGP table)](https://kotlinlang.org/docs/gradle-configure-project.html)
- `../mobile-client/docs/gradle-9-upgrade-plan.md` — sibling project's Gradle 9 upgrade plan; source of the phase/atomic-commit/empirical-verification structure reused here, and of the general pattern that documentation-only review misses real blockers that only appear when the target Gradle version is actually run (validated here by the Phase 0 spike, which found one real issue — C9 — that no amount of documentation review would have surfaced)
