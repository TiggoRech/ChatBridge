package com.tiggo.chatbridge.combat;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CombatConfigScreen extends Screen {

    private final @Nullable Screen parent;

    // Geral
    private Checkbox enabled;
    private EditBox cooldownMs;
    private EditBox distanceBlocks;
    private EditBox minHealth;

    // Entidades
    private Checkbox targetPlayers;
    private Checkbox targetMonsters;
    private Checkbox targetAnimals;
    private Checkbox ignoreCreepers;

    // Regras
    private Checkbox ruleRetaliate;
    private Checkbox ruleAssist;
    private Checkbox requireLOS;
    private Checkbox enableStrafe;

    private final List<LabelRef> checkboxLabels = new ArrayList<>();
    private int contentHeight = 0;
    private int scrollY = 0;

    public CombatConfigScreen(@Nullable Screen parent) {
        super(Component.literal("Configuração – ChatBridge Combate"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        final CombatConfig cfg = CombatSystem.CONFIG;

        final int panelW = Math.min(360, this.width - 40);
        final int centerX = this.width / 2;
        final int startX = centerX - panelW / 2;
        int y = 64;

        checkboxLabels.clear();

        // ===== Geral =====
        enabled = new Checkbox(startX + 12, y, 20, 20, Component.empty(), cfg.enabled);
        this.addRenderableWidget(enabled);
        addLabel(enabled, "Ativar combate");
        y += 30;

        cooldownMs = makeNumberBox(startX + 12, y, panelW - 24, String.valueOf(cfg.cooldownMs));
        this.addRenderableWidget(cooldownMs);
        y += 26;

        distanceBlocks = makeNumberBox(startX + 12, y, panelW - 24, String.valueOf(cfg.distanceBlocks));
        this.addRenderableWidget(distanceBlocks);
        y += 26;

        minHealth = makeNumberBox(startX + 12, y, panelW - 24, String.valueOf(cfg.minHealthToEngage));
        this.addRenderableWidget(minHealth);
        y += 30;

        // ===== Entidades =====
        targetPlayers = new Checkbox(startX + 16, y, 20, 20, Component.empty(), cfg.targetPlayers);
        this.addRenderableWidget(targetPlayers);
        addLabel(targetPlayers, "Jogadores");
        y += 24;

        targetMonsters = new Checkbox(startX + 16, y, 20, 20, Component.empty(), cfg.targetMonsters);
        this.addRenderableWidget(targetMonsters);
        addLabel(targetMonsters, "Monstros");
        y += 24;

        targetAnimals = new Checkbox(startX + 16, y, 20, 20, Component.empty(), cfg.targetAnimals);
        this.addRenderableWidget(targetAnimals);
        addLabel(targetAnimals, "Animais");
        y += 24;

        ignoreCreepers = new Checkbox(startX + 16, y, 20, 20, Component.empty(), cfg.ignoreCreepers);
        this.addRenderableWidget(ignoreCreepers);
        addLabel(ignoreCreepers, "Ignorar Creeper");
        y += 30;

        // ===== Regras =====
        ruleRetaliate = new Checkbox(startX + 16, y, 20, 20, Component.empty(), cfg.ruleRetaliateForFollowed);
        this.addRenderableWidget(ruleRetaliate);
        addLabel(ruleRetaliate, "Revidar quem atacar o seguido");
        y += 24;

        ruleAssist = new Checkbox(startX + 16, y, 20, 20, Component.empty(), cfg.ruleAssistFollowed);
        this.addRenderableWidget(ruleAssist);
        addLabel(ruleAssist, "Atacar junto do seguido (exceto Creeper)");
        y += 24;

        requireLOS = new Checkbox(startX + 16, y, 20, 20, Component.empty(), cfg.requireLineOfSight);
        this.addRenderableWidget(requireLOS);
        addLabel(requireLOS, "Exigir linha de visão (raycast)");
        y += 24;

        enableStrafe = new Checkbox(startX + 16, y, 20, 20, Component.empty(), cfg.enableStrafe);
        this.addRenderableWidget(enableStrafe);
        addLabel(enableStrafe, "Strafing leve durante combate");
        y += 36;

        Button done = Button.builder(Component.literal("Concluir"), b -> saveBack())
                .pos(centerX - 60, y)
                .size(120, 20)
                .build();
        this.addRenderableWidget(done);
        y += 28;

        contentHeight = y;
    }

    private static class LabelRef { Checkbox cb; String text; LabelRef(Checkbox c, String t){cb=c;text=t;} }
    private void addLabel(Checkbox cb, String text) { checkboxLabels.add(new LabelRef(cb, text)); }

    private EditBox makeNumberBox(int x, int y, int w, String text) {
        EditBox eb = new EditBox(this.font, x, y, w, 20, Component.empty());
        eb.setMaxLength(20);
        eb.setValue(text);
        return eb;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int viewH = this.height - 72;
        int maxScroll = Math.max(0, contentHeight - viewH);
        scrollY -= (int) (delta * 16);
        if (scrollY < 0) scrollY = 0;
        if (scrollY > maxScroll) scrollY = maxScroll;
        return true;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(g);

        int panelW = Math.min(360, this.width - 40);
        int panelX = (this.width - panelW) / 2;
        int panelY = 32;
        int panelH = this.height - 64;
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xAA000000);

        g.drawCenteredString(this.font, this.title, this.width / 2, 38, 0xFFFFFF);

        drawSection(g, "Geral", panelX + 12, 64 - 18 - scrollY);
        drawSection(g, "Entidades", panelX + 12, 180 - 18 - scrollY);
        drawSection(g, "Regras", panelX + 12, 278 - 18 - scrollY);

        g.drawString(this.font, "Cooldown (ms)", panelX + 16, (94 - scrollY), 0xAAAAAA);
        g.drawString(this.font, "Distância (blocos)", panelX + 16, (120 - scrollY), 0xAAAAAA);
        g.drawString(this.font, "Vida mínima p/ engajar", panelX + 16, (146 - scrollY), 0xAAAAAA);

        layoutWidgetsWithScroll();

        super.render(g, mouseX, mouseY, partialTicks);

        for (LabelRef lr : checkboxLabels) {
            g.drawString(this.font, lr.text, lr.cb.getX() + 24, lr.cb.getY() + 6, 0xFFFFFF);
        }
    }

    private void drawSection(GuiGraphics g, String title, int x, int y) {
        g.drawString(this.font, title, x, y, 0xFFFFFF);
        g.fill(x, y + 10, x + 320, y + 11, 0x55FFFFFF);
    }

    private void layoutWidgetsWithScroll() {
        int panelW = Math.min(360, this.width - 40);
        int centerX = this.width / 2;
        int startX = centerX - panelW / 2;
        int y = 64 - scrollY;

        enabled.setX(startX + 12); enabled.setY(y); y += 30;

        cooldownMs.setX(startX + 12); cooldownMs.setY(y); cooldownMs.setWidth(panelW - 24); y += 26;
        distanceBlocks.setX(startX + 12); distanceBlocks.setY(y); distanceBlocks.setWidth(panelW - 24); y += 26;
        minHealth.setX(startX + 12); minHealth.setY(y); minHealth.setWidth(panelW - 24); y += 30;

        targetPlayers.setX(startX + 16); targetPlayers.setY(y); y += 24;
        targetMonsters.setX(startX + 16); targetMonsters.setY(y); y += 24;
        targetAnimals.setX(startX + 16); targetAnimals.setY(y); y += 24;
        ignoreCreepers.setX(startX + 16); ignoreCreepers.setY(y); y += 30;

        ruleRetaliate.setX(startX + 16); ruleRetaliate.setY(y); y += 24;
        ruleAssist.setX(startX + 16); ruleAssist.setY(y); y += 24;
        requireLOS.setX(startX + 16); requireLOS.setY(y); y += 24;
        enableStrafe.setX(startX + 16); enableStrafe.setY(y); y += 36;

        if (!this.renderables.isEmpty() && this.renderables.get(this.renderables.size() - 1) instanceof Button done) {
            int btnY = Math.min(this.height - 34, y - scrollY);
            done.setX(centerX - 60);
            done.setY(btnY);
        }
    }

    @Override
    public void onClose() { saveBack(); }

    private void saveBack() {
        CombatConfig cfg = CombatSystem.CONFIG;

        cfg.enabled = enabled.selected();

        cfg.cooldownMs = parseIntSafe(cooldownMs.getValue(), cfg.cooldownMs);
        cfg.distanceBlocks = parseDoubleSafe(distanceBlocks.getValue(), cfg.distanceBlocks);
        cfg.minHealthToEngage = (float) parseDoubleSafe(minHealth.getValue(), cfg.minHealthToEngage);

        cfg.targetPlayers  = targetPlayers.selected();
        cfg.targetMonsters = targetMonsters.selected();
        cfg.targetAnimals  = targetAnimals.selected();
        cfg.ignoreCreepers = ignoreCreepers.selected();

        cfg.ruleRetaliateForFollowed = ruleRetaliate.selected();
        cfg.ruleAssistFollowed       = ruleAssist.selected();
        cfg.requireLineOfSight       = requireLOS.selected();
        cfg.enableStrafe             = enableStrafe.selected();

        if (this.minecraft != null) this.minecraft.setScreen(parent);
    }

    private int parseIntSafe(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return fallback; }
    }
    private double parseDoubleSafe(String s, double fallback) {
        try { return Double.parseDouble(s.trim().replace(',', '.')); } catch (Exception e) { return fallback; }
    }
}
