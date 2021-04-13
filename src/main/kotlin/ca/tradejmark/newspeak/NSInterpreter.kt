package ca.tradejmark.newspeak

import ca.tradejmark.newspeak.NewspeakParser.*
import ca.tradejmark.newspeak.error.WrongTypeError
import org.antlr.v4.runtime.tree.TerminalNode
import kotlin.system.exitProcess
import ca.tradejmark.newspeak.NSValue.*
import ca.tradejmark.newspeak.NSValue.NSNum.*
import ca.tradejmark.newspeak.error.BadParameterError
import ca.tradejmark.newspeak.error.NoResultError
import ca.tradejmark.newspeak.error.ParseError
import java.util.*

class NSInterpreter(val stdIn: Scanner, private val debug: Boolean = false): NewspeakBaseVisitor<Unit>() {
    val eval = NSEvaluator()
    private var breakFlag = false

    override fun visitAssignment(ctx: AssignmentContext) {
        if (debug) println("Visiting ass: ${ctx.text}")
        doAssignment(ctx.idOrPos(), ctx.expression())
    }

    override fun visitSet(ctx: SetContext) {
        if (debug) println("Visiting set: ${ctx.text}")
        doAssignment(ctx.idOrPos(), ctx.expression())
    }

    fun doRegularAssignment(id: TerminalNode, v: NSValue) {
        eval.scope[id.text] = v
    }

    private fun doAssignment(id: IdOrPosContext, v: NSValue) {
        if (id.ID() != null) {
            doRegularAssignment(id.ID(), v)
        }
        else {
            if (id.expression().bop != id.expression().POS().symbol) throw ParseError("Cannot assign into expression ${id.expression().text}.")
            val l = eval.visitListExpression(id.expression().expression(0))
            val ind = eval.visitIntExpression(id.expression().expression(1)).value
            l[ind] = v
        }
    }

    private fun doAssignment(id: IdOrPosContext, expr: ExpressionContext) {
        val v = resolveExpression(expr)
        doAssignment(id, v)
    }

    override fun visitPrint(ctx: PrintContext) {
        if (debug) println("Visiting print: ${ctx.text}")
        val value = resolveExpression(ctx.expression())
        println(value.toString())
    }

    override fun visitRead(ctx: ReadContext) {
        val v =
            if (ctx.type() != null) {
                val out = when (ctx.type().text.toLowerCase()) {
                    "integer" -> NSInt(stdIn.nextInt())
                    "real number" -> NSReal(stdIn.nextDouble())
                    "boolean" -> NSBool.fromBool(stdIn.nextBoolean())
                    else -> throw ParseError("${ctx.type().text} is not a valid type for reading.")
                }
                stdIn.nextLine()
                out
            }
            else {
                NSString(stdIn.nextLine())
            }
        doRegularAssignment(ctx.ID(), v)
    }

    override fun visitAdd(ctx: AddContext) {
        val l = eval.visitListExpression(ctx.expression(1))
        val v = resolveExpression(ctx.expression(0))
        l.add(v)
    }

    override fun visitWhl(ctx: WhlContext) {
        if (debug) println("Visiting whl: ${ctx.text}")
        while (eval.visitBooleanExpression(ctx.expression()) is NSBool.NSTrue) {
            if (breakFlag) {
                breakFlag = false
                break
            }
            subscoped {
                ctx.block()?.let { visitBlock(it) }
                ctx.sent()?.let { visitSent(it) }
            }
        }
    }

    override fun visitIff(ctx: IffContext) {
        if (debug) println("Visiting if: ${ctx.text}")
        if (eval.visitBooleanExpression(ctx.expression()) is NSBool.NSTrue) {
            subscoped {
                ctx.block()?.let { visitBlock(it) }
                ctx.sent()?.let { visitSent(it) }
            }
        }
        else if(ctx.elsee() != null) {
            visitElsee(ctx.elsee())
        }
    }

    override fun visitElsee(ctx: ElseeContext) {
        subscoped {
            ctx.block()?.let { visitBlock(it) }
            ctx.sent()?.let { visitSent(it) }
        }
    }

    override fun visitForeach(ctx: ForeachContext) {
        if (debug) println("Visiting foreach: ${ctx.text}")
        val list = eval.visitListExpression(ctx.expression())
        for (nsv in list) {
            if (breakFlag) {
                breakFlag = false
                break
            }
            subscoped {
                doRegularAssignment(ctx.ID(), nsv)
                ctx.block()?.let { visitBlock(it) }
                ctx.sent()?.let { visitSent(it) }
            }
        }
    }

    override fun visitBlock(ctx: BlockContext) {
        if (debug) println("Visiting block: ${ctx.text}")
        for (line in ctx.line()) {
            if (line?.command()?.exit() != null) {
                breakFlag = true
                break
            }
            else visitLine(line)
        }
    }

    override fun visitSent(ctx: SentContext) {
        if (debug) println("Visiting sentence: ${ctx.text}")
        visitLine(ctx.line())
    }

    override fun visitExit(ctx: ExitContext) {
        if (debug) println("Visiting exit: ${ctx.text}")
        exitProcess(0)
    }

    override fun visitIncdec(ctx: IncdecContext) {
        if (debug) println("Visiting incdec: ${ctx.text}")
        val id = ctx.ID().text
        val v = eval.scope[id]
        if (v is NSInt) {
            ctx.INC()?.let { eval.scope[id] = NSInt(v.value + 1) }
            ctx.DEC()?.let { eval.scope[id] = NSInt(v.value - 1) }
        }
        else {
            throw WrongTypeError(id, "integer")
        }
    }

    fun resolveExpression(ctx: ExpressionContext): NSValue {
        return if (ctx.fres() != null) resolveFunction(ctx.fres())
        else eval.visitExpression(ctx)
    }

    fun resolveFunction(ctx: FresContext): NSValue {
        val f = eval.visitFuncExpression(ctx.expression(0))
        return executeFunction(
            f,
            ctx.ID().map(TerminalNode::getText),
            ctx.expression().drop(1).map(::resolveExpression)
        ) ?: throw NoResultError()
    }

    fun executeFunction(f: NSFunc, params: List<String>, args: List<NSValue>): NSValue? {
        if (f.params.size != args.size) throw BadParameterError(f.params.size, args.size)
        eval.pushScope()
        for (i in params.indices) {
            val param = params[i]
            if (!f.params.contains(param)) throw BadParameterError(param)
            eval.scope.shadow(param, args[i])
        }
        var res: NSValue? = null
        for (line in f.block.line()) {
            if (line?.command()?.exit() != null) {
                break
            }
            else if (line?.special()?.result() != null) {
                res = resolveExpression(line.special().result().expression())
            }
            else visitLine(line)
        }
        eval.popScope()
        return res
    }

    override fun visitRun(ctx: RunContext) {
        val f = eval.visitFuncExpression(ctx.expression(0))
        executeFunction(
            f,
            ctx.ID().map(TerminalNode::getText),
            ctx.expression().drop(1).map(eval::visitExpression)
        )
    }

    override fun visitIdent(ctx: IdentContext) {
        val v = resolveExpression(ctx.expression())
        println(v.show())
    }

    private inline fun subscoped(proc: () -> Unit) {
        eval.pushScope()
        proc()
        eval.popScope()
    }
}