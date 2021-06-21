package com.geekbrains.tests.automator

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
class BehaviorTest {

    //Класс UiDevice предоставляет доступ к вашему устройству.
    //Именно через UiDevice вы можете управлять устройством, открывать приложения
    //и находить нужные элементы на экране
    private val uiDevice: UiDevice = UiDevice.getInstance(getInstrumentation())

    //Контекст нам понадобится для запуска нужных экранов и получения packageName
    private val context = ApplicationProvider.getApplicationContext<Context>()

    //Путь к классам нашего приложения, которые мы будем тестировать
    private val packageName = context.packageName

    @Before
    fun setup() {
        //Для начала сворачиваем все приложения, если у нас что-то запущено
        uiDevice.pressHome()

        //Запускаем наше приложение
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        //Мы уже проверяли Интент на null в предыдущем тесте, поэтому допускаем, что Интент у нас не null
        intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)//Чистим бэкстек от запущенных ранее Активити
        context.startActivity(intent)

        //Ждем, когда приложение откроется на смартфоне чтобы начать тестировать его элементы
        uiDevice.wait(Until.hasObject(By.pkg(packageName).depth(0)), TIMEOUT)
    }

    //Убеждаемся, что приложение открыто. Для этого достаточно найти на экране любой элемент
    //и проверить его на null
    @Test
    fun test_MainActivityIsStarted() {
        //Через uiDevice находим editText
        val editText = uiDevice.findObject(By.res(packageName, "searchEditText"))
        //Проверяем на null
        Assert.assertNotNull(editText)
    }

    //Убеждаемся, что поиск работает как ожидается
    @Test
    fun test_SearchIsPositive() {
        //Через uiDevice находим editText
        val editText = uiDevice.findObject(By.res(packageName, "searchEditText"))
        //Устанавливаем значение
        editText.text = "UiAutomator"
        //Отправляем запрос через Espresso
//        Espresso.onView(ViewMatchers.withId(R.id.searchEditText))
//            .perform(ViewActions.pressImeActionButton())

        //Отправляем запрос НЕ через Espresso
        val searchButton: UiObject2 = uiDevice.findObject(By.res(packageName, "searchButton"))
        searchButton.click()

        //Ожидаем конкретного события: появления текстового поля totalCountTextView.
        //Это будет означать, что сервер вернул ответ с какими-то данными, то есть запрос отработал.
        val changedText =
            uiDevice.wait(
                Until.findObject(By.res(packageName, "totalCountTextView")),
                TIMEOUT
            )
        //Убеждаемся, что сервер вернул корректный результат. Обратите внимание, что количество
        //результатов может варьироваться во времени, потому что количество репозиториев постоянно меняется.
        Assert.assertEquals(changedText.text.toString(), "Number of results: 672")
    }

    @Test
    fun test_SearchIsNegative() {
        uiDevice.executeShellCommand("settings put global airplane_mode_on 1");
        uiDevice.executeShellCommand(
            "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true");
        Thread.sleep(5000L)

        val editText = uiDevice.findObject(By.res(packageName, "searchEditText"))
        //Устанавливаем значение
        editText.text = "UiAutomator"

        val searchButton: UiObject2 = uiDevice.findObject(By.res(packageName, "searchButton"))
        searchButton.click()

        val changedText =
            uiDevice.wait(
                Until.findObject(By.res(packageName, "totalCountTextView")),
                TIMEOUT
            )

        Assert.assertEquals(changedText.text, "Undefined error")

        uiDevice.executeShellCommand("settings put global airplane_mode_on 0");
        uiDevice.executeShellCommand(
            "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false");
        Thread.sleep(10000L)
    }

    //Убеждаемся, что DetailsScreen открывается
    @Test
    fun test_OpenDetailsScreen() {
        //Находим кнопку
        val toDetails: UiObject2 = uiDevice.findObject(
            By.res(
                packageName,
                "toDetailsActivityButton"
            )
        )
        //Кликаем по ней
        toDetails.click()

        //Ожидаем конкретного события: появления текстового поля totalCountTextView.
        //Это будет означать, что DetailsScreen открылся и это поле видно на экране.
        val changedText =
            uiDevice.wait(
                Until.findObject(By.res(packageName, "totalCountTextView")),
                TIMEOUT
            )
        //Убеждаемся, что поле видно и содержит предполагаемый текст.
        //Обратите внимание, что текст должен быть "Number of results: 0",
        //так как мы кликаем по кнопке не отправляя никаких поисковых запросов.
        //Чтобы проверить отображение определенного количества репозиториев,
        //вам в одном и том же методе нужно отправить запрос на сервер и открыть DetailsScreen.
        Assert.assertEquals(changedText.text, "Number of results: 0")
    }

    //Убеждаемся, что после успешного выполнения запроса и получения нужного количества
    // репозиториев, DetailsScreen отображает именно это количество;
    @Test
    fun test_OpenDetailsScreen_RightResult() {
        val editText = uiDevice.findObject(By.res(packageName, "searchEditText"))
        editText.text = "UiAutomator"

        val searchButton: UiObject2 = uiDevice.findObject(By.res(packageName, "searchButton"))
        searchButton.click()

        val changedText = uiDevice.wait(
                Until.findObject(By.res(packageName, "totalCountTextView")),
                TIMEOUT
            )

        val toDetails: UiObject2 = uiDevice.findObject(By.res(packageName, "toDetailsActivityButton"))
        toDetails.click()

        val changedTextOnDetailsScreen = uiDevice.wait(
                Until.findObject(By.res(packageName, "totalCountTextView")),
                TIMEOUT
            )

        Assert.assertEquals(changedTextOnDetailsScreen.text, "Number of results: 672")
    }

    @Test
    fun test_DetailsScreen_ButtonsCheck() {
        val toDetails: UiObject2 = uiDevice.findObject(By.res(packageName, "toDetailsActivityButton"))
        toDetails.click()

        val textOnDetailsScreen = uiDevice.wait(
            Until.findObject(By.res(packageName, "totalCountTextView")),
            TIMEOUT
        )

        Assert.assertEquals(textOnDetailsScreen.text, "Number of results: 0")

        val incrementButton: UiObject2 =
            uiDevice.findObject(By.res(packageName, "incrementButton"))
        incrementButton.click()

        Assert.assertEquals(textOnDetailsScreen.text, "Number of results: 1")

        val decrementButton: UiObject2 =
            uiDevice.findObject(By.res(packageName, "decrementButton"))
        decrementButton.click()

        Assert.assertEquals(textOnDetailsScreen.text, "Number of results: 0")
    }

    companion object {
        private const val TIMEOUT = 5000L
    }
}
