import java.io.File

typealias BagsMap = MutableMap<String, BagConf>

data class InnerBag(val name: String, val count: Int) {}
data class BagConf(val innerBags: List<InnerBag>, var result: Boolean? = null) {}

/*
Algorithm idea:
=> for bag "A" with siblings "B,C,D" store {A: {innerBags: [B,C,D], result: null}} in MAP
=> iterate via map keys and recursively check if the bag at the given key can contain target bag
=> remember previous computations in "result" for all "recursive-invocations" so that each bag
is calculated only once, thus all is calculated in O(input_size)
 */
fun calculateBagColorsCountRec(bagsMap: BagsMap, bag: String, target: String): Boolean {
    val bagMap = bagsMap[bag] as BagConf

    // if the result is already calculated just return it
    if (bagMap.result != null) return bagMap.result as Boolean
    val innerBags = bagMap.innerBags

    for (b in innerBags) {
        // we found the match
        if (b.name == target) {
            bagMap.result = true
            return true
        }
        // find match recursively
        val res = calculateBagColorsCountRec(bagsMap, b.name, target)
        if (res) {
            bagMap.result = true
            return true
        }
    }
    // if we got there it means the match was never found
    bagMap.result = false
    return false
}

fun calculateBagColorsCount(bagsMap: BagsMap, target: String): Int {
    // Note that "bagsMap" is being recursively mutated here
    for ((bag, _) in bagsMap) {
        calculateBagColorsCountRec(bagsMap, bag, target)
    }
    return bagsMap.values.fold(0) {acc, it ->
        if (it.result == true) 1 + acc else acc
    }
}

// Note: memoized version
fun calculateInnerBagCountRec(memo: MutableMap<String, Int>, bagsMap: BagsMap, bag: String): Int {
    val bagMap = bagsMap[bag] as BagConf
    val innerBags = bagMap.innerBags

    var sum = 1
    for (b in innerBags) {
        if (memo[b.name] == null) {
            memo[b.name] = calculateInnerBagCountRec(memo, bagsMap, b.name)
        }
        sum += (memo[b.name]!!.times(b.count))
    }
    return sum
}

fun calculateInnerBagCount(bagsMap: BagsMap, bag: String): Int {
    // -1 is used not to count "target" bag
    return calculateInnerBagCountRec(mutableMapOf(), bagsMap, bag) - 1
}

fun splitBagName(rawName: String) = rawName.trim().split(" ")

fun parseBagName(rawName: String): String {
    val splitted = splitBagName(rawName)
            .filterIndexed { index, _ -> (0..1).contains(index) }
    return splitted.joinToString("-")
}

fun getInnerBag(rawName: String): InnerBag {
    val splitted = splitBagName(rawName)
            .filterIndexed { index, _ -> (0..2).contains(index) }
    val name = splitted.slice(1..2).joinToString("-")
    return InnerBag(name, splitted[0].toInt())
}

fun loadInputToBagsMap(filePath: String): BagsMap {
    val bagsMap = mutableMapOf<String, BagConf>()
    File(filePath).forEachLine {
        val splitted = it.split("contain")
        val currentBag = parseBagName(splitted[0])

        if (splitted[1].contains("no other bags")) {
            bagsMap[currentBag] = BagConf(listOf(), false)
            return@forEachLine
        }
        val innerBags = splitted[1].split(",").map { getInnerBag(it) }
        bagsMap[currentBag] = BagConf(innerBags)
    }
    return bagsMap
}

// https://adventofcode.com/2020/day/7
fun main () {
    val bagsMap = loadInputToBagsMap("./src/input.txt")
    // 229: is the correct result
    println("Result 1: ${calculateBagColorsCount(bagsMap, "shiny-gold")}")
    // 6683: is the correct result
    println("Result 2: ${calculateInnerBagCount(bagsMap, "shiny-gold")}")
}
