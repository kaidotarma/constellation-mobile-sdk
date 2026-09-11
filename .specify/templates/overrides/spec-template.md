# Feature Specification: [FEATURE NAME]

**Feature Branch**: `[###-feature-name]`
**Created**: [DATE]
**Status**: Draft
**Input**: [User or issue description]

## Summary

[Describe the value this change delivers for SDK users, application integrators, or maintainers.]

## Scope

### In Scope

- [Capability or behavior included]

### Out of Scope

- [Explicitly excluded behavior]

## User Scenarios & Testing

### User Story 1 – [Brief Title] (Priority: P1)

[Describe the user or integrator journey in plain language.]

**Why this priority**: [Explain the value and priority.]

**Independent Test**: [Explain how this story can be implemented and verified independently.]

**Acceptance Scenarios**:

1. **Given** [initial state], **When** [action], **Then** [expected outcome]
2. **Given** [initial state], **When** [action], **Then** [expected outcome]

### User Story 2 – [Brief Title] (Priority: P2)

[Describe another independently testable journey, or remove this section.]

**Why this priority**: [Explain the value and priority.]

**Independent Test**: [Explain how this story can be verified independently.]

**Acceptance Scenarios**:

1. **Given** [initial state], **When** [action], **Then** [expected outcome]

## Functional Requirements

- **FR-001**: The SDK MUST [requirement].
- **FR-002**: The change MUST preserve [existing behavior or compatibility boundary].
- **FR-003**: Errors MUST [observable and safe failure behavior].

## Non-Functional Requirements

- **NFR-001 Compatibility**: [Public API, version, or migration constraint.]
- **NFR-002 Security**: [Authentication, privacy, or logging constraint.]
- **NFR-003 Performance**: [Relevant latency, memory, rendering, or startup constraint.]
- **NFR-004 Platforms**: [Android, iOS, JVM, and source-set expectations.]

## Affected Areas

- Modules: [for example, `core`, `engine-webview`, `ui-components-cmp`, `ui-renderer-cmp`, or `samples`]
- Source sets: [for example, `commonMain`, `androidMain`, `iosMain`, or `jvmMain`]
- Public APIs or resources: [list or state none]

## Success Criteria

- **SC-001**: [Observable outcome tied to a user story.]
- **SC-002**: [Relevant test or build result.]

## Assumptions and Open Questions

- [Assumption or question. Use `research.md` for investigation details.]
