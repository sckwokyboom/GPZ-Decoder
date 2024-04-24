package model.canonicalconverter

import model.BinaryVector

open class Matrix {
    var cols: Int
    val rows: Int
    private var data: Array<Array<Int>>

    constructor(rows: Int, cols: Int) : this(
        Array(rows) { Array(cols) { 0 } }
    )

    constructor(vector: Array<Int>) {
        this.rows = 1
        this.cols = vector.size
        require(rows > 0 && cols > 0) { "Количество строк и столбцов должно быть положительным целым числом." }
        this.data = arrayOf(vector)
    }

    constructor(data: Array<Array<Int>>) {
        this.rows = data.size
        this.cols = data[0].size
        require(rows > 0 && cols > 0) { "Количество строк и столбцов должно быть положительным целым числом." }
        this.data = data
    }

    companion object {
        fun eye(size: Int): Matrix {
            val result = Matrix(size, size)
            for (i in 0 until size) {
                result[i, i] = 1
            }
            return result
        }

        fun hstack(a: Matrix, b: Matrix): Matrix {
            if (a.rows != b.rows) throw IllegalArgumentException("Matrices don't have the same number of rows.")
            val result = Matrix(a.rows, a.cols + b.cols)
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

    fun gaussianElimination(): Matrix {
        val A = this.copy()
        var lead = 0
        for (r in 0 until A.rows) {
            if (A.cols <= lead) break
            var i = r
            while (A.data[i][lead] == 0) {
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

            val div = A.data[r][lead]
            if (div != 0) { // Для избежания деления на ноль
                for (j in 0 until A.cols) {
                    A.data[r][j] /= div
                }
            }

            for (k in 0 until A.rows) {
                if (k != r) {
                    val mult = A.data[k][lead]
                    for (j in 0 until A.cols) {
                        A.data[k][j] -= A.data[r][j] * mult
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
                if (ref.data[i][j] != 0) {
                    rank++
                    break
                }
            }
        }
        return rank
    }

    fun subMatrix(startRow: Int, endRow: Int, startCol: Int, endCol: Int): Matrix {
        val subRows = endRow - startRow
        val subCols = endCol - startCol
        val result = Matrix(subRows, subCols)
        for (i in 0 until subRows) {
            for (j in 0 until subCols) {
                result[i, j] = this[startRow + i, startCol + j]
            }
        }
        return result
    }

    fun determinant(): Int {
        require(isSquare()) { "определитель можно вычислить только для квадратной матрицы" }
        if (rows == 1) return this[0, 0]
        if (rows == 2) return this[0, 0] * this[1, 1] - this[1, 0] * this[0, 1]

        var det = 0
        for (col in 0 until cols) {
            det += this[0, col] * cofactor(0, col)
        }
        return det
    }

    fun isSquare() = cols == rows

    private fun cofactor(row: Int, col: Int): Int {
        return minor(row, col).determinant() * if ((row + col) % 2 == 0) 1 else -1
    }

    private fun minor(row: Int, col: Int): Matrix {
        val result = Matrix(rows - 1, cols - 1)
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
    fun toRowEchelonFormMod2(): Pair<Matrix, List<Int>> {
        val matrix = copy()
        val pivotColumns = mutableListOf<Int>()
        var lead = 0

        for (r in 0 until rows) {
            if (lead >= cols) break
            var i = r
            while (matrix[i, lead] == 0) {
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
                if (j != r && matrix[j, lead] != 0) {
                    for (k in 0 until cols) {
                        matrix[j, k] = (matrix[j, k] + matrix[r, k]) % 2
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
    fun copy(): Matrix {
        val newMatrix = Matrix(rows, cols)
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                newMatrix[i, j] = this[i, j]
            }
        }
        return newMatrix
    }

    operator fun get(row: Int, col: Int): Int {
        if (row !in 0 until rows || col !in 0 until cols) {
            throw IndexOutOfBoundsException("Обращение за пределы матрицы.")
        }
        return data[row][col]
    }

    fun row(index: Int): Array<Int> {
        require(index in 0..<rows) { "Обращение за пределы матрицы. Получено: $index, это число не принадлежит интервалу [0, $rows)." }
        return data[index]
    }

    fun column(index: Int): Array<Int> {
        require(index in 0..<cols) { "Обращение за пределы матрицы. Получено: $index, это число не принадлежит интервалу [0, $cols)." }
        val column = mutableListOf<Int>()
        data.forEach { column.add(it[index]) }
        return column.toTypedArray()
    }

    operator fun set(row: Int, col: Int, value: Int) {
        if (row !in 0 until rows || col !in 0 until cols) {
            throw IndexOutOfBoundsException("Запись за пределы матрицы.")
        }
        data[row][col] = value
    }

    private fun plus(left: Matrix, right: Matrix): Matrix {
        val result = Matrix(rows, cols)
        if (left.rows != right.rows || left.cols != right.cols) {
            throw IllegalArgumentException("размеры матриц должны совпадать для операции сложения")
        }

        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result[i, j] = left[i, j] + right[i, j]
            }
        }
        return result
    }

    fun transpose(): Matrix {
        val result = Matrix(cols, rows)
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result[j, i] = this[i, j]
            }
        }
        return result
    }

    operator fun plus(other: Matrix): Matrix {
        return plus(this, other)
    }

    operator fun plusAssign(other: Matrix) {
        this.data = plus(this, other).data
    }

    operator fun rem(constant: Int): Matrix {
        val result = Matrix(rows, cols)
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result[i, j] = this[i, j] % constant
            }
        }
        return result
    }

    operator fun times(constant: Int): Matrix {
        val result = Matrix(rows, cols)
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result[i, j] = this[i, j] * constant
            }
        }
        return result
    }

    operator fun times(other: Matrix): Matrix {
        if (cols != other.rows) {
            throw IllegalArgumentException(
                "Количество колонок первой матрицы должно совпадать с количеством строк второй при умножении"
            )
        }
        val n = rows
        val p = other.cols
        val m = cols
        val result = Matrix(n, p)
        for (i in 0 until n) {
            for (j in 0 until p) {
                var sum = 0
                for (k in 0 until m) {
                    sum += this[i, k] * other[k, j]
                }
                result[i, j] = sum
            }
        }
        return result
    }

    fun isTransformableTo(other: Matrix): Boolean {
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
        // Находим максимальную ширину числа в матрице для выравнивания
        val maxNumberWidth = data.flatten().maxOfOrNull { it.toString().length } ?: 0

        return data.joinToString(separator = "\n") { row ->
            row.joinToString(separator = " ") { elem -> "%${maxNumberWidth}d".format(elem) }
        }
    }

    fun isZero(): Boolean {
        return data.flatten().all { it == 0 }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Matrix

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
        val transposeVector = Matrix(binaryVector.size(), 1)
        for (i in 0 until binaryVector.size()) {
            transposeVector[i, 0] = binaryVector[i].compareTo(false)
        }
        return BinaryVector((this * transposeVector).rem(2).column(0))
    }
}