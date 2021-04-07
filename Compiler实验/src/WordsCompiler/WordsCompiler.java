package WordsCompiler;
import java.io.*;
import java.util.Vector;

/**�ʷ�������  
 * @ ���룺�û�����(һ���ַ���)
 * @ ��������ʴ�
 * 
 * @ ���أ����ʵ����֣��ֱ��룬���ڵ��к�
 * @ ��������򱨸����ţ�������Ӧ����
 * */
public class WordsCompiler {
	private char ch;
	private int pointer,length;
	private String Buffer;
	private String strToken;
	private Vector<SYM> mySYM;// �ֱ���
	private Vector<String> myID;// ������ʶ��
	private Vector<String> myNUM;// ������ֵ
	private File inputfile;// �û�Դ�ļ�
	private BufferedReader reader;
	private Boolean isEnd;// ����Ƿ����
	
	
	/**������
	 * 
	 * @ ��ɳ�ʼ��
	 * @ ����CompilerԤ�����ӳ�����ɶԸ����ļ��Ĵʷ�����
	 * */ 
	public WordsCompiler(String filename)
	{
		Buffer="";
		mySYM=new Vector();
		myID=new Vector();
		myNUM=new Vector();
		length=0;// ��ʼ��һ�г���Ϊ0
		strToken=new String();// ��Ż�õĵ���
		pointer=-1; 
		inputfile=new File(filename);
		isEnd=false;
		Compiler();
	}
	
	/** ���ַ�����
	 * 
	 * @ ÿ����û������ж���һ���ַ�
	 * */
	public void GETSYS(){
		strToken="";
		SYM code=null;//�����ֱ���
		String warn="���ֲ��Ϸ��ķ��ţ�����ֹͣ��";
		GetChar();// �õ�һ���ַ���������ָ��
		GetBC();// ȥ���ո��Tab
		if(isLetter())
		{//�õ��ĵ�һ���ַ�����ĸ
			while(isLetter()||isDigital())
			{
				Concat();//ƴ���ַ�
				GetChar();//�������ĩ�����޷�������һ���ַ���
			}
			Retract();//�ص�
			code=Reserve();//����ֱ���
			if(code==SYM._ID)
			{//�õ����Ǳ�ʶ��
				InsertSYM(SYM._ID);
				InsertId(strToken);
				if (isEnd)
					return;
				GETSYS();//�ݹ����
			}
			else
			{//�ؼ���
				InsertSYM(code);
				if (isEnd)
					return;
				GETSYS();
			}
		}
		else if(isDigital())
		{//����
			while(isDigital())
			{
				Concat();
				GetChar();
			}
			Retract();
			InsertConst(strToken);
			InsertSYM(SYM._INT);//����
			if (isEnd)
				return;
			GETSYS();
		}
		//��������
		else if(ch=='='){InsertSYM(SYM._ASSIGN);if (isEnd)return;GETSYS();}
		else if(ch==','){InsertSYM(SYM._DH);if (isEnd)return;GETSYS();}
		else if(ch=='+'){InsertSYM(SYM._PLUS);if (isEnd)return;GETSYS();}
		else if(ch=='-'){InsertSYM(SYM._SUB);if (isEnd)return;GETSYS();}
		else if(ch=='*'){InsertSYM(SYM._STAR);if (isEnd)return;GETSYS();}
		else if(ch=='/'){InsertSYM(SYM._DIV);if (isEnd)return;GETSYS();}
		else if(ch=='('){InsertSYM(SYM._LEFT);if (isEnd)return;GETSYS();}
		else if(ch==')'){InsertSYM(SYM._RIGHT);	if (isEnd)return;GETSYS();}
		else if(ch=='#'){InsertSYM(SYM._JH);if (isEnd)return;GETSYS();}
		else if(ch==';'){InsertSYM(SYM._FH);if (isEnd)return;GETSYS();System.out.println("fenhao,"+pointer);}
		else if(ch=='<'){
			GetChar();
			if(ch=='='){InsertSYM(SYM._LESSEQ);if (isEnd)return;GETSYS();}
			else {InsertSYM(SYM._LESS);Retract();if (isEnd)return;GETSYS();}
		}
		else if(ch=='>')
		{
			GetChar();
			if(ch=='='){InsertSYM(SYM._MOREEQ);if (isEnd)return;GETSYS();}
			else {InsertSYM(SYM._MORE);Retract();if (isEnd)return;GETSYS();}
		}
		else if(ch==':')
		{
			GetChar();
			if(ch=='='){InsertSYM(SYM._MD);if (isEnd)return;GETSYS();}
			else {Retract();if (isEnd)return;GETSYS();}
		}
		else if(ch=='\n')return;
		else System.out.println(warn);
		
	}
	
