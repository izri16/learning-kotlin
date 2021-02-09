import java.io.File

typealias BagsMap = MutableMap<String, BagConf>

data class BagConf(val innerBags: List<String>, var result: Boolean? = null) {}

/*
Algorithm idea:
=> counts of bags does not matter
=> for bag "A" with siblings "B,C,D" store {A: {innerBags: [B,C,D], result: null}} in MAP
=> iterate via map keys and recursively check if the bag at the given key can contain target bag
=> remember previous computations in "result" for all "recursive-invocations" so that each bag
is calculated only once, thus all is calculated in O(input_size)
 */
fun calculateBagCountRec(bagsMap: BagsMap, bag: String, target: String): Boolean {
    val bagMap = bagsMap[bag] as BagConf

    // if the result is already calculated just return it
    if (bagMap.result != null) return bagMap.result as Boolean
    val innerBags = bagMap.innerBags

    for (b in innerBags) {
        // we found the match
        if (b == target) {
            bagMap.result = true
            return true
        }
        // find match recursively
        val res = calculateBagCountRec(bagsMap, b, target)
        if (res) {
            bagMap.result = true
            return true
        }
    }
    // if we got there it means the match was never found
    bagMap.result = false
    return false
}

fun calculateBagCount(bagsMap: BagsMap, target: String): Int {
    // Note that "bagsMap" is being recursively mutated here
    for ((bag, _) in bagsMap) {
        calculateBagCountRec(bagsMap, bag, target)
    }
    return bagsMap.values.fold(0) {acc, it ->
        if (it.result == true) 1 + acc else acc
    }
}

fun parseBagName(rawName: String, nameIndexes: Array<Int>): String {
    val filtered = rawName
            .trim()
            .split(" ")
            .filterIndexed { index, _ -> nameIndexes.contains(index) }
    return filtered.joinToString("-")
}

fun loadInputToBagsMap(filePath: String): BagsMap {
    val bagsMap = mutableMapOf<String, BagConf>()
    File(filePath).forEachLine {
        val splitted = it.split("contain")
        val currentBag = parseBagName(splitted[0], arrayOf(0, 1))

        if (splitted[1].contains("no other bags")) {
            bagsMap[currentBag] = BagConf(listOf(), false)
            return@forEachLine
        }
        val innerBags = splitted[1].split(",").map { it -> parseBagName(it, arrayOf(1, 2)) }
        bagsMap[currentBag] = BagConf(innerBags)
    }
    return bagsMap
}

// https://adventofcode.com/2020/day/7
// 229: is the correct result
fun main () {
    val bagsMap = loadInputToBagsMap("./src/input.txt")
    println("Result: ${calculateBagCount(bagsMap, "shiny-gold")}")
}
