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
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class GottaGoFastASMTransformer implements IClassTransformer {
    private static final String NETHANDLERSERVER_CLASS = "net.minecraft.network.NetHandlerPlayServer";//.processPlayer

    private static boolean isDev = (Boolean)Launch.blackboard.get("fml.deobfuscatedEnvironment");

    private static final String NetHandlerPlayServer_processPlayer = srg("processPlayer", "func_147347_a");
    private static final String NetHandlerPlayServer_Player = srg("player", "field_147369_b");
    private static final String EntityPlayerMP_isInvulnerableDimensionChange = srg("isInvulnerableDimensionChange", "func_184850_K");
    private static final String EntityPlayerMP_getServerWorld = srg("getServerWorld", "func_71121_q");
    private static final String EntityPlayerMP_isPlayerSleeping = srg("isPlayerSleeping", "func_70608_bn");
    private static final String EntityPlayerMP_interactionManager = srg("interactionManager", "field_71134_c");
    private static final String ServerWorld_getGameRules = srg("getGameRules", "func_82736_K");
    private static final String GameRules_getBoolean = srg("getBoolean", "func_82766_b");
    private static final String PlayerInteractionManager_isCreative = srg("isCreative", "func_73083_d");
    private static final String PlayerInteractionManager_getGameType = srg("getGameType", "func_73081_b");

    //instructions to search for the "{} moved too quickly! {},{},{}" check. We forcibly insert a jump after the first instruction as if the if check never was true.
    //NB dest label is followed by frame reset, so the GOTO must got after the frame instruction in the target list
    private AbstractInsnNode[] tooQuicklyInsnNodes;

    //"{} moved wrongly!" instructions
    private AbstractInsnNode[] movedWronglyInsnNodes;

    public GottaGoFastASMTransformer(){
        MethodNode targetInstructions = new MethodNode();
        targetInstructions.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);
        targetInstructions.visitVarInsn(Opcodes.ALOAD, 0);
        targetInstructions.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/network/NetHandlerPlayServer", NetHandlerPlayServer_Player, "Lnet/minecraft/entity/player/EntityPlayerMP;");
        targetInstructions.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/EntityPlayerMP", EntityPlayerMP_isInvulnerableDimensionChange, "()Z", false);
        Label l39 = new Label();
        targetInstructions.visitJumpInsn(Opcodes.IFNE, l39);
        targetInstructions.visitVarInsn(Opcodes.ALOAD, 0);
        targetInstructions.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/network/NetHandlerPlayServer", NetHandlerPlayServer_Player, "Lnet/minecraft/entity/player/EntityPlayerMP;");
        targetInstructions.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/EntityPlayerMP", EntityPlayerMP_getServerWorld, "()Lnet/minecraft/world/WorldServer;", false);
        targetInstructions.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/WorldServer", ServerWorld_getGameRules, "()Lnet/minecraft/world/GameRules;", false);
        targetInstructions.visitLdcInsn("disableElytraMovementCheck");
        targetInstructions.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/GameRules", GameRules_getBoolean, "(Ljava/lang/String;)Z", false);
        tooQuicklyInsnNodes = targetInstructions.instructions.toArray();

        //too wrongly check
        targetInstructions = new MethodNode();
        targetInstructions.visitVarInsn(Opcodes.ALOAD, 0);
        targetInstructions.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/network/NetHandlerPlayServer", NetHandlerPlayServer_Player, "Lnet/minecraft/entity/player/EntityPlayerMP;");
        targetInstructions.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/EntityPlayerMP", EntityPlayerMP_isInvulnerableDimensionChange, "()Z", false);
        Label l63 = new Label();
        targetInstructions.visitJumpInsn(Opcodes.IFNE, l63);
        targetInstructions.visitVarInsn(Opcodes.DLOAD, 27);
        targetInstructions.visitLdcInsn(new Double("0.0625"));
        targetInstructions.visitInsn(Opcodes.DCMPL);
        targetInstructions.visitJumpInsn(Opcodes.IFLE, l63);
        targetInstructions.visitVarInsn(Opcodes.ALOAD, 0);
        targetInstructions.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/network/NetHandlerPlayServer", NetHandlerPlayServer_Player, "Lnet/minecraft/entity/player/EntityPlayerMP;");
        targetInstructions.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/EntityPlayerMP", EntityPlayerMP_isPlayerSleeping, "()Z", false);
        targetInstructions.visitJumpInsn(Opcodes.IFNE, l63);
        targetInstructions.visitVarInsn(Opcodes.ALOAD, 0);
        targetInstructions.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/network/NetHandlerPlayServer", NetHandlerPlayServer_Player, "Lnet/minecraft/entity/player/EntityPlayerMP;");
        targetInstructions.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/entity/player/EntityPlayerMP", EntityPlayerMP_interactionManager, "Lnet/minecraft/server/management/PlayerInteractionManager;");
        targetInstructions.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/minecraft/server/management/PlayerInteractionManager", PlayerInteractionManager_isCreative, "()Z", false);
        targetInstructions.visitJumpInsn(Opcodes.IFNE, l63);
        targetInstructions.visitVarInsn(Opcodes.ALOAD, 0);
        targetInstructions.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/network/NetHandlerPlayServer", NetHandlerPlayServer_Player, "Lnet/minecraft/entity/player/EntityPlayerMP;");
        targetInstructions.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/entity/player/EntityPlayerMP", EntityPlayerMP_interactionManager, "Lnet/minecraft/server/management/PlayerInteractionManager;");
        targetInstructions.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/minecraft/server/management/PlayerInteractionManager", PlayerInteractionManager_getGameType, "()Lnet/minecraft/world/GameType;", false);
        targetInstructions.visitFieldInsn(Opcodes.GETSTATIC, "net/minecraft/world/GameType", "SPECTATOR", "Lnet/minecraft/world/GameType;");
        targetInstructions.visitJumpInsn(Opcodes.IF_ACMPEQ, l63);
        movedWronglyInsnNodes = targetInstructions.instructions.toArray();
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
                final ClassWriter writer = new MCClassWriter(ClassWriter.COMPUTE_FRAMES);
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
        boolean foundMethod = false;
        for (MethodNode methodNode : classNode.methods){
            if (methodNode.name.equals(NetHandlerPlayServer_processPlayer) && methodNode.desc != null && methodNode.desc.equals("(Lnet/minecraft/network/play/client/CPacketPlayer;)V")){
                GottaGoFastMod.logger.info("patching "+NETHANDLERSERVER_CLASS+".processPlayer(CPacketPlayer)");
                foundMethod = true;
                AbstractInsnNode tooQuicklyStartNode = null;
                AbstractInsnNode wronglyStartNode = null;
                AbstractInsnNode[] instructions = methodNode.instructions.toArray();

                for (int i = 0; i < instructions.length && i+ tooQuicklyInsnNodes.length <= instructions.length; i++) {//within array bounds & instructions list end is also
                    AbstractInsnNode instruction = instructions[i];

                    if (matchesTooQuickly(instruction, instructions, i)){
                        tooQuicklyStartNode = instruction;
                        GottaGoFastMod.logger.info("Found moved too fast check");
                        if (instructions[i+4] instanceof JumpInsnNode) {
                            methodNode.instructions.insert(tooQuicklyStartNode, new JumpInsnNode(Opcodes.GOTO, ((JumpInsnNode)instructions[i+4]).label));
                            /*((JumpInsnNode)instructions[i+4]).setOpcode(Opcodes.GOTO);
                            ((FrameNode)instruction).local = null;
                            ((FrameNode) instruction).type = Opcodes.F_SAME;*/
                        } else {
                            GottaGoFastMod.logger.error("Did not find jump class in correct place (but the list matched?!). Please report.");
                        }
                    } else if (matchesMovedWrongly(instruction, instructions, i)){
                        wronglyStartNode = instruction;
                        GottaGoFastMod.logger.info("Found moved wrongly check");
                        if (instructions[i+3] instanceof JumpInsnNode) {
                            //first instruction changes the stack, must insert BEFORE this one.
                            methodNode.instructions.insertBefore(wronglyStartNode, new JumpInsnNode(Opcodes.GOTO, ((JumpInsnNode)instructions[i+3]).label));
                            //((JumpInsnNode)instructions[i+3]).setOpcode(Opcodes.GOTO);
                        } else {
                            GottaGoFastMod.logger.error("Did not find jump class in correct place (but the list matched?!). Please report.");
                        }
                    }

                    /*if (MOVED_TOO_QUICKLY_PREDICATE.test(instruction)){
                        //found the node, but that's not the last instruction of that line
                        //keep going until we find a label node, which is usually a sign of a new code line
                        AbstractInsnNode localInst = instruction.getNext();
                        while (!(localInst instanceof LabelNode) && localInst != null){
                            localInst = localInst.getNext();
                        }
                        if (localInst != null) {
                            insertAfter = localInst.getPrevious();
                            break;
                        }
                    }*/
                }
                if (tooQuicklyStartNode == null){
                    GottaGoFastMod.logger.error("Couldn't find the 'moved too quickly' instruction to insert after :(");
                    GottaGoFastMod.logger.error("Please report this");
                }
                if (wronglyStartNode == null){
                    GottaGoFastMod.logger.error("Couldn't find the 'moved wrongly' instruction to insert after :(");
                    GottaGoFastMod.logger.error("Please report this");
                }
                return tooQuicklyStartNode != null || wronglyStartNode != null;
            }
        }
        if (!foundMethod) {
            GottaGoFastMod.logger.error("Couldn't find the processPlayer method to patch :(");
            GottaGoFastMod.logger.error("Please report this");
        }
        return foundMethod;
    }

    private boolean matchesTooQuickly(AbstractInsnNode testNode, AbstractInsnNode[] instructions, int testIndex){
        if (!instructionsEqivalent(testNode, tooQuicklyInsnNodes[0])){
            return false;
        }
        for (int j = 1; j < tooQuicklyInsnNodes.length; j++){
            if (!instructionsEqivalent(instructions[testIndex+j], tooQuicklyInsnNodes[j])){
                return false;
            }
        }
        return true;
    }

    private boolean matchesMovedWrongly(AbstractInsnNode testNode, AbstractInsnNode[] instructions, int testIndex){
        if (!instructionsEqivalent(testNode, movedWronglyInsnNodes[0])){
            return false;
        }
        for (int j = 1; j < movedWronglyInsnNodes.length; j++){
            if (!instructionsEqivalent(instructions[testIndex+j], movedWronglyInsnNodes[j])){
                return false;
            }
        }
        return true;
    }

    /*private boolean patchTriggersInit(ClassNode classNode){
        boolean foundMethod = false;
        for (MethodNode methodNode : classNode.methods){
            if (methodNode.name.equals("<clinit>") && methodNode.desc != null && methodNode.desc.equals("()V")){
                GottaGoFastMod.logger.info("patching net.minecraft.advancements.CriteriaTriggers init");
                foundMethod = true;
                AbstractInsnNode insertAfter = null;
                ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode instruction = iterator.next();
                    if (instruction instanceof InsnNode && instruction.getOpcode() == Opcodes.RETURN){
                        insertAfter = instruction.getPrevious();
                        break;
                    }
                }
                if (insertAfter == null){
                    GottaGoFastMod.logger.error("Couldn't find the instruction to insert after :(");
                    GottaGoFastMod.logger.error("Please report this");
                    return false;
                }
                methodNode.instructions.insert(insertAfter, callHook("com.thiakil.concretefactories.advancements.AdvancementsPatchTarget", "inject", "()V"));
                break;
            }
        }
        if (!foundMethod) {
            GottaGoFastMod.logger.error("Couldn't find the loadRecipes method to patch :(");
            GottaGoFastMod.logger.error("Please report this");
        }
        return foundMethod;
    }*/

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
}
