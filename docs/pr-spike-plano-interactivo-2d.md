## 📌 Summary

This PR delivers the research spike for implementing a 2D interactive floor plan in the mobile app using Kotlin and Jetpack Compose. No application code is included — the deliverables are research documents that establish the technical baseline before implementation begins.

The spike evaluates rendering alternatives (Compose Canvas, Coil + SVG, AndroidView + AndroidSVG) and gesture handling strategies for pan/zoom/drag, considering that the backend is an existing .NET REST endpoint. The final recommendation is documented: use Compose Canvas for rendering, `pointerInput` with `detectTransformGestures`/`detectDragGestures` for interaction, and direct API consumption via Retrofit for the MVP.

---

## ✅ Related Issue

Main issue: Research: Interactive 2D Floor Plan in Android with Kotlin and Jetpack Compose #297

Sub-issues:
- Evaluation of 2D Rendering Alternatives in Jetpack Compose #298
- Research on Touch Gestures for the Floor Plan #299
- Consumption and Persistence of Zones #300

---

## 🧩 Changes Made

- Added the full spike research document (`spike-plano-interactivo-2d.md`) covering all 10 research questions, comparative analysis of rendering alternatives, gesture handling patterns, and architecture options.
- Added a short recommendation document (`spike-decision-recomendada.md`) with the suggested technical approach and MVP justification.
- Exported an additional PDF version of the recommendation document (`decision-recomendada.pdf`) for easier sharing outside the repository.
- Rewrote sub-issues #298, #299, and #300 originally written for React Native to align with the actual project stack: Kotlin + Jetpack Compose.
- Adjusted the scope of issue #300 to evaluate data consumption from the existing .NET endpoint and define the most suitable persistence flow for the MVP.
- Also exported the spike document as a PDF with the same content as the Markdown version for easier external sharing.

---

## ✅ Acceptance Criteria Covered

- At least 3 rendering alternatives documented with pros and cons (Compose Canvas, Coil + SVG, AndroidView + AndroidSVG, plus ImageVector as a fourth reference).
- Comparative table including performance, maintainability, documentation quality, Compose integration, gesture support, and learning curve.
- Gesture handling approach documented using `pointerInput` with `detectTransformGestures` and `detectDragGestures`, including known conflicts (zone drag vs global pan) and resolution using `change.consume()` and layered pointer input modifiers.
- Coordinate system in Compose Canvas documented, including model, canvas, and screen transformation spaces, as well as hit-testing logic.
- Data flow with the .NET endpoint documented: contract (`GET`/`POST`/`PUT`/`DELETE /spaces/{id}/zones`), DTO format, domain model (`Zone`), and Retrofit interface.
- Final recommendation documented with rationale, risks, and mitigation strategies.

---

## 📁 Files Changed

<!-- No full repository paths available — to be filled manually once committed -->
- `docs/spike-plano-interactivo-2d.md`
- `docs/spike-plano-interactivo-2d.pdf`
- `docs/spike-decision-recomendada.md`
- `docs/spike-decision-recomendada.pdf`

---

## 🧪 Testing

This PR contains no application code, so there is nothing to build or run. Validation is based on documentation review:

- Pending technical review of the spike document by the team.
- Pending alignment with the backend team regarding the proposed .NET endpoint contract (`GET`/`POST`/`PUT`/`DELETE /spaces/{id}/zones`) and DTO format.

---

## 📸 Evidence

| # | Case | Description | Image / link |
|---|------|-------------|-----------------|
| 1 | Spike document (Markdown) | Full research document covering all 10 research questions | `docs/spike-plano-interactivo-2d.md` |
| 2 | Spike document (PDF) | Same content as Markdown version exported for easier sharing | `docs/spike-plano-interactivo-2d.pdf` |
| 3 | Recommendation summary (Markdown) | Short document with the recommended MVP technical approach | `docs/spike-decision-recomendada.md` |
| 4 | Recommendation summary (PDF) | PDF version of the recommendation document for easier sharing | `docs/spike-decision-recomendada.pdf` |

---

## 📝 Notes

- This is a **research spike**, not an implementation. No production code is included.
- The original sub-issues (#298, #299, #300) referenced React Native libraries and were rewritten to match the actual Kotlin + Jetpack Compose stack.
- The proposed .NET endpoint contract is **a suggestion, not a confirmed backend agreement**, and must be validated with the backend team before implementation.
- The final decision regarding persistence (API-only vs API + Room) remains open for discussion with product and design.
- Next step after merge: create a `spike/plano-2d-prototipo` branch and build a minimal prototype (Canvas with draggable rectangles, pan/zoom, and API consumption) to validate the approach with UX.

**📌 Note:** The main source of truth for this PR is the Markdown (`.md`) documentation files included in the repository.