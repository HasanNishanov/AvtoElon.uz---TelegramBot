package com.company.container;

import com.company.AvtoBot;
import com.company.enums.AdminStatus;
import com.company.enums.CustomerStatus;
import com.company.model.Product;


import java.util.HashMap;
import java.util.Map;

public abstract class ComponentContainer {

    public static final String BOT_TOKEN = "5298958747:AAFQ-D68J3mT8uBsfdHhZBqv6rRyE8Y6OKs";
    public static final String BOT_NAME = "avtoelonyz_bot";

    public static final String ADMIN_ID = "136028879";

    public static AvtoBot MY_TELEGRAM_BOT;

    public static Map<String, Product> productMap = new HashMap<>();

    public static Map<String, AdminStatus> productStepMap = new HashMap<>();
    public static Map<String, CustomerStatus> productStepMap1 = new HashMap<>();


}
