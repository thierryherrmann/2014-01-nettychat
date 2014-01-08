Netty Chat
==========

This is a simple asynchronous and non blocking chat server and client to play a bit with **Netty 4** and Java.

Features:

- account creation
- login / logout
- contact lists with offline invitations support
- instant messages with offline messaging support
- text UI or recordable commands

A permanent TCP connection is used similar to XMPP for all messages between clients and server. A fast custom encoder/decoder is used just as a demonstration of Netty capabilities.
* * *
Compile / run: 
mvn install


create DB tables:
com.thn.netty.chat.server.store.JdbcUserStore

start the server: 
com.thn.netty.chat.server.Server

start a client:
com.thn.netty.chat.client.Client
* * *
When starting the client:

    0) Exit 
    1) Create Account <username> <password>
    2) Login <username> <password>
    3) Logout 
    4) Add Contact Invite <contactName>
    5) Add Contact Response <userName> <accepted(true/false)>
    6) Remove Contact <contactName>
    7) Get Pending Contact Invitations 
    8) Instant Message <recipient> <message>
    9) Get Pending Messages 
    10) Shutdown Server 

#### Alice logs in
`2 Alice mypass`

    2014-01-08 12:34:08 INFO  MessageHandler:58 - cmd sent: LoginRequest[mType=LOGIN(2),mId=1,mUserName=UserName[mName=Alice],mPassword=mypass]
    2014-01-08 12:34:08 INFO  MessageHandler:73 - msg received: OkResponse[mType=OK(15),mId=1]

####  Bob logs in
`2 Bob mypass`

    2014-01-08 12:35:14 INFO  MessageHandler:58 - cmd sent: LoginRequest[mType=LOGIN(2),mId=1,mUserName=UserName[mName=Bob],mPassword=mypass]
    2014-01-08 12:35:14 INFO  MessageHandler:73 - msg received: OkResponse[mType=OK(15),mId=1]

(log4j logs omitted below)

#### Bob sends add contact invitation to Alice
`4 Alice`

###### Alice who's online receives the invitation
    Received contact request from UserName[mName=Bob]

#### Alice accepts the invitation: both users are contact of each other
`5 Bob true`

###### Bob receives Alice's response
    Received contact response from UserName[mName=Alice]. Invitation accepted: true

#### Bob sends a message to Alice
`8 Alice hello`

###### Alice who's online receives the message
    Received message from Bob: hello

#### Alice logs out and Bob sends a message to Alice
`8 Alice Hi`

#### Charlie logs in and sends a contact request to Alice who's offline
`2 Charlie mypass`

`4 Alice`

#### Alice logs in and fetches offline messages
`2 Alice mypass`

`9`

    New messages: [MessageInfo[mSender=UserName[mName=Bob],mRecipient=<null>,mMessage=Hi]]


#### Alice fetches pending contact requests
`7`

    These users ask permission to add you in their contact list: [UserName[mName=Charlie]]
