package org.fiolino.benchmark;

import org.fiolino.common.reflection.AlmostFinal;
import org.fiolino.common.reflection.Methods;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Performance tests for MethodHandles and their lambda counterparts.
 *
 * Created by kuli on 27.02.17.
 */
@BenchmarkMode(Mode.AverageTime) @Warmup(iterations = 2) @Fork(1) @OutputTimeUnit(TimeUnit.NANOSECONDS) @Measurement(iterations = 2)
@State(Scope.Benchmark)
public class BooleanBenchmark {

    private static final AlmostFinal<Boolean> WHAT_TO_ADD = AlmostFinal.forBoolean(false);
    private static final MethodHandle GETTER_HANDLE = WHAT_TO_ADD.createGetter();
    private static final BooleanSupplier GETTER_SUPPLIER = Methods.lambdafy(MethodHandles.lookup(), GETTER_HANDLE, BooleanSupplier.class);

    private final MethodHandle finalGetter = GETTER_HANDLE;
    private final BooleanSupplier finalGetterMethod = GETTER_SUPPLIER;

    private static boolean staticValue;

    {
        System.out.println("Lambda: " + !MethodHandleProxies.isWrapperInstance(GETTER_SUPPLIER));
    }

    @Setup(Level.Iteration)
    public void setup() {
        boolean newValue = !staticValue;
        WHAT_TO_ADD.updateTo(newValue);
        staticValue = newValue;
    }

    @Benchmark
    public void testStatic(Blackhole blackhole) {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            count += staticValue ? 5 : 1;
        }
        blackhole.consume(count);
    }

    @Benchmark
    public void testHandleInstanceMethod(Blackhole blackhole) throws Throwable {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            count += (boolean) finalGetter.invokeExact() ? 5 : 1;
        }
        blackhole.consume(count);
    }

    @Benchmark
    public void testHandleInstanceMethodSupplier(Blackhole blackhole) {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            count += finalGetterMethod.getAsBoolean() ? 5 : 1;
        }
        blackhole.consume(count);
    }

    @Benchmark
    public void testAlmostFinalHandle(Blackhole blackhole) throws Throwable {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            count += (boolean) GETTER_HANDLE.invokeExact() ? 5 : 1;
        }
        blackhole.consume(count);
    }

    @Benchmark
    public void testAlmostFinalSupplier(Blackhole blackhole) {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            count += GETTER_SUPPLIER.getAsBoolean() ? 5 : 1;
        }
        blackhole.consume(count);
    }
}
