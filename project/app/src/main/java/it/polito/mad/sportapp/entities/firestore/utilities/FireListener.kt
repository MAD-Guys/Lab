package it.polito.mad.sportapp.entities.firestore.utilities

import com.google.firebase.firestore.ListenerRegistration

class FireListener {
    private val callbacksToUnregisterListeners = mutableListOf<()->Unit>()

    constructor()

    constructor(listenerRegistration: ListenerRegistration) {
        add(listenerRegistration)
    }

    constructor(fireListener: FireListener) {
        add(fireListener)
    }

    /**
     * Add another listener which has to be unregistered
     */
    fun add(listenerRegistration: ListenerRegistration) {
        callbacksToUnregisterListeners.add(0, listenerRegistration::remove)
    }

    /**
     * Add another listener which has to be unregistered
     */
    fun add(fireListener: FireListener) {
        callbacksToUnregisterListeners.add(0, fireListener::unregister)
    }

    /**
     * Unregister all the underlying Firestore snapshot listeners.
     * If this object has not any active listener, the call has no effect
     */
    fun unregister() {
        callbacksToUnregisterListeners.forEach { it() }
    }
}