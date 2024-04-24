package model.cyclotomicclasses

class CyclotomicClassesFinder {
    companion object {
        fun findAllCyclotomicClasses(module: Int, p: Int): Map<Int, CyclotomicClass> {
            val leaderAndCyclotomicClass = HashMap<Int, CyclotomicClass>()
            for (i in 0 until module) {
                if (leaderAndCyclotomicClass.containsKey(i)) {
                    continue
                }
                val representatives = HashSet<Int>()
                var newRepresentative = i
                while (!representatives.contains(newRepresentative)) {
                    representatives.add(newRepresentative)
                    newRepresentative = (newRepresentative * p) % module
                }
                val cyclotomicClass = CyclotomicClass(representatives)
                for (representative in representatives) {
                    if (leaderAndCyclotomicClass.containsKey(representative)) {
                        continue
                    } else {
                        leaderAndCyclotomicClass[representative] = cyclotomicClass
                    }
                }
            }
            return leaderAndCyclotomicClass
        }
    }
}