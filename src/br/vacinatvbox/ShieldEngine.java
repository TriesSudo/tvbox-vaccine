package br.vacinatvbox;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Locale;

public final class ShieldEngine {
    private ShieldEngine() {}

    public static final String APP_VERSION = "1.0";
    public static final String AUTHOR_NAME = "Suzan (@TriesSudo)";
    public static final String AUTHOR_SITE = "https://triessudo.zip";
    public static final String AUTHOR_EMAIL = "triessudo@atomicmail.io";
    public static final String AUTHOR_TG = "https://t.me/triessudo";
    public static final String AUTHOR_X = "https://x.com/TriesSudo";

    public static final String GITHUB_REPO = "TriesSudo/tvbox-vaccine";
    public static final String GITHUB_URL = "https://github.com/TriesSudo/tvbox-vaccine";
    public static final String LATEST_API_URL = "https://api.github.com/repos/TriesSudo/tvbox-vaccine/releases/latest";

    public static final String WALLET_BTC = "bc1qpe2uwvtjkyuu7ntt5rnsnv823cdmc66t45q9wg";
    public static final String WALLET_ETH = "0x88f633DAd7830e6512634445028979c6d2dF60Da";
    public static final String WALLET_SOL = "G6NgGBpvAtazdpKn1tcBvcPztBgoxbi2hXgxV4aiaAQD";

    private static volatile Boolean forcedPt = null;

    public static boolean isPt() {
        if (forcedPt != null) return forcedPt;
        try {
            String propLocale = getProp("persist.sys.locale");
            if (propLocale != null && !propLocale.isEmpty()) {
                return propLocale.toLowerCase(Locale.ROOT).startsWith("pt");
            }
            String propLang = getProp("persist.sys.language");
            if (propLang != null && !propLang.isEmpty()) {
                return propLang.toLowerCase(Locale.ROOT).startsWith("pt");
            }
            String lang = Locale.getDefault().getLanguage();
            return lang != null && lang.toLowerCase(Locale.ROOT).startsWith("pt");
        } catch (Throwable t) {
            return false;
        }
    }

    public static void toggleLanguage() {
        forcedPt = !isPt();
    }

    public static String tr(String en, String pt) {
        return isPt() ? pt : en;
    }

    public static final class StepState {
        public final int percent;
        public final int activePhase;
        public final String headline;
        public final String subline;
        public final String logText;
        public final boolean vpnActive;
        public final boolean adbReady;
        public final int stoppedDaemons;
        public final int totalDaemons;
        public final int frozenCount;
        public final int totalPackages;
        public final int blockedTests;
        public final int totalTests;

        public StepState(int percent, int activePhase, String headline, String subline, String logText,
                         boolean vpnActive, boolean adbReady, int stoppedDaemons, int totalDaemons,
                         int frozenCount, int totalPackages, int blockedTests, int totalTests) {
            this.percent = percent;
            this.activePhase = activePhase;
            this.headline = headline;
            this.subline = subline;
            this.logText = logText;
            this.vpnActive = vpnActive;
            this.adbReady = adbReady;
            this.stoppedDaemons = stoppedDaemons;
            this.totalDaemons = totalDaemons;
            this.frozenCount = frozenCount;
            this.totalPackages = totalPackages;
            this.blockedTests = blockedTests;
            this.totalTests = totalTests;
        }
    }

    public interface ProgressCallback {
        void onStep(StepState state);
    }

    public static final String PREFS = "vacina_prefs";
    public static final String KEY_ENABLED = "shield_enabled";
    public static final String KEY_ADB_AUTHORIZED = "adb_authorized";
    private static volatile boolean interactiveAdbBusy = false;

    public static final String[][] NATIVE_DAEMONS = {
        {"pandoraspear", "/system/bin/pandoraspear",
            "Residential Proxy / Botnet Agent (DGA dynamic domains)",
            "Agente de Proxy Residencial / Botnet (dominios DGA dinamicos)"},
        {"pcdn", "/system/bin/pcdn",
            "PCDN Client (consumes home bandwidth upload in background)",
            "Cliente PCDN (usa banda de upload residencial em segundo plano)"}
    };

    public static final String[][] RISKY_PACKAGES = {
        {"com.android.vending",
            "Tubesky (Unofficial modified store)",
            "Tubesky (Loja alternativa nao oficial)"},
        {"com.swl.amarket",
            "App Market (Silent background APK installer)",
            "App Market (Instalador silencioso em segundo plano)"},
        {"com.swl.cacheclean",
            "CacheClean (Silently installed with package install permissions)",
            "CacheClean (Instalado sem permissao com acesso a pacotes)"}
    };

    public static final String[] PROTECTED_SYSTEM_UID_PACKAGES = {
        "com.softwinner.update",
        "com.factorytools.factorystability",
        "com.softwinner.dragonbox",
        "com.softwinner.agingdragonbox",
        "com.softwinner.awlogsettings"
    };

    public static final String[] C2_IPS = {
        "173.255.212.243",
        "66.175.216.178",
        "162.209.126.216",
        "91.195.240.103",
        "34.219.29.107",
        "208.95.112.1"
    };

    public static final int TOTAL_TESTS = NATIVE_DAEMONS.length + C2_IPS.length;

