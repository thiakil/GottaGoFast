var ASMAPI = Packages.net.minecraftforge.coremod.api.ASMAPI
var AbstractInsnNode = Packages.org.objectweb.asm.tree.AbstractInsnNode
var MethodInsnNode = Packages.org.objectweb.asm.tree.MethodInsnNode
var VarInsnNode = Packages.org.objectweb.asm.tree.VarInsnNode
var FieldInsnNode = Packages.org.objectweb.asm.tree.FieldInsnNode
var FrameNode = Packages.org.objectweb.asm.tree.FrameNode
var LdcInsnNode = Packages.org.objectweb.asm.tree.LdcInsnNode
var MethodNode = Packages.org.objectweb.asm.tree.MethodNode
var Opcodes = Packages.org.objectweb.asm.Opcodes
var Label = Packages.org.objectweb.asm.Label

function log(msg) {
    ASMAPI.log("INFO", msg)
}

function initializeCoreMod() {
    return {
        'playerMove': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.server.network.ServerGamePacketListenerImpl',
                'methodName': ASMAPI.mapMethod('m_7185_'),//handleMovePlayer
                'methodDesc': "(Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket;)V"
            },
            'transformer': movePlayerTransformer
        },
        'vehicleMove': {
            'target': {
                "type": "METHOD",
                "class": "net.minecraft.server.network.ServerGamePacketListenerImpl",
                "methodName": ASMAPI.mapMethod("m_5659_"),//handleMoveVehicle
                "methodDesc": "(Lnet/minecraft/network/protocol/game/ServerboundMoveVehiclePacket;)V"
            },
            'transformer': moveVehicleTransformer
        }
    };
}

function moveVehicleTransformer(methodNode) {
    var vehicleMovementConst = getInstructionsList(function (mv) {
        mv.visitVarInsn(Opcodes.DLOAD, 26);
        mv.visitVarInsn(Opcodes.DLOAD, 24);
        mv.visitInsn(Opcodes.DSUB);
        mv.visitLdcInsn(Java.to([100], 'double[]')[0]);
    });

    var vehicleMoveNode = null;
    var instructions = methodNode.instructions.toArray();
    for (var i = 0; i < instructions.length && vehicleMoveNode == null; i++) {//within array bounds & instructions list end is also
        if (matchesList(instructions, i, vehicleMovementConst)) {
            vehicleMoveNode = instructions[i + vehicleMovementConst.length - 1];
            methodNode.instructions.insertBefore(vehicleMoveNode, new FieldInsnNode(Opcodes.GETSTATIC, "com/thiakil/gottagofast/GottaGoFastMod", "MAX_PLAYER_VEHICLE_SPEED", "D"));
            methodNode.instructions.remove(vehicleMoveNode);
            log("patched handleMoveVehicle successfully")
        }
    }
    return methodNode
}

function movePlayerTransformer(methodNode) {
    var elytraConst = getInstructionsList(function (methodVisitor) {
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/server/network/ServerGamePacketListenerImpl", ASMAPI.mapField('f_9743_'), "Lnet/minecraft/server/level/ServerPlayer;"); // player
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/minecraft/server/level/ServerPlayer", ASMAPI.mapMethod('m_21255_'), "()Z", false);// isFallFlying
        var label9 = new Label();
        methodVisitor.visitJumpInsn(Opcodes.IFEQ, label9);
        methodVisitor.visitLdcInsn(Java.to([300], 'float[]')[0]);
    });

    var normalMovementConst = getInstructionsList(function (methodVisitor) {
        var label9 = new Label();
        methodVisitor.visitJumpInsn(Opcodes.IFEQ, label9);
        methodVisitor.visitLdcInsn(Java.to([300], 'float[]')[0]);
        var label10 = new Label();
        methodVisitor.visitJumpInsn(Opcodes.GOTO, label10);
        methodVisitor.visitLabel(label9);
        methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        methodVisitor.visitLdcInsn(Java.to([100], 'float[]')[0]);
    });

    var elytraMoveNode = null;
    var normalMoveNode = null;
    var instructions = methodNode.instructions.toArray();

    for (var i = 0; i < instructions.length && (elytraMoveNode == null || normalMoveNode == null); i++) {//within array bounds & instructions list end is also
        if (matchesList(instructions, i, elytraConst)) {
            elytraMoveNode = instructions[i + elytraConst.length - 1];
            methodNode.instructions.insertBefore(elytraMoveNode, new FieldInsnNode(Opcodes.GETSTATIC, "com/thiakil/gottagofast/GottaGoFastMod", "MAX_PLAYER_ELYTRA_SPEED", "F"));
            methodNode.instructions.remove(elytraMoveNode);
            log("patched handleMovePlayer elytra part successfully")
        } else if (matchesList(instructions, i, normalMovementConst)) {
            normalMoveNode = instructions[i + normalMovementConst.length - 1];
            methodNode.instructions.insertBefore(normalMoveNode, new FieldInsnNode(Opcodes.GETSTATIC, "com/thiakil/gottagofast/GottaGoFastMod", "MAX_PLAYER_SPEED", "F"));
            methodNode.instructions.remove(normalMoveNode);
            log("patched handleMovePlayer player speed part successfully")
        }
    }

    return methodNode
}

/**
 *
 * @param {(visitor: typeof MethodNode)=>void}consumer
 * @return {Array<AbstractInsnNode>}
 */
function getInstructionsList(consumer){
    var mn = new MethodNode();
    consumer(mn);
    return mn.instructions.toArray();
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