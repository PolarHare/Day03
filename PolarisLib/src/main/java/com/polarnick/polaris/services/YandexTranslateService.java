package com.polarnick.polaris.services;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.polarnick.polaris.concurrency.AsyncCallbackWithFailures;
import com.polarnick.polaris.utils.HTTPUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Date: 21.09.13
 *
 * @author Nickolay Polyarniy aka PolarNick
 */
public class YandexTranslateService {

    private static final String TRANSLATE_API_URL_PREFIX = "https://translate.yandex.net/api/v1.5/tr.json/translate" +
            "?key=%s" + "&text=%s" + "&lang=%s";
    private static final String DETECT_LANG_API_URL_PREFIX = "https://translate.yandex.net/api/v1.5/tr.json/detect" +
            "?key=%s" + "&text=%s";
    private static final String ENCODING_CHARSET = "UTF-8";

    private final ExecutorService threadsPool;
    private final String key;
    private final List<String> languages;

    public YandexTranslateService(String key, List<String> languages) {
        Preconditions.checkNotNull(key);
        Preconditions.checkArgument(languages.size() == 2, "There are must be exactly two languages!");
        this.threadsPool = Executors.newCachedThreadPool();
        this.key = key;
        this.languages = languages;
    }

    public void translateAsync(final String text, final AsyncCallbackWithFailures<String, IOException> callback) {
        threadsPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String translated = translate(text);
                    callback.onSuccess(translated);
                } catch (IOException e) {
                    callback.onFailure(e);
                }
            }
        });
    }

    public String translate(String text) throws IOException {
        try {
            String fromLanguage = recognizeLanguage(text);
            String toLanguage = null;
            for (String language : languages) {
                if (!language.equalsIgnoreCase(fromLanguage)) {
                    toLanguage = language;
                    break;
                }
            }
            Preconditions.checkState(toLanguage != null, "ToLanguage should be always found. At least because of" +
                    " that there are two languages chosen!");

            return translate(text, toLanguage);
        } catch (IOException e) {
            throw new IOException("Internet connection problem!", e);
        }
    }

    private String recognizeLanguage(String text) throws IOException {
        String url = String.format(DETECT_LANG_API_URL_PREFIX, key, URLEncoder.encode(text, ENCODING_CHARSET));
        String responseContent = HTTPUtils.getContent(url, ENCODING_CHARSET);
        DetectLanguageResult detectionResult = new Gson().fromJson(responseContent, DetectLanguageResult.class);
        return detectionResult.getLang();
    }

    private String translate(final String text, final String toLanguage) throws IOException {
        String url = String.format(TRANSLATE_API_URL_PREFIX, key, URLEncoder.encode(text, ENCODING_CHARSET), toLanguage);
        String responseContent = HTTPUtils.getContent(url, ENCODING_CHARSET);
        TranslatedResult translatedResult = new Gson().fromJson(responseContent, TranslatedResult.class);
        return translatedResult.getConcatanatedText();
    }

    @SuppressWarnings({"UnusedDeclaration", "MismatchedQueryAndUpdateOfCollection"})//Used and initialized by Gson
    private static class TranslatedResult {
        private int code;
        private List<String> text;

        public String getConcatanatedText() {
            StringBuilder result = new StringBuilder();
            for (String part : text) {
                result.append(part);
            }
            return result.toString();
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})//Used by Gson
    private static class DetectLanguageResult {
        private int code;
        private String lang;

        private String getLang() {
            return lang;
        }
    }
}
