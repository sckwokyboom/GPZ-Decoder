import model.Channel
import model.GPZDecoder
import model.code.BCHCode

fun findDifferences(str1: String, str2: String): List<Int> {
    val differences = mutableListOf<Int>()

    for (i in str1.indices) {
        if (i < str2.length && str1[i] != str2[i]) {
            differences.add(i)
        }
    }

    // Добавляем остаток второй строки, если она длиннее первой
    if (str2.length > str1.length) {
        for (i in str1.length until str2.length) {
            differences.add(i)
        }
    }

    return differences
}

fun main() {
    val bchCode = BCHCode(511)
    println(bchCode.generatorMatrix)
    val chan = Channel(0.00587, 511)
    val randCodeword = bchCode.getRandomCodeword()
    println(randCodeword)
    chan.send(randCodeword)
    println(findDifferences(randCodeword.toString(), chan.receive().toString()))
    println(chan.receive())
    println(GPZDecoder().decode(chan.receive()))
//    val test = MultiBinaryPolynomial(
//        listOf(
//            BinaryPolynomial(intArrayOf(1)),
//            BinaryPolynomial(intArrayOf(1)),
//            BinaryPolynomial(intArrayOf(1, 1)),
//        )
//    )
//    println(test)
//    println(test.substituteArgument(BinaryPolynomial(intArrayOf(1, 1, 1))))
}
