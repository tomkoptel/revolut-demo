package com.tom.personal.revolut

import io.reactivex.Scheduler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.functions.Function
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.concurrent.Callable

/**
 * JUnit Test Rule which overrides RxJava and Android schedulers for use in unit tests.
 *
 * Source from library: com.github.Plastix.RxSchedulerRule:rx2:1.0.2
 */
class RxSchedulerRule(private val computationalScheduler: Scheduler =  Schedulers.trampoline()) : TestRule {
    private val SCHEDULER_INSTANCE = Schedulers.trampoline()

    private val schedulerFunction = Function<Scheduler, Scheduler> { SCHEDULER_INSTANCE }
    private val computationalSchedulerFunction = Function<Scheduler, Scheduler> { computationalScheduler }
    private val schedulerFunctionLazy = Function<Callable<Scheduler>, Scheduler> { SCHEDULER_INSTANCE }

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                RxAndroidPlugins.reset()
                RxAndroidPlugins.setInitMainThreadSchedulerHandler(schedulerFunctionLazy)

                RxJavaPlugins.reset()
                RxJavaPlugins.setIoSchedulerHandler(schedulerFunction)
                RxJavaPlugins.setNewThreadSchedulerHandler(schedulerFunction)
                RxJavaPlugins.setComputationSchedulerHandler(computationalSchedulerFunction)

                base.evaluate()

                RxAndroidPlugins.reset()
                RxJavaPlugins.reset()
            }
        }
    }
}
