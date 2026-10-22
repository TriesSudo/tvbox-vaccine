package br.vacinatvbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.VpnService;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final int REQ_VPN = 101;

    // Impeccable TV Dark Design Palette (WCAG AAA High-Contrast)
    private static final String C_BG = "#0B0F17";
    private static final String C_SURFACE = "#111827";
    private static final String C_CARD = "#172033";
    private static final String C_CARD_HOVER = "#1E2C44";
    private static final String C_BORDER = "#28354D";
    private static final String C_TEXT_PRIMARY = "#F8FAFC";
    private static final String C_TEXT_SECONDARY = "#94A3B8";
    private static final String C_EMERALD = "#10B981";
    private static final String C_CYAN = "#38BDF8";
    private static final String C_AMBER = "#F59E0B";
    private static final String C_ROSE = "#F43F5E";
    private static final String C_PINK = "#EC4899";

    private TextView appTitle, appSub;
    private TextView step1Badge, step2Badge, step3Badge;
    private Button btnLang;
    private TextView metricDaemons, metricPackages, metricTests;
    private TextView liveHeadline, liveSubline, livePercent;
    private ProgressBar progressBar;
    private TextView outputView;
    private ScrollView scrollView;
    private Button btnMainFlow, btnScanOnly, btnTestOnly, btnUnitManage, btnUndoAll, btnDonateUpdates;

    private volatile boolean busy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor(C_BG));
        root.setPadding(dp(22), dp(14), dp(22), dp(14));

        // 1. TOP HEADER BAR: Logo + Brand + Pipeline Stages + Language Switcher
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setBackground(box(C_SURFACE, C_BORDER, 8, 1));
        topBar.setPadding(dp(14), dp(8), dp(14), dp(8));

        ImageView logoView = new ImageView(this);
        logoView.setImageResource(R.drawable.ic_launcher);
        LinearLayout.LayoutParams logoLp = new LinearLayout.LayoutParams(dp(42), dp(42));
        logoLp.setMargins(0, 0, dp(12), 0);
        topBar.addView(logoView, logoLp);

        LinearLayout titleCol = new LinearLayout(this);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

        appTitle = new TextView(this);
        appTitle.setTextColor(Color.parseColor(C_TEXT_PRIMARY));
        appTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.5f);
        appTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        titleCol.addView(appTitle);

        appSub = new TextView(this);
        appSub.setTextColor(Color.parseColor(C_TEXT_SECONDARY));
        appSub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        titleCol.addView(appSub);
        topBar.addView(titleCol, titleLp);

        LinearLayout stepsRow = new LinearLayout(this);
        stepsRow.setOrientation(LinearLayout.HORIZONTAL);
        stepsRow.setGravity(Gravity.CENTER_VERTICAL);

        step1Badge = makeStepChip("");
        step2Badge = makeStepChip("");
        step3Badge = makeStepChip("");

        stepsRow.addView(step1Badge);
        stepsRow.addView(makeArrow());
        stepsRow.addView(step2Badge);
        stepsRow.addView(makeArrow());
        stepsRow.addView(step3Badge);

        btnLang = new Button(this);
        btnLang.setId(1007);
        btnLang.setAllCaps(false);
        btnLang.setTextColor(Color.parseColor(C_TEXT_PRIMARY));
        btnLang.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        btnLang.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        btnLang.setBackground(box(C_CARD, C_CYAN, 6, 1));
        btnLang.setPadding(dp(12), dp(4), dp(12), dp(4));
        btnLang.setFocusable(true);
        btnLang.setFocusableInTouchMode(true);
        btnLang.setOnFocusChangeListener((v, hasFocus) -> {
            btnLang.setBackground(box(hasFocus ? "#0C4A6E" : C_CARD, C_CYAN, 6, hasFocus ? 3 : 1));
            btnLang.setScaleX(hasFocus ? 1.04f : 1.0f);
            btnLang.setScaleY(hasFocus ? 1.04f : 1.0f);
        });
        btnLang.setOnClickListener(v -> {
            if (busy) return;
            ShieldEngine.toggleLanguage();
            refreshLocalizedLabels();
            runTask(cb -> ShieldEngine.scanSystem(this, cb));
        });
        LinearLayout.LayoutParams langLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, dp(34));
        langLp.setMargins(dp(12), 0, 0, 0);
        stepsRow.addView(btnLang, langLp);

        topBar.addView(stepsRow);
        root.addView(topBar);

        // 2. HERO TELEMETRY: Live Status + Progress Bar + 3 Metrics Cards
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setBackground(box(C_SURFACE, C_BORDER, 8, 1));
        hero.setPadding(dp(16), dp(10), dp(16), dp(10));
        LinearLayout.LayoutParams heroLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        heroLp.setMargins(0, dp(8), 0, dp(8));

        LinearLayout statusHeader = new LinearLayout(this);
        statusHeader.setOrientation(LinearLayout.HORIZONTAL);
        statusHeader.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout statusTextCol = new LinearLayout(this);
        statusTextCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams stLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

        liveHeadline = new TextView(this);
        liveHeadline.setTextColor(Color.parseColor(C_TEXT_PRIMARY));
        liveHeadline.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        liveHeadline.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        statusTextCol.addView(liveHeadline);

        liveSubline = new TextView(this);
        liveSubline.setTextColor(Color.parseColor(C_TEXT_SECONDARY));
        liveSubline.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        liveSubline.setPadding(0, dp(2), 0, 0);
        statusTextCol.addView(liveSubline);
        statusHeader.addView(statusTextCol, stLp);

        livePercent = new TextView(this);
        livePercent.setText("0%");
        livePercent.setTextColor(Color.parseColor(C_EMERALD));
        livePercent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        livePercent.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        livePercent.setPadding(dp(12), 0, 0, 0);
        statusHeader.addView(livePercent);
        hero.addView(statusHeader);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        progressBar.setProgressDrawable(makeProgressDrawable());
        LinearLayout.LayoutParams pbLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(8));
        pbLp.setMargins(0, dp(6), 0, dp(8));
        hero.addView(progressBar, pbLp);

        LinearLayout metricsRow = new LinearLayout(this);
        metricsRow.setOrientation(LinearLayout.HORIZONTAL);
        metricDaemons = makeMetricPill("");
        metricPackages = makeMetricPill("");
        metricTests = makeMetricPill("");

        LinearLayout.LayoutParams mLp1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        mLp1.setMargins(0, 0, dp(6), 0);
        LinearLayout.LayoutParams mLp2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        mLp2.setMargins(dp(3), 0, dp(3), 0);
        LinearLayout.LayoutParams mLp3 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        mLp3.setMargins(dp(6), 0, 0, 0);

        metricsRow.addView(metricDaemons, mLp1);
        metricsRow.addView(metricPackages, mLp2);
        metricsRow.addView(metricTests, mLp3);
        hero.addView(metricsRow);
        root.addView(hero, heroLp);

        // 3. MAIN WORKSPACE: Button Action Column (Left) + Audit Terminal (Right)
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams bodyLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);

        LinearLayout leftCol = new LinearLayout(this);
        leftCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams leftLp = new LinearLayout.LayoutParams(
            dp(320), LinearLayout.LayoutParams.MATCH_PARENT);
        leftLp.setMargins(0, 0, dp(12), 0);

        btnMainFlow = makeActionButton("#064E3B", C_EMERALD, true);
        btnScanOnly = makeActionButton(C_CARD, C_CYAN, false);
        btnTestOnly = makeActionButton(C_CARD, C_CYAN, false);
        btnUnitManage = makeActionButton(C_CARD, C_AMBER, false);
        btnUndoAll = makeActionButton("#3B1219", C_ROSE, false);
        btnDonateUpdates = makeActionButton("#381335", C_PINK, false);

        btnMainFlow.setId(1001);
        btnScanOnly.setId(1002);
        btnTestOnly.setId(1003);
        btnUnitManage.setId(1004);
        btnUndoAll.setId(1005);
        btnDonateUpdates.setId(1006);

        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        btnLp.setMargins(0, 0, 0, dp(6));

        leftCol.addView(btnMainFlow, btnLp);
        leftCol.addView(btnScanOnly, btnLp);
        leftCol.addView(btnTestOnly, btnLp);
        leftCol.addView(btnUnitManage, btnLp);
        leftCol.addView(btnUndoAll, btnLp);
        leftCol.addView(btnDonateUpdates, btnLp);

        body.addView(leftCol, leftLp);

        scrollView = new ScrollView(this);
        scrollView.setId(2001);
        scrollView.setFocusable(true);
        scrollView.setFocusableInTouchMode(true);
        LinearLayout.LayoutParams rightLp = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        scrollView.setBackground(box(C_SURFACE, C_BORDER, 8, 2));
        scrollView.setPadding(dp(16), dp(12), dp(16), dp(12));
        scrollView.setOnFocusChangeListener((v, hasFocus) -> {
            scrollView.setBackground(box(C_SURFACE, hasFocus ? C_CYAN : C_BORDER, 8, hasFocus ? 3 : 2));
        });

        outputView = new TextView(this);
        outputView.setTextColor(Color.parseColor(C_TEXT_PRIMARY));
        outputView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        outputView.setTypeface(Typeface.MONOSPACE);
        outputView.setLineSpacing(dp(2), 1.05f);
        scrollView.addView(outputView);
        body.addView(scrollView, rightLp);

        root.addView(body, bodyLp);

        // TV Remote D-Pad Navigation Mapping
        btnMainFlow.setNextFocusUpId(1007);
        btnMainFlow.setNextFocusDownId(1002);
        btnScanOnly.setNextFocusUpId(1001);
        btnScanOnly.setNextFocusDownId(1003);
        btnTestOnly.setNextFocusUpId(1002);
        btnTestOnly.setNextFocusDownId(1004);
        btnUnitManage.setNextFocusUpId(1003);
        btnUnitManage.setNextFocusDownId(1005);
        btnUndoAll.setNextFocusUpId(1004);
        btnUndoAll.setNextFocusDownId(1006);
        btnDonateUpdates.setNextFocusUpId(1005);
        btnLang.setNextFocusDownId(1001);

        btnMainFlow.setNextFocusRightId(2001);
        btnScanOnly.setNextFocusRightId(2001);
        btnTestOnly.setNextFocusRightId(2001);
        btnUnitManage.setNextFocusRightId(2001);
        btnUndoAll.setNextFocusRightId(2001);
        btnDonateUpdates.setNextFocusRightId(2001);
        scrollView.setNextFocusLeftId(1001);

        btnMainFlow.setOnClickListener(v -> triggerProtectFlow());
        btnScanOnly.setOnClickListener(v -> runTask(cb -> ShieldEngine.scanSystem(this, cb)));
        btnTestOnly.setOnClickListener(v -> runTask(cb -> ShieldEngine.runBlockingTests(this, cb)));
        btnUnitManage.setOnClickListener(v -> showUnitPackageDialog());
        btnUndoAll.setOnClickListener(v -> showUndoAllWarningDialog());
        btnDonateUpdates.setOnClickListener(v -> {
            runTask(cb -> ShieldEngine.checkUpdatesAndSupportInfo(this, cb));
            showDonateQrModal();
        });

        refreshLocalizedLabels();
        setContentView(root);
        btnMainFlow.requestFocus();

        runTask(cb -> ShieldEngine.scanSystem(this, cb));
    }

    private void refreshLocalizedLabels() {
        appTitle.setText(ShieldEngine.tr(
            "TV Box Vaccine v" + ShieldEngine.APP_VERSION + "  -  by " + ShieldEngine.AUTHOR_NAME,
            "Vacina TV Box v" + ShieldEngine.APP_VERSION + "  -  por " + ShieldEngine.AUTHOR_NAME
        ));
        appSub.setText(ShieldEngine.tr(
            "Anti-Residential Proxy Shield, Daemon Killer & Silent Installer Blocker - triessudo.zip",
            "Escudo Anti-Proxy Residencial, Desligamento de Daemons e Bloqueio de Instaladores - triessudo.zip"
        ));
        step1Badge.setText(ShieldEngine.tr("STEP 1: ANALYZE", "ETAPA 1: ANALISAR"));
        step2Badge.setText(ShieldEngine.tr("STEP 2: VACCINATE", "ETAPA 2: VACINAR"));
        step3Badge.setText(ShieldEngine.tr("STEP 3: TEST AUDIT", "ETAPA 3: AUDITORIA"));
        btnLang.setText(ShieldEngine.isPt() ? "PT-BR  |  SWITCH TO EN" : "EN-US  |  MUDAR PARA PT");

        btnMainFlow.setText(ShieldEngine.tr(
            "1. Analyze + Vaccinate + Test\nExecute full pipeline: scan, stop proxy daemons & verify",
            "1. Analisar + Vacinar + Testar\nExecuta todo o pipeline: scan, parar daemons e verificar"
        ));
        btnScanOnly.setText(ShieldEngine.tr(
            "2. Analyze System Only\nInspect running daemons and routes without making changes",
            "2. Apenas Analisar Sistema\nInspeciona daemons e rotas atuais sem alterar nada"
        ));
        btnTestOnly.setText(ShieldEngine.tr(
            "3. Test Blocking Only\nVerify on-device TCP socket blocks and daemon status",
            "3. Apenas Testar Bloqueio\nVerifica bloqueios reais de sockets TCP e estado dos daemons"
        ));
        btnUnitManage.setText(ShieldEngine.tr(
            "4. Manage Single App\nChoose individual hidden installer to freeze or unlock",
            "4. Gerenciar App Unitario\nEscolha instalador especifico para congelar ou liberar"
        ));
        btnUndoAll.setText(ShieldEngine.tr(
            "5. Undo All Protections (Risk)\nRevert vaccine and restore original factory behavior",
            "5. Desfazer Tudo (Risco)\nReverte todas as protecoes e restaura padrao de fabrica"
        ));
        btnDonateUpdates.setText(ShieldEngine.tr(
            "6. Donate (Crypto QR) & Info\nSupport Suzan (@TriesSudo) - triessudo.zip",
            "6. Doar (QR Cripto) e Info\nApoie Suzan (@TriesSudo) - triessudo.zip"
        ));
    }

    private void showDonateQrModal() {
        LinearLayout modal = new LinearLayout(this);
        modal.setOrientation(LinearLayout.VERTICAL);
        modal.setBackgroundColor(Color.parseColor("#0F172A"));
        modal.setPadding(dp(20), dp(16), dp(20), dp(14));

        TextView header = new TextView(this);
        header.setText(ShieldEngine.tr(
            "Support Suzan (@TriesSudo) - Anti-Malware Research & Updates",
            "Apoie Suzan (@TriesSudo) - Pesquisa Anti-Malware e Atualizacoes"
        ));
        header.setTextColor(Color.parseColor("#F472B6"));
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        header.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        modal.addView(header);

        TextView sub = new TextView(this);
        sub.setText(ShieldEngine.tr(
            "If this tool protected your TV Box and residential network, consider donating to keep this work free.\n" +
            "Site: https://triessudo.zip   |   Email: triessudo@atomicmail.io   |   Telegram: t.me/triessudo",
            "Se esta ferramenta protegeu sua TV Box e sua rede residencial, considere apoiar para manter o projeto livre.\n" +
            "Site: https://triessudo.zip   |   Email: triessudo@atomicmail.io   |   Telegram: t.me/triessudo"
        ));
        sub.setTextColor(Color.parseColor(C_TEXT_PRIMARY));
        sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        sub.setPadding(0, dp(4), 0, dp(12));
        modal.addView(sub);

        LinearLayout cardsRow = new LinearLayout(this);
        cardsRow.setOrientation(LinearLayout.HORIZONTAL);
        cardsRow.setGravity(Gravity.CENTER);

        cardsRow.addView(makeQrCard("BTC - Bitcoin", ShieldEngine.WALLET_BTC, R.drawable.donate_btc, "#F59E0B"),
            new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        cardsRow.addView(makeQrCard("ETH - Ethereum", ShieldEngine.WALLET_ETH, R.drawable.donate_eth, "#818CF8"),
            new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        cardsRow.addView(makeQrCard("SOL - Solana", ShieldEngine.WALLET_SOL, R.drawable.donate_sol, "#34D399"),
            new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        modal.addView(cardsRow);

        new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setView(modal)
            .setPositiveButton(ShieldEngine.tr("Close", "Fechar"), null)
            .show();
    }

    private View makeQrCard(String coinTitle, String address, int drawableRes, String accentHex) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setBackground(box(C_CARD, accentHex, 8, 2));
        card.setPadding(dp(8), dp(8), dp(8), dp(8));

        TextView t = new TextView(this);
        t.setText(coinTitle);
        t.setTextColor(Color.parseColor(accentHex));
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        t.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        card.addView(t);

        ImageView iv = new ImageView(this);
        iv.setImageResource(drawableRes);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(dp(220), dp(155));
        ivLp.setMargins(0, dp(4), 0, dp(4));
        card.addView(iv, ivLp);

        TextView addr = new TextView(this);
        addr.setText(address);
        addr.setGravity(Gravity.CENTER);
        addr.setTextColor(Color.parseColor(C_TEXT_SECONDARY));
        addr.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.5f);
        addr.setTypeface(Typeface.MONOSPACE);
        card.addView(addr);

        return card;
    }

    private void showUnitPackageDialog() {
        if (busy) return;
        final int count = ShieldEngine.RISKY_PACKAGES.length;
        final String[] items = new String[count];
        final boolean[] currentEnabled = new boolean[count];

        for (int i = 0; i < count; i++) {
            String pkg = ShieldEngine.RISKY_PACKAGES[i][0];
            String desc = ShieldEngine.pkgDesc(i);
            boolean en = ShieldEngine.isPackageEnabled(this, pkg);
            currentEnabled[i] = en;
            String badge = en
                ? ShieldEngine.tr("[UNLOCKED] Click to Freeze ", "[LIBERADO] Clique p/ Congelar ")
                : ShieldEngine.tr("[FROZEN] Click to Undo ", "[CONGELADO] Clique p/ Desfazer ");
            items[i] = badge + "\n" + desc + " (" + pkg + ")";
        }

        new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle(ShieldEngine.tr("4. Manage Single Package (Freeze or Unlock)", "4. Gerenciar Pacote Unitario (Congelar ou Liberar)"))
            .setItems(items, (dialog, which) -> {
                String pkg = ShieldEngine.RISKY_PACKAGES[which][0];
                String desc = ShieldEngine.pkgDesc(which);
                boolean newState = !currentEnabled[which];
                runTask(cb -> ShieldEngine.setSinglePackageEnabled(this, pkg, desc, newState, cb));
            })
            .setNegativeButton(ShieldEngine.tr("Close", "Fechar"), null)
            .show();
    }

    private void showUndoAllWarningDialog() {
        if (busy) return;
        new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle(ShieldEngine.tr("[WARNING] Revert All Protections", "[AVISO] Risco ao Desfazer Toda a Vacina"))
            .setMessage(ShieldEngine.tr(
                "Are you sure you want to revert all protections?\n\n" +
                "WHAT WILL HAPPEN IF YOU CONTINUE:\n" +
                "- The Local Anti-Proxy Shield (VPN) will be turned off;\n" +
                "- Residential proxy daemons (/system/bin/pandoraspear and /system/bin/pcdn) will resume background operation;\n" +
                "- All silent background APK installers will become active again.\n\n" +
                "Do you really want to accept the risk and revert everything?",
                "Tem certeza que deseja desfazer todo o processo de vacinacao?\n\n" +
                "O QUE ACONTECERA SE VOCE CONTINUAR:\n" +
                "- O Filtro Local Anti-Proxy (VPN) sera desligado;\n" +
                "- Os processos de proxy residencial (/system/bin/pandoraspear e /system/bin/pcdn) voltarao a rodar em segundo plano;\n" +
                "- Os instaladores silenciosos voltarao a ficar ativos.\n\n" +
                "Deseja assumir o risco e reverter tudo?"
            ))
            .setPositiveButton(ShieldEngine.tr("Yes, Revert All (Accept Risk)", "Sim, Desfazer Tudo (Assumir Risco)"), (d, w) -> {
                runTask(cb -> ShieldEngine.undoAllProtections(this, cb));
            })
            .setNegativeButton(ShieldEngine.tr("Cancel (Keep Protected)", "Cancelar (Manter Protegido)"), null)
            .show();
    }

    private void triggerProtectFlow() {
        if (busy) return;
        try {
            Intent vpn = VpnService.prepare(this);
            if (vpn != null) {
                liveHeadline.setText(ShieldEngine.tr(
                    "Confirm on screen: Local Shield Permission (VPN)",
                    "Confirme na tela: Permissao de Filtro Local (VPN)"
                ));
                liveSubline.setText(ShieldEngine.tr(
                    "Android will show a VPN connection prompt. Select OK to activate the local sinkhole filter.",
                    "O Android mostrara um aviso de conexao VPN. Selecione OK para ativar o filtro sinkhole local."
                ));
                startActivityForResult(vpn, REQ_VPN);
                return;
            }
        } catch (Throwable ignored) {}
        runTask(cb -> ShieldEngine.applyProtectionAndTest(this, cb));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_VPN) {
            runTask(cb -> ShieldEngine.applyProtectionAndTest(this, cb));
        }
    }

    private interface TaskRunner {
        String run(ShieldEngine.ProgressCallback cb) throws Exception;
    }

    private void runTask(TaskRunner runner) {
        if (busy) return;
        busy = true;
        new Thread(() -> {
            try {
                final String finalOutput = runner.run(state -> runOnUiThread(() -> applyState(state)));
                runOnUiThread(() -> {
                    outputView.setText(finalOutput);
                    scrollView.scrollTo(0, 0);
                    busy = false;
                });
            } catch (Throwable t) {
                runOnUiThread(() -> {
                    liveHeadline.setText(ShieldEngine.tr("Error during operation", "Erro durante a operacao"));
                    liveSubline.setText(String.valueOf(t.getMessage()));
                    outputView.setText("Error details:\n" + t);
                    busy = false;
                });
            }
        }).start();
    }

    private void applyState(ShieldEngine.StepState s) {
        progressBar.setProgress(s.percent);
        livePercent.setText(s.percent + "%");
        liveHeadline.setText(s.headline);
        liveSubline.setText(s.subline);

        styleStepChip(step1Badge, s.activePhase == 1, s.activePhase > 1);
        styleStepChip(step2Badge, s.activePhase == 2, s.activePhase > 2);
        styleStepChip(step3Badge, s.activePhase == 3, s.percent == 100 && s.activePhase == 3);

        boolean daemonsOk = s.stoppedDaemons == s.totalDaemons;
        metricDaemons.setText(ShieldEngine.tr(
            "Native Daemons: " + s.stoppedDaemons + "/" + s.totalDaemons + " stopped",
            "Processos Nativos: " + s.stoppedDaemons + "/" + s.totalDaemons + " parados"
        ));
        metricDaemons.setTextColor(Color.parseColor(daemonsOk ? C_EMERALD : C_AMBER));

        boolean pkgsOk = s.frozenCount == s.totalPackages;
        metricPackages.setText(ShieldEngine.tr(
            "Hidden Apps: " + s.frozenCount + "/" + s.totalPackages + " frozen",
            "Apps Ocultos: " + s.frozenCount + "/" + s.totalPackages + " congelados"
        ));
        metricPackages.setTextColor(Color.parseColor(pkgsOk ? C_EMERALD : C_AMBER));

        boolean testsOk = s.blockedTests == s.totalTests && s.totalTests > 0;
        metricTests.setText(ShieldEngine.tr(
            "Block Test: " + s.blockedTests + "/" + s.totalTests + " passed",
            "Teste de Bloqueio: " + s.blockedTests + "/" + s.totalTests + " aprovados"
        ));
        metricTests.setTextColor(Color.parseColor(testsOk ? C_EMERALD : C_CYAN));

        if (s.logText != null && !s.logText.isEmpty()) {
            outputView.setText(s.logText);
        }
    }

    private TextView makeStepChip(String label) {
        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        tv.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        tv.setPadding(dp(10), dp(4), dp(10), dp(4));
        styleStepChip(tv, false, false);
        return tv;
    }

    private void styleStepChip(TextView tv, boolean active, boolean done) {
        if (active) {
            tv.setTextColor(Color.parseColor(C_TEXT_PRIMARY));
            tv.setBackground(box("#065F46", C_EMERALD, 6, 2));
        } else if (done) {
            tv.setTextColor(Color.parseColor(C_EMERALD));
            tv.setBackground(box(C_CARD, "#047857", 6, 1));
        } else {
            tv.setTextColor(Color.parseColor(C_TEXT_SECONDARY));
            tv.setBackground(box(C_CARD, C_BORDER, 6, 1));
        }
    }

    private TextView makeArrow() {
        TextView tv = new TextView(this);
        tv.setText(" > ");
        tv.setTextColor(Color.parseColor(C_TEXT_SECONDARY));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        return tv;
    }

    private TextView makeMetricPill(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(Color.parseColor(C_TEXT_SECONDARY));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        tv.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        tv.setBackground(box(C_CARD, C_BORDER, 6, 1));
        tv.setPadding(dp(8), dp(5), dp(8), dp(5));
        return tv;
    }

    private Button makeActionButton(String baseFillHex, String accentHex, boolean primary) {
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        b.setTextColor(Color.parseColor(C_TEXT_PRIMARY));
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, primary ? 11.8f : 11f);
        b.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        b.setBackground(box(baseFillHex, primary ? accentHex : C_BORDER, 8, primary ? 2 : 1));
        b.setPadding(dp(14), dp(6), dp(14), dp(6));
        b.setFocusable(true);
        b.setFocusableInTouchMode(true);
        b.setOnFocusChangeListener((View v, boolean hasFocus) -> {
            b.setBackground(box(
                hasFocus ? (primary ? "#047857" : C_CARD_HOVER) : baseFillHex,
                hasFocus ? accentHex : (primary ? accentHex : C_BORDER),
                8,
                hasFocus ? 3 : (primary ? 2 : 1)
            ));
            b.setScaleX(hasFocus ? 1.03f : 1.0f);
            b.setScaleY(hasFocus ? 1.03f : 1.0f);
        });
        return b;
    }

    private Drawable makeProgressDrawable() {
        GradientDrawable bg = box(C_CARD, C_BORDER, 5, 1);
        GradientDrawable fg = new GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            new int[]{Color.parseColor("#059669"), Color.parseColor("#10B981"), Color.parseColor("#34D399")}
        );
        fg.setCornerRadius(dp(5));
        ClipDrawable clip = new ClipDrawable(fg, Gravity.START, ClipDrawable.HORIZONTAL);
        LayerDrawable ld = new LayerDrawable(new Drawable[]{bg, clip});
        ld.setId(0, android.R.id.background);
        ld.setId(1, android.R.id.progress);
        return ld;
    }

    private GradientDrawable box(String fillHex, String strokeHex, int radiusDp, int strokeDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor(fillHex));
        gd.setCornerRadius(dp(radiusDp));
        gd.setStroke(dp(strokeDp), Color.parseColor(strokeHex));
        return gd;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
