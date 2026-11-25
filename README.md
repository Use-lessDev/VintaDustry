# VintaDustry

**Versão:** 1.1  
**Autor:** morajoe  
**Mindustry:** v154+  
**Tipo:** Java Mod

---

## Descrição

Mod derivado do Gamma que adiciona mecânicas de recursos duplos e árvores interativas (DonaTree) com galhos que podem ser colhidos.

---

## Funcionalidades

### 🌳 DonaTree - Árvores Interativas
- **Árvores modernas** com 8 galhos que crescem ao longo do tempo (5400 ticks = ~90 min por galho)
- **Colheita de galhos**: Clique em galhos totalmente crescidos (growth ≥ 1.0) para coletar
- **Puxar árvore inteira**: Quando NÃO há galhos crescidos, puxe a árvore inteira
- **Efeitos visuais**: Animações de crescimento, vento, e partículas ao colher

### 📦 Sistema de Cargo
- **DualUnit**: Unidade que carrega 2 tipos de recursos simultaneamente
- **CrateUnit**: Unidade de armazenamento expansível
- **AutoCargo**: Automação de coleta/entrega de recursos

### ⛏️ AutoDrill
- Perfuração automática com layout otimizado
- Suporte a múltiplos tipos de brocas
- Layouts: padrão, linha, espiral, grade

### 🌲 Sistema de Troncos
- **BranchUnit**: Galhos soltos que podem ser coletados
- **TreeBreakPacket/TreeSyncPacket**: Sincronização multiplayer de árvores
- **TreeFx**: Efeitos visuais de quebra/crescimento

### 🤖 Unidades Especiais
- **BranchUnit**: Galhos soltos que podem ser carregados
- **DualUnit**: Carrega 2 recursos, transfere entre si
- **CrateUnit**: Armazenamento móvel expansível
- **GumpyUnit**: Unidade de coleta
- **TaskyNest**: Ninho que spawna unidades

### 🔨 Blocos Especiais
- **PlankerSaw**: Serra que processa madeira
- **Boulder / BoulderOres**: Rochas com minérios
- **CrateTransferPacket**: Transferência de itens entre crates
- **Hand**: Sistema de "mão" para interação

### 🎮 Packets Personalizados
- `PullPacket` - Puxar galho/árvore
- `HookPacket` - Gancho de puxar
- `TreeBreakPacket` - Quebrar árvore
- `TreeSyncPacket` - Sincronização de árvore
- `TrunkPacket` - Sincronização de tronco
- `PullPacket` - Puxar galho/árvore inteira
- `SlotDropPacket` - Soltar item em slot
- `StashPacket` - Esconderijo
- `CrateTransferPacket` - Transferência entre crates
- `HandPacket` - Ações da mão
- `DragPacket` - Arrastar
- `AimPacket` - Mira

---

## Instalação

