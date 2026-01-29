package compiler;

import java.util.*;
import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;

public class SymbolTableASTVisitor extends BaseASTVisitor<Void,VoidException> {

    private final List<Map<String,STentry>> symTable = new ArrayList<>();
    private final Map<String,Map<String,STentry>> classTable = new HashMap<>();
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
        Map<String, STentry> hm = symTable.getFirst();

        // Create a new ClassTypeNode with empty lists for fields and methods
        // These lists will be populated during the visit of class members
        ClassTypeNode classType = new ClassTypeNode(n.id, new ArrayList<>(), new ArrayList<>());
        STentry entry = new STentry(0, classType, decOffset--);

        // Insert the class identifier into the global symbol table (level 0)
        if (hm.put(n.id, entry) != null) {
            System.out.println("Class id " + n.id + " at line " + n.getLine() + " already declared");
            stErrors++;
        }

        // Create a vtable (virtual table) to store class fields and methods
        // This vtable is added to both the classTable and the symbolTable
        Map<String, STentry> vtable = new HashMap<>();
        classTable.put(n.id, vtable);
        symTable.add(vtable);

        // Increase nesting level to enter the class scope
        nestingLevel++;
        int prevNLDecOffset = decOffset; // stores counter for offset of declarations at previous nesting level
        decOffset = -2;

        // Field offsets start at -1 and decrement (field 0 at offset -1, field 1 at offset -2, etc.)
        int fieldOffset  = -1;
        // Method offsets start at 0 and increment (method 0 at offset 0, method 1 at offset 1, etc.)
        int methodOffset =  0;

        // Visit class fields
        if(!n.fieldList.isEmpty())
            for (FieldNode field : n.fieldList) {
                // Create an entry for the field with negative offset
                STentry fieldEntry = new STentry(nestingLevel, field.getType(), fieldOffset);

                // Insert the field into the vtable
                if (vtable.put(field.id, fieldEntry) != null) {
                    System.out.println("Field id " + field.id + " at line " + n.getLine() + " already declared");
                    stErrors++;
                }

                // Add the field type to the ClassTypeNode's field list
                // The index is calculated as -offset-1 to maintain the correct order
                classType.allFields.add(-fieldEntry.offset - 1, field.type);
                fieldOffset--;
            }

        // Visit class methods
        // Save the current decOffset because methods use positive offsets
        int savedOffset = decOffset;
        decOffset = methodOffset;
        if(!n.methodList.isEmpty())
            for (MethodNode method : n.methodList) {
                visit(method);
                // Add the method type (ArrowTypeNode) to the ClassTypeNode's method list
                // The method's offset is used as the index in the list
                classType.allMethods.add(method.offset,  (ArrowTypeNode)method.getType());
            }

        // Restore the saved decOffset
        decOffset = savedOffset;

        // Remove the vtable from the symTable and exit the class scope
        symTable.remove(nestingLevel--);
        decOffset = prevNLDecOffset;
        return null;
    }

    @Override
    public Void visitNode(MethodNode n) {
        if (print) printNode(n);
        Map<String, STentry> hm = symTable.get(nestingLevel);

        // Build the list of parameter types for the method
        List<TypeNode> parTypes = new ArrayList<>();
        for (ParNode par : n.parlist) parTypes.add(par.getType());

        // Save the current offset in the method node for the dispatch table
        n.offset = decOffset;

        // Create the function type (ArrowTypeNode) for the method
        STentry entry = new STentry(nestingLevel, new ArrowTypeNode(parTypes, n.retType), decOffset++);

        // Insert the method into the class vtable (current nestingLevel)
        if (hm.put(n.id, entry) != null) {
            System.out.println("Method id " + n.id + " at line " + n.getLine() + " already declared");
            stErrors++;
        }

        // Create a new scope for the method body
        nestingLevel++;
        Map<String, STentry> hmn = new HashMap<>();
        symTable.add(hmn);

        int prevNLDecOffset = decOffset; // Store counter for offset of declarations at previous nesting level
        decOffset = -2;

        // Insert parameters into the method's symbol table
        // Parameter offsets start at 1 and increment
        int parOffset = 1;
        for (ParNode par : n.parlist)
            if (hmn.put(par.id, new STentry(nestingLevel, par.getType(), parOffset++)) != null) {
                System.out.println("Par id " + par.id + " at line " + n.getLine() + " already declared");
                stErrors++;
            }

        // Visit local declarations and the return expression of the method
        for (Node dec : n.declist)
            visit(dec);
        visit(n.exp);

        // Remove the symbol table for the method's scope
        symTable.remove(nestingLevel--);
        decOffset = prevNLDecOffset; // Restore counter for offset of declarations at previous nesting level
        return null;
    }

    @Override
    public Void visitNode(ClassCallNode n) {
        if (print) printNode(n);

        // Look up the object (id1) in the symbol table
        STentry entry = stLookup(n.id1);
        if (entry.type == null) {
            System.out.println("Var id1 " + n.id1 + " at line " + n.getLine() + " not declared");
            stErrors++;
            return null;
        }

        // Verify that id1 is of type RefTypeNode (class reference)
        RefTypeNode ref;
        if (!(entry.type instanceof RefTypeNode)) {
            System.out.println("ID1: " + n.id1 + " at line " + n.getLine() + " is not a class");
            stErrors++;
            return null;
        }
        ref = (RefTypeNode) entry.type;

        // Retrieve the vtable of the object's class
        Map<String, STentry> vtable = classTable.get(ref.id);
        if (vtable == null) {
            System.out.println("Class of id1 " + n.id1 + " at line " + n.getLine() + " not declared");
            stErrors++;
            return null;
        }

        // Verify that the object exists in the symbol table and save lookup information
        if(stLookup(n.id1) == null){
            System.out.println("ID1: " + n.id1 + " at line " + n.getLine() + " not found in vtable");
            return null;
        }else{
            n.nl = nestingLevel;
            n.entry = stLookup(n.id1);
        }

        // Look up the method (id2) in the class's vtable
        STentry methodEntry = vtable.get(n.id2);
        if (methodEntry == null) {
            System.out.println("Method id " + n.id1 + " at line " + n.getLine() + " not declared in class " + ref.id);
            stErrors++;
            return null;
        }

        // Save the method entry in the node for use during type checking
        n.methodEntry = methodEntry;

        // Visit the call arguments
        for (Node arg : n.arglist) visit(arg);
        return null;
    }

    @Override
    public Void visitNode(NewNode n) {
        if (print) printNode(n);

        // Retrieve the vtable of the class from the classTable
        Map<String, STentry> vtable = classTable.get(n.id);
        if (vtable == null) {
            System.out.println("Class id " + n.id + " at line " + n.getLine() + " not declared");
            stErrors++;
            return null;
        }

        // Retrieve the STentry of the class from level 0 (global level) of the symbol table
        // This is necessary to access the ClassTypeNode with information about fields and methods
        STentry classEntry = symTable.getFirst().get(n.id);
        if (classEntry == null) {
            System.out.println("Class id " + n.id + " at line " + n.getLine() + " not found in symbol table");
            stErrors++;
            return null;
        }else {
            // Save the class entry in the node for use during type checking
            n.entry = classEntry;
        }

        // Visit the arguments passed to the constructor (for field initialization)
        for (Node arg : n.arglist)
            visit(arg);
        return null;
    }

    @Override
    public Void visitNode(EmptyNode n) {
        if (print) printNode(n);
        return null;
    }

    public Void visitNode(RefTypeNode n) {
        if (print) printNode(n);
        return null;
    }
}
