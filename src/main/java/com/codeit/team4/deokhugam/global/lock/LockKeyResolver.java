package com.codeit.team4.deokhugam.global.lock;

import java.util.StringJoiner;
import org.springframework.stereotype.Component;

@Component
public class LockKeyResolver {

    private static final int SPLIT_LIMIT = 2;

    public String resolve(String key, String[] lockParams, String[] paramNames, Object[] args) {
        if (lockParams.length == 0) {
            return key;
        }

        StringJoiner joiner = new StringJoiner(":");
        joiner.add(key);

        for (String lockParam : lockParams) {
            Object value = resolveValue(lockParam, paramNames, args);
            joiner.add(String.valueOf(value));
        }

        return joiner.toString();
    }

    private Object resolveValue(String expression, String[] names, Object[] args) {
        String[] parts = expression.split("\\.", SPLIT_LIMIT);
        String paramName = parts[0];

        Object value = findArgByName(paramName, names, args);

        if (parts.length == SPLIT_LIMIT) {
            value = getNestedValue(value, parts[1], expression);
        }

        if (value == null) {
            throw new DistributedLockException("lockParam '" + expression + "' 값이 null입니다");
        }

        return value;
    }

    private Object findArgByName(String paramName, String[] names, Object[] args) {
        if (names == null) {
            throw new DistributedLockException("lockParam '" + paramName + "'에 해당하는 파라미터를 찾을 수 없습니다");
        }

        for (int i = 0; i < names.length; i++) {
            if (paramName.equals(names[i])) {
                return args[i];
            }
        }

        throw new DistributedLockException("lockParam '" + paramName + "'에 해당하는 파라미터를 찾을 수 없습니다");
    }

    private Object getNestedValue(Object target, String fieldPath, String expression) {
        if (target == null) {
            throw new DistributedLockException("lockParam '" + expression + "' 값이 null입니다");
        }

        try {
            return target.getClass().getMethod(fieldPath).invoke(target);
        } catch (Exception e) {
            throw new DistributedLockException(
                    "lockParam '" + expression + "'의 필드에 접근할 수 없습니다", e);
        }
    }
}
