# AGENTS.md — Constellation Mobile SDK

## Dev environment tips
1. Do not hallucinate. When a task requires specific data that is not provided and cannot be reliably inferred from existing code or fixtures, ask the user before proceeding.
2. Do not change the logic of the code while doing migrations or refactoring, unless explicitly asked for.
3. After `silent on` is typed by user you cannot send any messages in the conversation and only modify files in the workspace. You can still report errors or ask clarifying questions if absolutely necessary to proceed safely.
4. After `silent off` is typed by user you can resume normal chat output and explanations.
5. After `load instruction agents` is typed by user you should load ai-instructions/AGENTS.md. Otherwise, never load that file.