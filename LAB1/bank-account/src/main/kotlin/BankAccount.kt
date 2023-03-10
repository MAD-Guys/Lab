import java.util.concurrent.atomic.AtomicLong

class BankAccount {
    var atomic_balance = AtomicLong(0)
    var isClosed: Boolean = false;
    var balance: Long = 0
        set(value) { if (isClosed) {
            throw IllegalStateException("Account is closed")
        }
            atomic_balance.set(value)

        field = value
    }
        get() {
            if (isClosed) {
                throw IllegalStateException("Account is closed")
            }
            return atomic_balance.get()
        }

    fun adjustBalance(amount: Long){

        if (isClosed) {
            throw IllegalStateException("Account is closed")
        }
        atomic_balance.addAndGet(amount)
    }

    fun close() {
        if (isClosed) {
            throw IllegalStateException("Account is already closed")
        }
        isClosed = true
    }
}
