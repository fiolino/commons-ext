package org.fiolino.benchmark;

import org.fiolino.common.reflection.AlmostFinal;
import org.fiolino.common.reflection.Methods;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

/**
 * Created by kuli on 27.02.17.
 */
@BenchmarkMode(Mode.AverageTime) @Warmup(iterations = 5) @Fork(value = 1) @OutputTimeUnit(TimeUnit.NANOSECONDS)
public class AlmostFinalBenchmark {

//    public static void main(String[] args) throws RunnerException {
//        Options options = new OptionsBuilder().forks(1).threads(1).measurementIterations(10).warmupBatchSize(5).build();
//        new Runner(options).run();
//    }

    private static final AlmostFinal<Integer> WHAT_TO_ADD = AlmostFinal.forInt(2);
    private static final MethodHandle GETTER_HANDLE = WHAT_TO_ADD.createGetter();
    private static final IntSupplier GETTER_SUPPLIER = Methods.lambdafy(GETTER_HANDLE, IntSupplier.class);

    private static final int CONSTANT = 2;
    private static int notConstant = 2;
    private static volatile int definitelyNotConstant = 2;

    private static final MethodHandle CONSTANT_GETTER;
    private static final IntSupplier CONSTANT_SUPPLIER;

    private static int getValue() {
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

        System.out.println("Supp 1: " + MethodHandleProxies.isWrapperInstance(GETTER_SUPPLIER) + ", Supp 2: " + MethodHandleProxies.isWrapperInstance(CONSTANT_SUPPLIER) + ", Supp 3: " + MethodHandleProxies.isWrapperInstance(CONSTANT_SUPPLIER_METHOD));
    }

    @Benchmark
    public void testDirectConstant(Blackhole blackhole) {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            count += CONSTANT;
        }
        blackhole.consume(count);
    }

    @Benchmark
    public void testDirectStatic(Blackhole blackhole) {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            count += notConstant;
        }
        blackhole.consume(count);
    }

    @Benchmark
    public void testDirectVolatile(Blackhole blackhole) {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            count += definitelyNotConstant;
        }
        blackhole.consume(count);
    }

    @Benchmark
    public void testHandleConstant(Blackhole blackhole) throws Throwable {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            count += (int) CONSTANT_GETTER.invokeExact();
        }
        blackhole.consume(count);
    }

    @Benchmark
    public void testHandleConstantSupplier(Blackhole blackhole) {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            count += CONSTANT_SUPPLIER.getAsInt();
        }
        blackhole.consume(count);
    }

    @Benchmark
    public void testHandleMethod(Blackhole blackhole) throws Throwable {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            count += (int) CONSTANT_METHOD.invokeExact();
        }
        blackhole.consume(count);
    }

    @Benchmark
    public void testHandleMethodSupplier(Blackhole blackhole) {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            count += CONSTANT_SUPPLIER_METHOD.getAsInt();
        }
        blackhole.consume(count);
    }

    @Benchmark
    public void testAlmostFinalHandle(Blackhole blackhole) throws Throwable {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            count += (int) GETTER_HANDLE.invokeExact();
        }
        blackhole.consume(count);
    }

    @Benchmark
    public void testAlmostFinalSupplier(Blackhole blackhole) {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            count += GETTER_SUPPLIER.getAsInt();
        }
        blackhole.consume(count);
    }

    @Benchmark
    public void testReturn2(Blackhole blackhole) {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            count += RETURN_2.getAsInt();
        }
        blackhole.consume(count);
    }

    @Benchmark
    public void testReturn5(Blackhole blackhole) {
        int count = 0;
        for (int i=0; i < 10_000; i++) {
            count += RETURN_5.getAsInt();
        }
        blackhole.consume(count);
    }
}
