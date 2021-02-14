package cn.misection.cvac.ast.expr;

/**
 * @author Military Intelligence 6 root
 * @version 1.0.0
 * @ClassName CvaExpr
 * @Description TODO
 * @CreateTime 2021年02月14日 17:53:00
 */
public abstract class AbstractExpression implements IExpression
{
    protected int lineNum;

    protected AbstractExpression(int lineNum)
    {
        this.lineNum = lineNum;
    }

    public int getLineNum()
    {
        return lineNum;
    }
}
