package YuFaFenXi;

/** ����ָ���ʽ
 * 
 * @ ��һ��������instruct ������
 * @ �ڶ���������abslevel ��λ�����ֵ
 * @ ������������ addr ƫ����
 * */
public class Instruct {
	public FunctionCode instruct;// ������
	public int abslevel;// ��λ�����ֵ
	public int addr;// ƫ����
	
	/**	���캯��
	 * 	
	 * @ �����������ֱ��ǣ������룬��Σ�ƫ����
	 * */
	public Instruct(FunctionCode instruct,int abslevel,int addr)
	{
		this.instruct=instruct;
		this.abslevel=abslevel;
		this.addr=addr;
	}
}
