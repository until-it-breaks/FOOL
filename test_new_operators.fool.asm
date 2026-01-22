push 0
push 10
push 5
push 1
push 2
lfp
push -2
add
lw
lfp
push -3
add
lw
div
bleq label4
push 0
b label5
label4:
push 1
label5:
push 1
beq label2
lfp
push -2
add
lw
lfp
push -3
add
lw
sub
push 0
bleq label6
push 0
b label7
label6:
push 1
label7:
push 1
beq label2
push 0
b label3
label2:
push 1
label3:
lfp
push -4
add
lw
mult
push 1
sub
push 1
beq label0
push 1
print
b label1
label0:
push 0
print
label1:
halt