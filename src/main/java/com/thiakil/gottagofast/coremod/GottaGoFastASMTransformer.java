/*
 * This file is part of ConcreteFactories. Copyright 2017 Thiakil
 *
 * ConcreteFactories is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ConcreteFactories is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ConcreteFactories.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.thiakil.gottagofast.coremod;

import com.thiakil.gottagofast.GottaGoFastMod;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.function.Consumer;
import java.util.function.Function;

public class GottaGoFastASMTransformer implements IClassTransformer {
    private static final String NETHANDLERSERVER_CLASS = "net.minecraft.network.NetHandlerPlayServer";//.processPlayer

    private static boolean isDev = (Boolean)Launch.blackboard.get("fml.deobfuscatedEnvironment");

    private static final String NetHandlerPlayServer_processPlayer = srg("processPlayer", "func_147347_a");
    private static final String NetHandlerPlayServer_processVehicleMove = srg("processVehicleMove", "func_184338_a");
    private static final String NetHandlerPlayServer_Player = srg("player", "field_147369_b");
    private static final String EntityLivingBase_isElytraFlying = srg("isElytraFlying", "func_184613_cA");

    private AbstractInsnNode[] elytraConst = getInstructionsList(mv->{
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/network/NetHandlerPlayServer", NetHandlerPlayServer_Player, "Lnet/minecraft/entity/player/EntityPlayerMP;");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/EntityPlayerMP", EntityLivingBase_isElytraFlying, "()Z", false);
        Label l41 = new Label();
        mv.visitJumpInsn(Opcodes.IFEQ, l41);
        mv.visitLdcInsn(new Float("300.0"));
    });

    private AbstractInsnNode[] normalMovementConst = getInstructionsList(mv->{
        Label l41 = new Label();
        mv.visitJumpInsn(Opcodes.IFEQ, l41);
        mv.visitLdcInsn(new Float("300.0"));
        Label l42 = new Label();
        mv.visitJumpInsn(Opcodes.GOTO, l42);
        mv.visitLabel(l41);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.visitLdcInsn(new Float("100.0"));
    });

    private AbstractInsnNode[] vehicleMovementConst = getInstructionsList(mv->{
        mv.visitVarInsn(Opcodes.DLOAD, 26);
        mv.visitVarInsn(Opcodes.DLOAD, 24);
        mv.visitInsn(Opcodes.DSUB);
        mv.visitLdcInsn(new Double("100.0"));
    });

    public GottaGoFastASMTransformer(){
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!transformedName.equals(NETHANDLERSERVER_CLASS)) {
            return basicClass;
        }
        try {
            final ClassNode classNode = new ClassNode();
            final ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            boolean needsRewrite = false;

            if (transformedName.equals(NETHANDLERSERVER_CLASS)) {
                needsRewrite = patchProcessPlayer(classNode);
            }

            if (needsRewrite) {
                final ClassWriter writer = new MCClassWriter(ClassWriter.COMPUTE_MAXS);
                classNode.accept(writer);
                return writer.toByteArray();
            } else {
                return basicClass;
            }
        }catch (Exception e){
            GottaGoFastMod.logger.error("Something went seriously wrong", e);
            return basicClass;
        }
    }

    private boolean patchProcessPlayer(ClassNode classNode){
        boolean foundProcessPlayer = false;
        boolean foundProcessVehicle = false;
        for (MethodNode methodNode : classNode.methods){
            if (methodNode.name.equals(NetHandlerPlayServer_processPlayer) && methodNode.desc != null && methodNode.desc.equals("(Lnet/minecraft/network/play/client/CPacketPlayer;)V")){
                GottaGoFastMod.logger.info("patching "+NETHANDLERSERVER_CLASS+".processPlayer(CPacketPlayer)");
                AbstractInsnNode elytraMoveNode = null;
                AbstractInsnNode normalMoveNode = null;
                AbstractInsnNode[] instructions = methodNode.instructions.toArray();

                for (int i = 0; i < instructions.length && (elytraMoveNode == null || normalMoveNode == null); i++) {//within array bounds & instructions list end is also
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

                if (elytraMoveNode == null){
                    GottaGoFastMod.logger.error("Couldn't find the elytra constant instruction to modify :(");
                    GottaGoFastMod.logger.error("Please report this");
                }
                if (normalMoveNode == null){
                    GottaGoFastMod.logger.error("Couldn't find the player constant instruction to modify :(");
                    GottaGoFastMod.logger.error("Please report this");
                }

                foundProcessPlayer =  elytraMoveNode != null || normalMoveNode != null;
            } else if (methodNode.name.equals(NetHandlerPlayServer_processVehicleMove) && methodNode.desc != null && methodNode.desc.equals("(Lnet/minecraft/network/play/client/CPacketVehicleMove;)V")){
                GottaGoFastMod.logger.info("patching "+NETHANDLERSERVER_CLASS+".processVehicleMove(CPacketVehicleMove)");
                AbstractInsnNode vehicleMoveNode = null;
                AbstractInsnNode[] instructions = methodNode.instructions.toArray();
                for (int i = 0; i < instructions.length && vehicleMoveNode == null; i++) {//within array bounds & instructions list end is also
                    if (matchesList(instructions, i, vehicleMovementConst)){
                        vehicleMoveNode = instructions[i+vehicleMovementConst.length-1];
                        methodNode.instructions.insertBefore(vehicleMoveNode, new FieldInsnNode(Opcodes.GETSTATIC, "com/thiakil/gottagofast/GottaGoFastMod", "MAX_PLAYER_VEHICLE_SPEED", "D"));
                        methodNode.instructions.remove(vehicleMoveNode);
                    }
                }
                if (vehicleMoveNode == null){
                    GottaGoFastMod.logger.error("Couldn't find the vehicle constant instruction to modify :(");
                    GottaGoFastMod.logger.error("Please report this");
                }
                foundProcessVehicle = vehicleMoveNode != null;
            }
        }
        if (!foundProcessPlayer) {
            GottaGoFastMod.logger.error("Couldn't find the processPlayer method (or any of its patch targets) to patch :(");
            GottaGoFastMod.logger.error("Please report this");
        }
        if (!foundProcessVehicle) {
            GottaGoFastMod.logger.error("Couldn't find the processVehicleMove method (or any of its patch targets) to patch :(");
            GottaGoFastMod.logger.error("Please report this");
        }
        return foundProcessPlayer || foundProcessVehicle;
    }

    private boolean matchesList(AbstractInsnNode[] instructions, int testIndex, AbstractInsnNode[] testList){
        if (!instructionsEqivalent(instructions[testIndex], testList[0]) || testIndex + testList.length > instructions.length){
            return false;
        }
        for (int j = 1; j < testList.length; j++){
            if (!instructionsEqivalent(instructions[testIndex+j], testList[j])){
                return false;
            }
        }
        return true;
    }

    /**
     * Make a bytecode call to the specified static function
     * @param className target classname (FQ dot notation)
     * @param method target method name
     * @param desc target method desc
     * @return and instruction list that contains a call to the target function
     */
    private static InsnList callHook(String className, String method, String desc){
        MethodNode mv = new MethodNode();
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(0xC0DE, l0);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, className.replaceAll("\\.", "/"), method, desc, false);
        return mv.instructions;
    }

    private static boolean instructionsEqivalent(AbstractInsnNode left, AbstractInsnNode right){
        if (left.getType() != right.getType() || left.getOpcode() != right.getOpcode()){
            return false;
        }

        if (left instanceof MethodInsnNode && right instanceof MethodInsnNode){
            MethodInsnNode leftMethod = (MethodInsnNode)left;
            MethodInsnNode rightMethod = (MethodInsnNode)right;
            return (leftMethod.owner.equals(rightMethod.owner) && leftMethod.name.equals(rightMethod.name) && leftMethod.desc.equals(rightMethod.desc));
        }

        if (left instanceof VarInsnNode && right instanceof VarInsnNode){
            return ((VarInsnNode) left).var == ((VarInsnNode) right).var;
        }

        if (left instanceof FieldInsnNode && right instanceof FieldInsnNode){
            FieldInsnNode leftMethod = (FieldInsnNode)left;
            FieldInsnNode rightMethod = (FieldInsnNode)right;
            return (leftMethod.owner.equals(rightMethod.owner) && leftMethod.name.equals(rightMethod.name) && leftMethod.desc.equals(rightMethod.desc));
        }

        if (left instanceof FrameNode && right instanceof FrameNode){
            FrameNode leftNode = (FrameNode)left;
            FrameNode rightNode = (FrameNode)right;
            return leftNode.type == rightNode.type &&
                    (
                            (leftNode.local == null && rightNode.local == null) ||
                            (leftNode.local != null ? leftNode.local.size() : 0) == (rightNode.local != null ? rightNode.local.size() : 0)
                    ) &&
                    (
                            (leftNode.stack == null && rightNode.stack == null) ||
                            (leftNode.stack != null ? leftNode.stack.size() : 0) == (rightNode.stack != null ? rightNode.stack.size() : 0)
                    );
        }

        if (left instanceof LdcInsnNode && right instanceof LdcInsnNode){
            return ((LdcInsnNode) left).cst.equals(((LdcInsnNode) right).cst);
        }

        return true;//dunno how else to compare
    }

    private static String srg(String mcp, String srg){
        return isDev ? mcp : srg;
    }

    private static AbstractInsnNode[] getInstructionsList(Consumer<MethodVisitor> consumer){
        MethodNode mn = new MethodNode();
        consumer.accept(mn);
        return mn.instructions.toArray();
    }
}
