package tiles

import java.io.File

enum class Direction {
    se,
    sw,
    w,
    nw,
    ne,
    e,
}

object DirectionMover {
    fun se(pos: Pair<Int, Int>) = Pair(pos.first + 1, pos.second - 1)
    fun sw(pos: Pair<Int, Int>): Pair<Int, Int> = Pair(pos.first - 1, pos.second - 1)
    fun w(pos: Pair<Int, Int>): Pair<Int, Int> = Pair(pos.first - 2, pos.second)
    fun nw(pos: Pair<Int, Int>): Pair<Int, Int> = Pair(pos.first - 1, pos.second + 1)
    fun ne(pos: Pair<Int, Int>): Pair<Int, Int> = Pair(pos.first + 1, pos.second + 1)
    fun e(pos: Pair<Int, Int>): Pair<Int, Int> = Pair(pos.first + 2, pos.second)
}

fun isDirection(value: String): Boolean {
    return enumValues<Direction>().any{it -> it.name == value }
}

const val BLACK = "black"
const val WHITE = "white"

fun parseLine(line: String): List<Direction> {
    val parsed = mutableListOf<Direction>()
    var prevC = ""
    for (c in line) {
        val v = prevC + c.toString()
        if (isDirection(v)) {
            prevC = ""
            parsed.add(Direction.valueOf(v))
        } else {
            prevC = c.toString()
        }
    }
    return parsed
}

typealias TileConf = MutableMap<Pair<Int, Int>, String>

fun flip (tileConf: TileConf, pos: Pair<Int, Int>) {
    if (pos in tileConf) {
        tileConf[pos] = if (tileConf[pos] == WHITE) BLACK else WHITE
    } else {
        tileConf[pos] = BLACK
    }
    // neighbours
    tileConf.putIfAbsent(DirectionMover.se(pos), WHITE)
    tileConf.putIfAbsent(DirectionMover.sw(pos), WHITE)
    tileConf.putIfAbsent(DirectionMover.w(pos), WHITE)
    tileConf.putIfAbsent(DirectionMover.nw(pos), WHITE)
    tileConf.putIfAbsent(DirectionMover.ne(pos), WHITE)
    tileConf.putIfAbsent(DirectionMover.e(pos), WHITE)
}

fun flipByDirections (tileConf: TileConf, dl: List<Direction>, pos: Pair<Int, Int>, index: Int) {
    if (index == dl.size) {
        flip(tileConf, pos)
    } else {
        val direction = dl[index]
        if (direction == Direction.se) { flipByDirections(tileConf, dl, DirectionMover.se(pos), index + 1)}
        if (direction == Direction.sw) { flipByDirections(tileConf, dl, DirectionMover.sw(pos), index + 1)}
        if (direction == Direction.w) { flipByDirections(tileConf, dl, DirectionMover.w(pos), index + 1)}
        if (direction == Direction.nw) { flipByDirections(tileConf, dl, DirectionMover.nw(pos), index + 1)}
        if (direction == Direction.ne) { flipByDirections(tileConf, dl, DirectionMover.ne(pos), index + 1)}
        if (direction == Direction.e) { flipByDirections(tileConf, dl, DirectionMover.e(pos), index + 1)}
    }
}

fun blackAdjacentCount (tileConf: TileConf, pos: Pair<Int, Int>): Int {
    var count = 0
    if (tileConf[DirectionMover.se(pos)] == BLACK) {count += 1}
    if (tileConf[DirectionMover.sw(pos)] == BLACK) {count += 1}
    if (tileConf[DirectionMover.w(pos)] == BLACK) {count += 1}
    if (tileConf[DirectionMover.nw(pos)] == BLACK) {count += 1}
    if (tileConf[DirectionMover.ne(pos)] == BLACK) {count += 1}
    if (tileConf[DirectionMover.e(pos)] == BLACK) {count += 1}
    return count
}

fun countBlackTiles (tileConf: TileConf): Int {
    return tileConf.values.fold(0) { acc, v ->
        if (v == BLACK) acc + 1 else acc
    }
}

fun gameOfLife (tileConf: TileConf, rounds: Int): Int {
    var resultConf = tileConf.toMutableMap()
    for (i in 1..rounds) {
        val newTileConf = resultConf.toMutableMap()
        for ((k, v) in resultConf) {
            val adjCount = blackAdjacentCount(resultConf, k)
            if (v == BLACK) {
                if (adjCount == 0 || adjCount > 2) { flip(newTileConf, k) }
            } else {
                if (adjCount == 2) { flip(newTileConf, k)}
            }
        }
        resultConf = newTileConf
    }
    return countBlackTiles(resultConf)
}

// https://adventofcode.com/2020/day/24
fun main() {
    val tileConf = mutableMapOf<Pair<Int, Int>, String>()
    File("./src/tiles/input.txt").forEachLine { it ->
        flipByDirections(tileConf, parseLine(it), Pair(0, 0),0)
    }
    // Part 1: 495
    println(countBlackTiles(tileConf))
    // Part 2: 4012
    println(gameOfLife(tileConf, 100))
}
