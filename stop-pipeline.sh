#!/bin/bash
# Stops anything left over from a previous run of start-pipeline.sh.
cd "$(dirname "$0")"
here=$(pwd)
ours() {   # only touch a process that was started from this folder (pid files can go stale)
  [ -d /proc/$1 ] || { kill -0 "$1" 2>/dev/null; return; }
  case "$(readlink /proc/$1/cwd)" in "$here"*) return 0 ;; *) return 1 ;; esac
}
for f in run/streams.pid run/connect.pid run/wildfly.pid run/kafka.pid run/zookeeper.pid run/postgres.pid; do
  [ -f "$f" ] || continue
  pid=$(cat "$f"); rm -f "$f"
  ours "$pid" || continue
  pkill -TERM -P "$pid" 2>/dev/null; kill "$pid" 2>/dev/null
  i=0; while { pgrep -P "$pid" || kill -0 "$pid"; } >/dev/null 2>&1; do
    [ $i -ge 100 ] && { pkill -KILL -P "$pid"; kill -9 "$pid"; break; } 2>/dev/null
    sleep 0.1; i=$((i+1))
  done
done
true
