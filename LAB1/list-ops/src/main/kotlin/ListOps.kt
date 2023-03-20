fun <T> List<T>.customAppend(list: List<T>): List<T> {
    val returnList: MutableList<T> = this.toMutableList()

    if (returnList.addAll(list) || list.isEmpty()) {
        return returnList
    } else {
        throw IllegalStateException("Cannot append the list!")
    }
}

fun <T> List<T>.customAppend2(list: List<T>): List<T> {
    return list.customFoldLeft(this) { acc, it -> acc + it }
}

fun List<Any>.customConcat(): List<Any> {

    val returnList: MutableList<Any> = mutableListOf<Any>()

    if (this.all { it is List<*> }) {
        this.forEach() { outerEl ->
            val outerElement = outerEl as List<Any>
            val listOfLists: MutableList<Any> = mutableListOf<Any>()

            if (outerElement.all { it is List<*> }) {
                outerElement.forEach() {
                    val innerElement = it as List<Any>
                    listOfLists.addAll(innerElement)
                }
                returnList.addAll(listOfLists)
            } else {
                returnList.addAll(outerElement)
            }
        }
    }

    return returnList
}

fun <T> List<T>.customFilter(predicate: (T) -> Boolean): List<T> {

    val returnList: MutableList<T> = mutableListOf<T>()

    this.forEach() {
        if (predicate(it)) {
            returnList.add(it)
        }
    }

    return returnList
}

fun <T> List<T>.customFilter2(predicate: (T) -> Boolean): List<T> {
    return this.customFoldLeft(emptyList()) { acc, it -> if (predicate(it)) acc + it else acc }
}

val List<Any>.customSize: Int get() = this.fold(0) { acc, _ -> acc + 1 }

val List<Any>.customSize2: Int get() = this.customFoldLeft(0) { acc, _ -> acc + 1 }

fun <T, U> List<T>.customMap(transform: (T) -> U): List<U> {

    val returnList: MutableList<U> = mutableListOf<U>()

    this.forEach() {
        returnList.add(transform(it))
    }

    return returnList
}

fun <T, U> List<T>.customMap2(transform: (T) -> U): List<U> {
    return this.customFoldLeft(emptyList()) { acc, it -> acc + transform(it) }
}

fun <T, U> List<T>.customFoldLeft(initial: U, f: (U, T) -> U): U {

    var accumulator: U = initial

    this.forEach() {
        accumulator = f(accumulator, it)
    }

    return accumulator
}

fun <T, U> List<T>.customFoldRight(initial: U, f: (T, U) -> U): U {

    var accumulator: U = initial

    this.reversed().forEach() {
        accumulator = f(it, accumulator)
    }

    return accumulator
}

fun <T> List<T>.customReverse(): List<T> {

    val returnList: MutableList<T> = mutableListOf<T>()

    this.reversed().forEach() {
        returnList.add(it)
    }

    return returnList
}

fun <T> List<T>.customReverse2(): List<T> {
    return this.customFoldRight(emptyList()) { it, acc -> acc + it }
}