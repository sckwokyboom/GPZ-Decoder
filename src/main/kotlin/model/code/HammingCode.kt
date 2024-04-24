package model.code

import model.canonicalconverter.CanonicalConverter
import model.canonicalconverter.Matrix
import kotlin.math.pow

class HammingCode(override val parityCheckLength: Int) : Code() {
    companion object {
        private fun generateHammingParityCheckMatrix(parityLength: Int): Matrix {
            val codeLength = (2.0.pow(parityLength) - 1).toInt()
            val parityCheckMatrix = Matrix(parityLength, codeLength)
            for (i in 0 until codeLength) {
                val binaryString = Integer.toBinaryString(i + 1).padStart(parityLength, '0')
                for (j in 0 until parityLength) {
                    parityCheckMatrix[j, i] = binaryString[j].code.toByte() - '0'.code.toByte()
                }
            }
            return parityCheckMatrix
        }
    }

    override val length: Int = (2.0.pow(parityCheckLength) - 1).toInt()
    override val parityCheckMatrix: Matrix = generateHammingParityCheckMatrix(parityCheckLength)
    override val generatorMatrix: Matrix
    override val dimension: Int = length - parityCheckLength

    init {
        val convertor = CanonicalConverter()
        generatorMatrix = convertor.toGenerative(parityCheckMatrix)
    }
}