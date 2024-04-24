package model.binarycanonicalconverter

import model.binarycanonicalconverter.parser.BinaryMatrixParser
import model.binarycanonicalconverter.service.BinaryMatrixConverter
import model.binarycanonicalconverter.tester.IsLinearSystemSolutionBinaryTester
import model.binarycanonicalconverter.tester.NonDegeneracyBinaryTester

class BinaryCanonicalConverter {
    fun toGenerative(matrix: String): String {
        return convert(matrix, false)
    }

    fun toParityCheck(matrix: String): String {
        return convert(matrix, true)
    }

    fun toGenerative(matrix: BinaryMatrix): BinaryMatrix {

        return convert(matrix, false)
    }

    fun toParityCheck(matrix: BinaryMatrix): BinaryMatrix {
        return convert(matrix, true)
    }

    private fun convert(matrix: String, toParityCheck: Boolean): String {
        val parser = BinaryMatrixParser()
        val nonDegeneracyTester = NonDegeneracyBinaryTester()
        val isLinearSystemSolutionTester = IsLinearSystemSolutionBinaryTester()
        val from = parser.createMatrixFromString(matrix)

        require(nonDegeneracyTester.test(from)) { "Матрица непригодна для преобразования." }
        val to = BinaryMatrixConverter.canonical(from)

        if (toParityCheck) {
            require(isLinearSystemSolutionTester.test(to, from)) { "Полученная матрица не является проверочной." }
        } else {
            require(isLinearSystemSolutionTester.test(from, to)) { "Полученная матрица не является порождающей." }
        }

        return parser.createStringFromMatrix(to)
    }

    private fun convert(matrix: BinaryMatrix, toParityCheck: Boolean): BinaryMatrix {
        val nonDegeneracyTester = NonDegeneracyBinaryTester()
        val isLinearSystemSolutionTester = IsLinearSystemSolutionBinaryTester()

        require(nonDegeneracyTester.test(matrix)) { "Матрица непригодна для преобразования." }
        val to = BinaryMatrixConverter.canonical(matrix)

        if (toParityCheck) {
            require(isLinearSystemSolutionTester.test(to, matrix)) { "Полученная матрица не является проверочной." }
        } else {
            require(isLinearSystemSolutionTester.test(matrix, to)) { "Полученная матрица не является порождающей." }
        }

        return to
    }

    companion object {
        fun isParityCheckMatrix(candidate: BinaryMatrix): Boolean {
            val nonDegeneracyTester = NonDegeneracyBinaryTester()
            return nonDegeneracyTester.test(candidate)
        }
    }
}