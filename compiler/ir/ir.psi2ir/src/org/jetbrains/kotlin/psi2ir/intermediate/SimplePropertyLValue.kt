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

package org.jetbrains.kotlin.psi2ir.intermediate

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.psi2ir.generators.Scope
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.utils.SmartList

class SimplePropertyLValue(
        val scope: Scope,
        val startOffset: Int,
        val endOffset: Int,
        val irOperator: IrOperator?,
        val descriptor: PropertyDescriptor,
        val callReceiver: CallReceiver,
        val superQualifier: ClassDescriptor?
) : LValue, AssignmentReceiver {
    override val type: KotlinType get() = descriptor.type

    override fun load(): IrExpression {
        val getter = descriptor.getter ?: throw AssertionError("No getter for $descriptor")
        return callReceiver.call { dispatchReceiverValue, extensionReceiverValue ->
            IrGetterCallImpl(startOffset, endOffset, getter,
                             dispatchReceiverValue?.load(),
                             extensionReceiverValue?.load(),
                             irOperator,
                             superQualifier)
        }
    }

    override fun store(irExpression: IrExpression): IrExpression {
        val setter = descriptor.setter ?: throw AssertionError("No setter for $descriptor")
        return callReceiver.call { dispatchReceiverValue, extensionReceiverValue ->
            IrSetterCallImpl(startOffset, endOffset, setter,
                             dispatchReceiverValue?.load(),
                             extensionReceiverValue?.load(),
                             irExpression,
                             irOperator,
                             superQualifier)
        }
    }

    override fun assign(withLValue: (LValue) -> IrExpression): IrExpression {
        return callReceiver.call { dispatchReceiverValue, extensionReceiverValue ->
            val variablesForReceivers = SmartList<IrVariable>()

            val irResultExpression = withLValue(
                    SimplePropertyLValue(scope, startOffset, endOffset, irOperator, descriptor,
                                         SimpleCallReceiver(dispatchReceiverValue, extensionReceiverValue),
                                         superQualifier)
            )

            if (variablesForReceivers.isEmpty()) {
                irResultExpression
            }
            else {
                val irBlock = IrBlockImpl(startOffset, endOffset, irResultExpression.type, irOperator)
                irBlock.addAll(variablesForReceivers)
                irBlock.addStatement(irResultExpression)
                irBlock
            }
        }

    }
}