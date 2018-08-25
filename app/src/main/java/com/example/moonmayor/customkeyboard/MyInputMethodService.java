package com.example.moonmayor.customkeyboard;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

public class MyInputMethodService extends InputMethodService
    implements KeyboardView.OnKeyboardActionListener, GestureDetector.OnGestureListener,
        View.OnTouchListener {
    private KeyboardView keyboardView;
    private Keyboard keyboard;

    private AudioManager mAudioManager;
    private Vibrator mVibrator;

    private boolean caps = false;

    @Override
    public View onCreateInputView() {
        keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard_view, null);
        keyboard = new Keyboard(this, R.xml.keys_layout);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setOnKeyboardActionListener(this);

        keyboardView.setOnTouchListener(this);

        return keyboardView;
    }

    @Override
    public void onPress(int i) {

    }

    @Override
    public void onRelease(int i) {

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection != null) {
            switch(primaryCode) {
                case Keyboard.KEYCODE_DELETE :
                    CharSequence selectedText = inputConnection.getSelectedText(0);

                    if (TextUtils.isEmpty(selectedText)) {
                        inputConnection.deleteSurroundingText(1, 0);
                    } else {
                        inputConnection.commitText("", 1);
                    }
                    break;
                case Keyboard.KEYCODE_SHIFT:
                    caps = !caps;
                    keyboard.setShifted(caps);
                    keyboardView.invalidateAllKeys();
                    break;
                case Keyboard.KEYCODE_DONE:
                    inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));

                    break;
                default :
                    char code = (char) primaryCode;
                    if(Character.isLetter(code) && caps){
                        code = Character.toUpperCase(code);
                    }
                    inputConnection.commitText(String.valueOf(code), 1);

            }
        }
    }

    @Override
    public void onText(CharSequence charSequence) {

    }

    @Override
    public void swipeLeft() {
        Log.d("SWIPE", "left");
    }

    @Override
    public void swipeRight() {
        Log.d("SWIPE", "right");
    }

    @Override
    public void swipeDown() {
        Log.d("SWIPE", "down");
    }

    @Override
    public void swipeUp() {
        Log.d("SWIPE", "up");
    }

    private void playSound(int keyCode){
        mVibrator.vibrate(20);
        mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        switch(keyCode){
            case 32:
                mAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                mAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                mAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default: mAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    float xDown;
    float yDown;
    float xUp;
    float yUp;

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            xDown = motionEvent.getX();
            yDown = motionEvent.getY();
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            xUp = motionEvent.getX();
            yUp = motionEvent.getY();
            return reportTouch();
        }
        return false;
    }

    public boolean reportTouch() {
        float dx = xUp - xDown;
        float dy = yUp - yDown;

        int width = keyboardView.getWidth();
        int height = keyboardView.getHeight();

        float percentX = Math.abs(dx) / width * 100;
        float percentY = Math.abs(dy) / height * 100;

        boolean isMovingLeft = dx < 0;
        boolean isMovingRight = dx > 0;
        boolean isMovingUp = dy < 0;
        boolean isMovingDown = dy > 0;

        Log.d("TOUCH", "stats");
        Log.d("TOUCH", "dx: " + dx + " dy: " + dy);
        Log.d("TOUCH", "x%: " + percentX + " y%: " + percentY);
        Log.d("TOUCH", isMovingUp + " " + isMovingRight + " " + isMovingDown + " " + isMovingDown);

        int minLittleSwipe = 5;
        int minBigSwipe = 40;

        if (isMovingDown && percentY > 10) {
            if (minLittleSwipe < percentX && percentX < minBigSwipe) {
                if (isMovingLeft) {
                    deleteOneCharBackward();
                    return true;
                } else if (isMovingRight) {
                    deleteOneCharForward();
                    return true;
                }
            } else if (percentX > minBigSwipe) {
                if (isMovingLeft) {
                    deleteOneWordBackward();
                    return true;
                } else if (isMovingRight) {
                    deleteOneWordForward();
                    return true;
                }
            }
        } else {
            if (minLittleSwipe < percentX && percentX < minBigSwipe) {
                if (isMovingLeft) {
                    moveOneCharBackward();
                    return true;
                } else if (isMovingRight) {
                    moveOneCharForward();
                    return true;
                }
            } else if (percentX > minBigSwipe) {
                if (isMovingLeft) {
                    moveOneWordBackward();
                    return true;
                } else if (isMovingRight) {
                    moveOneWordForward();
                    return true;
                }
            }
        }

        return false;
    }

    private void moveOneWordForward() {
        Log.d("MOVE", "one word forward");
    }

    private void moveOneWordBackward() {
        Log.d("MOVE", "one word backward");

    }

    private void moveOneCharForward() {
        Log.d("MOVE", "one char forward");
    }

    private void moveOneCharBackward() {
        Log.d("MOVE", "one char backward");
    }

    private void deleteOneWordForward() {
        Log.d("DELETE", "one word forward");
        deleteWord(false);
    }

    private void deleteOneWordBackward() {
        Log.d("DELETE", "one word backward");
        deleteWord(true);
    }

    private void deleteWord(boolean isDeletingBackward) {
        InputConnection inputConnection = getCurrentInputConnection();
        CharSequence selectedText = inputConnection.getSelectedText(0);

        ExtractedText et = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        String text = et.text.toString();
        int selectionStart = et.selectionStart;
        int selectionEnd = et.selectionEnd;

        Log.d("CURSOR", "start: " + selectionStart + " end: " + selectionEnd);

        int increment = -1;
        if (!isDeletingBackward) {
            increment = 1;
        }

        int deleteTo = selectionStart;
        for (int i = selectionStart - 1; i >= 0 && i < text.length(); i += increment) {
            if (text.charAt(i) == ' ') {
               deleteTo = i;
               break;
            }
        }

        if (deleteTo == selectionStart) {
            if (isDeletingBackward) {
                deleteTo = 0;
            } else {
                deleteTo = text.length();
            }
        }

        Log.d("DELETE", "to: " + (selectionStart - deleteTo));
        if (isDeletingBackward) {
            delete(selectionStart - deleteTo, 0);
        } else {
            delete(0, Math.abs(selectionStart - deleteTo) + 1);
        }
    }

    private void deleteOneCharForward() {
        Log.d("DELETE", "one char forward");
        delete(0, 1);
    }

    private void deleteOneCharBackward() {
        Log.d("DELETE", "one char backward");
        delete(1, 0);
    }

    private void delete(int backward, int forward) {
        InputConnection inputConnection = getCurrentInputConnection();
        CharSequence selectedText = inputConnection.getSelectedText(0);
        if (TextUtils.isEmpty(selectedText)) {
            inputConnection.deleteSurroundingText(backward, forward);
        } else {
            inputConnection.commitText("", 1);
        }
    }
}
