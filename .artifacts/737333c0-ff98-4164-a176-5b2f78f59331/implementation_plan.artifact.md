# Refine Cash In / Cash Out Design and Functionality

The user wants to refine the recently added `AddTransactionActivity` to improve the layout and add specific behaviors for the calculator and bill attachments.

## User Review Required

> [!IMPORTANT]
> - **Calculator Shortcut**: If the app cannot find a calculator on the device, it will prompt the user to install one from the Play Store.
> - **Bill Attachments**: The "Add Bills" dialog will now show descriptive icons (Camera, Gallery, PDF) next to each option for better clarity.

## Proposed Changes

### UI Components

#### [MODIFY] [activity_add_transaction.xml](file:///C:/Users/SAR/StudioProjects/MyCalendar-New-22-07-2026/app/src/main/res/layout/activity_add_transaction.xml)
- Change "Cach In" and "Cach Out" buttons from vertical to horizontal (same line).
- Add a camera icon drawable to the "Add Bills" button itself.

#### [NEW] [dialog_item_with_icon.xml](file:///C:/Users/SAR/StudioProjects/MyCalendar-New-22-07-2026/app/src/main/res/layout/dialog_item_with_icon.xml)
- Create a reusable layout for dialog items consisting of an icon and text.

#### [MODIFY] [AddTransactionActivity.java](file:///C:/Users/SAR/StudioProjects/MyCalendar-New-22-07-2026/app/src/main/java/com/example/mycalendar2026sar/AddTransactionActivity.java)
- Update calculator click listener to suggest installation if no calculator app is found.
- Update `showBillsOptions` to use the new custom layout with icons.

## Verification Plan

### Manual Verification
- Deploy to device.
- Open **Cash In** page.
- Verify "Cach In" and "Cach Out" buttons are on the same line.
- Click the **Calculator** icon; if no calculator exists, verify the "Install" prompt appears.
- Click **Add Bills**; verify the menu shows icons for Camera, Gallery, and PDF.
