import java.math.BigInteger

fun allBinaryVectorPermutations(weight: Int, vectorLength: Int): Sequence<BigInteger> {
    if (weight > vectorLength) {
        throw IllegalArgumentException("Вес не может быть больше длины вектора.")
    }
    val permutations = ArrayList<BigInteger>()
    val binaryLimit = BigInteger.TWO.pow(vectorLength)
    var initVector = BigInteger.TWO.pow(weight) - BigInteger.ONE
    var vectorPerm = initVector
    while (vectorPerm < binaryLimit) {
        permutations.add(vectorPerm)
        val t = (vectorPerm or (vectorPerm - BigInteger.ONE)) + BigInteger.ONE
        val nextVectorPerm = t or ((((t and -t) / (vectorPerm and -vectorPerm)) shr 1) - BigInteger.ONE)
        if (nextVectorPerm < binaryLimit) {
            vectorPerm = nextVectorPerm
        } else {
            break
        }
    }
    return permutations.asSequence()
}

fun binaryVectorPermutationsIterator(weight: Int, vectorLength: Int): Iterator<BigInteger> {
    if (weight > vectorLength) {
        throw IllegalArgumentException("Вес не может быть больше длины вектора.")
    }

    val binaryLimit = BigInteger.TWO.pow(vectorLength)
    var vectorPerm = BigInteger.TWO.pow(weight) - BigInteger.ONE

    return object : Iterator<BigInteger> {
        override fun hasNext(): Boolean = vectorPerm < binaryLimit

        override fun next(): BigInteger {
            val currentPerm = vectorPerm
            val t = (vectorPerm or (vectorPerm - BigInteger.ONE)) + BigInteger.ONE
            vectorPerm = t or ((((t and -t) / (vectorPerm and -vectorPerm)) shr 1) - BigInteger.ONE)
            return currentPerm
        }
    }
}