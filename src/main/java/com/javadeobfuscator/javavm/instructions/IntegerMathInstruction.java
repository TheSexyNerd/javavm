package com.javadeobfuscator.javavm.instructions;

import com.javadeobfuscator.javavm.Locals;
import com.javadeobfuscator.javavm.MethodExecution;
import com.javadeobfuscator.javavm.Stack;
import com.javadeobfuscator.javavm.exceptions.ExecutionException;
import com.javadeobfuscator.javavm.internals.VMSymbols;
import com.javadeobfuscator.javavm.utils.BiIntegerFunction;
import com.javadeobfuscator.javavm.utils.ExecutionUtils;
import com.javadeobfuscator.javavm.values.JavaUnknown;
import com.javadeobfuscator.javavm.values.JavaValue;
import com.javadeobfuscator.javavm.values.JavaValueType;
import com.javadeobfuscator.javavm.values.JavaWrapper;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.List;

public class IntegerMathInstruction extends Instruction {
    private final BiIntegerFunction _function;
    private final boolean division;

    public IntegerMathInstruction(BiIntegerFunction function) {
        this._function = function;
        this.division = false;
    }

    public IntegerMathInstruction(BiIntegerFunction function, boolean division) {
        this._function = function;
        this.division = division;
    }

    @Override
    public void execute(MethodExecution execution, AbstractInsnNode currentInsn, Stack stack, Locals locals, List<AbstractInsnNode> branchTo) {
        JavaValue b = stack.pop().get();
        JavaValue a = stack.pop().get();

        if (ExecutionUtils.areValuesUnknown(a, b)) {
            stack.push(JavaWrapper.wrap(new JavaUnknown(execution.getVM(), execution.getVM().INTEGER, JavaUnknown.UnknownCause.INTEGER_MATH, b, a)));
            return;
        }

        if (!a.is(JavaValueType.INTEGER) || !b.is(JavaValueType.INTEGER)) {
            throw new ExecutionException("Expected to find integer on stack");
        }

        if (division && b.asInt() == 0) {
            throw execution.getVM().newThrowable(VMSymbols.java_lang_ArithmeticException, "/ by zero");
        }

        stack.push(JavaWrapper.createInteger(execution.getVM(), _function.apply(a.asInt(), b.asInt())));
    }
}
