# Implementation Plan: Rename Expense Categories

Rename the navigation buttons and their associated activities to reflect the new categories: Income, Expense, and Transfer.

## User Review Required

> [!IMPORTANT]
> To keep the codebase clean, I will also rename the Activity classes and Layout files.
> - "Daily" becomes **Income**
> - "Weekly" becomes **Expense**
> - "Monthly" becomes **Transfer**

## Proposed Changes

### Resources Refactoring

#### [MODIFY] [strings.xml](file:///C:/Users/simon/StudioProjects/MyCalendar-New-23-07-2026/app/src/main/res/values/strings.xml)
- Rename `daily` string to `income` ("Income").
- Rename `weekly` string to `expense` ("Expense").
- Rename `monthly` string to `transfer` ("Transfer").

#### [MODIFY] [activity_expenses.xml](file:///C:/Users/simon/StudioProjects/MyCalendar-New-23-07-2026/app/src/main/res/layout/activity_expenses.xml)
- Rename button IDs: `dailyButton` -> `incomeButton`, `weeklyButton` -> `expenseButton`, `monthlyButton` -> `transferButton`.
- Update text references to the new string names.

#### [NEW] [activity_income.xml](file:///C:/Users/simon/StudioProjects/MyCalendar-New-23-07-2026/app/src/main/res/layout/activity_income.xml)
- Copy of `activity_daily_expenses.xml` with updated IDs and text.

#### [NEW] [activity_expense_type.xml](file:///C:/Users/simon/StudioProjects/MyCalendar-New-23-07-2026/app/src/main/res/layout/activity_expense_type.xml)
- Copy of `activity_weekly_expenses.xml` with updated IDs and text.

#### [NEW] [activity_transfer.xml](file:///C:/Users/simon/StudioProjects/MyCalendar-New-23-07-2026/app/src/main/res/layout/activity_transfer.xml)
- Copy of `activity_monthly_expenses.xml` with updated IDs and text.

#### [DELETE] [activity_daily_expenses.xml](file:///C:/Users/simon/StudioProjects/MyCalendar-New-23-07-2026/app/src/main/res/layout/activity_daily_expenses.xml)
#### [DELETE] [activity_weekly_expenses.xml](file:///C:/Users/simon/StudioProjects/MyCalendar-New-23-07-2026/app/src/main/res/layout/activity_weekly_expenses.xml)
#### [DELETE] [activity_monthly_expenses.xml](file:///C:/Users/simon/StudioProjects/MyCalendar-New-23-07-2026/app/src/main/res/layout/activity_monthly_expenses.xml)

### Code Refactoring

#### [NEW] [IncomeActivity.java](file:///C:/Users/simon/StudioProjects/MyCalendar-New-23-07-2026/app/src/main/java/com/example/mycalendar2026sar/IncomeActivity.java)
- Renamed from `DailyExpensesActivity`.

#### [NEW] [ExpenseTypeActivity.java](file:///C:/Users/simon/StudioProjects/MyCalendar-New-23-07-2026/app/src/main/java/com/example/mycalendar2026sar/ExpenseTypeActivity.java)
- Renamed from `WeeklyExpensesActivity`.

#### [NEW] [TransferActivity.java](file:///C:/Users/simon/StudioProjects/MyCalendar-New-23-07-2026/app/src/main/java/com/example/mycalendar2026sar/TransferActivity.java)
- Renamed from `MonthlyExpensesActivity`.

#### [DELETE] [DailyExpensesActivity.java](file:///C:/Users/simon/StudioProjects/MyCalendar-New-23-07-2026/app/src/main/java/com/example/mycalendar2026sar/DailyExpensesActivity.java)
#### [DELETE] [WeeklyExpensesActivity.java](file:///C:/Users/simon/StudioProjects/MyCalendar-New-23-07-2026/app/src/main/java/com/example/mycalendar2026sar/WeeklyExpensesActivity.java)
#### [DELETE] [MonthlyExpensesActivity.java](file:///C:/Users/simon/StudioProjects/MyCalendar-New-23-07-2026/app/src/main/java/com/example/mycalendar2026sar/MonthlyExpensesActivity.java)

#### [MODIFY] [ExpensesActivity.java](file:///C:/Users/simon/StudioProjects/MyCalendar-New-23-07-2026/app/src/main/java/com/example/mycalendar2026sar/ExpensesActivity.java)
- Update button click listeners to use new IDs and launch the new Activity classes.

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/simon/StudioProjects/MyCalendar-New-23-07-2026/app/src/main/AndroidManifest.xml)
- Update Activity declarations to the new class names.

## Verification Plan

### Automated Tests
- Build the project to ensure all references are correctly updated.

### Manual Verification
- Open Expenses screen.
- Verify buttons say "Income", "Expense", and "Transfer".
- Click each button and verify they open the correct (renamed) screens.
