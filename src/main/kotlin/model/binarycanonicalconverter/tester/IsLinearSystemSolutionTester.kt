package model.binarycanonicalconverter.tester

import model.binarycanonicalconverter.BinaryMatrix

class IsLinearSystemSolutionBinaryTester : BinaryMatrixTester {
    override fun test(vararg matrices: BinaryMatrix): Boolean {
        require(matrices.size == 2) {
            "Количество матриц не соответствует двум: матрица коэффициентов и матрица решений."
        }
        val (a: BinaryMatrix, x: BinaryMatrix) = matrices
        return (a * x.transpose()).isZero()
    }
}