package fr.guedesite.ummo.proxy.client.gui;

import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.AIR;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.ALL;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.ARMOR;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.BOSSHEALTH;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.CHAT;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.CROSSHAIRS;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.DEBUG;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.EXPERIENCE;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.FOOD;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.FPS_GRAPH;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.HEALTH;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.HEALTHMOUNT;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.HELMET;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.HOTBAR;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.JUMPBAR;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.PLAYER_LIST;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.PORTAL;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.POTION_ICONS;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.SUBTITLES;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.TEXT;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.VIGNETTE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.Blocks;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.overlay.DebugOverlayGui;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.AttackIndicatorStatus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.FoodStats;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBusInvokeDispatcher;
import net.minecraftforge.eventbus.api.IEventListener;

@SuppressWarnings("deprecation")
public class UGuiInGame extends ForgeIngameGui
{

    private static final int WHITE = 0xFFFFFF;



    private FontRenderer fontrenderer = null;
    private RenderGameOverlayEvent eventParent;
    //private static final String MC_VERSION = MinecraftForge.MC_VERSION;
    private GuiOverlayDebugForge debugOverlay;

    public UGuiInGame(Minecraft mc)
    {
        super(mc);
        debugOverlay = new GuiOverlayDebugForge(mc);
    }

    @Override
    public void render(MatrixStack mStack, float partialTicks)
    {
        this.screenWidth = this.minecraft.getWindow().getGuiScaledWidth();
        this.screenHeight = this.minecraft.getWindow().getGuiScaledHeight();
        eventParent = new RenderGameOverlayEvent(mStack, partialTicks, this.minecraft.getWindow());
        renderHealthMount = minecraft.player.getVehicle() instanceof LivingEntity;
        renderFood = !renderHealthMount;
        renderJumpBar = minecraft.player.isRidingJumpable();

        right_height = 39;
        left_height = 39;

        if (pre(ALL, mStack)) return;

        fontrenderer = minecraft.font;
        //mc.entityRenderer.setupOverlayRendering();
        RenderSystem.enableBlend();
        if (renderVignette && Minecraft.useFancyGraphics())
        {
            renderVignette(minecraft.getCameraEntity());
        }
        else
        {
            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();
        }

        if (renderHelmet) renderHelmet(partialTicks, mStack);

        if (renderPortal && !minecraft.player.hasEffect(Effects.CONFUSION))
        {
            renderPortalOverlay(partialTicks);
        }

        if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR)
        {
            if (renderSpectatorTooltip) spectatorGui.renderHotbar(mStack, partialTicks);
        }
        else if (!this.minecraft.options.hideGui)
        {
            if (renderHotbar) renderHotbar(partialTicks, mStack);
        }

        if (!this.minecraft.options.hideGui) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            setBlitOffset(-90);
            random.setSeed((long)(tickCount * 312871));

            if (renderCrosshairs) renderCrosshair(mStack);
            if (renderBossHealth) renderBossHealth(mStack);

            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            if (this.minecraft.gameMode.canHurtPlayer() && this.minecraft.getCameraEntity() instanceof PlayerEntity)
            {
                if (renderHealth) renderHealth(this.screenWidth, this.screenHeight, mStack);
                if (renderArmor)  renderArmor(mStack, this.screenWidth, this.screenHeight);
                if (renderFood)   renderFood(this.screenWidth, this.screenHeight, mStack);
                if (renderHealthMount) renderHealthMount(this.screenWidth, this.screenHeight, mStack);
                if (renderAir)    renderAir(this.screenWidth, this.screenHeight, mStack);
            }

