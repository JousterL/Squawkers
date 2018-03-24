package org.fog.bot;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.fog.bot.group.Group;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Component
public class SquawkersLongPulling extends TelegramLongPollingBot implements ApplicationContextAware{

    @Value("${bot.api-key}")
    private String botToken;
    @Value("${bot.username}")
    private String botUserName;
    @Value("${admin.users}")
    private String admins;
    @Value("${group.whitelist}")
    private String authorizedGroupIds;

    private Map<String,Group> groupMap;
    private ConfigurableApplicationContext cac;

    @PostConstruct
    public void buildGroupMap(){
        groupMap = new HashMap<>();

        File directory = new File(".");
        log.info("working dir {}", directory.getAbsolutePath());
        Collection<File> fileList = FileUtils.listFiles(directory,new String[]{"grp"},false);
        if (fileList.isEmpty()){
            log.error("No group files found in current directory.");
            return;
        }
        fileList.forEach(file -> {
            try {
                String groupName=StringUtils.removeEndIgnoreCase(file.getName(),".grp");
                Group g = Group.parseGroupFile(groupName);
                log.info("Loaded group, {}",groupName);
                groupMap.put(groupName,g);
            } catch (IOException e) {
                log.error("Failed to load group file. {}", e.getMessage());
            }

        });
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.debug("Got an update {}", update);
        Long chatId = update.getMessage().getChatId();
        User usr = update.getMessage().getFrom();
        if(update.hasMessage()){
            if(!validateGroup(chatId)){
                log.info("Unauthorized Chat. {}",update.getMessage().getChatId());
                return;
            }
            if(StringUtils.startsWithIgnoreCase("/list",update.getMessage().getText())){
                return;
            } else if (StringUtils.startsWithIgnoreCase("/help",update.getMessage().getText())){
                                String commandList = "List of all possible commands.\n" +
                        "/list\n" +
                        "/help\n" +
                        "/join\n" +
                        "/notify\n" +
                        "/create\n" +
                        "/remove\n" +
                        "/delete\n";
                sendMessage(commandList,chatId);
                return;
            } else if (StringUtils.startsWithIgnoreCase("/exit",update.getMessage().getText())){
                if(validateAdmin(usr.getId())) {
                    sendMessage("Shutting Down-not", chatId);
                } else {
                    sendMessage("I can't let you do that "+usr.getUserName(), chatId);
                }
                return;
            }
            String[] messageArray = StringUtils.split(update.getMessage().getText()," ");
            String command = StringUtils.remove(StringUtils.lowerCase(messageArray[0]),"/");

            if(messageArray.length<=1){
                return;
            }

            String group = StringUtils.lowerCase(messageArray[1]);
            StringBuilder argBuilder = new StringBuilder();
            for(int i=2; i<messageArray.length; i++){
                argBuilder.append(messageArray[i]);
            }
            String args = argBuilder.toString();
            switch(command){
                case "join":
                    log.info("Got the join command.");
                    if(!groupMap.containsKey(group)){
                        sendMessage("Unable to find group: "+group, chatId);
                        break;
                    }
                    groupMap.get(group).getMembers().add(usr.getUserName());
                    try {
                        Group.saveGroupFile(groupMap.get(group));
                    } catch (IOException e) {
                        sendMessage("Failed save group: "+group, chatId);
                        break;
                    }
                    sendMessage("Added to group: "+group, chatId);
                    break;
                case "notify":
                    log.info("Got the notify command.");
                    if(!groupMap.containsKey(group)){
                        sendMessage("Unable to find group: "+group, chatId);
                        break;
                    }
                    if(groupMap.get(group).getMembers().isEmpty()){
                        sendMessage("Group is empty :<", chatId);
                        break;
                    }
                    StringBuilder notList = new StringBuilder();
                    groupMap.get(group).getMembers().forEach(mem -> notList.append("@").append(mem).append(" "));
                    sendMessage(notList.toString(), chatId);
                    break;
                case "create":
                    log.info("Got the create command.");
                    try {
                        Group.saveGroupFile(new Group(group,null));
                    } catch (IOException e) {
                        log.error("Failed to save group.");
                        sendMessage("Failed to create group",chatId);
                    }
                    break;
                case "remove":
                    log.info("Got the remove command.");
                    if(!groupMap.containsKey(group)){
                        sendMessage("Unable to find group: "+group, chatId);
                        break;
                    }
                    groupMap.get(group).getMembers().remove(usr.getUserName());
                    try {
                        Group.saveGroupFile(groupMap.get(group));
                    } catch (IOException e) {
                        log.error("Failed to save group.");
                    }
                    sendMessage("Removed from group: "+group, chatId);
                    break;
                case "delete":
                    log.info("Got the delete command.");
                    if(validateAdmin(usr.getId())) {
                        if(!groupMap.containsKey(group)){
                            sendMessage("Unable to find group: "+group, chatId);
                            break;
                        }
                        try {
                            Group.deleteGroupFile(group);
                            groupMap.remove(group);
                        } catch (IOException e) {
                            sendMessage("Failed to delete: "+group, chatId);
                        }
                        sendMessage("Deleted group: "+group, chatId);
                        break;
                    } else {
                        sendMessage("I can't let you do that "+usr.getUserName(), chatId);
                    }
                    break;
                default:
                    sendMessage("Invalid command", chatId);
                    break;
            }
        }
    }

    private boolean validateGroup(Long groupId) {
        for(String id : StringUtils.split(authorizedGroupIds,",")){
            if(groupId==Long.parseLong(id)){return true;}
        }
        return false;
    }

    private boolean validateAdmin(Integer dudeId) {
        for(String admin : StringUtils.split(admins,",")){
            if(dudeId==Integer.parseInt(admin)){return true;}
        }
        return false;
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    private void sendMessage(String message, Long chatId){
        SendMessage response = new SendMessage();
        response.setChatId(chatId);
        response.setText(message);
        try {
            execute(response);
        } catch (TelegramApiException e) {
            log.error("Failed to send message. {}", e.getMessage());
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.cac=(ConfigurableApplicationContext)applicationContext;
    }
}
