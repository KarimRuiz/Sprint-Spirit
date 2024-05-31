import GCMManager as fcm

tokens = ["d2AKl0_7SLuCylZbd2Z6_k:APA91bFqBnHOKblIeajF4WXQM2bG3m5duF7dx9vQH0iihKm_qMDUU9Z3JaP-NkrZVIOPyStWZyf3vBl2HkOrft3mPZwj9mTjfzaWxPRRUE71tjltcLCGTvCT5E6I-VETX35LcL01RzDG"]
# fcm.sendPush("Hi", "This is my next msg", tokens)
fcm.sendPushToTopic("Hi", "This is my next msg", "RiQ0KGUYkYHsySyip0z8")