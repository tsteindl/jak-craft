#!/usr/bin/env bash
#
# Pulls the newest jak-craft build published by the GitHub "server-latest" release
# and, if it changed, swaps it into the server's mods/ folder and restarts Minecraft.
#
# Runs on the Minecraft server itself (see deploy/jakcraft-update.timer). Downloads a
# PUBLIC release asset over plain HTTPS, so it needs no GitHub token or SSH key.
#
# Configure via environment (overridable in jakcraft-update.service), or edit defaults:
set -euo pipefail

# --- CONFIG -----------------------------------------------------------------
# Public URL of the rolling release asset. Change the owner/repo if you fork.
RELEASE_URL="${JAKCRAFT_RELEASE_URL:-https://github.com/tsteindl/jak-craft/releases/download/server-latest/jakcraft-server.jar}"
# The server's mods directory (where forge loads mods from).
MODS_DIR="${JAKCRAFT_MODS_DIR:-/home/minecraft/server/mods}"
# Command used to restart the Minecraft server. A systemd service is recommended
# because its ExecStop can send a graceful "stop" to the server console.
RESTART_CMD="${JAKCRAFT_RESTART_CMD:-systemctl restart minecraft}"
# Where to remember the last deployed build's checksum.
STATE_DIR="${JAKCRAFT_STATE_DIR:-/home/minecraft/.jakcraft-deploy}"
# ----------------------------------------------------------------------------

mkdir -p "$STATE_DIR"
tmp="$(mktemp /tmp/jakcraft-server.XXXXXX.jar)"
trap 'rm -f "$tmp"' EXIT

# Download the latest jar. -f => fail on HTTP error, so a broken release won't nuke the mod.
if ! curl -fsSL -o "$tmp" "$RELEASE_URL"; then
  echo "$(date -Is) could not download $RELEASE_URL - leaving current jar in place" >&2
  exit 0
fi

# Sanity check: a real jar is a zip and more than a few KB.
if ! unzip -l "$tmp" >/dev/null 2>&1 || [ "$(stat -c%s "$tmp")" -lt 10000 ]; then
  echo "$(date -Is) downloaded file is not a valid jar - skipping" >&2
  exit 0
fi

new_sum="$(sha256sum "$tmp" | awk '{print $1}')"
old_sum="$(cat "$STATE_DIR/last.sha256" 2>/dev/null || echo none)"

if [ "$new_sum" = "$old_sum" ]; then
  echo "$(date -Is) no change ($new_sum) - nothing to do"
  exit 0
fi

echo "$(date -Is) new build $new_sum - deploying"
rm -f "$MODS_DIR"/jakcraft-*.jar
install -m 0644 "$tmp" "$MODS_DIR/jakcraft-server.jar"
echo "$new_sum" > "$STATE_DIR/last.sha256"

echo "$(date -Is) restarting Minecraft: $RESTART_CMD"
eval "$RESTART_CMD"
echo "$(date -Is) deploy complete"
