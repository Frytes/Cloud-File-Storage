package com.frytes.cloudstorage.e2e;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideElement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;

class CloudStorageE2ETest {

    @BeforeAll
    static void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-proxy-server");
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        Configuration.browserCapabilities = options;

        Configuration.headless = true;
        Configuration.baseUrl = "http://localhost";
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 10000;
    }

    @Test
    void shouldCompleteFullUserFlow() {
        String testUser = "u_" + System.currentTimeMillis();
        String testPass = "Secret123!";
        String folderName = "MyTestFolder";

        // 1. Регистрация
        open("/registration");
        $("#username").setValue(testUser);
        $("#password").setValue(testPass);
        $("#password_confirm").setValue(testPass);
        $(byText("Зарегистрироваться")).click();
        $(byText("Корневой каталог")).shouldBe(visible, Duration.ofSeconds(10));

        // 2. Загрузка файла
        $$("input[type='file']").first().uploadFromClasspath("e2e-test.txt");

        SelenideElement file = $("[data-id='e2e-test.txt']");
        file.shouldBe(visible, Duration.ofSeconds(15));

        // 3. Создание папки
        $("svg[data-testid='AddIcon']").parent().click();
        $$(byText("Cоздать папку")).findBy(visible).click();
        $("#filename").setValue(folderName);
        $(byText("Save")).click();

        SelenideElement targetFolder = $("[data-id='" + folderName + "/']");
        targetFolder.shouldBe(visible, Duration.ofSeconds(10));

        // 4. Перемещение файла (Drag & Drop)
        file.click();
        sleep(300);
        actions()
                .clickAndHold(file)
                .pause(Duration.ofMillis(500))
                .moveToElement(targetFolder)
                .pause(Duration.ofMillis(500))
                .release()
                .perform();

        file.shouldNotBe(visible, Duration.ofSeconds(10));

        // 5. Удаление папки
        targetFolder.contextClick();
        $$(byText("Удалить")).findBy(visible).shouldBe(visible).click();

        targetFolder.shouldNotBe(visible, Duration.ofSeconds(10));
    }
}