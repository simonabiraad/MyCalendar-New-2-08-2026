# Standardize Note Input Borders

The user wants to replace the standard line-based note input with a bordered design (using `summary_border`) in both the main screen and the notification bar. The voice icon must be inside this border, and the height should be approximately two lines.

## User Review Required

> [!IMPORTANT]
> - **Unified Design**: The main screen's note input will now look like the "Secure Box" and "Add Transaction" inputs—a clean white border with the voice icon on the right.
> - **Height Restriction**: The input area height will be fixed to ~50-60dp to ensure it occupies exactly about two lines of text space.
> - **Voice Icon**: The microphone logo will be moved inside the bordered box.

## Proposed Changes

### UI Components

#### [MODIFY] [activity_main.xml](file:///C:/Users/SAR/StudioProjects/MyCalendar-New-22-07-2026/app/src/main/res/layout/activity_main.xml)
- Change `noteInputContainer` background to `@drawable/summary_border`.
- Set `noteInput` background to `@null`.
- Move `voiceNoteButton` to the right side of the `noteInput` within the container.
- Set container height to `50dp` or `60dp` (two lines).

#### [MODIFY] [notification_widget.xml](file:///C:/Users/SAR/StudioProjects/MyCalendar-New-22-07-2026/app/src/main/res/layout/notification_widget.xml)
- Verify and ensure the `RemoteViews` layout matches the "two lines" height (adjusting from `64dp` to `50dp` if `64dp` feels too tall).
- Ensure the voice button and text area are perfectly centered within the border.

## Verification Plan

### Manual Verification
- **Main Screen**: Open the app and confirm the note entry at the bottom has a white border and the microphone icon is inside it on the right.
- **Notification**: Toggle the Quick Note Bar and pull down the notification shade. Verify the bar has a similar bordered look and is roughly the height of two lines of text.
- **Functionality**: Tap the voice icon in both places to ensure they still trigger speech-to-text correctly.
