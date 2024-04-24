package polynomials

import model.field.GaloisFieldMod2


class BinaryPolynomialMatrix {
    var cols: Int
    val rows: Int
    private var data: Array<Array<BinaryPolynomial>>
    private val gf: GaloisFieldMod2

    constructor(rows: Int, cols: Int, gf: GaloisFieldMod2) : this(
        Array(rows) { Array(cols) { BinaryPolynomial(emptyList()) } },
        gf
    )

    constructor(vector: Array<BinaryPolynomial>, gf: GaloisFieldMod2) {
        this.rows = 1
        this.cols = vector.size
        require(rows > 0 && cols > 0) { "Количество строк и столбцов должно быть положительным целым числом." }
        this.data = arrayOf(vector)
        this.gf = gf
    }

    constructor(data: Array<Array<BinaryPolynomial>>, gf: GaloisFieldMod2) {
        this.rows = data.size
        this.cols = data[0].size
        require(rows > 0 && cols > 0) { "Количество строк и столбцов должно быть положительным целым числом." }
        this.data = data
        this.gf = gf
    }

    fun gaussianElimination(): BinaryPolynomialMatrix {
        val A = this.copy()
        var lead = 0
        for (r in 0 until A.rows) {
            if (A.cols <= lead) break
            var i = r
            while (A.data[i][lead].castToFieldElements(gf).isZero()) {
                i++
                if (A.rows == i) {
                    i = r
                    lead++
                    if (A.cols == lead) {
                        for (u in 0 until A.rows) {
                            for (j in 0 until A.cols) {
                                A.data[u][j] = A.data[u][j].castToFieldElements(gf)
                            }
                        }
                        return A
                    }
                }
            }

            val temp = A.data[r]
            A.data[r] = A.data[i]
            A.data[i] = temp

            val leaderElement = A.data[r][lead]
            if (leaderElement.isZero()) {
                throw RuntimeException("Лидер оказался равным нулю.")
            }
            val inv = gf[leaderElement]
            for (j in 0 until A.cols) {
                A.data[r][j] = (A.data[r][j] * inv)
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

        for (i in 0 until A.rows) {
            for (j in 0 until A.cols) {
                A.data[i][j] = A.data[i][j].castToFieldElements(gf)
            }
        }
        return A
    }

    private fun backSubstitute(): ArrayList<BinaryPolynomial> {
        val A = this.copy()
        val solutions = ArrayList<BinaryPolynomial>(A.rows)
        for (i in 0 until A.rows) {
            solutions.add(BinaryPolynomial(emptyList()))
        }
        for (i in rows - 1 downTo 0) {
            var sum = BinaryPolynomial(emptyList())
            for (j in i until cols - 1) {
                sum += A[i, j] * solutions[j]
            }
            solutions[i] = (A[i, cols - 1] - sum)
        }
        return solutions
    }

    fun solveSystem(): ArrayList<BinaryPolynomial> {
        val stepMatrix = this.gaussianElimination()
        val solutions = stepMatrix.backSubstitute()
        return solutions
    }

    fun rank(): Int {
        val ref = this.toRowEchelonForm().first
        var rank = 0
        for (i in 0 until ref.rows) {
            for (j in 0 until ref.cols) {
                if (!ref.data[i][j].isZero()) {
                    rank++
                    break
                }
            }
        }
        return rank
    }

    fun subMatrix(startRow: Int, endRow: Int, startCol: Int, endCol: Int): BinaryPolynomialMatrix {
        val subRows = endRow - startRow
        val subCols = endCol - startCol
        val result = BinaryPolynomialMatrix(subRows, subCols, gf)
        for (i in 0 until subRows) {
            for (j in 0 until subCols) {
                result[i, j] = this[startRow + i, startCol + j]
            }
        }
        return result
    }

    fun determinant(): BinaryPolynomial {
        require(isSquare()) { "определитель можно вычислить только для квадратной матрицы" }
        if (rows == 1) return this[0, 0]
        if (rows == 2) return this[0, 0] * this[1, 1] - this[1, 0] * this[0, 1]

        var det = BinaryPolynomial(emptyList())
        for (col in 0 until cols) {
            det += this[0, col] * cofactor(0, col)
        }
        return det
    }

    fun isSquare() = cols == rows

    private fun cofactor(row: Int, col: Int): BinaryPolynomial {
        return minor(row, col).determinant() * BinaryPolynomial(intArrayOf(1))
    }

    private fun minor(row: Int, col: Int): BinaryPolynomialMatrix {
        val result = BinaryPolynomialMatrix(rows - 1, cols - 1, gf)
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                if (i == row || j == col) continue
                result[if (i < row) i else i - 1, if (j < col) j else j - 1] = this[i, j]
            }
        }
        return result
    }


