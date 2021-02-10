package expressions

import java.io.File

fun exprsLeftRightPrioritySum(exprs: List<String>): Long {
    return exprs.foldIndexed(0) {index, acc, exp ->
        if (index == 0) return@foldIndexed exp.toLong()
        if (exp != "*" && exp != "+") {
            val operator = exprs[index - 1]
            val current = exp.toLong()
            return@foldIndexed if (operator == "+") acc + current else acc * current
        }
        acc
    }
}

fun exprsInversePrioritySum(exprs: List<String>): Long {
    val partialExprsList = mutableListOf<String>()

    for (e in exprs) {
        if (e == "+" || e == "*") {
            partialExprsList.add(e)
        } else {
            val last = partialExprsList.lastOrNull()
            if (last == "+") {
                partialExprsList.removeLast() // remove "+"
                partialExprsList.add((partialExprsList.removeLast().toLong() + e.toLong()).toString())
            } else {
                partialExprsList.add(e)
            }
        }
    }
    return exprsLeftRightPrioritySum(partialExprsList)
}

fun evalExp (exprString: String, exprsSummer: (List<String>) -> Long): Long {
    var bracketsCount = 0
    var partialNumber = ""
    var innerExpression = ""
    val expList = mutableListOf<String>()

    for (c in exprString) {
        if (bracketsCount == 0 && (c == '*' || c == '+')) {
            if (partialNumber != "") {
                expList.add(partialNumber)
                partialNumber = ""
            }
            expList.add(c.toString())
        } else if (bracketsCount == 0 && c.isDigit()) {
            partialNumber += c.toString()
        } else if (c == '(') {
            if (bracketsCount > 0) {
                innerExpression += "("
            }
            bracketsCount += 1
        } else if (c == ')') {
            bracketsCount -= 1
            if (bracketsCount == 0) {
                expList.add(evalExp(innerExpression, exprsSummer).toString())
                innerExpression = ""
            } else {
                innerExpression += ")"
            }
        } else {
            // characters inside brackets
            val trimmed = c.toString().trim()
            if (trimmed.isNotEmpty()) {
                innerExpression += trimmed
            }
        }
    }

    if (partialNumber != "") { expList.add(partialNumber) }
    return exprsSummer(expList)
}

// https://adventofcode.com/2020/day/18
fun main() {
    var sum1: Long = 0
    var sum2: Long = 0
    File("./src/expressions/input.txt").forEachLine { it ->
        sum1 += evalExp(it) {expList -> exprsLeftRightPrioritySum(expList)}
        sum2 += evalExp(it) {expList -> exprsInversePrioritySum(expList)}
    }
    // part1: 53660285675207
    println("SUM: $sum1")
    // part2: 141993988282687
    println("SUM2: $sum2")
}
