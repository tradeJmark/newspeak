package ca.tradejmark.newspeak

import ca.tradejmark.newspeak.NSValue.*
import ca.tradejmark.newspeak.NSValue.NSNum.*
import ca.tradejmark.newspeak.error.*
import org.antlr.v4.runtime.tree.TerminalNode
import kotlin.reflect.KClass

class NSEvaluator: NewspeakBaseVisitor<NSValue>() {
    var scope: NSScope = NSScope()

    fun pushScope() {
        scope = NSScope(scope)
    }

    fun popScope() {
        scope = scope.parent!!
    }

    fun visitBooleanExpression(ctx: NewspeakParser.ExpressionContext): NSBool {
        return visitExpSafe("boolean", NSBool::class, ctx)
    }

    fun visitListExpression(ctx: NewspeakParser.ExpressionContext): NSList {
        return visitExpSafe("list", NSList::class, ctx)
    }

    fun visitIntExpression(ctx: NewspeakParser.ExpressionContext): NSInt {
        return visitExpSafe("integer", NSInt::class, ctx)
    }

    fun visitFuncExpression(ctx: NewspeakParser.ExpressionContext): NSFunc {
        return visitExpSafe("function", NSFunc::class, ctx)
    }

    private fun <T : NSValue> visitExpSafe(tName: String, c: KClass<T>, ctx: NewspeakParser.ExpressionContext): T {
        val value = visitExpression(ctx)
        if (!c.isInstance(value)) throw WrongTypeError(ctx.text, tName)
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    override fun visitExpression(ctx: NewspeakParser.ExpressionContext): NSValue {
        ctx.INT()?.text?.let { return NSInt(it.toInt()) }
        ctx.REAL()?.text?.let { return NSReal(it.toDouble()) }
        ctx.ID()?.text?.let { return scope[it] }
        ctx.STRING()?.text?.let { return NSString(it.substring(1, it.length - 1)) }
        ctx.bool()?.let { return visitBool(it) }
        ctx.list()?.let { return visitList(it) }
        ctx.func()?.let { return visitFunc(it) }
        ctx.fres()?.let { return visitFres(it) }
        ctx.subex?.let { return visitExpression(it) }
        ctx.bop?.let { bop ->
            val a = visitExpression(ctx.expression(0))
            val b = visitExpression(ctx.expression(1))
            if (bop == ctx.EQS()?.symbol || bop == ctx.EQUALS()?.symbol) {
                return NSBool.fromBool(a == b)
            }
            if (bop == ctx.NEQUALS()?.symbol) {
                return NSBool.fromBool(a != b)
            }
            return when (a) {
                is NSNum -> {
                    if (b !is NSNum) throw MismatchedOperandsError(ctx.text)
                    when (bop) {
                        ctx.PLUS()?.symbol -> a + b
                        ctx.MINUS()?.symbol -> a - b
                        ctx.TIMES()?.symbol -> a * b
                        ctx.DIV()?.symbol -> a / b
                        ctx.POWER()?.symbol -> a.pow(b)
                        ctx.GT()?.symbol -> NSBool.fromBool(a > b)
                        ctx.LT()?.symbol -> NSBool.fromBool(a < b)
                        ctx.GTE()?.symbol -> NSBool.fromBool(a >= b)
                        ctx.LTE()?.symbol -> NSBool.fromBool(a <= b)
                        else -> throw UnsupportedOperatorError("number", bop.text)
                    }
                }
                is NSString -> {
                    if (b !is NSString) throw MismatchedOperandsError(ctx.text)
                    when(bop) {
                        ctx.PLUS()?.symbol -> NSString(a.value + b.value)
                        else -> throw UnsupportedOperatorError("string", bop.text)
                    }
                }
                is NSBool -> throw UnsupportedOperatorError("boolean", bop.text)
                is NSList ->
                    if (bop == ctx.POS()?.symbol) {
                        if (b !is NSInt) throw UnsupportedOperatorError(b.typeName, bop.text)
                        val ind = b.value
                        return a[ind]
                    }
                    else throw UnsupportedOperatorError("list", bop.text)
                is NSFunc -> throw UnsupportedOperatorError(a.typeName, bop.text)
            }
        }
        throw ParseError(ctx.text)
    }

    override fun visitBool(ctx: NewspeakParser.BoolContext): NSBool {
        ctx.TRUE()?.let { return NSBool.NSTrue }
        ctx.FALSE()?.let { return NSBool.NSFalse }
        throw ParseError(ctx.text)
    }

    override fun visitList(ctx: NewspeakParser.ListContext): NSList {
        val l = NSList()
        ctx.expression()
            .map(::visitExpression)
            .forEach(l::add)
        return l
    }

    override fun visitFunc(ctx: NewspeakParser.FuncContext): NSFunc {
        return NSFunc(ctx.ID().map { it.text }, ctx.block())
    }
}