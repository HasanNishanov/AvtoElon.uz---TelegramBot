package com.company.util;

import com.vdurmont.emoji.EmojiParser;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class InlineButtonUtil {

    public static InlineKeyboardButton button(String text, String callbackdata) {

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackdata);


        return button;
    }

    public static InlineKeyboardButton button(String text, String callbackdata , String emoji) {
        InlineKeyboardButton button = new InlineKeyboardButton();
      String  emojiText = EmojiParser.parseToUnicode(emoji + " " + text);
        button.setText(emojiText);
        button.setCallbackData(callbackdata);
return  button;
    }
    public  static List<InlineKeyboardButton> row(InlineKeyboardButton...InlineKeyboardButtons){
        List<InlineKeyboardButton> row = new LinkedList<>();
        row.addAll(Arrays.asList(InlineKeyboardButtons));
        return  row;
    }
    public static List<List<InlineKeyboardButton>> collection(List<InlineKeyboardButton>...rows) {
        List<List<InlineKeyboardButton>> collection = new LinkedList<>();
        collection.addAll(Arrays.asList(rows));

return collection;
    }
    public  static InlineKeyboardMarkup keyboard(List<List<InlineKeyboardButton>> collection ){
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(collection);

        return keyboardMarkup;
    }
}

