import firebase_admin
import InitFirebaseCreds
from firebase_admin import credentials, messaging

def sendPush(title, msg, registration_token, dataObject=None):
    message = messaging.MulticastMessage(
        notification=messaging.Notification(
            title=title,
            body=msg
        ),
        data=dataObject,
        tokens=registration_token,
    )

    response = messaging.send_multicast(message)
    print('Successfully sent message:', response)

def sendPushToTopic(topic, dataObject=None):
    message = messaging.Message(
        data=dataObject,
        topic=topic,
    )

    response = messaging.send(message)

    print('Successfully sent message:', response)