# Pipeline de personagens 3D

O projeto passa a ter uma camada preparada para modelos **glTF 2.0 / GLB**.

## Renderer

O Android usa Google Filament 1.77.2 como base para a futura renderização PBR. O Filament possui suporte a glTF 2.0, incluindo modelos binários GLB, materiais PBR, skinning e animações. A integração será feita de forma incremental para preservar a lógica atual de capítulos, missões e salvamento.

## Organização

- `android/assets/3d/characters/` — personagens humanos.
- `android/assets/3d/animals/` — animais reais do ambiente rural.
- `android/assets/3d/creatures/` — criaturas originais de Horizonte Selvagem.
- `android/assets/3d/environment/` — objetos e elementos do cenário.

## Requisitos para cada modelo

Preferir GLB com:
- materiais PBR;
- texturas incorporadas;
- escala consistente;
- origem/pivô no chão;
- animações nomeadas quando existirem;
- licença compatível com redistribuição no jogo.

Não adicionar modelos de franquias de terceiros. Personagens e criaturas do universo de Horizonte Selvagem devem permanecer originais.

## Próxima integração

1. carregar um GLB de teste pelo glTF loader;
2. sincronizar posição/rotação com o sistema de jogo;
3. adicionar animações idle/walk/run;
4. substituir gradualmente os modelos procedurais de Kael, NPCs e animais;
5. manter Mística, Aurino e Lumea como criaturas autorais, com modelos próprios.

A camada procedural atual continua como fallback enquanto os assets 3D finais são integrados.
