#!/data/data/com.termux/files/usr/bin/bash

set -u

echo
echo "=========================================="
echo "        JARVIS V5 FINAL CHECK"
echo "=========================================="
echo

PASS=0
WARN=0
FAIL=0

check_file() {
    if [ -f "$1" ]; then
        echo "[PASS] $1"
        PASS=$((PASS + 1))
    else
        echo "[FAIL] Missing: $1"
        FAIL=$((FAIL + 1))
    fi
}

check_text() {
    FILE="$1"
    TEXT="$2"
    LABEL="$3"

    if grep -qF "$TEXT" "$FILE" 2>/dev/null; then
        echo "[PASS] $LABEL"
        PASS=$((PASS + 1))
    else
        echo "[FAIL] $LABEL"
        FAIL=$((FAIL + 1))
    fi
}

echo "1. Core Java files"
echo "------------------------------------------"

check_file "app/src/main/java/com/jarvis/app/MainActivity.java"
check_file "app/src/main/java/com/jarvis/app/SettingsActivity.java"
check_file "app/src/main/java/com/jarvis/app/AccessCenterActivity.java"
check_file "app/src/main/java/com/jarvis/app/ChatMessage.java"
check_file "app/src/main/java/com/jarvis/app/ChatAdapter.java"
check_file "app/src/main/java/com/jarvis/app/ChatController.java"
check_file "app/src/main/java/com/jarvis/app/ChatStorage.java"
check_file "app/src/main/java/com/jarvis/app/ChatRepository.java"
check_file "app/src/main/java/com/jarvis/app/ApiClient.java"
check_file "app/src/main/java/com/jarvis/app/ActionParser.java"
check_file "app/src/main/java/com/jarvis/app/ActionDispatcher.java"
check_file "app/src/main/java/com/jarvis/app/ActionExecutor.java"
check_file "app/src/main/java/com/jarvis/app/JarvisActionReceiver.java"
check_file "app/src/main/java/com/jarvis/app/JarvisPlanner.java"
check_file "app/src/main/java/com/jarvis/app/MemoryManager.java"
check_file "app/src/main/java/com/jarvis/app/ActionHistory.java"
check_file "app/src/main/java/com/jarvis/app/VoiceManager.java"
check_file "app/src/main/java/com/jarvis/app/TextToSpeechManager.java"
check_file "app/src/main/java/com/jarvis/app/PermissionManager.java"
check_file "app/src/main/java/com/jarvis/app/AttachmentManager.java"
check_file "app/src/main/java/com/jarvis/app/ImageUtils.java"
check_file "app/src/main/java/com/jarvis/app/CalendarManager.java"
check_file "app/src/main/java/com/jarvis/app/ContactsManager.java"
check_file "app/src/main/java/com/jarvis/app/AppLauncher.java"
check_file "app/src/main/java/com/jarvis/app/NotificationHelper.java"
check_file "app/src/main/java/com/jarvis/app/SettingsStorage.java"

echo
echo "2. Layout files"
echo "------------------------------------------"

check_file "app/src/main/res/layout/activity_main.xml"
check_file "app/src/main/res/layout/item_message_user.xml"
check_file "app/src/main/res/layout/item_message_ai.xml"
check_file "app/src/main/res/layout/item_typing.xml"
check_file "app/src/main/res/layout/activity_access_center.xml"
check_file "app/src/main/res/layout/settings_activity.xml"

echo
echo "3. Drawable files"
echo "------------------------------------------"

check_file "app/src/main/res/drawable/bg_user_message.xml"
check_file "app/src/main/res/drawable/bg_ai_message.xml"
check_file "app/src/main/res/drawable/bg_input.xml"
check_file "app/src/main/res/drawable/bg_quick_action.xml"
check_file "app/src/main/res/drawable/bg_send.xml"

echo
echo "4. Manifest"
echo "------------------------------------------"

check_file "app/src/main/AndroidManifest.xml"

check_text \
    "app/src/main/AndroidManifest.xml" \
    "android.permission.RECORD_AUDIO" \
    "Microphone permission"

check_text \
    "app/src/main/AndroidManifest.xml" \
    "android.permission.CAMERA" \
    "Camera permission"

