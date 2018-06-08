'use strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendNotification = functions.database.ref('/Notifications/{user_id}/{notification_id}').onWrite((event,context) => {
	const userId = context.params.user_id;
	const notificationId = context.params.notification_id;

	const afterData = event.after.val(); // data after write

	//console.log('We have a notification to send to: ', userId);
	//console.log('AfterData: ', afterData);

	if (afterData !== null){

		const fromUser = admin.database().ref(`/Notifications/${userId}/${notificationId}`).once('value');
		return fromUser.then(fromUserResult =>{
			const from_user_id = fromUserResult.val().from;
			const notify_type = fromUserResult.val().type;
			const seen = fromUserResult.val().seen;
			var message = fromUserResult.val().text;
			if (message === undefined){
				message = "";
			}
			//console.log('message: ', message);
			//console.log('From User: ', from_user_id);

			//console.log('a seen erteke: ', seen);
			if (!seen){


				const userQuery = admin.database().ref(`/Users/${from_user_id}`).once('value');
				const deviceToken = admin.database().ref(`/Users/${userId}/device_token`).once('value');



				return Promise.all([userQuery, deviceToken]).then(result =>{

					const from_user_name = result[0].val().name;
					const from_user_img = result[0].val().image_thumb;
					//console.log('from_user_name',from_user_name);
					const token_id = result[1].val();

					var type = "";
					var action="";
					var extraText="";
					var megjelenitettUzenet="";
					var color = '#2062af';

					if ( notify_type === 'new_message'){
						type = "Új üzenet";
						action = "lapitchat_TARGET_CHAT";
						extraText = `${from_user_name} küldött neked egy új üzenetet.`;
						megjelenitettUzenet = `${message}`;
						//console.log('new message ag lefutott',type);

					}else if ( notify_type === 'new_image'){
						type = "Új üzenet";
						action = "lapitchat_TARGET_CHAT";
						extraText = `${from_user_name} küldött neked egy új üzenetet.`;
						megjelenitettUzenet = `képet küldött`;
						//console.log('new message ag lefutott',type);

					}else if (notify_type === 'friend_request') {
						type = "Barát jelölés";
						action ="lapitchat_TARGET_FRIENDS";
						extraText = `${from_user_name} bejelölt téged barátnak.`;
						color = '#007200';
						//console.log('new message ELSE ag lefutott');

					}else if (notify_type === 'update') {
						type = "Frissítés";
						action = "lapitchat_TARGET_UPDATE";
						extraText = `Frissítés vált elérhetővé`;
						color = '#f44d28';
					}

					const payload = {
							notification: {
								title: extraText,
								tag: `${from_user_id}`,
								body: megjelenitettUzenet,
								icon: "notify_icon",
								click_action: `${action}`,
								sound: "light",
								color: `${color}`,
								vibrate: "true"
							},
							data: {
								uid: from_user_id,
								type: notify_type,
								name: from_user_name,
								tag: `${from_user_id}`,
								img: from_user_img,
								ledColor: "[0, 0, 255, 0]",
								vibrationPattern: "[2000, 1000, 500, 500]"
							}
						};

					return admin.messaging().sendToDevice(token_id , payload).then(response => {
						//console.log('This was');
						return 0;
					});
				});
			}else{
				return 0;
			}
		});
	}else{
		return 0;
	}
});
