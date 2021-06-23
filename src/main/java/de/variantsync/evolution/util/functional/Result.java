package de.variantsync.evolution.util.functional;

import de.variantsync.evolution.util.Logger;
import de.variantsync.evolution.util.functional.interfaces.FragileProcedure;
import de.variantsync.evolution.util.functional.interfaces.FragileSupplier;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Type to capture results of computations that might fail.
 * A result reflects either the return valure of a successful computation or a failure state.
 * @param <SuccessType> Type for values in case of success.
 * @param <FailureType> Type for values in case of failure.
 */
public class Result<SuccessType, FailureType> {
    public static boolean HARD_CRASH_ON_TRY = true;

    private final SuccessType result;
    private final FailureType failure;

    /**
     * Creates a new Result where exactly one of the arguments is excepted to be null.
     * @param result Success value or null
     * @param failure Failure value or null
     */
    protected Result(SuccessType result, FailureType failure) {
        this.result = result;
        this.failure = failure;
    }

    /// Constructors

    /**
     * Creates a successful result with the given value.
     * @param s Return value of the result.
     * @return Success value.
     */
    public static <S, F> Result<S, F> Success(S s) {
        return new Result<>(s, null);
    }


    /**
     * Creates a failed result with the given error value.
     * @param f Value indicating failure.
     * @return Failure result.
     */
    public static <S, F> Result<S, F> Failure(F f) {
        return new Result<>(null, f);
    }

    /**
     * Runs the given computation that indicates success by returning a boolean.
     * Running f will be interpreted as a success iff f returns true.
     * Running f will be interpreted as a failure iff f returns false.
     * In case of failure, a failure value will be produced with the given failure supplier.
     * @param f Computation to run that indicates success with a boolean return value.
     * @param failure Factory for failure message in case f returned false.
     * @return Success iff f returned true, Failure otherwise.
     */
    public static <F> Result<Unit, F> FromFlag(Supplier<Boolean> f, Supplier<F> failure) {
        if (f.get()) {
            return Success(Unit.Instance());
        } else {
            return Failure(failure.get());
        }
    }

    /**
     * Runs the given computation that indicates success by returning a boolean and that may throw an exception.
     * Running f will be interpreted as a success iff f returns true and throws no exception.
     * Running f will be interpreted as a failure iff f returns false or throws an exception.
     * If f did not throw an exception but returned false, a failure value will be produced with the given failure supplier.
     * @param f Computation to run that indicates success with a boolean return value or an exception.
     * @param failure Factory for failure message in case f returned false.
     * @return Success iff f returned true and did not throw an exception, Failure otherwise.
     */
    public static <E extends Exception> Result<Unit, E> FromFlag(FragileSupplier<Boolean, E> f, Supplier<E> failure) {
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

    /**
     * Runs the given computation that may throw an exception.
     * @param s Computation to run.
     * @param <E> The type of exception that may be thrown by s.
     * @return A result containing the result of the given computation or the exception in case it was thrown.
     */
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


    /**
     * Runs the given computation that may throw an exception.
     * @param s Computation to run.
     * @param <E> The type of exception that may be thrown by s.
     * @return A result containing the result of the given computation or the exception in case it was thrown.
     */
    public static <E extends Exception> Result<Unit, E> Try(FragileProcedure<E> s) {
        return Try(Functional.LiftFragile(s));
    }

    /// Operations

    /**
     * Map over success type.
     */
    public <S2> Result<S2, FailureType> map(Function<SuccessType, S2> successCase) {
        return bimap(successCase, Function.identity());
    }

    /**
     * Map over failure type.
     */
    public <F2> Result<SuccessType, F2> mapFail(Function<FailureType, F2> failureCase) {
        return bimap(Function.identity(), failureCase);
    }

    /**
     * Result is a bifunctor.
     */
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

    /**
     * Combines two results over monoidal values.
     * Returns failure if at least one of the given results is a failure.
     */
    public static <S extends Monoid<S>, F extends Monoid<F>> Result<S, F> mappend(Result<S, F> a, Result<S, F> b) {
        final Result<S, F> prec = a.isFailure() ? a : b;
        final Result<S, F> other = a.isFailure() ? b : a;
        return prec.bimap(
                s -> other.isSuccess() ? s.mAppend(other.getSuccess()) : s,
                f -> other.isFailure() ? f.mAppend(other.getFailure()) : f
        );
    }
}
