package model

import java.util.*

class Channel(private val probabilityOfError: Double, private val lengthOfCodewords: Int) {
    private val random = Random()
    private var currentDataVector: BinaryVector = BinaryVector(lengthOfCodewords)

    fun send(dataVector: BinaryVector) {
        this.currentDataVector = dataVector.clone() as BinaryVector
        for (i in 0 until dataVector.size()) {
            if (random.nextDouble() < probabilityOfError) {
                this.currentDataVector.flip(i)
            }
        }
    }

    fun receive(): BinaryVector {
        return currentDataVector
    }
}