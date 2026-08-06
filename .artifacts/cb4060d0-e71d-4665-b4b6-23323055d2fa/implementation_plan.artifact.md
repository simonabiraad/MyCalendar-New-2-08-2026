# Implementation Plan - Vibrant Colorful Category Logos

This plan aims to ensure all categories in the Expenses and Category screens use high-quality, multi-colored "real" logos, and to fix the issue where icons appear monochromatic (single-colored).

## User Review Required

> [!IMPORTANT]
> - I will disable automatic icon tinting in the category lists to ensure the "real and colored" designs are visible.
> - I will expand the logo mapping to ensure more specific icons are matched to category names (e.g., "Air Tickets" getting a Plane icon, "Bonus" getting a Money icon).

## Proposed Changes

### 1. Unified Logo Logic

#### [MODIFY] [CategoryActivity.java](file:///C:/Users/simon/StudioProjects/MyCalendar-New-2-08-2026/app/src/main/java/com/example/mycalendar2026sar/CategoryActivity.java)
- **Refine `getCategoryLogo(String name)`**:
    - Add mappings for "Air Tickets", "Auto Rickshaw", "Bike", "Bills", "Cable TV", "Car", "Car Insurance", "Card Fee", "Cigarette", "Clothes", etc.
    - Ensure keywords are checked efficiently (e.g., "Air" or "Tickets" -> Plane icon).
- **Update `CategoryAdapter`**:
    - Call `holder.logoImg.setImageTintList(null);` in `onBindViewHolder` to prevent the system from applying a single color tint to the colorful logos.

### 2. Expenses Grid Update

#### [MODIFY] [ExpensesActivity.java](file:///C:/Users/simon/StudioProjects/MyCalendar-New-2-08-2026/app/src/main/java/com/example/mycalendar2026sar/ExpensesActivity.java)
- Update the `getCategoryLogo` method to match the comprehensive list in `CategoryActivity`.
- In the `showAddTransactionDialog` adapter, call `logo.setImageTintList(null);` to preserve the vibrant colors in the selection grid.

## Verification Plan

### Manual Verification
1.  Open **Expenses**.
2.  Tap **Cach In**.
3.  **Expected Result**: The selection grid shows vibrant, multi-colored logos (not just green ones).
4.  Navigate to the **Category** screen (from the overflow menu).
5.  **Expected Result**: The long list of categories displays colorful icons for "Air Tickets" (Plane), "Salary" (Cash), "Food" (Burger), etc., and they are not tinted blue or grey.
6.  Search for a category (e.g., "Fuel") and verify the correct colorful icon remains.
