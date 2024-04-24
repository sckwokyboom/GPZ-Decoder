package model.binarycanonicalconverter

import model.BinaryVector

class BinaryMatrix {
    var cols: Int
    val rows: Int
    private var data: Array<Array<Boolean>>

    constructor(rows: Int, cols: Int) : this(
        Array(rows) { Array(cols) { false } }
    )

    constructor(vector: Array<Boolean>) {
        this.rows = 1
        this.cols = vector.size
        require(rows > 0 && cols > 0) { "Количество строк и столбцов должно быть положительным целым числом." }
        this.data = arrayOf(vector)
    }

    constructor(data: Array<Array<Boolean>>) {
        this.rows = data.size
        this.cols = data[0].size
        require(rows > 0 && cols > 0) { "Количество строк и столбцов должно быть положительным целым числом." }
        this.data = data
    }

    companion object {
        fun eye(size: Int): BinaryMatrix {
            val result = BinaryMatrix(size, size)
            for (i in 0 until size) {
                result[i, i] = true
            }
            return result
        }

        fun hstack(a: BinaryMatrix, b: BinaryMatrix): BinaryMatrix {
            if (a.rows != b.rows) throw IllegalArgumentException("Matrices don't have the same number of rows.")
            val result = BinaryMatrix(a.rows, a.cols + b.cols)
            for (i in 0 until a.rows) {
                for (j in 0 until a.cols) {
                    result[i, j] = a[i, j]
                }
                for (j in 0 until b.cols) {
                    result[i, j + a.cols] = b[i, j]
                }
            }
            return result
        }
    }

    fun gaussianElimination(): BinaryMatrix {
        val A = this.copy()
        var lead = 0
        for (r in 0 until A.rows) {
            if (A.cols <= lead) break
            var i = r
            while (A.data[i][lead] == false) {
                i++
                if (A.rows == i) {
                    i = r
                    lead++
                    if (A.cols == lead) return A
                }
            }

            val temp = A.data[r]
            A.data[r] = A.data[i]
            A.data[i] = temp


            for (k in 0 until A.rows) {
                if (k != r) {
                    val mult = A.data[k][lead]
                    for (j in 0 until A.cols) {
                        A.data[k][j] = A.data[k][j] xor (A.data[r][j] and mult)
                    }
                }
            }
            lead++
        }
        return A
    }

    fun rank(): Int {
        val ref = this.toRowEchelonFormMod2().first
        var rank = 0
        for (i in 0 until ref.rows) {
            for (j in 0 until ref.cols) {
                if (ref.data[i][j]) {
                    rank++
                    break
                }
            }
        }
        return rank
    }

    fun subMatrix(startRow: Int, endRow: Int, startCol: Int, endCol: Int): BinaryMatrix {
        val subRows = endRow - startRow
        val subCols = endCol - startCol
        val result = BinaryMatrix(subRows, subCols)
        for (i in 0 until subRows) {
            for (j in 0 until subCols) {
                result[i, j] = this[startRow + i, startCol + j]
            }
        }
        return result
    }

    fun determinant(): Boolean {
        require(isSquare()) { "определитель можно вычислить только для квадратной матрицы" }
        if (rows == 1) return this[0, 0]
        if (rows == 2) return (this[0, 0] and this[1, 1]) xor (this[1, 0] and this[0, 1])

        var det = false
        for (col in 0 until cols) {
            det = det xor (this[0, col] and cofactor(0, col))
        }
        return det
    }

    fun isSquare() = cols == rows

    private fun cofactor(row: Int, col: Int): Boolean {
        return minor(row, col).determinant() and if ((row + col) % 2 == 0) true else true
    }

