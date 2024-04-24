package model.binarycanonicalconverter.tester

import model.binarycanonicalconverter.BinaryMatrix


interface BinaryMatrixTester {
    fun test(vararg matrices: BinaryMatrix): Boolean
}