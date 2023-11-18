var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI')
var AbstractInsnNode = Java.type('org.objectweb.asm.tree.AbstractInsnNode')
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode')
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode')
var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode')
var FrameNode = Java.type('org.objectweb.asm.tree.FrameNode')
var LdcInsnNode = Java.type('org.objectweb.asm.tree.LdcInsnNode')
var MethodNode = Java.type('org.objectweb.asm.tree.MethodNode')
var Opcodes = Java.type('org.objectweb.asm.Opcodes')
var Label = Java.type('org.objectweb.asm.Label')

function getInstructionsList(consumer){
    var mn = new MethodNode();
    consumer.accept(mn);
    return mn.instructions.toArray();
}

function initializeCoreMod() {
    return {
        'playerMove': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.server.network.ServerGamePacketListenerImpl',
                'methodName': ASMAPI.mapMethod('m_7185_'),
                'methodDesc': "(Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket;)V"
            },
            'transformer': function (methodNode) {
                var elytraConst = getInstructionsList(function (methodVisitor) {
                    methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                    methodVisitor.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/server/network/ServerGamePacketListenerImpl", ASMAPI.mapField('f_9743_'), "Lnet/minecraft/server/level/ServerPlayer;"); // player
                    methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/minecraft/server/level/ServerPlayer", ASMAPI.mapMethod('m_21255_'), "()Z", false);// isFallFlying
                    var label9 = new Label();
                    methodVisitor.visitJumpInsn(Opcodes.IFEQ, label9);
                    methodVisitor.visitLdcInsn(new java.lang.Float("300.0"));
                });

                var normalMovementConst = getInstructionsList(function(methodVisitor){
                    var label9 = new Label();
                    methodVisitor.visitJumpInsn(IFEQ, label9);
                    methodVisitor.visitLdcInsn(java.lang.Float.valueOf(300));
                    var label10 = new Label();
                    methodVisitor.visitJumpInsn(GOTO, label10);
                    methodVisitor.visitLabel(label9);
                    methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                    methodVisitor.visitLdcInsn(java.lang.Float.valueOf(100));
                })

                var elytraMoveNode = null;
                var normalMoveNode = null;
                var instructions = methodNode.instructions.toArray();

                for (var i = 0; i < instructions.length && (elytraMoveNode == null || normalMoveNode == null); i++) {//within array bounds & instructions list end is also
                    if (matchesList(instructions, i, elytraConst)){
                        elytraMoveNode = instructions[i+elytraConst.length-1];
                        methodNode.instructions.insertBefore(elytraMoveNode, new FieldInsnNode(Opcodes.GETSTATIC, "com/thiakil/gottagofast/GottaGoFastMod", "MAX_PLAYER_ELYTRA_SPEED", "F"));
                        methodNode.instructions.remove(elytraMoveNode);
                    } else if (matchesList(instructions, i, normalMovementConst)){
                        normalMoveNode = instructions[i+normalMovementConst.length-1];
                        methodNode.instructions.insertBefore(normalMoveNode, new FieldInsnNode(Opcodes.GETSTATIC, "com/thiakil/gottagofast/GottaGoFastMod", "MAX_PLAYER_SPEED", "F"));
                        methodNode.instructions.remove(normalMoveNode);
                    }
                }

                return methodNode
            }
        },
        'vehicleMove': {
            'target': {
                "type": "METHOD",
                "class": "net.minecraft.server.network.ServerGamePacketListenerImpl",
                "methodName": ASMAPI.mapMethod("m_5659_"),
                "methodDesc": "(Lnet/minecraft/network/protocol/game/ServerboundMoveVehiclePacket;)V"
            },
            'transformer': function (method) {
                var vehicleMovementConst = getInstructionsList(function(mv){
                    mv.visitVarInsn(Opcodes.DLOAD, 26);
                    mv.visitVarInsn(Opcodes.DLOAD, 24);
                    mv.visitInsn(Opcodes.DSUB);
                    mv.visitLdcInsn(java.lang.Double.valueOf(100));
                });

                var vehicleMoveNode = null;
                var instructions = methodNode.instructions.toArray();
                for (var i = 0; i < instructions.length && vehicleMoveNode == null; i++) {//within array bounds & instructions list end is also
                    if (matchesList(instructions, i, vehicleMovementConst)){
                        vehicleMoveNode = instructions[i+vehicleMovementConst.length-1];
                        methodNode.instructions.insertBefore(vehicleMoveNode, new FieldInsnNode(Opcodes.GETSTATIC, "com/thiakil/gottagofast/GottaGoFastMod", "MAX_PLAYER_VEHICLE_SPEED", "D"));
                        methodNode.instructions.remove(vehicleMoveNode);
                    }
                }
                return method
            }
        }
    };
}

function matchesList(instructions, testIndex, testList){
    if (!instructionsEqivalent(instructions[testIndex], testList[0]) || testIndex + testList.length > instructions.length){
        return false;
    }
    for (var j = 1; j < testList.length; j++){
        if (!instructionsEqivalent(instructions[testIndex+j], testList[j])){
            return false;
        }
    }
    return true;
}

/**
 *
 * @param {AbstractInsnNode} left
 * @param {AbstractInsnNode} right
 * @return {boolean}
 */
function instructionsEqivalent(left, right){
    if (left.getType() != right.getType() || left.getOpcode() != right.getOpcode()){
        return false;
    }

    if (left instanceof MethodInsnNode && right instanceof MethodInsnNode){
        return (left.owner.equals(right.owner) && left.name.equals(right.name) && left.desc.equals(right.desc));
    }

    if (left instanceof VarInsnNode && right instanceof VarInsnNode){
        return left.var == right.var;
    }

    if (left instanceof FieldInsnNode && right instanceof FieldInsnNode){
        return (left.owner.equals(right.owner) && left.name.equals(right.name) && left.desc.equals(right.desc));
    }

    if (left instanceof FrameNode && right instanceof FrameNode){
        return left.type == right.type &&
            (
                (left.local == null && right.local == null) ||
                (left.local != null ? left.local.size() : 0) == (right.local != null ? right.local.size() : 0)
            ) &&
            (
                (left.stack == null && right.stack == null) ||
                (left.stack != null ? left.stack.size() : 0) == (right.stack != null ? right.stack.size() : 0)
            );
    }

    if (left instanceof LdcInsnNode && right instanceof LdcInsnNode){
        return left.cst.equals(right.cst);
    }

    return true;//dunno how else to compare
}