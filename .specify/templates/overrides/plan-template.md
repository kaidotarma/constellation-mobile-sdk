# Implementation Plan: [FEATURE NAME]

**Branch**: `[###-feature-name]` | **Date**: [DATE] | **Spec**: `specs/[###-feature-name]/spec.md`

## Technical Context

- **Language/runtime**: Kotlin 2.4.0, JavaScript where applicable, JDK 17+
- **Build**: Gradle 9.4.0 via `./gradlew`
- **Architecture**: Kotlin Multiplatform with `commonMain`, Android, iOS, and JVM targets
- **Affected modules**: [list modules]
- **Testing**: [unit, device, sample, iOS, JavaScript, or manual checks]
- **Dependencies**: [new dependencies or `None`]

## Constitution Check

- [ ] Specification scenarios are independently testable.
- [ ] Module and source-set boundaries are preserved.
- [ ] Public API and compatibility impact are documented.
- [ ] Verification covers affected platforms and failure paths.
- [ ] Authentication, secrets, and diagnostic data remain safe.
- [ ] Documentation and release impact are addressed.

## Repository Analysis

### Relevant Existing Files

- `[path]`: [why it matters]

### Current Behavior

[Describe the existing flow and constraints before changes.]

## Proposed Design

[Describe the smallest design that satisfies the specification. Include data flow, component or API boundaries, platform differences, and fallback behavior.]

## API and Compatibility

- Public API changes: [none or details]
- Behavior changes: [none or details]
- Migration or release notes: [none or details]

## Test Strategy

| Scenario | Test type | Location or command | Expected result |
| --- | --- | --- | --- |
| [scenario] | [unit/device/sample/manual] | `[path or command]` | [result] |

## Documentation Plan

- [ ] Update relevant README or `docs/` page.
- [ ] Add or update an integration example if public behavior changes.
- [ ] Add release documentation when the change affects a published artifact.

## Risks and Alternatives

- **Risk**: [risk and mitigation]
- **Alternative considered**: [alternative and why it was not selected]
