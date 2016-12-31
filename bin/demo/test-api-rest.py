
# coding: utf-8

# In[36]:

#!/usr/bin/python

import requests

URL = "http://localhost:9997"

# [POST] Login test
def login(username, password):
    address = URL + "/login"
    return requests.post(address, data= {'uname': username, 'psw': password})

# [POST] Delete channel
def deleteChannel(channelTitle):
    address = URL + "/deleteCh"
    return requests.post(address, data= {'title': channelTitle})

# [GET] Get all channels
def listChannels():
    address = URL + "/channels"
    return requests.get(address)

# [GET] Get all channel messages
def listChannelMessages(channelTitle):
    address = URL + "/messages/"+channelTitle
    return requests.get(address)

# [POST] Adding a new channel
def addChannel(channelTitle):
    address = URL + "/addCh/"
    return requests.post(address, {'ch-title': channelTitle})

# [GET] Logout : redirect to login page
def logout():
    address = URL + "/logout"
    return requests.get(address)

# [GET] Get current user
def getUser():
    address = URL + "/getUser"
    return requests.get(address)

print("########[POST] Login ########\n")
print(login("amine", "amine").content)

print("########[GET] Get current user ########\n")
print(getUser().content)

print("########[POST] Adding a new channel ########\n")
print(addChannel("New").content)

print("########[GET] List all channel messages ########\n")
print(listChannelMessages("General").content)

print("########[POST] Delete a channel ########\n")
print(deleteChannel("qsd").content)

print("########[GET] List all channels ########\n")
print(listChannels().content)

print("########[GET] Logout ########\n")
print(logout().content)


# In[ ]:




# In[ ]:



