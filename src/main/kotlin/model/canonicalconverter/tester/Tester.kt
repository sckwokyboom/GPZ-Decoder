package model.canonicalconverter.tester

import model.canonicalconverter.Matrix

interface MatrixTester {
    fun test(vararg matrices: Matrix): Boolean
}