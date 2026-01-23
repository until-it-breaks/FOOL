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

    public static class ClassNode extends DecNode {
        final String id;
        final List<FieldNode> fields;
        final List<MethodNode> methods;

        ClassNode(String i, List<FieldNode> fl, List<MethodNode> ml) {
            this.id = i;
            this.fields = Collections.unmodifiableList(fl);
            this.methods = Collections.unmodifiableList(ml);
        }
        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class FieldNode extends DecNode {
        final String id;

        FieldNode(String i, TypeNode t) {
            this.id = i;
            type = t;
        }

        @Override
        public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class MethodNode extends DecNode {
        final String id;
        final TypeNode retType;
        final List<ParNode> parlist;
        final List<DecNode> declist;
        final Node exp;

        MethodNode(String i, TypeNode rt, List<ParNode> pl, List<DecNode> dl, Node e) {
            this.id=i;
            this.retType=rt;
            this.parlist=Collections.unmodifiableList(pl);
            this.declist=Collections.unmodifiableList(dl);
            this.exp=e;
        }
        //void setType(TypeNode t) {type = t;}

        @Override
        public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {
            return visitor.visitNode(this);
        }
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

    public static class NotNode extends Node {
        final Node exp;
        NotNode(Node e) {exp = e;}

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

    public static class LessEqualNode extends Node {
        final Node left;
        final Node right;
        LessEqualNode(Node l, Node r) {left = l; right = r;}

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

    public static class OrNode extends Node {
        final Node left;
        final Node right;
        OrNode(Node l, Node r) {left = l; right = r;}

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

    public static class DivNode extends Node {
        final Node left;
        final Node right;
        DivNode(Node l, Node r) {left = l; right = r;}

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

    public static class MinusNode extends Node {
        final Node left;
        final Node right;
        MinusNode(Node l, Node r) {left = l; right = r;}

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

    public static class ClassCallNode extends Node {
        final String id;
        final String dotid;
        final List<Node> arglist;
        STentry entry;
        int nl;
        ClassCallNode(String i, String dotid, List<Node> p) {
            this.id = i;
            this.dotid = dotid;
            this.arglist = Collections.unmodifiableList(p);
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class NewNode extends Node {
        final String id;
        final List<Node> arglist;
        STentry entry;
        int nl;

        NewNode(String i, List<Node> arg) {
            this.id = i;
            this.arglist = arg;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class EmptyNode extends Node {

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return null;
        }
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

    public static class ClassTypeNode extends TypeNode {
        final ArrayList<TypeNode> allFields;
        final ArrayList<ArrowTypeNode> allMethods;

        ClassTypeNode(ArrayList<TypeNode> af, ArrayList<ArrowTypeNode> am) {
            this.allFields = af;
            this.allMethods = am;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class RefTypeNode extends TypeNode {
        final String id;

        RefTypeNode(String i) {
            this.id = i;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class EmptyTypeNode extends TypeNode {

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return null;
        }
    }
}