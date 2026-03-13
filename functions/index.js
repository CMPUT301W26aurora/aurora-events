const { onDocumentUpdated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");
admin.initializeApp();

const db = admin.firestore();

/**
 * Firestore-triggered Cloud Function that sends push notifications to entrants
 * when they are moved between registration lists on an Event document.
 *
 * Listens for updates to any document in the "Events" collection and compares
 * the before/after state of each participant list. Any entrant newly added to
 * a list receives a push notification via FCM.
 */
exports.onEventListChange = onDocumentUpdated("Events/{eventId}", async (event) => {
    const before = event.data.before.data().registrationList || {};
    const after  = event.data.after.data().registrationList || {};

    console.log("Function triggered for event:", event.params.eventId);
    console.log("Before selectedList:", JSON.stringify(before.selectedList));
    console.log("After selectedList:", JSON.stringify(after.selectedList));

    await notifyNewEntrants(before.selectedList,  after.selectedList,  "You've been selected!",     "Check the app to confirm your spot.");
    await notifyNewEntrants(before.attendingList, after.attendingList, "You're confirmed!",          "You're now on the attending list.");
    await notifyNewEntrants(before.declinedList,  after.declinedList,  "Invitation declined",        "Your invitation has been declined.");
    await notifyNewEntrants(before.cancelledList, after.cancelledList, "Registration cancelled",     "Your registration has been cancelled.");
    await notifyNewEntrants(before.removedList,   after.removedList,   "Removed from event",         "You have been removed from this event.");
});

/**
 * Finds entrants newly added to a list by comparing its before and after state,
 * then sends a push notification to each new entrant.
 *
 * @param {string[]} beforeList - The list of device IDs before the update.
 * @param {string[]} afterList  - The list of device IDs after the update.
 * @param {string}   title      - The notification title to display on the device.
 * @param {string}   body       - The notification body text to display on the device.
 * @returns {Promise<void>}
 */
async function notifyNewEntrants(beforeList, afterList, title, body) {
    const before = beforeList || [];
    const after  = afterList  || [];

    const newEntrants = after.filter(id => !before.includes(id));

    for (const deviceId of newEntrants) {
        await sendNotification(deviceId, title, body);
    }
}

/**
 * Looks up a user's FCM token from the "Users" Firestore collection and sends
 * them a push notification via Firebase Cloud Messaging.
 *
 * Silently returns if the user document does not exist or has no FCM token stored.
 *
 * @param {string} deviceId - The user's device ID, used as the Firestore document ID in "Users".
 * @param {string} title    - The notification title to display on the device.
 * @param {string} body     - The notification body text to display on the device.
 * @returns {Promise<void>}
 */
async function sendNotification(deviceId, title, body) {
    console.log("sendNotification called for deviceId:", deviceId);

    const userDoc = await db.collection("Users").doc(deviceId).get();
    if (!userDoc.exists) {
        console.log("No user document found for deviceId:", deviceId);
        return;
    }

    const token = userDoc.data().fcmToken;
    if (!token) {
        console.log("No FCM token found for deviceId:", deviceId);
        return;
    }

    console.log("Sending notification to token:", token);
    try {
        const result = await admin.messaging().send({
            token: token,
            notification: { title, body }
        });
        console.log("Notification sent successfully:", result);
    } catch (error) {
        console.error("Failed to send notification:", error);
    }
}