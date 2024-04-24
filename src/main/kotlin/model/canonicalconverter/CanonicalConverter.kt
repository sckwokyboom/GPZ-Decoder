package model.canonicalconverter

import model.canonicalconverter.parser.MatrixParser
import model.canonicalconverter.service.MatrixConverter
import model.canonicalconverter.tester.IsLinearSystemSolutionTester
import model.canonicalconverter.tester.NonDegeneracyTester

class CanonicalConverter {
    fun toGenerative(matrix: String): String {
        return convert(matrix, false)
    }

    fun toParityCheck(matrix: String): String {
        return convert(matrix, true)
    }

    fun toGenerative(matrix: Matrix): Matrix {

        return convert(matrix, false)
    }

    fun toParityCheck(matrix: Matrix): Matrix {
        return convert(matrix, true)
    }

    private fun convert(matrix: String, toParityCheck: Boolean): String {
        val parser = MatrixParser()
        val nonDegeneracyTester = NonDegeneracyTester()
        val isLinearSystemSolutionTester = IsLinearSystemSolutionTester()
        val from = parser.createMatrixFromString(matrix)

        require(nonDegeneracyTester.test(from)) { "Матрица непригодна для преобразования." }
        val to = MatrixConverter.canonical(from)

        if (toParityCheck) {
            require(isLinearSystemSolutionTester.test(to, from)) { "Полученная матрица не является проверочной." }
        } else {
            require(isLinearSystemSolutionTester.test(from, to)) { "Полученная матрица не является порождающей." }
        }

        return parser.createStringFromMatrix(to)
    }

    private fun convert(matrix: Matrix, toParityCheck: Boolean): Matrix {
        val nonDegeneracyTester = NonDegeneracyTester()
        val isLinearSystemSolutionTester = IsLinearSystemSolutionTester()

        require(nonDegeneracyTester.test(matrix)) { "Матрица непригодна для преобразования." }
        val to = MatrixConverter.canonical(matrix)

        if (toParityCheck) {
            require(isLinearSystemSolutionTester.test(to, matrix)) { "Полученная матрица не является проверочной." }
        } else {
            require(isLinearSystemSolutionTester.test(matrix, to)) { "Полученная матрица не является порождающей." }
        }

        return to
    }

    companion object {
        fun isParityCheckMatrix(candidate: Matrix): Boolean {
            val nonDegeneracyTester = NonDegeneracyTester()
            return nonDegeneracyTester.test(candidate)
        }
    }
}