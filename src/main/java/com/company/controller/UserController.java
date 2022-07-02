package com.company.controller;

import com.company.container.ComponentContainer;
import com.company.database.Database;
import com.company.enums.AdminStatus;
import com.company.enums.CustomerStatus;
import com.company.model.Customer;
import com.company.model.Product;
import com.company.service.CategoryService;
import com.company.service.CustomerService;
import com.company.service.ProductService;
import com.company.util.InlineButtonUtil;
import com.company.util.InlineKeyboardUtil;
import com.company.util.KeyboardUtil;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

import static com.company.container.ComponentContainer.productMap;

public class UserController {

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

        if (ComponentContainer.productStepMap1.containsKey(chatId)) {
            Product product = productMap.get(chatId);
            if (ComponentContainer.productStepMap1.get(chatId).equals(CustomerStatus.ENTERED_PRODUCT_PRICE)) {
                product.setImage(photoSizeList.get(photoSizeList.size() - 1).getFileId());
                // USERGA YUBORILADIGAN SOROV
                SendPhoto sendPhoto = new SendPhoto(chatId, new InputFile(product.getImage()));
                sendPhoto.setCaption(String.format("*Kategoriya: %s\n" +
                                "Mahsulot: %s \n Narxi: %s $\n\n ✅ Adminga yuborildi , yaqin orada e'lon joylashtiriladi ✅*",
                        CategoryService.getCategoryById(product.getCategoryId()).getName(),
                        product.getName(), product.getPrice()));
                sendPhoto.setParseMode(ParseMode.MARKDOWN);
                sendPhoto.setChatId(String.valueOf(message.getChatId()));
                ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendPhoto);
                // ADMINGA YUBORILADIGAN SOROV
                SendPhoto sendPhoto1 = new SendPhoto(chatId, new InputFile(product.getImage()));
                sendPhoto1.setCaption(String.format("*Kategoriya: %s\n" +
                                "Mahsulot: %s \n Narxi: %s $\n\n E'lon database-ga qoshilsinmi?*",
                        CategoryService.getCategoryById(product.getCategoryId()).getName(),
                        product.getName(), product.getPrice()));
                productMap.put(chatId,new Product(product.getCategoryId(),product.getName(), product.getPrice(), product.getImage()));
                sendPhoto1.setReplyMarkup(InlineKeyboardUtil.confirmAddProductMarkup());
                sendPhoto1.setChatId("1360288792");  // ADMIN ID
                sendPhoto1.setParseMode(ParseMode.MARKDOWN);
                ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendPhoto1);

            }
        }
    }

    private void handleContact(User user, Message message) {
        Contact contact = message.getContact();
        String customerId = String.valueOf(contact.getUserId());

        Customer customer = CustomerService.getCustomerById(customerId);
        if (customer == null) {
            customer = new Customer(customerId, contact.getFirstName(),
                    contact.getLastName(), contact.getPhoneNumber(), CustomerStatus.SHARE_CONTACT);
            CustomerService.addCustomer(customer);
        }

        SendMessage sendMessage = new SendMessage(
                String.valueOf(message.getChatId()), "*Foydalanish uchun /go - commandasini yuboring*"

        );
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage.setParseMode("Markdown");
        ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendMessage);
    }

    private void handleText(User user, Message message) {
        String text = message.getText();
        String chatId = String.valueOf(message.getChatId());
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        Customer customer = CustomerService.getCustomerById(String.valueOf(message.getChatId()));

        if (text.equals("/start")) {
            if (customer == null) {
                sendMessage.setText("\uD83D\uDC4B *Assalomu alaykum!\n\n" +
                        "\uD83D\uDCF1Raqamingizni jo'nating.*");
                sendMessage.setParseMode("Markdown");
                sendMessage.setReplyMarkup(KeyboardUtil.contactMarkup());

                ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendMessage);
            } else {
                sendMessage.setText("*Siz avval start bosgansiz , botni ishlatish uchun /go yuboring*");
                sendMessage.setParseMode("Markdown");
                ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendMessage);
            }
        }
        else if (text.equals("/go")) {
            sendMessage.setText("*\uD83D\uDC4BAssalomu Aleykum , AvtoElon botiga hush kelibsiz! E'lon joylashtirish qoydasi bilan tanishib chiqing❗️*");
            sendMessage.setParseMode("Markdown");
            sendMessage.setChatId(chatId);
            InlineKeyboardButton go = InlineButtonUtil.button("Elo'n berish", "Подать объявление", "\uD83D\uDC8E");
            InlineKeyboardButton po = InlineButtonUtil.button("Elo'nlarni korish", "Просмотреть объявление", "✅");
            InlineKeyboardButton bx = InlineButtonUtil.button("Bizga xat yozing", "Написать нам", "\uD83D\uDCEC");
            InlineKeyboardButton sh = InlineButtonUtil.button("Biz haqimizda", "О нас", "\uD83D\uDC64");
            InlineKeyboardButton ks = InlineButtonUtil.button("Ko'p beriladigan savollar", "often", "❓");
            List<InlineKeyboardButton> row = InlineButtonUtil.row(go, po);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(bx, sh);
            List<InlineKeyboardButton> row3 = InlineButtonUtil.row(ks);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row, row2, row3);
            sendMessage.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendMessage);
        }
        else if (text.equals("/ifs")) {
            EditMessageText editMessageText = new EditMessageText();
            sendMessage.setText(" ❗ AvtoElon - boti bilan foydalanish uchun *Sotuvchilar* uchun malumot: " +
                    "\n\uD83D\uDE97  *1.  AvtoMobillar haqida" +
                    "\n⚙  2.  Ehtiyot qismlar va avtomobil mahsulotlari haqida" +
                    "\n\uD83D\uDE9C  3.  Tijorat Transportlar haqida*");
            sendMessage.setParseMode("Markdown");
            InlineKeyboardButton one = InlineButtonUtil.button("", "one", ":one:");
            InlineKeyboardButton two = InlineButtonUtil.button("", "two", ":two:");
            InlineKeyboardButton three = InlineButtonUtil.button("", "three", ":three:");
            InlineKeyboardButton backmenu = InlineButtonUtil.button("Menu-ga qaytish ❎", "backmenu", "❎");
            List<InlineKeyboardButton> row = InlineButtonUtil.row(one, two, three);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(backmenu);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row, row2);
            sendMessage.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendMessage);
        }
        else if (text.equals("/ifp")) {
            sendMessage.setText("❗ Elonlarni joylayshtirish qoidasini oqib chiqing️" +
                    "         https://avtoelon.uz/content/articles/service-regulations/");
            sendMessage.setParseMode("Markdown");
            sendMessage.setChatId(String.valueOf(message.getChatId()));
            InlineKeyboardButton backmenu = InlineButtonUtil.button("Menu-ga qaytish ❎", "backmenu", "❎");
            List<InlineKeyboardButton> row = InlineButtonUtil.row(backmenu);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row);
            sendMessage.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendMessage);
        }
        else if (text.startsWith("/report")) {
            String c = message.getFrom().getUserName();
            SendMessage sendMessage1 = new SendMessage();
            sendMessage.setText("*    ❗️@" + c +
                    " - dan Report qabul qilindi \n \n \uD83D\uDCCAReport haqida ma'lumot: \n \n"
                    + message.getText().substring(7) + "*");
            sendMessage.setParseMode("Markdown");
            sendMessage.setChatId("1360288792");
            sendMessage1.setText("*✅ Report adminga yuborildi " +
                    "\n Admin @hasannishanov tez orada javob beradi!*");
            sendMessage1.setParseMode("Markdown");
            sendMessage1.setChatId(String.valueOf(message.getChatId()));
            InlineKeyboardButton backmenu = InlineButtonUtil.button("Menu-ga qaytish ❎", "backmenu", "❎");
            List<InlineKeyboardButton> row = InlineButtonUtil.row(backmenu);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row);
            sendMessage1.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendMessage);
        }
        else if (ComponentContainer.productStepMap1.containsKey(chatId)) {

            Product product = productMap.get(chatId);

            if (ComponentContainer.productStepMap1.get(chatId).equals(CustomerStatus.SELECT_CATEGORY_FOR_ADD_PRODUCT)) {
                product.setName(text);
                ComponentContainer.productStepMap1.put(chatId, CustomerStatus.ENTERED_PRODUCT_NAME);
                sendMessage.setText("Mahsulot narxini kiriting(haqiqiy musbat son): ");


            } else if (ComponentContainer.productStepMap1.get(chatId).equals(CustomerStatus.ENTERED_PRODUCT_NAME)) {
                double price = 0;
                try {
                    price = Double.parseDouble(text.trim());
                } catch (NumberFormatException e) {
                }

                if (price <= 0) {
                    sendMessage.setText("Narx noto'g'ri kiritildi, Qaytadan narxni kiriting: ");

                } else {
                    product.setPrice(price);
                    ComponentContainer.productStepMap1.put(chatId, CustomerStatus.ENTERED_PRODUCT_PRICE);

                    sendMessage.setText("Mahsulotning rasmini jo'nating: ");

                }
            } else if (ComponentContainer.productStepMap1.get(chatId).equals(CustomerStatus.DELETE_PRODUCT)) {
                ProductService.deleteProduct(Integer.valueOf(text));
                sendMessage.setText("Mahsulot o'chirildi ! \n\n Amalni Tanlang ");

                productMap.remove(chatId);
                ComponentContainer.productStepMap1.remove(chatId);


                sendMessage.setReplyMarkup(InlineKeyboardUtil.productMenu());

            }

        }
        ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendMessage);


    }

    public void handleCallBack(User user, Message message, String data) {


        SendMessage sendMessage = new SendMessage();

        EditMessageText editMessageText = new EditMessageText();


        String chatId = String.valueOf(message.getChatId());

        User from = message.getFrom();

        String text = message.getText();


        if (data.equals("one")) {
            System.out.println("one ishladi");
            sendMessage.setText("\uD83D\uDE98 Agar siz avtomobilingizni sotmoqchi bolsangiz shu ko'rsatmaga amal qilishingiz kerak:" + "\n       *1. Rasmlar bolish shart" + "\n       2. Uning marka/modelini tanlash kerak" + "\n       3. Uning kuzovi (sedan/mikroavtobus...) tanlash kerak" + "\n       4. Chiqazilgan yilini belgilang" + "\n       5. Savdo (bor/yoq) belgilang" + "\n       6. Dvigatel quvvati(2.3l/3...) tanlash kerak" + "\n       7. Yoqilg'i turi (benzin/gaz/dizel/elektr) belgilang" + "\n       8. Uzatish qutisi (mehanika/avtomat) belgilang " + "\n       9. Probegi(km) belgilang" + "\n       10. Rangini tanlash kerak" + "\n       11. Haydovchi Blok (oldingi/orqa/to'la) belgilang" + "\n       12. Qoshimcha malumotlarni qozib qoldiring*");
            sendMessage.setParseMode("Markdown");
            sendMessage.setChatId(chatId);
            InlineKeyboardButton fnazad = InlineButtonUtil.button("", "one.nazad", "⬅  \uD83D\uDE9C ️");
            InlineKeyboardButton fpered = InlineButtonUtil.button("", "one.pered", "⚙️  ➡");
            InlineKeyboardButton backmenu = InlineButtonUtil.button("Menu-ga qaytish ❎", "backmenu", "❎");
            List<InlineKeyboardButton> row = InlineButtonUtil.row(fnazad, fpered);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(backmenu);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row, row2);
            sendMessage.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendMessage);
        }
        if (data.equals("two")) {
            sendMessage.setText("⚙️Agar siz ehtiyot qismlar va avtomobil mahsulotlarini sotmoqchi bolsangiz shu ko'rsatmaga amal qilishingiz kerak:" + "\n        *1. Rasmlar bolish shart" + "\n        2. Ehtiyot qism nomini belgilang" + "\n        3. Uning marka/modelini tanlash kerak" + "\n        4. Uning holati (бу/yangi) belgilang" + "\n        5. Mavjudligini (mavjud/zakazga) belgilang" + "\n        6. Savdoni (ha/yoq) belgilang" + "\n        7. Qoshimcha malumotlarni yozib qoldiring*");
            sendMessage.setParseMode("Markdown");
            sendMessage.setChatId(chatId);
            InlineKeyboardButton fnazad = InlineButtonUtil.button("", "two.nazad", "⬅  \uD83D\uDE98  ️");
            InlineKeyboardButton fpered = InlineButtonUtil.button("", "two.pered", "\uD83D\uDE9C️  ➡");
            InlineKeyboardButton backmenu = InlineButtonUtil.button("Menu-ga qaytish ❎", "backmenu", "❎");
            List<InlineKeyboardButton> row = InlineButtonUtil.row(fnazad, fpered);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(backmenu);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row, row2);
            sendMessage.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendMessage);

        }
        if (data.equals("three")) {
            sendMessage.setText("\uD83D\uDE9C Agar siz Tijorat Transportni sotmoqchi bolsangiz shu ko'esatmaga amal qilishingiz kerak:" + "\n        *1. Rasmlar bolish shart" + "\n        2. Uskunalar turini (abrovoz/samosvla...) belgilang" + "\n        3. Uning markasini tanlash kerak" + "\n        4. Uning modelini (~75 belgi) tanlang" + "\n        5. Chiqazilgan yilini belgilang" + "\n        6. Savodoni(ha/yoq) belgilang" + "\n        7. Yoqilg'i(bengiz/gaz...) turini belgilang" + "\n        8. Qoshimcha melumotlarni yozib qoldiring*");
            sendMessage.setParseMode("Markdown");
            sendMessage.setChatId(chatId);
            InlineKeyboardButton fnazad = InlineButtonUtil.button("", "three.nazad", "⬅⚙️");
            InlineKeyboardButton fpered = InlineButtonUtil.button("", "three.pered", "\uD83D\uDE97➡");
            InlineKeyboardButton backmenu = InlineButtonUtil.button("Menu-ga qaytish ❎", "backmenu", "❎");
            List<InlineKeyboardButton> row = InlineButtonUtil.row(fnazad, fpered);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(backmenu);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row, row2);
            sendMessage.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendMessage);

        }
        if (data.equals("one.nazad")) {
            editMessageText.setText("\uD83D\uDE9C Agar siz Tijorat Transportni sotmoqchi bolsangiz shu ko'esatmaga amal qilishingiz kerak:" + "\n        *1. Rasmlar bolish shart" + "\n        2. Uskunalar turini (abrovoz/samosvla...) belgilang" + "\n        3. Uning markasini tanlash kerak" + "\n        4. Uning modelini (~75 belgi) tanlang" + "\n        5. Chiqazilgan yilini belgilang" + "\n        6. Narhini (so'm/y.e) belgilang" + "\n        7. Savodoni(ha/yoq) belgilang" + "\n        8. Yoqilg'i(bengiz/gaz...) turini belgilang" + "\n        9. Qoshimcha melumotlarni yozib qoldiring*");
            editMessageText.setParseMode("Markdown");
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(message.getMessageId());
            InlineKeyboardButton fnazad = InlineButtonUtil.button("", "three.nazad", "⬅⚙️");
            InlineKeyboardButton fpered = InlineButtonUtil.button("", "three.pered", "\uD83D\uDE97➡");
            InlineKeyboardButton backmenu = InlineButtonUtil.button("Menu-ga qaytish ❎", "backmenu", "❎");
            List<InlineKeyboardButton> row = InlineButtonUtil.row(fnazad, fpered);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(backmenu);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row, row2);
            editMessageText.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(editMessageText);
        }
        if (data.equals("one.pered")) {
            editMessageText.setText("⚙️Agar siz ehtiyot qismlar va avtomobil mahsulotlarini sotmoqchi bolsangiz shu ko'rsatmaga amal qilishingiz kerak:" + "\n        *1. Rasmlar bolish shart" + "\n        2. Ehtiyot qism nomini belgilang" + "\n        3. Uning marka/modelini tanlash kerak" + "\n        4. Uning holati (бу/yangi) belgilang" + "\n        5. Mavjudligini (mavjud/zakazga) belgilang" + "\n        6. Narhini (so'm/y.e) belgilang" + "\n        7. Savdoni (ha/yoq) belgilang" + "\n        8. Qoshimcha malumotlarni yozib qoldiring*");
            editMessageText.setParseMode("Markdown");
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(message.getMessageId());
            InlineKeyboardButton fnazad = InlineButtonUtil.button("", "two.nazad", "⬅  \uD83D\uDE97  ️");
            InlineKeyboardButton fpered = InlineButtonUtil.button("", "two.pered", "\uD83D\uDE9C️  ➡");
            InlineKeyboardButton backmenu = InlineButtonUtil.button("Menu-ga qaytish ❎", "backmenu", "❎");
            List<InlineKeyboardButton> row = InlineButtonUtil.row(fnazad, fpered);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(backmenu);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row, row2);
            editMessageText.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(editMessageText);
        }
        if (data.equals("two.nazad")) {

            editMessageText.setText("\uD83D\uDE98 Agar siz avtomobilingizni sotmoqchi bolsangiz shu ko'rsatmaga amal qilishingiz kerak:" + "\n       *1. Rasmlar bolish shart" + "\n       2. Uning marka/modelini tanlash kerak" + "\n       3. Uning kuzovi (sedan/mikroavtobus...) tanlash kerak" + "\n       4. Chiqazilgan yilini belgilang" + "\n       5. Narhini belgilang" + "\n       6. Savdo (bor/yoq) belgilang" + "\n       7. Dvigatel quvvati(2.3l/3...) tanlash kerak" + "\n       8. Yoqilg'i turi (benzin/gaz/dizel/elektr) belgilang" + "\n       9. Uzatish qutisi (mehanika/avtomat) belgilang " + "\n       10. Probegi(km) belgilang" + "\n       11. Rangini tanlash kerak" + "\n       12. Haydovchi Blok (oldingi/orqa/to'la) belgilang" + "\n       13. Qoshimcha malumotlarni qozib qoldiring*");
            editMessageText.setParseMode("Markdown");
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(message.getMessageId());
            InlineKeyboardButton fnazad = InlineButtonUtil.button("", "one.nazad", "⬅  \uD83D\uDE9C ️");
            InlineKeyboardButton fpered = InlineButtonUtil.button("", "one.pered", "⚙️  ➡");
            InlineKeyboardButton backmenu = InlineButtonUtil.button("Menu-ga qaytish ❎", "backmenu", "❎");
            List<InlineKeyboardButton> row = InlineButtonUtil.row(fnazad, fpered);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(backmenu);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row, row2);
            editMessageText.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(editMessageText);
        }
        if (data.equals("two.pered")) {
            editMessageText.setText("\uD83D\uDE9C Agar siz Tijorat Transportni sotmoqchi bolsangiz shu ko'esatmaga amal qilishingiz kerak:" + "\n        *1. Rasmlar bolish shart" + "\n        2. Uskunalar turini (abrovoz/samosvla...) belgilang" + "\n        3. Uning markasini tanlash kerak" + "\n        4. Uning modelini (~75 belgi) tanlang" + "\n        5. Chiqazilgan yilini belgilang" + "\n        6. Narhini (so'm/y.e) belgilang" + "\n        7. Savodoni(ha/yoq) belgilang" + "\n        8. Yoqilg'i(bengiz/gaz...) turini belgilang" + "\n        9. Qoshimcha melumotlarni yozib qoldiring*");
            editMessageText.setParseMode("Markdown");
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(message.getMessageId());
            InlineKeyboardButton fnazad = InlineButtonUtil.button("", "three.nazad", "⬅  ⚙️ ️");
            InlineKeyboardButton fpered = InlineButtonUtil.button("", "three.pered", "\uD83D\uDE97️  ➡");
            InlineKeyboardButton backmenu = InlineButtonUtil.button("Menu-ga qaytish ❎", "backmenu", "❎");
            List<InlineKeyboardButton> row = InlineButtonUtil.row(fnazad, fpered);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(backmenu);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row, row2);
            editMessageText.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(editMessageText);
        }
        if (data.equals("three.nazad")) {
            editMessageText.setText("⚙️Agar siz ehtiyot qismlar va avtomobil mahsulotlarini sotmoqchi bolsangiz shu ko'rsatmaga amal qilishingiz kerak:" + "\n        *1. Rasmlar bolish shart" + "\n        2. Ehtiyot qism nomini belgilang" + "\n        3. Uning marka/modelini tanlash kerak" + "\n        4. Uning holati (бу/yangi) belgilang" + "\n        5. Mavjudligini (mavjud/zakazga) belgilang" + "\n        6. Narhini (so'm/y.e) belgilang" + "\n        7. Savdoni (ha/yoq) belgilang" + "\n        8. Qoshimcha malumotlarni yozib qoldiring*");
            editMessageText.setParseMode("Markdown");
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(message.getMessageId());
            InlineKeyboardButton fnazad = InlineButtonUtil.button("", "two.nazad", "⬅  \uD83D\uDE98 ️");
            InlineKeyboardButton fpered = InlineButtonUtil.button("", "two.pered", "\uD83D\uDE9C️  ➡");
            InlineKeyboardButton backmenu = InlineButtonUtil.button("Menu-ga qaytish ❎", "backmenu", "❎");
            List<InlineKeyboardButton> row = InlineButtonUtil.row(fnazad, fpered);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(backmenu);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row, row2);
            editMessageText.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(editMessageText);

        }
        if (data.equals("three.pered")) {
            editMessageText.setText("\uD83D\uDE98 Agar siz avtomobilingizni sotmoqchi bolsangiz shu ko'rsatmaga amal qilishingiz kerak:" + "\n       *1. Rasmlar bolish shart" + "\n       2. Uning marka/modelini tanlash kerak" + "\n       3. Uning kuzovi (sedan/mikroavtobus...) tanlash kerak" + "\n       4. Chiqazilgan yilini belgilang" + "\n       5. Narhini belgilang" + "\n       6. Savdo (bor/yoq) belgilang" + "\n       7. Dvigatel quvvati(2.3l/3...) tanlash kerak" + "\n       8. Yoqilg'i turi (benzin/gaz/dizel/elektr) belgilang" + "\n       9. Uzatish qutisi (mehanika/avtomat) belgilang " + "\n       10. Probegi(km) belgilang" + "\n       11. Rangini tanlash kerak" + "\n       12. Haydovchi Blok (oldingi/orqa/to'la) belgilang" + "\n       13. Qoshimcha malumotlarni qozib qoldiring*");
            editMessageText.setParseMode("Markdown");
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(message.getMessageId());
            InlineKeyboardButton fnazad = InlineButtonUtil.button("", "one.nazad", "⬅  \uD83D\uDE9C ️");
            InlineKeyboardButton fpered = InlineButtonUtil.button("", "one.pered", "⚙️  ➡");
            InlineKeyboardButton backmenu = InlineButtonUtil.button("Menu-ga qaytish ❎", "backmenu", "❎");
            List<InlineKeyboardButton> row = InlineButtonUtil.row(fnazad, fpered);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(backmenu);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row, row2);
            editMessageText.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(editMessageText);
        }
        // DOP
        if (data.equals("often")) {
            System.out.println("ifga kirdi");
            editMessageText.setText("*\uD83D\uDCB0 /ifs - Sotuvchilar uchun malumot - bilish uchun commandasini yuboring.                        \n" +
                    " \uD83D\uDCB0 /ifp - E'lonlarni joylashtirish qoidalari.*");
            editMessageText.setParseMode(ParseMode.MARKDOWN);
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(message.getMessageId());
            InlineKeyboardButton backmenu = InlineButtonUtil.button("Menu-ga qaytish ❎", "backmenu", "❎");
            List<InlineKeyboardButton> row = InlineButtonUtil.row(backmenu);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row);
            editMessageText.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(editMessageText);
        }
        // NM
        if (data.equals("Написать нам")) {
            editMessageText.setText("*Siz support bolimida siz , qanaqa savolaringiz bor?\n" +
                    "       1. ⚙️Texnik muomolar boyicha\n" +
                    "       2. \uD83D\uDCFA Reklama boyicha\n" +
                    "       3. \uD83D\uDCAC Admin bilan muloqaga chiqish*");

            editMessageText.setParseMode("Markdown");
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(message.getMessageId());
            InlineKeyboardButton tp = InlineButtonUtil.button("", "tp", ":one:");
            InlineKeyboardButton pb = InlineButtonUtil.button("", "pb", ":two:");
            InlineKeyboardButton ac = InlineButtonUtil.button("", "ac", ":three:");
            InlineKeyboardButton backmenu = InlineButtonUtil.button("Menu-ga qaytish ❎", "backmenu", "❎");
            List<InlineKeyboardButton> row = InlineButtonUtil.row(tp, pb, ac);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(backmenu);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row, row2);
            editMessageText.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(editMessageText);
        }
        if (data.equals("tp")) {
            editMessageText.setText("❗️*Ozingizni muamoingizni forma boylab toldirib yuboring!" +
                    "\n /report - [Muamongizni yozing]*");
            editMessageText.setParseMode("Markdown");
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(message.getMessageId());
            InlineKeyboardButton backmenu = InlineButtonUtil.button("Support menusiga qaytish ❎", "backsmenu", "❎");
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(backmenu);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row2);
            editMessageText.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(editMessageText);


        }
        if (data.equals("pb")) {
            editMessageText.setText("❗️\uD83D\uDCB8 *Reklama Narhlari:\n" +
                    "        1️⃣ 4 - Sogot top-da/24 sog'ot lentada + chat =70.000 so'm\n" +
                    "        2️⃣ Tungi top-da/48 sog'ot lentada + chat = 99.999 so'm\n" +
                    "        3️⃣ 1 - Kunga kanalda zakrep = 18.000 so'm\n" +
                    "Reklama boyicha: @hasannishanov - ga yozing*");
            editMessageText.setParseMode("Markdown");
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(message.getMessageId());
            InlineKeyboardButton backmenu = InlineButtonUtil.button("Support menusiga qaytish ❎", "backsmenu", "❎");
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(backmenu);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row2);
            editMessageText.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(editMessageText);
        }
        if (data.equals("ac")) {
            editMessageText.setText("❗️*Admin useri: @hasannishanov , Feyklarga aldanmang\uD83D\uDEAB*");
            editMessageText.setParseMode("Markdown");
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(message.getMessageId());
            InlineKeyboardButton backmenu = InlineButtonUtil.button("Support menusiga qaytish ❎", "backsmenu", "❎");
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(backmenu);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row2);
            editMessageText.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(editMessageText);
        }
        // NM
        if (data.equals("backmenu")) {
            editMessageText.setText("*\uD83D\uDC4BAssalomu Aleykum , AvtoElon botiga hush kelibsiz! E'lon joylashtirish qoydasi bilan tanishib chiqing❗️*");
            editMessageText.setParseMode("Markdown");
            editMessageText.setChatId(chatId);
            InlineKeyboardButton go = InlineButtonUtil.button("Elo'n berish", "Подать объявление", "\uD83D\uDC8E");
            InlineKeyboardButton po = InlineButtonUtil.button("Elo'nlarni korish", "Просмотреть объявление", "✅");
            InlineKeyboardButton bx = InlineButtonUtil.button("Bizga xat yozing", "Написать нам", "\uD83D\uDCEC");
            InlineKeyboardButton sh = InlineButtonUtil.button("Biz haqimizda", "О нас", "\uD83D\uDC64");
            InlineKeyboardButton ks = InlineButtonUtil.button("Ko'p beriladigan savollar", "often", "❓");
            List<InlineKeyboardButton> row = InlineButtonUtil.row(go, po);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(bx, sh);
            List<InlineKeyboardButton> row3 = InlineButtonUtil.row(ks);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row, row2, row3);
            editMessageText.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            editMessageText.setMessageId(message.getMessageId());
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(editMessageText);
        }
        if (data.equals("backsmenu")) {
            editMessageText.setText("*Siz support bolimida siz , qanaqa savolaringiz bor?\n" +
                    "       1. ⚙️Texnik muomolar boyicha\n" +
                    "       2. \uD83D\uDCFA Reklama boyicha\n" +
                    "       3. \uD83D\uDCAC Admin bilan muloqaga chiqish*");

            editMessageText.setParseMode("Markdown");
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(message.getMessageId());
            InlineKeyboardButton tp = InlineButtonUtil.button("", "tp", ":one:");
            InlineKeyboardButton pb = InlineButtonUtil.button("", "pb", ":two:");
            InlineKeyboardButton ac = InlineButtonUtil.button("", "ac", ":three:");
            InlineKeyboardButton backmenu = InlineButtonUtil.button("Menu-ga qaytish ❎", "backmenu", "❎");
            List<InlineKeyboardButton> row = InlineButtonUtil.row(tp, pb, ac);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(backmenu);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row, row2);
            editMessageText.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(editMessageText);


        }
        // PO
        if (data.equals("Подать объявление")) {
            editMessageText.setText("*\uD83C\uDF0DE'lon berishdan avval , /ifs - qoydalarni oqib chiqin*");
            editMessageText.setParseMode("Markdown");
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(message.getMessageId());
            InlineKeyboardButton mc = InlineButtonUtil.button("Qoydalarni oqib chiqtim", "prochel", "✅");
            InlineKeyboardButton exit = InlineButtonUtil.button("Menu-ga qaytish❎", "backmenu", "❎");
            List<InlineKeyboardButton> row = InlineButtonUtil.row(mc);
            List<InlineKeyboardButton> row1 = InlineButtonUtil.row(exit);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row,row1);
            editMessageText.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(editMessageText);
        }
        if (data.equals("prochel")) {
            editMessageText.setText("❗️*E'lon tog'ri toldirish uchun , Namunalarni korib chiqin " +
                    "\n          1. \uD83D\uDE98 Avtomobil namunasi" +
                    "\n          2. ⚙️ Ehtiyot qismlar va avtomobil mahsulotlari namunasi" +
                    "\n          3. \uD83D\uDE9C Tijorat Transportni namunasi*");
            editMessageText.setMessageId(message.getMessageId());
            editMessageText.setChatId(chatId);
            editMessageText.setParseMode(ParseMode.MARKDOWN);
            InlineKeyboardButton a = InlineButtonUtil.button("", "oneb", ":one:");
            InlineKeyboardButton b = InlineButtonUtil.button("", "twob", ":two:");
            InlineKeyboardButton c = InlineButtonUtil.button("", "threeb", ":three:");
            InlineKeyboardButton d = InlineButtonUtil.button("E'lon berish", "giveelon", "✅");
            InlineKeyboardButton e = InlineButtonUtil.button("Menuga qaytish❎", "add_product", "❎");
            List<InlineKeyboardButton> row = InlineButtonUtil.row(a, b, c);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(d);
            List<InlineKeyboardButton> row3 = InlineButtonUtil.row(e);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row, row2, row3);
            editMessageText.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(editMessageText);
        }
        if (data.equals("oneb")) {
            SendPhoto sendPhoto = new SendPhoto();
            String c = ("*❗️E'lon berish uchun , e'lonni tog'ri toldiring \n Namuna:\n     \uD83D\uDE98 Avtomobil sotiladi \n       1. Chevrolet / Lacetti \n       2. Kuzov: Sedan \n       3. Chiqazilgan yili: 2021 \n       4. Narxi: 10.000$ \n       5. Savdo: Bor \n       6. Dvigatel quvvati: 1.4l  \n       7. Yoqilg'i tury: Benzin \n       8. Uzatish qutisi: Mehanik Korobka \n       9. Probegi: 250km \n       10. Rangi: Qora \n       11. Haydovchi blok: To'la \n       12. Qoshimcha ma'lumot: Lacetti ozim olganman , urilagan , chizilmagan , oladigan odam bilan savdolashamiz*");
            InputFile inputFile = new InputFile("https://files.glotr.uz/company/000/015/715/products/2020/04/27/2020-04-27-13-49-51-732565-fc3dd513b91dd08c4af34a89f4dcc642.jpg?_=ozbol");
            sendPhoto.setPhoto(inputFile);
            sendPhoto.setCaption(c);
            sendPhoto.setChatId(chatId);
            sendPhoto.setParseMode("Markdown");
            InlineKeyboardButton fnazad = InlineButtonUtil.button("", "oneb.nazad", "⬅  \uD83D\uDE9C ️");
            InlineKeyboardButton fpered = InlineButtonUtil.button("", "oneb.pered", "⚙️  ➡");
            InlineKeyboardButton mc = InlineButtonUtil.button("E'lon berish", "add_product", "✅");
            InlineKeyboardButton addButton1 = InlineButtonUtil.button("❎ Menuga qaytish ❎", "backmenu");
            List<InlineKeyboardButton> row1 = InlineButtonUtil.row(fnazad, fpered);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(mc);
            List<InlineKeyboardButton> row3 = InlineButtonUtil.row(addButton1);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row1, row2,row3);
            sendPhoto.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendPhoto);
        }
        if (data.equals("twob")) {
            SendPhoto sendPhoto = new SendPhoto();
            String c = ("*❗️E'lon berish uchun , e'lonni tog'ri toldiring \n Namuna:\n     ⚙️ Ehtiyot qism sotiladi \n       1. Nomi: Antiradar \n       2. Modeli: Neoline 7500s \n       3. Holati: б/у \n       4. Narxi: 110$ \n       5. Savdo: Yoq \n       6. Qoshimcha ma'lumot: Holati yahshi , hamma proshivkasi ornatilgan*");
            InputFile inputFile = new InputFile("https://tasinha-photos-kluz.kcdn.online/webp/07/07a05604-cb9f-402e-88fa-16218b1820fb/1-408x306.jpg");
            sendPhoto.setPhoto(inputFile);
            sendPhoto.setCaption(c);
            sendPhoto.setChatId(chatId);
            sendPhoto.setParseMode("Markdown");
            InlineKeyboardButton fnazad = InlineButtonUtil.button("", "twob.nazad", "⬅  \uD83D\uDE98  ️");
            InlineKeyboardButton fpered = InlineButtonUtil.button("", "twob.pered", "\uD83D\uDE9C️  ➡");
            InlineKeyboardButton mc = InlineButtonUtil.button("E'lon berish", "add_product", "✅");
            InlineKeyboardButton addButton1 = InlineButtonUtil.button("❎ Menuga qaytish ❎", "backmenu");
            List<InlineKeyboardButton> row1 = InlineButtonUtil.row(fnazad, fpered);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(mc);
            List<InlineKeyboardButton> row3 = InlineButtonUtil.row(addButton1);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row1, row2,row3);
            sendPhoto.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendPhoto);
        }
        if (data.equals("threeb")) {
            SendPhoto sendPhoto = new SendPhoto();
            String c = ("*❗️E'lon berish uchun , e'lonni tog'ri toldiring \n Namuna:\n     \uD83D\uDE9C Tijorat transporti sotiladi \n       1. Uskuna turi: Bortovoy \n       2. Marka: UAZ \n       3. Model: Profi Polutorka \n       4. Chiqazilgan yili: 2021 \n       5. Narxi: 21.000$ \n       6. Savdo: Yoq  \n       7. Yoqilg'i tury: Gaz \n       12. Qoshimcha ma'lumot: Iztotermik furgon bazasi chozilgan(4x3). Garantiya 3 yil!*");
            InputFile inputFile = new InputFile("https://tasinha-photos-kluz.kcdn.online/webp/ec/ec08387e-d430-4ae4-878f-11c231fd1705/5-408x306.jpg");
            sendPhoto.setPhoto(inputFile);
            sendPhoto.setCaption(c);
            sendPhoto.setChatId(chatId);
            sendPhoto.setParseMode("Markdown");
            InlineKeyboardButton fnazad = InlineButtonUtil.button("", "threeb.nazad", "⬅⚙️");
            InlineKeyboardButton fpered = InlineButtonUtil.button("", "threeb.pered", "\uD83D\uDE97➡");
            InlineKeyboardButton mc = InlineButtonUtil.button("E'lon berish", "add_product", "✅");
            InlineKeyboardButton addButton1 = InlineButtonUtil.button("❎ Menuga qaytish ❎", "backmenu");
            List<InlineKeyboardButton> row1 = InlineButtonUtil.row(fnazad, fpered);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(mc);
            List<InlineKeyboardButton> row3 = InlineButtonUtil.row(addButton1);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row1, row2,row3);
            sendPhoto.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendPhoto);
        }
        if (data.equals("oneb.nazad")) {
            SendPhoto sendPhoto = new SendPhoto();
            String c = ("*❗️E'lon berish uchun , e'lonni tog'ri toldiring \n Namuna:\n     \uD83D\uDE9C Tijorat transporti sotiladi \n       1. Uskuna turi: Bortovoy \n       2. Marka: UAZ \n       3. Model: Profi Polutorka \n       4. Chiqazilgan yili: 2021 \n       5. Narxi: 21.000$ \n       6. Savdo: Yoq  \n       7. Yoqilg'i tury: Gaz \n       12. Qoshimcha ma'lumot: Iztotermik furgon bazasi chozilgan(4x3). Garantiya 3 yil!*");
            InputFile inputFile = new InputFile("https://tasinha-photos-kluz.kcdn.online/webp/ec/ec08387e-d430-4ae4-878f-11c231fd1705/5-408x306.jpg");
            sendPhoto.setPhoto(inputFile);
            sendPhoto.setCaption(c);
            sendPhoto.setChatId(chatId);
            sendPhoto.setParseMode("Markdown");
            InlineKeyboardButton fnazad = InlineButtonUtil.button("", "threeb.nazad", "⬅⚙️");
            InlineKeyboardButton fpered = InlineButtonUtil.button("", "threeb.pered", "\uD83D\uDE97➡");
            InlineKeyboardButton mc = InlineButtonUtil.button("E'lon berish", "add_product", "✅");
            InlineKeyboardButton addButton1 = InlineButtonUtil.button("❎ Menuga qaytish ❎", "backmenu");
            List<InlineKeyboardButton> row1 = InlineButtonUtil.row(fnazad, fpered);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(mc);
            List<InlineKeyboardButton> row3 = InlineButtonUtil.row(addButton1);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row1, row2,row3);
            sendPhoto.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendPhoto);
        }
        if (data.equals("oneb.pered")) {
            SendPhoto sendPhoto = new SendPhoto();
            String c = ("*❗️E'lon berish uchun , e'lonni tog'ri toldiring \n Namuna:\n     ⚙️ Ehtiyot qism sotiladi \n       1. Nomi: Antiradar \n       2. Modeli: Neoline 7500s \n       3. Holati: б/у \n       4. Narxi: 110$ \n       5. Savdo: Yoq \n       6. Qoshimcha ma'lumot: Holati yahshi , hamma proshivkasi ornatilgan*");
            InputFile inputFile = new InputFile("https://tasinha-photos-kluz.kcdn.online/webp/07/07a05604-cb9f-402e-88fa-16218b1820fb/1-408x306.jpg");
            sendPhoto.setPhoto(inputFile);
            sendPhoto.setCaption(c);
            sendPhoto.setChatId(chatId);
            sendPhoto.setParseMode("Markdown");
            InlineKeyboardButton fnazad = InlineButtonUtil.button("", "twob.nazad", "⬅  \uD83D\uDE98  ️");
            InlineKeyboardButton fpered = InlineButtonUtil.button("", "twob.pered", "\uD83D\uDE9C️  ➡");
            InlineKeyboardButton mc = InlineButtonUtil.button("E'lon berish", "add_product", "✅");
            InlineKeyboardButton addButton1 = InlineButtonUtil.button("❎ Menuga qaytish ❎", "backmenu");
            List<InlineKeyboardButton> row1 = InlineButtonUtil.row(fnazad, fpered);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(mc);
            List<InlineKeyboardButton> row3 = InlineButtonUtil.row(addButton1);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row1, row2,row3);
            sendPhoto.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendPhoto);
        }
        if (data.equals("twob.nazad")) {
            SendPhoto sendPhoto = new SendPhoto();
            String c = ("*❗️E'lon berish uchun , e'lonni tog'ri toldiring \n Namuna:\n     \uD83D\uDE98 Avtomobil sotiladi \n       1. Chevrolet / Lacetti \n       2. Kuzov: Sedan \n       3. Chiqazilgan yili: 2021 \n       4. Narxi: 10.000$ \n       5. Savdo: Bor \n       6. Dvigatel quvvati: 1.4l  \n       7. Yoqilg'i tury: Benzin \n       8. Uzatish qutisi: Mehanik Korobka \n       9. Probegi: 250km \n       10. Rangi: Qora \n       11. Haydovchi blok: To'la \n       12. Qoshimcha ma'lumot: Lacetti ozim olganman , urilagan , chizilmagan , oladigan odam bilan savdolashamiz*");
            InputFile inputFile = new InputFile("https://files.glotr.uz/company/000/015/715/products/2020/04/27/2020-04-27-13-49-51-732565-fc3dd513b91dd08c4af34a89f4dcc642.jpg?_=ozbol");
            sendPhoto.setPhoto(inputFile);
            sendPhoto.setCaption(c);
            sendPhoto.setChatId(chatId);
            sendPhoto.setParseMode("Markdown");
            InlineKeyboardButton fnazad = InlineButtonUtil.button("", "oneb.nazad", "⬅  \uD83D\uDE9C ️");
            InlineKeyboardButton fpered = InlineButtonUtil.button("", "oneb.pered", "⚙️  ➡");
            InlineKeyboardButton mc = InlineButtonUtil.button("E'lon berish", "add_product", "✅");
            InlineKeyboardButton addButton1 = InlineButtonUtil.button("❎ Menuga qaytish ❎", "backmenu");
            List<InlineKeyboardButton> row1 = InlineButtonUtil.row(fnazad, fpered);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(mc);
            List<InlineKeyboardButton> row3 = InlineButtonUtil.row(addButton1);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row1, row2,row3);
            sendPhoto.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendPhoto);
        }
        if (data.equals("twob.pered")) {
            SendPhoto sendPhoto = new SendPhoto();
            String c = ("*❗️E'lon berish uchun , e'lonni tog'ri toldiring \n Namuna:\n     \uD83D\uDE9C Tijorat transporti sotiladi \n       1. Uskuna turi: Bortovoy \n       2. Marka: UAZ \n       3. Model: Profi Polutorka \n       4. Chiqazilgan yili: 2021 \n       5. Narxi: 21.000$ \n       6. Savdo: Yoq  \n       7. Yoqilg'i tury: Gaz \n       12. Qoshimcha ma'lumot: Iztotermik furgon bazasi chozilgan(4x3). Garantiya 3 yil!*");
            InputFile inputFile = new InputFile("https://tasinha-photos-kluz.kcdn.online/webp/ec/ec08387e-d430-4ae4-878f-11c231fd1705/5-408x306.jpg");
            sendPhoto.setPhoto(inputFile);
            sendPhoto.setCaption(c);
            sendPhoto.setChatId(chatId);
            sendPhoto.setParseMode("Markdown");
            InlineKeyboardButton fnazad = InlineButtonUtil.button("", "threeb.nazad", "⬅⚙️");
            InlineKeyboardButton fpered = InlineButtonUtil.button("", "threeb.pered", "\uD83D\uDE97➡");
            InlineKeyboardButton mc = InlineButtonUtil.button("E'lon berish", "add_product", "✅");
            InlineKeyboardButton addButton1 = InlineButtonUtil.button("❎ Menuga qaytish ❎", "backmenu");
            List<InlineKeyboardButton> row1 = InlineButtonUtil.row(fnazad, fpered);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(mc);
            List<InlineKeyboardButton> row3 = InlineButtonUtil.row(addButton1);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row1, row2,row3);
            sendPhoto.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendPhoto);
        }
        if (data.equals("threeb.nazad")) {
            SendPhoto sendPhoto = new SendPhoto();
            String c = ("*❗️E'lon berish uchun , e'lonni tog'ri toldiring \n Namuna:\n     ⚙️ Ehtiyot qism sotiladi \n       1. Nomi: Antiradar \n       2. Modeli: Neoline 7500s \n       3. Holati: б/у \n       4. Narxi: 110$ \n       5. Savdo: Yoq \n       6. Qoshimcha ma'lumot: Holati yahshi , hamma proshivkasi ornatilgan*");
            InputFile inputFile = new InputFile("https://tasinha-photos-kluz.kcdn.online/webp/07/07a05604-cb9f-402e-88fa-16218b1820fb/1-408x306.jpg");
            sendPhoto.setPhoto(inputFile);
            sendPhoto.setCaption(c);
            sendPhoto.setChatId(chatId);
            sendPhoto.setParseMode("Markdown");
            InlineKeyboardButton fnazad = InlineButtonUtil.button("", "twob.nazad", "⬅  \uD83D\uDE98  ️");
            InlineKeyboardButton fpered = InlineButtonUtil.button("", "twob.pered", "\uD83D\uDE9C️  ➡");
            InlineKeyboardButton mc = InlineButtonUtil.button("E'lon berish", "add_product", "✅");
            InlineKeyboardButton addButton1 = InlineButtonUtil.button("❎ Menuga qaytish ❎", "backmenu");
            List<InlineKeyboardButton> row1 = InlineButtonUtil.row(fnazad, fpered);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(mc);
            List<InlineKeyboardButton> row3 = InlineButtonUtil.row(addButton1);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row1, row2,row3);
            sendPhoto.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendPhoto);
        }
        if (data.equals("threeb.pered")) {
            SendPhoto sendPhoto = new SendPhoto();
            String c = ("*❗️E'lon berish uchun , e'lonni tog'ri toldiring \n Namuna:\n     \uD83D\uDE98 Avtomobil sotiladi \n       1. Chevrolet / Lacetti \n       2. Kuzov: Sedan \n       3. Chiqazilgan yili: 2021 \n       4. Narxi: 10.000$ \n       5. Savdo: Bor \n       6. Dvigatel quvvati: 1.4l  \n       7. Yoqilg'i tury: Benzin \n       8. Uzatish qutisi: Mehanik Korobka \n       9. Probegi: 250km \n       10. Rangi: Qora \n       11. Haydovchi blok: To'la \n       12. Qoshimcha ma'lumot: Lacetti ozim olganman , urilagan , chizilmagan , oladigan odam bilan savdolashamiz*");
            InputFile inputFile = new InputFile("https://files.glotr.uz/company/000/015/715/products/2020/04/27/2020-04-27-13-49-51-732565-fc3dd513b91dd08c4af34a89f4dcc642.jpg?_=ozbol");
            sendPhoto.setPhoto(inputFile);
            sendPhoto.setCaption(c);
            sendPhoto.setChatId(chatId);
            sendPhoto.setParseMode("Markdown");
            InlineKeyboardButton fnazad = InlineButtonUtil.button("", "oneb.nazad", "⬅  \uD83D\uDE9C ️");
            InlineKeyboardButton fpered = InlineButtonUtil.button("", "oneb.pered", "⚙️  ➡");
            InlineKeyboardButton mc = InlineButtonUtil.button("E'lon berish", "add_product", "✅");
            InlineKeyboardButton addButton1 = InlineButtonUtil.button("❎ Menuga qaytish ❎", "backmenu");
            List<InlineKeyboardButton> row1 = InlineButtonUtil.row(fnazad, fpered);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(mc);
            List<InlineKeyboardButton> row3 = InlineButtonUtil.row(addButton1);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row1, row2,row3);
            sendPhoto.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendPhoto);
        }
        if (data.equals("giveelon")) {
            editMessageText.setText("*❗️Yodindizga tuting agar e'lon notog'ri forma bilan toldirilsa Admin e'lon joylashuvini rad eta oladi " +
                    "\n        Admin: @hasannishanov*");
            editMessageText.setParseMode(ParseMode.MARKDOWN);
            editMessageText.setChatId(String.valueOf(message.getChatId()));
            editMessageText.setMessageId(message.getMessageId());



            InlineKeyboardButton addButton = InlineButtonUtil.button("✅ Chunarli ✅","add_product");
            InlineKeyboardButton addButton1 = InlineButtonUtil.button("❎ Menuga qaytish ❎", "backmenu");
            List<InlineKeyboardButton> row1 = InlineButtonUtil.row(addButton);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(addButton1);
            List<List<InlineKeyboardButton>> rowList = InlineButtonUtil.collection(row1,row2);
            editMessageText.setReplyMarkup(InlineButtonUtil.keyboard(rowList));
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(editMessageText);


        }
        if (data.equals("Просмотреть объявление")) {
            editMessageText.setText("*\uD83D\uDCCA E'lon kategorisayini belgilang*");
            editMessageText.setParseMode(ParseMode.MARKDOWN);
            InlineKeyboardButton car = InlineButtonUtil.button("Avtomobil", "avt", "\uD83D\uDE98");
            InlineKeyboardButton ehq = InlineButtonUtil.button("Ehtiyot qismlar", "ehq", "⚙️");
            InlineKeyboardButton tjr = InlineButtonUtil.button("Tijorat transport", "tjr", "\uD83D\uDE9C️");
            InlineKeyboardButton bm = InlineButtonUtil.button("Menu-ga qaytish❎", "backmenu", "❎️");
            List<InlineKeyboardButton> row1 = InlineButtonUtil.row(car);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(ehq);
            List<InlineKeyboardButton> row3 = InlineButtonUtil.row(tjr);
            List<InlineKeyboardButton> row4 = InlineButtonUtil.row(bm);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row1,row2,row3,row4);
            editMessageText.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(message.getMessageId());
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(editMessageText);

        }
        else if(data.equals("avt")) {
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
                InlineKeyboardButton bm = InlineButtonUtil.button("Menuga qaytish❎", "backmenu", "❎️");
                List<InlineKeyboardButton> row4 = InlineButtonUtil.row(bm);
                List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row4);
                sendPhoto.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
                sendPhoto.setParseMode(ParseMode.MARKDOWN);
                ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendPhoto);
            }


        }
        else if(data.equals("ehq")){
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
        }
        else if(data.equals("tjr")){
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

        }
        if (data.equals("add_product")) {

            DeleteMessage deleteMessage = new DeleteMessage(
                    chatId, message.getMessageId()
            );
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(deleteMessage);

            sendMessage.setParseMode(ParseMode.MARKDOWN);

            sendMessage.setText("*\uD83D\uDCCAKategoriyalardan birini tanlang:*");
            InlineKeyboardButton mc = InlineButtonUtil.button("Menuga qaytish", "backmenu", "✅");
            List<InlineKeyboardButton> row1 = InlineButtonUtil.row(mc);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row1);
            sendMessage.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            sendMessage.setChatId(chatId);
            sendMessage.setReplyMarkup(InlineKeyboardUtil.categoryInlineMarkup());
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendMessage);



            productMap.remove(chatId);
            ComponentContainer.productStepMap1.remove(chatId);

            ComponentContainer.productStepMap1.put(chatId, CustomerStatus.CLICKED_ADD_PRODUCT);

            productMap.put(chatId,
                    new Product(null, null, null, null));

        }







        else if (data.startsWith("add_product_category_id")) {
            DeleteMessage deleteMessage = new DeleteMessage(
                    chatId, message.getMessageId()
            );
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(deleteMessage);

            int categoryId = Integer.parseInt(data.split("/")[1]);

            SendMessage sendMessage1 = new SendMessage(
                    chatId, "*❗️E'lon berish uchun , e'lonni tog'ri toldiring *"
            );
            sendMessage1.setParseMode("Markdown");
            ComponentContainer.productStepMap1.put(chatId, CustomerStatus.SELECT_CATEGORY_FOR_ADD_PRODUCT);
            Product product = productMap.get(chatId);
            product.setCategoryId(categoryId);
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendMessage1);

        }












        else if (data.equals("add_product_commit")) {

            Product product = productMap.get(chatId);
            System.out.println("product = " + product);

            System.out.println("Setttan otdi");

            ProductService.addProduct(product);
            System.out.println("add boldi");

            productMap.remove(chatId);
            System.out.println("productMap remove boldi");
            ComponentContainer.productStepMap1.remove(chatId);
            System.out.println("productstepmap remove boldi");


            sendMessage.setText("\t \n ✅ Saqlandi.\n\n" + "Amalni tanlang:");
            sendMessage.setChatId(chatId);
            sendMessage.setReplyMarkup(InlineKeyboardUtil.productMenu());
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendMessage);

        }
        else if (data.equals("add_product_cancel")) {
            DeleteMessage deleteMessage = new DeleteMessage(
                    chatId, message.getMessageId()
            );
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(deleteMessage);

            productMap.remove(chatId);
            ComponentContainer.productStepMap1.remove(chatId);
            sendMessage.setText("✅ Bekor qilindi" +
                    "\n \uD83D\uDCCA Kategoriyalardan birini tanlang:");
            sendMessage.setChatId(chatId);
            sendMessage.setChatId(String.valueOf(message.getChatId()));
            sendMessage.setReplyMarkup(InlineKeyboardUtil.productMenu());
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendMessage);

        }
        else if (data.equals("show_product_list")) {
            editMessageText.setText("*\uD83D\uDCCA E'lon kategorisayini belgilang*");
            editMessageText.setParseMode(ParseMode.MARKDOWN);
            InlineKeyboardButton car = InlineButtonUtil.button("Avtomobil", "avt", "\uD83D\uDE98");
            InlineKeyboardButton ehq = InlineButtonUtil.button("Ehtiyot qismlar", "ehq", "⚙️");
            InlineKeyboardButton tjr = InlineButtonUtil.button("Tijorat transport", "tjr", "\uD83D\uDE9C️");
            InlineKeyboardButton bm = InlineButtonUtil.button("Menuga qaytish❎", "backmenu", "❎️");
            List<InlineKeyboardButton> row1 = InlineButtonUtil.row(car);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(ehq);
            List<InlineKeyboardButton> row3 = InlineButtonUtil.row(tjr);
            List<InlineKeyboardButton> row4 = InlineButtonUtil.row(bm);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row1,row2,row3,row4);
            editMessageText.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(message.getMessageId());
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(editMessageText);

        }
        else if(data.equals("avt")) {
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
        }
        else if(data.equals("ehq")){
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
        }
        else if(data.equals("tjr")){
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
        }
        else if (data.equals("delete_product")) {

            DeleteMessage deleteMessage = new DeleteMessage(
                    chatId, message.getMessageId()
            );
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(deleteMessage);

            ProductService.loadProductList();

            for (Product product : Database.productList) {
                SendPhoto sendPhoto = new SendPhoto(chatId, new InputFile(product.getImage()));
                sendPhoto.setCaption(String.format("*\uD83D\uDCCA Kategoriya: %s\n" +
                                "Mahsulot:\n %s \n Narxi: %s\n*",
                        CategoryService.getCategoryById(product.getCategoryId()).getName(),
                        product.getName(), product.getPrice()));
                sendPhoto.setParseMode(ParseMode.MARKDOWN);
                ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendPhoto);
            }

            sendMessage.setText("*\uD83D\uDDD1 O'chirmoqchi bolgan mahsulotning ID sini kriiting*");
            sendMessage.setChatId(chatId);
            ComponentContainer.productStepMap1.put(chatId, CustomerStatus.DELETE_PRODUCT);

            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(sendMessage);

        }
        else if (data.equals("О нас")){
            editMessageText.setText("*\uD83D\uDCCAAvtoelon - O'zbekiston muallifi uchun mahsulot: veb-sayt, mobil ilova, Telegram kanali. Autoelon 20 yillik rivojlanish tajribasiga ega bo'lgan mutaxassislar tomonidan yaratilgan. Biz gazetani kolesa.kz yirik avtoportalidan va \"G'ildiraklar\" mobil ilovalaridan chiqib ketganimizdan so'ng, Qozog'istonda tarqalish bo'yicha muhim bo'lganlarni hisobga olmaganda boshladik. Avtoelon mazmuni - jismoniy va firmalarning manzillari e'lonlari, avtomobillar, mototsikllar va maxsus texnika, ehtiyot qismlar, aksessuarlar xaridorlari va sotuvchilari uchun qidiruv bloki, shuningdek, avtomobil xizmatlarini qidirish. Sayt va mobil ilovalar bitta asosiy va funksional funksiya bilan ishlaydi.:*");
            editMessageText.setParseMode(ParseMode.MARKDOWN);
            InlineKeyboardButton bizhaqimizda = InlineButtonUtil.button("Batafsil malumot", "backmenu", "✅");
            InlineKeyboardButton mc = InlineButtonUtil.button("Menu-ga qaytish❎", "backmenu", "❎️");
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText("ℹ Batafsil malumot");
            inlineKeyboardButton.setUrl("https://avtoelon.uz/content/articles/about/");
            InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
            inlineKeyboardButton1.setText("\uD83D\uDDFA Bizning haritamiz");
            inlineKeyboardButton1.setUrl("https://avtoelon.uz/sitemap/");
            List<InlineKeyboardButton> row1 = InlineButtonUtil.row(inlineKeyboardButton);
            List<InlineKeyboardButton> row2 = InlineButtonUtil.row(inlineKeyboardButton1);
            List<InlineKeyboardButton> row3 = InlineButtonUtil.row(mc);
            List<List<InlineKeyboardButton>> rowcollection = InlineButtonUtil.collection(row1,row2,row3);
            editMessageText.setReplyMarkup(InlineButtonUtil.keyboard(rowcollection));
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(message.getMessageId());
            ComponentContainer.MY_TELEGRAM_BOT.sendMsg(editMessageText);

        }
    }
}

