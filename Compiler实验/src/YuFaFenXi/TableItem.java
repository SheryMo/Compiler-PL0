package YuFaFenXi;

import WordsCompiler.SYM;

/** ���ű�Ԫ�ظ�ʽ
 * 
 * @ name ��ʶ������
 * @ kind ��ʶ�����ͣ���3�֣���������������������
 * @ level ���ڲ�Σ�������ֵ��
 * @ address �ڱ����ڵ�ƫ����
 * @ tx ��ָ��
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

