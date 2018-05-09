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


	

	const fromUser = admin.database().ref(`/Notifications/${userId}/${notificationId}`).once('value');
	return fromUser.then(fromUserResult =>{
		const from_user_id = fromUserResult.val().from;
		console.log('From User: ', from_user_id);

		const userQuery = admin.database().ref(`/Users/${from_user_id}`).once('value');
		const deviceToken = admin.database().ref(`/Users/${userId}/device_token`).once('value');
		
		return Promise.all([userQuery, deviceToken]).then(result =>{

			const from_user_name = result[0].val().name;
			console.log('from_user_name',from_user_name);
			const token_id = result[1].val();

			const payload = {
					notification: {
						title: "Friend Request from ",
						body: `${from_user_name} has sent you a new Friend Request`,
						icon: "default",
						click_action: "lapitchat_TARGET_NOTIFICATION"
					},
					data: {
						uid: from_user_id
					}
				};

			return admin.messaging().sendToDevice(token_id , payload).then(response => {

				//console.log('This was');
				return 0;

			});


		});

	});

	
});
