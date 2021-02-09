package tickets

import java.io.File

// https://adventofcode.com/2020/day/16

data class Interval (val start: Int, val end: Int) {}
typealias Parts = MutableMap<String, Pair<Interval, Interval>>

// TODO (optional): for huge inputs one could consider interval tree instead
fun inRangePair (pair: Pair<Interval, Interval>, value: Int): Boolean {
    return value in (pair.first.start..pair.first.end) || value in (pair.second.start..pair.second.end)
}

fun inAnyRangePair (parts: Parts, value: Int) = parts.values.any() {inRangePair(it, value)}

fun columnMatchesRangePair (pair: Pair<Interval, Interval>, values: List<Int>) = values.all() {inRangePair(pair, it)}

fun getAllValidColumnNames (parts: Parts, validLines: List<List<Int>>, columnIndex: Int): List<String> {
    val columnValues = validLines.map {it[columnIndex]}
    val possibleNames = mutableListOf<String>()
    for ((k, v) in parts) {
        if (columnMatchesRangePair(v, columnValues)) possibleNames.add(k)
    }
    return possibleNames
}

data class ParsedInput(val parts: Parts, val yourTicket: List<Int>, val nearbyTickets: List<List<Int>> ) {}

fun parseInput(filePath: String): ParsedInput {
    val parts = mutableMapOf<String, Pair<Interval, Interval>>()
    val nearbyTickets = mutableListOf<List<Int>>()
    var yourTicket = listOf<Int>()

    var readFileState = "parts"
    File(filePath).forEachLine { it ->
        when {
            it.isEmpty() -> { }
            it.contains("your ticket") -> { readFileState = "your-ticket" }
            it.contains("nearby tickets") -> { readFileState = "nearby-tickets" }
            readFileState == "parts" -> {
                val regex = """(.*): (\d+-\d+) or (\d+-\d+)""".toRegex()
                val regexGroup = regex.find(it)?.groupValues
                val name = regexGroup!![1]
                val interval1 = regexGroup!![2]?.split('-').map { it.toInt() }
                val interval2 = regexGroup!![3]?.split('-').map { it.toInt() }
                parts[name] = Pair(Interval(interval1[0], interval1[1]), Interval(interval2[0], interval2[1]))
            }
            readFileState == "your-ticket" -> { yourTicket = it.split(",").map { it.toInt() } }
            readFileState == "nearby-tickets" -> { nearbyTickets.add(it.split(",").map { it.toInt() }) }
        }
    }
    return ParsedInput(parts, yourTicket, nearbyTickets)
}

fun main() {
    val (parts, yourTicket, nearbyTickets ) = parseInput("./src/tickets/input.txt")

    var errorScore = 0
    val validNearbyLines = nearbyTickets.mapNotNull {
        val err = it.fold(0) { acc, n ->
            if (!inAnyRangePair(parts, n)) acc + n else acc
        }
        errorScore += err
        if (err == 0) it else null
    }

    // Part one of the task (22057)
    println("Error score: $errorScore")

    val columnLength = validNearbyLines[0].size
    val validColsMap = mutableMapOf<Int, String>()
    val allValidColsMap = mutableMapOf<Int, List<String>>()

    // init map with all possible configurations
    for (i in 0 until columnLength) {
        allValidColsMap[i] = getAllValidColumnNames(parts, validNearbyLines, i)
    }

    // keep repeating while some configuration is ambiguous
    while (allValidColsMap.values.any {it.size > 1}) {
        for ((k, v) in allValidColsMap) { if (v.size == 1) {validColsMap[k] = v[0] } }

        val finished = validColsMap.values
        for ((k, v) in allValidColsMap) {
            if (v.size > 1) { allValidColsMap[k] = allValidColsMap[k]!!.filter { !finished.contains(it)} }
        }
    }

    val finalResult: Long = validColsMap.entries.fold(1) {acc, mutableEntry ->
        val (key, value) = mutableEntry
        if (value.startsWith("departure")) acc * yourTicket[key] else acc
    }
    // Part two of the task (1093427331937)
    println("Final result $finalResult")
}
