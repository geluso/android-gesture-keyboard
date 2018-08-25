# Android Gesture Keyboard

## Acknowledgements
* [Create a Custom Keyboard on Android](https://code.tutsplus.com/tutorials/create-a-custom-keyboard-on-android--cms-22615)

Many thanks to "Create a Custom Keyboard on Android" by Ashraff Hathibelagal
for providing an excellent guide to get started building custom keyboards on
Android.

## Overview
This keyboard uses Ashraff's basic keyboard and adds some swipe functionality
on top of it. The keyboard discerns between short and long swipes and pays
attention to what direction the swipe was moving (left, right, up and down).
The app combines this info to move the cursor around letters or words and
delete text.

* small swipe left - move one letter left
* small swipe right - move one letter right
* big swipe left - move one word left
* big swipe right - move one word right
* any swipe + down - delete word/letter left/right
* swipe straight down - delete entire word containing cursor

This was created to deal with the frustration of using a finger to manually
manipulate the cursor inside text. It's absolutely inspired by vim text
motions.

