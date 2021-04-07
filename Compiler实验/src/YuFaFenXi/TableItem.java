package YuFaFenXi;

import WordsCompiler.SYM;

/** 符号表元素格式
 * 
 * @ name 标识符名字
 * @ kind 标识符类型（共3种：过程名、变量、常量）
 * @ level 所在层次（或常量的值）
 * @ address 在本层内的偏移量
 * @ tx 表指针
 * */
public class TableItem {
	String name;
	KIND   kind;
	int level;
	int address;
	int tx;
	
	public TableItem(String name,KIND kind,int level,int address,int tx)
	{
		this.name=name;
		this.kind=kind;
		this.level=level;
		this.address=address;
		this.tx=tx;
	}
}

