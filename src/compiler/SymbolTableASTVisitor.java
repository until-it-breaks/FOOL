package compiler;

import java.util.*;

import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;

public class SymbolTableASTVisitor extends BaseASTVisitor<Void,VoidException> {

    private List<Map<String, STentry>> symTable = new ArrayList<>();
    private Map<String, Map<String, STentry>> classTable = new HashMap<>();
    private int nestingLevel = 0; // current nesting level
    private int decOffset = -2; // counter for offset of local declarations at current nesting level
    int stErrors = 0;

    SymbolTableASTVisitor() {
    }

    SymbolTableASTVisitor(boolean debug) {
        super(debug);
    } // enables print for debugging

    private STentry stLookup(String id) {
        int j = nestingLevel;
        STentry entry = null;
        while (j >= 0 && entry == null)
            entry = symTable.get(j--).get(id);
        return entry;
    }

    @Override
    public Void visitNode(ProgLetInNode n) {
        if (print) printNode(n);
        Map<String, STentry> hm = new HashMap<>();
        symTable.add(hm);
        for (Node dec : n.declist) visit(dec);
        visit(n.exp);
        symTable.remove(0);
        return null;
    }

    @Override
    public Void visitNode(ProgNode n) {
        if (print) printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(FunNode n) {
        if (print) printNode(n);
        Map<String, STentry> hm = symTable.get(nestingLevel);
        List<TypeNode> parTypes = new ArrayList<>();
        for (ParNode par : n.parlist) parTypes.add(par.getType());
        STentry entry = new STentry(nestingLevel, new ArrowTypeNode(parTypes, n.retType), decOffset--);
        //inserimento di ID nella symtable
        if (hm.put(n.id, entry) != null) {
            System.out.println("Fun id " + n.id + " at line " + n.getLine() + " already declared");
            stErrors++;
        }
        //creare una nuova hashmap per la symTable
        nestingLevel++;
        Map<String, STentry> hmn = new HashMap<>();
        symTable.add(hmn);
        int prevNLDecOffset = decOffset; // stores counter for offset of declarations at previous nesting level
        decOffset = -2;

        int parOffset = 1;
        for (ParNode par : n.parlist)
            if (hmn.put(par.id, new STentry(nestingLevel, par.getType(), parOffset++)) != null) {
                System.out.println("Par id " + par.id + " at line " + n.getLine() + " already declared");
                stErrors++;
            }
        for (Node dec : n.declist) visit(dec);
        visit(n.exp);
        //rimuovere la hashmap corrente poiche' esco dallo scope
        symTable.remove(nestingLevel--);
        decOffset = prevNLDecOffset; // restores counter for offset of declarations at previous nesting level
        return null;
    }

    @Override
    public Void visitNode(VarNode n) {
        if (print) printNode(n);
        visit(n.exp);
        Map<String, STentry> hm = symTable.get(nestingLevel);
        STentry entry = new STentry(nestingLevel, n.getType(), decOffset--);
        //inserimento di ID nella symtable
        if (hm.put(n.id, entry) != null) {
            System.out.println("Var id " + n.id + " at line " + n.getLine() + " already declared");
            stErrors++;
        }
        return null;
    }

    @Override
    public Void visitNode(PrintNode n) {
        if (print) printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(IfNode n) {
        if (print) printNode(n);
        visit(n.cond);
        visit(n.th);
        visit(n.el);
        return null;
    }

    @Override
    public Void visitNode(EqualNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(GreaterEqualNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(LessEqualNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(TimesNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(DivNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(PlusNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(MinusNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(AndNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(OrNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(NotNode n) {
        if (print) printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(CallNode n) {
        if (print) printNode(n);
        STentry entry = stLookup(n.id);
        if (entry == null) {
            System.out.println("Fun id " + n.id + " at line " + n.getLine() + " not declared");
            stErrors++;
        } else {
            n.entry = entry;
            n.nl = nestingLevel;
        }
        for (Node arg : n.arglist) visit(arg);
        return null;
    }

    @Override
    public Void visitNode(IdNode n) {
        if (print) printNode(n);
        STentry entry = stLookup(n.id);
        if (entry == null) {
            System.out.println("Var or Par id " + n.id + " at line " + n.getLine() + " not declared");
            stErrors++;
        } else {
            n.entry = entry;
            n.nl = nestingLevel;
        }
        return null;
    }

    @Override
    public Void visitNode(BoolNode n) {
        if (print) printNode(n, n.val.toString());
        return null;
    }

    @Override
    public Void visitNode(IntNode n) {
        if (print) printNode(n, n.val.toString());
        return null;
    }

    @Override
    public Void visitNode(ClassNode n) {
        if (print) printNode(n);
        Map<String, STentry> hm = symTable.get(0); // All classes found at nestling level 0

        // Create a new ClassTypeNode with empty lists for fields and methods
        // These lists will be populated during the visit of class members
        ClassTypeNode classTypeNode = new ClassTypeNode(new ArrayList<>(), new ArrayList<>());
        STentry entry = new STentry(0, classTypeNode, decOffset--);

        // Insert the class identifier into the global symbol table (level 0)
        if (hm.put(n.id, entry) != null) {
            System.out.println("Class id " + n.id + " at line " + n.getLine() + " already declared");
            stErrors++;
        }

        // Create a virtual table to store class fields and methods
        // This vtable is added to both the classTable and the symbolTable
        Map<String, STentry> virtualTable = new HashMap<>();
        classTable.put(n.id, virtualTable);
        nestingLevel++;
        symTable.add(virtualTable);

        int prevNLDecOffset = decOffset;

        // Field offsets start at -1 and decrement (field 0 at offset -1, field 1 at offset -2, etc.)
        int fieldOffset  = -1;
        for (FieldNode field : n.fieldList) {
            STentry fieldEntry = new STentry(nestingLevel, field.getType(), fieldOffset);
            if (virtualTable.put(field.id, fieldEntry) != null) {
                System.out.println("Field id " + field.id + " at line " + n.getLine() + " already declared");
                stErrors++;
            }
            int position = -fieldEntry.offset - 1;
            classTypeNode.allFields.add(position, field.type);
            fieldOffset--;
        }

        // Method offsets start at 0 and increment (method 0 at offset 0, method 1 at offset 1, etc.)
        decOffset = 0;
        for (MethodNode method : n.methodList) {
            visit(method);
            // Add the method type (ArrowTypeNode) to the ClassTypeNode's method list
            // The method's offset is used as the index in the list
            classTypeNode.allMethods.add(method.offset, (ArrowTypeNode) method.getType());
        }

        symTable.remove(nestingLevel--);
        decOffset = prevNLDecOffset;

        return null;
    }

    @Override
    public Void visitNode(MethodNode n) {
        if (print) printNode(n);
        Map<String, STentry> hm = symTable.get(nestingLevel);
        List<TypeNode> parTypes = new ArrayList<>();
        for (ParNode par : n.parlist) parTypes.add(par.getType());

        // Remember the method offset and create an entry with decOffset which start at 0 and goes upwards.
        // Save the current offset in the method node for the dispatch table
        n.offset = decOffset;
        STentry entry = new STentry(nestingLevel, new ArrowTypeNode(parTypes, n.retType), decOffset++);

        // Insert the method in virtual table
        if (hm.put(n.id, entry) != null) {
            System.out.println("Method id " + n.id + " at line " + n.getLine() + " already declared");
            stErrors++;
        }

        // Inner scope of the method
        nestingLevel++;
        Map<String, STentry> hmn = new HashMap<>();
        symTable.add(hmn);

        int prevNLDecOffset = decOffset;
        decOffset = -2; // Set decOffset to -2 for normal declarations

        int parOffset = 1;
        for (ParNode par : n.parlist)
            if (hmn.put(par.id, new STentry(nestingLevel, par.getType(), parOffset++)) != null) {
                System.out.println("Par id " + par.id + " at line " + n.getLine() + " already declared");
                stErrors++;
            }

        for (Node dec : n.declist) visit(dec);
        visit(n.exp);

        symTable.remove(nestingLevel--);
        decOffset = prevNLDecOffset;
        return null;
    }

    @Override
    public Void visitNode(ClassCallNode n) {
        if (print) printNode(n);

        // Look up the object (id1) in the symbol table
        STentry entry = stLookup(n.id1);
        if (entry.type == null) {
            System.out.println("Object id " + n.id1 + " at line " + n.getLine() + " not declared");
            stErrors++;
            return null;
        }

        n.entry = entry;
        n.nl = nestingLevel;

        // Verify that id1 is of type RefTypeNode (class reference)
        if (!(entry.type instanceof RefTypeNode)) {
            System.out.println("Id " + n.id1 + " at line " + n.getLine() + " is not an object");
            stErrors++;
            return null;
        }

        String className = ((RefTypeNode) entry.type).id;

        Map<String, STentry> virtualTable = classTable.get(className);

        // Verify that the object exists in the symbol table and save lookup information
        if (virtualTable == null) {
            System.out.println("Class id" + n.id1 + " at line " + n.getLine() + " not declared");
            stErrors++;
            return null;
        }

        // Look up the method (id2) in the class's virtual table
        STentry methodEntry = virtualTable.get(n.id2);
        if (methodEntry == null) {
            System.out.println("Method id " + n.id2 + " at line " + n.getLine() + " not found in class " + className);
            stErrors++;
            return null;
        }
        // Save the method entry in the node for use during type checking
        n.methodEntry = methodEntry;

        for (Node arg : n.arglist) visit(arg);

        return null;
    }

    @Override
    public Void visitNode(NewNode n) {
        if (print) printNode(n);

        // Retrieve the virtual table of the class from the classTable
        Map<String, STentry> virtualTable = classTable.get(n.id);
        if (virtualTable == null) {
            System.out.println("Class id " + n.id + " at line " + n.getLine() + " not declared");
            stErrors++;
            return null;
        }

        // Retrieve the STentry of the class from level 0 (global level) of the symbol table
        // This is necessary to access the ClassTypeNode with information about fields and methods
        STentry classEntry = symTable.get(0).get(n.id);
        if (classEntry == null) {
            System.out.println("Class id " + n.id + " at line " + n.getLine() + " not found in symbol table");
            stErrors++;
            return null;
        }

        n.entry = classEntry;

        for (Node arg : n.arglist) visit(arg);
        return null;
    }

    @Override
    public Void visitNode(EmptyNode n) {
        if (print) printNode(n);
        return null;
    }
}
