import firebase_admin
from firebase_admin import credentials

cred = credentials.Certificate("sprint-spirit-firebase-adminsdk-k04zl-661197e725.json")
firebase_admin.initialize_app(cred, {
    'databaseURL': 'https://sprint-spirit-default-rtdb.europe-west1.firebasedatabase.app/'
})