check_text \
    "app/src/main/AndroidManifest.xml" \
    "android.permission.CALL_PHONE" \
    "Phone permission"

check_text \
    "app/src/main/AndroidManifest.xml" \
    "android.permission.SEND_SMS" \
    "SMS permission"

check_text \
    "app/src/main/AndroidManifest.xml" \
    "android.permission.READ_CONTACTS" \
    "Contacts permission"

check_text \
    "app/src/main/AndroidManifest.xml" \
    "android.permission.READ_CALENDAR" \
    "Calendar permission"

check_text \
    "app/src/main/AndroidManifest.xml" \
    "android.permission.ACCESS_FINE_LOCATION" \
    "Location permission"

check_text \
    "app/src/main/AndroidManifest.xml" \
    "android.permission.POST_NOTIFICATIONS" \
    "Notification permission"

check_text \
    "app/src/main/AndroidManifest.xml" \
    ".SettingsActivity" \
    "Settings activity"

check_text \
    "app/src/main/AndroidManifest.xml" \
    ".AccessCenterActivity" \
    "Access Center activity"

check_text \
    "app/src/main/AndroidManifest.xml" \
    ".JarvisActionReceiver" \
    "Action receiver"

if grep -q "ReminderReceiver" app/src/main/AndroidManifest.xml; then
    echo "[WARN] Legacy ReminderReceiver still exists in manifest"
    WARN=$((WARN + 1))
else
    echo "[PASS] Legacy ReminderReceiver removed"
    PASS=$((PASS + 1))
fi

echo
echo "5. Main UI IDs"
echo "------------------------------------------"

check_text \
    "app/src/main/res/layout/activity_main.xml" \
    "@+id/messageInput" \
    "Message input"

check_text \
    "app/src/main/res/layout/activity_main.xml" \
    "@+id/clearInputButton" \
    "Clear input button"

check_text \
    "app/src/main/res/layout/activity_main.xml" \
    "@+id/clearChatButton" \
    "Clear chat button"

check_text \
    "app/src/main/res/layout/activity_main.xml" \
    "@+id/voiceButton" \
    "Voice button"

check_text \
    "app/src/main/res/layout/activity_main.xml" \
    "@+id/attachButton" \
    "Attachment button"

check_text \
    "app/src/main/res/layout/activity_main.xml" \
    "@+id/chatContainer" \
    "Chat container"

echo
echo "6. API architecture"
echo "------------------------------------------"

check_text \
    "app/src/main/java/com/jarvis/app/ApiClient.java" \
    "/api/plan" \
    "Planner API endpoint"

check_text \
    "app/src/main/java/com/jarvis/app/MainActivity.java" \
    "sendChat" \
    "MainActivity planner communication"

check_text \
    "app/src/main/java/com/jarvis/app/ActionDispatcher.java" \
    "dispatchAll" \
    "Action dispatcher"

check_text \
    "app/src/main/java/com/jarvis/app/ActionConfirmationDialog.java" \
    "show" \
    "Confirmation dialog"

echo
echo "7. Backend"
echo "------------------------------------------"

if [ -f "../jarvis-brain/app.py" ]; then

    check_text \
        "../jarvis-brain/app.py" \
        "/api/plan" \
        "Backend /api/plan"

else

    echo "[WARN] ../jarvis-brain/app.py not found"
    WARN=$((WARN + 1))

fi

echo
echo "8. Git"
echo "------------------------------------------"

if git status --short >/dev/null 2>&1; then

    echo "[PASS] Git repository detected"
    PASS=$((PASS + 1))

    echo
    echo "Changed files:"
    git status --short

else

    echo "[FAIL] Git repository not detected"
    FAIL=$((FAIL + 1))

fi

echo
echo "=========================================="
echo "             FINAL RESULT"
echo "=========================================="
echo
echo "PASS : $PASS"
echo "WARN : $WARN"
echo "FAIL : $FAIL"
echo

if [ "$FAIL" -eq 0 ]; then

    echo "JARVIS verification completed."
    echo "No missing required files were detected."
    echo
    echo "NEXT:"
    echo "Push the project and let GitHub Actions build the APK."

else

    echo "JARVIS verification found failures."
    echo "Fix the FAIL entries before building."

fi

echo
echo "=========================================="