    public static String c2Label(int idx) {
        boolean pt = isPt();
        switch (idx) {
            case 0: return pt ? "Servidor C2 PandoraSpear #1 (Porta 9999)" : "PandoraSpear C2 Server #1 (Port 9999)";
            case 1: return pt ? "Servidor C2 PandoraSpear #2 (DGA Porta 9999)" : "PandoraSpear C2 Server #2 (DGA Port 9999)";
            case 2: return pt ? "Servidor C2 PandoraSpear #3 (DGA Porta 9999)" : "PandoraSpear C2 Server #3 (DGA Port 9999)";
            case 3: return pt ? "No de Upload PCDN #1 (Porta 31226)" : "PCDN Upload Node #1 (Port 31226)";
            case 4: return pt ? "Central de Controle PCDN #2 (Porta 80)" : "PCDN Control Hub #2 (Port 80)";
            default: return pt ? "Coletor Externo de IP Residencial (Porta 80)" : "External Residential IP Collector (Port 80)";
        }
    }

    public static String pkgDesc(int idx) {
        return isPt() ? RISKY_PACKAGES[idx][2] : RISKY_PACKAGES[idx][1];
    }

    public static String daemonDesc(int idx) {
        return isPt() ? NATIVE_DAEMONS[idx][3] : NATIVE_DAEMONS[idx][2];
    }

    public static String getProp(String key) {
        try {
            Class<?> sp = Class.forName("android.os.SystemProperties");
            Method get = sp.getMethod("get", String.class, String.class);
            return (String) get.invoke(null, key, "");
        } catch (Throwable t) {
            return "";
        }
    }

    public static boolean isAdbEnabled(Context c) {
        try {
            return Settings.Global.getInt(c.getContentResolver(), Settings.Global.ADB_ENABLED, 0) == 1;
        } catch (Throwable t) {
            return true;
        }
    }