	/** Ԥ�����ӳ���
	 * 
	 * @ �����һ��ȷ�����ȣ�120���ַ����������ַ���������װ���ʷ���������ָ���Ļ�������
	 * */
	private void Compiler(){		
		try{
			if(!inputfile.exists()||inputfile.isDirectory())
			{//�ļ�������
				throw new FileNotFoundException();
			}
			reader=new BufferedReader(new FileReader(inputfile) );
			Buffer=reader.readLine();// temp���ض������ַ�����
			length=Buffer.length();
			while(Buffer!=null)
			{
				System.out.println(Buffer.toString());
				GETSYS();// ����ÿ�ζ���һ���ַ�
				if(isEnd)
				{
					isEnd=false;
					Buffer=reader.readLine();//ÿ�δ��û������ж���һ��
				}
				length=Buffer.length();
				pointer=-1;
			}
		}
		catch(FileNotFoundException e){
			System.out.println("δ�ҵ���Ҫ������ļ����ʷ�����������");
		}
		catch(Exception e){
			System.out.println("");
			System.out.println("�ʷ�������ɣ�");
            System.out.println("SYM:"+mySYM);
            System.out.println("ID:"+myID);
            System.out.println("NUM:"+myNUM);
            System.out.println("");
		}
		finally
		{
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
	
	/** �ӳ�����̣�����һ�������ַ�����ch�У�����ָʾ��ǰ��һ���ַ�λ��
	 * 
	 * @ ���pointer==length-1�����ǵ�����β
	 * */
	private void GetChar()
	{
		if(pointer<length-1)
		{
			pointer++;//ָ��ǰ��
			ch=Buffer.charAt(pointer);
			System.out.println(ch+","+pointer);	
			if(pointer==length-1)//��β
				isEnd=true;
		}	
		else 	
		{//������β
			ch='\n';
			isEnd=true;		
		}			
	}
	
	/** �ӳ�����̣����ch�е��ַ��Ƿ�Ϊ�հ׻�Tab���н�����־
	 *
	 * @ ���ǿհ׻�Tab�������GetCharֱ��ch�н���һ���ǿհ��ַ�
	 * @ �����н�����־\n����ֱ�ӷ����˳�
	 * */
	private void GetBC(){
		if (ch=='\n')
		{
			isEnd=true;
			return;
		}
		while (ch==' '||ch== 9)
			GetChar();	
	}
	
	/**�ӳ�����̣�����Ƿ�Ϊ��ĸ
	 * 
	 * @ ʵ�򷵻�ture
	 * @ ���򷵻� false
	 * */
	private Boolean isLetter(){
		if(ch=='q'||ch=='w'||ch=='e'||ch=='r'||ch=='t'||ch=='y'||ch=='u'||ch=='i'||ch=='o'||ch=='p'||
				ch=='a'||ch=='s'||ch=='d'||ch=='f'||ch=='g'||ch=='h'||ch=='j'||ch=='k'||ch=='l'||
				ch=='z'||ch=='x'||ch=='c'||ch=='v'||ch=='b'||ch=='n'||ch=='m'||ch=='Q'||ch=='W'||
				ch=='E'||ch=='R'||ch=='T'||ch=='Y'||ch=='U'||ch=='I'||ch=='O'||ch=='P'||ch=='A'||
				ch=='S'||ch=='D'||ch=='F'||ch=='G'||ch=='H'||ch=='J'||ch=='K'||ch=='L'||ch=='Z'||
				ch=='X'||ch=='C'||ch=='V'||ch=='B'||ch=='N'||ch=='M')
			return true;
		else
			return false;
			
	}
	
	/**�ӳ�����̣�����Ƿ�Ϊ����
	 * 
	 * @ ʵ�򷵻�ture
	 * @ ���򷵻� false
	 * */
	private Boolean isDigital(){
		if(ch=='1'||ch=='2'||ch=='3'||ch=='4'||ch=='5'||ch=='6'||ch=='7'||ch=='8'||ch=='9'||ch=='0')
			return true;
		else 
			return false;
	}
	
	
	/**
	 * �ӳ�����̣���ch�е��ַ����ӵ�strToken֮��
	 * */
	private void Concat()
	{
		strToken=strToken+ch;		
	}
	
	/** ���͹��̣���strToken�е��ַ������ҹؼ��ֱ�
	 * 
	 *  @ ����һ�������֡���ʶ�����򷵻����ı���
	 *  @ ���򷵻�0
	 * */
	private SYM Reserve(){
		SYM flag=SYM._ID;//Ĭ��Ϊ��ʶ��
		//����ǹؼ��֣�������Ϊ�ؼ���
		if(strToken.toUpperCase().compareTo("CONST")==0)
			flag=SYM._CONST;
		if(strToken.toUpperCase().compareTo("VAR")==0)
			flag=SYM._VAR;
		if(strToken.toLowerCase().compareTo("procedure")==0)
			flag=SYM._procedure;
		if(strToken.toLowerCase().compareTo("begin")==0)
			flag=SYM._begin;
		if(strToken.toLowerCase().compareTo("end")==0)
			flag=SYM._end;
		if(strToken.toLowerCase().compareTo("odd")==0)
			flag=SYM._odd;
		if(strToken.toLowerCase().compareTo("if")==0)
			flag=SYM._if;
		if(strToken.toLowerCase().compareTo("then")==0)
			flag=SYM._then;
		if(strToken.toLowerCase().compareTo("call")==0)
			flag=SYM._call;
		if(strToken.toLowerCase().compareTo("while")==0)
			flag=SYM._while;
		if(strToken.toLowerCase().compareTo("do")==0)
			flag=SYM._do;
		if(strToken.toLowerCase().compareTo("read")==0)
			flag=SYM._read;
		if(strToken.toLowerCase().compareTo("write")==0)
			flag=SYM._write;
	    return flag;
	}
	
	/**�ص�һ���ַ�
	 * 
	 * @ �����ֺš�������ʱ�ص��ַ�
	 * @ ���õ�ǰ����ַ�Ϊ�ո�
	 * */
	private void Retract(){
		pointer--;
		if(isEnd && ch==';')
			isEnd=false;
		if(isEnd && ch==')')
			isEnd=false;
		ch=' ';
	}
	
	//��ӱ�ʶ����myID
	private void InsertId(String strToken){
		myID.add(strToken);
	}
	
	//����û����������myNUM
	private void InsertConst(String strToken){
		myNUM.add(strToken);
		
	}
	
	//����ֱ�����mySYM
	private void InsertSYM(SYM sym){
		mySYM.add(sym);
	}
	
	public Vector<String> getMyID(){
		return this.myID;
	}
	
    public Vector<String> getMyValue(){
		return this.myNUM;
	}
    
    public Vector<SYM> getMySYM(){
	    return this.mySYM;
    }
}
