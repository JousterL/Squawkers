package org.fog.bot;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;


@Slf4j
@Component
public class SquawkersLongPulling extends TelegramLongPollingBot {

    @Value("${bot.api-key}")
    private String botToken;

    @Value("${bot.username}")
    private String botUserName;

    @Override
    public void onUpdateReceived(Update update) {
        log.info("Got an update {}", update);
        if(update.hasMessage()){
            if(StringUtils.equalsIgnoreCase("",update.getMessage().));
            String[] messageArray = StringUtils.split(update.getMessage().getText()," ");
            String command = StringUtils.lowerCase(messageArray[0]);
            switch(command){
                case "help":
                    break;
                case "list":
                    break;
                case "join":
                    break;
                case "notify":
                    break;
                case "create":
                    break;
                case "remove":
                    break;
                case "delete":
                    break;
                case "exit":
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
