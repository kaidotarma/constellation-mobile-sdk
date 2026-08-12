# Contributing to Constellation Mobile SDK

## Before You Start

Read the root `AGENTS.md`, `.specify/memory/constitution.md`, the relevant module README, and any related page under `docs/`. Use existing code, tests, and fixtures as the primary source of truth. Do not include secrets, customer data, or generated build output in a change.

## Spec-Driven Development

Use the following sequence for non-trivial features, behavior changes, public API changes, and cross-platform work:

1. Create `specs/<feature>/spec.md` with user value, scope, prioritized scenarios, acceptance criteria, constraints, and success criteria.
2. Resolve ambiguity in `specs/<feature>/research.md` before planning. Ask the user when repository evidence is insufficient.
3. Create `specs/<feature>/plan.md` with affected modules/source sets, design, compatibility impact, test strategy, and documentation impact.
4. Create `specs/<feature>/tasks.md` with exact paths, dependencies, acceptance-test tasks, and validation tasks.
5. Implement the approved tasks without unrelated refactoring. Keep the specification and plan current when discoveries change scope.
6. Run the planned checks, review the complete diff, and update documentation or release notes as needed.

GitHub Copilot users can use the installed Spec-Kit skills in this order: `/speckit-constitution`, `/speckit-specify`, `/speckit-clarify` when needed, `/speckit-plan`, `/speckit-checklist` or `/speckit-analyze` when appropriate, `/speckit-tasks`, `/speckit-implement`, and `/speckit-converge` when needed. The skills are guidance, not a substitute for engineering review.

## Local Verification

Use the checked-in wrapper and JDK 17 or newer:

```bash
./gradlew build
```

Run narrower checks when appropriate, for example:

```bash
./gradlew :core:allTests
./gradlew :test:pixelAndroidDeviceTest
```

Android device tests require an available emulator or device. iOS sample and UI tests require macOS and a compatible Xcode installation. If a check cannot run locally, report the limitation and validate the remaining affected paths.

## Pull Requests

- Explain the user or integrator value and link the relevant specification.
- Summarize affected modules, platforms, public APIs, and compatibility impact.
- List verification commands and results, including unavailable checks.
- Include screenshots or recordings for meaningful UI changes when practical.
- Keep changes focused and do not add GitHub workflows as part of scaffolding or feature work unless explicitly requested.
