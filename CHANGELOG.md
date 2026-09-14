# Changelog

## [V2 Release Candidate] - Final Hardening
### Added
- Complete V2 UI/UX redesign featuring professional dark glassmorphism aesthetic.
- Interactive, deterministic Auto-Layout Dependency Map (`/dependencies`) with health/blast-radius correlation.
- Blast Radius projection tool (`/troubleshooting/blast-radius`).
- Evidence-first Root Cause Analysis (RCA) interface (`/troubleshooting/root-cause`).
- Service Investigation Hub mapping (`/services/[serviceId]`).
- Strict TypeScript normalization layer for all API telemetry.

### Changed
- Refactored `dependencies/page.tsx` to handle gracefully empty/malformed edge-only backend contracts.
- Hardened all array iterations (`.map()`, `.filter()`) across the frontend to prevent runtime crashes during transient data-loss periods.
- Re-styled ReactFlow topological elements for clear anomaly visibility.

### Fixed
- Fixed `Runtime TypeError: Cannot read properties of undefined (reading 'map')` in the Dependency Map.
- Fixed ReactFlow canvas collapsing due to `min-h` CSS styles.
- Fixed unchecked properties in Incident and Troubleshooting components.

### Verified
- Runtime verification for AI service on port 8000 (`/health`, `/api/ai/anomaly`, `/api/ai/predict`).
- Runtime verification for Frontend service on port 3000.
- Full test pass across all 14 Maven modules.
- Complete type-check (`npm run typecheck`) and build (`npm run build`) success.
- Architecture frozen for release.
