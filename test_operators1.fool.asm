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
bleq label8
push 0
b label9
label8:
push 1
label9:
push 1
beq label6
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
bleq label10
push 0
b label11
label10:
push 1
label11:
push 1
beq label6
push 0
b label7
label6:
push 1
label7:
push 0
beq label4
lfp
push -4
add
lw
push 0
beq label4
push 1
b label5
label4:
push 0
label5:
push 1
beq label2
push 1
b label3
label2:
push 0
label3:
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