# Development workflow

This repository uses a low-noise CI workflow for Android development.

## Branching

- `main` is the stable integration branch.
- Beta work is collected on a version branch such as `beta/2.0.0-beta04`.
- Focused work may use short-lived branches such as `feature/<scope>` or `fix/<scope>`.
- Avoid direct development commits on `main`.

## Commit and push discipline

Group related edits locally before pushing. A push should represent a coherent checkpoint, not a single tiny edit.

Recommended checkpoints:
1. Data/schema change is internally consistent.
2. Domain logic compiles against the updated model.
3. UI flow is wired to the updated domain state.
4. Unit tests for the affected logic are added or updated.
5. The feature is ready for CI verification.

Small corrective commits are acceptable after a CI failure, but repeated speculative pushes should be avoided.

## Pull requests

- Open a Pull Request when a coherent slice is ready for verification.
- CI runs for Android-relevant changes on Pull Requests targeting `main`.
- Merge only after CI passes.
- Prefer squash merge so the history records one logical change rather than many intermediate fixes.

## CI behavior

The Android workflow runs only when Android-relevant files change.

For a given Pull Request or branch, a newer run cancels the older in-progress run. This prevents stale builds from consuming time.

Pull Request runs execute unit tests and build the debug APK but do not upload the APK artifact.

Pushes to `main` and manual workflow runs execute the same checks and upload the debug APK.

## beta04

Continue beta04 on `beta/2.0.0-beta04`. Accumulate related changes there and push at logical checkpoints. Use short-lived feature/fix branches only when isolation is useful.
