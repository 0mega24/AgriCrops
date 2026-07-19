# shell.nix — FHS dev environment for building Agricrops on NixOS.
#
# Why an FHS sandbox: this is a Forge 1.12.2 project built with ForgeGradle 2.x,
# which shells out to prebuilt native binaries (the MCP decompiler/deobf toolchain
# and, at runtime, LWJGL/GLFW/OpenAL natives) that assume a standard FHS layout.
# NixOS does not provide that layout, so those binaries fail to find their loader
# and libraries. buildFHSUserEnv drops you into a sandbox where they behave as they
# would on a "normal" distro. (See also crop-mod-getting-started.md §1 — this is the
# `buildFHSUserEnv` route; the alternative is enabling `programs.nix-ld` system-wide.)
#
# Usage:
#   nix-shell                              # enter the FHS env (bash)
#   ./gradlew setupDecompWorkspace         # one-time FG2 setup (decompiles Minecraft)
#   ./gradlew build                        # build the mod jar
#   ./gradlew genIntellijRuns              # generate IntelliJ runClient/runServer configs
#
# Forge 1.12.2 requires Java 8 SPECIFICALLY (not a newer LTS). If `pkgs.jdk8` is not
# available on your channel, swap in `pkgs.temurin-bin-8` or `pkgs.zulu8`.

{ pkgs ? import <nixpkgs> { } }:

(pkgs.buildFHSUserEnv {
  name = "agricrops-dev";

  targetPkgs = pkgs: (with pkgs; [
    jdk8            # Forge 1.12.2 toolchain — Java 8 only
    git             # gradlew + submodules

    # Native libraries ForgeGradle / LWJGL2 / GLFW expect to dlopen at runtime:
    zlib
    stdenv.cc.cc.lib
    libGL
    glfw
    openal
    alsa-lib
    xorg.libX11
    xorg.libXext
    xorg.libXcursor
    xorg.libXrandr
    xorg.libXxf86vm
    xorg.libXi
    xorg.libXtst
  ]);

  runScript = "bash";

  profile = ''
    export JAVA_HOME="${pkgs.jdk8}"
    export PATH="$JAVA_HOME/bin:$PATH"
    echo "agricrops FHS dev env ready — $(java -version 2>&1 | head -n1)"
  '';
}).env
