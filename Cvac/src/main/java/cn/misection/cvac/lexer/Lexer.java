package cn.misection.cvac.lexer;

/**
 * @author MI6 root;
 */
public final class Lexer
{
    /**
     * input stream of the file;
     */
    private final IBufferedQueue stream;

    private int lineNum;

    public Lexer(IBufferedQueue stream)
    {
        this.stream = stream;
        this.lineNum = 1;
    }

    public CvaToken nextToken()
    {
        return lex();
    }

    private void errorLog()
    {
        System.err.printf("line %d: unknown error occur!", lineNum);
        System.exit(1);
    }

    private void errorLog(String excepted, String got)
    {
        System.err.printf("line %d: Excepted: %s, but got %s",
                lineNum, excepted, got);
        System.exit(1);
    }

    private CvaToken lex()
    {
        char ch = this.stream.poll();
        // skip all kinds of blanks
        ch = handleWhiteSpace(ch);
        switch (ch)
        {
            case LexerConstPool.EOF:
                return new CvaToken(CvaKind.EOF, lineNum);
            case '+':
                return handlePlus();
            case '-':
                return handleMinus();
            case '*':
                return handleStar();
            case '&':
                return handleAnd();
            case '|':
                return handleOr();
            case '=':
                return handleEqual();
            case '<':
                return handleLessThan();
            case '>':
                return handleMoreThan();
            case '^':
                return handleXOr();
            case '~':
                return handleBitNegate();
            case '/':
                return handleSlash();
            case '%':
                return handlePercent();
            case '\'':
                return handleApostrophe();
            case '"':
                return handleDoubleQuotes();
            default:
                return handleNorPrefOrIdOrNum(ch);
        }
    }

    private static boolean isSpecialCharacter(char ch)
    {
        return CvaKind.containsKind(String.valueOf(ch))
                ||'+' == ch || '-' == ch || '*' == ch || ch == '/'
                || '&' == ch || ch == '|' || ch == '~' || ch == '^'
                || '=' == ch || '<' == ch || ch == '>'
                || ch == '%' || ch == '@' || ch == '#' || ch == '`'
                || ch == '\\' || ch == '"' || ch == '\''
                || ch == LexerConstPool.EOF;
    }

    private static boolean isNumber(String str)
    {
        for (int i = 0; i < str.length(); i++)
        {
            if (str.charAt(i) < '0' || str.charAt(i) > '9')
            {
                return false;
            }
        }
        return true;
    }

    private static boolean isIdentifier(String str)
    {
        // 只接受字母开头, 不接受下划线开头;
        return Character.isAlphabetic(str.charAt(0));
    }

    private CvaToken handlePlus()
    {
        if (stream.hasNext())
        {
            switch (stream.peek())
            {
                case '+':
                {
                    // 截取两个;
                    stream.poll();
                    return new CvaToken(CvaKind.INCREMENT, lineNum);
                }
                case '=':
                {
                    stream.poll();
                    return new CvaToken(CvaKind.ADD_ASSIGN, lineNum);
                }
                default:
                {
                    break;
                }
            }
        }
        return new CvaToken(CvaKind.ADD, lineNum);
    }

    private CvaToken handleMinus()
    {
        if (stream.hasNext())
        {
            switch (stream.peek())
            {
                case '>':
                {
                    stream.poll();
                    return new CvaToken(CvaKind.ARROW, lineNum);
                }
                case '-':
                {
                    stream.poll();
                    return new CvaToken(CvaKind.DECREMENT, lineNum);
                }
                case '=':
                {
                    stream.poll();
                    return new CvaToken(CvaKind.SUB_ASSIGN, lineNum);
                }
                default:
                {
                    // 说明应该不是以上几种;
                    break;
                }
            }
        }
        return new CvaToken(CvaKind.SUB, lineNum);
    }

    private CvaToken handleStar()
    {
        if (stream.hasNext() && stream.peek() == '=')
        {
            stream.poll();
            return new CvaToken(CvaKind.MULTIPLY_ASSIGN, lineNum);
        }
        return new CvaToken(CvaKind.STAR, lineNum);
    }

    private CvaToken handleEqual()
    {
        if (stream.hasNext() && stream.peek() == '=')
        {
            stream.poll();
            return new CvaToken(CvaKind.EQUALS, lineNum);
        }
        return new CvaToken(CvaKind.ASSIGN, lineNum);
    }

