# Refine Transaction Page: Add "Items" Field and Internal Calculator

This plan covers two major updates to the `AddTransactionActivity`: adding a dedicated "Items" selection field and replacing the external calculator intent with an internal calculator dialog.

## User Review Required

> [!IMPORTANT]
> - **Internal Calculator**: The app will now have its own calculator dialog. No more switching to external apps! It supports basic math and automatically fills the amount field.
> - **New "Items" Field**: A new bordered box for "Items" will be added between the Amount and Notes fields. It includes a Category icon to quickly pick from your category list.
> - **UI Cleanup**: The "Add Items" button at the bottom will be removed since the new "Items" field makes it redundant.

## Proposed Changes

### UI Components

#### [MODIFY] [activity_add_transaction.xml](file:///C:/Users/SAR/StudioProjects/MyCalendar-New-22-07-2026/app/src/main/res/layout/activity_add_transaction.xml)
- Insert a new `LinearLayout` (`itemsContainer`) between `amountContainer` and `notesContainer`.
- This container will have a bordered background, a "Items" hint/title, and a Category icon.
- Remove the `btnAddItems` button from the bottom `actionButtonsContainer`.

#### [NEW] [dialog_calculator.xml](file:///C:/Users/SAR/StudioProjects/MyCalendar-New-22-07-2026/app/src/main/res/layout/dialog_calculator.xml)
- Implement a 4-column grid for the internal calculator (digits, operators, clear, backspace).

### Logic and Integration

#### [NEW] [CalculatorDialogFragment.java](file:///C:/Users/SAR/StudioProjects/MyCalendar-New-22-07-2026/app/src/main/java/com/example/mycalendar2026sar/CalculatorDialogFragment.java)
- Handle arithmetic logic and result callbacks.

#### [MODIFY] [AddTransactionActivity.java](file:///C:/Users/SAR/StudioProjects/MyCalendar-New-22-07-2026/app/src/main/java/com/example/mycalendar2026sar/AddTransactionActivity.java)
- Bind new "Items" field views.
- Update `btnCalculator` to open `CalculatorDialogFragment`.
- Update `saveTransaction` to use the "Items" field text as the transaction title.

## Verification Plan

### Manual Verification
- **Calculator**: Click the calculator icon, perform a calculation, and verify the result fills the amount field.
- **Items Field**: Verify the "Items" field is correctly positioned. Tap the category icon, select a category, and verify the field updates.
- **Saving**: Save a transaction with an item name and verify it appears correctly in the Expenses list.
