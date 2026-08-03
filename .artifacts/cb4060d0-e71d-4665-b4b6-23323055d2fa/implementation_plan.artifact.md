# Implementation Plan - Display Images in Full-Page Sticky Note Viewer

The user wants to see the actual image instead of the text link (path) when opening a sticky note that contains an image in the Secure Box.

## Proposed Changes

### Secure Box Component

#### [MODIFY] [SecureBoxActivity.java](file:///C:/Users/simon/StudioProjects/MyCalendar-New-2-08-2026/app/src/main/java/com/example/mycalendar2026sar/SecureBoxActivity.java)
- **Modify `showEditFullPage` method**:
    - Add logic to detect if the `currentContent` follows the image path format `[IMG:path]`.
    - If it is an image note:
        - Create an `ImageView` and set its image from the extracted path.
        - Add the `ImageView` to the full-page dialog layout.
        - Ensure the `EditText` for content is either hidden or replaced, while still allowing the title to be edited and saved.
    - If it is a text note, continue showing the `EditText` as usual.

## Verification Plan

### Manual Verification
1.  Open **Secure Box**.
2.  Create a new sticky note with an image (using Camera, Gallery, or PDF).
3.  Tap on the newly created note in the grid to open it full-page.
4.  **Expectation**: The actual image should be visible in the viewer, not the `[IMG:...]` text.
5.  Edit the title of the image note and tap **Save**.
6.  **Expectation**: The note should update its title while preserving the image content.
7.  Repeat with a normal text note to ensure no regression.
