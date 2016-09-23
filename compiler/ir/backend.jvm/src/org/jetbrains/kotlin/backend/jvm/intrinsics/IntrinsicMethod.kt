/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.backend.jvm.intrinsics

import org.jetbrains.kotlin.backend.jvm.JvmBackendContext
import org.jetbrains.kotlin.codegen.Callable
import org.jetbrains.kotlin.codegen.CallableMethod
import org.jetbrains.kotlin.codegen.ExpressionCodegen
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodSignature
import org.jetbrains.org.objectweb.asm.Type

abstract class IntrinsicMethod {

    open fun toCallable(
            expression: IrMemberAccessExpression,
            signature: JvmMethodSignature,
            context: JvmBackendContext
    ): IrIntrinsicFunction {
        TODO()
    }


    open fun toCallable(
            fd: FunctionDescriptor,
            isSuper: Boolean,
            resolvedCall: ResolvedCall<*>,
            codegen: ExpressionCodegen
    ): Callable {
        throw UnsupportedOperationException("Not implemented")
    }

    open  fun toCallable(method: CallableMethod): Callable {
        throw UnsupportedOperationException("Not implemented")
    }


    companion object {
        fun calcReceiverType(call: IrMemberAccessExpression, context: JvmBackendContext): Type {
            return context.state.typeMapper.mapType(call.dispatchReceiver?.type ?: call.extensionReceiver!!.type)
        }

        fun expressionType(expression: IrExpression, context: JvmBackendContext): Type {
            return context.state.typeMapper.mapType(expression.type)
        }
    }
}