package de.variantsync.evolution.util.functional;

import de.variantsync.evolution.util.functional.interfaces.FragileProcedure;
import de.variantsync.evolution.util.functional.interfaces.FragileSupplier;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class Result<SuccessType, FailureType> {
    private final SuccessType result;
    private final FailureType failure;

    protected Result(SuccessType result, FailureType failure) {
        this.result = result;
        this.failure = failure;
    }

    public static <S, F> Result<S, F> Success(S s) {
        return new Result<>(s, null);
    }

    public static <S, F> Result<S, F> Failure(F f) {
        return new Result<>(null, f);
    }

    public <S2> Result<S2, FailureType> map(Function<SuccessType, S2> successCase) {
        return bimap(successCase, Function.identity());
    }

    public <S2, F2> Result<S2, F2> bimap(Function<SuccessType, S2> successCase, Function<FailureType, F2> failureCase) {
        if (result != null) {
            return Success(successCase.apply(result));
        } else {
            return Failure(failureCase.apply(failure));
        }
    }

    public boolean isSuccess() {
        return result != null;
    }

    public boolean isFailure() {
        return failure != null;
    }

    public SuccessType getSuccess() {
        return result;
    }

    public FailureType getFailure() {
        return failure;
    }

    @SuppressWarnings("unchecked")
    public static <S, E extends Exception> Result<S, E> Try(FragileSupplier<S, E> s) {
        try {
            final S result = s.get();
            return Result.Success(result);
        } catch (Exception e) { // We cannot catch E directly.
//            throw new RuntimeException(e);
            return Result.Failure((E) e);
        }
    }

    public static <E extends Exception> Result<Unit, E> Try(FragileProcedure<E> s) {
        return Try(Functional.LiftFragile(s));
    }
}
