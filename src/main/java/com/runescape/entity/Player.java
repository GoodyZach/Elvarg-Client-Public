package com.runescape.entity;

import com.runescape.Client;
import com.runescape.cache.anim.Animation;
import com.runescape.cache.anim.Frame;
import com.runescape.cache.anim.Graphic;
import com.runescape.cache.def.ItemDefinition;
import com.runescape.cache.def.NpcDefinition;
import com.runescape.collection.ReferenceCache;
import com.runescape.entity.model.IdentityKit;
import com.runescape.entity.model.Model;
import com.runescape.io.Buffer;
import com.runescape.util.StringUtils;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.rs.api.*;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;

public final class Player extends Mob implements RSPlayer {


    public static ReferenceCache models = new ReferenceCache(260);
    public final int[] appearanceColors = new int[5];
    public final int[] equipment = new int[12];
    public NpcDefinition npcDefinition;
    public boolean aBoolean1699;
    public int team;
    public String name;
    public int combatLevel;
    public int headIcon;
    public int skullIcon;
    public int hintIcon;
    public int objectModelStart;
    public int objectModelStop;
    public int anInt1709;
    public boolean visible;
    public int objectXPos;
    public int objectCenterHeight;
    public int objectYPos;
    public Model playerModel;
    public int objectAnInt1719LesserXLoc;
    public int objectAnInt1720LesserYLoc;
    public int objectAnInt1721GreaterXLoc;
    public int objectAnInt1722GreaterYLoc;
    public int skill;
    public String clanName = "None";
    public int rights, donatorRights;
    private long cachedModel = -1L;
    private int gender;
    private long appearanceOffset;

    public Model getRotatedModel() {

        if (!visible) {
            return null;
        }

        Model animatedModel = getAnimatedModel();

        if (animatedModel == null) {
            return null;
        }

        super.height = animatedModel.modelBaseY;
        animatedModel.singleTile = true;

        if (aBoolean1699) {
            return animatedModel;
        }

        if (super.graphic != -1 && super.currentAnimation != -1) {
            Graphic spotAnim = Graphic.cache[super.graphic];

            Model spotAnimationModel = spotAnim.getModel();

            /**
             * MAKE SURE WE'VE LOADED THE GRAPHIC BEFORE ATTEMPTING TO DO IT.
             * Fixes graphics flickering.
             */
            //if (Frame.animationlist[spotAnim.animationSequence.primaryFrames[0] >> 16].length == 0) {
            //    spotAnimationModel = null;
            //}

            if (spotAnimationModel != null) {

                Model model_3 = new Model(true, Frame.noAnimationInProgress(super.currentAnimation), false, spotAnimationModel);
                model_3.scaleT(0, -super.graphicHeight, 0);
                model_3.generateBones();
                /*model_3.applyAnimationFrame(spotAnim.animationSequence.primaryFrames[super.currentAnimation], nextFrame,
                        cycle1, cycle2);*/
                model_3.applyTransform(spotAnim.animationSequence.primaryFrames[super.currentAnimation]);
                model_3.faceGroups = null;
                model_3.vertexGroups = null;
                if (spotAnim.resizeXY != 128 || spotAnim.resizeZ != 128)
                    model_3.scale(spotAnim.resizeXY, spotAnim.resizeXY, spotAnim.resizeZ);
                model_3.light(64 + spotAnim.modelShadow, 850 + spotAnim.modelBrightness, -30, -50, -30, true);
                Model models[] = {animatedModel, model_3};
                animatedModel = new Model(models);
            }
        }

        if (playerModel != null) {
            if (Client.tick >= objectModelStop)
                playerModel = null;
            if (Client.tick >= objectModelStart && Client.tick < objectModelStop) {
                Model model_1 = playerModel;
                model_1.scaleT(objectXPos - super.x, objectCenterHeight - anInt1709, objectYPos - super.y);
                if (super.nextStepOrientation == 512) {
                    model_1.rotate90Degrees();
                    model_1.rotate90Degrees();
                    model_1.rotate90Degrees();
                } else if (super.nextStepOrientation == 1024) {
                    model_1.rotate90Degrees();
                    model_1.rotate90Degrees();
                } else if (super.nextStepOrientation == 1536)
                    model_1.rotate90Degrees();
                Model models[] = {animatedModel, model_1};
                animatedModel = new Model(models);
                if (super.nextStepOrientation == 512)
                    model_1.rotate90Degrees();
                else if (super.nextStepOrientation == 1024) {
                    model_1.rotate90Degrees();
                    model_1.rotate90Degrees();
                } else if (super.nextStepOrientation == 1536) {
                    model_1.rotate90Degrees();
                    model_1.rotate90Degrees();
                    model_1.rotate90Degrees();
                }
                model_1.scaleT(super.x - objectXPos, anInt1709 - objectCenterHeight, super.y - objectYPos);
            }
        }
        animatedModel.singleTile = true;
        return animatedModel;
    }

