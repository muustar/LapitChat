'use strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendNotification = functions.database.ref('/Notifications/{user_id}/{notification_id}').onWrite((event,context) => {
	const userId = context.params.user_id;
	const notificationId = context.params.notification_id;

	const afterData = event.after.val(); // data after write

	console.log('We have a notification to send to: ', userId);
	console.log('AfterData: ', afterData);


	//if (!event.after.val()){
	//	return console.log('A Notification has been deleted from the database: ', notification_id);
	//}
	
	const deviceToken = admin.database().ref(`/Users/${userId}/device_token`).once('value');
	return deviceToken.then(result => {
			const token_id = result.val();

			const payload = {
					notification: {
						title: "Friend Request",
						body: "You've receive a new Friend Request",
						icon: "default"
					}
				};

			return admin.messaging().sendToDevice(token_id , payload).then(response => {

				console.log('This was');
				return 0;

			});



	});

	
});
