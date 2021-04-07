package WordsCompiler;
/** 定义单词符号及内部表示
 *  
 *  @ 关键字、算符、界符、字符串、常量
 */
public enum SYM {
	//关键字
	_CONST,
	_VAR,
	_procedure,
	_begin,
	_end,
	_odd,//一元运算符：奇偶判断
	_if,
	_then,
	_call,
	_while,
	_do,
	_read,
	_write,
	_ID,// 变量标识符
	_INT,// 常量
	_ASSIGN,// '='
	_PLUS,// '+'
	_SUB,// '-'
	_STAR,// '*'
	_DIV,// '/'
	_LESS,// '<'
	_MORE,// '>'
	_LESSEQ,// '<='
    _MOREEQ,// '>='
    _DH,// ','
    _MD,// ':='
    _LEFT,// '('
    _RIGHT,// ')'
    _JH,// '#'
    _FH// ‘;’

}
