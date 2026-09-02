package net.cobbleservertools.compat.rct.runbun.api.ai.utils;

import java.util.ArrayList;
import java.util.List;

public class RBMoveList {
   private static final List<String> priorityDamageMoves = new ArrayList<>(
      List.of(
         "quickattack",
         "extremespeed",
         "fakeout",
         "firstimpression",
         "accelerock",
         "aquajet",
         "bulletpunch",
         "iceshard",
         "machpunch",
         "shadowsneak",
         "suckerpunch",
         "thunderclap",
         "vacuumwave",
         "watershuriken",
         "jetpunch"
      )
   );
   private static final List<String> abilityStatBooster = new ArrayList<>(List.of("moxie", "beastboost", "chillingneigh", "grimneigh"));
   private static final List<String> highCriticalMoves = new ArrayList<>(
      List.of(
         "aeroblast",
         "aircutter",
         "aquacutter",
         "attackorder",
         "blazekick",
         "crabhammer",
         "crosschop",
         "crosspoison",
         "direclaw",
         "drillrun",
         "esperwing",
         "ivycudgel",
         "karatechop",
         "leafblade",
         "nightslash",
         "poisontail",
         "psychocut",
         "razorleaf",
         "razorwind",
         "shadowblast",
         "shadowclaw",
         "skyattack",
         "slash",
         "snipeshot",
         "spacialrend",
         "stoneedge",
         "triplearrows"
      )
   );
   private static final List<String> trapMoves = new ArrayList<>(List.of("bind", "firespin", "infestation", "sandtomb", "whirlpool", "wrap"));
   private static final List<String> speedReductionMoves = new ArrayList<>(
      List.of("bulldoze", "electroweb", "icywind", "lowsweep", "mudshot", "pounce", "rocktomb")
   );
   private static final List<String> physicalAttackReductionMoves = new ArrayList<>(
      List.of("breakingswipe", "lunge", "tropkick", "skittersmack", "spiritbreak")
   );
   private static final List<String> specialAttackReductionMoves = new ArrayList<>(List.of("chillingwater", "mysticalfire", "snarl", "strugglebug"));
   private static final List<String> generalSetupMoves = new ArrayList<>(
      List.of(
         "poweruppunch",
         "swordsdance",
         "howl",
         "stuffcheeks",
         "barrier",
         "acidarmor",
         "irondefense",
         "cottonguard",
         "chargebeam",
         "tailglow",
         "nastyplot",
         "cosmicpower",
         "bulkup",
         "calmmind",
         "dragondance",
         "coil",
         "honeclaws",
         "quiverdance",
         "shiftgear",
         "shellsmash",
         "growth",
         "workup",
         "curse",
         "coil",
         "noretreat",
         "tidyup",
         "geomancy",
         "filletaway"
      )
   );
   private static final List<String> ignoreStatDropAbilities = new ArrayList<>(List.of("contrary", "clearbody", "whitesmoke"));
   private static final List<String> specialFunctionMoves = new ArrayList<>(
      List.of(
         "futuresight",
         "spikyshield",
         "relicsong",
         "suckerpunch",
         "thunderclap",
         "pursuit",
         "fellstinger",
         "rollout",
         "stealthrock",
         "spikes",
         "toxicspikes",
         "stickyweb",
         "protect",
         "kingsshield",
         "burningbulwark",
         "detect",
         "obstruct",
         "silktrap",
         "banefulbunker",
         "fling",
         "roleplay",
         "shadowsneak",
         "aquajet",
         "iceshard",
         "magnitude",
         "earthquake",
         "imprison",
         "batonpass",
         "tailwind",
         "trickroom",
         "fakeout",
         "helpinghand",
         "followme",
         "finalgambit",
         "electricterrain",
         "psychicterrain",
         "grassyterrain",
         "mistyterrain",
         "lightscreen",
         "reflect",
         "substitute",
         "explosion",
         "selfdestruct",
         "mistyexplosion",
         "memento",
         "thunderwave",
         "stunspore",
         "glare",
         "nuzzle",
         "zapcannon",
         "willowisp",
         "trick",
         "switcheroo",
         "yawn",
         "darkvoid",
         "grasswhistle",
         "sing",
         "dreameater",
         "nightmare",
         "snore",
         "sleeptalk",
         "hypnosis",
         "lovelykiss",
         "sleeppowder",
         "spore",
         "poisongas",
         "poisonpoder",
         "toxic",
         "mirrorcoat",
         "counter",
         "raindance",
         "sunnyday",
         "sandstorm",
         "chillyreception",
         "hail",
         "snowscape",
         "bloodmoon",
         "gigatonhammer",
         "revivalblessing",
         "ruination",
         "shedtail",
         "trick",
         "poisonpowder",
         "wakeupslap",
         "venomdrench",
         "shelltrap"
      )
   );
   private static final List<String> soundMoves = new ArrayList<>(
      List.of(
         "alluringvoice",
         "boomburst",
         "bugbuzz",
         "chatter",
         "clangingscales",
         "clangoroussoul",
         "clangoroussoulblaze",
         "confide",
         "disarmingvoice",
         "echoedvoice",
         "eeriespell",
         "grasswhistle",
         "growl",
         "healbell",
         "howl",
         "hypervoice",
         "metalsound",
         "nobleroar",
         "overdrive",
         "partingshot",
         "perishsong",
         "psychicnoise",
         "relicsong",
         "roar",
         "round",
         "screech",
         "shadowpanic",
         "sing",
         "snarl",
         "snore",
         "sparklingaria",
         "supersonic",
         "torchsong",
         "uproar"
      )
   );
   private static final List<String> flinchMoves = new ArrayList<>(
      List.of(
         "airslash",
         "astonish",
         "bite",
         "boneclub",
         "darkpulse",
         "doubleironbash",
         "dragonrush",
         "extrasensory",
         "fierywrath",
         "firefang",
         "floatyfall",
         "headbutt",
         "heartstamp",
         "hyperfang",
         "icefang",
         "iciclecrash",
         "ironhead",
         "mountaingale",
         "needlearm",
         "rockslide",
         "rollingkick",
         "skyattack",
         "steamroller",
         "stomp",
         "thunderfang",
         "triplearrows",
         "twister",
         "waterfall",
         "zenheadbutt",
         "zingzap"
      )
   );
   private static final List<String> thawingMoves = new ArrayList<>(
      List.of("burnup", "flamewheel", "flareblitz", "fusionflare", "matchagatcha", "pyroball", "sacredfire", "scald", "scorchingsands", "steameruption")
   );
   private static final List<String> rechargeMoves = new ArrayList<>(
      List.of(
         "blastburn",
         "eternabeam",
         "frenzyplant",
         "gigaimpact",
         "hydrocannon",
         "hyperbeam",
         "meteorassault",
         "prismaticlaser",
         "roaroftime",
         "rockwrecker",
         "shadowhalf"
      )
   );
   private static final List<String> recoveryMoves = new ArrayList<>(
      List.of(
         "roost", "slackoff", "healorder", "recover", "strengthsap", "morningsun", "synthesis", "moonlight", "rest", "junglehealing", "lifedew", "softboiled"
      )
   );
   private static final List<String> megaStones = new ArrayList<>(
      List.of(
         "absolite",
         "kangaskhanite",
         "aerodactylite",
         "aggronite",
         "alakazite",
         "altarianite",
         "ampharosite",
         "audinite",
         "banettite",
         "beedrillite",
         "blastoisinite",
         "blazikenite",
         "cameruptite",
         "charizarditex",
         "charizarditey",
         "diancite",
         "gardevoirite",
         "galladite",
         "gyaradosite",
         "garchompite",
         "gengarite",
         "heracronite",
         "houndoominite",
         "lopunnite",
         "lucarionite",
         "latiasite",
         "latiosite",
         "manectite",
         "mawilite",
         "metagrossite",
         "pinsirite",
         "sablenite",
         "salamencite",
         "sharpedonite",
         "slowbronite",
         "steelixite",
         "swampertite",
         "sceptilite",
         "scizorite",
         "tyranitarite",
         "venusaurite",
         "mewtwonitex",
         "mewtwonitey",
         "clefablite",
         "victreebelite",
         "starminite",
         "dragoninite",
         "meganiumite",
         "feraligite",
         "skarmorite",
         "froslassite",
         "emboarite",
         "excadrite",
         "scolipite",
         "scraftinite",
         "eelektrossite",
         "chandelurite",
         "chesnaughtite",
         "delphoxite",
         "greninjite",
         "pyroarite",
         "floettite",
         "malamarite",
         "barbaracite",
         "dragalgite",
         "hawluchanite",
         "zygardite",
         "drampanite",
         "falinksite",
         "raichunitex",
         "raichunitey",
         "chimechite",
         "absolitez",
         "staraptite",
         "garchompitez",
         "lucarionitez",
         "heatranite",
         "darkranite",
         "golurkite",
         "meowsticite",
         "crabominite",
         "golisopite",
         "magearnite",
         "zeraorite",
         "scovillainite",
         "glimmoranite",
         "tatsugirinite",
         "baxcalibrite"
      )
   );
   private static final List<String> ignoreDamageMoves = new ArrayList<>(
      List.of("explosion", "selfdestruct", "mistyexplosion", "rollout", "meteorbeam", "finalgambit", "relicsong", "futuresight")
   );
   private static final List<String> ignoreSleepAbilities = new ArrayList<>(
      List.of("comatose", "insomnia", "vitalspirit", "sweetveil", "purifyingsalt", "goodasgold")
   );
   private static final List<String> sleepMoves = new ArrayList<>(
      List.of("spore", "yawn", "darkvoid", "lovelykiss", "sleeppowder", "hypnosis", "sing", "grasswhistle")
   );
   private static final List<String> statusMoves = new ArrayList<>(
      List.of(
         "acupressure",
         "acidarmor",
         "afteryou",
         "agility",
         "allyswitch",
         "amnesia",
         "aquaring",
         "aromatherapy",
         "aromaticmist",
         "assist",
         "attract",
         "auroraveil",
         "autotomize",
         "defendorder",
         "defensecurl",
         "defog",
         "destinybond",
         "detect",
         "disable",
         "doubleteam",
         "dragondance",
         "eerieimpulse",
         "electricterrain",
         "electrify",
         "embargo",
         "encore",
         "endure",
         "entrainment",
         "featherdance",
         "filletaway",
         "finalgambit",
         "flatter",
         "floralhealing",
         "flowershield",
         "focusenergy",
         "followme",
         "forestscurse",
         "foresight",
         "gastroacid",
         "gearup",
         "glare",
         "growth",
         "grassyterrain",
         "gravity",
         "growl",
         "guardsplit",
         "guardswap",
         "hail",
         "haze",
         "healbell",
         "healblock",
         "healorder",
         "healpulse",
         "helpinghand",
         "howl",
         "hypnosis",
         "imprison",
         "ingrain",
         "instruct",
         "iondeluge",
         "kinesis",
         "kingsshield",
         "laserfocus",
         "leechseed",
         "lightscreen",
         "lockon",
         "luckychant",
         "lunardance",
         "magiccoat",
         "magicpowder",
         "magnetrise",
         "meanlook",
         "meditate",
         "mimic",
         "minimize",
         "mist",
         "mistyterrain",
         "moonlight",
         "morningsun",
         "mudsport",
         "nastyplot",
         "naturepower",
         "nuzzle",
         "octolock",
         "odorsleuth",
         "painsplit",
         "partingshot",
         "perishsong",
         "playnice",
         "poisongas",
         "poisonpowder",
         "powersplit",
         "powerswap",
         "powertrick",
         "present",
         "protect",
         "psychicterrain",
         "psychup",
         "purify",
         "quash",
         "quickguard",
         "ragepowder",
         "raindance",
         "recycle",
         "reflect",
         "reflecttype",
         "refresh",
         "rest",
         "roar",
         "roleplay",
         "safeguard",
         "sandattack",
         "sandstorm",
         "scaryface",
         "screech",
         "sharpen",
         "shellsmash",
         "shoreup",
         "skillswap",
         "sleeppowder",
         "sleeptalk",
         "smokescreen",
         "snatch",
         "softboiled",
         "spikes",
         "spiderweb",
         "spikyshield",
         "splash",
         "spore",
         "stealthrock",
         "stickyweb",
         "stockpile",
         "stringshot",
         "substitute",
         "sunnyday",
         "supersonic",
         "swagger",
         "sweetkiss",
         "sweetscent",
         "switcheroo",
         "swordsdance",
         "synthesis",
         "tailglow",
         "tailwhip",
         "tailwind",
         "taunt",
         "teeterdance",
         "teleport",
         "tickle",
         "torment",
         "toxic",
         "toxicspikes",
         "trick",
         "trickroom",
         "venomdrench",
         "whirlwind",
         "wideguard",
         "willowisp",
         "wish",
         "withdraw",
         "wonderroom",
         "workup",
         "yawn"
      )
   );
   private static final List<String> criticalMoves = new ArrayList<>(
      List.of("flowertrick", "frostbreath", "stormthrow", "surgingstrikes", "wickedblow", "zippyzap")
   );
   private static final List<String> punchingMoves = new ArrayList<>(
      List.of(
         "bulletpunch",
         "cometpunch",
         "dizzypunch",
         "doubleironbash",
         "drainpunch",
         "dynamicpunch",
         "firepunch",
         "focuspunch",
         "hammerarm",
         "headlongrush",
         "icehammer",
         "icepunch",
         "jetpunch",
         "machpunch",
         "megapunch",
         "meteormash",
         "plasmafists",
         "poweruppunch",
         "ragefist",
         "shadowpunch",
         "skyuppercut",
         "surgingstrikes",
         "thunderpunch",
         "wickedblow"
      )
   );
   private static final List<String> directDamageMoves = new ArrayList<>(
      List.of(
         "bide",
         "comeuppance",
         "counter",
         "dragonrage",
         "endeavor",
         "finalgambit",
         "guardianofalola",
         "metalburst",
         "mirrorcoat",
         "nature'smadness",
         "nightshade",
         "psywave",
         "ruination",
         "seismictoss",
         "shadowhalf",
         "sonicboom",
         "superfang"
      )
   );
   private static final List<String> windMoves = new ArrayList<>(
      List.of(
         "aircutter",
         "bleakwindstorm",
         "blizzard",
         "fairywind",
         "gust",
         "heatwave",
         "hurricane",
         "icywind",
         "petalblizzard",
         "sandsearstorm",
         "sandstorm",
         "springtidestorm",
         "tailwind",
         "twister",
         "whirlwind",
         "wildboltstorm"
      )
   );
   private static final List<String> contactMoves = new ArrayList<>(
      List.of(
         "pound",
         "karatechop",
         "doubleslap",
         "cometpunch",
         "megapunch",
         "firepunch",
         "icepunch",
         "thunderpunch",
         "scratch",
         "visegrip",
         "guillotine",
         "cut",
         "wingattack",
         "fly",
         "bind",
         "slam",
         "vinewhip",
         "stomp",
         "doublekick",
         "megakick",
         "jumpkick",
         "rollingkick",
         "headbutt",
         "hornattack",
         "furyattack",
         "horndrill",
         "tackle",
         "bodyslam",
         "wrap",
         "takedown",
         "thrash",
         "doubleedge",
         "bite",
         "peck",
         "drillpeck",
         "submission",
         "lowkick",
         "counter",
         "seismictoss",
         "strength",
         "petaldance",
         "dig",
         "quickattack",
         "rage",
         "bide",
         "lick",
         "waterfall",
         "clamp",
         "skullbash",
         "constrict",
         "highjumpkick",
         "leechlife",
         "dizzypunch",
         "crabhammer",
         "furyswipes",
         "hyperfang",
         "superfang",
         "slash",
         "struggle",
         "triplekick",
         "thief",
         "flamewheel",
         "flail",
         "reversal",
         "machpunch",
         "feintattack",
         "outrage",
         "rollout",
         "falseswipe",
         "spark",
         "furycutter",
         "steelwing",
         "return",
         "frustration",
         "dynamicpunch",
         "megahorn",
         "pursuit",
         "rapidspin",
         "irontail",
         "metalclaw",
         "vitalthrow",
         "crosschop",
         "crunch",
         "extremespeed",
         "rocksmash",
         "fakeout",
         "facade",
         "focuspunch",
         "smellingsalts",
         "superpower",
         "revenge",
         "brickbreak",
         "knockoff",
         "endeavor",
         "dive",
         "armthrust",
         "blazekick",
         "iceball",
         "needlearn",
         "poisonfang",
         "crushclaw",
         "meteormash",
         "astonish",
         "shadowpunch",
         "skyuppercut",
         "aerialace",
         "dragonclaw",
         "bounce",
         "poisontail",
         "covet",
         "volttackle",
         "leafblade",
         "wakeupslap",
         "hammerarm",
         "gyroball",
         "pluck",
         "uturn",
         "closecombat",
         "payback",
         "assurance",
         "trumpcard",
         "wringout",
         "punishment",
         "lastresort",
         "suckerpunch",
         "flareblitz",
         "forcepalm",
         "poisonjab",
         "nightslash",
         "aquatail",
         "xscissor",
         "dragonrush",
         "drainpunch",
         "bravebird",
         "gigaimpact",
         "bulletpunch",
         "avalanche",
         "shadowclaw",
         "thunderfang",
         "icefang",
         "firefang",
         "shadowsneak",
         "zenheadbutt",
         "rockclimb",
         "powerwhip",
         "crosspoison",
         "ironhead",
         "grassknot",
         "bugbite",
         "woodhammer",
         "aquajet",
         "headsmash",
         "doublehit",
         "crushgrip",
         "shadowforce",
         "stormthrow",
         "heavyslam",
         "flamecharge",
         "lowsweep",
         "foulplay",
         "chipaway",
         "skydrop",
         "circlethrow",
         "acrobatics",
         "retaliate",
         "dragontail",
         "wildcharge",
         "drillrun",
         "dualchop",
         "heartstamp",
         "hornleech",
         "sacredsword",
         "razorshell",
         "heatcrash",
         "steamroller",
         "tailslap",
         "headcharge",
         "geargrind",
         "boltstrike",
         "vcreate",
         "flyingpress",
         "fellstinger",
         "phantomforce",
         "drainingkiss",
         "playrough",
         "nuzzle",
         "holdback",
         "infestation",
         "poweruppunch",
         "dragonascent",
         "catastropika",
         "firstimpression",
         "darkestlariat",
         "icehammer",
         "highhorsepower",
         "solarblade",
         "throatchop",
         "anchorshot",
         "lunge",
         "firelash",
         "powertrip",
         "smartstrike",
         "tropkick",
         "dragonhammer",
         "brutalswing",
         "maliciousmoonsault",
         "soulstealing7starstrike",
         "pulverizingpancake",
         "psychicfangs",
         "stompingtantrum",
         "accelerock",
         "liquidation",
         "spectralthief",
         "sunsteelstrike",
         "zingzap",
         "multiattack",
         "plasmafists",
         "searingsunrazesmash",
         "letssnuggleforever",
         "zippyzap",
         "floatyfall",
         "sizzlyslide",
         "veeveevolley",
         "doubleironbash",
         "jawlock",
         "boltbeak",
         "fishiousrend",
         "bodypress",
         "snaptrap",
         "behemothblade",
         "behemothbash",
         "breakingswipe",
         "branchpoke",
         "spiritbreak",
         "falsesurrender",
         "steelroller",
         "grassyglide",
         "skittersmack",
         "lashout",
         "flipturn",
         "tripleaxel",
         "dualwingbeat",
         "wickedblow",
         "surgingstrikes",
         "thunderouskick",
         "direclaw",
         "psyshieldbash",
         "stoneaxe",
         "wavecrash",
         "headlongrush",
         "ceaselessedge",
         "axekick",
         "jetpunch",
         "spinout",
         "populationbomb",
         "icespinner",
         "glaiverush",
         "tripledive",
         "mortalspin",
         "kowtowcleave",
         "aquastep",
         "ragingbull",
         "psyblade",
         "collisioncourse",
         "electrodrift",
         "pounce",
         "trailblaze",
         "hyperdrill",
         "ragefist",
         "bitterblade",
         "doubleshock",
         "comeuppance",
         "mightycleave",
         "hardpress",
         "temperflare",
         "supercellslam",
         "upperhand",
         "shadowblitz",
         "shadowbreak",
         "shadowend",
         "shadowrush"
      )
   );
   private static final List<String> bitingMoves = new ArrayList<>(
      List.of("bite", "crunch", "firefang", "fishiousrend", "hyperfang", "icefang", "jawlock", "poisonfang", "psychicfangs", "thunderfang")
   );
   private static final List<String> consectutiveMoves = new ArrayList<>(
      List.of("iceball", "outrage", "petaldance", "ragingfury", "rollout", "thrash", "uproar")
   );
   private static final List<String> recoilMoves = new ArrayList<>(
      List.of(
         "axekick",
         "bravebird",
         "doubleedge",
         "flareblitz",
         "headcharge",
         "headsmash",
         "highjumpkick",
         "jumpkick",
         "submission",
         "supercellslam",
         "takedown",
         "wavecrash",
         "wildcharge",
         "lightofruin",
         "volttackle",
         "woodhammer"
      )
   );
   private static final List<String> slicingMoves = new ArrayList<>(
      List.of(
         "cut",
         "razorleaf",
         "slash",
         "furycutter",
         "aircutter",
         "aerialace",
         "leafblade",
         "nightslash",
         "airslash",
         "xscissor",
         "psychocut",
         "crosspoison",
         "sacredsword",
         "razorshell",
         "secretsword",
         "solarblade",
         "behemothblade",
         "stoneaxe",
         "ceaselessedge",
         "populationbomb",
         "kowtowcleave",
         "psyblade",
         "bitterblade",
         "aquacutter",
         "mightycleave",
         "tachyoncutter"
      )
   );
   private static final List<String> goodAsGold = new ArrayList<>(
      List.of(
         "attract",
         "disable",
         "encore",
         "taunt",
         "torment",
         "imprison",
         "meanlook",
         "block",
         "spiderweb",
         "healblock",
         "magicroom",
         "wonderroom",
         "gravity",
         "telekinesis",
         "leechseed",
         "painsplit",
         "confuseray",
         "supersonic",
         "swagger",
         "flatter",
         "tearfullook",
         "captivate",
         "worryseed",
         "gastroacid",
         "skillswap",
         "roleplay",
         "entrainment",
         "simplebeam",
         "corrosivegas",
         "trick",
         "switcheroo"
      )
   );
   private static final List<String> nonVolatileMoves = new ArrayList<>(
      List.of(
         "willowisp",
         "toxic",
         "poisonpowder",
         "thunderwave",
         "stunspore",
         "glare",
         "spore",
         "sleeppowder",
         "hypnosis",
         "lovelykiss",
         "sing",
         "grasswhistle",
         "darkvoid",
         "yawn"
      )
   );
   private static final List<String> multiHit2to5 = new ArrayList<>(
      List.of(
         "armthrust",
         "barrage",
         "bonerush",
         "bulletseed",
         "cometpunch",
         "doubleslap",
         "furyattack",
         "furyswipes",
         "iciclespear",
         "pinmissile",
         "rockblast",
         "scaleshot",
         "spikecannon",
         "tailslap"
      )
   );
   private static final List<String> multiHit2 = new ArrayList<>(
      List.of(
         "bonemerang",
         "doublehit",
         "doubleironbash",
         "doublekick",
         "dragondarts",
         "dualchop",
         "dualwingbeat",
         "geargrind",
         "tachyoncutter",
         "twinbeam",
         "twineedle"
      )
   );
   private static final List<String> multiHit3 = new ArrayList<>(List.of("surgingstrikes", "tripledive", "watershuriken"));
   private static final List<String> explosionMoves = new ArrayList<>(List.of("explosion", "mistyexplosion", "mindblown", "selfdestruct"));
   private static final List<String> sporePowderMoves = new ArrayList<>(
      List.of("poisonpowder", "stunspore", "sleeppowder", "spore", "cottonspore", "ragepowder", "powder", "magicpowder")
   );
   private static final List<String> abilitiesThatCannotBeSuppressed = new ArrayList<>(
      List.of(
         "neutralizinggas",
         "multitype",
         "zenmode",
         "stancechange",
         "powerconstruct",
         "schooling",
         "rkssystem",
         "shieldsdown",
         "battlebond",
         "comatose",
         "disguise",
         "gulpmissile",
         "iceface",
         "asone",
         "terashift"
      )
   );
   public static final List<String> allyTargetingMoves = new ArrayList<>(List.of("helpinghand", "aromaticmist", "lifedew"));
   public static final List<String> ballAndBulletMoves = new ArrayList<>(
      List.of(
         "acidspray",
         "aurasphere",
         "barrage",
         "beakblast",
         "bulletseed",
         "eggbomb",
         "electroball",
         "energyball",
         "focusblast",
         "gyroball",
         "iceball",
         "magnetbomb",
         "mistball",
         "mudbomb",
         "octazooka",
         "pollenpuff",
         "pyroball",
         "rockblast",
         "rockwrecker",
         "searingshot",
         "seedbomb",
         "syrupbomb",
         "shadowball",
         "sludgebomb",
         "weatherball",
         "zapcannon"
      )
   );
   public static final List<String> fixedDamagingMoves = new ArrayList<>(
      List.of(
         "superfang",
         "dragonrage",
         "nightshade",
         "seismictoss",
         "flail",
         "reversal",
         "return",
         "frustration",
         "sonicboom",
         "endeavor",
         "lowkick",
         "grassknot",
         "gyroball",
         "trumpcard",
         "crushgrip",
         "wringout",
         "punishment"
      )
   );

