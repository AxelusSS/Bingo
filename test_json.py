import json
root = '''{
  "display": {
    "icon": { "id": "minecraft:nether_star" },
    "title": { "text": "Hel Classique" },
    "description": { "text": "Utilisez /bg pour voir votre progression" },
    "background": "minecraft:textures/block/light_blue_concrete_powder.png",
    "show_toast": false,
    "announce_to_chat": false,
    "hidden": false
  },
  "criteria": {
    "auto": { "trigger": "minecraft:tick" }
  }
}'''
item = '''{
  "parent": "hel:root",
  "display": {
    "icon": { "id": "minecraft:stone" },
    "title": { "text": "Stone" },
    "description": { "text": "Obtenir Stone" },
    "frame": "challenge",
    "show_toast": false,
    "announce_to_chat": false,
    "hidden": false
  },
  "criteria": {
    "found": { "trigger": "minecraft:impossible" }
  }
}'''
json.loads(root)
json.loads(item)
print('JSON IS VALID')
