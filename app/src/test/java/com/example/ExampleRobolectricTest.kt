package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.TertemizStaticData
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `app name matches Tertemiz Yarinlar`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Tertemiz Yarınlar", appName)
    }

    @Test
    fun `project team list contains all eight members`() {
        assertEquals(8, TertemizStaticData.projectTeam.size)
        val names = TertemizStaticData.projectTeam.map { it.name }
        assert(names.contains("Merve Arslan"))
        assert(names.contains("Yakub Çadırcı"))
        assert(names.contains("Mustafa Adıgüzel"))
        assert(names.contains("Yusuf Bozlak"))
        assert(names.contains("Ecrin Ekici"))
        assert(names.contains("Kaan Talha Doğan"))
        assert(names.contains("Ömer Kaan Köksal"))
        assert(names.contains("Seyid Talha Elmalı"))
    }

    @Test
    fun `digital badges include required goals`() {
        assertEquals(8, TertemizStaticData.digitalBadges.size)
        val titles = TertemizStaticData.digitalBadges.map { it.title }
        assert(titles.contains("1 Hafta Su Tasarrufu"))
        assert(titles.contains("5 Hijyen Bildirimi"))
        assert(titles.contains("Sıfır Atık Elçisi"))
    }

    @Test
    fun `bottom navigation tabs switch screens cleanly`() {
        composeRule.onNodeWithTag("home_quick_report_button").assertIsDisplayed()
        composeRule.onNodeWithTag("home_start_handwash_button").assertIsDisplayed()

        composeRule.onNodeWithTag("nav_tab_report").performClick()
        composeRule.onNodeWithText("Hijyen & Alan Bildirimi").assertIsDisplayed()
        composeRule.onNodeWithTag("save_report_button").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("nav_tab_timer").performClick()
        composeRule.onNodeWithText("El Yıkama & Tasarruf").assertIsDisplayed()
        composeRule.onNodeWithTag("timer_start_button").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("nav_tab_badges").performClick()
        composeRule.onNodeWithText("Profil & Dijital Rozetler").assertIsDisplayed()
        composeRule.onNodeWithTag("profile_rank_card").assertIsDisplayed()

        composeRule.onNodeWithTag("nav_tab_guide").performClick()
        composeRule.onNodeWithText("Rehber & Proje Ekibi").assertIsDisplayed()
    }
}
