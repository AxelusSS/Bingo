import sys

doc_path = r"C:\Users\louis\.gemini\antigravity\brain\1cbc4e6e-f70e-480b-b0fd-84a9acaf3d42\hel_documentation.md"

with open(doc_path, 'r', encoding='utf-8') as f:
    c = f.read()

docker_section = '''
## 🐳 Hébergement & Docker (Coolify)
Si vous souhaitez héberger ce serveur sur un VPS utilisant Docker ou Coolify, voici le fichier docker-compose.yml recommandé (basé sur l'image itzg/minecraft-server:java21). 
Ce fichier inclut automatiquement les plugins requis (comme FastAsyncWorldEdit et Chunky), et configure le ressource pack :

`yaml
services:
  bingo-server:
    image: 'itzg/minecraft-server:java21'
    container_name: bingo-server
    cpu_shares: 2048
    ports:
      - '25565:25565'
    environment:
      TYPE: PURPUR
      VERSION: 1.21.3
      EULA: 'true'
      ONLINE_MODE: 'true'
      INIT_MEMORY: 2G
      MAX_MEMORY: 6G
      USE_AIKAR_FLAGS: 'true'
      PLUGINS: 'https://github.com/AxelusSS/Bingo/releases/download/V1.6.0/bingo-1.6.0.jar'
      MODRINTH_PROJECTS: 'chunky, fastasyncworldedit'
      MOTD: '§b§lServeur Bingo HEL §r- §aPrêt pour la game !'
      SPAWN_PROTECTION: '0'
      ENABLE_COMMAND_BLOCK: 'true'
      RESOURCE_PACK: 'https://github.com/AxelusSS/Bingo/releases/download/V1.6.0/BingoPack.zip'
      RESOURCE_PACK_SHA1: <METTRE_LE_NOUVEAU_SHA1_ICI>
      REQUIRE_RESOURCE_PACK: 'true'
      ICON: 'https://github.com/AxelusSS/Bingo/releases/download/V1.6.0/server-icon.png'
    volumes:
      - 'bingo_data:/data'
volumes:
  bingo_data:
    name: bingo_data
`
'''

with open(doc_path, 'w', encoding='utf-8') as f:
    f.write(c + docker_section)

