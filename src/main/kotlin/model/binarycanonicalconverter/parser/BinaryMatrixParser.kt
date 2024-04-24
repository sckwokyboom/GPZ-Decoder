package model.binarycanonicalconverter.parser

import model.binarycanonicalconverter.BinaryMatrix
import mu.KotlinLogging

class BinaryMatrixParser {
    private val logger = KotlinLogging.logger {}

    fun createMatrixFromString(matrixString: String): BinaryMatrix {
        logger.trace { "входящая строка\n$matrixString" }

        val rows = matrixString.trim().removeSurrounding("[[", "]]").split("],[", "], [", "],\n[")
        val numRows = rows.size
        val numCols = rows.first().split(",").size
        val matrix = BinaryMatrix(numRows, numCols)
        logger.trace { "параметры матрицы\nколичество строк: $numRows\nколичество столбцов: $numCols" }

        for ((i, row) in rows.withIndex()) {
            val elements = row.split(",").map { it.trim().toBoolean() }

            for ((j, elem) in elements.withIndex()) {
                matrix[i, j] = elem
            }
        }

        logger.trace { "восстановленная из входящей строки матрица\n$matrixString" }
        return matrix
    }

    fun createStringFromMatrix(matrix: BinaryMatrix): String {
        val numRows = matrix.rows
        val numCols = matrix.cols
        val stringBuilder = StringBuilder()

        stringBuilder.append("[")
        for (i in 0 until numRows) {
            stringBuilder.append("[")
            for (j in 0 until numCols) {
                stringBuilder.append(matrix[i, j])
                if (j < numCols - 1) {
                    stringBuilder.append(", ")
                }
            }
            stringBuilder.append("]")
            if (i < numRows - 1) {
                stringBuilder.append(",\n")
            }
        }
        stringBuilder.append("]")

        return stringBuilder.toString()
    }

}