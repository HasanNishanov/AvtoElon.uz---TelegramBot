package com.company.controller;

import com.company.container.ComponentContainer;
import com.company.database.Database;
import com.company.enums.AdminStatus;
import com.company.model.Product;
import com.company.service.CategoryService;
import com.company.service.ProductService;
import com.company.util.InlineButtonUtil;
import com.company.util.InlineKeyboardUtil;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

import static com.company.container.ComponentContainer.productMap;

public class AdminController {

    public void handleMessage(User user, Message message) {
        if (message.hasText()) {
            handleText(user, message);
        } else if (message.hasContact()) {
            handleContact(user, message);
        } else if (message.hasPhoto()) {
            handlePhoto(user, message);
        }
    }

    private void handlePhoto(User user, Message message) {
        List<PhotoSize> photoSizeList = message.getPhoto();

        String chatId = String.valueOf(message.getChatId());

        if (ComponentContainer.productStepMap.containsKey(chatId)) {
            Product product = productMap.get(chatId);

            if (ComponentContainer.productStepMap.get(chatId).equals(AdminStatus.ENTERED_PRODUCT_PRICE)) {
                product.setImage(photoSizeList.get(photoSizeList.size() - 1).getFileId());

                SendPhoto sendPhoto = new SendPhoto(chatId, new InputFile(product.getImage()));
                sendPhoto.setCaption(String.format("Kategoriya: %s\n" +
                                "Mahsulot: %s \n Narxi: %s\n\n Quyidagi mahsulot bazaga qo'shilsinmi?",
                        CategoryService.getCategoryById(product.getCategoryId()).getName(),
                        product.getName(), product.getPrice()));
                sendPhoto.setReplyMarkup(InlineKeyboardUtil.confirmAddProductMarkup());

                ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendPhoto);
            }
        }

    }

    private void handleContact(User user, Message message) {

    }

    private void handleText(User user, Message message) {
        String text = message.getText();
        String chatId = String.valueOf(message.getChatId());
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));

        if (text.equals("/start")) {
            sendMessage.setText("*\uD83D\uDC4BAssalomu aleykum , Amalni tanlang*");
            sendMessage.setParseMode(ParseMode.MARKDOWN);
            sendMessage.setReplyMarkup(InlineKeyboardUtil.productMenu());
        } else if (ComponentContainer.productStepMap.containsKey(chatId)) {

            Product product = productMap.get(chatId);

            if (ComponentContainer.productStepMap.get(chatId).equals(AdminStatus.SELECT_CATEGORY_FOR_ADD_PRODUCT)) {
                product.setName(text);
                ComponentContainer.productStepMap.put(chatId, AdminStatus.ENTERED_PRODUCT_NAME);

                sendMessage.setText("Mahsulot narxini kiriting(haqiqiy musbat son): ");

            } else if (ComponentContainer.productStepMap.get(chatId).equals(AdminStatus.ENTERED_PRODUCT_NAME)) {
                double price = 0;
                try {
                    price = Double.parseDouble(text.trim());
                } catch (NumberFormatException e) {
                }

                if (price <= 0) {
                    sendMessage.setText("Narx noto'g'ri kiritildi, Qaytadan narxni kiriting: ");
                } else {
                    product.setPrice(price);
                    ComponentContainer.productStepMap.put(chatId, AdminStatus.ENTERED_PRODUCT_PRICE);

                    sendMessage.setText("Mahsulotning rasmini jo'nating: ");
                }
            } else if (ComponentContainer.productStepMap.get(chatId).equals(AdminStatus.DELETE_PRODUCT)) {
                ProductService.deleteProduct(Integer.valueOf(text));
                sendMessage.setText("Mahsulot o'chirildi ! \n\n Amalni Tanlang ");

                productMap.remove(chatId);
                ComponentContainer.productStepMap.remove(chatId);


                sendMessage.setReplyMarkup(InlineKeyboardUtil.productMenu());

            }

        }
        ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendMessage);
    }

    public void handleCallBack(User user, Message message, String data) {
        String chatId = String.valueOf(message.getChatId());

        if (data.equals("add_product")) {
            DeleteMessage deleteMessage = new DeleteMessage(
                    chatId, message.getMessageId()
            );
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(deleteMessage);

            SendMessage sendMessage = new SendMessage(
                    chatId, "Kategoriyalardan birini tanlang:"
            );
            sendMessage.setReplyMarkup(InlineKeyboardUtil.categoryInlineMarkup());
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendMessage);

            productMap.remove(chatId);
            ComponentContainer.productStepMap.remove(chatId);

            ComponentContainer.productStepMap.put(chatId, AdminStatus.CLICKED_ADD_PRODUCT);
            productMap.put(chatId,
                    new Product(null, null, null, null));

        } else if (data.startsWith("add_product_category_id")) {
            DeleteMessage deleteMessage = new DeleteMessage(
                    chatId, message.getMessageId()
            );
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(deleteMessage);

            int categoryId = Integer.parseInt(data.split("/")[1]);

            SendMessage sendMessage = new SendMessage(
                    chatId, "*❗️E'lon berish uchun , e'lonni tog'ri toldiring *"
            );
            sendMessage.setParseMode("Markdown");

            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendMessage);

            ComponentContainer.productStepMap.put(chatId, AdminStatus.SELECT_CATEGORY_FOR_ADD_PRODUCT);
            Product product = productMap.get(chatId);
            product.setCategoryId(categoryId);
        } else if (data.equals("add_product_commit")) {
            DeleteMessage deleteMessage = new DeleteMessage(
                    chatId, message.getMessageId()
            );
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(deleteMessage);

            Product product = productMap.get(chatId);
            System.out.println("product.getName() = " + product.getName());

            ProductService.addProduct(product);

            productMap.remove(chatId);
            ComponentContainer.productStepMap.remove(chatId);

            SendMessage sendMessage = new SendMessage(
                    chatId, "\t \n ✅ Saqlandi.\n\n" + "Amalni tanlang:"
            );
            sendMessage.setReplyMarkup(InlineKeyboardUtil.productMenu());
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendMessage);

        } else if (data.equals("add_product_cancel")) {
            DeleteMessage deleteMessage = new DeleteMessage(
                    chatId, message.getMessageId()
            );
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(deleteMessage);

            productMap.remove(chatId);
            ComponentContainer.productStepMap.remove(chatId);

            SendMessage sendMessage = new SendMessage(
                    chatId, "Amalni tanlang:"
            );
            sendMessage.setReplyMarkup(InlineKeyboardUtil.productMenu());
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendMessage);
        }
        else if (data.equals("show_product_list")) {
            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setText("*\uD83D\uDCCA E'lon kategorisayini belgilang*");
            editMessageText.setParseMode(ParseMode.MARKDOWN);
            InlineKeyboardButton car = InlineButtonUtil.button("Avtomobil", "avt", "\uD83D\uDE98");
            InlineKeyboardButton ehq = InlineButtonUtil.button("Ehtiyot qismlar", "ehq", "⚙️");
            InlineKeyboardButton tjr = InlineButtonUtil.button("Tijorat transport", "tjr", "\uD83D\uDE9C️");
            List<InlineKeyboardButton> row1 = InlineButtonUtil.row(car);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(ehq);
            List<InlineKeyboardButton> row3 = InlineButtonUtil.row(tjr);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row1, row2, row3);
            editMessageText.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(message.getMessageId());
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(editMessageText);

        } else if (data.equals("avt")) {
            DeleteMessage deleteMessage = new DeleteMessage(
                    chatId, message.getMessageId()
            );
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(deleteMessage);

            ProductService.loadProductList();

            for (Product product : Database.productList) {
                SendPhoto sendPhoto = new SendPhoto(chatId, new InputFile(product.getImage()));
                sendPhoto.setCaption(String.format("*Kategoriya: %s\n" +
                                "Mahsulot:\n %s \n Narxi: %s\n*",
                        CategoryService.getCategoryById(product.getCategoryId()).getName(),
                        product.getName(), product.getPrice()));
                sendPhoto.setParseMode(ParseMode.MARKDOWN);
                ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendPhoto);
            }

        } else if (data.equals("ehq")) {
            DeleteMessage deleteMessage = new DeleteMessage(
                    chatId, message.getMessageId()
            );
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(deleteMessage);

            ProductService.loadProductList2();

            for (Product product : Database.productList) {
                SendPhoto sendPhoto = new SendPhoto(chatId, new InputFile(product.getImage()));
                sendPhoto.setCaption(String.format("*Kategoriya: %s\n" +
                                "Mahsulot:\n %s \n Narxi: %s\n*",
                        CategoryService.getCategoryById(product.getCategoryId()).getName(),
                        product.getName(), product.getPrice()));
                sendPhoto.setParseMode(ParseMode.MARKDOWN);
                ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendPhoto);
            }
        } else if (data.equals("tjr")) {
            DeleteMessage deleteMessage = new DeleteMessage(
                    chatId, message.getMessageId()
            );
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(deleteMessage);

            ProductService.loadProductList3();

            for (Product product : Database.productList) {
                SendPhoto sendPhoto = new SendPhoto(chatId, new InputFile(product.getImage()));
                sendPhoto.setCaption(String.format("*Kategoriya: %s\n" +
                                "Mahsulot:\n %s \n Narxi: %s\n*",
                        CategoryService.getCategoryById(product.getCategoryId()).getName(),
                        product.getName(), product.getPrice()));
                sendPhoto.setParseMode(ParseMode.MARKDOWN);
                ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendPhoto);
            }

        } else if (data.equals("delete_product")) {

            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setText("*\uD83D\uDCCA E'lon kategorisayini belgilang*");
            editMessageText.setParseMode(ParseMode.MARKDOWN);
            InlineKeyboardButton car = InlineButtonUtil.button("Avtomobil", "avt1", "\uD83D\uDE98");
            InlineKeyboardButton ehq = InlineButtonUtil.button("Ehtiyot qismlar", "ehq1", "⚙️");
            InlineKeyboardButton tjr = InlineButtonUtil.button("Tijorat transport", "tjr1", "\uD83D\uDE9C️");
            List<InlineKeyboardButton> row1 = InlineButtonUtil.row(car);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(ehq);
            List<InlineKeyboardButton> row3 = InlineButtonUtil.row(tjr);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row1, row2, row3);
            editMessageText.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(message.getMessageId());
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(editMessageText);

        }
        else if (data.equals("avt1")) {
            DeleteMessage deleteMessage = new DeleteMessage(
                    chatId, message.getMessageId()
            );
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(deleteMessage);

            ProductService.loadProductList();

            for (Product product : Database.productList) {
                SendPhoto sendPhoto = new SendPhoto(chatId, new InputFile(product.getImage()));
                sendPhoto.setCaption(String.format("*Kategoriya: %s\n" +
                                "Mahsulot:\n %s \n Narxi: %s\n IDsi: %s*",
                        CategoryService.getCategoryById(product.getCategoryId()).getName(),
                        product.getName(), product.getPrice(), product.getId()));
                sendPhoto.setParseMode(ParseMode.MARKDOWN);
                ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendPhoto);
            }
            SendMessage sendMessage = new SendMessage(chatId, "O'chirmoqchi bo'lgan mahsulotning ID sini kiriting: ");
            ComponentContainer.productStepMap.put(chatId, AdminStatus.DELETE_PRODUCT);

            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendMessage);

        } else if (data.equals("ehq1")) {
            DeleteMessage deleteMessage = new DeleteMessage(
                    chatId, message.getMessageId()
            );
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(deleteMessage);

            ProductService.loadProductList2();

            for (Product product : Database.productList) {
                SendPhoto sendPhoto = new SendPhoto(chatId, new InputFile(product.getImage()));
                sendPhoto.setCaption(String.format("*Kategoriya: %s\n" +
                                "Mahsulot:\n %s \n Narxi: %s\n IDsi: %s*",
                        CategoryService.getCategoryById(product.getCategoryId()).getName(),
                        product.getName(), product.getPrice(),product.getId()));
                sendPhoto.setParseMode(ParseMode.MARKDOWN);
                ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendPhoto);
            }
            SendMessage sendMessage = new SendMessage(chatId, "O'chirmoqchi bo'lgan mahsulotning ID sini kiriting: ");
            ComponentContainer.productStepMap.put(chatId, AdminStatus.DELETE_PRODUCT);

            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendMessage);
        } else if (data.equals("tjr1")) {
            DeleteMessage deleteMessage = new DeleteMessage(
                    chatId, message.getMessageId()
            );
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(deleteMessage);

            ProductService.loadProductList3();

            for (Product product : Database.productList) {
                SendPhoto sendPhoto = new SendPhoto(chatId, new InputFile(product.getImage()));
                sendPhoto.setCaption(String.format("*Kategoriya: %s\n" +
                                "Mahsulot:\n %s \n Narxi: %s\n IDsi: %s*",
                        CategoryService.getCategoryById(product.getCategoryId()).getName(),
                        product.getName(), product.getPrice(),product.getId()));
                sendPhoto.setParseMode(ParseMode.MARKDOWN);
                ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendPhoto);
            }
            SendMessage sendMessage = new SendMessage(chatId, "O'chirmoqchi bo'lgan mahsulotning ID sini kiriting: ");
            ComponentContainer.productStepMap.put(chatId, AdminStatus.DELETE_PRODUCT);

            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendMessage);

        }


    }

}
