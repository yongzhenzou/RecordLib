package zyz.hero.record_kit

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object ThreadPool {
    private val EXECUTOR_THREADS = Runtime.getRuntime().availableProcessors()
    var processorsPools: ExecutorService? = null
        get() {
            if (field == null) {
                field = Executors.newFixedThreadPool(EXECUTOR_THREADS)
            }
            return field
        }
        private set
    var cachedPools: ExecutorService? = null
        get() {
            if (field == null) {
                field = Executors.newCachedThreadPool()
            }
            return field
        }
        private set
}