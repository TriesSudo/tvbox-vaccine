<div align="center">

<img src="./assets/logo.png" alt="TV Box Vaccine Logo" width="130" />

# TV Box Vaccine (Vacina TV Box) v1.0

**On-Device Anti-Residential Proxy Shield, Native Daemon Killer and Silent Installer Blocker for Android TV Boxes**  
*Escudo Anti-Proxy Residencial, Desligamento de Daemons Nativos e Bloqueador de Instaladores Ocultos para Android TV Box*

[![Version](https://img.shields.io/badge/version-1.0-10B981?style=for-the-badge)](https://github.com/TriesSudo/tvbox-vaccine/releases/latest)
[![Download Release APK](https://img.shields.io/badge/Download-Release_v1.0-059669?style=for-the-badge&logo=android)](https://github.com/TriesSudo/tvbox-vaccine/releases/latest)
[![Website](https://img.shields.io/badge/Website-triessudo.zip-EC4899?style=for-the-badge)](https://triessudo.zip)
[![Telegram](https://img.shields.io/badge/Telegram-@triessudo-38BDF8?style=for-the-badge&logo=telegram)](https://t.me/triessudo)
[![X](https://img.shields.io/badge/X-@TriesSudo-A855F7?style=for-the-badge&logo=x)](https://x.com/TriesSudo)

**Author: Suzan ([@TriesSudo](https://github.com/TriesSudo))** | **[triessudo.zip](https://triessudo.zip)** | **triessudo@atomicmail.io**

[**English Documentation (Primary)**](#english-documentation-primary) | [**Documentacao em Portugues (PT-BR)**](#documentacao-em-portugues-pt-br) | [**Support & Donate**](#support-my-work--donate-crypto)

</div>

---

## English Documentation (Primary)

### Overview
TV Box Vaccine is a lightweight, on-device security tool built for Android TV devices (Allwinner H616/H313 boards, UniTV V10, and generic TV boxes). It neutralizes hidden background daemons that sell residential internet bandwidth to commercial proxy networks and freezes silent APK installer backdoors, while preserving critical vendor system packages to ensure 100% safe boot stability.

### Key Capabilities
- **Native Daemon Termination:** Shuts down native binaries `/system/bin/pandoraspear` and `/system/bin/pcdn` in memory using system property controls (`ctl.stop`).
- **Domain Generation Algorithm (DGA) Mitigation:** Disables the execution source of dynamic domain generation without modifying read-only system partitions.
- **Silent Installer Freezing:** Disables third-party silent installer services (`com.android.vending`, `com.swl.amarket`, `com.swl.cacheclean`) using user-level package management (`pm disable-user --user 0`).
- **Local Anti-Proxy Shield (VpnService Sinkhole):** Drops outbound communication to known Command & Control (C2) hosts locally using an on-device null route.
- **Bootloop Immunity:** Strictly preserves core `UID 1000` system packages (`com.softwinner.update`, `com.factorytools.factorystability`, `qw`) to prevent watchdog resets and Android `RescueParty` bootloops.
- **Dual Language Auto-Detection:** Automatically detects whether the Android TV operating system is set to English or Portuguese, with an instant 1-click toggle button.

---

### Verified Interface on TV Box Hardware

| English Interface (`EN-US`) | Portuguese Interface (`PT-BR`) |
| :---: | :---: |
| ![English UI](./screenshots/ui_impeccable_en.png) | ![Portuguese UI](./screenshots/ui_impeccable_pt.png) |

| On-Screen Crypto Donate Modal (Option 6) | Single Package Manager (Option 4) |
| :---: | :---: |
| ![Donate Modal](./screenshots/ui_impeccable_donate.png) | ![Option 4 Dialog](./screenshots/option4_manage.png) |

---

### Threat Analysis: What Does TV Box Vaccine Stop?

Commercial Android TV Box firmwares frequently integrate covert monetization components:

1. **`/system/bin/pandoraspear` (Residential Proxy / Botnet Daemon):**
   - Establishes outbound persistent TCP sockets to external C2 nodes on port `9999`.
   - Routes third-party web traffic through the consumer's home IP address, exposing residential connections to blacklisting and abuse complaints.
   - Leverages a Domain Generation Algorithm (DGA) to cycle through domains periodically.
2. **`/system/bin/pcdn` (Background P2P Upload Client):**
   - Silently consumes domestic upload bandwidth on port `31226` to distribute third-party streaming assets.
3. **Third-Party Silent APK Droppers:**
   - Packages including `com.android.vending` (an unofficial, modified Tubesky store), `com.swl.amarket`, and `com.swl.cacheclean` hold installation privileges and download APK payloads without user consent.

---

### Step-by-Step Installation (USB Flash Drive)

1. Download **`TVBoxVaccine.apk`** from the **[Latest GitHub Releases](https://github.com/TriesSudo/tvbox-vaccine/releases/latest)**.
2. Copy the APK file to a USB flash drive and plug it into the TV Box.
3. Open the TV Box **File Manager** and install the APK.
4. **Enable USB Debugging** on the device (required for internal loopback communication):
   - Go to **Settings** -> **Device Preferences** -> **About**.
   - Click **Build number** **7 times** until Developer Mode is unlocked.
   - Go back, open **Developer Options**, and toggle **USB Debugging** to ON.
5. Launch **TV Box Vaccine** from your app drawer.
6. Click **`1. Analyze + Vaccinate + Test`**.
7. Accept the **Connection request (VPN)** and check **"Always allow from this computer"** on the USB debugging authorization prompt.
8. The live audit will confirm:
   - `Native Daemons: 2/2 stopped`
   - `Hidden Apps: 3/3 frozen`
   - `Block Test: 8/8 passed`

---

## Documentacao em Portugues (PT-BR)

### Visao Geral
A Vacina TV Box e uma ferramenta de seguranca desenvolvida para aparelhos Android TV (placas Allwinner H616/H313, UniTV V10 e modelos similares). O aplicativo desliga daemons nativos ocultos que comercializam a conexao residencial como proxy comercial e congela instaladores silenciosos de segundo plano, garantindo inicializacao estavel sem risco de bootloop.

### Funcionalidades
- **Encerramento de Processos Nativos:** Desliga `/system/bin/pandoraspear` e `/system/bin/pcdn` na memoria via comandos de sistema (`ctl.stop`).
- **Defesa Contra DGA:** Corta a fonte geradora de dominios variaveis diretamente no processo, sem alterar a particao `/system`.
- **Congelamento de Instaladores Ocultos:** Desativa servicos de instalacao silenciosa (`com.android.vending`, `com.swl.amarket`, `com.swl.cacheclean`) de forma reversivel (`pm disable-user --user 0`).
- **Filtro Local Anti-Proxy (VPN Sinkhole):** Bloqueia rotas para servidores de comando e controle (C2) diretamente no aparelho, sem reduzir a velocidade da internet.
- **Arquitetura de Boot Seguro:** Preserva pacotes vitais de sistema com `UID 1000` (`com.softwinner.update`, `com.factorytools.factorystability`, `qw`), prevenindo disparos de watchdog e loops de boot.
- **Deteccao Automatica de Idioma:** Reconhece o idioma padrao do sistema (Portugues ou Ingles) no primeiro carregamento.

---

### Instalacao via Pendrive (Passo a Passo)

1. Baixe o arquivo **`TVBoxVaccine.apk`** na secao de **[Releases Oficiais](https://github.com/TriesSudo/tvbox-vaccine/releases/latest)**.
2. Copie o APK para um pendrive e conecte-o na porta USB da TV Box.
3. Abra o gerenciador de arquivos e conclua a instalacao.
4. **Ative a Depuracao USB**:
   - Acesse **Configuracoes** -> **Preferencias do Dispositivo** -> **Sobre**.
   - Clique **7 vezes** consecutivas em **Numero da versao**.
   - Retorne, entre em **Opcoes do desenvolvedor** e ative a **Depuracao USB**.
5. Abra o aplicativo **Vacina TV Box**.
6. Clique no botao **`1. Analisar + Vacinar + Testar`**.
7. Autorize a conexao do filtro local (VPN) e marque a opcao **"Sempre permitir a partir deste computador"** no aviso de depuracao USB.
8. O placar de telemetria confirmara a protecao completa do aparelho.

---

## Support My Work / Donate (Crypto)

If this project helped protect your home bandwidth and clean your TV Box, consider supporting independent anti-malware research:

| Cryptocurrency | Network | Wallet Address |
| :--- | :--- | :--- |
| **Bitcoin (BTC)** | Native BTC | `bc1qpe2uwvtjkyuu7ntt5rnsnv823cdmc66t45q9wg` |
| **Ethereum (ETH)** | ERC-20 | `0x88f633DAd7830e6512634445028979c6d2dF60Da` |
| **Solana (SOL)** | Solana | `G6NgGBpvAtazdpKn1tcBvcPztBgoxbi2hXgxV4aiaAQD` |

- Official Website (additional coins and updates): **[https://triessudo.zip](https://triessudo.zip)**
- Direct Contact: **`triessudo@atomicmail.io`**
- Telegram: **[t.me/triessudo](https://t.me/triessudo)**
- X (Twitter): **[@TriesSudo](https://x.com/TriesSudo)**

---

## Technical Audit & Source Compilation

```bash
git clone https://github.com/TriesSudo/tvbox-vaccine.git
cd tvbox-vaccine
# Compiles with Android SDK build-tools 34+ and Java 8+
```

### Privacy & Anonymity Audit
- Zero telemetry, zero analytics, zero external logging.
- 100% on-device operation.
