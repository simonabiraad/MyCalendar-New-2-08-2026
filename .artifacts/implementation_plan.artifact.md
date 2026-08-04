# Implementation Plan - Category Password Protection in Secure Box

Add the ability to enable or disable password protection for individual categories within the Secure Box. This ensures that sensitive categories can be further protected beyond the main Secure Box access.

## User Review Required

> [!IMPORTANT]
> - Each category can independently have its password protection enabled or disabled.
> - Disabling protection for a category will require authentication to prevent unauthorized changes.
> - The password used is the same as the one configured in the main app settings (Custom Password or Phone Lock).
> - If a category is protected, it will prompt for authentication every time it's selected.

## Proposed Changes

### [Secure Box Activity]

#### [MODIFY] [SecureBoxActivity.java](file:///C:/Users/SAR/StudioProjects/MyCalendar-New-2-08-2026/app/src/main/java/com/example/mycalendar2026sar/SecureBoxActivity.java)
- Add `securityPrefs` field to store category security settings.
- Initialize `securityPrefs` in `onCreate`.
- Update `showCategoryOptionsDialog` to include a "Set Password" option.
- Implement `showCategorySecurityToggleDialog(String key, String categoryName)` to allow users to toggle protection (Yes/No).
- Implement `verifyThenDisableCatPassword(String categoryName, String prefKey)` to authenticate before disabling category protection.
- Implement `verifyCatAccess(String key, int color)` to handle authentication when a protected category is clicked.
- Refactor `selectCategory` to check for protection and call `verifyCatAccess` if needed.
- Move the actual category selection logic to a new `performSelectCategory(String key, int color)` method.
- Add necessary biometric and authentication imports and logic.

## Verification Plan

### Manual Verification
1.  **Enable Category Password:**
    - Open Secure Box.
    - Long-press a category button (e.g., "Family").
    - Select **Set Password**.
    - Choose **Yes (Require Password)**.
    - Select another category, then select "Family" again. Verify it prompts for a password.
2.  **Disable Category Password:**
    - Long-press the protected category ("Family").
    - Select **Set Password**.
    - Choose **No (No Password)**.
    - Verify it prompts for the current password/biometric *before* disabling.
    - After successful authentication, verify that selecting the category no longer prompts for a password.
3.  **Independent Toggles:**
    - Enable protection for one category and disable it for another.
    - Verify they behave independently.
