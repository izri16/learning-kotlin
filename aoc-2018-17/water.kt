package water

import java.io.File
import java.lang.Integer.MAX_VALUE

typealias WaterMap = MutableMap<Pair<Int, Int>, String>

// https://adventofcode.com/2018/day/17
fun main () {
    // "main" globals to avoid passing a lot of params
    val xOfYMap = mutableMapOf<Int, MutableList<Pair<Int, Int>>>()
    val yOfXMap = mutableMapOf<Int, MutableList<Pair<Int, Int>>>()
    var largestY = -MAX_VALUE
    var lowestY = MAX_VALUE

    // Parse input & fill globals
    File("./src/water/input.txt").forEachLine { it ->
        val regex1 = """x=(\d+), y=(\d+)..(\d+)""".toRegex()
        val regexGroup1 = regex1.find(it)?.groupValues
        if (regexGroup1 != null) {
            val key = regexGroup1[1].toInt()
            if (xOfYMap[key] == null) {xOfYMap[key] = mutableListOf()}
            val min = minOf(regexGroup1[2].toInt(), regexGroup1[3].toInt())
            if (min < lowestY) {lowestY = min}
            val max = maxOf(regexGroup1[2].toInt(), regexGroup1[3].toInt())
            if (max > largestY) {largestY = max}
            xOfYMap[key]?.add(Pair(regexGroup1[2].toInt(), regexGroup1[3].toInt()))
        }
        val regex2 = """y=(\d+), x=(\d+)..(\d+)""".toRegex()
        val regexGroup2 = regex2.find(it)?.groupValues
        if (regexGroup2 != null) {
            val key = regexGroup2[1].toInt()
            if (yOfXMap[key] == null) {yOfXMap[key] = mutableListOf()}
            yOfXMap[key]?.add(Pair(regexGroup2[2].toInt(), regexGroup2[3].toInt()))
            if (key < lowestY) {lowestY = key}
            if (key > largestY) {largestY = key}
        }
    }

    fun isClay(field: Pair<Int, Int>): Boolean {
        val x = field.first
        val y = field.second
        if (x in xOfYMap) {
            val found = xOfYMap[x]?.any { y in (it.first..it.second) } ?: false
            if (found) return true
        }
        if (y in yOfXMap) {
            val found = yOfXMap[y]?.any { x in (it.first..it.second) } ?: false
            if (found) return true
        }
        return false
    }

    fun isWater(waterMap: WaterMap, field: Pair<Int, Int>): Boolean {
        if (field in waterMap) { return waterMap[field] == "water" }
        return false
    }

    fun isEmpty(waterMap: WaterMap, field: Pair<Int, Int>) = !isClay(field) && !isWater(waterMap, field)

    fun waterFlowRec (waterMap: WaterMap, unstable: MutableMap<Pair<Int, Int>, Boolean>, waterPos: Pair<Int, Int>) {
        // prevent flowing down to infinity
        if (waterPos.second > largestY) {
            unstable[waterPos] = true
            return
        }

        waterMap[waterPos] = "visited"

        val bottomField = Pair(waterPos.first, waterPos.second + 1)
        val leftField = Pair(waterPos.first - 1, waterPos.second)
        val rightField = Pair(waterPos.first + 1, waterPos.second)

        // Go down
        if (isEmpty(waterMap, bottomField)) {
            waterFlowRec(waterMap, unstable, bottomField)
            // if below field is unstable, this must also be unstable
            if (bottomField in unstable) { unstable[waterPos] = true }
        }

        // Go left (do not allow going left if already visited)
        if (!isEmpty(waterMap, bottomField) && isEmpty(waterMap, leftField) && (leftField !in waterMap)) {
            waterFlowRec(waterMap, unstable, leftField)
            // if left field is unstable, this must also be unstable
            if (leftField in unstable) { unstable[waterPos] = true }
        }

        // Go right (go to right even if already visited) otherwise we can not know
        // if the field can be water or is unstable
        if (!isEmpty(waterMap, bottomField) && isEmpty(waterMap, rightField)) {
            // if current field is unstable the upcoming right must also be
            if (waterPos in unstable) {
                unstable[rightField] = true
            }
            waterFlowRec(waterMap, unstable, rightField)
            // if right field is unstable, this must also be unstable
            if (rightField in unstable) {
                unstable[waterPos] = true
            }
        }

        // If current field or its neighbours are not unstable we found water
        if (waterPos !in unstable && rightField !in unstable && leftField !in unstable) {
            waterMap[waterPos] = "water"
        }
    }

    fun waterFlow(): WaterMap {
        val waterMap: WaterMap = mutableMapOf()
        val unstableMap: MutableMap<Pair<Int, Int>, Boolean> = mutableMapOf()
        waterFlowRec(waterMap, unstableMap, Pair(500, 0))
        return waterMap.filterKeys { it.second in lowestY..largestY } as WaterMap
    }

    val waterMap = waterFlow()
    // 37073
    println("Part 1: ${waterMap.values.size}")
    // 29289
    println("Part 2: ${ waterMap.filterValues { it == "water" }.size}")
}
