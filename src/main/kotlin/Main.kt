import model.Channel
import model.code.BCHCode
import model.decoder.GPZDecoder
import polynomials.BinaryPolynomial
import javax.swing.SwingUtilities

fun findDifferences(str1: String, str2: String): List<Int> {
    val differences = mutableListOf<Int>()

    for (i in str1.indices) {
        if (i < str2.length && str1[i] != str2[i]) {
            differences.add(i)
        }
    }

    if (str2.length > str1.length) {
        for (i in str1.length until str2.length) {
            differences.add(i)
        }
    }

    return differences
}

fun main() {
    val bchCode = BCHCode(511, 1, 7, BinaryPolynomial(intArrayOf(1, 0, 0, 0, 1, 0, 0, 0, 0, 1)), BinaryPolynomial.X)
//    val bchCode = BCHCode(15, 1, 5, polynomials.BinaryPolynomial(intArrayOf(1, 1, 0, 0, 1)), polynomials.BinaryPolynomial.X)
    val chan = Channel(0.00587, bchCode.length)
    val randCodeword = bchCode.getRandomCodeword()
    println("Отправленное сообщение (кодовое слово): $randCodeword")
    chan.send(randCodeword)
    println("Полученное сообщение: ${chan.receive()}")
    println("Фактические индексы ошибок: ${findDifferences(randCodeword.toString(), chan.receive().toString())}")
    val decodedVector = GPZDecoder(bchCode).decode(chan.receive())
    if (decodedVector == null) {
        println("Декодирование невозможно. Слишком много ошибок (> ${(bchCode.delta - 1) / 2}).")
    } else {
        println("Декодированное сообщение: $decodedVector")
        println("Индексы исправлений: ${findDifferences(decodedVector.toString(), chan.receive().toString())}")
    }
    try {
        SwingUtilities.invokeLater {
            val chart = Chart()
            val randomCode = RandomCode(randomCodeLength)
            println("Проверочная матрица:")
            println(randomCode.parityCheckMatrix)
            println("Порождающая матрица:")
            println(randomCode.generatorMatrix)
            chart.update(randomCode, numOfIterations, channelErrorProbabilityStep)
            val jframe = JFrame()
            jframe.add(chart)
            jframe.setSize(Dimension(500, 500))
            jframe.defaultCloseOperation = EXIT_ON_CLOSE
            jframe.isVisible = true
        }
    } catch (e: Exception) {
        System.err.println(e.message)
    }
}
