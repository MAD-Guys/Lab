data class MinesweeperBoard(val board: List<String>) {

    private fun Char.toNumber() :Int { if(this=='*') return -1 else return 0 } //Converts the board to Int, mapping the mines with -1
    private fun Int.toStar() :Char { if(this==-1) return '*' else if(this==0) return ' ' else return this.digitToChar()} //Converts from Int to the chars expected by the tests ('*',' ','1',...)
    private fun Int.increment() :Int { if(this !=-1) return this+1 else return -1 } //Increments if there is not a mine (-1)
    private fun incrementCircle(b: List<MutableList<Int>>, i :Int, j :Int) :List<List<Int>> {
        //i and j are indexes of a *. This function tries to increment the 7 cells around it

        //row i-1
        if(i-1 >= 0){
            if(j-1 >= 0) b[i-1][j-1] = b[i-1][j-1].increment()
            b[i-1][j] = b[i-1][j].increment()
            if(j+1 < b[i-1].size) b[i-1][j+1] = b[i-1][j+1].increment()
        }

        //Row i
        if(j-1 >= 0) b[i][j-1] = b[i][j-1].increment()
        if(j+1 < b[i].size) b[i][j+1] = b[i][j+1].increment()

        //Row i+1
        if(i+1 < b.size){
            if(j-1 >= 0) b[i+1][j-1] = b[i+1][j-1].increment()
            b[i+1][j] = b[i+1][j].increment()
            if(j+1 < b[i+1].size) b[i+1][j+1] = b[i+1][j+1].increment()
        }
        //println(b)
        return b
    }

    fun withNumbers(): List<String> {
        var numberBoard = board.map() { it.map() { a -> a.toNumber() } }

        for ((i, row) in numberBoard.withIndex()) {
            for ((j, value) in row.withIndex()) {
                if (value == -1) numberBoard = incrementCircle(numberBoard as List<MutableList<Int>>, i, j)
            }
        }

        return numberBoard.map() { it.map() { x -> x.toStar() }.joinToString(separator="") }
    }
}