            if (renderJumpBar)
            {
                renderJumpMeter(mStack, this.screenWidth / 2 - 91);
            }
            else if (renderExperiance)
            {
                renderExperience(this.screenWidth / 2 - 91, mStack);
            }
            if (this.minecraft.options.heldItemTooltips && this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
                this.renderSelectedItemName(mStack);
             } else if (this.minecraft.player.isSpectator()) {
                this.spectatorGui.renderTooltip(mStack);
             }
        }

        renderSleepFade(this.screenWidth, this.screenHeight, mStack);

        renderHUDText(this.screenWidth, this.screenHeight, mStack);
        renderFPSGraph(mStack);
        renderEffects(mStack);
        if (!minecraft.options.hideGui) {
            renderRecordOverlay(this.screenWidth, this.screenHeight, partialTicks, mStack);
            renderSubtitles(mStack);
            renderTitle(this.screenWidth, this.screenHeight, partialTicks, mStack);
        }


        Scoreboard scoreboard = this.minecraft.level.getScoreboard();
        ScoreObjective objective = null;
        ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(minecraft.player.getScoreboardName());
        if (scoreplayerteam != null)
        {
            int slot = scoreplayerteam.getColor().getId();
            if (slot >= 0) objective = scoreboard.getDisplayObjective(3 + slot);
        }
        ScoreObjective scoreobjective1 = objective != null ? objective : scoreboard.getDisplayObjective(1);
        if (renderObjective && scoreobjective1 != null)
        {
            this.displayScoreboardSidebar(mStack, scoreobjective1);
        }

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        RenderSystem.disableAlphaTest();

        renderChat(this.screenWidth, this.screenHeight, mStack);

        renderPlayerList(this.screenWidth, this.screenHeight, mStack);

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableAlphaTest();

        post(ALL, mStack);
    }

    @Override
    protected void renderCrosshair(MatrixStack mStack)
    {
        if (pre(CROSSHAIRS, mStack)) return;
        bind(AbstractGui.GUI_ICONS_LOCATION);
        RenderSystem.enableBlend();
        RenderSystem.enableAlphaTest();
        GameSettings gamesettings = this.minecraft.options;
        if (gamesettings.getCameraType().isFirstPerson()) {
           if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR || this.canRenderCrosshairForSpectator(this.minecraft.hitResult)) {
              if (gamesettings.renderDebug && !gamesettings.hideGui && !this.minecraft.player.isReducedDebugInfo() && !gamesettings.reducedDebugInfo) {
                 RenderSystem.pushMatrix();
                 RenderSystem.translatef((float)(this.screenWidth / 2), (float)(this.screenHeight / 2), (float)this.getBlitOffset());
                 ActiveRenderInfo activerenderinfo = this.minecraft.gameRenderer.getMainCamera();
                 RenderSystem.rotatef(activerenderinfo.getXRot(), -1.0F, 0.0F, 0.0F);
                 RenderSystem.rotatef(activerenderinfo.getYRot(), 0.0F, 1.0F, 0.0F);
                 RenderSystem.scalef(-1.0F, -1.0F, -1.0F);
                 RenderSystem.renderCrosshair(10);
                 RenderSystem.popMatrix();
              } else {
                 RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                 int i = 15;
                 this.blit(mStack, (this.screenWidth - 15) / 2, (this.screenHeight - 15) / 2, 0, 0, 15, 15);
                 if (this.minecraft.options.attackIndicator == AttackIndicatorStatus.CROSSHAIR) {
                    float f = this.minecraft.player.getAttackStrengthScale(0.0F);
                    boolean flag = false;
                    if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && f >= 1.0F) {
                       flag = this.minecraft.player.getCurrentItemAttackStrengthDelay() > 5.0F;
                       flag = flag & this.minecraft.crosshairPickEntity.isAlive();
                    }

                    int j = this.screenHeight / 2 - 7 + 16;
                    int k = this.screenWidth / 2 - 8;
                    if (flag) {
                       this.blit(mStack, k, j, 68, 94, 16, 16);
                    } else if (f < 1.0F) {
                       int l = (int)(f * 17.0F);
                       this.blit(mStack, k, j, 36, 94, 16, 4);
                       this.blit(mStack, k, j, 52, 94, l, 4);
                    }
                 }
              }

           }
        }

        post(CROSSHAIRS, mStack);
    }

    @Override
    protected void renderEffects(MatrixStack mStack)
    {
        if (pre(POTION_ICONS, mStack)) return;
        Collection<EffectInstance> collection = this.minecraft.player.getActiveEffects();
        if (!collection.isEmpty()) {
           RenderSystem.enableBlend();
           int i = 0;
           int j = 0;
           PotionSpriteUploader potionspriteuploader = this.minecraft.getMobEffectTextures();
           List<Runnable> list = Lists.newArrayListWithExpectedSize(collection.size());
           this.minecraft.getTextureManager().bind(ContainerScreen.INVENTORY_LOCATION);

           for(EffectInstance effectinstance : Ordering.natural().reverse().sortedCopy(collection)) {
              Effect effect = effectinstance.getEffect();
              if (!effectinstance.shouldRenderHUD()) continue;
              // Rebind in case previous renderHUDEffect changed texture
              this.minecraft.getTextureManager().bind(ContainerScreen.INVENTORY_LOCATION);
              if (effectinstance.showIcon()) {
                 int k = this.screenWidth;
                 int l = 1;
                 if (this.minecraft.isDemo()) {
                    l += 15;
                 }

                 if (effect.isBeneficial()) {
                    ++i;
                    k = k - 25 * i;
                 } else {
                    ++j;
                    k = k - 25 * j;
                    l += 26;
                 }

                 RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                 float f = 1.0F;
                 if (effectinstance.isAmbient()) {
                    this.blit(mStack, k, l, 165, 166, 24, 24);
                 } else {
                    this.blit(mStack, k, l, 141, 166, 24, 24);
                    if (effectinstance.getDuration() <= 200) {
                       int i1 = 10 - effectinstance.getDuration() / 20;
                       f = MathHelper.clamp((float)effectinstance.getDuration() / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + MathHelper.cos((float)effectinstance.getDuration() * (float)Math.PI / 5.0F) * MathHelper.clamp((float)i1 / 10.0F * 0.25F, 0.0F, 0.25F);
                    }
                 }

                 TextureAtlasSprite textureatlassprite = potionspriteuploader.get(effect);
                 int j1 = k;
                 int k1 = l;
                 float f1 = f;
                 list.add(() -> {
                    this.minecraft.getTextureManager().bind(textureatlassprite.atlas().location());
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, f1);
                    blit(mStack, j1 + 3, k1 + 3, this.getBlitOffset(), 18, 18, textureatlassprite);
                 });
                 effectinstance.renderHUDEffect(this, mStack, k, l, this.getBlitOffset(), f);
              }
           }

           list.forEach(Runnable::run);
        }
        post(POTION_ICONS, mStack);
    }

    protected void renderSubtitles(MatrixStack mStack)
    {
        if (pre(SUBTITLES, mStack)) return;
        this.subtitleOverlay.render(mStack);
        post(SUBTITLES, mStack);
    }

    protected void renderBossHealth(MatrixStack mStack)
    {
        if (pre(BOSSHEALTH, mStack)) return;
        bind(AbstractGui.GUI_ICONS_LOCATION);
        RenderSystem.defaultBlendFunc();
        minecraft.getProfiler().push("bossHealth");
        RenderSystem.enableBlend();
        this.bossOverlay.render(mStack);
        RenderSystem.disableBlend();
        minecraft.getProfiler().pop();
        post(BOSSHEALTH, mStack);
    }

    @Override
    protected void renderVignette(Entity entity)
    {
        MatrixStack mStack = new MatrixStack();
        if (pre(VIGNETTE, mStack))
        {
            // Need to put this here, since Vanilla assumes this state after the vignette was rendered.
            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();
            return;
        }
        WorldBorder worldborder = this.minecraft.level.getWorldBorder();
        float f = (float)worldborder.getDistanceToBorder(entity);
        double d0 = Math.min(worldborder.getLerpSpeed() * (double)worldborder.getWarningTime() * 1000.0D, Math.abs(worldborder.getLerpTarget() - worldborder.getSize()));
        double d1 = Math.max((double)worldborder.getWarningBlocks(), d0);
        if ((double)f < d1) {
           f = 1.0F - (float)((double)f / d1);
        } else {
           f = 0.0F;
        }

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        if (f > 0.0F) {
           RenderSystem.color4f(0.0F, f, f, 1.0F);
        } else {
           RenderSystem.color4f(this.vignetteBrightness, this.vignetteBrightness, this.vignetteBrightness, 1.0F);
        }

        this.minecraft.getTextureManager().bind(VIGNETTE_LOCATION);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.vertex(0.0D, (double)this.screenHeight, -90.0D).uv(0.0F, 1.0F).endVertex();
        bufferbuilder.vertex((double)this.screenWidth, (double)this.screenHeight, -90.0D).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex((double)this.screenWidth, 0.0D, -90.0D).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(0.0D, 0.0D, -90.0D).uv(0.0F, 0.0F).endVertex();
        tessellator.end();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        post(VIGNETTE, mStack);
    }


    private void renderHelmet(float partialTicks, MatrixStack mStack)
    {
        if (pre(HELMET, mStack)) return;

        ItemStack itemstack = this.minecraft.player.inventory.getArmor(3);

        if (this.minecraft.options.getCameraType().isFirstPerson() && !itemstack.isEmpty())
        {
            Item item = itemstack.getItem();
            if (item == Blocks.CARVED_PUMPKIN.asItem())
            {
                renderPumpkin();
            }
            else
            {
                item.renderHelmetOverlay(itemstack, minecraft.player, this.screenWidth, this.screenHeight, partialTicks);
            }
        }

        post(HELMET, mStack);
    }

    protected void renderArmor(MatrixStack mStack, int width, int height)
    {
        if (pre(ARMOR, mStack)) return;
        minecraft.getProfiler().push("armor");

        RenderSystem.enableBlend();
        int left = width / 2 - 91;
        int top = height - left_height;

        int level = minecraft.player.getArmorValue();
        for (int i = 1; level > 0 && i < 20; i += 2)
        {
            if (i < level)
            {
                blit(mStack, left, top, 34, 9, 9, 9);
            }
            else if (i == level)
            {
                blit(mStack, left, top, 25, 9, 9, 9);
            }
            else if (i > level)
            {
                blit(mStack, left, top, 16, 9, 9, 9);
            }
            left += 8;
        }
        left_height += 10;

        RenderSystem.disableBlend();
        minecraft.getProfiler().pop();
        post(ARMOR, mStack);
    }

    @Override
    protected void renderPortalOverlay(float partialTicks)
    {
        MatrixStack mStack = new MatrixStack();
        if (pre(PORTAL, mStack)) return;

        float f1 = minecraft.player.oPortalTime + (minecraft.player.portalTime - minecraft.player.oPortalTime) * partialTicks;

        if (f1 > 0.0F)
        {
        	if (partialTicks < 1.0F) {
        		partialTicks = partialTicks * partialTicks * partialTicks * 0.8F + 0.2F;
             }

             RenderSystem.disableAlphaTest();
             RenderSystem.disableDepthTest();
             RenderSystem.depthMask(false);
             RenderSystem.defaultBlendFunc();
             RenderSystem.color4f(1.0F, 1.0F, 1.0F, partialTicks);
             this.minecraft.getTextureManager().bind(AtlasTexture.LOCATION_BLOCKS);
             TextureAtlasSprite textureatlassprite = this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
             float f = textureatlassprite.getU0();
             float f11 = textureatlassprite.getV0();
             float f2 = textureatlassprite.getU1();
             float f3 = textureatlassprite.getV1();
             Tessellator tessellator = Tessellator.getInstance();
             BufferBuilder bufferbuilder = tessellator.getBuilder();
             bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
             bufferbuilder.vertex(0.0D, (double)this.screenHeight, -90.0D).uv(f, f3).endVertex();
             bufferbuilder.vertex((double)this.screenWidth, (double)this.screenHeight, -90.0D).uv(f2, f3).endVertex();
             bufferbuilder.vertex((double)this.screenWidth, 0.0D, -90.0D).uv(f2, f11).endVertex();
             bufferbuilder.vertex(0.0D, 0.0D, -90.0D).uv(f, f11).endVertex();
             tessellator.end();
             RenderSystem.depthMask(true);
             RenderSystem.enableDepthTest();
             RenderSystem.enableAlphaTest();
             RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        }

        post(PORTAL, mStack);
    }

    @Override
    protected void renderHotbar(float partialTicks, MatrixStack mStack)
    {
        if (pre(HOTBAR, mStack)) return;

        if (minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR)
        {
            this.spectatorGui.renderHotbar(mStack, partialTicks);
        }
        else
        {
        	PlayerEntity playerentity = this.getCameraPlayer();
            if (playerentity != null) {
               RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
               this.minecraft.getTextureManager().bind(WIDGETS_LOCATION);
               ItemStack itemstack = playerentity.getOffhandItem();
               HandSide handside = playerentity.getMainArm().getOpposite();
               int i = this.screenWidth / 2;
               int j = this.getBlitOffset();
               int k = 182;
               int l = 91;
               this.setBlitOffset(-90);
               this.blit(mStack, i - 91, this.screenHeight - 22, 0, 0, 182, 22);
               this.blit(mStack, i - 91 - 1 + playerentity.inventory.selected * 20, this.screenHeight - 22 - 1, 0, 22, 24, 22);
               if (!itemstack.isEmpty()) {
                  if (handside == HandSide.LEFT) {
                     this.blit(mStack, i - 91 - 29, this.screenHeight - 23, 24, 22, 29, 24);
                  } else {
                     this.blit(mStack, i + 91, this.screenHeight - 23, 53, 22, 29, 24);
                  }
               }

               this.setBlitOffset(j);
               RenderSystem.enableRescaleNormal();
               RenderSystem.enableBlend();
               RenderSystem.defaultBlendFunc();

               for(int i1 = 0; i1 < 9; ++i1) {
                  int j1 = i - 90 + i1 * 20 + 2;
                  int k1 = this.screenHeight - 16 - 3;
                  this.renderSlot(j1, k1, partialTicks, playerentity, playerentity.inventory.items.get(i1));
               }

               if (!itemstack.isEmpty()) {
                  int i2 = this.screenHeight - 16 - 3;
                  if (handside == HandSide.LEFT) {
                     this.renderSlot(i - 91 - 26, i2, partialTicks, playerentity, itemstack);
                  } else {
                     this.renderSlot(i + 91 + 10, i2, partialTicks, playerentity, itemstack);
                  }
               }

               if (this.minecraft.options.attackIndicator == AttackIndicatorStatus.HOTBAR) {
                  float f = this.minecraft.player.getAttackStrengthScale(0.0F);
                  if (f < 1.0F) {
                     int j2 = this.screenHeight - 20;
                     int k2 = i + 91 + 6;
                     if (handside == HandSide.RIGHT) {
                        k2 = i - 91 - 22;
                     }

                     this.minecraft.getTextureManager().bind(AbstractGui.GUI_ICONS_LOCATION);
                     int l1 = (int)(f * 19.0F);
                     RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                     this.blit(mStack, k2, j2, 0, 94, 18, 18);
                     this.blit(mStack, k2, j2 + 18 - l1, 18, 112 - l1, 18, l1);
                  }
               }

               RenderSystem.disableRescaleNormal();
               RenderSystem.disableBlend();
            }
        }

        post(HOTBAR, mStack);
    }

    protected void renderAir(int width, int height, MatrixStack mStack)
    {
        if (pre(AIR, mStack)) return;
        minecraft.getProfiler().push("air");
        PlayerEntity player = (PlayerEntity)this.minecraft.getCameraEntity();
        RenderSystem.enableBlend();
        int left = width / 2 + 91;
        int top = height - right_height;

        int air = player.getAirSupply();
        if (player.isEyeInFluid(FluidTags.WATER) || air < 300)
        {
            int full = MathHelper.ceil((double)(air - 2) * 10.0D / 300.0D);
            int partial = MathHelper.ceil((double)air * 10.0D / 300.0D) - full;

            for (int i = 0; i < full + partial; ++i)
            {
                blit(mStack, left - i * 8 - 9, top, (i < full ? 16 : 25), 18, 9, 9);
            }
            right_height += 10;
        }

        RenderSystem.disableBlend();
        minecraft.getProfiler().pop();
        post(AIR, mStack);
    }

    public void renderHealth(int width, int height, MatrixStack mStack)
    {
        bind(GUI_ICONS_LOCATION);
        if (pre(HEALTH, mStack)) return;
        minecraft.getProfiler().push("health");
        RenderSystem.enableBlend();

        PlayerEntity player = (PlayerEntity)this.minecraft.getCameraEntity();
        int health = MathHelper.ceil(player.getHealth());
        boolean highlight = healthBlinkTime > (long)tickCount && (healthBlinkTime - (long)tickCount) / 3L %2L == 1L;
        if (health < this.lastHealth && player.invulnerableTime > 0)
        {
            this.lastHealthTime = Util.getMillis();
            this.healthBlinkTime = (long)(this.tickCount + 20);
        }
        else if (health > this.lastHealth && player.invulnerableTime > 0)
        {
            this.lastHealthTime = Util.getMillis();
            this.healthBlinkTime = (long)(this.tickCount + 10);
        }
        if (Util.getMillis() - this.lastHealthTime > 1000L)
        {
            this.lastHealth = health;
            this.displayHealth = health;
            this.lastHealthTime = Util.getMillis();
        }
        this.lastHealth = health;
        int healthLast = this.displayHealth;

        ModifiableAttributeInstance attrMaxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        float healthMax = (float)attrMaxHealth.getValue();
        float absorb = MathHelper.ceil(player.getAbsorptionAmount());

        int healthRows = MathHelper.ceil((healthMax + absorb) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);

        this.random.setSeed((long)(tickCount * 312871));

        int left = width / 2 - 91;
        int top = height - left_height;
        left_height += (healthRows * rowHeight);
        if (rowHeight != 10) left_height += 10 - rowHeight;

        int regen = -1;
        if (player.hasEffect(Effects.REGENERATION))
        {
            regen = tickCount % 25;
        }

        // int TOP =  9 * (minecraft.level.getLevelData().isHardcore() ? 5 : 0);
        int TOP = 0;
        final int BACKGROUND = (highlight ? 25 : 16);
        int MARGIN = 16;
        if (player.hasEffect(Effects.POISON))      MARGIN += 36;
        else if (player.hasEffect(Effects.WITHER)) MARGIN += 72;
        float absorbRemaining = absorb;
        for (int i = MathHelper.ceil((healthMax + absorb) / 2.0F) - 1; i >= 0; --i)
        {
        	TOP = i < 3 ? 45 : 0;
            int row = MathHelper.ceil((float)(i + 1) / 10.0F) - 1;
            int x = left + i % 10 * 8;
            int y = top - row * rowHeight;

            if (health <= 4) y += random.nextInt(2);
            if (i == regen) y -= 2;

            blit(mStack, x, y, BACKGROUND, TOP, 9, 9);

            if (highlight)
            {
                if (i * 2 + 1 < healthLast)
                    blit(mStack, x, y, MARGIN + 54, TOP, 9, 9); //6
                else if (i * 2 + 1 == healthLast)
                    blit(mStack, x, y, MARGIN + 63, TOP, 9, 9); //7
            }

            if (absorbRemaining > 0.0F)
            {
                if (absorbRemaining == absorb && absorb % 2.0F == 1.0F)
                {
                    blit(mStack, x, y, MARGIN + 153, TOP, 9, 9); //17
                    absorbRemaining -= 1.0F;
                }
                else
                {
                    blit(mStack, x, y, MARGIN + 144, TOP, 9, 9); //16
                    absorbRemaining -= 2.0F;
                }
            }
            else
            {
                if (i * 2 + 1 < health)
                    blit(mStack, x, y, MARGIN + 36, TOP, 9, 9); //4
                else if (i * 2 + 1 == health)
                    blit(mStack, x, y, MARGIN + 45, TOP, 9, 9); //5
            }
        }

        RenderSystem.disableBlend();
        minecraft.getProfiler().pop();
        post(HEALTH, mStack);
    }

    public void renderFood(int width, int height, MatrixStack mStack)
    {
        if (pre(FOOD, mStack)) return;
        minecraft.getProfiler().push("food");

        PlayerEntity player = (PlayerEntity)this.minecraft.getCameraEntity();
        RenderSystem.enableBlend();
        int left = width / 2 + 91;
        int top = height - right_height;
        right_height += 10;
        boolean unused = false;// Unused flag in vanilla, seems to be part of a 'fade out' mechanic

        FoodStats stats = minecraft.player.getFoodData();
        int level = stats.getFoodLevel();

        for (int i = 0; i < 10; ++i)
        {
            int idx = i * 2 + 1;
            int x = left - i * 8 - 9;
            int y = top;
            int icon = 16;
            byte background = 0;

            if (minecraft.player.hasEffect(Effects.HUNGER))
            {
                icon += 36;
                background = 13;
            }
            if (unused) background = 1; //Probably should be a += 1 but vanilla never uses this

            if (player.getFoodData().getSaturationLevel() <= 0.0F && tickCount % (level * 3 + 1) == 0)
            {
                y = top + (random.nextInt(3) - 1);
            }

            blit(mStack, x, y, 16 + background * 9, 27, 9, 9);

            if (idx < level)
                blit(mStack, x, y, icon + 36, 27, 9, 9);
            else if (idx == level)
                blit(mStack, x, y, icon + 45, 27, 9, 9);
        }
        RenderSystem.disableBlend();
        minecraft.getProfiler().pop();
        post(FOOD, mStack);
    }

    protected void renderSleepFade(int width, int height, MatrixStack mStack)
    {
        if (minecraft.player.getSleepTimer() > 0)
        {
            minecraft.getProfiler().push("sleep");
            RenderSystem.disableDepthTest();
            RenderSystem.disableAlphaTest();
            int sleepTime = minecraft.player.getSleepTimer();
            float opacity = (float)sleepTime / 100.0F;

            if (opacity > 1.0F)
            {
                opacity = 1.0F - (float)(sleepTime - 100) / 10.0F;
            }

            int color = (int)(220.0F * opacity) << 24 | 1052704;
            fill(mStack, 0, 0, width, height, color);
            RenderSystem.enableAlphaTest();
            RenderSystem.enableDepthTest();
            minecraft.getProfiler().pop();
        }
    }

    protected void renderExperience(int x, MatrixStack mStack)
    {
        bind(GUI_ICONS_LOCATION);
        if (pre(EXPERIENCE, mStack)) return;
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

        if (minecraft.gameMode.hasExperience())
        {
        	this.minecraft.getProfiler().push("expBar");
            this.minecraft.getTextureManager().bind(AbstractGui.GUI_ICONS_LOCATION);
            int i = this.minecraft.player.getXpNeededForNextLevel();
            if (i > 0) {
               int j = 182;
               int k = (int)(this.minecraft.player.experienceProgress * 183.0F);
               int l = this.screenHeight - 32 + 3;
               this.blit(mStack, x, l, 0, 64, 182, 5);
               if (k > 0) {
                  this.blit(mStack, x, l, 0, 69, k, 5);
               }
            }

            this.minecraft.getProfiler().pop();
            if (this.minecraft.player.experienceLevel > 0) {
               this.minecraft.getProfiler().push("expLevel");
               String s = "" + this.minecraft.player.experienceLevel;
               int i1 = (this.screenWidth - this.getFont().width(s)) / 2;
               int j1 = this.screenHeight - 31 - 4;
               this.getFont().draw(mStack, s, (float)(i1 + 1), (float)j1, 0);
               this.getFont().draw(mStack, s, (float)(i1 - 1), (float)j1, 0);
               this.getFont().draw(mStack, s, (float)i1, (float)(j1 + 1), 0);
               this.getFont().draw(mStack, s, (float)i1, (float)(j1 - 1), 0);
               this.getFont().draw(mStack, s, (float)i1, (float)j1, 8453920);
               this.minecraft.getProfiler().pop();
            }
        }
        RenderSystem.enableBlend();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        post(EXPERIENCE, mStack);
    }

    @Override
    public void renderJumpMeter(MatrixStack mStack, int x)
    {
        bind(GUI_ICONS_LOCATION);
        if (pre(JUMPBAR, mStack)) return;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

        this.minecraft.getProfiler().push("expBar");
        this.minecraft.getTextureManager().bind(AbstractGui.GUI_ICONS_LOCATION);
        int i = this.minecraft.player.getXpNeededForNextLevel();
        if (i > 0) {
           int k = (int)(this.minecraft.player.experienceProgress * 183.0F);
           int l = this.screenHeight - 32 + 3;
           this.blit(mStack, x, l, 0, 64, 182, 5);
           if (k > 0) {
              this.blit(mStack, x, l, 0, 69, k, 5);
           }
        }

        this.minecraft.getProfiler().pop();
        if (this.minecraft.player.experienceLevel > 0) {
           this.minecraft.getProfiler().push("expLevel");
           String s = "" + this.minecraft.player.experienceLevel;
           int i1 = (this.screenWidth - this.getFont().width(s)) / 2;
           int j1 = this.screenHeight - 31 - 4;
           this.getFont().draw(mStack, s, (float)(i1 + 1), (float)j1, 0);
           this.getFont().draw(mStack, s, (float)(i1 - 1), (float)j1, 0);
           this.getFont().draw(mStack, s, (float)i1, (float)(j1 + 1), 0);
           this.getFont().draw(mStack, s, (float)i1, (float)(j1 - 1), 0);
           this.getFont().draw(mStack, s, (float)i1, (float)j1, 8453920);
           this.minecraft.getProfiler().pop();
        }

        RenderSystem.enableBlend();
        minecraft.getProfiler().pop();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        post(JUMPBAR, mStack);
    }

    protected void renderHUDText(int width, int height, MatrixStack mStack)
    {
        minecraft.getProfiler().push("forgeHudText");
        RenderSystem.defaultBlendFunc();
        ArrayList<String> listL = new ArrayList<String>();
        ArrayList<String> listR = new ArrayList<String>();

        if (minecraft.isDemo())
        {
            long time = minecraft.level.getGameTime();
            if (time >= 120500L)
            {
                listR.add(I18n.get("demo.demoExpired"));
            }
            else
            {
                listR.add(I18n.get("demo.remainingTime", StringUtils.formatTickDuration((int)(120500L - time))));
            }
        }

        if (this.minecraft.options.renderDebug && !pre(DEBUG, mStack))
        {
            debugOverlay.update();
            listL.addAll(debugOverlay.getLeft());
            listR.addAll(debugOverlay.getRight());
            post(DEBUG, mStack);
        }

        RenderGameOverlayEvent.Text event = new RenderGameOverlayEvent.Text(mStack, eventParent, listL, listR);
        if (!MinecraftForge.EVENT_BUS.post(event))
        {
            int top = 2;
            for (String msg : listL)
            {
                if (msg == null) continue;
                fill(mStack, 1, top - 1, 2 + fontrenderer.width(msg) + 1, top + fontrenderer.lineHeight - 1, -1873784752);
                fontrenderer.draw(mStack, msg, 2, top, 14737632);
                top += fontrenderer.lineHeight;
            }

            top = 2;
            for (String msg : listR)
            {
                if (msg == null) continue;
                int w = fontrenderer.width(msg);
                int left = width - 2 - w;
                fill(mStack, left - 1, top - 1, left + w + 1, top + fontrenderer.lineHeight - 1, -1873784752);
                fontrenderer.draw(mStack, msg, left, top, 14737632);
                top += fontrenderer.lineHeight;
            }
        }

        minecraft.getProfiler().pop();
        post(TEXT, mStack);
    }

    protected void renderFPSGraph(MatrixStack mStack)
    {
        if (this.minecraft.options.renderDebug && this.minecraft.options.renderFpsChart && !pre(FPS_GRAPH, mStack))
        {
            this.debugOverlay.render(mStack);
            post(FPS_GRAPH, mStack);
        }
    }

    protected void renderRecordOverlay(int width, int height, float partialTicks, MatrixStack mStack)
    {
        if (overlayMessageTime > 0)
        {
            minecraft.getProfiler().push("overlayMessage");
            float hue = (float)overlayMessageTime - partialTicks;
            int opacity = (int)(hue * 255.0F / 20.0F);
            if (opacity > 255) opacity = 255;

            if (opacity > 8)
            {
                RenderSystem.pushMatrix();
                RenderSystem.translatef((float)(width / 2), (float)(height - 68), 0.0F);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                int color = (animateOverlayMessageColor ? MathHelper.hsvToRgb(hue / 50.0F, 0.7F, 0.6F) & WHITE : WHITE);
                drawBackdrop(mStack, fontrenderer, -4, fontrenderer.width(overlayMessageString), 16777215 | (opacity << 24));
                fontrenderer.draw(mStack, overlayMessageString.getVisualOrderText(), -fontrenderer.width(overlayMessageString) / 2, -4, color | (opacity << 24));
                RenderSystem.disableBlend();
                RenderSystem.popMatrix();
            }

            minecraft.getProfiler().pop();
        }
    }

    protected void renderTitle(int width, int height, float partialTicks, MatrixStack mStack)
    {
        if (title != null && titleTime > 0)
        {
            minecraft.getProfiler().push("titleAndSubtitle");
            float age = (float)this.titleTime - partialTicks;
            int opacity = 255;

            if (titleTime > titleFadeOutTime + titleStayTime)
            {
                float f3 = (float)(titleFadeInTime + titleStayTime + titleFadeOutTime) - age;
                opacity = (int)(f3 * 255.0F / (float)titleFadeInTime);
            }
            if (titleTime <= titleFadeOutTime) opacity = (int)(age * 255.0F / (float)this.titleFadeOutTime);

            opacity = MathHelper.clamp(opacity, 0, 255);

            if (opacity > 8)
            {
                RenderSystem.pushMatrix();
                RenderSystem.translatef((float)(width / 2), (float)(height / 2), 0.0F);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.pushMatrix();
                RenderSystem.scalef(4.0F, 4.0F, 4.0F);
                int l = opacity << 24 & -16777216;
                this.getFont().drawShadow(mStack, this.title.getVisualOrderText(), (float)(-this.getFont().width(this.title) / 2), -10.0F, 16777215 | l);
                RenderSystem.popMatrix();
                if (this.subtitle != null)
                {
                    RenderSystem.pushMatrix();
                    RenderSystem.scalef(2.0F, 2.0F, 2.0F);
                    this.getFont().drawShadow(mStack, this.subtitle.getVisualOrderText(), (float)(-this.getFont().width(this.subtitle) / 2), 5.0F, 16777215 | l);
                    RenderSystem.popMatrix();
                }
                RenderSystem.disableBlend();
                RenderSystem.popMatrix();
            }

            this.minecraft.getProfiler().pop();
        }
    }

    protected void renderChat(int width, int height, MatrixStack mStack)
    {
        minecraft.getProfiler().push("chat");

        RenderGameOverlayEvent.Chat event = new RenderGameOverlayEvent.Chat(mStack, eventParent, 0, height - 48);
        if (MinecraftForge.EVENT_BUS.post(event)) return;

        RenderSystem.pushMatrix();
        RenderSystem.translatef((float) event.getPosX(), (float) event.getPosY(), 0.0F);
        chat.render(mStack, tickCount);
        RenderSystem.popMatrix();

        post(CHAT, mStack);

        minecraft.getProfiler().pop();
    }

    protected void renderPlayerList(int width, int height, MatrixStack mStack)
    {
        ScoreObjective scoreobjective = this.minecraft.level.getScoreboard().getDisplayObjective(0);
        ClientPlayNetHandler handler = minecraft.player.connection;

        if (minecraft.options.keyPlayerList.isDown() && (!minecraft.isLocalServer() || handler.getOnlinePlayers().size() > 1 || scoreobjective != null))
        {
            this.tabList.setVisible(true);
            if (pre(PLAYER_LIST, mStack)) return;
            this.tabList.render(mStack, width, this.minecraft.level.getScoreboard(), scoreobjective);
            post(PLAYER_LIST, mStack);
        }
        else
        {
            this.tabList.setVisible(false);
        }
    }

    protected void renderHealthMount(int width, int height, MatrixStack mStack)
    {
        PlayerEntity player = (PlayerEntity)minecraft.getCameraEntity();
        Entity tmp = player.getVehicle();
        if (!(tmp instanceof LivingEntity)) return;

        bind(GUI_ICONS_LOCATION);

        if (pre(HEALTHMOUNT, mStack)) return;

        boolean unused = false;
        int left_align = width / 2 + 91;

        minecraft.getProfiler().popPush("mountHealth");
        RenderSystem.enableBlend();
        LivingEntity mount = (LivingEntity)tmp;
        int health = (int)Math.ceil((double)mount.getHealth());
        float healthMax = mount.getMaxHealth();
        int hearts = (int)(healthMax + 0.5F) / 2;

        if (hearts > 30) hearts = 30;

        final int MARGIN = 52;
        final int BACKGROUND = MARGIN + (unused ? 1 : 0);
        final int HALF = MARGIN + 45;
        final int FULL = MARGIN + 36;

        for (int heart = 0; hearts > 0; heart += 20)
        {
            int top = height - right_height;

            int rowCount = Math.min(hearts, 10);
            hearts -= rowCount;

            for (int i = 0; i < rowCount; ++i)
            {
                int x = left_align - i * 8 - 9;
                blit(mStack, x, top, BACKGROUND, 9, 9, 9);

                if (i * 2 + 1 + heart < health)
                    blit(mStack, x, top, FULL, 9, 9, 9);
                else if (i * 2 + 1 + heart == health)
                    blit(mStack, x, top, HALF, 9, 9, 9);
            }

            right_height += 10;
        }
        RenderSystem.disableBlend();
        post(HEALTHMOUNT, mStack);
    }

    //Helper macros
    private boolean pre(ElementType type, MatrixStack mStack)
    {
        return MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Pre(mStack, eventParent, type));
    }
    private void post(ElementType type, MatrixStack mStack)
    {
        MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Post(mStack, eventParent, type));
    }
    
    
    private void bind(ResourceLocation res)
    {
        minecraft.getTextureManager().bind(res);
    }

    private class GuiOverlayDebugForge extends DebugOverlayGui
    {
        private Minecraft mc;
        private GuiOverlayDebugForge(Minecraft mc)
        {
            super(mc);
            this.mc = mc;
        }
        public void update()
        {
            Entity entity = this.mc.getCameraEntity();
            this.block = entity.pick(rayTraceDistance, 0.0F, false);
            this.liquid = entity.pick(rayTraceDistance, 0.0F, true);
        }
        @Override protected void drawGameInformation(MatrixStack mStack){}
        @Override protected void drawSystemInformation(MatrixStack mStack){}
        private List<String> getLeft()
        {
            List<String> ret = this.getGameInformation();
            ret.add("");
            ret.add("Debug: Pie [shift]: " + (this.mc.options.renderDebugCharts ? "visible" : "hidden") + " FPS [alt]: " + (this.mc.options.renderFpsChart ? "visible" : "hidden"));
            ret.add("For help: press F3 + Q");
            return ret;
        }
        private List<String> getRight(){ return this.getSystemInformation(); }
    }
    
    private boolean canRenderCrosshairForSpectator(RayTraceResult p_212913_1_) {
        if (p_212913_1_ == null) {
           return false;
        } else if (p_212913_1_.getType() == RayTraceResult.Type.ENTITY) {
           return ((EntityRayTraceResult)p_212913_1_).getEntity() instanceof INamedContainerProvider;
        } else if (p_212913_1_.getType() == RayTraceResult.Type.BLOCK) {
           BlockPos blockpos = ((BlockRayTraceResult)p_212913_1_).getBlockPos();
           World world = this.minecraft.level;
           return world.getBlockState(blockpos).getMenuProvider(world, blockpos) != null;
        } else {
           return false;
        }
     }
    private PlayerEntity getCameraPlayer() {
        return !(this.minecraft.getCameraEntity() instanceof PlayerEntity) ? null : (PlayerEntity)this.minecraft.getCameraEntity();
     }
    
    private void renderSlot(int p_184044_1_, int p_184044_2_, float p_184044_3_, PlayerEntity p_184044_4_, ItemStack p_184044_5_) {
        if (!p_184044_5_.isEmpty()) {
           float f = (float)p_184044_5_.getPopTime() - p_184044_3_;
           if (f > 0.0F) {
              RenderSystem.pushMatrix();
              float f1 = 1.0F + f / 5.0F;
              RenderSystem.translatef((float)(p_184044_1_ + 8), (float)(p_184044_2_ + 12), 0.0F);
              RenderSystem.scalef(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
              RenderSystem.translatef((float)(-(p_184044_1_ + 8)), (float)(-(p_184044_2_ + 12)), 0.0F);
           }

           this.itemRenderer.renderAndDecorateItem(p_184044_4_, p_184044_5_, p_184044_1_, p_184044_2_);
           if (f > 0.0F) {
              RenderSystem.popMatrix();
           }

           this.itemRenderer.renderGuiItemDecorations(this.minecraft.font, p_184044_5_, p_184044_1_, p_184044_2_);
        }
     }
}
