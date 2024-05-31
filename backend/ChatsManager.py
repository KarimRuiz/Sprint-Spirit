import firebase_admin
from firebase_admin import credentials
from firebase_admin import db
import InitFirebaseCreds
import GCMManager as fcm

def listener(event):
    path = event.path

    data = event.data

    if path != '/':
        path_parts = path.strip('/').split('/')
        if len(path_parts) >= 2:
            chat_id = path_parts[0]
            message_id = path_parts[1]
            if isinstance(data, dict) and 'content' in data:
                message_content = data['content']
                message_sender = data['user']['username']
                print(f'New message in chat {chat_id} by {message_sender}: {message_content}')
                print('Sending message...')

                data = {"chatId": chat_id, "sender":message_sender, "content":message_content}

                fcm.sendPushToTopic(chat_id, data)



#add the listener
db_ref = db.reference('routeChats')
db_ref.listen(listener)

#keep it running
import time
try:
    while True:
        time.sleep(3)
except KeyboardInterrupt:
    print("Stopped by the user.")