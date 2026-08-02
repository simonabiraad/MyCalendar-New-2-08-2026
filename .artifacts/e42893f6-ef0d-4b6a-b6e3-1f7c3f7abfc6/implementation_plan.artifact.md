# Implementation Plan - Add Category Page and Navigation

Add a "Category" navigation button to the Expenses header and create a new Category management page with toggleable "Expense" and "Income" views, featuring a comprehensive list of expense categories.

## User Review Required

> [!IMPORTANT]
> - I will add a "Category" button in the top header row of the Expenses screen, specifically next to the search bar.
> - A new `CategoryActivity` will be created to host the category lists.
> - The requested 61 expense categories will be displayed in a scrollable list/grid, each with a representative icon.
> - Since there are many categories, I will use a `RecyclerView` with a `GridLayoutManager` for optimal performance and display.

## Proposed Changes

### [Expenses Component]

#### [MODIFY] [activity_expenses.xml](file:///C:/Users/simon/StudioProjects/MyCalendar-New-22-07-2026/app/src/main/res/layout/activity_expenses.xml)
- Add a new `ImageButton` (ID: `expensesCategoryButton`) in the top header row, positioned next to the `expensesSearchView`.
- Use a category-related icon (e.g., `@android:drawable/ic_menu_manage`).

#### [MODIFY] [ExpensesActivity.java](file:///C:/Users/simon/StudioProjects/MyCalendar-New-22-07-2026/app/src/main/java/com/example/mycalendar2026sar/ExpensesActivity.java)
- Initialize the Category button and add an `OnClickListener` to launch `CategoryActivity`.

### [Category Component]

#### [NEW] [CategoryActivity.java](file:///C:/Users/simon/StudioProjects/MyCalendar-New-22-07-2026/app/src/main/java/com/example/mycalendar2026sar/CategoryActivity.java)
- Handle switching between "Expense" and "Income" categories.
- Manage the list of categories using a `RecyclerView` and a custom adapter.

#### [NEW] [activity_category.xml](file:///C:/Users/simon/StudioProjects/MyCalendar-New-22-07-2026/app/src/main/res/layout/activity_category.xml)
- Top row with "Expense" and "Income" toggle buttons (Material style).
- `RecyclerView` to display the list of categories with icons.

#### [NEW] [item_category.xml](file:///C:/Users/simon/StudioProjects/MyCalendar-New-22-07-2026/app/src/main/res/layout/item_category.xml)
- Layout for a single category item (Icon + Text).

#### [MODIFY] [strings.xml](file:///C:/Users/simon/StudioProjects/MyCalendar-New-22-07-2026/app/src/main/res/values/strings.xml)
- Add string resources for all 61 categories and the new page elements.

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/simon/StudioProjects/MyCalendar-New-22-07-2026/app/src/main/AndroidManifest.xml)
- Register `CategoryActivity`.

## Verification Plan

### Manual Verification
- Deploy the app and navigate to the Expenses screen.
- Verify the "Category" button is visible and functional.
- In the Category page:
    - Verify the "Expense" and "Income" buttons toggle correctly.
    - Verify the long list of expense categories (Air Tickets, etc.) is displayed with icons.
    - Ensure the list is scrollable and looks consistent.