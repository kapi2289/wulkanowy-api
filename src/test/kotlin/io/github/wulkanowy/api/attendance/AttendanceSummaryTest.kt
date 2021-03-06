package io.github.wulkanowy.api.attendance

import io.github.wulkanowy.api.BaseLocalTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.Month

class AttendanceSummaryTest : BaseLocalTest() {

    private val table by lazy {
        getSnpRepo(AttendanceSummaryTest::class.java, "Frekwencja.html").getAttendanceSummary(-1).blockingGet()
    }

    private val subjects by lazy {
        getSnpRepo(AttendanceSummaryTest::class.java, "Frekwencja.html").getSubjects().blockingGet()
    }

    @Test
    fun getAttendanceSummary() {
        assertEquals(10, table.size)
    }

    @Test
    fun getSubjects() {
        assertEquals(23, subjects.size)
        assertEquals("Wszystkie", subjects[0].name)
        assertEquals(-1, subjects[0].value)
    }

    @Test
    fun getAttendanceSummary_month() {
        assertEquals(Month.SEPTEMBER, table[0].month)
        assertEquals(Month.OCTOBER, table[1].month)
        assertEquals(Month.NOVEMBER, table[2].month)
        assertEquals(Month.JUNE, table[9].month)
    }

    @Test
    fun getAttendanceSummary_presence() {
        assertEquals(135, table[0].presence)
        assertEquals(103, table[1].presence)
        assertEquals(108, table[2].presence)
        assertEquals(54, table[3].presence)
        assertEquals(37, table[4].presence)
        assertEquals(100, table[5].presence)
        assertEquals(33, table[6].presence)
        assertEquals(90, table[7].presence)
        assertEquals(103, table[8].presence)
        assertEquals(59, table[9].presence)
    }

    @Test
    fun getAttendanceSummary_absence() {
        assertEquals(0, table[0].absence)
        assertEquals(2, table[5].absence)
        assertEquals(4, table[9].absence)
    }

    @Test
    fun getAttendanceSummary_exemption() {
        assertEquals(0, table[0].exemption)
        assertEquals(1, table[1].exemption)
        assertEquals(0, table[9].exemption)
    }
}
