package com.cebolao.lotofacil.data

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class HistoryParserTest {

    @Test
    fun `parse valid line standard format`() {
        val line = "3455 - 01,02,03,05,06,09,10,13,15,17,19,21,22,23,25"
        val parsed = HistoryParser.parseLine(line)
        assertNotNull(parsed)
    }

    @Test
    fun `parse valid line without leading zeros`() {
        val line = "123 - 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15"
        val parsed = HistoryParser.parseLine(line)
        assertNotNull(parsed)
    }

    @Test
    fun `parse valid line with extra spaces`() {
        val line = "  200 -  01 , 02 ,03, 04,05,06,07,08,09,10,11,12,13,14,15  "
        val parsed = HistoryParser.parseLine(line)
        assertNotNull(parsed)
    }

    @Test
    fun `parse invalid line malformed`() {
        val line = "not a valid line"
        val parsed = HistoryParser.parseLine(line)
        assertNull(parsed)
    }

    @Test
    fun `parse invalid line wrong count`() {
        val line = "400 - 01,02,03" // too few numbers
        val parsed = HistoryParser.parseLine(line)
        assertNull(parsed)
    }

    @Test
    fun `parse invalid line out of range numbers`() {
        val line = "500 - 01,02,03,04,05,06,07,08,09,10,11,12,13,14,30" // 30 out of 1..25
        val parsed = HistoryParser.parseLine(line)
        assertNull(parsed)
    }
}