    public void updateAppearance(Buffer buffer) {
        buffer.currentPosition = 0;

        gender = buffer.readUnsignedByte();
        headIcon = buffer.readUnsignedByte();
        skullIcon = buffer.readUnsignedByte();
        hintIcon = buffer.readUnsignedByte();
        npcDefinition = null;
        team = 0;

        for (int bodyPart = 0; bodyPart < 12; bodyPart++) {

            int reset = buffer.readUnsignedByte();

            if (reset == 0) {
                equipment[bodyPart] = 0;
                continue;
            }

            int id = buffer.readUnsignedByte();

            equipment[bodyPart] = (reset << 8) + id;

            if (bodyPart == 0 && equipment[0] == 65535) {
                npcDefinition = NpcDefinition.lookup(buffer.readUShort());
                break;
            }

            if (equipment[bodyPart] >= 512 && equipment[bodyPart] - 512 < ItemDefinition.totalItems) {
                int team = ItemDefinition.lookup(equipment[bodyPart] - 512).team;

                if (team != 0) {
                    this.team = team;
                }

            }

        }

        for (int part = 0; part < 5; part++) {
            int color = buffer.readUnsignedByte();
            if (color < 0 || color >= Client.PLAYER_BODY_RECOLOURS[part].length) {
                color = 0;
            }
            appearanceColors[part] = color;
        }

        super.idleAnimation = buffer.readUShort();
        if (super.idleAnimation == 65535) {
            super.idleAnimation = -1;
        }

        super.standTurnAnimIndex = buffer.readUShort();
        if (super.standTurnAnimIndex == 65535) {
            super.standTurnAnimIndex = -1;
        }

        super.walkAnimIndex = buffer.readUShort();
        if (super.walkAnimIndex == 65535) {
            super.walkAnimIndex = -1;
        }

        super.turn180AnimIndex = buffer.readUShort();
        if (super.turn180AnimIndex == 65535) {
            super.turn180AnimIndex = -1;
        }

        super.turn90CWAnimIndex = buffer.readUShort();
        if (super.turn90CWAnimIndex == 65535) {
            super.turn90CWAnimIndex = -1;
        }

        super.turn90CCWAnimIndex = buffer.readUShort();
        if (super.turn90CCWAnimIndex == 65535) {
            super.turn90CCWAnimIndex = -1;
        }

        super.runAnimIndex = buffer.readUShort();
        if (super.runAnimIndex == 65535) {
            super.runAnimIndex = -1;
        }

        name = StringUtils.formatText(StringUtils.decodeBase37(buffer.readLong()));
        combatLevel = buffer.readUnsignedByte();
        rights = buffer.readUnsignedByte();

        //skill = buffer.readUShort();
        visible = true;
        appearanceOffset = 0L;

        for (int index = 0; index < 12; index++) {
            appearanceOffset <<= 4;

            if (equipment[index] >= 256) {
                appearanceOffset += equipment[index] - 256;
            }

        }

        if (equipment[0] >= 256) {
            appearanceOffset += equipment[0] - 256 >> 4;
        }

        if (equipment[1] >= 256) {
            appearanceOffset += equipment[1] - 256 >> 8;
        }

        for (int index = 0; index < 5; index++) {
            appearanceOffset <<= 3;
            appearanceOffset += appearanceColors[index];
        }

        appearanceOffset <<= 1;
        appearanceOffset += gender;
    }

