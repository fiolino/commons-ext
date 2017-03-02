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
 * Performance tests for MethodHandles and their lambda counterparts.
 *
 * Created by kuli on 27.02.17.
 */
@BenchmarkMode(Mode.AverageTime) @Warmup(iterations = 5) @Fork(value = 1) @OutputTimeUnit(TimeUnit.NANOSECONDS) @Measurement(iterations = 10)
@State(Scope.Benchmark)
public class SingleCallBenchmark {

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

        RETURN_2 = Methods.lambdafy(lookup, r, IntSupplier.class, 2);
        RETURN_5 = Methods.lambdafy(lookup, r, IntSupplier.class, 5);

        System.out.println("Supp 1: " + MethodHandleProxies.isWrapperInstance(GETTER_SUPPLIER) + ", Supp 2: " + MethodHandleProxies.isWrapperInstance(CONSTANT_SUPPLIER) + ", Supp 3: " + MethodHandleProxies.isWrapperInstance(CONSTANT_SUPPLIER_METHOD));
    }

    private final MethodHandle finalGetter = CONSTANT_GETTER;
    private final IntSupplier finalGetterMethod = CONSTANT_SUPPLIER_METHOD;

    @Benchmark
    public void testDirectConstant(Blackhole blackhole) {
        blackhole.consume(CONSTANT);
    }

    @Benchmark
    public void testDirectStatic(Blackhole blackhole) {
        blackhole.consume(notConstant);
    }

    @Benchmark
    public void testDirectVolatile(Blackhole blackhole) {
        blackhole.consume(definitelyNotConstant);
    }

    @Benchmark
    public void testHandleConstant(Blackhole blackhole) throws Throwable {
        blackhole.consume((int) CONSTANT_GETTER.invokeExact());
    }

    @Benchmark
    public void testHandleConstantSupplier(Blackhole blackhole) {
        blackhole.consume(CONSTANT_SUPPLIER.getAsInt());
    }

    @Benchmark
    public void testHandleMethod(Blackhole blackhole) throws Throwable {
        blackhole.consume((int) CONSTANT_METHOD.invokeExact());
    }

    @Benchmark
    public void testHandleMethodSupplier(Blackhole blackhole) {
        blackhole.consume(CONSTANT_SUPPLIER_METHOD.getAsInt());
    }

    @Benchmark
    public void testHandleInstanceMethod(Blackhole blackhole) throws Throwable {
        blackhole.consume((int) finalGetter.invokeExact());
    }

    @Benchmark
    public void testHandleInstanceMethodSupplier(Blackhole blackhole) {
        blackhole.consume(finalGetterMethod.getAsInt());
    }

    @Benchmark
    public void testAlmostFinalHandle(Blackhole blackhole) throws Throwable {
        blackhole.consume((int) GETTER_HANDLE.invokeExact());
    }

    @Benchmark
    public void testAlmostFinalSupplier(Blackhole blackhole) {
        blackhole.consume(GETTER_SUPPLIER.getAsInt());
    }

    @Benchmark
    public void testReturn2(Blackhole blackhole) {
        blackhole.consume(RETURN_2.getAsInt());
    }

    @Benchmark
    public void testReturn5(Blackhole blackhole) {
        blackhole.consume(RETURN_5.getAsInt());
    }
}
