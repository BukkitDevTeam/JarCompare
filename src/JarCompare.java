import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.ZipEntry;

import org.objectweb.asm.*;

import java.io.*;

class Field {
	public String owner;
	public String name;
	public Field(String owner, String name) {
		this.owner=owner;
		this.name=name;
	}
	
	public boolean equals(Field b) {
		return this.owner.equals(b.owner) && this.name.equals(b.name);
	}
}

class FieldComparator implements Comparator<Field> {
	public int compare(Field a, Field b) {
		int cmpOwner=a.owner.compareTo(b.owner);
		int cmpName=a.name.compareTo(b.name);
		if (cmpOwner<0 || cmpOwner>0) return cmpOwner;
		return cmpName;
	}
}

class Method {
	public String owner;
	public String name;
	public String arguments;
	public Method(String owner, String name, String arguments) {
		this.owner=owner;
		this.name=name;
		this.arguments=arguments;
	}
	public boolean equals(Method b) {
		return this.owner.equals(b.owner) && this.name.equals(b.name)&& this.arguments.equals(b.arguments); 
	}
}

class MethodComparator implements Comparator<Method> {
	public int compare(Method a, Method b) {
		int cmpOwner=a.owner.compareTo(b.owner);
		int cmpName=a.name.compareTo(b.name);
		int cmpArguments=a.arguments.compareTo(b.arguments);
		if (cmpOwner<0 || cmpOwner>0) return cmpOwner;
		if (cmpName<0 || cmpName>0) return cmpName;
		return cmpArguments;
	}
}


class MyVisitor implements ClassVisitor, MethodVisitor {
	public ArrayList<String> arrClasses=new ArrayList<String>();
	public ArrayList<Field> arrFields=new ArrayList<Field>();
	public ArrayList<Method> arrMethods=new ArrayList<Method>();
	public String strMe="";
	
	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		strMe=name;
		arrClasses.add(name);
		arrClasses.add(superName);
		if (interfaces!=null) {
			for (int i=0;i<interfaces.length; i++)
				arrClasses.add(interfaces[i]);
		}
	}

	@Override
	public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
		//No need to visit annotations
		return null;
	}

	@Override
	public void visitAttribute(Attribute arg0) {
		
	}

	@Override
	public void visitEnd() {

	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		arrFields.add(new Field(strMe,name));
		return null;
	}

	@Override
	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		//TODO?
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		arrMethods.add(new Method(strMe,name,desc));
		return this;
	}

	@Override
	public void visitOuterClass(String owner, String name, String desc) {
	}

	@Override
	public void visitSource(String source, String debug) {
	}

	@Override
	public AnnotationVisitor visitAnnotationDefault() {
		return null;
	}

	@Override
	public void visitCode() {
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		arrClasses.add(owner);
		arrFields.add(new Field(owner,name));
	}

	@Override
	public void visitFrame(int type, int nLocal, Object[] local, int nStack,
			Object[] stack) {
	}

	@Override
	public void visitIincInsn(int var, int increment) {
	}

	@Override
	public void visitInsn(int opcode) {
	}

	@Override
	public void visitIntInsn(int opcode, int operand) {
	}

	@Override
	public void visitJumpInsn(int opcode, Label label) {
	}

	@Override
	public void visitLabel(Label label) {
	}

	@Override
	public void visitLdcInsn(Object cst) {
		if (cst instanceof Type) {
			visitType((Type)cst);
		}	
	}

	@Override
	public void visitLineNumber(int line, Label start) {
	}

	@Override
	public void visitLocalVariable(String name, String desc, String signature,
			Label start, Label end, int index) {
	}

	@Override
	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		arrClasses.add(owner);
		arrMethods.add(new Method(owner,name,desc));
		visitTypes(Type.getArgumentTypes(desc));
	}

	@Override
	public void visitMultiANewArrayInsn(String desc, int dims) {
	}

	@Override
	public AnnotationVisitor visitParameterAnnotation(int parameter, String desc,
			boolean visible) {
		return null;
	}

	@Override
	public void visitTableSwitchInsn(int min, int max, Label dflt,
			Label[] labels) {
	}

	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler,
			String type) {
	}

	@Override
	public void visitTypeInsn(int opcode, String type) {
		arrClasses.add(type);
	}

	@Override
	public void visitVarInsn(int opcode, int var) {
	}
	
	public void visitType(Type t) {
		if (t.getSort()==Type.OBJECT)
			arrClasses.add(t.getInternalName());
		if (t.getSort()==Type.ARRAY)
			visitType(t.getElementType());
	}
	
	public void visitTypes(Type t[]) {
		for (int i=0; i<t.length; i++)
			visitType(t[i]);
	}
}