    private CvaToken handleAnd()
    {
        if (stream.hasNext())
        {
            switch (stream.peek())
            {
                case '&':
                {
                    stream.poll();
                    return new CvaToken(CvaKind.AND_AND, lineNum);
                }
                case '=':
                {
                    stream.poll();
                    return new CvaToken(CvaKind.AND_ASSIGN, lineNum);
                }
                default:
                {
                    break;
                }
            }
        }

        return new CvaToken(CvaKind.AND, lineNum);
    }

    private CvaToken handleOr()
    {
        if (stream.hasNext())
        {
            switch (stream.peek())
            {
                case '|':
                {
                    stream.poll();
                    return new CvaToken(CvaKind.OR_OR, lineNum);
                }
                case '=':
                {
                    stream.poll();
                    return new CvaToken(CvaKind.OR_ASSIGN, lineNum);
                }
                default:
                {
                    break;
                }
            }
        }

        return new CvaToken(CvaKind.OR, lineNum);
    }

    private CvaToken handleXOr()
    {
        if (stream.hasNext()
                && stream.peek() == '=')
        {
            stream.poll();
            return new CvaToken(CvaKind.XOR_ASSIGN, lineNum);
        }

        return new CvaToken(CvaKind.XOR, lineNum);
    }

    private CvaToken handleSlash()
    {
        if (stream.hasNext())
        {
            switch (stream.peek())
            {
                case '/':
                {
                    handleLineComment();
                    break;
                }
                case '*':
                {
                    handleBlockComment();
                    break;
                }
                case '=':
                {
                    stream.poll();
                    return new CvaToken(CvaKind.DIV_ASSIGN, lineNum);
                }
                default:
                {
                    return new CvaToken(CvaKind.DIV, lineNum);
                }
            }
        }
        // 说明是注释, 继续执行;
        return lex();
    }

    private CvaToken handlePercent()
    {
        if (stream.hasNext()
                && stream.peek() == '=')
        {
            stream.poll();
            return new CvaToken(CvaKind.REMAINDER_ASSIGN, lineNum);
        }

        return new CvaToken(CvaKind.REMAINDER, lineNum);
    }

    private CvaToken handleBitNegate()
    {
        if (stream.hasNext()
                && stream.peek() == '=')
        {
            stream.poll();
            return new CvaToken(CvaKind.BIT_NEGATE_ASSIGN, lineNum);
        }

        return new CvaToken(CvaKind.BIT_NEGATE, lineNum);
    }

    private CvaToken handleMoreThan()
    {
        if (stream.hasNext())
        {
            switch (stream.peek())
            {
                case '=':
                {
                    stream.poll();
                    // TODO 看不懂???;
                    return new CvaToken(CvaKind.MORE_OR_EQUALS, lineNum);
                }
                case '>':
                {
                    if (stream.hasNext(2)
                            && stream.peek(2) == '=')
                    {
                        stream.poll(2);
                        return new CvaToken(CvaKind.RIGHT_SHIFT_ASSIGN, lineNum);
                    }
                    stream.poll();
                    return new CvaToken(CvaKind.RIGHT_SHIFT, lineNum);
                }
                default:
                {
                    return new CvaToken(CvaKind.MORE_THAN, lineNum);
                }
            }
        }
        return new CvaToken(CvaKind.MORE_THAN, lineNum);
    }

    private CvaToken handleLessThan()
    {
        if (stream.hasNext())
        {
            switch (stream.peek())
            {
                case '=':
                {
                    stream.poll();
                    // TODO 看不懂???;
                    return new CvaToken(CvaKind.LESS_OR_EQUALS, lineNum);
                }
                case '<':
                {
                    if (stream.hasNext(2)
                            && stream.peek(2) == '=')
                    {
                        stream.poll(2);
                        return new CvaToken(CvaKind.LEFT_SHIFT_ASSIGN, lineNum);
                    }
                    stream.poll();
                    return new CvaToken(CvaKind.LEFT_SHIFT, lineNum);
                }
                default:
                {
                    // TODO 为啥?;
                    return new CvaToken(CvaKind.LESS_THAN, lineNum);
                }
            }
        }
        return new CvaToken(CvaKind.LESS_THAN, lineNum);
    }

