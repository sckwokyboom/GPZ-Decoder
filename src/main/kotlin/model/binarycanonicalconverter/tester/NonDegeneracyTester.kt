package model.binarycanonicalconverter.tester

import model.binarycanonicalconverter.BinaryMatrix
import mu.KotlinLogging

class NonDegeneracyBinaryTester : BinaryMatrixTester {
    private val logger = KotlinLogging.logger {}

    override fun test(vararg matrices: BinaryMatrix) = matrices.all {
        if (containsZero(it)) {
            logger.trace { "Матрица вырожденная, так как содержит нулевую строку." }
            return@all false
        }
        if (it.isSquare() && !it.determinant()) {
            logger.trace { "матрица вырожденная, так как ее детерминант равен 0" }
            return@all false
        }

        if (it.rank() < minOf(it.cols, it.rows)) {
            logger.trace { "матрица вырожденная, так как ее ранг меньше ее порядка" }
            return@all false
        }

        logger.trace { "матрица невырожденная" }
        return@all true
    }

    private fun containsZero(matrix: BinaryMatrix): Boolean {
        for (i in 0..<matrix.rows) {
            val row = matrix.row(i)
            val isZero = row.all { !it }
            if (isZero) {
                return true
            }
        }

        return false
    }
}