    private fun minor(row: Int, col: Int): BinaryMatrix {
        val result = BinaryMatrix(rows - 1, cols - 1)
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                if (i == row || j == col) continue
                result[if (i < row) i else i - 1, if (j < col) j else j - 1] = this[i, j]
            }
        }
        return result
    }


    /**
     * Приведение матрицы к ступенчатому виду в кольце по модулю 2.
     */
    fun toRowEchelonFormMod2(): Pair<BinaryMatrix, List<Int>> {
        val matrix = copy()
        val pivotColumns = mutableListOf<Int>()
        var lead = 0

        for (r in 0 until rows) {
            if (lead >= cols) break
            var i = r
            while (!matrix[i, lead]) {
                i++
                if (i == rows) {
                    i = r
                    lead++
                    if (lead == cols) {
                        return Pair(matrix, pivotColumns)
                    }
                }
            }

            // Перестановка строк
            for (k in 0 until cols) {
                val temp = matrix[r, k]
                matrix[r, k] = matrix[i, k]
                matrix[i, k] = temp
            }
            pivotColumns.add(lead)

            // Приведение всех элементов в столбце к 0, кроме ведущего элемента
            for (j in 0 until rows) {
                if (j != r && matrix[j, lead]) {
                    for (k in 0 until cols) {
                        matrix[j, k] = (matrix[j, k] xor matrix[r, k])
                    }
                }
            }
            lead++
        }

        return Pair(matrix, pivotColumns)
    }

    /**
     * Создает и возвращает глубокую копию этой матрицы.
     */
    fun copy(): BinaryMatrix {
        val newBinaryMatrix = BinaryMatrix(rows, cols)
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                newBinaryMatrix[i, j] = this[i, j]
            }
        }
        return newBinaryMatrix
    }

    operator fun get(row: Int, col: Int): Boolean {
        if (row !in 0 until rows || col !in 0 until cols) {
            throw IndexOutOfBoundsException("Обращение за пределы матрицы.")
        }
        return data[row][col]
    }

    fun row(index: Int): Array<Boolean> {
        require(index in 0..<rows) { "Обращение за пределы матрицы. Получено: $index, это число не принадлежит интервалу [0, $rows)." }
        return data[index]
    }

    fun column(index: Int): Array<Boolean> {
        require(index in 0..<cols) { "Обращение за пределы матрицы. Получено: $index, это число не принадлежит интервалу [0, $cols)." }
        val column = mutableListOf<Boolean>()
        data.forEach { column.add(it[index]) }
        return column.toTypedArray()
    }

    operator fun set(row: Int, col: Int, value: Boolean) {
        if (row !in 0 until rows || col !in 0 until cols) {
            throw IndexOutOfBoundsException("Запись за пределы матрицы.")
        }
        data[row][col] = value
    }

    private fun plus(left: BinaryMatrix, right: BinaryMatrix): BinaryMatrix {
        val result = BinaryMatrix(rows, cols)
        if (left.rows != right.rows || left.cols != right.cols) {
            throw IllegalArgumentException("размеры матриц должны совпадать для операции сложения")
        }

        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result[i, j] = left[i, j] xor right[i, j]
            }
        }
        return result
    }

    fun transpose(): BinaryMatrix {
        val result = BinaryMatrix(cols, rows)
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result[j, i] = this[i, j]
            }
        }
        return result
    }

    operator fun plus(other: BinaryMatrix): BinaryMatrix {
        return plus(this, other)
    }

    operator fun plusAssign(other: BinaryMatrix) {
        this.data = plus(this, other).data
    }

    operator fun times(constant: Boolean): BinaryMatrix {
        val result = BinaryMatrix(rows, cols)
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result[i, j] = this[i, j] and constant
            }
        }
        return result
    }

    operator fun times(other: BinaryMatrix): BinaryMatrix {
        if (cols != other.rows) {
            throw IllegalArgumentException(
                "Количество колонок первой матрицы должно совпадать с количеством строк второй при умножении"
            )
        }
        val n = rows
        val p = other.cols
        val m = cols
        val result = BinaryMatrix(n, p)
        for (i in 0 until n) {
            for (j in 0 until p) {
                var sum = false
                for (k in 0 until m) {
                    sum = sum xor (this[i, k] and other[k, j])
                }
                result[i, j] = sum
            }
        }
        return result
    }

    fun isTransformableTo(other: BinaryMatrix): Boolean {
        if (this.rows != other.rows || this.cols != other.cols) {
            throw IllegalArgumentException("матрицы должны иметь одинаковую размерность")
        }

        val refThis = this.toRowEchelonFormMod2()
        val refOther = other.toRowEchelonFormMod2()

        // Compare the REF forms of both matrices
        for (i in 0 until refThis.first.rows) {
            for (j in 0 until refThis.first.cols) {
                if (refThis.first.data[i][j] != refOther.first.data[i][j]) {
                    return false
                }
            }
        }
        return true
    }

    override fun toString(): String {
        return data.joinToString(separator = "\n") { row ->
            row.joinToString(separator = " ") { elem -> "${elem.compareTo(false)}" }
        }
    }

    fun isZero(): Boolean {
        return data.flatten().all { !it }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BinaryMatrix

        if (rows != other.rows || cols != other.cols) return false
        if (!data.contentDeepEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rows
        result = 31 * result + cols
        result = 31 * result + data.contentDeepHashCode()
        return result
    }

    operator fun times(binaryVector: BinaryVector): BinaryVector {
        val transposeVector = BinaryMatrix(binaryVector.size(), 1)
        for (i in 0 until binaryVector.size()) {
            transposeVector[i, 0] = binaryVector[i]
        }
        return BinaryVector((this * transposeVector).column(0))
    }
}