    private CvaToken handleNorPrefOrIdOrNum(char ch)
    {
        // 先看c是否是非前缀字符, 这里是 int, 必须先转成char看在不在表中;
        if (CvaKind.containsKind(String.valueOf(ch)))
        {
            return new CvaToken(CvaKind.selectReverse(String.valueOf(ch)), lineNum);
        }
        StringBuilder builder = new StringBuilder();
        builder.append(ch);
        while (true)
        {
            ch = stream.peek();
            // Cva命名容许_和$符号;
            if (ch != LexerConstPool.EOF
                    && !Character.isWhitespace(ch)
                    && !isSpecialCharacter(ch))
            {
                builder.append(ch);
                this.stream.poll();
                continue;
            }
            break;
        }
        String literal = builder.toString();
        // 关键字;
        if (CvaKind.containsKind(literal))
        {
            return new CvaToken(CvaKind.selectReverse(literal), lineNum);
        }
        else
        {
            if (isNumber(literal))
            {
                // FIXME 自动机;
                if (isInt(literal))
                {
                    // FIXME 联系后端改成 INT;
                    return new CvaToken(CvaKind.NUMBER, lineNum, builder.toString());
                }
            }
            else if (isIdentifier(literal))
            {
                return new CvaToken(CvaKind.IDENTIFIER, lineNum, builder.toString());
            }
            else
            {
                errorLog("identifier or number which can only include alphabet, number or _, $",
                        "an illegal identifier with illegal char");
            }
        }
        return null;
    }

    private CvaToken handleApostrophe()
    {
        char chch = stream.poll();
        if (chch == '\\')
        {
            chch = handleEscape();
        }
        char eoc = stream.poll();
        if (eoc != '\'')
        {
            errorLog("end of char which refer to '",
                    String.valueOf(eoc));
        }
        return new CvaToken(CvaKind.CHAR, lineNum, String.valueOf(chch));
    }

    private CvaToken handleDoubleQuotes()
    {
        // 全局 index 不仅仅在循环中;
        // TODO 转义字符都出现在字符串里, 这里应该处理;
//        StringBuilder builder = new StringBuilder("\"");
        StringBuilder builder = new StringBuilder();
        // hasNext() 会屏蔽eof, 所以用true;
        while (true)
        {
            char ch = stream.poll();
            switch (ch)
            {
                case '"':
                {
//                    builder.append('"');
                    break;
                }
                case '\\':
                {
                    char escapeCh = handleEscape();
                    builder.append(escapeCh);
                    continue;
                }
                case LexerConstPool.EOF:
                {
                    errorLog("string literal char or end of string '\"'",
                            "EOF in error place");
                    // 没用的break, 只是为了防止warning;
                    break;
                }
                default:
                {
                    builder.append(ch);
                    continue;
                }
            }
            break;
        }
        String literal = String.valueOf(builder);
        return new CvaToken(CvaKind.STRING, lineNum, literal);
    }

    private char handleWhiteSpace(char ch)
    {
        while (Character.isWhitespace(ch))
        {
            switch (ch)
            {
                case LexerConstPool.NEW_LINE:
                {
                    lineNum++;
                    break;
                }
                case LexerConstPool.EOF:
                {
                    return LexerConstPool.EOF;
                }
                default:
                {
                    break;
                }
            }
            ch = this.stream.poll();
        }
        return ch;
    }

    /**
     * @return
     * @TODO 转义处理;
     */
    private char handleEscape()
    {
        char escapeCh = stream.poll();
        switch (escapeCh)
        {
            case 'r':
            {
                return '\r';
            }
            case 'n':
            {
                return '\n';
            }
            case 't':
            {
                return '\t';
            }
            case '"':
            {
                // FIXME, 应该拿\"还是"呢?;
                return '"';
            }
            default:
            {
                errorLog("escape char only '\\n', '\\r', '\\t', '\\\"' supported!",
                        String.valueOf(escapeCh));
            }
        }
        return 0;
    }

    private void handleLineComment()
    {
        char commentChar;
        while (true)
        {
            if ((commentChar = stream.poll()) == LexerConstPool.NEW_LINE)
            {
                break;
            }
//                        if (MacroConfig.DEBUG)
//                        {
//                            System.out.printf("remove %s\n", commentChar);
//                        }
        }
        lineNum++;
    }

    private void handleBlockComment()
    {
        // 推掉*;
        stream.poll();
        while (true)
        {
            // switch case 内部引进了 break, 所以不用goto跳不出去;
            // 还是就if, 虽然丑一点, 判定也多一点;
            switch (stream.poll())
            {
                case '*':
                {
                    if (stream.peek() == '/')
                    {
                        // 结束了;
                        stream.poll();
                        break;
                    }
                    continue;
                }
                case LexerConstPool.NEW_LINE:
                {
                    lineNum++;
                    continue;
                }
                default:
                {
                    // 这里用 continue 来中断switch继续while true;
                    continue;
                }
            }
            break;
        }
    }

    private boolean isInt(String literal)
    {
        // FIXME 修改;
        return true;
    }
}
