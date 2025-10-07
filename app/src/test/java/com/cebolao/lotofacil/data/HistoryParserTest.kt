package com.cebolao.lotofacil.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class HistoryParserTest {

    @Test
    fun `parse valid line standard format`() {
        val line = "3455 - 01,02,03,05,06,09,10,13,15,17,19,21,22,23,25"
        val parsed = HistoryParser.parseLine(line)
        assertNotNull(parsed)
        assertEquals(15, parsed?.numbers?.size)
        assertEquals(3455, parsed?.contestNumber)
    }

    @Test
    fun `parse valid line without leading zeros`() {
        val line = "123 - 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15"
        val parsed = HistoryParser.parseLine(line)
        assertNotNull(parsed)
        assertEquals(15, parsed?.numbers?.size)
    }

    @Test
    fun `parse valid line with extra spaces`() {
        val line = "  200 -  01 , 02 ,03, 04,05,06,07,08,09,10,11,12,13,14,15  "
        val parsed = HistoryParser.parseLine(line)
        assertNotNull(parsed)
        assertEquals(15, parsed?.numbers?.size)
    }

    @Test
    fun `parse invalid line malformed`() {
        val line = "not a valid line"
        val parsed = HistoryParser.parseLine(line)
        assertNull(parsed)
    }

    @Test
    fun `parse invalid line with too few numbers`() {
        // 14 números
        val line = "400 - 01,02,03,04,05,06,07,08,09,10,11,12,13,14"
        val parsed = HistoryParser.parseLine(line)
        assertNull(parsed)
    }

    @Test
    fun `parse invalid line with too many numbers`() {
        // 16 números
        val line = "401 - 01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16"
        val parsed = HistoryParser.parseLine(line)
        assertNull(parsed)
    }

    @Test
    fun `parse invalid line with wrong count due to out of range number`() {
        // A linha tem 15 dezenas, mas uma delas ('30') não é válida. O parser deve rejeitar a linha.
        val line = "500 - 01,02,03,04,05,06,07,08,09,10,11,12,13,14,30"
        val parsed = HistoryParser.parseLine(line)
        assertNull(parsed)
    }

    @Test
    fun `parse invalid line with too many numbers even if one is out of range`() {
        // A linha tem 16 dezenas. A lógica deve falhar na checagem inicial de contagem, antes de analisar os números.
        val line = "501 - 01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,30"
        val parsed = HistoryParser.parseLine(line)
        assertNull(parsed)
    }

    @Test
    fun `parse invalid line with negative contest number`() {
        val line = "-1 - 01,02,03,04,05,06,07,08,09,10,11,12,13,14,15"
        val parsed = HistoryParser.parseLine(line)
        assertNull(parsed)
    }

    @Test
    fun `parse invalid line with zero contest number`() {
        val line = "0 - 01,02,03,04,05,06,07,08,09,10,11,12,13,14,15"
        val parsed = HistoryParser.parseLine(line)
        assertNull(parsed)
    }
}