   public static List<String> getPriorityDamageMoves() {
      return priorityDamageMoves;
   }

   public static List<String> getAbilityStatBooster() {
      return abilityStatBooster;
   }

   public static List<String> getHighCriticalMoves() {
      return highCriticalMoves;
   }

   public static List<String> getTrapMoves() {
      return trapMoves;
   }

   public static List<String> getSpeedReductionMoves() {
      return speedReductionMoves;
   }

   public static List<String> getPhysicalAttackReductionMoves() {
      return physicalAttackReductionMoves;
   }

   public static List<String> getSpecialAttackReductionMoves() {
      return specialAttackReductionMoves;
   }

   public static List<String> getGeneralSetupMoves() {
      return generalSetupMoves;
   }

   public static List<String> getIgnoreStatDropAbilities() {
      return ignoreStatDropAbilities;
   }

   public static List<String> getSpecialFunctionMoves() {
      return specialFunctionMoves;
   }

   public static List<String> getSoundMoves() {
      return soundMoves;
   }

   public static List<String> getFlinchMoves() {
      return flinchMoves;
   }

   public static List<String> getThawingMoves() {
      return thawingMoves;
   }

   public static List<String> getRechargeMoves() {
      return rechargeMoves;
   }

   public static List<String> getRecoveryMoves() {
      return recoveryMoves;
   }