public class JarCompare {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		JarFile jarSecond=new JarFile("minecraft-server-1.2.5.jar");
		JarFile jarFirst=new JarFile("minecraft_server.jar");
		Manifest mfFirst=jarFirst.getManifest();
		Manifest mfSecond=jarSecond.getManifest();
		String mainFirst=mfFirst.getMainAttributes().getValue("Main-Class");
		String mainSecond=mfSecond.getMainAttributes().getValue("Main-Class");
		Map<String,String> mapClasses=new HashMap<String,String>();
		Map<Field,Field> mapFields=new TreeMap<Field,Field>(new FieldComparator());
		Map<Method,Method> mapMethods=new TreeMap<Method,Method>(new MethodComparator());
		Set<String> setClasses=new TreeSet<String>();
		mapClasses.put(mainFirst.replace('.','/'), mainSecond.replace('.','/'));
		Queue<String> q=new LinkedList<String>();
		q.add(mainFirst.replace('.','/'));
		while (!q.isEmpty()) {
			String classFirst=q.remove();
			String classSecond=mapClasses.get(classFirst);
			String fileFirst=classFirst+".class";
			String fileSecond=classSecond+".class";
			ZipEntry zeFirst=jarFirst.getEntry(fileFirst);
			ZipEntry zeSecond=jarSecond.getEntry(fileSecond);
			if (zeFirst == null && zeSecond == null) {
				continue;
			}
			if (zeFirst==null || zeSecond==null) {
				System.err.println("Classes "+classFirst+" or "+classSecond+" not found");
				return;
			}
			setClasses.add(classFirst);
			//System.out.println("Comparing "+classFirst+" to "+classSecond);
			ClassReader crFirst=new ClassReader(jarFirst.getInputStream(zeFirst));
			ClassReader crSecond=new ClassReader(jarSecond.getInputStream(zeSecond));
			MyVisitor cvFirst=new MyVisitor();
			MyVisitor cvSecond=new MyVisitor();
			crFirst.accept(cvFirst,0);
			crSecond.accept(cvSecond,0);
			if (
					cvFirst.arrClasses.size()!=cvSecond.arrClasses.size() ||
					cvFirst.arrFields.size()!=cvSecond.arrFields.size() ||
					cvFirst.arrMethods.size()!=cvSecond.arrMethods.size()
					)
			{
				System.err.println("Classes "+classFirst+" or "+classSecond+" do not match");
				return;
			}
			for (int c=0; c<cvFirst.arrClasses.size(); c++) {
				String strFrom=cvFirst.arrClasses.get(c);
				String strTo=cvSecond.arrClasses.get(c);
				if (mapClasses.containsKey(strFrom)) {
					if (!mapClasses.get(strFrom).equals(strTo)) {
						System.err.println("Mismatching mappings from "+strTo);
						return;
					}
				} else {
					mapClasses.put(strFrom, strTo);
					q.add(strFrom);
				}
			}
			for (int f=0; f<cvFirst.arrFields.size(); f++) {
				Field fFrom=cvFirst.arrFields.get(f);
				Field fTo=cvSecond.arrFields.get(f);
				if (mapFields.containsKey(fFrom)) {
					if (!mapFields.get(fFrom).equals(fTo)) {
						Field fPrev=mapFields.get(fFrom);
						System.err.println("Mismatching mappings from field "+fFrom.owner+" "+fFrom.name);
						System.err.println("Previous mapping was "+fPrev.owner+" "+fPrev.name);
						System.err.println("New mapping is "+fTo.owner+" "+fTo.name);
						return;
					}
				} else {
					mapFields.put(fFrom,fTo);
				}
			}
			for (int m=0; m<cvFirst.arrMethods.size(); m++) {
				Method mFrom=cvFirst.arrMethods.get(m);
				Method mTo=cvSecond.arrMethods.get(m);
				if (mapMethods.containsKey(mFrom)) {
					if (!mapMethods.get(mFrom).equals(mTo)) {
						System.err.println("Mismatching mappings from method "+mFrom.owner+" "+mFrom.name+" "+mFrom.arguments);
						return;
					}
				} else {
					mapMethods.put(mFrom,mTo);
				}
			}
		}
		for (String c : setClasses) {
			System.out.println("CL: "+c+" "+mapClasses.get(c));
		}
		for (Field f : mapFields.keySet()) {
			if (setClasses.contains(f.owner)) {
				Field b=mapFields.get(f);
				System.out.println("FD: "+f.owner+"/"+f.name+" "+b.owner+"/"+b.name);
			}
		}
		for (Method m : mapMethods.keySet()) {
			if (setClasses.contains(m.owner) && !m.name.equals("<init>")) {
				Method b=mapMethods.get(m);
				System.out.println("MD: "+m.owner+"/"+m.name+" "+m.arguments+" "+b.owner+"/"+b.name+" "+b.arguments);
			}
		}
		System.out.println("Number of classes: "+setClasses.size());
	}

}
