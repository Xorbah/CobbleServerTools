# Source state and reconstruction notes

The KantoNPCs-to-NeoForge recreation evolved through a mixture of recovered source checkpoints and targeted compiled-JAR overlays. RC22 onward was built against the latest known-good reconstructed JAR so newer work would not regress older recovered systems.

The files under `src/main/java/` are the latest preserved Java sources for the RC28 work. They are real source used to produce the corresponding replacement classes, but they are not yet the entire historical mod source tree.

The scripts under `scripts/legacy-patch-build/` document how RC25–RC28 were produced during reconstruction. They expect the previous RC JAR as a base. They are kept for provenance and reproducibility of those milestones, not as the desired final build architecture.

The latest compiled artifact is included under `dist/` so the exact tested checkpoint is preserved while the repository is normalized into a full standalone project.
