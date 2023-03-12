import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.util.NoSuchElementException
import java.util.Stack

class Forth {

    private val stack: Stack<Int> = Stack<Int>()

    private val userCommands: MutableMap<String, String> = mutableMapOf()

    fun evaluate(vararg line: String): List<Int> {

        var commands = line.last()
        val pattern = ":([^:;]|-*\\d+)*;".toRegex()

        when {
            line.size == 1 && line.first().matches(pattern) -> fillMap(line.toList(), pattern)
            else -> fillMap(line.dropLast(1), pattern)
        }

        if (userCommands.isNotEmpty()) {
            for (element in commands.split(' ')) {
                for ((key, value) in userCommands) {
                    if (element.lowercase() == key) {
                        commands = commands.replace(element, value)
                    }
                }
            }
            println(commands)
        }

        val splitArgs: List<String> = commands.split(' ')

        for (element in splitArgs) {
            when {
                element == "+" -> sum()
                element == "-" -> subtract()
                element == "*" -> multiply()
                element == "/" -> divide()
                element.lowercase() == "dup" -> dup()
                element.lowercase() == "drop" -> drop()
                element.lowercase() == "swap" -> swap()
                element.lowercase() == "over" -> over()
                isStringDigit(element) -> stack.push(element.toInt())
                else -> throw IllegalArgumentException("undefined operation")
            }
        }

        return stack.toList()
    }

    private fun fillMap(line: List<String>, pattern: Regex) {
        var commands: Array<String>

        for (element in line) {
            if (pattern.matches(element)) {

                commands = element.replace(": ", "").replace(" ;", "").split(' ', limit = 2).toTypedArray()

                /*
                    Commands[0] is the key of the Map's entry
                    Commands[1] is the value of the Map's entry
                */

                if (isStringDigit(commands[0])) {
                    throw IllegalArgumentException("illegal operation")
                }
                if (userCommands.containsKey(commands[0]) && !commands[1].contains(commands[0].toRegex())) {
                    userCommands.replace(commands[0], commands[1])
                }

                for ((key, value) in userCommands) {
                    if (commands[1].contains(key.toRegex())) {
                        commands[1] = commands[1].replace(key, value)
                    }
                }

                userCommands.put(commands[0].lowercase(), commands[1].lowercase())
            }
        }

    }

    private fun isStringDigit(string: String): Boolean {
        return string.toIntOrNull() != null
    }

    private fun sum() {

        if (stack.isEmpty()) {
            throw NoSuchElementException("empty stack")
        }
        if (stack.size == 1) {
            throw IllegalStateException("only one value on the stack")
        }

        val operand2: Int = stack.pop()
        val operand1: Int = stack.pop()

        val sum: Int = operand1 + operand2

        stack.push(sum)
    }

    private fun subtract() {

        if (stack.isEmpty()) {
            throw NoSuchElementException("empty stack")
        }
        if (stack.size == 1) {
            throw IllegalStateException("only one value on the stack")
        }

        val operand2: Int = stack.pop()
        val operand1: Int = stack.pop()

        val subtraction: Int = operand1 - operand2

        stack.push(subtraction)
    }

    private fun multiply() {

        if (stack.isEmpty()) {
            throw NoSuchElementException("empty stack")
        }
        if (stack.size == 1) {
            throw IllegalStateException("only one value on the stack")
        }

        val operand2: Int = stack.pop()
        val operand1: Int = stack.pop()

        val multiplication: Int = operand1 * operand2

        stack.push(multiplication)
    }

    private fun divide() {

        if (stack.isEmpty()) {
            throw NoSuchElementException("empty stack")
        }
        if (stack.size == 1) {
            throw IllegalStateException("only one value on the stack")
        }

        val operand2: Int = stack.pop() //divisor
        val operand1: Int = stack.pop() //divider

        if (operand2 == 0) {
            throw ArithmeticException("divide by zero")
        }

        val division: Int = operand1 / operand2

        stack.push(division)
    }

    private fun dup() {
        if (stack.isEmpty()) {
            throw NoSuchElementException("empty stack")
        }

        stack.push(stack.lastElement())
    }

    private fun drop() {
        if (stack.isEmpty()) {
            throw NoSuchElementException("empty stack")
        }

        stack.pop()
    }

    private fun swap() {
        if (stack.isEmpty()) {
            throw NoSuchElementException("empty stack")
        }
        if (stack.size == 1) {
            throw IllegalStateException("only one value on the stack")
        }

        val element1: Int = stack.pop()
        val element2: Int = stack.pop()


        stack.push(element1)
        stack.push(element2)
    }

    private fun over() {
        if (stack.isEmpty()) {
            throw NoSuchElementException("empty stack")
        }
        if (stack.size == 1) {
            throw IllegalStateException("only one value on the stack")
        }

        stack.push(stack[stack.size - 2])
    }

}