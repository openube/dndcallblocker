# DND Call Blocker #
DND Call Blocker is a simple Android application that automatically block unwanted incoming calls. User can define call filtering rules and exceptions.

## Gingerbread ##
On Gingerbread, user applications can not end a call programmatically anymore. See issue: http://code.google.com/p/android/issues/detail?id=15031

## Android permissions ##
This program requests several permissions from user:
  * To check incoming calls:
> android.permission.READ\_PHONE\_STATE
  * To react incoming calls (mute/block):
> android.permission.MODIFY\_AUDIO\_SETTINGS, android.permission.MODIFY\_PHONE\_STATE, android.permission.CALL\_PHONE
  * To handle contact list (block list):
> android.permission.READ\_CONTACTS
  * To clear filtered call from call history:
> android.permission.WRITE\_CONTACTS
  * To display ads:
> android.permission.INTERNET, android.permission.ACCESS\_COARSE\_LOCATION

## Thanks ##
For call handling techniques thanks to: [Android AutoAnswer](http://code.google.com/p/auto-answer/) and [Tedd's Droid Tools](http://code.google.com/p/teddsdroidtools/)

## Donate ##
If you find this application useful and if you like to support the development you may make a little donation by clicking the `PayPal` donation button below.

[![](https://www.paypal.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=NF4VH9Y7F4PQS)
