# Implementation Plan - Add Expenses to Main Menu

The user wants to add an "Expenses" option to the main popup menu in `MainActivity`, which will open the Expenses section directly.

## User Review Required

> [!IMPORTANT]
> - The new "Expenses" menu item will be placed after "Secure Box" in the menu.
> - It will use a colorful icon (`ic_menu_cash_in_color`) to match the existing menu style.

## Proposed Changes

### [Menu]

#### [MODIFY] [main_popup_menu.xml](file:///C:/Users/SAR/StudioProjects/MyCalendar-New-2-08-2026/app/src/main/res/menu/main_popup_menu.xml)
- Add a new menu item for Expenses:
  ```xml
  <item
      android:id="@+id/action_expenses"
      android:icon="@drawable/ic_menu_cash_in_color"
      android:title="Expenses" />
  ```

### [MainActivity]

#### [MODIFY] [MainActivity.java](file:///C:/Users/SAR/StudioProjects/MyCalendar-New-2-08-2026/app/src/main/java/com/example/mycalendar2026sar/MainActivity.java)
- In the `OnMenuItemClickListener` for the main menu, add a branch to handle `R.id.action_expenses`:
  ```java
  else if (id == R.id.action_expenses) {
      launchExpenses();
  }
  ```

## Verification Plan

### Manual Verification
1.  Open the app and tap the main menu button (top-left).
2.  Verify that "Expenses" is now an option in the list with a colorful icon.
3.  Tap "Expenses" and verify that the Expenses section opens directly.