    public Model getAnimatedModel() {
        if (npcDefinition != null) {
            int currentFrame = -1;
            if (super.emoteAnimation >= 0 && super.animationDelay == 0) {
                Animation animation = Animation.animations[super.emoteAnimation];
                currentFrame = animation.primaryFrames[super.displayedEmoteFrames];
            } else if (super.movementAnimation >= 0) {
                Animation animation = Animation.animations[super.movementAnimation];
                currentFrame = animation.primaryFrames[super.displayedMovementFrames];
            }
            Model model = npcDefinition.method164(-1, currentFrame, null);
            return model;
        }


        long l = appearanceOffset;
        int currentFrame = -1;
        int i1 = -1;
        int j1 = -1;
        int k1 = -1;
        if (super.emoteAnimation >= 0 && super.animationDelay == 0) {
            Animation animation = Animation.animations[super.emoteAnimation];
            currentFrame = animation.primaryFrames[super.displayedEmoteFrames];
            if (super.movementAnimation >= 0 && super.movementAnimation != super.idleAnimation)
                i1 = Animation.animations[super.movementAnimation].primaryFrames[super.displayedMovementFrames];
            if (animation.playerOffhand >= 0) {
                j1 = animation.playerOffhand;
                l += j1 - equipment[5] << 40;
            }
            if (animation.playerMainhand >= 0) {
                k1 = animation.playerMainhand;
                l += k1 - equipment[3] << 48;
            }
        } else if (super.movementAnimation >= 0) {
            Animation animation = Animation.animations[super.movementAnimation];
            currentFrame = animation.primaryFrames[super.displayedMovementFrames];
        }
        Model model_1 = (Model) models.get(l);
        if (model_1 == null) {
            boolean flag = false;
            for (int i2 = 0; i2 < 12; i2++) {
                int k2 = equipment[i2];
                if (k1 >= 0 && i2 == 3)
                    k2 = k1;
                if (j1 >= 0 && i2 == 5)
                    k2 = j1;
                if (k2 >= 256 && k2 < 512 && !IdentityKit.kits[k2 - 256].bodyLoaded())
                    flag = true;
                if (k2 >= 512 && !ItemDefinition.lookup(k2 - 512).isEquippedModelCached(gender))
                    flag = true;
            }

            if (flag) {
                if (cachedModel != -1L)
                    model_1 = (Model) models.get(cachedModel);
                if (model_1 == null)
                    return null;
            }
        }
        if (model_1 == null) {
            Model aclass30_sub2_sub4_sub6s[] = new Model[14];
            int j2 = 0;
            for (int l2 = 0; l2 < 12; l2++) {
                int i3 = equipment[l2];
                if (k1 >= 0 && l2 == 3)
                    i3 = k1;
                if (j1 >= 0 && l2 == 5)
                    i3 = j1;
                if (i3 >= 256 && i3 < 512) {
                    Model model_3 = IdentityKit.kits[i3 - 256].bodyModel();
                    if (model_3 != null)
                        aclass30_sub2_sub4_sub6s[j2++] = model_3;
                }
                if (i3 >= 512) {
                    Model model_4 = ItemDefinition.lookup(i3 - 512).getEquippedModel(gender);
                    if (model_4 != null)
                        aclass30_sub2_sub4_sub6s[j2++] = model_4;
                }
            }
            model_1 = new Model(j2, aclass30_sub2_sub4_sub6s);
            for (int j3 = 0; j3 < 5; j3++)
                if (appearanceColors[j3] != 0) {
                    model_1.recolor(Client.PLAYER_BODY_RECOLOURS[j3][0],
                            Client.PLAYER_BODY_RECOLOURS[j3][appearanceColors[j3]]);
                    if (j3 == 1)
                        model_1.recolor(Client.anIntArray1204[0], Client.anIntArray1204[appearanceColors[j3]]);
                }

            model_1.generateBones();
            model_1.light(64, 850, -30, -50, -30, true);
            models.put(model_1, l);
            cachedModel = l;
        }

        if (aBoolean1699) {
            return model_1;
        }

        Model emptyModel = Model.emptyModel;

        emptyModel.replaceModel(model_1, Frame.noAnimationInProgress(currentFrame) & Frame.noAnimationInProgress(i1));
        if (currentFrame != -1 && i1 != -1) {
            emptyModel.mix(Animation.animations[super.emoteAnimation].interleaveOrder, i1, currentFrame);
        } else if (currentFrame != -1) {
            emptyModel.applyTransform(currentFrame);
        }
        emptyModel.calculateDiagonals();
        emptyModel.faceGroups = null;
        emptyModel.vertexGroups = null;
        return emptyModel;
    }

