package model.canonicalconverter.service

import model.canonicalconverter.Matrix
import mu.KotlinLogging

object MatrixConverter {
    private val logger = KotlinLogging.logger {}

    fun canonical(matrix: Matrix): Matrix {
        logger.trace { "исходная матрица\n$matrix" }

        val (rrefMatrix, pivotColumns) = matrix.toRowEchelonFormMod2()
        logger.trace { "ступенчатый вид\n$rrefMatrix" }
        logger.trace { "индексы ведущих столбцов\n$pivotColumns" }

        val rearrangedMatrix = Matrix(matrix.rows, matrix.cols)
        val columnOrder = pivotColumns + (0 until matrix.cols).filterNot { pivotColumns.contains(it) }
        columnOrder.forEachIndexed { index, colIndex ->
            if (index < matrix.rows) {
                // Перестановка столбцов для ведущих столбцов
                for (row in 0 until matrix.rows) {
                    rearrangedMatrix[row, index] = rrefMatrix[row, colIndex]
                }
            } else {
                // Заполнение оставшихся столбцов
                for (row in 0 until matrix.rows) {
                    rearrangedMatrix[row, index] = rrefMatrix[row, colIndex]
                }
            }
        }

        logger.trace { "перестановка столбцов согласно pivot columns\n$rearrangedMatrix" }
        // Транспонирование подматрицы A и объединение с единичной матрицей
        val aMatrix = rearrangedMatrix.subMatrix(0, matrix.rows, matrix.rows, matrix.cols)
        val aTransposed = aMatrix.transpose()

        logger.trace { "подматрица A после перестановки и транспонирования\n$aTransposed" }
        val eyeMatrix = Matrix.eye(aTransposed.rows)
        val generativeMatrix = Matrix.hstack(aTransposed, eyeMatrix)
        logger.trace { "порождающая матрица для проверочной с примененной перестановкой\n$generativeMatrix" }

        // Восстановление исходной матрицы G
        val restoredG = Matrix(generativeMatrix.rows, generativeMatrix.cols)
        pivotColumns.forEachIndexed { index, pivot ->
            for (row in 0 until generativeMatrix.rows) {
                restoredG[row, pivot] = generativeMatrix[row, index]
            }
        }
        (0 until generativeMatrix.cols).filterNot { pivotColumns.contains(it) }.forEachIndexed { index, col ->
            for (row in 0 until generativeMatrix.rows) {
                restoredG[row, col] = generativeMatrix[row, index + pivotColumns.size]
            }
        }
        logger.trace { "восстановленная исходная матрица\n$restoredG" }
        val restoredGModule = restoredG % 2
        logger.trace { "восстановленная исходная матрица с примененным модулем\n$restoredGModule" }
        return restoredGModule
    }
}