    /**
     * Приведение матрицы к ступенчатому виду.
     */
    fun toRowEchelonForm(): Pair<BinaryPolynomialMatrix, List<Int>> {
        val matrix = copy()
        val pivotColumns = mutableListOf<Int>()
        var lead = 0

        for (r in 0 until rows) {
            if (lead >= cols) break
            var i = r
            while (matrix[i, lead].isZero()) {
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
                if (j != r && !matrix[j, lead].isZero()) {
                    for (k in 0 until cols) {
                        matrix[j, k] = (matrix[j, k] + matrix[r, k])
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
    fun copy(): BinaryPolynomialMatrix {
        val newMatrix = BinaryPolynomialMatrix(rows, cols, gf)
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                newMatrix[i, j] = this[i, j]
            }
        }
        return newMatrix
    }

    operator fun get(row: Int, col: Int): BinaryPolynomial {
        if (row !in 0 until rows || col !in 0 until cols) {
            throw IndexOutOfBoundsException("Обращение за пределы матрицы.")
        }
        return data[row][col]
    }

    fun row(index: Int): Array<BinaryPolynomial> {
        require(index in 0..<rows) { "Обращение за пределы матрицы. Получено: $index, это число не принадлежит интервалу [0, $rows)." }
        return data[index]
    }

    fun column(index: Int): Array<BinaryPolynomial> {
        require(index in 0..<cols) { "Обращение за пределы матрицы. Получено: $index, это число не принадлежит интервалу [0, $cols)." }
        val column = mutableListOf<BinaryPolynomial>()
        data.forEach { column.add(it[index]) }
        return column.toTypedArray()
    }

    operator fun set(row: Int, col: Int, value: BinaryPolynomial) {
        if (row !in 0 until rows || col !in 0 until cols) {
            throw IndexOutOfBoundsException("Запись за пределы матрицы.")
        }
        data[row][col] = value
    }

    private fun plus(left: BinaryPolynomialMatrix, right: BinaryPolynomialMatrix): BinaryPolynomialMatrix {
        val result = BinaryPolynomialMatrix(rows, cols, gf)
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

    fun transpose(): BinaryPolynomialMatrix {
        val result = BinaryPolynomialMatrix(cols, rows, gf)
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result[j, i] = this[i, j]
            }
        }
        return result
    }

    operator fun plus(other: BinaryPolynomialMatrix): BinaryPolynomialMatrix {
        return plus(this, other)
    }

    operator fun plusAssign(other: BinaryPolynomialMatrix) {
        this.data = plus(this, other).data
    }

    operator fun times(other: BinaryPolynomialMatrix): BinaryPolynomialMatrix {
        if (cols != other.rows) {
            throw IllegalArgumentException(
                "Количество колонок первой матрицы должно совпадать с количеством строк второй при умножении"
            )
        }
        val n = rows
        val p = other.cols
        val m = cols
        val result = BinaryPolynomialMatrix(n, p, gf)
        for (i in 0 until n) {
            for (j in 0 until p) {
                var sum = BinaryPolynomial(emptyList())
                for (k in 0 until m) {
                    sum += this[i, k] * other[k, j]
                }
                result[i, j] = sum
            }
        }
        return result
    }

    fun isTransformableTo(other: BinaryPolynomialMatrix): Boolean {
        if (this.rows != other.rows || this.cols != other.cols) {
            throw IllegalArgumentException("матрицы должны иметь одинаковую размерность")
        }

        val refThis = this.toRowEchelonForm()
        val refOther = other.toRowEchelonForm()

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
            row.joinToString(separator = " ") { elem -> "$elem" }
        }
    }

    fun isZero(): Boolean {
        return data.flatten().all { it.isZero() }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BinaryPolynomialMatrix

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
}