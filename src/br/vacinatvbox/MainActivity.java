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

    private static final String C_BG = "#080B10";
    private static final String C_SURFACE = "#101620";
    private static final String C_ELEVATED = "#182130";
    private static final String C_BORDER = "#243146";
    private static final String C_TEXT = "#F3F6FA";
    private static final String C_MUTED = "#94A3B8";
    private static final String C_EMERALD = "#10B981";
    private static final String C_AMBER = "#F59E0B";
    private static final String C_ROSE = "#F43F5E";
    private static final String C_CYAN = "#38BDF8";
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

        // 1. TOP BAR: Logo + Title + 3-Step Pipeline + Language Toggle
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setBackground(box(C_SURFACE, C_BORDER, 10, 1));
        topBar.setPadding(dp(14), dp(8), dp(14), dp(8));

        ImageView logoView = new ImageView(this);
        logoView.setImageResource(R.drawable.ic_launcher);
        LinearLayout.LayoutParams logoLp = new LinearLayout.LayoutParams(dp(40), dp(40));
        logoLp.setMargins(0, 0, dp(12), 0);
        topBar.addView(logoView, logoLp);

        LinearLayout titleCol = new LinearLayout(this);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

        appTitle = new TextView(this);
        appTitle.setTextColor(Color.parseColor(C_TEXT));
        appTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.5f);
        appTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        titleCol.addView(appTitle);

        appSub = new TextView(this);
        appSub.setTextColor(Color.parseColor(C_MUTED));
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
        btnLang.setTextColor(Color.parseColor(C_TEXT));
        btnLang.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        btnLang.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        btnLang.setBackground(box(C_ELEVATED, C_PINK, 6, 1));
        btnLang.setPadding(dp(10), dp(4), dp(10), dp(4));
        btnLang.setFocusable(true);
        btnLang.setFocusableInTouchMode(true);
        btnLang.setOnFocusChangeListener((v, hasFocus) -> {
            btnLang.setBackground(box(hasFocus ? "#4A154B" : C_ELEVATED, C_PINK, 6, hasFocus ? 2 : 1));
        });
        btnLang.setOnClickListener(v -> {
            if (busy) return;
            ShieldEngine.toggleLanguage();
            refreshLocalizedLabels();
            runTask(cb -> ShieldEngine.scanSystem(this, cb));
        });
        LinearLayout.LayoutParams langLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, dp(32));
        langLp.setMargins(dp(10), 0, 0, 0);
        stepsRow.addView(btnLang, langLp);

        topBar.addView(stepsRow);
        root.addView(topBar);

        // 2. HERO TELEMETRY: Progress Bar + Live Status + 3 Score Pills
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setBackground(box(C_SURFACE, C_BORDER, 10, 1));
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
        liveHeadline.setTextColor(Color.parseColor(C_TEXT));
        liveHeadline.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        liveHeadline.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        statusTextCol.addView(liveHeadline);

        liveSubline = new TextView(this);
        liveSubline.setTextColor(Color.parseColor(C_MUTED));
        liveSubline.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        liveSubline.setPadding(0, dp(2), 0, 0);
        statusTextCol.addView(liveSubline);
        statusHeader.addView(statusTextCol, stLp);

        livePercent = new TextView(this);
        livePercent.setText("0%");
        livePercent.setTextColor(Color.parseColor(C_EMERALD));
        livePercent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
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

        // 3. BODY: Action Column (Left) + Forensic Console (Right)
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams bodyLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);

        LinearLayout leftCol = new LinearLayout(this);
        leftCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams leftLp = new LinearLayout.LayoutParams(
            dp(310), LinearLayout.LayoutParams.MATCH_PARENT);
        leftLp.setMargins(0, 0, dp(12), 0);

        btnMainFlow = makeActionButton("#0D3B2E", C_EMERALD, true);
        btnScanOnly = makeActionButton(C_ELEVATED, C_CYAN, false);
        btnTestOnly = makeActionButton(C_ELEVATED, C_CYAN, false);
        btnUnitManage = makeActionButton(C_ELEVATED, C_AMBER, false);
        btnUndoAll = makeActionButton("#2D1219", C_ROSE, false);
        btnDonateUpdates = makeActionButton("#2A1228", C_PINK, false);

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
        scrollView.setBackground(box(C_SURFACE, C_BORDER, 10, 2));
        scrollView.setPadding(dp(16), dp(12), dp(16), dp(12));
        scrollView.setOnFocusChangeListener((v, hasFocus) -> {
            scrollView.setBackground(box(C_SURFACE, hasFocus ? C_CYAN : C_BORDER, 10, hasFocus ? 3 : 2));
        });

        outputView = new TextView(this);
        outputView.setTextColor(Color.parseColor(C_TEXT));
        outputView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        outputView.setTypeface(Typeface.MONOSPACE);
        outputView.setLineSpacing(dp(2), 1.05f);
        scrollView.addView(outputView);
        body.addView(scrollView, rightLp);

        root.addView(body, bodyLp);

        // Explicit D-Pad Navigation
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
            "TV Box Vaccine v" + ShieldEngine.APP_VERSION + "  ·  by " + ShieldEngine.AUTHOR_NAME,
            "Vacina TV Box v" + ShieldEngine.APP_VERSION + "  ·  por " + ShieldEngine.AUTHOR_NAME
        ));
        appSub.setText(ShieldEngine.tr(
            "Anti-Residential Proxy Shield, Daemon Killer & Silent Installer Blocker  ·  triessudo.zip",
            "Escudo Anti-Proxy Residencial, Desligamento de Daemons e Bloqueio de Instaladores  ·  triessudo.zip"
        ));
        step1Badge.setText(ShieldEngine.tr("1. Analyze", "1. Analisar"));
        step2Badge.setText(ShieldEngine.tr("2. Vaccinate + Block", "2. Vacinar + Bloquear"));
        step3Badge.setText(ShieldEngine.tr("3. Test Block", "3. Testar Bloqueio"));
        btnLang.setText(ShieldEngine.isPt() ? "🌐 PT-BR (Mudar p/ EN)" : "🌐 EN-US (Switch to PT)");

        btnMainFlow.setText(ShieldEngine.tr(
            "1. Analyze + Vaccinate + Test\nRuns full step-by-step protection and verifies block",
            "1. Analisar + Vacinar + Testar\nExecuta todo o passo a passo e verifica no final"
        ));
        btnScanOnly.setText(ShieldEngine.tr(
            "2. Analyze System Only\nShows current diagnostics without changing anything",
            "2. Apenas Analisar Sistema\nMostra o diagnóstico atual sem alterar nada"
        ));
        btnTestOnly.setText(ShieldEngine.tr(
            "3. Test Blocking Only\nProves native daemons and C2 routes are blocked",
            "3. Apenas Testar Bloqueio\nProva que processos e rotas C2 estão barrados"
        ));
        btnUnitManage.setText(ShieldEngine.tr(
            "4. Undo / Manage Single Item\nChoose which app or store to unlock/freeze 1 by 1",
            "4. Desfazer / Gerenciar Item Unitário\nEscolha qual app/loja liberar ou congelar 1 a 1"
        ));
        btnUndoAll.setText(ShieldEngine.tr(
            "5. Undo All Protections (Risk)\nReverts vaccine and restores factory behavior",
            "5. Desfazer Todo o Processo (Risco)\nReverte toda a vacina e restaura estado de fábrica"
        ));
        btnDonateUpdates.setText(ShieldEngine.tr(
            "6. 💖 Donate (QR Codes) & Updates\nSupport Suzan (@TriesSudo) · triessudo.zip",
            "6. 💖 Doar (QR Codes) e Atualizações\nApoie Suzan (@TriesSudo) · triessudo.zip"
        ));
    }

    /**
     * Option 6: Displays a visual dialog with the 3 hardcoded crypto QR codes (BTC, ETH, SOL),
     * wallet addresses, and Suzan's official website (triessudo.zip) for more coins.
     */
    private void showDonateQrModal() {
        LinearLayout modal = new LinearLayout(this);
        modal.setOrientation(LinearLayout.VERTICAL);
        modal.setBackgroundColor(Color.parseColor("#0D111A"));
        modal.setPadding(dp(18), dp(14), dp(18), dp(10));

        TextView header = new TextView(this);
        header.setText(ShieldEngine.tr(
            "💖 Support Suzan (@TriesSudo) — Help keep this project free & fighting malware!",
            "💖 Apoie Suzan (@TriesSudo) — Ajude a manter este projeto gratuito contra malwares!"
        ));
        header.setTextColor(Color.parseColor("#F472B6"));
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        header.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        modal.addView(header);

        TextView sub = new TextView(this);
        sub.setText(ShieldEngine.tr(
            "If this tool protected your TV Box and home network, please consider donating any amount by scanning a QR Code below.\n" +
            "🌐 More coins, updates & projects at: https://triessudo.zip   |   ✉️ Contact: triessudo@atomicmail.io   |   Telegram: t.me/triessudo",
            "Se esta ferramenta protegeu sua TV Box e sua rede, considere doar qualquer valor escaneando um dos QR Codes abaixo.\n" +
            "🌐 Mais moedas, atualizações e projetos em: https://triessudo.zip   |   ✉️ Contato: triessudo@atomicmail.io   |   Telegram: t.me/triessudo"
        ));
        sub.setTextColor(Color.parseColor(C_TEXT));
        sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        sub.setPadding(0, dp(4), 0, dp(10));
        modal.addView(sub);

        LinearLayout cardsRow = new LinearLayout(this);
        cardsRow.setOrientation(LinearLayout.HORIZONTAL);
        cardsRow.setGravity(Gravity.CENTER);

        cardsRow.addView(makeQrCard("Bitcoin (BTC)", ShieldEngine.WALLET_BTC, R.drawable.donate_btc, "#F59E0B"),
            new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        cardsRow.addView(makeQrCard("Ethereum (ETH)", ShieldEngine.WALLET_ETH, R.drawable.donate_eth, "#818CF8"),
            new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        cardsRow.addView(makeQrCard("Solana (SOL)", ShieldEngine.WALLET_SOL, R.drawable.donate_sol, "#34D399"),
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
        card.setBackground(box(C_SURFACE, accentHex, 8, 2));
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
        addr.setTextColor(Color.parseColor(C_MUTED));
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
                ? ShieldEngine.tr("🔓 [UNLOCKED - Click to Freeze] ", "🔓 [LIBERADO - Clique p/ Congelar] ")
                : ShieldEngine.tr("🔒 [FROZEN - Click to Undo] ", "🔒 [CONGELADO - Clique p/ Desfazer] ");
            items[i] = badge + "\n" + desc + " (" + pkg + ")";
        }

        new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle(ShieldEngine.tr("4. Manage or Undo Single Package Block", "4. Gerenciar ou Desfazer Bloqueio de Forma Unitária"))
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
            .setTitle(ShieldEngine.tr("⚠️ WARNING: Security Risk When Undoing All", "⚠️ ATENÇÃO: Risco de Segurança ao Desfazer Tudo"))
            .setMessage(ShieldEngine.tr(
                "Are you sure you want to undo the entire vaccination process?\n\n" +
                "WHAT WILL HAPPEN IF YOU CONTINUE:\n" +
                "• The Local Anti-Proxy Shield (VPN) will be turned off;\n" +
                "• Residential proxy daemons (/system/bin/pandoraspear and /system/bin/pcdn) will resume using your internet and residential IP;\n" +
                "• All 8 hidden factory installers will become active in the background again.\n\n" +
                "Do you really want to accept the risk and revert everything?",
                "Tem certeza que deseja desfazer todo o processo de vacinação?\n\n" +
                "O QUE ACONTECERÁ SE VOCÊ CONTINUAR:\n" +
                "• O Filtro Local Anti-Proxy (VPN) será desligado;\n" +
                "• Os processos de proxy residencial (/system/bin/pandoraspear e /system/bin/pcdn) voltarão a usar sua internet e seu IP residencial;\n" +
                "• Os 8 instaladores ocultos de fábrica voltarão a ficar ativos em segundo plano.\n\n" +
                "Deseja mesmo assumir o risco e reverter tudo?"
            ))
            .setPositiveButton(ShieldEngine.tr("⚠️ Yes, Undo All (Accept Risk)", "⚠️ Sim, Desfazer Tudo (Assumir Risco)"), (d, w) -> {
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
                    "Confirme na tela: Permissão de Filtro Local (VPN)"
                ));
                liveSubline.setText(ShieldEngine.tr(
                    "Android will show a VPN connection prompt. This is our internal on-device filter: select OK.",
                    "O Android mostrará um aviso de conexão VPN. Trata-se do nosso filtro interno: selecione OK."
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
                    liveHeadline.setText(ShieldEngine.tr("Error during operation", "Erro durante a operação"));
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
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        tv.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        tv.setPadding(dp(8), dp(4), dp(8), dp(4));
        styleStepChip(tv, false, false);
        return tv;
    }

    private void styleStepChip(TextView tv, boolean active, boolean done) {
        if (active) {
            tv.setTextColor(Color.parseColor(C_TEXT));
            tv.setBackground(box("#0D3B2E", C_EMERALD, 6, 2));
        } else if (done) {
            tv.setTextColor(Color.parseColor(C_EMERALD));
            tv.setBackground(box(C_ELEVATED, "#065F46", 6, 1));
        } else {
            tv.setTextColor(Color.parseColor(C_MUTED));
            tv.setBackground(box(C_ELEVATED, C_BORDER, 6, 1));
        }
    }

    private TextView makeArrow() {
        TextView tv = new TextView(this);
        tv.setText(" › ");
        tv.setTextColor(Color.parseColor(C_MUTED));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        return tv;
    }

    private TextView makeMetricPill(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(Color.parseColor(C_MUTED));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        tv.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        tv.setBackground(box(C_ELEVATED, C_BORDER, 6, 1));
        tv.setPadding(dp(8), dp(5), dp(8), dp(5));
        return tv;
    }

    private Button makeActionButton(String baseFillHex, String accentHex, boolean primary) {
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        b.setTextColor(Color.parseColor(C_TEXT));
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, primary ? 11.5f : 10.8f);
        b.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        b.setBackground(box(baseFillHex, primary ? accentHex : C_BORDER, 8, primary ? 2 : 1));
        b.setPadding(dp(12), dp(5), dp(12), dp(5));
        b.setFocusable(true);
        b.setFocusableInTouchMode(true);
        b.setOnFocusChangeListener((View v, boolean hasFocus) -> {
            b.setBackground(box(
                hasFocus ? (primary ? "#115E46" : "#1E293B") : baseFillHex,
                hasFocus ? accentHex : (primary ? accentHex : C_BORDER),
                8,
                hasFocus ? 3 : (primary ? 2 : 1)
            ));
            b.setScaleX(hasFocus ? 1.02f : 1.0f);
            b.setScaleY(hasFocus ? 1.02f : 1.0f);
        });
        return b;
    }

    private Drawable makeProgressDrawable() {
        GradientDrawable bg = box(C_ELEVATED, C_BORDER, 5, 1);
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