### Para Jogadores
1. Baixe o arquivo `.jar` da [página de Releases](https://github.com/seu-usuario/VintaDustry/releases)
2. No Mindustry: **Mods → Import Mod → Import File**
3. Selecione o arquivo `.jar` baixado
4. Reinicie o jogo se necessário

### Para Desenvolvedores
```bash
# Clone o repositório
git clone https://github.com/seu-usuario/VintaDustry.git
cd VintaDustry

# Compile
./gradlew jar

# O .jar será gerado em build/libs/
# Instale copiando para a pasta de mods do Mindustry
```

---

## Compilação

### Requisitos
- Java 17+
- Gradle 8.1+

### Comandos
```bash
# Compilar
./gradlew jar

# Limpar
./gradlew clean

# Deploy direto para pasta de mods (Linux/Mac)
./gradlew deployMod

# Windows
gradlew.bat deployMod
```

---

## Estrutura do Projeto

```
VintaDustry/
├── src/main/java/vintadustry/     # Código fonte Java
│   ├── TestMod.java               # Classe principal do mod
│   ├── DonaTree.java              # Árvore interativa com galhos
│   ├── BranchUnit.java            # Unidade de galho solto
│   ├── DualUnit.java              # Unidade de 2 recursos
│   ├── CrateUnit.java             # Unidade de armazenamento
│   ├── AutoDrill.java             # Perfuração automática
│   ├── AutoCargo.java             # Automação de cargo
│   ├── PullPacket.java            # Packet: puxar galho/árvore
│   ├── HookPacket.java            # Gancho
│   ├── TreeBreakPacket.java       # Quebrar árvore
│   ├── TreeSyncPacket.java        # Sync árvore
│   ├── TrunkPacket.java           # Tronco
│   ├── HandPacket.java            # Mão
│   ├── HookPacket.java            # Gancho
│   ├── DragPacket.java            # Arrastar
│   ├── AimPacket.java             # Mira
│   ├── SlotDropPacket.java        # Soltar slot
│   ├── StashPacket.java           # Esconderijo
│   ├── CrateTransferPacket.java   # Transfer crate
│   ├── HandPacket.java            # Mão
│   ├── AimPacket.java             # Mira
│   ├── Boulder.java               # Rocha
│   ├── BoulderOres.java           # Rochas com minérios
│   ├── PlankerSaw.java            # Serra de madeira
│   ├── BranchUnit.java            # Galho solto
│   ├── DualUnit.java              # Unidade dual
│   ├── CrateUnit.java             # Unidade crate
│   ├── GumpyUnit.java             # Unidade Gumpy
│   ├── TaskyNest.java             # Ninho
│   ├── TaskyFx.java               # Efeitos Tasky
│   ├── TreeFx.java                # Efeitos árvore
│   ├── TreeFx.java                # Efeitos árvore
│   ├── TreeBreakPacket.java       # Quebrar árvore
│   ├── TreeSyncPacket.java        # Sync árvore
│   ├── TrunkPacket.java           # Tronco
│   ├── PullPacket.java            # Puxar
│   ├── HookPacket.java            # Gancho
│   ├── HandPacket.java            # Mão
│   ├── DragPacket.java            # Arrastar
│   ├── AimPacket.java             # Mira
│   ├── SlotDropPacket.java        # Soltar slot
│   ├── StashPacket.java           # Esconderijo
│   ├── CrateTransferPacket.java   # Transfer crate
│   ├── HandPacket.java            # Mão
│   ├── AimPacket.java             # Mira
│   ├── UnitMenu.java              # Menu de unidade
│   ├── CrateUI.java               # UI de crate
│   ├── Hand.java                  # Sistema de mão
│   ├── Rope.java                  # Corda
│   ├── Follow.java                # Seguir
│   ├── GumpyShot.java             # Tiro Gumpy
│   ├── DeathScreen.java           # Tela de morte
│   ├── UnitMenu.java              # Menu unidade
│   ├── CrateUI.java               # UI crate
│   ├── Hand.java                  # Mão
│   ├── Rope.java                  # Corda
│   ├── Follow.java                # Seguir
│   ├── GumpyShot.java             # Tiro
│   ├── DeathScreen.java           # Morte
│   ├── LobbyAmbient.java          # Ambiente lobby
│   ├── PartMap.java               # Mapa de partes
│   ├── PlankerSaw.java            # Serra
│   ├── CrateUI.java               # UI crate
│   ├── Hand.java                  # Mão
│   ├── Rope.java                  # Corda
│   ├── Follow.java                # Seguir
│   ├── GumpyShot.java             # Tiro
│   ├── DeathScreen.java           # Morte
│   ├── LobbyAmbient.java          # Lobby
│   ├── PartMap.java               # PartMap
│   ├── PlankerSaw.java            # Serra
│   ├── CrateTransferPacket.java   # Transfer
│   ├── HandPacket.java            # Mão
│   ├── DragPacket.java            # Drag
│   ├── AimPacket.java             # Aim
│   ├── SlotDropPacket.java        # Slot drop
│   ├── StashPacket.java           # Stash
│   ├── TaskyFx.java               # Tasky FX
│   ├── TaskyNest.java             # Tasky Nest
│   ├── TestMod.java               # Main mod class
│   ├── TreeBreakPacket.java       # Tree break
│   ├── TreeFx.java                # Tree FX
│   ├── TreeSyncPacket.java        # Tree sync
│   ├── TrunkPacket.java           # Trunk
│   ├── UnitMenu.java              # Unit menu
│   └── ...                        # Demais classes
├── src/main/resources/
│   ├── content/           # Blocos, unidades, itens (JSON)
│   ├── sprites/           # Sprites/imagens
│   ├── sounds/            # Sons
│   ├── music/             # Músicas
│   ├── bundles/           # Traduções
│   └── mod.json           # Manifesto do mod
├── build.gradle
├── settings.gradle
├── .gitignore
└── README.md
```

---

## Créditos

- **Base:** Gamma (placeholder)
- **Autor:** morajoe
- **Engine:** Mindustry v154+

---

## Licença

Este mod é baseado no Gamma e segue as mesmas diretrizes de uso.