    public Model getHeadModel() {
        if (!visible) {
            return null;
        }

        if (npcDefinition != null) {
            return npcDefinition.model();
        }

        boolean cached = false;

        for (int index = 0; index < 12; index++) {
            int appearanceId = equipment[index];

            if (appearanceId >= 256 && appearanceId < 512 && !IdentityKit.kits[appearanceId - 256].headLoaded()) {
                cached = true;
            }

            if (appearanceId >= 512 && !ItemDefinition.lookup(appearanceId - 512).isDialogueModelCached(gender)) {
                cached = true;
            }
        }

        if (cached) {
            return null;
        }

        Model headModels[] = new Model[12];

        int headModelsOffset = 0;

        for (int modelIndex = 0; modelIndex < 12; modelIndex++) {
            int appearanceId = equipment[modelIndex];

            if (appearanceId >= 256 && appearanceId < 512) {

                Model subModel = IdentityKit.kits[appearanceId - 256].headModel();

                if (subModel != null) {
                    headModels[headModelsOffset++] = subModel;
                }

            }
            if (appearanceId >= 512) {
                Model subModel = ItemDefinition.lookup(appearanceId - 512).getChatEquipModel(gender);

                if (subModel != null) {
                    headModels[headModelsOffset++] = subModel;
                }

            }
        }

        Model headModel = new Model(headModelsOffset, headModels);

        for (int index = 0; index < 5; index++) {
            if (appearanceColors[index] != 0) {
                headModel.recolor(Client.PLAYER_BODY_RECOLOURS[index][0],
                        Client.PLAYER_BODY_RECOLOURS[index][appearanceColors[index]]);
                if (index == 1) {
                    headModel.recolor(Client.anIntArray1204[0], Client.anIntArray1204[appearanceColors[index]]);
                }
            }
        }

        return headModel;
    }

    public boolean isVisible() {
        return visible;
    }

    @Nullable
    @Override
    public String getName() {
        return null;
    }

    @Override
    public Actor getInteracting() {
        return null;
    }

    @Override
    public int getHealthRatio() {
        return 0;
    }

    @Override
    public int getHealthScale() {
        return 0;
    }

    @Override
    public WorldPoint getWorldLocation() {
        return null;
    }

    @Override
    public LocalPoint getLocalLocation() {
        return null;
    }

    @Override
    public void setIdleRotateLeft(int animationID) {

    }

    @Override
    public void setIdleRotateRight(int animationID) {

    }

    @Override
    public void setWalkAnimation(int animationID) {

    }

    @Override
    public void setWalkRotateLeft(int animationID) {

    }

    @Override
    public void setWalkRotateRight(int animationID) {

    }

    @Override
    public void setWalkRotate180(int animationID) {

    }

    @Override
    public void setRunAnimation(int animationID) {

    }

    @Override
    public Polygon getCanvasTilePoly() {
        return null;
    }

    @Nullable
    @Override
    public Point getCanvasTextLocation(Graphics2D graphics, String text, int zOffset) {
        return null;
    }

    @Override
    public Point getCanvasImageLocation(BufferedImage image, int zOffset) {
        return null;
    }

    @Override
    public Point getCanvasSpriteLocation(SpritePixels sprite, int zOffset) {
        return null;
    }

    @Override
    public Point getMinimapLocation() {
        return null;
    }

    @Override
    public Shape getConvexHull() {
        return null;
    }

    @Override
    public WorldArea getWorldArea() {
        return null;
    }

    @Override
    public boolean isDead() {
        return false;
    }

    @Override
    public boolean isMoving() {
        return false;
    }

    @Override
    public Polygon[] getPolygons() {
        return new Polygon[0];
    }

    @Nullable
    @Override
    public HeadIcon getOverheadIcon() {
        return null;
    }

