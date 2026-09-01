#!/usr/bin/env bash
set -euo pipefail
BASE=${1:?Usage: BuildRc27.sh <rc26.jar> <output.jar>}
OUT=${2:?Usage: BuildRc27.sh <rc26.jar> <output.jar>}
ROOT=$(cd "$(dirname "$0")" && pwd)
rm -rf "$ROOT/stubclasses27" "$ROOT/rc27classes" "$ROOT/testclasses27"
mkdir -p "$ROOT/stubclasses27" "$ROOT/rc27classes" "$ROOT/testclasses27"

javac --release 21 -d "$ROOT/stubclasses27" $(find "$ROOT/stubs" -name '*.java')
javac --release 21 -cp "$ROOT/stubclasses27:$BASE" -d "$ROOT/rc27classes" \
  "$ROOT/src/net/crulim/kantonpcs/client/gui/RoamingPokemonVariantSupport.java" \
  "$ROOT/src/net/crulim/kantonpcs/client/gui/RoamingPokemonVariantScreen.java" \
  "$ROOT/src/net/crulim/kantonpcs/client/gui/RoamingPokemonEditorScreen.java" \
  "$ROOT/src/net/crulim/kantonpcs/roaming/RoamingPokemonManager.java"

javac --release 21 -cp "$ROOT/stubclasses27:$ROOT/rc27classes" -d "$ROOT/testclasses27" \
  "$ROOT/tools/Rc27VariantSupportTest.java"
java -cp "$ROOT/stubclasses27:$ROOT/rc27classes:$ROOT/testclasses27" Rc27VariantSupportTest

python3 - "$BASE" "$OUT" "$ROOT/rc27classes" <<'PY'
import sys,zipfile,os
base,out,classes=sys.argv[1:]
targets={
'net/crulim/kantonpcs/client/gui/RoamingPokemonEditorScreen.class',
'net/crulim/kantonpcs/client/gui/RoamingPokemonVariantSupport.class',
'net/crulim/kantonpcs/client/gui/RoamingPokemonVariantSupport$FormChoice.class',
'net/crulim/kantonpcs/client/gui/RoamingPokemonVariantScreen.class',
'net/crulim/kantonpcs/client/gui/RoamingPokemonVariantScreen$Selection.class',
'net/crulim/kantonpcs/roaming/RoamingPokemonManager.class',
'net/crulim/kantonpcs/roaming/RoamingPokemonManager$Region.class',
'net/crulim/kantonpcs/roaming/RoamingPokemonManager$Roamer.class'}
with zipfile.ZipFile(base) as zin, zipfile.ZipFile(out,'w') as zout:
    seen=set()
    for info in zin.infolist():
        data=open(os.path.join(classes,info.filename),'rb').read() if info.filename in targets else zin.read(info.filename)
        if info.filename in targets: seen.add(info.filename)
        if info.filename in ('META-INF/neoforge.mods.toml','META-INF/MANIFEST.MF'):
            data=data.replace(b'1.0.5-neoforge-port.6-rc26',b'1.0.5-neoforge-port.6-rc27')
        zi=zipfile.ZipInfo(info.filename,info.date_time); zi.compress_type=info.compress_type
        zi.comment=info.comment; zi.extra=info.extra; zi.internal_attr=info.internal_attr; zi.external_attr=info.external_attr
        zi.create_system=info.create_system; zi.flag_bits=info.flag_bits
        zout.writestr(zi,data)
    for name in sorted(targets-seen):
        zout.write(os.path.join(classes,name),name,compress_type=zipfile.ZIP_DEFLATED)
PY
unzip -t "$OUT"
sha256sum "$OUT"
