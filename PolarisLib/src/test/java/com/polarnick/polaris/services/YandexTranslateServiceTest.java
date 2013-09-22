package com.polarnick.polaris.services;

import com.google.common.collect.Lists;
import com.polarnick.polaris.concurrency.AsyncCallbackWithFailures;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Date: 21.09.13
 *
 * @author Nickolay Polyarniy aka PolarNick
 */
public class YandexTranslateServiceTest {

    public static final long MAX_WAIT_MS = 2000;

    public static final String ACCOUNT_KEY = "trnsl.1.1.20130921T184657Z.2b7426257ae60b23.5a54cb565a7471b7462b5f3bef1895e281bfc9d8";

    public static final List<String> EN_RU = Lists.newArrayList("en", "ru");
    public static final List<String> RU_EN = Lists.newArrayList("ru", "en");

    public static final String TREE_TEXT_RU = "дерево";
    public static final String TREE_TEXT_EN = "tree";

    private final YandexTranslateService translatorEnRu = new YandexTranslateService(ACCOUNT_KEY, EN_RU);
    private final YandexTranslateService translatorRuEn = new YandexTranslateService(ACCOUNT_KEY, RU_EN);

    @Test
    public void testTranslating() throws IOException {
        Assert.assertEquals(TREE_TEXT_RU, translatorEnRu.translate(TREE_TEXT_EN));
        Assert.assertEquals(TREE_TEXT_EN, translatorEnRu.translate(TREE_TEXT_RU));
        Assert.assertEquals(TREE_TEXT_RU, translatorRuEn.translate(TREE_TEXT_EN));
        Assert.assertEquals(TREE_TEXT_EN, translatorRuEn.translate(TREE_TEXT_RU));
    }

//    @Test
//    public void testTranslatingAsync() throws InterruptedException {
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicReference<String> translated = new AtomicReference<String>(null);
//        translatorRuEn.translateAsync(TREE_TEXT_EN, new AsyncCallbackWithFailures<String, IOException>() {
//
//            @Override
//            public void onSuccess(String result) {
//                Assert.assertEquals(TREE_TEXT_RU, result);
//                translated.set(result);
//                latch.countDown();
//            }
//
//            @Override
//            public void onFailure(IOException reason) {
//                Assert.fail();
//            }
//        });
//        latch.await(MAX_WAIT_MS, TimeUnit.MILLISECONDS);
//        Assert.assertEquals(TREE_TEXT_RU, translated.get());
//    }

}