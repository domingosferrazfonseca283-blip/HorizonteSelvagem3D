# Horizonte Selvagem 3D

Protótipo jogável de aventura/exploração 3D com personagens humanoides e criaturas originais. O foco é ligação, compromisso, lealdade, aventura, exploração e história — não apenas batalhas.

## Visão

A direção busca **aventura anime cinematográfica + exploração 3D madura**, usando apenas personagens, criaturas, nomes, cenários e arte originais. Não reproduz personagens, criaturas, nomes ou assets de Pokémon/Horizontes.

## Já incluído

- Mundo 3D procedural com floresta, rochas, névoa e iluminação dinâmica.
- Personagem humanoide original e criaturas originais.
- Controles WASD, corrida, salto e câmera com mouse.
- Sistema de missão, XP, nível e energia.
- Encontros com Pulso, Cuidar e Vínculo, colocando confiança acima da força.
- Fauna com vida, movimento e estados.
- Primeira etapa da história centrada em uma região rural, Mística e um ovo misterioso.
- HUD responsivo.
- Protótipo Android nativo em `android/`, com build automático via GitHub Actions.
- Cada build do Android no branch `master` também é publicada como GitHub Release pública com o APK.

## Executar Android

O APK de teste é gerado automaticamente pelo workflow **Android APK**. As releases públicas ficam na página de Releases do repositório.

## Executar Web

Abra `index.html` em um servidor estático. Para desenvolvimento local:

```bash
python -m http.server 8080
```

Depois visite `http://localhost:8080`.

## Próxima fase de produção

Para transformar o protótipo em um jogo comercial completo: substituir os meshes procedurais por modelos 3D licenciados/originais, adicionar animações e captura facial, áudio, inventário, árvores de habilidades, mapas por bioma, save/cloud, NPCs, quests, multiplayer opcional e pipeline de build para Android/Windows/Web.

## Jogar online

O jogo web é publicado automaticamente pelo GitHub Actions em cada alteração no branch `master`.

**Página do jogo:** https://domingosferrazfonseca283-blip.github.io/HorizonteSelvagem3D/

### Controles
- **WASD** — mover
- **SHIFT** — correr
- **Mouse** — olhar
- **ESPAÇO** — saltar
- **E** — interagir com criaturas próximas
- **F** — iniciar encontro com uma criatura
- **PULSO** — reduzir a vida da criatura
- **GUARDAR** — recuperar energia
- **VÍNCULO** — fortalecer uma relação baseada em confiança

> Na primeira publicação, pode ser necessário ativar **Settings → Pages** e selecionar **GitHub Actions** como fonte de publicação.
