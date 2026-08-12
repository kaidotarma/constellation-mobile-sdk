# Spec-Driven Development Scaffolding

This directory contains the repository-specific Spec-Kit memory, templates, scripts, and metadata for the Constellation Mobile SDK. The repository uses the GitHub Copilot skills integration with shell helpers.

## Feature Layout

Create one directory per feature under `specs/`:

```text
specs/<feature>/
├── spec.md
├── research.md          # when clarification or investigation is needed
├── plan.md
├── tasks.md
├── data-model.md        # when the feature introduces or changes data
├── quickstart.md        # when an integration or usage guide is useful
├── contracts/           # when module or process contracts are involved
└── checklists/          # when a requirements review checklist is requested
```

## Templates

`.specify/templates/*.md` are the **pristine, upstream Spec-Kit templates** installed by the CLI (`spec-template.md`, `plan-template.md`, `tasks-template.md`, `checklist-template.md`, `constitution-template.md`). Their SHA-256 hashes are tracked in `.specify/integrations/speckit.manifest.json` and `.specify/memory/.constitution-template.json`. **Do not edit these files directly** — doing so breaks drift detection and future `specify` upgrades.

Repository-specific customizations to the spec/plan/tasks/checklist templates live in `.specify/templates/overrides/`, following Spec-Kit's built-in override resolution (`.specify/scripts/bash/common.sh:resolve_template`). Skills automatically prefer the override when one exists, falling back to the core template otherwise.

`research-template.md`, `data-model-template.md`, `quickstart-template.md`, and `templates/contracts/README.md` are supplementary reference templates for this repository (not part of core Spec-Kit and not resolved by `resolve_template`); copy them into a feature directory manually when useful.

The helper scripts under `.specify/scripts/bash/` create and resolve feature artifacts.

## Workflow

Use the GitHub Copilot skills in `.github/skills/` in this order:

1. `/speckit-constitution`
2. `/speckit-specify`
3. `/speckit-clarify` when requirements need clarification
4. `/speckit-plan`
5. `/speckit-checklist` or `/speckit-analyze` when appropriate
6. `/speckit-tasks`
7. `/speckit-implement`
8. `/speckit-converge` when implementation leaves documented work incomplete

The project metadata records the Copilot integration and Spec-Kit version. The bundled Spec-Kit workflow descriptor is available under `.specify/workflows/`; it is not a GitHub Actions workflow.
