package de.variantsync.evolution.util.functional;

import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.functional.interfaces.FragileProcedure;
import de.variantsync.evolution.util.functional.interfaces.FragileSupplier;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Result<SuccessType, FailureType> {
    public static boolean HARD_CRASH_ON_TRY = true;

    private final SuccessType result;
    private final FailureType failure;

    protected Result(SuccessType result, FailureType failure) {
        this.result = result;
        this.failure = failure;
    }

    /// Constructors

    public static <S, F> Result<S, F> Success(S s) {
        return new Result<>(s, null);
    }

    public static <S, F> Result<S, F> Failure(F f) {
        return new Result<>(null, f);
    }

    public static <F> Result<Unit, F> FromSuccessReturningProcedure(Supplier<Boolean> f, Supplier<F> failure) {
        if (f.get()) {
            return Success(Unit.Instance());
        } else {
            return Failure(failure.get());
        }
    }

    public static <E extends Exception> Result<Unit, E> FromSuccessReturningProcedure(FragileSupplier<Boolean, E> f, Supplier<E> failure) {
        final Result<Boolean, E> r = Try(f);
        if (r.isSuccess()) {
            if (r.getSuccess()) {
                return Success(Unit.Instance());
            } else {
                return Failure(failure.get());
            }
        } else {
            return Failure(r.getFailure());
        }
    }

    @SuppressWarnings("unchecked")
    public static <S, E extends Exception> Result<S, E> Try(FragileSupplier<S, E> s) {
        try {
            final S result = s.get();
            return Result.Success(result);
        } catch (Exception e) { // We cannot catch E directly.
            if (HARD_CRASH_ON_TRY) {
                throw new RuntimeException(e);
            } else {
                return Result.Failure((E) e);
            }
        }
    }

    public static <E extends Exception> Result<Unit, E> Try(FragileProcedure<E> s) {
        return Try(Functional.LiftFragile(s));
    }

    /// Operations

    public <S2> Result<S2, FailureType> map(Function<SuccessType, S2> successCase) {
        return bimap(successCase, Function.identity());
    }

    public <F2> Result<SuccessType, F2> mapFail(Function<FailureType, F2> failureCase) {
        return bimap(Function.identity(), failureCase);
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

    public void assertSuccess() {
        if (isFailure()) {
            Logger.error(getFailure().toString());
        }
        assert isSuccess();
    }

    public void ifSuccess(Consumer<SuccessType> f) {
        if (isSuccess()) {
            f.accept(getSuccess());
        }
    }

    public void ifFailure(Consumer<FailureType> f) {
        if (isFailure()) {
            f.accept(getFailure());
        }
    }

    public static <S extends Monoid<S>, F extends Monoid<F>> Result<S, F> mappend(Result<S, F> a, Result<S, F> b) {
        final Result<S, F> prec = a.isFailure() ? a : b;
        final Result<S, F> other = a.isFailure() ? b : a;
        return prec.bimap(
                s -> other.isSuccess() ? s.mappend(other.getSuccess()) : s,
                f -> other.isFailure() ? f.mappend(other.getFailure()) : f
        );
    }
}
