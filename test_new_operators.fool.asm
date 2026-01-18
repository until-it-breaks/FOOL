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
bleq label10
push 0
b label11
label10:
push 1
label11:
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
bleq label12
push 0
b label13
label12:
push 1
label13:
beq label7
b label8
label7:
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
bleq label14
push 0
b label15
label14:
push 1
label15:
push 1
beq label8
push 0
b label9
label8:
push 1
label9:
lfp
push -4
add
lw
beq label4
b label5
label4:
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
bleq label19
push 0
b label20
label19:
push 1
label20:
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
bleq label21
push 0
b label22
label21:
push 1
label22:
beq label16
b label17
label16:
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
bleq label23
push 0
b label24
label23:
push 1
label24:
push 1
beq label17
push 0
b label18
label17:
push 1
label18:
push 0
beq label5
push 1
b label6
label5:
push 0
label6:
push 0
beq label2
push 0
b label3
label2:
push 1
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