package compiler;

import java.util.*;
import compiler.lib.*;

public class AST {
	
	public static class ProgLetInNode extends Node {
		final List<DecNode> declist;
		final Node exp;
		ProgLetInNode(List<DecNode> d, Node e) {
			declist = Collections.unmodifiableList(d); 
			exp = e;
		}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class ProgNode extends Node {
		final Node exp;
		ProgNode(Node e) {exp = e;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class FunNode extends DecNode {
		final String id;
		final TypeNode retType;
		final List<ParNode> parlist;
		final List<DecNode> declist; 
		final Node exp;
		FunNode(String i, TypeNode rt, List<ParNode> pl, List<DecNode> dl, Node e) {
	    	id=i; 
	    	retType=rt; 
	    	parlist=Collections.unmodifiableList(pl); 
	    	declist=Collections.unmodifiableList(dl); 
	    	exp=e;
	    }
		
		//void setType(TypeNode t) {type = t;}
		
		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class ParNode extends DecNode {
		final String id;
		ParNode(String i, TypeNode t) {id = i; type = t;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class VarNode extends DecNode {
		final String id;
		final Node exp;
		VarNode(String i, TypeNode t, Node v) {id = i; type = t; exp = v;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
		
	public static class PrintNode extends Node {
		final Node exp;
		PrintNode(Node e) {exp = e;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class IfNode extends Node {
		final Node cond;
		final Node th;
		final Node el;
		IfNode(Node c, Node t, Node e) {cond = c; th = t; el = e;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class EqualNode extends Node {
		final Node left;
		final Node right;
		EqualNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class TimesNode extends Node {
		final Node left;
		final Node right;
		TimesNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class PlusNode extends Node {
		final Node left;
		final Node right;
		PlusNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class CallNode extends Node {
		final String id;
		final List<Node> arglist;
		STentry entry;
		int nl;
		CallNode(String i, List<Node> p) {
			id = i; 
			arglist = Collections.unmodifiableList(p);
		}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class IdNode extends Node {
		final String id;
		STentry entry;
		int nl;
		IdNode(String i) {id = i;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class BoolNode extends Node {
		final Boolean val;
		BoolNode(boolean n) {val = n;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class IntNode extends Node {
		final Integer val;
		IntNode(Integer n) {val = n;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class ArrowTypeNode extends TypeNode {
		final List<TypeNode> parlist;
		final TypeNode ret;
		ArrowTypeNode(List<TypeNode> p, TypeNode r) {
			parlist = Collections.unmodifiableList(p); 
			ret = r;
		}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class BoolTypeNode extends TypeNode {

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class IntTypeNode extends TypeNode {

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

    // New nodes

    public static class LessEqualNode extends Node {
        final Node left;
        final Node right;
        LessEqualNode(Node l, Node r) {left = l; right = r;}

        @Override
        public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
    }

    public static class GreaterEqualNode extends Node {
        final Node left;
        final Node right;
        GreaterEqualNode(Node l, Node r) {left = l; right = r;}

        @Override
        public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
    }

    public static class OrNode extends Node {
        final Node left;
        final Node right;
        OrNode(Node l, Node r) {left = l; right = r;}

        @Override
        public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
    }

    public static class AndNode extends Node {
        final Node left;
        final Node right;
        AndNode(Node l, Node r) {left = l; right = r;}

        @Override
        public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
    }

    public static class DivNode extends Node {
        final Node left;
        final Node right;
        DivNode(Node l, Node r) {left = l; right = r;}

        @Override
        public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
    }

    public static class MinusNode extends Node {
        final Node left;
        final Node right;
        MinusNode(Node l, Node r) {left = l; right = r;}

        @Override
        public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
    }

    public static class NotNode extends Node {
        final Node exp;
        NotNode(Node e) { exp = e;}

        @Override
        public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
    }

    // Object Oriented Nodes

    // New Object-Oriented Declaration Nodes

    // Like ParNode
    public static class FieldNode extends DecNode {
        final String id;
        FieldNode(String id, TypeNode type) {
            this.id = id;
            this.type = type;
        }

        @Override
        public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
    }

    // Like FunNode
    public static class MethodNode extends DecNode {
        final String id;
        final TypeNode retType;
        final List<ParNode> parlist;
        final List<DecNode> declist;
        final Node exp;
        MethodNode(String id, TypeNode retType, List<ParNode> parlist, List<DecNode> declist, Node exp) {
            this.id = id;
            this.retType = retType;
            this.parlist = Collections.unmodifiableList(parlist);
            this.declist = Collections.unmodifiableList(declist);
            this.exp = exp;
        }

        @Override
        public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
    }

    public static class ClassNode extends DecNode {
        final String id;
        final List<FieldNode> fieldList;
        final List<MethodNode> methodList;
        ClassNode(String id, List<FieldNode> fields, List<MethodNode> methods) {
            this.id = id;
            this.fieldList = Collections.unmodifiableList(fields);
            this.methodList = Collections.unmodifiableList(methods);
        }

        @Override
        public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
    }

    // New Object-Oriented Expression Nodes

    // Example: ID1.ID2(a,b,c)
    public static class ClassCallNode extends Node {
        final String objectId;
        final String methodId;
        final List<Node> argList;
        STentry entry;                              // STentry di ID1
        STentry methodEntry;                        // STentry di ID2
        int nl;
        ClassCallNode(String objectId, String methodId, List<Node> args) {
            this.objectId = objectId;
            this.methodId = methodId;
            this.argList = Collections.unmodifiableList(args);
        }

        @Override
        public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
    }

    // Example: new ID1(a,b,c)
    public static class NewNode extends Node {
        final String id;
        final List<Node> argList;

        STentry entry;

        public NewNode(String id, List<Node> args) {
            this.id = id;
            this.argList = Collections.unmodifiableList(args);
        }

        @Override
        public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
    }

    // null
    public static class EmptyNode extends Node {
        @Override
        public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
    }

    // New Object-Oriented Type Nodes

    /**
     * Whenever a class variable appears, this is used to keep track of its class.
     * Example: var x: Point.
     * RefTypeNode stores the string "Point" in this case.
     */
    public static class RefTypeNode extends TypeNode {
        final String id;

        public RefTypeNode(String id) {
            this.id = id;
        }

        @Override
        public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
    }

    /**
     * Handles the "null" literal type scenario. It's compatible with any RefTypeNode and allows assignments such as
     * "var p:Point = null"
     */
    public static class EmptyTypeNode extends TypeNode {
        @Override
        public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
    }

    /**
     * Barebone version of ClassNode for type checking used to check that if I have "new Point(1,0)", then "Point" as
     * class accepts two integer arguments. Another scenario is when a "Dot Call" is performed, for example if I have
     * "p.setPoint(10,20)", it looks at p, sees it as RefTypeNode("Point"), pulls from the Symbol Table a ClassTypeNode
     * for "Point", check if "setPoint" exists among "allMethods". After that ArrowTypeNode is also checked for
     * "setPoint", making sure that 10 and 20 are of the right type.
     */
    public static class ClassTypeNode extends TypeNode {
        final List<TypeNode> allFields;
        final List<ArrowTypeNode> allMethods;

        public ClassTypeNode(List<TypeNode> allFields, List<ArrowTypeNode> allMethods) {
            this.allFields = Collections.unmodifiableList(allFields);
            this.allMethods = Collections.unmodifiableList(allMethods);
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }
}