   public static List<String> getMegaStones() {
      return megaStones;
   }

   public static List<String> getIgnoreDamageMoves() {
      return ignoreDamageMoves;
   }

   public static List<String> getIgnoreSleepAbilities() {
      return ignoreSleepAbilities;
   }

   public static List<String> getStatusMoves() {
      return statusMoves;
   }

   public static List<String> getCriticalMoves() {
      return criticalMoves;
   }

   public static List<String> getPunchingMoves() {
      return punchingMoves;
   }

   public static List<String> getDirectDamageMoves() {
      return directDamageMoves;
   }

   public static List<String> getWindMoves() {
      return windMoves;
   }

   public static List<String> getContactMoves() {
      return contactMoves;
   }

   public static List<String> getBitingMoves() {
      return bitingMoves;
   }

   public static List<String> getConsectutiveMoves() {
      return consectutiveMoves;
   }

   public static List<String> getRecoilMoves() {
      return recoilMoves;
   }

   public static List<String> getSlicingMoves() {
      return slicingMoves;
   }

   public static List<String> getGoodAsGold() {
      return goodAsGold;
   }

   public static List<String> getNonVolatileMoves() {
      return nonVolatileMoves;
   }

   public static List<String> getMultiHit2to5() {
      return multiHit2to5;
   }

   public static List<String> getMultiHit2() {
      return multiHit2;
   }

   public static List<String> getMultiHit3() {
      return multiHit3;
   }

   public static List<String> getExplosionMoves() {
      return explosionMoves;
   }

   public static List<String> getSleepMoves() {
      return sleepMoves;
   }

   public static List<String> getSporePowderMoves() {
      return sporePowderMoves;
   }

   public static List<String> getAbilitiesThatCannotBeSuppressed() {
      return abilitiesThatCannotBeSuppressed;
   }

   public static List<String> getAllyTargetingMoves() {
      return allyTargetingMoves;
   }

   public static List<String> getBallAndBulletMoves() {
      return ballAndBulletMoves;
   }

   public static final List<String> getFixedDamagingMoves() {
      return fixedDamagingMoves;
   }
}
