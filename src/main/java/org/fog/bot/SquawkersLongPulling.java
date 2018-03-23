package org.fog.bot;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.List;


@Slf4j
@Component
public class SquawkersLongPulling extends TelegramLongPollingBot {

    @Value("${bot.api-key}")
    private String botToken;

    @Value("${bot.username}")
    private String botUserName;

    @Value("${admin.users}")
    private List<Long> admins;

    @Value("${group.whitelist}")
    private List<Long> authorizedGroupIds;

    @Override
    public void onUpdateReceived(Update update) {
        log.info("Got an update {}", update);
        if(update.hasMessage()){
            if(!authorizedGroupIds.contains(update.getMessage().getChatId())){
                return;
            }
            if(StringUtils.startsWithIgnoreCase("/list",update.getMessage().getText())){
                return;
            } else if (StringUtils.startsWithIgnoreCase("/help",update.getMessage().getText())){
                SendMessage response = new SendMessage();
                response.setChatId(update.getMessage().getChatId());
                String commandList = "List of all possible commands.\n" +
                        "/list\n" +
                        "/help\n" +
                        "/join\n" +
                        "/notify\n" +
                        "/create\n" +
                        "/remove\n" +
                        "/delete\n";
                response.setText(commandList);
                try {
                    execute(response);
                } catch (TelegramApiException e) {
                    log.error("Failed to send message. {}", e.getMessage());
                }
                return;
            } else if (StringUtils.startsWithIgnoreCase("/exit",update.getMessage().getText())){
                return;
            }
            String[] messageArray = StringUtils.split(update.getMessage().getText()," ");
            String command = StringUtils.remove(StringUtils.lowerCase(messageArray[0]),"/");
            String group = StringUtils.lowerCase(messageArray[1]);
            StringBuilder argBuilder = new StringBuilder();
            for(int i=2; i<messageArray.length; i++){
                argBuilder.append(messageArray[i]);
            }
            String args = argBuilder.toString();
            switch(command){
                case "join":
                    log.info("Got the join command.");
                    break;
                case "notify":
                    log.info("Got the notify command.");
                    break;
                case "create":
                    log.info("Got the create command.");
                    break;
                case "remove":
                    log.info("Got the remove command.");
                    break;
                case "delete":
                    log.info("Got the delete command.");
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
