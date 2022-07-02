package com.company;


import com.company.container.ComponentContainer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {

    public static void main(String[] args) {


        TelegramBotsApi telegramBotsApi = null;
        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        AvtoBot avtoBot = new AvtoBot ();
        ComponentContainer.MY_TELEGRAM_BOT = avtoBot ;
        try {
            assert telegramBotsApi != null;
            telegramBotsApi.registerBot(avtoBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }


    }
}
