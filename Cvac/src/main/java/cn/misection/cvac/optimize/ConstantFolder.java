package cn.misection.cvac.optimize;

import cn.misection.cvac.ast.IVisitor;
import cn.misection.cvac.ast.clas.*;
import cn.misection.cvac.ast.decl.*;
import cn.misection.cvac.ast.entry.*;
import cn.misection.cvac.ast.expr.*;
import cn.misection.cvac.ast.method.*;
import cn.misection.cvac.ast.program.*;
import cn.misection.cvac.ast.statement.*;
import cn.misection.cvac.ast.type.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by MI6 root 1/23.
 */
public final class ConstantFolder
        implements IVisitor, Optimizable
{
    private AbstractExpression lastExp;
    private boolean isOptimizing;

    private boolean isConstant()
    {
        return lastExp != null && this.isConstant(this.lastExp);
    }

    private boolean isConstant(AbstractExpression exp)
    {
        return exp instanceof CvaNumberIntExpr
                || exp instanceof CvaTrueExpr
                || exp instanceof CvaFalseExpr;
    }

    @Override
    public void visit(CvaBoolean t) {}

    @Override
    public void visit(CvaClassType t) {}

    @Override
    public void visit(CvaInt t) {}

    @Override
    public void visit(CvaString type) {}

    @Override
    public void visit(CvaDeclaration d) {}

    @Override
    public void visit(CvaAddExpr e)
    {
        this.visit(e.getLeft());
        if (isConstant())
        {
            CvaNumberIntExpr temLeft = (CvaNumberIntExpr) this.lastExp;
            this.visit(e.getRight());
            if (isConstant())
            {
                this.isOptimizing = true;
                this.lastExp = new CvaNumberIntExpr(
                        temLeft.getValue() + ((CvaNumberIntExpr) this.lastExp).getValue(),
                        this.lastExp.getLineNum());
            }
            else
            {
                this.lastExp = new CvaAddExpr(
                        this.lastExp.getLineNum(),
                        temLeft,
                        this.lastExp);
            }
        }
        else
        {
            this.lastExp = e;
        }
    }

    @Override
    public void visit(CvaAndAndExpr e)
    {
        this.visit(e.getLeft());
        AbstractExpression temLeft = this.lastExp;
        this.visit(e.getRight());
        AbstractExpression temRight = this.lastExp;

        this.isOptimizing = true;
        if (temLeft instanceof CvaFalseExpr
                || temRight instanceof CvaFalseExpr)
        {
            this.lastExp = new CvaFalseExpr(e.getLineNum());
        }
        else if (temLeft instanceof CvaTrueExpr
                && temRight instanceof CvaTrueExpr)
        {
            this.lastExp = temLeft;
        }
        else if (temLeft instanceof CvaTrueExpr)
        {
            this.lastExp = temRight;
        }
        else if (temRight instanceof CvaTrueExpr)
        {
            this.lastExp = temLeft;
        }
        else
        {
            this.isOptimizing = false;
            this.lastExp = e;
        }
    }

    @Override
    public void visit(CvaCallExpr e)
    {
        List<AbstractExpression> argList = new LinkedList<>();
        e.getArgs().forEach(arg ->
        {
            this.visit(arg);
            argList.add(this.lastExp);
        });
        e.setArgs(argList);
        this.lastExp = e;
    }

    @Override
    public void visit(CvaFalseExpr e)
    {
        this.lastExp = e;
    }

    @Override
    public void visit(CvaIdentifier e)
    {
        this.lastExp = e;
    }

    @Override
    public void visit(CvaLessThanExpr e)
    {
        this.visit(e.getLeft());
        if (isConstant())
        {
            CvaNumberIntExpr temLeft = (CvaNumberIntExpr) this.lastExp;
            this.visit(e.getRight());
            if (isConstant())
            {
                this.isOptimizing = true;
                this.lastExp = temLeft.getValue() < ((CvaNumberIntExpr) this.lastExp).getValue()
                        ? new CvaTrueExpr(this.lastExp.getLineNum())
                        : new CvaFalseExpr(this.lastExp.getLineNum());
            }
            else
            {
                this.lastExp = new CvaLessThanExpr(
                        this.lastExp.getLineNum(),
                        temLeft,
                        this.lastExp);
            }
        }
        else
        {
            this.lastExp = e;
        }
    }

    @Override
    public void visit(CvaNewExpr e)
    {
        this.lastExp = e;
    }

    @Override
    public void visit(CvaNegateExpr e)
    {
        this.visit(e.getExpr());
        if (isConstant())
        {
            this.isOptimizing = true;
            this.lastExp = this.lastExp instanceof CvaTrueExpr
                    ? new CvaFalseExpr(this.lastExp.getLineNum())
                    : new CvaTrueExpr(this.lastExp.getLineNum());
        }
        else
        {
            this.lastExp = e;
        }
    }

    @Override
    public void visit(CvaNumberIntExpr e)
    {
        this.lastExp = e;
    }

    @Override
    public void visit(CvaSubExpr e)
    {
        this.visit(e.getLeft());
        if (isConstant())
        {
            CvaNumberIntExpr temLeft = (CvaNumberIntExpr) this.lastExp;
            this.visit(e.getRight());
            if (isConstant())
            {
                this.isOptimizing = true;
                this.lastExp = new CvaNumberIntExpr(
                        temLeft.getValue() - ((CvaNumberIntExpr) this.lastExp).getValue(),
                        this.lastExp.getLineNum());
            }
            else
            {
                this.lastExp = new CvaSubExpr(
                        this.lastExp.getLineNum(),
                        temLeft,
                        this.lastExp);
            }
        }
        else
        {
            this.lastExp = e;
        }
    }

    @Override
    public void visit(CvaThisExpr e)
    {
        this.lastExp = e;
    }

    @Override
    public void visit(CvaMuliExpr e)
    {
        this.visit(e.getLeft());
        if (isConstant())
        {
            CvaNumberIntExpr temLeft = (CvaNumberIntExpr) this.lastExp;
            this.visit(e.getRight());
            if (isConstant())
            {
                this.isOptimizing = true;
                this.lastExp = new CvaNumberIntExpr(
                        temLeft.getValue() * ((CvaNumberIntExpr) this.lastExp).getValue(),
                        this.lastExp.getLineNum());
            }
            else
            {
                this.lastExp = new CvaMuliExpr(
                        this.lastExp.getLineNum(),
                        temLeft,
                        this.lastExp);
            }
        }
        else
        {
            this.lastExp = e;
        }
    }

    @Override
    public void visit(CvaTrueExpr e)
    {
        this.lastExp = e;
    }

    @Override
    public void visit(CvaAssign s)
    {
        this.visit(s.getExpr());
        s.setExpr(this.lastExp);
    }

    @Override
    public void visit(CvaBlock s)
    {
        s.getStatementList().forEach(this::visit);
    }

    @Override
    public void visit(CvaIfStatement s)
    {
        this.visit(s.getCondition());
        s.setCondition(this.lastExp);
        this.visit(s.getThenStatement());
        if (s.getElseStatement() != null)
        {
            this.visit(s.getElseStatement());
        }
    }

    @Override
    public void visit(CvaWriteOperation s)
    {
        this.visit(s.getExpr());
        s.setExpr(this.lastExp);
    }

    @Override
    public void visit(CvaWhileStatement s)
    {
        this.visit(s.getCondition());
        s.setCondition(this.lastExp);
        this.visit(s.getBody());
    }

    @Override
    public void visit(CvaMethod cvaMethod)
    {
        cvaMethod.getStatementList().forEach(this::visit);
        this.visit(cvaMethod.getRetExpr());
        cvaMethod.setRetExpr(this.lastExp);
    }

    @Override
    public void visit(CvaClass cvaClass)
    {
        cvaClass.getMethodList().forEach(this::visit);
    }

    @Override
    public void visit(CvaEntry c)
    {
        this.visit(c.getStatement());
    }

    @Override
    public void visit(CvaProgram p)
    {
        this.isOptimizing = false;
        this.visit(p.getEntry());
        p.getClassList().forEach(this::visit);
    }

    @Override
    public boolean isOptimizing()
    {
        return this.isOptimizing;
    }
}
