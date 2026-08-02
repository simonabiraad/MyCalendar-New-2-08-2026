# Implementation Plan - Fix Scrolling in Sticky Note Editor

The user wants to be able to scroll up and down in the sticky note edit dialog even when clicking/touching the "empty space" (background).

## Proposed Changes

### [Component Name] SecureBoxActivity

#### [MODIFY] [SecureBoxActivity.java](file:///C:/Users/simon/StudioProjects/MyCalendar-New-20-07-2026/app/src/main/java/com/example/mycalendar2026sar/SecureBoxActivity.java)

- In `showEditFullPage`:
    - Create a `ScrollView` and set it as the root of the dialog's content.
    - Set `android:fillViewport="true"` on the `ScrollView` to ensure it stretches to the full height of the dialog.
    - Wrap the existing `LinearLayout` (which contains the title, content, and buttons) inside this `ScrollView`.
    - Modify the `contentEdit` (EditText) layout parameters:
        - Change height from `0` to `WRAP_CONTENT`.
        - Keep or adjust the `weight` to ensure it still occupies available space, but allowing the `ScrollView` to handle the overall vertical scrolling of the entire page.
        - Set `contentEdit.setNestedScrollingEnabled(false)` to let the outer `ScrollView` take priority for scrolling when touching the text area, providing a more consistent "page-wide" scrolling feel.

## Verification Plan

### Manual Verification
1. Open the "Secure Box" (Sticky Notes).
2. Click a sticky note to open the full-screen editor.
3. Add enough text so that it exceeds the screen height OR keep it short.
4. Attempt to scroll by touching the title area, the text area, and the "empty space" (background areas).
5. Verify that the entire content (Title, Content, and Buttons) scrolls smoothly as a single page.
6. Verify that "Save" and "Cancel" buttons are still accessible (either at the bottom of the scroll or at the bottom of the screen).
