# Walkthrough - New Rounded Icons for Income and Expense Buttons

I have updated the icons for the **Add Income** and **Add Expense** buttons to use rounded "+" and "-" symbols, providing a more intuitive and visually appealing interface.

## Changes

### [Resources Component]

#### [NEW] [ic_add_circle.xml](file:///C:/Users/simon/StudioProjects/MyCalendar-New-22-07-2026/app/src/main/res/drawable/ic_add_circle.xml)
Created a new custom vector drawable for a "+" inside a solid circle.

#### [NEW] [ic_remove_circle.xml](file:///C:/Users/simon/StudioProjects/MyCalendar-New-22-07-2026/app/src/main/res/drawable/ic_remove_circle.xml)
Created a new custom vector drawable for a "-" inside a solid circle.

### [Expenses Component]

#### [MODIFY] [activity_expenses.xml](file:///C:/Users/simon/StudioProjects/MyCalendar-New-22-07-2026/app/src/main/res/layout/activity_expenses.xml)
Updated the icons for the main Home action buttons:
- **Add Income** now uses the new `@drawable/ic_add_circle`.
- **Add Expense** now uses the new `@drawable/ic_remove_circle`.

## Verification Results

### Automated Tests
- Ran `gradlew app:assembleDebug`: **Build Successful**. Confirmed that the new resources are correctly compiled and referenced in the layout.

### Manual Verification
- Navigated to the Expenses screen.
- Verified that the **Add Income** button displays the "+" circle icon.
- Verified that the **Add Expense** button displays the "-" circle icon.
- Both icons are correctly rendered in white and properly aligned above the button text.