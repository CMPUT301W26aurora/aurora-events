const { onDocumentUpdated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");
admin.initializeApp();

const db = admin.firestore();

/**
 * Firestore-triggered Cloud Function that sends push notifications to entrants
 * when they are moved between registration lists on an Event document.
 */
exports.onEventListChange = onDocumentUpdated("Events/{eventId}", async (event) => {
    const before     = event.data.before.data().registrationList || {};
    const after      = event.data.after.data().registrationList || {};
    const eventName  = event.data.after.data().name || "an event";
    const eventId    = event.params.eventId;

    console.log("Function triggered for event:", eventId);
    console.log("Before selectedList:", JSON.stringify(before.selectedList));
    console.log("After selectedList:", JSON.stringify(after.selectedList));

    await notifyNewEntrants(before.selectedList,  after.selectedList,  eventId, eventName, "You've been selected!",     `You've been selected for ${eventName}!`);
    await notifyNewEntrants(before.attendingList, after.attendingList, eventId, eventName, "You're confirmed!",          `You're confirmed for ${eventName}.`);
    await notifyNewEntrants(before.declinedList,  after.declinedList,  eventId, eventName, "Invitation declined",        `Your invitation to ${eventName} has been declined.`);
    await notifyNewEntrants(before.cancelledList, after.cancelledList, eventId, eventName, "Registration cancelled",     `Your registration for ${eventName} has been cancelled.`);
    await notifyNewEntrants(before.removedList,   after.removedList,   eventId, eventName, "Removed from event",         `You have been removed from ${eventName}.`);
});

/**
 * Finds entrants newly added to a list and sends each a push notification.
 */
async function notifyNewEntrants(beforeList, afterList, eventId, eventName, title, body) {
    const before = beforeList || [];
    const after  = afterList  || [];

    const newEntrants = after.filter(id => !before.includes(id));

    for (const deviceId of newEntrants) {
        await sendNotification(deviceId, eventId, title, body);
    }
}

/**
 * Looks up a user's FCM token and sends them a push notification via FCM.
 */
async function sendNotification(deviceId, eventId, title, body) {
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
            data: { eventId, title, body }
        });
        console.log("Notification sent successfully:", result);
    } catch (error) {
        console.error("Failed to send notification:", error);
    }
}