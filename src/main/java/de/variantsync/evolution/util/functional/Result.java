package de.variantsync.evolution.util.functional;

import java.util.function.Function;

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
}
