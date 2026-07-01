# Automatic deployment to the Minecraft server

On every push to `main`, GitHub builds the mod jar and publishes it. The Minecraft
server automatically picks up the new jar and restarts with it — no manual copying.

## How it works

```
git push main
      │
      ▼
GitHub Actions (deploy.yml, hosted runner)
   builds jakcraft-server.jar
   publishes it to the "server-latest" GitHub Release
      │   (public asset, plain HTTPS)
      ▼
Minecraft server (10.0.0.13)
   systemd timer polls the release every ~1 min
   → if the jar changed: swap into mods/ and restart Minecraft
```

### Why not "GitHub SSHes into the server on push"?

`10.0.0.13` is a **private LAN address**. GitHub's hosted runners are on the public
internet and cannot route to a `10.x.x.x` host — no SSH key can fix that (it's a
networking problem, not an auth one). So instead the **server pulls** from a public
release URL. Benefits:

- Works through the home router with **no port forwarding / inbound access**.
- **No secrets** anywhere — the repo is public, and public release assets download
  without a token or SSH key. (This is the concern you raised about everything being
  visible on GitHub: there is simply nothing sensitive to leak.)
- Safe on a public repo (no self-hosted runner executing pushed code on your machine).

Trade-off: deployment lands within the poll interval (~1 minute) rather than instantly.
See [Alternatives](#alternatives-instant-deploy) if you want instant.

## One-time server setup

SSH into the server (`ssh minecraft@10.0.0.13`) and run:

```bash
# 1. Put the deploy script on the server
mkdir -p ~/deploy
curl -fsSL -o ~/deploy/update-jakcraft.sh \
  https://raw.githubusercontent.com/tsteindl/jak-craft/main/deploy/update-jakcraft.sh
chmod +x ~/deploy/update-jakcraft.sh

# 2. Tell it where your mods/ folder is and how to restart the server.
#    Edit the two Environment= lines in the service file below to match your setup.
```

Then install the systemd units (needs sudo once):

```bash
# From a checkout of the repo, or download each file from deploy/ in the repo:
sudo cp deploy/jakcraft-update.service /etc/systemd/system/
sudo cp deploy/jakcraft-update.timer   /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now jakcraft-update.timer
```

Check it:

```bash
systemctl list-timers jakcraft-update.timer     # shows next run
sudo systemctl start jakcraft-update.service     # force a poll now
journalctl -u jakcraft-update.service -n 30      # see what it did
```

### Configuration

`deploy/update-jakcraft.sh` reads these (defaults in parentheses); set them via the
`Environment=` lines in `jakcraft-update.service`:

| Variable | Meaning | Default |
|----------|---------|---------|
| `JAKCRAFT_MODS_DIR` | server mods folder | `/home/minecraft/server/mods` |
| `JAKCRAFT_RESTART_CMD` | how to restart Minecraft | `systemctl restart minecraft` |
| `JAKCRAFT_RELEASE_URL` | release asset URL | the repo's `server-latest` asset |
| `JAKCRAFT_STATE_DIR` | stores last-deployed checksum | `/home/minecraft/.jakcraft-deploy` |

**Find your real mods dir** if unsure: `find /home/minecraft -maxdepth 4 -type d -name mods`.

### Restarting Minecraft

The default restart command assumes the server runs under systemd as `minecraft`. If it
doesn't yet, `deploy/minecraft.service.example` is a ready-to-adapt unit with a **graceful**
stop (sends `stop` to the console so worlds save cleanly). If instead you run it in
`screen`/`tmux`, set e.g.
`JAKCRAFT_RESTART_CMD='screen -S mc -X stuff "stop^M"'` and let your supervisor relaunch it.

If the `minecraft` user needs sudo to restart the service, add a narrow sudoers rule:

```
minecraft ALL=(root) NOPASSWD: /bin/systemctl restart minecraft
```

(and set `JAKCRAFT_RESTART_CMD="sudo systemctl restart minecraft"`).

## First run / testing

1. Merge these files to `main`. The `deploy.yml` workflow runs and creates the
   **server-latest** release (check the repo's *Releases* and *Actions* tabs).
2. On the server, force a poll: `sudo systemctl start jakcraft-update.service`.
3. Confirm the new `jakcraft-server.jar` is in the mods folder and the server restarted.
4. You can also trigger a build manually from the **Actions** tab (workflow_dispatch).

> Note: the WorldEdit jar and the other mods already in the instance are untouched —
> this only manages `jakcraft-*.jar`.

## Alternatives (instant deploy)

If the ~1-minute poll delay bothers you, two options give push-to-restart instantly:

- **Tailscale + SSH from Actions.** You already have Tailscale installed. Put the server
  on your tailnet, add a Tailscale auth key + SSH deploy key as GitHub repo secrets, and
  have the workflow `tailscale up` then `ssh` in to deploy. Instant, but adds secrets to a
  public repo (guard them by never running deploy on `pull_request`).
- **Self-hosted runner on the server.** Install a GitHub Actions runner on `10.0.0.13`;
  the workflow then builds + restarts locally on push. Instant and secretless, but on a
  **public** repo a self-hosted runner can be risky (only ever trigger it on `push` to
  `main`, never on `pull_request`, and require approval for outside collaborators).

The pull-based approach above is the default because it needs no secrets and no inbound
access. Say the word if you'd rather set up one of these instead.
