package com.company.util;




import com.company.database.Database;
import com.company.model.Category;
import com.company.service.CategoryService;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InlineKeyboardUtil {
    public static InlineKeyboardMarkup productMenu() {
        InlineKeyboardButton addButton = getButton("➕E'lon qoshish➕", "add_product");
        InlineKeyboardButton deleteButton = getButton("➖E'lonni ochirish➖", "delete_product");
        InlineKeyboardButton listButton = getButton("\uD83D\uDC41E'lonlarni korish\uD83D\uDC41", "show_product_list");

        List<InlineKeyboardButton> row1 = getRow(addButton);
        List<InlineKeyboardButton> row3 = getRow(deleteButton);
        List<InlineKeyboardButton> row4 = getRow(listButton);

        List<List<InlineKeyboardButton>> rowList = getRowList(row1, row3, row4);
        return new InlineKeyboardMarkup(rowList);
    }
    public static InlineKeyboardMarkup productMenuForUser() {
        InlineKeyboardButton addButton = getButton("✅ Chunarli ✅", "add_product_foruser");
        InlineKeyboardButton addButton1 = getButton("❎ Menu-ga qaytish ❎", "backmenu");
        List<InlineKeyboardButton> row1 = getRow(addButton);
        List<InlineKeyboardButton> row2 = getRow(addButton1);
        List<List<InlineKeyboardButton>> rowList = getRowList(row1,row2);
        return new InlineKeyboardMarkup(rowList);
    }

    private static InlineKeyboardButton getButton(String demo, String data) {
        InlineKeyboardButton button = new InlineKeyboardButton(demo);
        button.setCallbackData(data);
        return button;
    }

    private static List<InlineKeyboardButton> getRow(InlineKeyboardButton... buttons) {
        return Arrays.asList(buttons);
    }

    private static List<List<InlineKeyboardButton>> getRowList(List<InlineKeyboardButton>... rows) {
        return Arrays.asList(rows);
    }

    public static InlineKeyboardMarkup categoryInlineMarkup() {

        CategoryService.loadCategoryList();

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        for (Category category : Database.categoryList) {
            List<InlineKeyboardButton> buttonList = new ArrayList<>();

            InlineKeyboardButton button = new InlineKeyboardButton(category.getName());
            button.setCallbackData("add_product_category_id/" + category.getId());
            buttonList.add(button);
            rowList.add(buttonList);
        }
        return new InlineKeyboardMarkup(rowList);
    }

    public static InlineKeyboardMarkup confirmAddProductMarkup() {

        InlineKeyboardButton commit = getButton("Ha", "add_product_commit");
        InlineKeyboardButton cancel = getButton("Yo'q", "add_product_cancel");

        return new InlineKeyboardMarkup(getRowList(getRow(commit, cancel)));
    }
}
