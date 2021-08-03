package com.swein.easyphotox.util.eventsplitshot.eventcenter

import java.lang.ref.WeakReference

object EventCenter {

    private const val TAG = "EventCenter"

    interface EventRunnable {
        fun run(arrow: String, poster: Any, data: MutableMap<String, Any>?)
    }

    private var map: MutableMap<String, MutableList<EventObserver>> = mutableMapOf()

    class EventObserver {

        var arrow: String? = null
        var objectWeakReference: WeakReference<Any>? = null

        var runnable: EventRunnable? = null

        constructor(arrow: String, obj: Any, runnable: EventRunnable) {
            this.arrow = arrow
            this.objectWeakReference = WeakReference(obj)
            this.runnable = runnable
        }
    }

    fun addEventObserver(arrow: String, obj: Any, runnable: EventRunnable) {
        val eventObserver = EventObserver(arrow, obj, runnable)
        getObserverListForArrows(arrow).add(eventObserver)
    }

    fun removeAllObserver(obj: Any) {
        val deleteList: MutableList<EventObserver> = mutableListOf()

        for(arrayList in map.values) {
            deleteList.clear()

            for(observer in arrayList) {
                if(observer.objectWeakReference?.get() === obj) {
                    deleteList.add(observer)
                }
            }
            arrayList.removeAll(deleteList)
        }
    }

    fun removeObserverForArrows(arrow: String, obj: Any) {

        val result: MutableList<EventObserver> = getObserverListForArrows(arrow)
        val deleteList: MutableList<EventObserver> = mutableListOf()

        var any: Any?

        for(eventObserver in result) {
            any = eventObserver.objectWeakReference?.get()
            if (any === obj) {
                deleteList.add(eventObserver)
            }
        }

        for (eventObserver in deleteList) {
            result.remove(eventObserver)
        }
    }

    fun sendEvent(arrow: String, sender: Any, data: MutableMap<String, Any>?) {
        val result: MutableList<EventObserver> = getObserverListForArrows(arrow)

        for(eventObserver in result) {
            if (eventObserver.runnable != null) {
                eventObserver.runnable!!.run(arrow, sender, data)
            }
        }
    }

    private fun getObserverListForArrows(arrow: String): MutableList<EventObserver> {
        var result: MutableList<EventObserver>?= map[arrow]

        if (result == null) {
            result = mutableListOf()
            map[arrow] = result
        }

        return result
    }
}