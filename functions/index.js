const {
    onDocumentUpdated,
    onDocumentDeleted
} = require("firebase-functions/v2/firestore");
const { onCall } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
admin.initializeApp();

const db = admin.firestore();

exports.onUserDeleted = onDocumentDeleted("Users/{userId}", async (event)=>{
    const deletedUserId = event.params.userId;
    const batch = db.batch();

    console.log("Triggered Cleanup for deleted user:", deletedUserId);
    try {
            // delete from all event lists
            const eventsSnapshot = await db.collection('Events').get();

            eventsSnapshot.forEach(doc => {
                const eventData = doc.data();
                const regList = data.registrationList || {};

                if (regList.selectedList) {
                      const updatedSelected = regList.selectedList.filter(u => u.userId !== deletedUserId);
                      batch.update(doc.ref, { "registrationList.selectedList": updatedSelected });
                }

                batch.update(doc.ref, {
                    "registrationList.waitingList": admin.firestore.FieldValue.arrayRemove(deletedUserId),
                    "registrationList.attendingList": admin.firestore.FieldValue.arrayRemove(deletedUserId),
                    "registrationList.cancelledList": admin.firestore.FieldValue.arrayRemove(deletedUserId),
                    "registrationList.declinedList": admin.firestore.FieldValue.arrayRemove(deletedUserId),
                    "registrationList.removedList": admin.firestore.FieldValue.arrayRemove(deletedUserId)
                });
            });

            // delete users parent comments
            const parentCommentSnapshot = await db.collection("Comments")
                .where("userId", "==", deletedUserId)
                .where("parentId", "==", null)
                .get();

            parentCommentSnapshot.forEach(doc => {
                batch.delete(doc.ref);
            });

            // delete users replies, cascade will destroy the replies to self, this handles replies to others
            const replyCommentSnapshot = await db.collection("Comments")
                .where("userId", "==", deletedUserId)
                .where("parentId", "!=", null)
                .get();

            replyCommentSnapshot.forEach(doc =>{
                batch.delete(doc.ref);
            })

            // delete users notifications
            const notificationsSnapshot = await db.collection("Notifications")
                .where("deviceId", "==", deletedUserId)
                .get();

            notificationsSnapshot.forEach(doc => {
                batch.delete(doc.ref);
            });
            await batch.commit();
            console.log("Cleanup complete for user:", deletedUserId);

        } catch (error) {
            console.error("Error during user cleanup:", error);
        }

});

exports.onCommentDeleted = onDocumentDeleted("Comments/{commentId}", async (event) => {
    const deletedCommentId = event.params.commentId;
    const batch = db.batch();

    console.log("Checking for replies to deleted comment:", deletedCommentId);

    try {
        // Find all comments that are replies, delete
        const repliesSnapshot = await db.collection("Comments")
            .where("parentId", "==", deletedCommentId)
            .get();

        //if none exit early
        if (repliesSnapshot.empty) {
            return null;
        }

        repliesSnapshot.forEach(doc => {
            batch.delete(doc.ref);
        });

        await batch.commit();
        console.log("Deleted replies for parent:",  deletedCommentId, repliesSnapshot.size);
    } catch (error) {
        console.error("Error cascading comment deletion:", error);
    }
});

exports.onEventDeleted= onDocumentDeleted("Events/{eventId}", async (event)=>{
    const deletedEventId = event.params.eventId;
    const batch = db.batch();

    try {
        // Find all parent comments, cascade will delete replies
        const parentSnapshot = await db.collection("Comments")
            .where("eventId", "==", deletedEventId)
            .where("parentId", "==", null)
            .get();

        parentSnapshot.forEach(doc=>{
            batch.delete(doc.ref);
        });

        await batch.commit();
        console.log("Deleted event and its comments", deletedEventId);
        }catch (error) {
            console.error("Error deleting events comments", error);
        }
});

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
 * Callable Cloud Function that lets the organizer send a custom push
 * notification to a specific device. Called from NotificationSender.java.
 */
exports.sendNotification = onCall(async (request) => {
    const { token, title, body, eventId } = request.data;
    if (!token || !title || !body) {
        throw new Error("Missing required fields: token, title, body");
    }
    try {
        const result = await admin.messaging().send({
            token: token,
            data: { eventId: eventId || "", title, body }
        });
        console.log("Custom notification sent:", result);
        return { success: true };
    } catch (error) {
        console.error("Failed to send custom notification:", error);
        throw new Error("Failed to send notification: " + error.message);
    }
});

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