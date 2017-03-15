package org.fiolino.benchmark;

import org.fiolino.common.reflection.AlmostFinal;
import org.fiolino.common.reflection.Methods;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

/**
 * Performance tests for MethodHandles and their lambda counterparts.
 *
 * Created by kuli on 27.02.17.
 */
@BenchmarkMode(Mode.AverageTime) @Warmup(iterations = 2) @Fork(value = 1) @OutputTimeUnit(TimeUnit.NANOSECONDS) @Measurement(iterations = 2)
public class CheckBooleanBenchmark {

    private static final AlmostFinal<Integer> WHAT_TO_ADD = AlmostFinal.forInt(2);
    private static final MethodHandle GETTER_HANDLE = WHAT_TO_ADD.createGetter();
    private static final IntSupplier GETTER_SUPPLIER = Methods.lambdafy(GETTER_HANDLE, IntSupplier.class);

    private static final int CONSTANT = 2;
    private static int notConstant = 2;
    private static volatile int definitelyNotConstant = 2;

    private static final MethodHandle CONSTANT_GETTER;
    private static final IntSupplier CONSTANT_SUPPLIER;

    static int getValue() {
        return 2;
    }

    private static final MethodHandle CONSTANT_METHOD;
    private static final IntSupplier CONSTANT_SUPPLIER_METHOD;

    private static int returnParameter(int value) {
        return value;
    }

    private static final IntSupplier RETURN_2;
    private static final IntSupplier RETURN_5;

    static {
        MethodHandles.Lookup lookup = lookup();
        MethodHandle r;
        try {
            CONSTANT_GETTER = lookup.findStaticGetter(lookup.lookupClass(), "CONSTANT", int.class);
            CONSTANT_METHOD = lookup.findStatic(lookup.lookupClass(), "getValue", methodType(int.class));
            r = lookup.findStatic(lookup.lookupClass(), "returnParameter", methodType(int.class, int.class));
        } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException ex) {
            throw new AssertionError(ex);
        }
        CONSTANT_SUPPLIER = Methods.lambdafy(lookup, CONSTANT_GETTER, IntSupplier.class);
        CONSTANT_SUPPLIER_METHOD = Methods.lambdafy(lookup, CONSTANT_METHOD, IntSupplier.class);

        MethodHandle lambdaFactory = Methods.createLambdaFactory(lookup, r, IntSupplier.class);
        try {
            RETURN_2 = (IntSupplier) lambdaFactory.invokeExact(2);
            RETURN_5 = (IntSupplier) lambdaFactory.invokeExact(5);
        } catch (Throwable t) {
            throw new AssertionError(t);
        }
    }

    @Benchmark
    public void testDirectConstant(Blackhole blackhole) {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            if (CONSTANT < 2) count++;
        }
        blackhole.consume(count);
    }

    @Benchmark
    public void testDirectStatic(Blackhole blackhole) {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            if (notConstant < 2) count++;
        }
        blackhole.consume(count);
    }

    @Benchmark
    public void testDirectVolatile(Blackhole blackhole) {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            if (definitelyNotConstant < 2) count++;
        }
        blackhole.consume(count);
    }

    @Benchmark
    public void testHandleConstant(Blackhole blackhole) throws Throwable {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            if ((int) CONSTANT_GETTER.invokeExact() < 2) count++;
        }
        blackhole.consume(count);
    }

    @Benchmark
    public void testHandleConstantSupplier(Blackhole blackhole) {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            if (CONSTANT_SUPPLIER.getAsInt() < 2) count++;
        }
        blackhole.consume(count);
    }

    @Benchmark
    public void testHandleMethod(Blackhole blackhole) throws Throwable {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            if ((int) CONSTANT_METHOD.invokeExact() < 2) count++;
        }
        blackhole.consume(count);
    }

    @Benchmark
    public void testHandleMethodSupplier(Blackhole blackhole) {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            if (CONSTANT_SUPPLIER_METHOD.getAsInt() < 2) count++;
        }
        blackhole.consume(count);
    }

    @Benchmark
    public void testAlmostFinalHandle(Blackhole blackhole) throws Throwable {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            if ((int) GETTER_HANDLE.invokeExact() < 2) count++;
        }
        blackhole.consume(count);
    }

    @Benchmark
    public void testAlmostFinalSupplier(Blackhole blackhole) {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            if (GETTER_SUPPLIER.getAsInt() < 2) count++;
        }
        blackhole.consume(count);
    }

    @Benchmark
    public void testReturn2(Blackhole blackhole) {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            if (RETURN_2.getAsInt() < 3) count++;
        }
        blackhole.consume(count);
    }

    @Benchmark
    public void testReturn5(Blackhole blackhole) {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            if (RETURN_5.getAsInt() < 3) count++;
        }
        blackhole.consume(count);
    }
}