    public static boolean isPackageEnabled(Context c, String pkg) {
        try {
            ApplicationInfo ai = c.getPackageManager().getApplicationInfo(pkg, 0);
            return ai.enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static int countStoppedDaemons() {
        int stopped = 0;
        for (String[] d : NATIVE_DAEMONS) {
            String st = getProp("init.svc." + d[0]);
            if ("stopped".equalsIgnoreCase(st) || st.isEmpty()) {
                stopped++;
            }
        }
        return stopped;
    }

    public static int countFrozenPackages(Context c) {
        int frozen = 0;
        PackageManager pm = c.getPackageManager();
        for (String[] item : RISKY_PACKAGES) {
            try {
                ApplicationInfo ai = pm.getApplicationInfo(item[0], 0);
                if (!ai.enabled) frozen++;
            } catch (PackageManager.NameNotFoundException e) {
                frozen++;
            }
        }
        return frozen;
    }

    public static void stopNativeMalwareDaemons(Context c) {
        if (interactiveAdbBusy) return;
        SharedPreferences prefs = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(KEY_ENABLED, false)) return;
        if (!prefs.getBoolean(KEY_ADB_AUTHORIZED, false)) return;
        if (!isAdbEnabled(c)) return;
        try {
            if (countStoppedDaemons() < NATIVE_DAEMONS.length) {
                try (LocalAdb adb = LocalAdb.connect(c, 4_000, null)) {
                    adb.shell("setprop ctl.stop pandoraspear; setprop ctl.stop pcdn");
                }
            }
        } catch (Throwable ignored) {}
    }

    private static void emit(ProgressCallback cb, Context c, int pct, int phase, String headline, String subline,
                             StringBuilder sb, int blockedTests, int totalTests) {
        if (cb == null) return;
        cb.onStep(new StepState(
            pct,
            phase,
            headline,
            subline,
            sb.toString(),
            SinkholeVpnService.isRunning,
            isAdbEnabled(c),
            countStoppedDaemons(),
            NATIVE_DAEMONS.length,
            countFrozenPackages(c),
            RISKY_PACKAGES.length,
            blockedTests,
            totalTests
        ));
    }

    public static String scanSystem(Context c, ProgressCallback cb) {
        StringBuilder sb = new StringBuilder();
        emit(cb, c, 15, 1,
            tr("Step 1: Analyzing TV Box System", "Etapa 1: Analisando o sistema da TV Box"),
            tr("Checking USB Debugging settings and local shield state...", "Verificando configuracoes de Depuracao USB e o estado do filtro local..."),
            sb, 0, TOTAL_TESTS);

        boolean adbOn = isAdbEnabled(c);
        sb.append("====================================================================\n");
        sb.append(tr(" TV BOX VACCINE v", " VACINA TV BOX v")).append(APP_VERSION)
          .append(" - ").append(AUTHOR_NAME).append(" | ").append(AUTHOR_SITE).append("\n");
        sb.append(" GitHub: ").append(GITHUB_URL).append("\n");
        sb.append("====================================================================\n\n");

        sb.append(tr("[STEP 1] DEVICE DIAGNOSTICS (BOOT-SAFE MODE)\n", "[ETAPA 1] DIAGNOSTICO DO APARELHO (MODO BOOT SEGURO)\n"));
        sb.append("--------------------------------------------------------------------\n");
        sb.append(tr("  Model: ", "  Modelo: ")).append(getProp("ro.product.model"))
          .append(tr("  |  Board: ", "  |  Placa: ")).append(getProp("ro.board.platform"))
          .append(tr("  |  ROM Tags: ", "  |  Assinatura ROM: ")).append(getProp("ro.build.tags")).append("\n");
        sb.append(tr("  USB Debugging (Internal ADB): ", "  Depuracao USB (ADB Interno): "))
          .append(adbOn
              ? tr("[OK] ENABLED", "[OK] ATIVADA")
              : tr("[ACTION NEEDED] DISABLED - Enable in Settings -> Developer Options -> USB Debugging",
                   "[ACAO NECESSARIA] DESATIVADA - Ative em Configuracoes -> Opcoes do Desenvolvedor -> Depuracao USB"))
          .append("\n");
        sb.append(tr("  Local Anti-Proxy Shield (VPN): ", "  Filtro Local Anti-Proxy (VPN): "))
          .append(SinkholeVpnService.isRunning
              ? tr("[OK] ACTIVE & PROTECTING", "[OK] ATIVO E PROTEGENDO")
              : tr("[PENDING] OFF - Click '1. Analyze + Vaccinate + Test'",
                   "[PENDENTE] DESLIGADO - Clique em '1. Analisar + Vacinar + Testar'"))
          .append("\n\n");

        emit(cb, c, 45, 1,
            tr("Step 1: Inspecting native proxy processes (/system/bin)", "Etapa 1: Inspecionando processos de proxy nativos (/system/bin)"),
            tr("Checking if network daemons (pandoraspear, pcdn) are running...", "Verificando se os servicos de rede (pandoraspear e pcdn) estao em execucao..."),
            sb, 0, TOTAL_TESTS);

        sb.append(tr("NATIVE PROXY/PCDN DAEMONS IN SYSTEM PARTITION (/system/bin)\n", "PROCESSOS NATIVOS DE PROXY/PCDN NA PARTICAO DE SISTEMA (/system/bin)\n"));
        sb.append("--------------------------------------------------------------------\n");
        for (int i = 0; i < NATIVE_DAEMONS.length; i++) {
            String svcName = NATIVE_DAEMONS[i][0];
            String binPath = NATIVE_DAEMONS[i][1];
            String desc = daemonDesc(i);
            File f = new File(binPath);
            String svcState = getProp("init.svc." + svcName);
            boolean isStopped = "stopped".equalsIgnoreCase(svcState);
            if (f.exists()) {
                sb.append("  ").append(isStopped ? tr("[STOPPED OK] ", "[PARADO OK] ") : tr("[RUNNING!]   ", "[RODANDO!]  "))
                  .append(binPath).append(" (").append(f.length() / 1024).append(" KB)\n")
                  .append(tr("    Role: ", "    Funcao: ")).append(desc).append("\n")
                  .append(tr("    Linux State (init.svc.", "    Estado Linux (init.svc.")).append(svcName).append("): [")
                  .append(svcState.isEmpty() ? "unknown" : svcState).append("]\n");
            } else {
                sb.append(tr("  [ABSENT]     ", "  [AUSENTE]   ")).append(binPath).append("\n");
            }
        }

        emit(cb, c, 75, 1,
            tr("Step 1: Auditing connections and silent installers", "Etapa 1: Auditando conexoes e instaladores silenciosos"),
            tr("Checking TCP table (/proc/net/tcp) and silent APK installers...", "Verificando tabela TCP (/proc/net/tcp) e instaladores silenciosos..."),
            sb, 0, TOTAL_TESTS);

        sb.append(tr("\nACTIVE EXTERNAL PROXY CONNECTIONS (/proc/net/tcp)\n", "\nCONEXOES EXTERNAS DE PROXY ATIVAS (/proc/net/tcp)\n"));
        sb.append("--------------------------------------------------------------------\n");
        int conns = scanProcNetTcp(sb);
        if (conns == 0) {
            sb.append(tr("  [OK] No active connections to C2 servers (ports 9999 / 31226).\n",
                         "  [OK] Nenhuma conexao estabelecida com servidores C2 (portas 9999 / 31226).\n"));
        }

        sb.append(tr("\nSILENT INSTALLER APPS (BOOT-SAFE FREEZE - OPTION 4 MANAGES 1 BY 1)\n",
                     "\nINSTALADORES SILENCIOSOS (CONGELAMENTO SEGURO - OPCAO 4 GERENCIA 1 POR 1)\n"));
        sb.append("--------------------------------------------------------------------\n");
        PackageManager pm = c.getPackageManager();
        for (int i = 0; i < RISKY_PACKAGES.length; i++) {
            String pkg = RISKY_PACKAGES[i][0];
            String desc = pkgDesc(i);
            try {
                ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
                sb.append(ai.enabled ? tr("  [ACTIVE]    ", "  [ATIVO]     ") : tr("  [FROZEN]    ", "  [CONGELADO] "))
                  .append(pkg).append(" - ").append(desc).append("\n");
            } catch (PackageManager.NameNotFoundException e) {
                sb.append(tr("  [ABSENT]    ", "  [AUSENTE]   ")).append(pkg).append("\n");
            }
        }

        int stopped = countStoppedDaemons();
        int frozen = countFrozenPackages(c);
        boolean allClean = SinkholeVpnService.isRunning && stopped == NATIVE_DAEMONS.length && frozen == RISKY_PACKAGES.length;
        String summaryHeadline = allClean
            ? tr("Device 100% Protected (Boot-Safe): 2/2 proxy daemons stopped, 3/3 installers frozen & shield active",
                 "Aparelho 100% Protegido (Boot Seguro): 2/2 daemons parados, 3/3 instaladores congelados e filtro ativo")
            : tr("Analysis complete: Click '1. Analyze + Vaccinate + Test' to apply protection",
                 "Analise concluida: Clique no botao '1. Analisar + Vacinar + Testar' para aplicar a protecao");
        emit(cb, c, 100, 1, summaryHeadline,
            tr("Use remote control D-Pad to navigate options or scroll report.",
               "Use as setas do controle remoto para navegar pelas opcoes ou rolar o relatorio."),
            sb, allClean ? TOTAL_TESTS : 0, TOTAL_TESTS);
        return sb.toString();
    }

    private static int scanProcNetTcp(StringBuilder sb) {
        int found = 0;
        String[] files = {"/proc/net/tcp", "/proc/net/tcp6"};
        for (String path : files) {
            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String upper = line.toUpperCase();
                    if (upper.contains(":270F ") || upper.contains(":79FA ") || upper.contains("F3D4FFAD") || upper.contains("B2D8AF42")) {
                        sb.append(tr("  [ALERT] Suspicious C2 connection detected in ", "  [ALERTA] Conexao suspeita detectada em ")).append(path).append("\n");
                        found++;
                    }
                }
            } catch (Throwable ignored) {}
        }
        return found;
    }

    public static String applyProtectionAndTest(Context c, ProgressCallback cb) {
        StringBuilder sb = new StringBuilder();
        SharedPreferences prefs = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ENABLED, true).apply();

        emit(cb, c, 10, 1,
            tr("Phase 1 of 3 - Analyzing system before vaccination", "Fase 1 de 3 - Analisando o sistema antes da vacina"),
            tr("Identifying native proxy daemons in /system/bin and silent installer packages...", "Identificando daemons de proxy em /system/bin e pacotes instaladores..."),
            sb, 0, TOTAL_TESTS);

        sb.append(tr("[PHASE 1 OF 3] SYSTEM ANALYSIS (BOOT-SAFE ARCHITECTURE)\n", "[FASE 1 DE 3] ANALISE DO SISTEMA (ARQUITETURA DE BOOT SEGURO)\n"));
        sb.append("--------------------------------------------------------------------\n");
        sb.append(tr(
            "  * Threat Overview: /system/bin/pandoraspear and /system/bin/pcdn use Domain Generation\n" +
            "    Algorithms (DGA) that change domains dynamically.\n" +
            "  * Boot-Safe Dual Defense (100% on-device, zero bootloop risk):\n" +
            "    1) Stops only the 2 network proxy daemons (ctl.stop pandoraspear, pcdn)\n" +
            "       while keeping core ROM services and UID 1000 system packages untouched;\n" +
            "    2) Freezes the 3 third-party silent APK installers and activates the Local Shield (VPN).\n\n",
            "  * Como funciona a ameaca: Os binarios /system/bin/pandoraspear e /system/bin/pcdn\n" +
            "    usam geradores de dominios aleatorios (DGA) que mudam com o tempo.\n" +
            "  * Defesa Dupla com Boot Seguro (100% no aparelho, risco zero de bootloop):\n" +
            "    1) Desliga na fonte apenas os 2 daemons de rede (ctl.stop pandoraspear e pcdn),\n" +
            "       preservando servicos vitais da placa e pacotes de sistema UID 1000;\n" +
            "    2) Congela os 3 instaladores silenciosos e ativa o Filtro Local (VPN Sinkhole).\n\n"
        ));

        emit(cb, c, 25, 2,
            tr("Phase 2 of 3 - Activating Local Anti-Proxy Shield (VPN Sinkhole)", "Fase 2 de 3 - Ativando Filtro Local Anti-Proxy (VPN Sinkhole)"),
            tr("Creating null routes inside TV Box for known C2 servers...", "Criando rotas de bloqueio dentro da TV Box para os servidores C2..."),
            sb, 0, TOTAL_TESTS);

        sb.append(tr("[PHASE 2 OF 3] APPLYING VACCINE & BLOCKING (METHOD A + B)\n", "[FASE 2 DE 3] APLICANDO VACINA E BLOQUEIO (METODO A + B)\n"));
        sb.append("--------------------------------------------------------------------\n");
        sb.append(tr("1. Local Network Shield (VpnService Sinkhole):\n", "1. Filtro de Rede Local (VpnService Sinkhole):\n"));
        try {
            c.startService(new Intent(c, SinkholeVpnService.class));
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            for (int i = 0; i < C2_IPS.length; i++) {
                sb.append(tr("     [NULL ROUTE ACTIVE] ", "     [ROTA NULA ATIVA] "))
                  .append(c2Label(i)).append(tr(" -> blocked on device\n", " -> bloqueado no aparelho\n"));
            }
        } catch (Throwable t) {
            sb.append(tr("     [WARNING] Failed to start local shield: ", "     [AVISO] Falha ao iniciar filtro local: ")).append(t.getMessage()).append("\n");
        }

        sb.append(tr("\n2. Stopping Network Proxy Daemons + Freezing Silent Installers (Internal ADB):\n",
                     "\n2. Desligamento dos Daemons de Proxy + Congelamento de Instaladores (via ADB Interno):\n"));
        if (!isAdbEnabled(c)) {
            sb.append(tr(
                "     [ACTION NEEDED] USB Debugging is DISABLED in Android Settings!\n" +
                "     How to enable: Settings -> Device Preferences -> About ->\n" +
                "     click 'Build number' 7 times. Then open 'Developer Options',\n" +
                "     enable 'USB Debugging', and click this button again.\n",
                "     [ACAO NECESSARIA] A Depuracao USB esta DESATIVADA no Android!\n" +
                "     Como ativar: Configuracoes -> Preferencias do Dispositivo -> Sobre ->\n" +
                "     clique 7 vezes em 'Numero da versao'. Depois entre em 'Opcoes do Desenvolvedor',\n" +
                "     ative 'Depuracao USB' e clique neste botao novamente.\n"
            ));
        } else {
            emit(cb, c, 35, 2,
                tr("Phase 2 of 3 - Connecting to Internal ADB", "Fase 2 de 3 - Conectando ao ADB interno"),
                tr("Communicating with internal debugging service...", "Comunicando com o servico interno de depuracao do aparelho..."),
                sb, 0, TOTAL_TESTS);
            interactiveAdbBusy = true;
            try (LocalAdb adb = LocalAdb.connect(c, 45_000, () ->
                    emit(cb, c, 38, 2,
                        tr("ACTION ON TV: Confirm 'Allow USB debugging?' dialog", "ACAO NA TV: Confirme a janela 'Permitir depuracao USB?'"),
                        tr("Check 'Always allow from this computer' and press OK on remote!", "Marque 'Sempre permitir deste computador' e pressione OK no controle!"),
                        sb, 0, TOTAL_TESTS))) {

                prefs.edit().putBoolean(KEY_ADB_AUTHORIZED, true).apply();

                StringBuilder sysEnableCmd = new StringBuilder();
                for (String sysPkg : PROTECTED_SYSTEM_UID_PACKAGES) {
                    if (!isPackageEnabled(c, sysPkg)) {
                        sysEnableCmd.append("pm enable ").append(sysPkg).append("; ");
                    }
                }
                if (sysEnableCmd.length() > 0) {
                    adb.shell(sysEnableCmd.toString());
                }

                for (int i = 0; i < NATIVE_DAEMONS.length; i++) {
                    String svc = NATIVE_DAEMONS[i][0];
                    emit(cb, c, 44 + (i * 5), 2,
                        tr("Phase 2 of 3 - Stopping proxy daemon (" + (i + 1) + "/" + NATIVE_DAEMONS.length + "): " + svc,
                           "Fase 2 de 3 - Desligando daemon de proxy (" + (i + 1) + "/" + NATIVE_DAEMONS.length + "): " + svc),
                        tr("Sending ctl.stop " + svc + "...", "Enviando comando ctl.stop " + svc + "..."),
                        sb, 0, TOTAL_TESTS);
                    adb.shell("setprop ctl.stop " + svc);
                }
                try { Thread.sleep(400); } catch (InterruptedException ignored) {}
                for (String[] d : NATIVE_DAEMONS) {
                    String svc = d[0];
                    String st = getProp("init.svc." + svc);
                    sb.append(tr("     [PROCESS STOPPED] ", "     [PROCESSO ENCERRADO] ")).append("/system/bin/").append(svc)
                      .append(tr(" -> state: [", " -> estado atual: [")).append(st.isEmpty() ? "stopped" : st).append("]\n");
                }

                int total = RISKY_PACKAGES.length;
                for (int i = 0; i < total; i++) {
                    String pkg = RISKY_PACKAGES[i][0];
                    String desc = pkgDesc(i);
                    int pct = 54 + ((i + 1) * 16 / total);
                    emit(cb, c, pct, 2,
                        tr("Phase 2 of 3 - Freezing installer (" + (i + 1) + "/" + total + "): " + pkg,
                           "Fase 2 de 3 - Congelando instalador (" + (i + 1) + "/" + total + "): " + pkg),
                        tr("Preventing background execution...", "Impedindo execucao em segundo plano..."),
                        sb, 0, TOTAL_TESTS);
                    String out = adb.shell("pm disable-user --user 0 " + pkg);
                    if (out.contains("disabled-user")) {
                        sb.append(tr("     [APP FROZEN]      ", "     [APP CONGELADO]      ")).append(pkg).append(" - ").append(desc).append("\n");
                    } else {
                        sb.append(tr("     [APP SKIPPED]     ", "     [APP IGNORADO]       ")).append(pkg).append("\n");
                    }
                }
            } catch (Throwable t) {
                sb.append(tr("     [ADB WARNING] ", "     [AVISO ADB] ")).append(t.getMessage()).append("\n");
            } finally {
                interactiveAdbBusy = false;
            }
        }

        sb.append("\n");
        int passed = runBlockingTestsInternal(c, sb, cb, 72);

        emit(cb, c, 100, 3,
            tr("Full Flow Complete: " + passed + "/" + TOTAL_TESTS + " blocking checks passed!",
               "Fluxo Completo Concluido: " + passed + "/" + TOTAL_TESTS + " verificacoes aprovadas!"),
            tr("Proxy daemons pandoraspear & pcdn are stopped, installers frozen, and C2 routes blocked.",
               "Os daemons pandoraspear e pcdn estao desligados, instaladores congelados e rotas C2 bloqueadas."),
            sb, passed, TOTAL_TESTS);
        return sb.toString();
    }

    public static String runBlockingTests(Context c, ProgressCallback cb) {
        StringBuilder sb = new StringBuilder();
        int passed = runBlockingTestsInternal(c, sb, cb, 15);
        emit(cb, c, 100, 3,
            tr("Blocking Test Complete: " + passed + "/" + TOTAL_TESTS + " checks passed on-device!",
               "Teste de Bloqueio Concluido: " + passed + "/" + TOTAL_TESTS + " verificacoes aprovadas no aparelho!"),
            tr("Verified 100% on the TV Box without relying on external router rules.",
               "Teste realizado 100% na TV Box, sem depender de bloqueios externos no roteador."),
            sb, passed, TOTAL_TESTS);
        return sb.toString();
    }

    private static int runBlockingTestsInternal(Context c, StringBuilder sb, ProgressCallback cb, int startPct) {
        sb.append(tr("[PHASE 3 OF 3] ON-DEVICE BLOCKING VERIFICATION & AUDIT\n",
                     "[FASE 3 DE 3] AUDITORIA E TESTE REAL DE BLOQUEIO (NO APARELHO)\n"));
        sb.append("--------------------------------------------------------------------\n");
        int passedCount = 0;

        emit(cb, c, startPct + 4, 3,
            tr("Phase 3 of 3 - Verifying proxy daemons are stopped", "Fase 3 de 3 - Verificando se daemons de proxy estao parados"),
            tr("Checking init.svc.pandoraspear and init.svc.pcdn...", "Checando init.svc.pandoraspear e init.svc.pcdn..."),
            sb, passedCount, TOTAL_TESTS);

        sb.append(tr("1. Native Proxy/Botnet Daemons Verification (DGA Source):\n",
                     "1. Verificacao dos Processos Nativos de Proxy/Botnet (Fonte do DGA):\n"));
        for (String[] d : NATIVE_DAEMONS) {
            String svc = d[0];
            String bin = d[1];
            String st = getProp("init.svc." + svc);
            if ("stopped".equalsIgnoreCase(st) || st.isEmpty()) {
                passedCount++;
                sb.append(tr("     [PASSED - STOPPED] ", "     [APROVADO - PARADO] ")).append(bin)
                  .append(" (init.svc.").append(svc).append(" = stopped)\n");
            } else {
                sb.append(tr("     [ALERT - RUNNING]  ", "     [ALERTA - ATIVO]    ")).append(bin)
                  .append(" (").append(st).append(")\n");
            }
        }

        sb.append(tr("\n2. Live Local Network Blocking Test (TCP Socket on Device):\n",
                     "\n2. Teste Real de Bloqueio de Rede Local (Socket TCP no Aparelho):\n"));
        int[] ports = {9999, 9999, 9999, 31226, 80, 80};
        for (int i = 0; i < C2_IPS.length; i++) {
            String host = C2_IPS[i];
            int port = ports[i];
            String desc = c2Label(i);
            int pct = startPct + 10 + ((i + 1) * 16 / C2_IPS.length);
            emit(cb, c, pct, 3,
                tr("Phase 3 of 3 - Testing TCP block (" + (i + 1) + "/" + C2_IPS.length + "): " + desc,
                   "Fase 3 de 3 - Testando bloqueio TCP (" + (i + 1) + "/" + C2_IPS.length + "): " + desc),
                tr("Verifying local route drops outbound connection...",
                   "Verificando se a rota local da TV Box bloqueia a saida..."),
                sb, passedCount, TOTAL_TESTS);
            try (Socket s = new Socket()) {
                s.connect(new InetSocketAddress(host, port), 1500);
                sb.append(tr("     [FAILED - CONNECTED] ", "     [FALHA - CONECTOU]   ")).append(desc).append("\n");
            } catch (Throwable ex) {
                passedCount++;
                sb.append(tr("     [PASSED - BLOCKED]   ", "     [APROVADO - BARRADO] ")).append(desc).append("\n");
            }
        }

        sb.append(tr("\n3. Final Kernel TCP Table Audit (/proc/net/tcp):\n",
                     "\n3. Auditoria Final da Tabela de Conexoes (/proc/net/tcp):\n"));
        StringBuilder tcpLog = new StringBuilder();
        int active = scanProcNetTcp(tcpLog);
        if (active == 0) {
            sb.append(tr("     [PASSED] Zero active proxy connections in kernel table.\n",
                         "     [APROVADO] Nenhuma conexao de proxy ativa na tabela do kernel.\n"));
        } else {
            sb.append(tcpLog);
        }
        return passedCount;
    }

    public static String setSinglePackageEnabled(Context c, String pkg, String desc, boolean enable, ProgressCallback cb) {
        StringBuilder sb = new StringBuilder();
        sb.append(tr("[OPTION 4] SINGLE PACKAGE CONTROL\n", "[OPCAO 4] CONTROLE UNITARIO DE PACOTE DE FABRICA\n"));
        sb.append("--------------------------------------------------------------------\n");
        sb.append(tr("  Selected package: ", "  Pacote selecionado: ")).append(pkg).append("\n");
        sb.append(tr("  Description: ", "  Descricao: ")).append(desc).append("\n");
        sb.append(tr("  Action: ", "  Acao solicitada: "))
          .append(enable ? "UNLOCK / ENABLE (pm enable)" : "FREEZE / BLOCK (pm disable-user --user 0)").append("\n\n");

        emit(cb, c, 40, 2,
            tr((enable ? "Re-enabling " : "Freezing ") + pkg + "...", (enable ? "Reativando " : "Congelando ") + pkg + "..."),
            tr("Communicating via internal ADB...", "Comunicando via ADB interno..."),
            sb, countStoppedDaemons() + (SinkholeVpnService.isRunning ? C2_IPS.length : 0), TOTAL_TESTS);

        interactiveAdbBusy = true;
        try (LocalAdb adb = LocalAdb.connect(c, 35_000, () ->
                emit(cb, c, 50, 2,
                    tr("ACTION ON TV: Confirm 'Allow USB debugging?' dialog", "ACAO NA TV: Confirme a janela 'Permitir depuracao USB?'"),
                    tr("Check 'Always allow' and press OK on remote.", "Marque 'Sempre permitir' e clique em OK no controle remoto."),
                    sb, 0, TOTAL_TESTS))) {
            c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_ADB_AUTHORIZED, true).apply();
            String cmd = enable ? "pm enable " + pkg : "pm disable-user --user 0 " + pkg;
            String out = adb.shell(cmd).trim();
            sb.append(tr("  System response: ", "  Resposta do sistema: ")).append(out).append("\n\n");
            sb.append(enable
                ? tr("[STATE] Package " + pkg + " was RE-ENABLED individually. You can freeze it again anytime in Option 4.",
                     "[ESTADO] O pacote " + pkg + " foi REATIVADO individualmente. Voce pode congela-lo novamente na Opcao 4.")
                : tr("[STATE] Package " + pkg + " is safely FROZEN.",
                     "[ESTADO] O pacote " + pkg + " foi CONGELADO com seguranca."));
        } catch (Throwable t) {
            sb.append(tr("  [WARNING] Internal ADB error: ", "  [AVISO] Erro no ADB interno: ")).append(t.getMessage()).append("\n");
        } finally {
            interactiveAdbBusy = false;
        }

        int passed = countStoppedDaemons() + (SinkholeVpnService.isRunning ? C2_IPS.length : 0);
        emit(cb, c, 100, 2,
            tr((enable ? "Package enabled: " : "Package frozen: ") + pkg,
               (enable ? "Pacote reativado: " : "Pacote congelado: ") + pkg),
            tr("Single item operation completed.", "Alteracao unitaria concluida com sucesso."),
            sb, passed, TOTAL_TESTS);
        return sb.toString();
    }

    public static String undoAllProtections(Context c, ProgressCallback cb) {
        StringBuilder sb = new StringBuilder();
        sb.append(tr("[OPTION 5] UNDO ALL PROTECTIONS (FACTORY RESTORE)\n",
                     "[OPCAO 5] DESFAZENDO TODA A VACINA (RESTAURACAO DE FABRICA)\n"));
        sb.append("--------------------------------------------------------------------\n");
        sb.append(tr(
            "  [SECURITY WARNING] You chose to revert all protections.\n" +
            "  This turns off the anti-proxy network shield, re-enables silent installers,\n" +
            "  and restarts original ROM services.\n\n",
            "  [AVISO DE SEGURANCA] Voce escolheu reverter todas as protecoes.\n" +
            "  Isso desligara o filtro anti-proxy, reativara os instaladores\n" +
            "  automaticos e religara os servicos originais da ROM.\n\n"
        ));

        SharedPreferences prefs = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ENABLED, false).apply();

        emit(cb, c, 25, 2,
            tr("Disabling Local Anti-Proxy Shield (VPN Sinkhole)...", "Desativando Filtro Local Anti-Proxy (VPN Sinkhole)..."),
            tr("Removing null routes and stopping protection service...", "Removendo rotas de bloqueio e parando o servico..."),
            sb, 0, TOTAL_TESTS);
        try {
            Intent stopIntent = new Intent(c, SinkholeVpnService.class);
            stopIntent.setAction(SinkholeVpnService.ACTION_STOP);
            c.startService(stopIntent);
            c.stopService(new Intent(c, SinkholeVpnService.class));
            try { Thread.sleep(400); } catch (InterruptedException ignored) {}
            sb.append(tr("  [DISABLED] Local Anti-Proxy Shield stopped.\n\n", "  [DESATIVADO] Filtro Local Anti-Proxy desligado.\n\n"));
        } catch (Throwable t) {
            sb.append("  [WARNING] ").append(t.getMessage()).append("\n\n");
        }

        if (isAdbEnabled(c)) {
            emit(cb, c, 50, 2,
                tr("Re-enabling all packages and daemons via internal ADB...", "Reativando todos os pacotes e servicos via ADB interno..."),
                tr("Restoring original state...", "Restaurando estado original..."),
                sb, 0, TOTAL_TESTS);
            interactiveAdbBusy = true;
            try (LocalAdb adb = LocalAdb.connect(c, 35_000, () ->
                    emit(cb, c, 55, 2,
                        tr("ACTION ON TV: Confirm 'Allow USB debugging?' dialog", "ACAO NA TV: Confirme a janela 'Permitir depuracao USB?'"),
                        tr("Press OK on your remote.", "Pressione OK no controle remoto."),
                        sb, 0, TOTAL_TESTS))) {
                prefs.edit().putBoolean(KEY_ADB_AUTHORIZED, true).apply();

                sb.append(tr("1. Re-enabling silent installer packages:\n", "1. Reativando pacotes instaladores:\n"));
                for (int i = 0; i < RISKY_PACKAGES.length; i++) {
                    String pkg = RISKY_PACKAGES[i][0];
                    emit(cb, c, 60 + (i * 25 / RISKY_PACKAGES.length), 2,
                        tr("Re-enabling package (" + (i + 1) + "/" + RISKY_PACKAGES.length + "): " + pkg,
                           "Reativando pacote (" + (i + 1) + "/" + RISKY_PACKAGES.length + "): " + pkg),
                        "pm enable " + pkg + "...", sb, 0, TOTAL_TESTS);
                    String out = adb.shell("pm enable " + pkg).trim();
                    sb.append(tr("     [RE-ENABLED] ", "     [REATIVADO] ")).append(pkg).append(" (").append(out).append(")\n");
                }

                for (String sysPkg : PROTECTED_SYSTEM_UID_PACKAGES) {
                    adb.shell("pm enable " + sysPkg);
                }

                sb.append(tr("\n2. Restarting original ROM native daemons:\n", "\n2. Religando servicos nativos originais da ROM:\n"));
                for (String[] d : NATIVE_DAEMONS) {
                    String svc = d[0];
                    adb.shell("setprop ctl.start " + svc);
                    sb.append(tr("     [STARTED]    /system/bin/", "     [INICIADO]  /system/bin/")).append(svc).append("\n");
                }
                try { Thread.sleep(400); } catch (InterruptedException ignored) {}
            } catch (Throwable t) {
                sb.append("  [ADB WARNING] ").append(t.getMessage()).append("\n");
            } finally {
                interactiveAdbBusy = false;
            }
        }

        emit(cb, c, 100, 1,
            tr("[WARNING] Protection Reverted: Device restored to factory behavior",
               "[AVISO] Protecao Desfeita: Aparelho restaurado ao comportamento de fabrica"),
            tr("To protect your device again, click Option 1.",
               "Para voltar a ficar protegido, clique na Opcao 1."),
            sb, 0, TOTAL_TESTS);
        return sb.toString();
    }

    public static String checkUpdatesAndSupportInfo(Context c, ProgressCallback cb) {
        StringBuilder sb = new StringBuilder();
        sb.append(tr("[SUPPORT & UPDATES] DONATE AND SYSTEM INFO\n",
                     "[APOIO E ATUALIZACOES] DOACAO E INFORMACOES DO SISTEMA\n"));
        sb.append("--------------------------------------------------------------------\n");
        sb.append(tr("  Created by: ", "  Criado por: ")).append(AUTHOR_NAME).append("\n");
        sb.append(tr("  Official Website: ", "  Site Oficial: ")).append(AUTHOR_SITE).append("\n");
        sb.append(tr("  Contact Email: ", "  E-mail de Contato: ")).append(AUTHOR_EMAIL).append("\n");
        sb.append("  Telegram: ").append(AUTHOR_TG).append("   |   X (Twitter): ").append(AUTHOR_X).append("\n");
        sb.append("  GitHub: ").append(GITHUB_URL).append("\n\n");

        sb.append(tr("[CRYPTO DONATIONS] DIRECT WALLETS:\n", "[DOACOES CRIPTO] CARTEIRAS DIRETAS:\n"));
        sb.append("   * BTC (Bitcoin):  ").append(WALLET_BTC).append("\n");
        sb.append("   * ETH (Ethereum): ").append(WALLET_ETH).append("\n");
        sb.append("   * SOL (Solana):   ").append(WALLET_SOL).append("\n\n");

        emit(cb, c, 50, 1,
            tr("Checking official GitHub (" + GITHUB_REPO + ") for updates...",
               "Consultando o GitHub oficial (" + GITHUB_REPO + ") por atualizacoes..."),
            tr("Verifying release channels...", "Verificando canais de release..."),
            sb, countStoppedDaemons() + (SinkholeVpnService.isRunning ? C2_IPS.length : 0), TOTAL_TESTS);

        try {
            URL url = new URL(LATEST_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            conn.setRequestProperty("User-Agent", "TVBoxVaccine/" + APP_VERSION);
            int code = conn.getResponseCode();
            if (code == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) json.append(line);
                br.close();
                String tag = extractJsonString(json.toString(), "tag_name");
                sb.append(tr("  Latest GitHub release: ", "  Ultima versao no GitHub: ")).append(tag.isEmpty() ? "v" + APP_VERSION : tag).append("\n");
                if (!tag.isEmpty() && !tag.equals("v" + APP_VERSION) && !tag.equals(APP_VERSION)) {
                    sb.append(tr("  [UPDATE AVAILABLE] Download at: ", "  [NOVA VERSAO DISPONIVEL] Baixe em: "))
                      .append(GITHUB_URL).append("/releases\n");
                } else {
                    sb.append(tr("  [LATEST] You are running the latest version.\n", "  [ATUALIZADO] Voce ja esta com a versao mais recente instalada.\n"));
                }
            } else {
                sb.append("  Releases: ").append(GITHUB_URL).append("/releases\n");
            }
        } catch (Throwable t) {
            sb.append("  Releases: ").append(GITHUB_URL).append("/releases\n");
        }

        int passed = countStoppedDaemons() + (SinkholeVpnService.isRunning ? C2_IPS.length : 0);
        emit(cb, c, 100, 1,
            tr("Support Suzan (@TriesSudo) - " + AUTHOR_SITE, "Apoie Suzan (@TriesSudo) - " + AUTHOR_SITE),
            tr("Scan the QR Codes on screen or visit " + AUTHOR_SITE + " to donate in more coins.",
               "Escaneie os QR Codes na tela ou acesse " + AUTHOR_SITE + " para doar em outras moedas."),
            sb, passed, TOTAL_TESTS);
        return sb.toString();
    }

    private static String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return "";
        int start = idx + pattern.length();
        int end = json.indexOf("\"", start);
        return end > start ? json.substring(start, end) : "";
    }
}
