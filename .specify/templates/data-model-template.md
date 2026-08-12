# Data Model: [FEATURE NAME]

**Feature**: `specs/[###-feature-name]/spec.md`

## Entities

### [Entity Name]

- **Purpose**: [What the entity represents]
- **Fields**:
  - `[field]`: [type, constraints, and meaning]
- **Relationships**: [Related entities or `None`]
- **Lifecycle**: [Creation, updates, completion, cancellation, and error behavior]

## State Transitions

| Current state | Event | Next state | Invariants |
| --- | --- | --- | --- |
| [state] | [event] | [state] | [invariant] |

## Serialization and Compatibility

- [Serialization format, resource, or API contract]
- [Backward-compatibility and migration requirement]