    @Nullable
    @Override
    public SkullIcon getSkullIcon() {
        return null;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public int getRSInteracting() {
        return 0;
    }

    @Override
    public String getOverheadText() {
        return null;
    }

    @Override
    public void setOverheadText(String overheadText) {

    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int[] getPathX() {
        return pathX;
    }

    @Override
    public int[] getPathY() {
        return pathY;
    }

    @Override
    public int getAnimation() {
        return 0;
    }

    @Override
    public void setAnimation(int animation) {

    }

    @Override
    public int getAnimationFrame() {
        return 0;
    }

    @Override
    public int getActionFrame() {
        return 0;
    }

    @Override
    public void setAnimationFrame(int frame) {

    }

    @Override
    public void setActionFrame(int frame) {

    }

    @Override
    public int getActionFrameCycle() {
        return 0;
    }

    @Override
    public int getGraphic() {
        return 0;
    }

    @Override
    public void setGraphic(int id) {

    }

    @Override
    public int getSpotAnimFrame() {
        return 0;
    }

    @Override
    public void setSpotAnimFrame(int id) {

    }

    @Override
    public int getSpotAnimationFrameCycle() {
        return 0;
    }

    @Override
    public int getIdlePoseAnimation() {
        return 0;
    }

    @Override
    public void setIdlePoseAnimation(int animation) {

    }

    @Override
    public int getPoseAnimation() {
        return 0;
    }

    @Override
    public void setPoseAnimation(int animation) {

    }

    @Override
    public int getPoseFrame() {
        return 0;
    }

    @Override
    public void setPoseFrame(int frame) {

    }

    @Override
    public int getPoseFrameCycle() {
        return 0;
    }

    @Override
    public int getLogicalHeight() {
        return 0;
    }

    @Override
    public int getOrientation() {
        return 0;
    }

    @Override
    public int getCurrentOrientation() {
        return 0;
    }

    @Override
    public RSIterableNodeDeque getHealthBars() {
        return null;
    }

    @Override
    public int[] getHitsplatValues() {
        return new int[0];
    }

    @Override
    public int[] getHitsplatTypes() {
        return new int[0];
    }

    @Override
    public int[] getHitsplatCycles() {
        return new int[0];
    }

    @Override
    public int getIdleRotateLeft() {
        return 0;
    }

    @Override
    public int getIdleRotateRight() {
        return 0;
    }

    @Override
    public int getWalkAnimation() {
        return 0;
    }

    @Override
    public int getWalkRotate180() {
        return 0;
    }

    @Override
    public int getWalkRotateLeft() {
        return 0;
    }

    @Override
    public int getWalkRotateRight() {
        return 0;
    }

    @Override
    public int getRunAnimation() {
        return 0;
    }

    @Override
    public void setDead(boolean dead) {

    }

    @Override
    public int getPathLength() {
        return 0;
    }

    @Override
    public int getOverheadCycle() {
        return 0;
    }

    @Override
    public void setOverheadCycle(int cycle) {

    }

    @Override
    public int getPoseAnimationFrame() {
        return 0;
    }

    @Override
    public void setPoseAnimationFrame(int frame) {

    }

    @Override
    public RSNode getNext() {
        return null;
    }

    @Override
    public long getHash() {
        return 0;
    }

    @Override
    public RSNode getPrevious() {
        return null;
    }

    @Override
    public void onUnlink() {

    }

    @Override
    public RSUsername getRsName() {
        return null;
    }

    @Override
    public int getPlayerId() {
        return 0;
    }

    @Override
    public RSPlayerComposition getPlayerComposition() {
        return null;
    }

    @Override
    public int getCombatLevel() {
        return 0;
    }

    @Override
    public int getTotalLevel() {
        return 0;
    }

    @Override
    public int getTeam() {
        return 0;
    }

    @Override
    public boolean isFriendsChatMember() {
        return false;
    }

    @Override
    public boolean isClanMember() {
        return false;
    }

    @Override
    public boolean isFriend() {
        return false;
    }

    @Override
    public boolean isFriended() {
        return false;
    }

    @Override
    public int getRsOverheadIcon() {
        return 0;
    }

    @Override
    public int getRsSkullIcon() {
        return 0;
    }

    @Override
    public int getRSSkillLevel() {
        return 0;
    }

    @Override
    public String[] getActions() {
        return new String[0];
    }

    @Override
    public int getModelHeight() {
        return modelBaseY;
    }

    @Override
    public void setModelHeight(int modelHeight) {
        this.modelBaseY = modelHeight;
    }

    @Override
    public RSModel getModel() {
        return getRotatedModel();
    }

    @Override
    public void draw(int orientation, int pitchSin, int pitchCos, int yawSin, int yawCos, int x, int y, int z, long hash) {

    }
}
