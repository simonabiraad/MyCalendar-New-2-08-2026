# Task: Category Password Protection in Secure Box

- [x] Update `SecureBoxActivity.java`
    - [x] Add imports for Biometric and authentication
    - [x] Add `securityPrefs` field and initialize it
    - [x] Update `showCategoryOptionsDialog` with "Set Password"
    - [x] Implement `showCategorySecurityToggleDialog`
    - [x] Implement `verifyThenDisableCatPassword`
    - [x] Implement `verifyCatAccess`
    - [x] Refactor `selectCategory` and implement `performSelectCategory`
- [x] Verification
    - [x] Test enabling protection for a category
    - [x] Test disabling protection (verify auth requirement)
    - [x] Test independent behavior for different categories
