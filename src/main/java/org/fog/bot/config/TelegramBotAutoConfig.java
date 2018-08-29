package org.fog.bot.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.generics.BotSession;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties("telegram")
@Slf4j
@ConditionalOnBean({TelegramLongPollingBot.class, TelegramWebhookBot.class})
public class TelegramBotAutoConfig {
    private List<BotSession> sessions = new ArrayList<>();

    @Autowired(required = false)
    private List<TelegramLongPollingBot> pollingBots = new ArrayList<>();

    @Autowired(required = false)
    private List<TelegramWebhookBot> webHookBots = new ArrayList<>();

    @Getter
    @Setter
    private String externalUrl = null;

    @Getter
    @Setter
    private String internalUrl = null;

    @Getter
    @Setter
    private String keyStore = null;

    @Getter
    @Setter
    private String keyStorePassword;

    @Getter
    @Setter
    private String pathToCertificate;

    static {
        ApiContextInitializer.init();
    }

    @PostConstruct
    public void start() throws TelegramApiRequestException {
        log.info("Starting auto config for telegram bots");
        TelegramBotsApi api = getApi();
        pollingBots.forEach(bot -> {
            try {
                log.info("Registering polling bot: {}", bot.getBotUsername());
                sessions.add(api.registerBot(bot));
            } catch (TelegramApiException e) {
                log.error("Failed to register bot {} due to error {}", bot.getBotUsername(), e.getMessage());
            }
        });
        webHookBots.forEach(bot -> {
            try {
                log.info("Registering web hook bot: {}", bot.getBotUsername());
                api.registerBot(bot);
            } catch (TelegramApiException e) {
                log.error("Failed to register bot {} due to error {}", bot.getBotUsername(), e.getMessage());
            }
        });
    }

    /**
     * Get API object depending on configured properties.
     * If no properties are configured, the API object won't support webhooks.
     *
     * @return api object
     * @throws TelegramApiRequestException
     */
    private TelegramBotsApi getApi() throws TelegramApiRequestException {
        TelegramBotsApi result;
        if (!StringUtils.isEmpty(externalUrl) && !StringUtils.isEmpty(internalUrl)) {
            if (!StringUtils.isEmpty(keyStore) && !StringUtils.isEmpty(keyStorePassword)) {
                if (!StringUtils.isEmpty(pathToCertificate)) {
                    log.info("Initializing API with webhook support and configured keystore and path to certificate");
                    result = new TelegramBotsApi(keyStore, keyStorePassword, externalUrl, internalUrl, pathToCertificate);
                } else {
                    log.info("Initializing API with webhook support and configured keystore");
                    result = new TelegramBotsApi(keyStore, keyStorePassword, externalUrl, internalUrl);
                }
            } else {
                log.info("Initializing API with webhook support");
                result = new TelegramBotsApi(externalUrl, internalUrl);
            }
        } else {
            log.info("Initializing API without webhook support");
            result = new TelegramBotsApi();
        }
        return result;
    }

    @PreDestroy
    public void stop() {
        sessions.stream().forEach(session -> {
            if (session != null) {
                session.stop();
            }
        });
    }

}

