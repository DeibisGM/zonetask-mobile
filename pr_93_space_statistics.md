## 📌 Summary

This PR integrates the space-level task completion statistics screen into the Android app, completing the frontend side of issue #93. It adds the `GET /api/spaces/{spaceId}/statistics` endpoint call and exposes the results in a new `SpaceStatisticsScreen` accessible from the space detail screen. The individual user statistics screen was already in place; this PR completes coverage by adding the aggregated space-level view.

## ✅ Related Issue

Main issue: Tracking and Statistics: View task completion percentage #93

Sub-issues:
- Define completion percentage formula #217
- Calculate percentages by user and by space #218
- Present the data in a simple visual format #219
- Ensure calculations use the selected date range #220

## 🧩 Changes Made

- Added `SpaceStatisticsResponse` DTO with fields: `spaceId`, `period`, `dateFrom`, `dateTo`, `totalAssigned`, `completedTasks`, `overdueTasks`, `pendingTasks`, `completionRate`.
- Added `getSpaceStatistics()` to `StatisticsApiService` targeting the new `GET /api/spaces/{spaceId}/statistics` endpoint.
- Added `getSpaceStatistics()` to `StatisticsRepository` with full HTTP and network error handling.
- Added `SpaceStatisticsUiState` and `SpaceStatisticsViewModel` with period selector (Week / Month / Year / Custom) and custom date range support.
- Added `SpaceStatisticsScreen` composable displaying a hero completion-rate card and a 2×2 metric grid (Total Assigned, Completed, Overdue, Pending), color-coded by performance threshold.
- Added `SPACE_STATISTICS = "space_statistics/{spaceId}"` route and `spaceStatistics()` builder to `SpacesDestinations`.
- Added `onOpenSpaceStatistics: (spaceId: Int) -> Unit` action to `SpacesNavActions`.
- Registered the `SpaceStatisticsScreen` composable in `SpacesNavGraph`.
- Added "Space Statistics" navigation row to `SpaceDetailScreen`, placed below the existing "My Statistics" row.
- Wired `onOpenSpaceStatistics` in `AppNavHost` to navigate to the new route.

## ✅ Acceptance Criteria Covered

- Space-level task completion percentage is viewable via `GET /api/spaces/{spaceId}/statistics`.
- Completion rate is displayed using the formula `completedTasks / totalAssigned * 100`, rounded to 1 decimal.
- Statistics can be filtered by period (week, month, year) and by custom date range (`date_from` / `date_to`).
- The displayed values match the selected period filter.
- `completionRate` shows `0.0%` when no tasks are assigned in the period (handled by the backend, displayed correctly by the UI).
- Space Statistics screen is accessible from the Space Detail screen.

## 📁 Files Changed

- `app/src/main/java/com/app/zonetask/core/AppConstants.kt`
- `app/src/main/java/com/app/zonetask/data/remote/dto/SpaceStatisticsResponse.kt`
- `app/src/main/java/com/app/zonetask/data/remote/service/StatisticsApiService.kt`
- `app/src/main/java/com/app/zonetask/data/remote/repository/StatisticsRepository.kt`
- `app/src/main/java/com/app/zonetask/ui/screens/statistics/SpaceStatisticsUiState.kt`
- `app/src/main/java/com/app/zonetask/ui/screens/statistics/SpaceStatisticsViewModel.kt`
- `app/src/main/java/com/app/zonetask/ui/screens/statistics/SpaceStatisticsScreen.kt`
- `app/src/main/java/com/app/zonetask/navigation/spaces/SpacesDestinations.kt`
- `app/src/main/java/com/app/zonetask/navigation/spaces/SpacesNavActions.kt`
- `app/src/main/java/com/app/zonetask/navigation/spaces/SpacesNavGraph.kt`
- `app/src/main/java/com/app/zonetask/ui/screens/spaces/SpaceDetailScreen.kt`
- `app/src/main/java/com/app/zonetask/navigation/AppNavHost.kt`

## 🧪 Testing

- Pending Android build verification.
- Pending manual verification of the Space Statistics screen with a valid space and default period (month).
- Pending verification of period filters (week, month, year).
- Pending verification of custom date range (`date_from` / `date_to`) and Apply button behavior.
- Pending verification of loading and error states (no connection, 404 space not found, 400 invalid range).
- Pending verification that `completionRate` shows `0.0%` when no tasks are assigned in the selected period.
- Pending verification of back navigation from Space Statistics to Space Detail.

## 📸 Evidence

| # | Case | Description | Image / link |
|---|------|-------------|--------------|
| 1 |      |             |              |
| 2 |      |             |              |

## 📝 Notes

- `SpaceStatisticsScreen` reuses the same `StatsPeriod` enum defined in `IndividualStatisticsUiState.kt` — no duplication.
- The space-level view aggregates all members; no per-user filtering is applied at this endpoint.
- Depends on the backend PR that added `GET /api/spaces/{spaceId}/statistics` (issue #93 backend side).
